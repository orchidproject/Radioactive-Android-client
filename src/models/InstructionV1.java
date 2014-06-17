package models;


public class InstructionV1 {
	//private static char[] char_mapping = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	
	
	int id;
	private String direction;
	//change to bitmap later
	private Player teammate = null;
	private int task;
	private int time;
	private int status;
	private int player_id;
	private String task_initials = null;
	public InstructionV1(int id, int time,int status, String direction, Player teammate,int task, int player_id){
		this.id = id;
		this.direction = direction;
		this.teammate = teammate;
		this.time = time;
		this.task = task;
		this.status = status;
		this.player_id = player_id;
		//map it to a string representation 
		if (this.task ==  -1){
			this.task_initials = null;
		}else{
			int first = (this.task/10)%10;
			int second = this.task%10;
			this.task_initials =  first+""+second;
		}
	}
	
	public Player getTeammate(){
		return teammate;
	}
	
	public String getDirection() {
		return direction;
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
	
}
