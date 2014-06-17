package com.geoloqi.widget;

import org.json.JSONException;
import org.json.JSONObject;

import com.geoloqi.mapattack.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskMsgArrayAdaptor extends ArrayAdapter<JSONObject> {

	private LayoutInflater mInflater;
	
	
	public TaskMsgArrayAdaptor(Context context) {
		super(context, 0);
		mInflater =  LayoutInflater.from(context);
		
		
	}
	
	public String caculateTime(int time_second){
			long timeStamp = System.currentTimeMillis();
			long sentTime =  time_second*1000;
			int difference = (int) (timeStamp-sentTime);
			int mins = difference/(60*1000);
			int sec = (difference-(mins*60*1000))/1000;
			return "sent "+ mins + " mins " +sec+ " secs"+ " ago";
			
	}
	public View getView(final int position,View contentView,ViewGroup parent){
		//startUpdateTimeIfNot();
		final JSONObject message = getItem(position);
		if(contentView == null){
            contentView = mInflater.inflate(R.layout.activity_game_msg_list_cell_v2, parent, false);
            
        }
		final TextView content = (TextView)contentView.findViewById(R.id.msg_data);
		final ImageView img = (ImageView)contentView.findViewById(R.id.sender_img);
		
		
		//HQ message, no image required
		if (message.optInt("player_id")==-1) {
			content.setText("From HQ: "+ message.optString("content"));
			img.setImageBitmap(null);
			return contentView;
		}
		try {
			
			content.setText("From "+message.getString("player_initials")+ ": "+ message.optString("content"));
			/*ImageLoader.getImageLoader().loadPlayerImage(
					message.getInt("player_id"), 
					message.getString("player_initials"), 
					message.getString("player_skill"), 
					new ImageLoader.Callback() {
				
				@Override
				public void callback(Bitmap bm) {
					img.setImageBitmap(bm);
				}
			});*/
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	
		return contentView;
	}

}
