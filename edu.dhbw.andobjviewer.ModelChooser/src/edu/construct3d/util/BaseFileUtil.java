package edu.construct3d.util;

import java.io.BufferedReader;

import android.graphics.Bitmap;

public abstract class BaseFileUtil {
	
	protected String baseFolder = null;

	
	public String getBaseFolder() {
		return baseFolder;
	}

	
	public void setBaseFolder(String baseFolder) {
		this.baseFolder = baseFolder;
	}

	/**
	 * get an reader through it's filename
	 * @param name
	 * @return may be null, in case of an exception 
	 */
	public abstract BufferedReader getReaderFromName(String name);

	/**
	 * get a bitmap object through an filename.
	 * @param name
	 * @return may be null, in case of an exception 
	 */
	public abstract Bitmap getBitmapFromName(String name);

}