package soccer;

import java.util.LinkedList;
import java.util.List;

public class Model {

	String team;
	char field_side;
	int unum;
	int time;

	List<Flag> flags;
	List<Player> players;
        Body body;
	Ball ball;
	Flag goal;
	boolean goalInVision;
	boolean ballInVision;

	public Model(String team) {
		this.team = team;
		time = -1;
		ballInVision = false;
		goalInVision = false;
                body = new Body();
		flags = new LinkedList<Flag>();
		players = new LinkedList<Player>();
	}

	void setFieldSide(char field_side) {
		// TODO Auto-generated method stub
		this.field_side = field_side;
	}

	void setUnum(int unum) {
		this.unum = unum;
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
			ans[i] = distance(ball.direction, ball.distance,
					players.get(i).direction, players.get(i).distance);
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
		return Math.sqrt(Math.pow(distanceA, 2) + Math.pow(distanceB, 2) - 2
						* distanceA * distanceB
						* Math.cos(Math.abs(degreeA - degreeB)));
	}


    void addObject(String description, String values) {
        String[] desc = description.split("\\s\\\"|\\s|\\\"\\s|\\\"");
        switch(desc[0]){
            case("p"):
            case("P"):
                addPlayer(desc, values.split("\\s"));
                break;
            case("b"):
            case("B"):
                addBall(desc, values.split("\\s"));
                break;
            default:
                addFlag(desc, values.split("\\s"));
        }
    }

    private void addPlayer(String[] desc, String[] values) {
        Player p = new Player();
        if(desc.length>1){
            p.team = desc[1];
            if(desc.length>2){
                p.unum = Integer.parseInt(desc[2]);
                if(desc.length>3){
                    p.goalie = true;
                }
            }
        }
        int length = values.length;
        try{
        
        if(values.length == 1){
            p.direction = Integer.parseInt(values[0]);
        }else if(values.length>1){
            p.distance = Double.parseDouble(values[0]);
            p.direction = Integer.parseInt(values[1]);
            if(values.length>2){
                if(values[values.length-1].matches("t|k")){
                    if(values[values.length-1].equals("k")){
                        p.kick = true;
                    }else{
                        p.tackle = true;
                    }
                    length--;
                }
                if(length==3){               
                    p.pointintDir = Integer.parseInt(values[2]);
                }else if(length>3){
                    p.distChange = Double.parseDouble(values[2]);
                    p.dirChange = Double.parseDouble(values[3]);
                    if(length==5){
                        p.pointintDir = Integer.parseInt(values[4]);
                    }else{
                        p.bodyFacingDir = Integer.parseInt(values[4]);
                        p.headFacingDir = Integer.parseInt(values[5]);
                        if(length==7){
                            p.pointintDir = Integer.parseInt(values[6]);
                        }
                    }
                }
            }
        }
        }catch(NumberFormatException e){
            for(String s: values){
                System.err.println(s);
            }
            System.err.format("length %d  values.length %d matches t|k %b %n", length, values.length, values[values.length-1].matches("t|k"));
            e.printStackTrace();
        }
        players.add(p);
    }

    private void addBall(String[] desc, String[] values) {
        this.ball = new Ball();
        ballInVision = true;
        if(values.length == 1){
            this.ball.direction = Integer.parseInt(values[0]);
        }else if(values.length>1){
            this.ball.distance = Double.parseDouble(values[0]);
            this.ball.direction = Integer.parseInt(values[1]);
            if(values.length>2){
                this.ball.distChange = Double.parseDouble(values[2]);
                this.ball.dirChange = Double.parseDouble(values[3]);
            }
        }
    }

    private void addFlag(String[] desc, String[] values) {
        Flag f = new Flag(desc[0].charAt(0));
        for(int i = 1; i < desc.length; i++){
            f.set(desc[i]);
        }
        if(values.length == 1){
            f.direction = Integer.parseInt(values[0]);
        }else if(values.length>1){
            f.distance = Double.parseDouble(values[0]);
            f.direction = Integer.parseInt(values[1]);
            if(values.length>2){
                f.distChange = Double.parseDouble(values[2]);
                f.dirChange = Double.parseDouble(values[3]);
            }
        }
        if (f.name == 'g') {
            if ((field_side == 'r' && f.left) || (field_side == 'l' && f.right)) {
                goalInVision = true;
                goal = f;
            }
        }
        flags.add(f);
    }
}
