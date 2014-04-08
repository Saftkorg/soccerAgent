package soccer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * This is the main class of the RoboCup Soccer Agent
 *
 * @author Gavelli, Viktor
 * @author Gomez, Alexander
 *
 */
public class SoccerAgent extends Thread {

    private final Model model;
    private final Communicator com;

    //private final double DEGREE_DELTA = 20.0;
    private final Queue<String> commands;

    //private final boolean avoidFKF = false;
    private final Random r = new Random();

    private final Formation f;

    //int x;
    //int y;
    //boolean goalie;
    /**
     *
     * @param args
     * @param f
     * @throws SocketException
     * @throws UnknownHostException
     * @throws NumberFormatException
     */
    public SoccerAgent(String[] args, Formation f) throws NumberFormatException,
            UnknownHostException, SocketException {
        commands = new LinkedList();
        this.f = f;
        //x = f.x;
        //y = f.y;
        //goalie = f.goalie;
        model = new Model(args[0], f.goalie);
        com = new Communicator(args[1], Integer.parseInt(args[2]), model);

    }

    private double[] getWeights() {
        //int holdW, coverPW, coverGW, getFreeW, goToBallW,dribbleW,kickW,passW, catchW;

        if (model.ballInVision) {

            //if (model.ball.distance > 35.0) {
            if (model.ball.distance > model.parameters[Model.FAR_BALL_DIST]) {

                return model.farArray;
                //int[] retint = {1, 0, 0, 0, 0, 0, 0, 0, 0};
                //return retint;

            } else if (model.ball.distance > model.parameters[Model.MED_BALL_DIST]) {
                //int[] retint = {0, 1, 0, 1, 0, 0, 0, 0, 0};
                return model.medArray;
                //double[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
                //return retint;
            } else if (model.ball.distance > 0.7) {
                //int[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
                return model.cloArray;
                //double[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
                //return retint;
            } else {
                //int[] retint = {0, 0, 0, 0, 0, 1, 1, 1, 0};
                return model.hasArray;
                //double[] retint = {0, 0, 0, 0, 0, 1, 1, 1, 0};
                //return retint;
            }
        } else {
            return model.nobArray;
            //double[] retint = {1, 0, 0, 0, 0, 0, 0, 0, 0};
            //return retint;
        }
    }

    @Override
    public void run() {

        String msg;
        int connectCount = 0;
        for (; connectCount < 2; connectCount++) {
            if (com.send(model.initMsg)) {
                break;
            }
        }
        if (connectCount == 2) {
            com.quit();
            System.err.println("NOPE!");
            return;
        }

        f.setSide(model.field_side);
        msg = "(move " + f.x + " " + f.y + ")";
        int lastTime = 0;
        com.send(msg);
        model.time(0);
        while (com.send(msg)) {
            msg = null;
            if (model.freeMove) {
                msg = "(move " + f.x + " " + f.y + ")";
                model.freeMove = false;
            } else if (lastTime != model.time) {
                lastTime = model.time;

                if (!commands.isEmpty()) {
                    msg = commands.poll();
                    continue;
                }

                double[] actionW = getWeights();
                double[] actionScore = new double[actionW.length];

                //int holdW, coverPW, coverGW, getFreeW, goToBallW,dribbleW,kickW,passW, catchW;
                for (int i = 0; i < actionW.length; i++) {

                    if (actionW[i] != 0) {

                        switch (i) {
                            case (0):
                                actionScore[i] = actionW[i] * holdEval();

                                break;
                            case (1):
                                actionScore[i] = actionW[i] * coverPEval();

                                break;
                            case (2):
                                actionScore[i] = actionW[i] * coverGEval();

                                break;
                            case (3):
                                actionScore[i] = actionW[i] * getFreeEval();

                                break;
                            case (4):
                                actionScore[i] = actionW[i] * goToBallEval();

                                break;
                            case (5):
                                actionScore[i] = actionW[i] * dribbleEval();

                                break;
                            case (6):
                                actionScore[i] = actionW[i] * kickEval();

                                break;
                            case (7):
                                actionScore[i] = actionW[i] * passEval();

                                break;
                            case (8):
                                actionScore[i] = actionW[i] * catchEval();

                                break;
                        }
                    } else {
                        actionScore[i] = 0;
                    }
                }

                double maxScore = 0;
                int maxInt = -1;
                for (int i = 0; i < actionScore.length; i++) {
                    //System.err.print(actionScore[i] + " ");
                    if (actionScore[i] > maxScore) {
                        maxScore = actionScore[i];
                        maxInt = i;
                    }
                }
                //System.err.println("doing");
                if (maxInt != -1) {
                    switch (maxInt) {
                        case (0):
                            holdAction();
                            break;
                        case (1):
                            coverPAction();
                            break;
                        case (2):
                            coverGAction();
                            break;
                        case (3):
                            getFreeAction();
                            break;
                        case (4):
                            goToBallAction();
                            break;
                        case (5):
                            dribbleAction();
                            break;
                        case (6):
                            kickAction();
                            break;
                        case (7):
                            passAction();
                            break;
                        case (8):
                            catchAction();
                            break;
                    }
                } else {
                    lookForBallAction();
                }
                if (!commands.isEmpty()) {
                    msg = commands.poll();
                }
            }
        }
        com.quit();
        System.err.println("quitting agent");
    }

