package soccer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Model {
    /**
     * weight values for far distance
     */
    double[] farArray = new double[9];
    /**
     * weight values for medium distance
     */
    double[] medArray = new double[9];
    /**
     * weight values for close distance
     */
    double[] cloArray = new double[9];
    /**
     * weight values for possession of the ball
     */
    double[] hasArray = new double[9];
    /**
     * weights for when no ball is seen
     */
    double[] nobArray = new double[9];
    int ballNotInVisionCount;


    //public enum Parameter {
     static final int FAR_BALL_DIST = 0;
//        FAR_HOLDW,
//        FAR_COVERPW,
//        FAR_COVERGW,
//        FAR_GETFREEW,
//        FAR_GOTOBALLW,
      static final int  MED_BALL_DIST = 1;
//        MED_HOLDW,
//        MED_COVERPW,
//        MED_COVERGW,
//        MED_GETFREEW,
//        MED_GOTOBALLW,
//        CLO_HOLDW,
//        CLO_COVERPW,
//        CLO_COVERGW,
//        CLO_GETFREEW,
//        CLO_GOTOBALLW,
//        CLO_CATCHW,// goalie? 2 ball dist
//        HAS_HOLDW,
//        HAS_GOTOBALLW,
//        HAS_DRIBBLEW,
//        HAS_KICKW,
//        HAS_PASSW,
//        HAS_CATCHW,
//        NOB_HOLDW, //noballinvision
//        NOB_COVERPW,
//        NOB_COVERGW,
//        NOB_GETFREEW,
//        NOB_GOTOBALLW,
       static final int GTB_DISTMULTI_EVAL  =2 ; //GoToBall distance closer than other team player 0.0 - 5.0
       static final int DRB_DIST_EVAL = 3; //distance to opponent for dribble 0.7-100.0
       static final int KCK_GOALDIST_EVAL = 4; //distance to goal before shooting 5 - 50
       static final int PAS_PLYRDIST_EVAL = 5; // distance to team player within shoot 3-50
       static final int GTB_TURNBALLDIR = 6; // turn when ball is this much off center  3-44   
   // }

    double[] parameters ;
    String team;
    char field_side;
    char opp_field_side;
    int unum;
    int time;
    String initMsg;
    int VERSION = 13;
    String recMsg;
    HashMap<String, FieldObject> flags;
    List<Player> players;
    boolean ourTeamLastKick;
    boolean lastKickCertainty;
    Body body;
    FieldObject ball;
    FieldObject goal;
    boolean goalInVision;
    boolean ballInVision;
    boolean goalie;
    boolean freeMove;
    boolean kickIn;
    double threshold_adjuster = 0;
	public boolean kickoff;
	public boolean otherKick;

    public Model(String team, boolean goalie) {
        
        parameters  = new double[7];
        
        this.team = team;
        this.goalie = goalie;
        initMsg = "(init " + team + " (version " + VERSION + ")" + (goalie ? " (goalie)" : "") + ")";
        time = -1;
        ballInVision = false;
        goalInVision = false;
        lastKickCertainty = false;
        otherKick = false;
        ballNotInVisionCount = 0;
        body = new Body();
        flags = new HashMap<>();
        players = new LinkedList<>();
    }

    void setFieldSide(char field_side) {
        // TODO Auto-generated method stub
        this.field_side = field_side;
        
        if(field_side == 'l'){
            this.opp_field_side = 'r';
        }else{
            this.opp_field_side = 'l';
        }
        
    }

    void setUnum(int unum) {
        this.unum = unum;
    }

    String getTeam() {
        return team;
    }

    public void time(int int1) {
        time = int1;
        ballInVision = false;
        goalInVision = false;
        flags.clear();
        players.clear();
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
        if (players.isEmpty()) {
            return null;
        }
        if (!ballInVision) {
            return null;
        }
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
                    if (minDistance == -1) {
                        minDistance = i;
                    }
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
                    if (minDistance == -1) {
                        minDistance = i;
                    }
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
        switch (description.charAt(0)) {
            case ('p'):
            case ('P'):
                addPlayer(description.split("\\s\\\"|\\s|\\\"\\s|\\\""), values.split("\\s"));
                break;
            case ('b'):
            case ('B'):
                //System.err.println("adding ball " + description + " " + values);
                addBall(description.split("\\s\\\"|\\s|\\\"\\s|\\\""), values.split("\\s"));
                break;
            default:
                addFlag(description, values.split("\\s"));
        }
    }

    private void addPlayer(String[] desc, String[] values) {
        Player p = new Player();
        if (desc.length > 1) {
            p.team = desc[1];
            if (desc.length > 2) {
                p.unum = Integer.parseInt(desc[2]);
                if (desc.length > 3) {
                    p.goalie = true;
                }
            }
        }
        int length = values.length;
        try {

            if (values.length == 1) {
                p.direction = Integer.parseInt(values[0]);
            } else if (values.length > 1) {
                p.distance = Double.parseDouble(values[0]);
                p.direction = Integer.parseInt(values[1]);
                if (values.length > 2) {
                    if (values[values.length - 1].matches("t|k")) {
                        if (values[values.length - 1].equals("k")) {
                            p.kick = true;
                            lastKickCertainty = true;
                            ourTeamLastKick = p.team.equals(this.team);
                        } else {
                            p.tackle = true;
                        }
                        length--;
                    }
                    if (length == 3) {
                        p.pointintDir = Integer.parseInt(values[2]);
                    } else if (length > 3) {
                        p.distChange = Double.parseDouble(values[2]);
                        p.dirChange = Double.parseDouble(values[3]);
                        if (length == 5) {
                            p.pointintDir = Integer.parseInt(values[4]);
                        } else {
                            p.bodyFacingDir = Integer.parseInt(values[4]);
                            p.headFacingDir = Integer.parseInt(values[5]);
                            if (length == 7) {
                                p.pointintDir = Integer.parseInt(values[6]);
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            //for (String s : values) {
            //    System.err.println(s);
            //}
            //System.err.format("length %d  values.length %d matches t|k %b %n", length, values.length, values[values.length - 1].matches("t|k"));
            e.printStackTrace();
        }
        players.add(p);
    }

    private void addBall(String[] desc, String[] values) {
        //prevball = ball;
        ball = new FieldObject();
        ballInVision = true;
        if (values.length == 1) {
            ball.direction = Integer.parseInt(values[0]);
        } else if (values.length > 1) {
            ball.distance = Double.parseDouble(values[0]);
            ball.direction = Integer.parseInt(values[1]);
            if (values.length > 2) {
                ball.distChange = Double.parseDouble(values[2]);
                ball.dirChange = Double.parseDouble(values[3]);
            }
        }
    }

    private void addFlag(String desc, String[] values) {
        FieldObject f = new FieldObject();
        if (values.length == 1) {
            f.direction = Integer.parseInt(values[0]);
        } else if (values.length > 1) {
            f.distance = Double.parseDouble(values[0]);
            f.direction = Integer.parseInt(values[1]);
            if (values.length > 2) {
                f.distChange = Double.parseDouble(values[2]);
                f.dirChange = Double.parseDouble(values[3]);
            }
        }
        if (desc.charAt(0) == 'g') {
            if ( opp_field_side == desc.charAt(desc.length()-1)) {
                goalInVision = true;
                goal = f;
            }
        }
        flags.put(desc, f);
    }

}
