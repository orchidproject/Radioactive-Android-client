package com.geoloqi.interfaces;

import java.util.HashMap;
import java.util.Map;

public class RoleMapping {
	public static Map<String, Integer> imeiMap;
	public static Map<Integer, String> roleMap;
	public static Map<String, Integer> roleIdMap;
	static {
		
		long[] mappings = new long[] {
				355310042629679l , 3,
				353833044766498l , 3,
				351822050902701l , 3,
				353833044759428l , 3,
				354957031750519l , 2,
				358883049587436l , 4,
				359234040746424l , 4,
				353833044768841l , 3,
				358350040620079l , 1, // not setup yet
				355310042381271l , 3,
				351822050905126l , 3,
				354957031707824l , 3, // not setup yet
				355310042607741l , 3,
				355310042629695l , 1
		};

		
		imeiMap = new HashMap<String, Integer>();
		for (int i = 0; i < mappings.length; i = i + 2) {
			imeiMap.put("" + mappings[i], (int)mappings[i+1]);
		}
		
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
