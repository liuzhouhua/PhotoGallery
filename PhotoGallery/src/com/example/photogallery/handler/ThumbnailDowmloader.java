package com.example.photogallery.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.example.photogallery.httputils.FlickrFetcher;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

public class ThumbnailDowmloader<Token> extends HandlerThread {
	
	private static final String TAG = "ThumbnailDowmloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	
	Handler mHandler;
	Handler mResponseHandler;
	Listener<Token> mListener;
	//同步HashMap
	Map<Token, String> requsetMap = Collections.synchronizedMap(new HashMap<Token, String>());
	
	private Bitmap bitmap=null;

	public ThumbnailDowmloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
	}

	public interface Listener<Token>{
		void onThumbnailDownloaded(Token token,Bitmap thumbnail);
	}
	
	public void setListener(Listener<Token> listener){
		mListener = listener;
	}
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==MESSAGE_DOWNLOAD){
					@SuppressWarnings("unchecked")
					Token token = (Token) msg.obj;
					Log.i(TAG, "Got a request for url :"+requsetMap.get(token));
					handleRequest(token);
				}
			}
		};
	}
	
	public void queryThumbnail(Token token,String url){
		Log.d(TAG, "Get an url :"+url);
		requsetMap.put(token, url);
		
		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
	}
	
	private void handleRequest(final Token token){
		try{
			final String url = requsetMap.get(token);
			if(url==null){
				return;
			}
			
			byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
			bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
			Log.i(TAG, "Bitmap created");
			
			
			//返回订制的message给主线程
			mResponseHandler.post(new Runnable() {
				
				@Override
				public void run() {
					if(requsetMap.get(token)!=url){
						return;
					}
					requsetMap.remove(token);
					mListener.onThumbnailDownloaded(token, bitmap);
				}
			});
		}catch(IOException e){
			Log.e(TAG, "Error downloading image", e);
		}
	}

	public void clearQueue(){
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requsetMap.clear();
	}
}
