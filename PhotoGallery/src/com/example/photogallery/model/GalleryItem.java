package com.example.photogallery.model;

public class GalleryItem {
	
	//БъЬт
	private String mCaption;
	//ID
	private String mId;
	//URL
	private String mUrl;
	
	private String mOwner;
	
	public String getmOwner() {
		return mOwner;
	}

	public void setmOwner(String mOwner) {
		this.mOwner = mOwner;
	}

	public String getmCaption() {
		return mCaption;
	}

	public void setmCaption(String mCaption) {
		this.mCaption = mCaption;
	}

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public String getmUrl() {
		return mUrl;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String toString(){
		return mCaption;
	}
	
	public String getPhotoPageUrl(){
		return "https://www.flickr.com/photos/"+mOwner+"/"+mId;
	}

}
