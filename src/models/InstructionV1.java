package models;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


public class InstructionV1 {
	//private static char[] char_mapping = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	
	int id;
	private String direction;
	//change to bitmap later
	private Player teammate = null;
	private Player player = null;
	private int task;
	private Task taskObj;
	private int time;
	private int status;
	private int player_id;
	private String task_initials = null;
	private int deadline = 0;
	private HashMap<GoogleMap,ToggleManager> tManagers = new HashMap<GoogleMap,ToggleManager>();
	private long create_time = 0;
	
	
	public InstructionV1(int id, 
			int time,
			int status, 
			String direction, 
			Player teammate,
			Player player,
			Task taskObj,
			int task, 
			int player_id,
			int deadline){
		this.id = id;
		this.direction = direction;
		this.teammate = teammate;
		this.time = time;
		this.task = task;
		this.status = status;
		this.player_id = player_id;
		this.player = player;
		this.taskObj = taskObj;
		this.deadline = deadline;
		
		//map it to a string representation 
		if (this.task ==  -1){
			this.task_initials = null;
		}else{
			int first = (this.task/10)%10;
			int second = this.task%10;
			this.task_initials =  first+""+second;
		}
		
		create_time =  System.currentTimeMillis()/1000;
	}
	
	public Player getTeammate(){
		return teammate;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public Task getTaskObj(){
		return taskObj;
	}
	//return null if the task is empty( marked is -1)
	public String getTask() {
		return task_initials;
	}
	public int getTime() {
		
		return time;
	}
	public int getStatus(){
		return status;
	}

	public int getId() {
		
		return id;
	}
	
	public int getPlayerId(){
		return player_id;
	}

	public void setStatus(int status) {
		this.status = status;
		
	}

	public int getTaskId() {
		return task;
	}
	
	public boolean toggleMap(GoogleMap map){
		if(map == null){
			return false;
		}	
		
		ToggleManager tm = tManagers.get(map);
		
		if (tm == null) {
			tm = new ToggleManager(map);
			tManagers.put(map,tm );
		}
		
		tm.toggle();
		return tm.toggled;
	}
	
	
	
    private class ToggleManager{
    	boolean toggled = false;
    	GoogleMap map;
    	ArrayList<Polyline> ps = new ArrayList<Polyline>();
    	
    	ToggleManager(GoogleMap sMap){
    		map = sMap;
    	}
    	
    	
    	private void clear(){
    		for (Polyline p: ps){
				p.remove();
			}
			ps.clear();	
    	}
    	
    	private void render(){
    		PolylineOptions op = new PolylineOptions()
			.add(getPlayer().getLatLng())
			.add(getTaskObj().getLatLng()).width(3);
    	
    		Polyline p1 = map.addPolyline(op);
	
    		op = new PolylineOptions()
			.add(getTeammate().getLatLng())
			.add(getTaskObj().getLatLng()).width(3);

    		Polyline p2 = map.addPolyline(op);
    	
    		ps.add(p1);
    		ps.add(p2);
    	}
    	
    	
    	public void toggle(){
    		
    		if(!toggled){
    	    	render();
    		}
    		else{
    			clear();
    		}
    		
    		toggled = !toggled;
    	}


		public void update(){
			if(toggled){
				clear();
				render();
			}
		}
    }



	public void updateOnMap(GoogleMap map) {
		if(map == null){
			return ;
		}	
		
		ToggleManager tm = tManagers.get(map);
		
		if (tm == null) {
			tm = new ToggleManager(map);
			tManagers.put(map,tm );
		}
		
		tm.update();
	}

	public long getDeadlineMins() {
		long elipsed_time =  (System.currentTimeMillis()/1000 - create_time)/60;
		return deadline/10 - elipsed_time ;
	}
	
}
