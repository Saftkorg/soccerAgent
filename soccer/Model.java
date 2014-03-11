package soccer;

import java.util.LinkedList;
import java.util.List;

public class Model {

	String team;
	char field_side;
	int Unum;
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
		if (f.name == 'g') {
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

	public int closestPlayer() {
		int k = 0;
		for (int i = 0; i < players.size(); i++) {
			if (players.get(k).distance > players.get(i).distance) {
				k = i;
			}
		}
		return k;
	}

	public int closestFriendlyPlayer() {
		int k = -1;
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).team != null && players.get(i).team.equals(team)) {
				k = i;
				break;
			}
		}
		for (int i = k + 1; i < players.size(); i++) {
			if (players.get(i).team != null && players.get(i).team.equals(team)) {
				if (players.get(k).distance > players.get(i).distance) {
					k = i;
				}
			}
		}
		return k;
	}

	public double[] playersBallDistance() {
		if (players.size() == 0)
			return null;
		if (!ballInVision)
			return null;
		double[] ans = new double[players.size()];
		for (int i = 0; i < players.size(); i++) {
			ans[i] = distance(ball.degree, ball.distance,
					players.get(i).degree, players.get(i).distance);
		}
		return ans;
	}

	public int playerClosestToBall() {
		double[] distances = playersBallDistance();
		int minDistance = -1;
		if (distances != null) {
			minDistance = 0;
			for (int i = 1; i < distances.length; i++) {
				if (distances[i] < distances[minDistance]) {
					minDistance = i;
				}
			}
		}
		return minDistance;
	}

	public int friendlyPlayerClosestToBall(double[] distances) {
		int minDistance = -1;
		if (distances != null) {
			for (int i = 0; i < distances.length; i++) {
				if (players.get(i).team != null
						&& players.get(i).team.equals(team)) {
					if (minDistance == -1)
						minDistance = i;
					if (distances[i] < distances[minDistance]) {
						minDistance = i;
					}
				}

			}
		}
		return minDistance;
	}
	
	public int friendlyPlayerClosestToBall() {
		double[] distances = playersBallDistance();
		int minDistance = -1;
		if (distances != null) {
			for (int i = 0; i < distances.length; i++) {
				if (players.get(i).team != null
						&& players.get(i).team.equals(team)) {
					if (minDistance == -1)
						minDistance = i;
					if (distances[i] < distances[minDistance]) {
						minDistance = i;
					}
				}

			}
		}
		return minDistance;
	}

	/**
	 * Calculates distance between two points with reference angles and
	 * distances.
	 * 
	 * @param degreeA
	 * @param distanceA
	 * @param degreeB
	 * @param distanceB
	 * @return
	 */
	public double distance(double degreeA, double distanceA, double degreeB,
			double distanceB) {
		return Math
				.sqrt(Math.pow(distanceA, 2) + Math.pow(distanceB, 2) - 2
						* distanceA * distanceB
						* Math.cos(Math.abs(degreeA - degreeB)));
	}
}
