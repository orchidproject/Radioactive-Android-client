package com.geoloqi.interfaces;

/**
 * This is a template file. Rename this class to GeoloqiConstants and update
 * GEOLOQI_ID and GEOLOQI_SECRET with your API keys to get started.
 * 
 * @author tristanw
 */
public abstract interface OrchidConstants {
	public static final String GEOLOQI_ID = "";
	public static final String GEOLOQI_SECRET = "";

	// public static final String URL_BASE = "https://api.geoloqi.com/1/";
	public static final String URL_BASE = "http://holt.mrl.nott.ac.uk:49992/";
	//public static final String URL_BASE = "http://192.168.43.79:49992/";
	
	public static final String GAME_URL_BASE = URL_BASE +  "game/mobile/";
	
	public static final String GAME_LIST_ADDRESS = URL_BASE + "games/list";
	public static final String GAME_REQUEST_ADDRESS = URL_BASE + "join/";

	public static final String IOSOCKET_ADDRESS = "holt.mrl.nott.ac.uk";
	//public static final String IOSOCKET_ADDRESS = "192.168.43.79";
	public static final int IOSOCKET_PORT = 49991;

	//public static final int UPLOAD_PORT = 49991;
	public static final int DOWNLOAD_PORT = 49992;

	public static final String PREFERENCES_FILE = "GEOLOQIHTTPCLIENT";
	public static final String VERSION = "1";
	
	public static final String[] roleMapping = {};
	public static final String[] taskMapping = {};
	
	public static final String  IMAGE_URL_BASE =  URL_BASE + "img/" ;
	
}