    private int holdEval() {
        int ret = 0;
        //String[] lines = {"l t", "l b", "l r", "l l"};

        //double dist;
        for (Threshold th : f.thresholds) {

            FieldObject fo = model.flags.get(th.name);
            if (fo != null) {
                fo.distance = Math.abs(fo.distance * Math.sin(Math.toRadians(fo.direction)));

                if (th.name.charAt(th.name.length() - 1) == model.opp_field_side) {
                    if (model.ballInVision) {
                        model.threshold_adjuster = (model.ball.distance - f.away_from_ball)>0 ? (model.ball.distance - f.away_from_ball): 0;
                        
                    }
                    if (fo.distance < th.min - model.threshold_adjuster || fo.distance > th.max - model.threshold_adjuster) {
                        ret++;
                    }
                    
                } else if (th.name.charAt(th.name.length() - 1) == model.field_side) {
                    
                    if (model.ballInVision) {
                        model.threshold_adjuster = 0;

                    }
                    if (fo.distance < th.min + model.threshold_adjuster || fo.distance > th.max + model.threshold_adjuster) {
                        ret++;
                    }
                    
                }else if (fo.distance < th.min || fo.distance > th.max) {
                    ret++;
                }
            }
        }
        return ret;
    }

    private int coverPEval() {
        int ret = 0;
        
        if(model.lastKickCertainty && !model.ourTeamLastKick){

            for(Player pl: model.players){
                if (!model.team.equals(pl.team) && pl.distance < 20) {
                    if(pl.distance < 3){
                        return 2;           //TODO some other value
                    }else{
                        double dPlD = pl.distance * 1.8;
                        for(Player pl1: model.players){
                            if(model.team.equals(pl.team) && pl.distance < dPlD && proximityHelp(pl1.distance,pl1.direction,dPlD,pl.direction)){
                                ret++;  //TODO something else
                            }
                        }
                    }
                }
            }
            
        }
        return ret;
    }

    
    private boolean proximityHelp(double objDist, double objAngle, double toObjDist, double toObjDir){
        return (((toObjDist * Math.pow(Math.abs(objAngle - toObjDir) / 90, 2.0))
                            + objDist) < toObjDir);
    }
    
    private int coverGEval() {
        return 0;
    }

    private int getFreeEval() {
        if(model.lastKickCertainty && model.ourTeamLastKick){
            
        }
        return 0;
    }

