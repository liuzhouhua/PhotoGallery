package com.example.photogallery.httputils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlickrFetcher {


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
}
