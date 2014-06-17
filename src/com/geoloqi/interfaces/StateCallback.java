package com.geoloqi.interfaces;

import org.json.JSONObject;

public interface StateCallback {
	public void update(JSONObject upadates);
	public void preLoad();
	public void afterLoad(JSONObject upadates);
}
