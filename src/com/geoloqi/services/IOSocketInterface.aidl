package com.geoloqi.services;
interface IOSocketInterface {
	void startTest(float lat,float lng,int time);
	void stopTest();
	
	void sendMsg(String msg);
	String connect();
	String disconnect();
	String get_info();
	String get_skill();
	String get_initials();
	String get_userID();
}  