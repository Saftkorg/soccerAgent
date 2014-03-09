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
	Flag goal;
	boolean goalInVision;
	boolean ballInVision;

	public Model(String team) {
		this.team = team;
		time = -1;
		ballInVision = false;
		goalInVision = false;
		flags = new LinkedList<Flag>();
		players = new LinkedList<Player>();
	}

	void setFieldSide(char field_side) {
		// TODO Auto-generated method stub
		this.field_side = field_side;
	}

	void setUnum(int Unum) {
		this.Unum = Unum;
	}

	String getTeam() {
		return team;
	}

	public void time(int int1) {
		// TODO Auto-generated method stub
		time = int1;
		ballInVision = false;
		goalInVision = false;
		flags = new LinkedList<Flag>();
		players = new LinkedList<Player>();

	}

	public void addFlag(Flag f) {
		// TODO Auto-generated method stub
		flags.add(f);
		if (f.goal) {
			if ((field_side == 'r' && f.left) || (field_side == 'l' && f.right)) {
				goalInVision = true;
				goal = f;
			}
		}
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
