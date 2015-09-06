package com.example.photogallery.activity;

import com.example.photogallery.fragment.PhotoGalleryFragment;

import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity{


	@Override
	protected Fragment createFragment() {
		return new PhotoGalleryFragment();
	}

}
