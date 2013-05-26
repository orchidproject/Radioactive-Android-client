package com.geoloqi.interfaces;

import org.json.JSONObject;

public interface StateCallback {
	public void update(JSONObject update);
	public void bulkUpdate(JSONObject upadates);
	public void setGameArea();
}
