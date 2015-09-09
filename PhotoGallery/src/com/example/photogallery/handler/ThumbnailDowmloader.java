package com.example.photogallery.handler;

import android.os.HandlerThread;
import android.util.Log;

public class ThumbnailDowmloader<Token> extends HandlerThread {
	
	private static final String TAG = "ThumbnailDowmloader";

	public ThumbnailDowmloader() {
		super(TAG);
	}

	public void queryThumbnail(Token token,String url){
		Log.d(TAG, "Get an url :"+url);
	}

}
