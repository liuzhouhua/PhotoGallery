package com.example.photogallery.activity;

import com.example.photogallery.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public abstract class SingleFragmentActivity extends FragmentActivity {

	private static final String TAG = "SingleFragmentActivity";
	
	protected abstract Fragment createFragment();
	
	public int getLayoutResource(){
		return R.layout.activity_fragment;
	}
	
	@Override
	protected void onCreate(Bundle saveInstnce) {
		super.onCreate(saveInstnce);
		setContentView(getLayoutResource());
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
		
		if(fragment==null){
			fragment = createFragment();
			ft.add(R.id.fragmentContainer, fragment).commit();
		}
	}

}
