package com.example.photogallery.httputils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.Uri;
import android.util.Log;

public class FlickrFetcher {
	
	private static final String TAG = "FlickrFetcher";
	
	//访问端点
	private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
	//API Key
	private static final String API_KEY = "a94fbb36986ad8cd98867d5a3090a5b5";
	//方法名
	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
	private static final String PASSCODE = "f77fa3b0d3513fdb";
	//一个值为url_s的extra参数
	private static final String PARAM_EXTRAS = "extras";
	private static final String EXTRA_SMALL_URL = "url_s";


	byte[] getUrlBytes(String urlSpec) throws IOException{
		URL url = new URL(urlSpec);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream is = httpURLConnection.getInputStream();
			
			if(httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK){
				return null;
			}
			
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while((bytesRead = is.read(buffer))>0){
				out.write(buffer, 0, bytesRead);
			}
			
			out.close();
			return out.toByteArray();
		}finally{
			httpURLConnection.disconnect();
		}
	}
	
	public String getUrl(String urlSpec) throws IOException{
		return new String(getUrlBytes(urlSpec));
	}
	
	public void fetchItems(){
		try{
			String url = Uri.parse(ENDPOINT).buildUpon()
					.appendQueryParameter("method", METHOD_GET_RECENT)
					.appendQueryParameter("api_key", API_KEY)
					.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
					.build().toString();
			Log.d(TAG, "url :"+url);
			String xmlString = getUrl(url);
			Log.i(TAG, "Received xml : "+xmlString);
		}catch(IOException e){
			Log.e(TAG, "Failed ti fetch items", e);
		}
	}
}
