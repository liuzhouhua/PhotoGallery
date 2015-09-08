package com.example.photogallery.fragment;

import java.io.IOException;

import com.example.photogallery.R;
import com.example.photogallery.httputils.FlickrFetcher;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class PhotoGalleryFragment extends Fragment {
	
	private static final String TAG = "PhotoGalleryFragment";
	
	private GridView mGridView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		
		mGridView = (GridView) view.findViewById(R.id.gridview);
		return view;
	}
	
	
	public class FetchItemsTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			new FlickrFetcher().fetchItems();
			return null;
		}
		
	}
}
