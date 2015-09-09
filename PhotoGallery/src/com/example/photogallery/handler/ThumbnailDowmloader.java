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
	
	//缓存
	LruCache<String, Bitmap> mLruCache;
	
	private Bitmap bitmap=null;

	public ThumbnailDowmloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
		LruCacheUtils();
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
			
			if(getBitmapFromLruCache(url)==null){
				byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
				bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				Log.i(TAG, "Bitmap created");
				addBitmapToLruCache(url, bitmap);
			}else{
				bitmap = getBitmapFromLruCache(url);
			}
			
			
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
			Log.e(TAG, "Error downloading image", e);//
		}
	}

	public void clearQueue(){
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requsetMap.clear();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void LruCacheUtils(){
		int MAXMEMONRY = (int) (Runtime.getRuntime().maxMemory()/1024);
		if(mLruCache==null){
			mLruCache = new LruCache<String, Bitmap>(MAXMEMONRY/8){
				@Override
				protected int sizeOf(String key, Bitmap value) {
					// 重写此方法来衡量每张图片的大小，默认返回图片数量。
					return super.sizeOf(key, value);
				}
				
				@Override
				protected void entryRemoved(boolean evicted, String key,
						Bitmap oldValue, Bitmap newValue) {
					super.entryRemoved(evicted, key, oldValue, newValue);
					Log.v("tag", "hard cache is full , push to soft cache");
				}
			};
		}
	}
	
	/*
	 * 清空缓存
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void clearCache(){
		if(mLruCache!=null){
			if(mLruCache.size()>0){
				Log.d("CacheUtils","mLruCache.size() " + mLruCache.size());
				mLruCache.evictAll();
				Log.d("CacheUtils","mLruCache.size() " + mLruCache.size());
			}
			mLruCache = null;
		}
	}
	
	/*
	 * 添加图片到缓存
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public synchronized void addBitmapToLruCache(String url,Bitmap bitmap){
		if(mLruCache.get(url)==null){
			if(url!=null && bitmap!=null){
				mLruCache.put(url, bitmap);
			}
		}else{
			Log.w(TAG, "the res is aready exits");
		}
	}
	
	/*
	 * 从缓存中取图片
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public synchronized Bitmap getBitmapFromLruCache(String url){
		Bitmap bitmap = mLruCache.get(url);
		if(bitmap!=null){
			return bitmap;
		}
		return null;
	}
	
	/*
	 * 从缓存中移除
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public synchronized void removeCache(String url){
		if(url!=null){
			if(mLruCache!=null){
				Bitmap bitmap = mLruCache.get(url);
				if(bitmap!=null){
					bitmap.recycle();
				}
			}
		}
	}
}
