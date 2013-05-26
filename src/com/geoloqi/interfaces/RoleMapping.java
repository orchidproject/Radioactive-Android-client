package com.geoloqi.interfaces;

import java.util.HashMap;
import java.util.Map;

public class RoleMapping {
	public static Map<String, Integer> imeiMap;
	public static Map<Integer, String> roleMap;
	public static Map<String, Integer> roleIdMap;
	static {
		
		roleMap = new HashMap<Integer, String>();
		roleMap.put(0, "medic");
		roleMap.put(1, "firefighter");
		roleMap.put(2, "soldier");
		roleMap.put(3, "transporter");
		
		roleIdMap = new HashMap<String,Integer>();
		roleIdMap.put("medic",0);
		roleIdMap.put("firefighter",1 );
		roleIdMap.put("soldier",2 );
		roleIdMap.put("transporter",3);
		
	}	
}
