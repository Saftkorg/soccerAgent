package soccer;

import java.util.LinkedList;
import java.util.List;

public class Model {

	private String team;
	char field_side;
	private int Unum;
	int time;
	
	List<Flag> flags;
	List<Player> players;
	Ball ball;
	boolean ballInVision;
	
	public Model(String team){
		this.team = team;
		time = -1;
		ballInVision = false;
		flags = new LinkedList<Flag>();
		players = new LinkedList<Player>();
	}
	void setFieldSide(char field_side) {
		// TODO Auto-generated method stub
		this.field_side = field_side;
	}
	
	void setUnum(int Unum){
		this.Unum = Unum;
	}
	
	String getTeam(){
		return team;
	}
	
	public void time(int int1) {
		// TODO Auto-generated method stub
		time = int1;
		ballInVision = false;
		flags = new LinkedList<Flag>();
		players = new LinkedList<Player>();
		
	}
	public void addFlag(Flag f) {
		// TODO Auto-generated method stub
		flags.add(f);
	}
	public void addPlayer(Player player) {
		// TODO Auto-generated method stub
		players.add(player);
	}
	public void addBall(Ball ball) {
		// TODO Auto-generated method stub
		this.ball = ball;
		ballInVision = true;
	}
	
}
