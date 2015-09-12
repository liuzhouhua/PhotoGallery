package com.example.photogallery.fragment;

import java.io.IOException;
import java.util.ArrayList;

import com.example.photogallery.R;
import com.example.photogallery.handler.ThumbnailDowmloader;
import com.example.photogallery.handler.ThumbnailDowmloader.Listener;
import com.example.photogallery.httputils.FlickrFetcher;
import com.example.photogallery.model.GalleryItem;
import com.example.photogallery.service.PollService;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SearchView;

public class PhotoGalleryFragment extends Fragment {
	
	private static final String TAG = "PhotoGalleryFragment";
	
	private GridView mGridView;
	private ArrayList<GalleryItem> mItems;
	private ThumbnailDowmloader<ImageView> mThumbnailThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		updateItems();
		
		mThumbnailThread = new ThumbnailDowmloader<ImageView>(new Handler());
		mThumbnailThread.setListener(new Listener<ImageView>() {

			@Override
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				if(isVisible()){
					imageView.setImageBitmap(thumbnail);
				}
			}
			
		});
		mThumbnailThread.start();
		mThumbnailThread.getLooper();
		Log.i(TAG, "Background thread is started");
	}
	
	public void updateItems(){
		new FetchItemsTask().execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		
		mGridView = (GridView) view.findViewById(R.id.gridview);
		setupAdapter();
		return view;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_gallery, menu);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			//Pull out the searchView
			MenuItem searchItem = menu.findItem(R.id.menu_item_search);
			SearchView searchView = (SearchView) searchItem.getActionView();
			
			//Get the data from our searchable.xml as a searchAbleInfo
			SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
			ComponentName name = getActivity().getComponentName();
			SearchableInfo searchableInfo = searchManager.getSearchableInfo(name);
			
			if(searchView!=null){
				searchView.setSearchableInfo(searchableInfo);
			}
		}
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menu_item_search:
			getActivity().onSearchRequested();
			return true;
		case R.id.menu_item_clear:
			PreferenceManager.getDefaultSharedPreferences(getActivity())
				.edit()
				.putString(FlickrFetcher.PREF_SEARCH_QUERY, null)
				.commit();
			updateItems();
			return true;
		case R.id.menu_item_toggle_polling:
			boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
			PollService.setServiceAlam(getActivity(), shouldStartAlarm);
			
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
				getActivity().invalidateOptionsMenu();
			}
			
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
		if(PollService.isServiceAlarmOn(getActivity())){
			toggleItem.setTitle(R.string.stop_polling);
		}else{
			toggleItem.setTitle(R.string.start_polling);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailThread.quit();
		Log.i(TAG, "Background thread destroyed");
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailThread.clearQueue();
	}
	
	private void setupAdapter() {
		if(getActivity()==null || mGridView==null){
			return;
		}
		
		if(mItems!=null){
			mGridView.setAdapter(new GalleryItemAdapter(mItems));
		}else{
			mGridView.setAdapter(null);
		}
	}

	public class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
		
		public GalleryItemAdapter(ArrayList<GalleryItem> items){
			super(getActivity(), 0, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
			}
			
			ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
			imageView.setImageResource(R.drawable.brian_up_close);
			
			GalleryItem item = getItem(position);
			mThumbnailThread.queryThumbnail(imageView, item.getmUrl());
			
			return convertView;
		}
	}

	public class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>>{

		@Override
		protected ArrayList<GalleryItem> doInBackground(Void... params) {
			
			Activity activity = getActivity();
			if(activity==null){
				return new ArrayList<GalleryItem>();
			}
			
			String query = PreferenceManager.getDefaultSharedPreferences(activity).getString(FlickrFetcher.PREF_SEARCH_QUERY, null);
			
			if(query!=null){
				return new FlickrFetcher().search(query);
			}else{
				return new FlickrFetcher().fetchItems();
			}
		}
		
		@Override
		protected void onPostExecute(ArrayList<GalleryItem> items) {
			mItems = items;
			setupAdapter();
		}
		
	}
}