    private double goToBallEval() {

        double dBallDist = 1.8 * model.ball.distance;//1.9 * model.ball.distance; //double ball distance 
        double ret = -(Math.pow(model.ball.distance, 2.1) / 10000) + 1.2;//1.2;
        if (model.ball.distChange != null && model.ball.distChange < -0.1) {
            return ret;
        }

        for (Player pl : model.players) {
            if (model.team.equals(pl.team)) {
                if (pl.distance < dBallDist) {
                    if ( proximityHelp(pl.distance,pl.direction,dBallDist,model.ball.direction)){//      ((dBallDist * Math.pow(Math.abs(pl.direction - model.ball.direction) / 90, 2.0))
                            //+ pl.distance) < dBallDist) {
                        return 0; //TODO should make ret smaller rather than returning zero
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Always returns 1...not good?
     *
     * @return
     */
    private double dribbleEval() {
        double ret = 1.0;
        for (Player pl : model.players) {
            if (!model.team.equals(pl.team) && pl.distance < model.parameters[model.DRB_DIST_EVAL]) {
                return 0.5;
            }
        }
        return ret;
    }

    /**
     * good to shoot but maybe need some more analysis of opponent players
     *
     * @return
     */
    private int kickEval() {
        if (model.goalInVision && model.goal.distance < model.parameters[model.KCK_GOALDIST_EVAL]) {
            return 2;
        } else {
            return 0;
        }
    }

    private int passEval() {
        for (Player pl : model.players) {
            if (model.team.equals(pl.team) && pl.distance < model.parameters[model.PAS_PLYRDIST_EVAL]) {
                return 2;
            }
        }
        return 0;
    }

    private int catchEval() {
        return 0;
    }

    /**
     * Hold position consistent with the formation thresholds, angels given by
     * the server for the lines aren't to the closest point
     */
    private void holdAction() {
        //String[] lines = {"l t", "l b", "l r", "l l"};
        //for (String fl : lines) {
        for (Threshold th : f.thresholds) {
            FieldObject fo = model.flags.get(th.name);
            if (fo != null) {
                double adj = 0;
                if(th.name.charAt(th.name.length()-1) == model.opp_field_side){
                    adj = model.threshold_adjuster;
                }else if(th.name.charAt(th.name.length()-1) == model.field_side){
                    adj = -1*model.threshold_adjuster ;
                }
                
                if (fo.distance > th.max - adj) {
                    if (Math.abs(fo.direction) < 88) {
                        if (fo.direction < 0) {
                            commands.add("(turn " + (fo.direction + 90) + ")");
                        } else if (fo.direction > 0) {
                            commands.add("(turn " + (fo.direction - 90) + ")");
                        }
                    }
                    commands.add("(dash 75)");
                    break;
                } else if (fo.distance < th.min -adj) {
                    if (Math.abs(fo.direction) < 88) {
                        if (fo.direction < 0) {
                            commands.add("(turn " + (fo.direction - 90) + ")");
                        } else if (fo.direction > 0) {
                            commands.add("(turn " + (90 + fo.direction) + ")");
                        }
                    } else {
                        commands.add("(turn 180)");
                    }
                    commands.add("(dash 75)");
                    break;
                }
            }
        }
    }

    private void coverPAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void coverGAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void getFreeAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * goes to the ball , will also somewhat intercept
     */
    private void goToBallAction() {
        if (Math.abs(model.ball.direction) > model.parameters[model.GTB_TURNBALLDIR] /*20*/) {
            //System.err.println("dir " + model.ball.direction + " dirChange " + model.ball.dirChange);
            if (model.ball.dirChange != null) {
                commands.add("(turn " + (model.ball.direction + (model.ball.dirChange)) + ")");
            } else {
                commands.add("(turn " + model.ball.direction + ")");
            }
        } else if (model.ball.distance > 0.7) {
            if (model.ball.dirChange != null && Math.abs(model.ball.dirChange) > 0.1 && Math.abs(model.ball.distChange) > 0.1) {
                double turnThis;
                if (model.ball.distChange < -0.1) {
                    turnThis = (model.ball.direction
                            + 2 * (Math.toDegrees(Math.asin((model.ball.distance * Math.sin(Math.toRadians(model.ball.dirChange))) / Math.abs(model.ball.distChange)))));
                } else {
                    turnThis
                            = (model.ball.direction
                            + (Math.toDegrees(Math.asin((model.ball.distance * Math.sin(Math.toRadians(model.ball.dirChange))) / Math.abs(model.ball.distChange)))));
                }
                if (Double.isNaN(turnThis)) {
                    //System.err.println("found NaN");
                    turnThis = model.ball.direction;
                } else if (Math.abs(turnThis) > 90) {
                    turnThis = Math.signum(turnThis) * 90;
                }
                commands.add("(dash " + 100 + " " + turnThis + ")");
                //System.err.println(commands.peek());
            } else {
                commands.add("(dash 95)");
            }
        }
    }

    /**
     * dribbles with the ball
     */
    private void dribbleAction() {

        int leftDegree = -30;
        int rightDegree = 30;
        int leftScore = 0;
        int middleScore = 0;
        int rightScore = 0;

        if (model.goalInVision) {
            if (Math.abs(model.goal.direction) < 15) {
                middleScore = 5;
            } else {
                if (model.goal.direction < 0) {
                    leftScore = 5;
                } else {
                    rightScore = 5;
                }
            }
        } else {
            if (model.flags.get("l t") != null) {
                if (model.field_side == 'l') {
                    rightScore = 10;
                } else {
                    leftScore = 10;
                }
            } else if (model.flags.get("l b") != null) {
                if (model.field_side == 'l') {
                    leftScore = 10;
                } else {
                    rightScore = 10;
                }
            }
            leftDegree = -60;
            rightDegree = 60;
        }
        for (Player p : model.players) {
            if (!model.team.equals(p.team)) {
                if (Math.abs(p.direction) < 15) {
                    middleScore -= (10 / (p.distance + 1));

                } else {
                    if (p.direction < 0) {
                        leftScore -= (10 / (p.distance + 1));
                    } else {
                        rightScore -= (10 / (p.distance + 1));
                    }

                }
            }
        }
        if (middleScore > leftScore && middleScore > rightScore) {
            //go left
            commands.add("(kick 15 0)");

        } else {
            if (leftScore > rightScore) {
                commands.add("(kick 10 " + (leftDegree) + ")");
                commands.add("(turn " + (leftDegree) + ")");
            } else {
                commands.add("(kick 10 " + (rightDegree) + ")");
                commands.add("(turn " + (rightDegree) + ")");
            }
        }
    }

    /**
     * not very intelligent
     */
    private void kickAction() {
        commands.add("(kick 100 " + (model.goal.direction + (r.nextInt(20) - 10)) + ")");
    }

    /**
     * not intelligent
     */
    private void passAction() {
        int[] degrees = new int[11];
        double[] distance = new double[11];
        int count = 0;
        for (Player pl : model.players) {
            if (model.team.equals(pl.team) && pl.distance < 50) {
                degrees[count] = pl.direction;
                distance[count] = pl.distance;
                count++;
            }
        }
        int rand = r.nextInt(count);
        double power = (2.5 * distance[rand]);
        commands.add("(kick " + (power > 100.0 ? 100.0 : power) + " " + degrees[rand] + ")");
    }

    /**
     *
     */
    private void catchAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     */
    private void lookForBallAction() {
        if (model.ballInVision) {
            commands.add("(turn " + model.ball.direction + ")");
        } else {
            commands.add("(turn 90)");
        }
    }
}
