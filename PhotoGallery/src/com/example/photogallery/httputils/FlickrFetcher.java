package com.example.photogallery.httputils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.photogallery.model.GalleryItem;

import android.net.Uri;
import android.util.Log;

public class FlickrFetcher {
	
	private static final String TAG = "FlickrFetcher";
	
	public static final String PREF_SEARCH_QUERY = "searchQuery";
	public static final String PREF_LAST_RESULT_ID = "lastResultId";
	
	//访问端点
	private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
	//API Key
	private static final String API_KEY = "a94fbb36986ad8cd98867d5a3090a5b5";
	//方法名
	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
	private static final String METHOD_SERACH = "flickr.photos.search";
	//一个值为url_s的extra参数
	private static final String PARAM_EXTRAS = "extras";
	private static final String PARAM_TEXT = "text";
	private static final String EXTRA_SMALL_URL = "url_s";
	//xml元素名称的常量
	private static final String XML_PHOTO = "photo";


	public byte[] getUrlBytes(String urlSpec) throws IOException{
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
	
	public ArrayList<GalleryItem> downloadGalleryItems(String url){
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
		try{
			Log.d(TAG, "url :"+url);
			String xmlString = getUrl(url);
			Log.i(TAG, "Received xml : "+xmlString);
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(xmlString));
			
			parseItems(items, parser);
		}catch(IOException e){
			Log.e(TAG, "Failed to fetch items", e);
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Failed to parse items", e);
		}
		return items;
	}
	
	public ArrayList<GalleryItem> fetchItems(){
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method", METHOD_GET_RECENT)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				.build().toString();
		return downloadGalleryItems(url);
	}
	
	public ArrayList<GalleryItem> search(String query){
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method", METHOD_SERACH)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				.appendQueryParameter(PARAM_TEXT, query)
				.build().toString();
		return downloadGalleryItems(url);
	}
	
	void parseItems(ArrayList<GalleryItem> items,XmlPullParser parser) throws XmlPullParserException,IOException{
		int eventType = parser.next();
		
		while(eventType != XmlPullParser.END_DOCUMENT){
			if(eventType == XmlPullParser.START_TAG
					&& XML_PHOTO.equals(parser.getName())){
				String id = parser.getAttributeValue(null, "id");
				String caption = parser.getAttributeValue(null, "title");
				String samllUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
				GalleryItem item = new GalleryItem();
				item.setmId(id);
				item.setmCaption(caption);
				item.setmUrl(samllUrl);
				items.add(item);
			}
			eventType = parser.next();
		}
	}
}
