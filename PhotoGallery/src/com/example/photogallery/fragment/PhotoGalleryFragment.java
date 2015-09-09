package com.example.photogallery.fragment;

import java.io.IOException;
import java.util.ArrayList;

import com.example.photogallery.R;
import com.example.photogallery.handler.ThumbnailDowmloader;
import com.example.photogallery.handler.ThumbnailDowmloader.Listener;
import com.example.photogallery.httputils.FlickrFetcher;
import com.example.photogallery.model.GalleryItem;

import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

public class PhotoGalleryFragment extends Fragment {
	
	private static final String TAG = "PhotoGalleryFragment";
	
	private GridView mGridView;
	private ArrayList<GalleryItem> mItems;
	private ThumbnailDowmloader<ImageView> mThumbnailThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
		
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
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		
		mGridView = (GridView) view.findViewById(R.id.gridview);
		setupAdapter();
		return view;
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
			return new FlickrFetcher().fetchItems();
		}
		
		@Override
		protected void onPostExecute(ArrayList<GalleryItem> items) {
			mItems = items;
			setupAdapter();
		}
		
	}
}
