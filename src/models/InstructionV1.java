package models;

public class InstructionV1 {
	int id;
	private String direction;
	//change to bitmap later
	private Player teammate = null;
	private String task;
	private int time;
	private int status;
	public InstructionV1(int id, int time,int status, String direction, Player teammate,String task){
		this.id = id;
		this.direction = direction;
		this.teammate = teammate;
		this.time = time;
		this.task = task;
		this.status = status;
	}
	
	public Player getTeammate(){
		return teammate;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public String getTask() {
		return task;
	}
	public int getTime() {
		
		return time;
	}
	public int getStatus(){
		return status;
	}

	public Player getPlayer() {
		return teammate;
	}

	public int getId() {
		
		return id;
	}
	
}
