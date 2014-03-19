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

    Model model;
    Communicator com;
    int VERSION = 13;
    private final double DEGREE_DELTA = 20.0;
    

    private Queue<String> commands;

    private boolean avoidFKF = false;

    Random r = new Random();

    Formation f;

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
        model = new Model(args[0]);
        com = new Communicator(args[1], Integer.parseInt(args[2]), model);

    }

    private int[] getWeights() {
        //int holdW, coverPW, coverGW, getFreeW, goToBallW,dribbleW,kickW,passW, catchW;

        if (model.ballInVision) {
            if (model.ball.distance > 45.0) {
                int[] retint = {1, 0, 0, 0, 0, 0, 0, 0, 0};
                return retint;
            } else if (model.ball.distance > 10.0 && model.ball.distance <= 45.0) {
                //int[] retint = {0, 1, 0, 1, 0, 0, 0, 0, 0};
                int[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
                return retint;
            } else if (model.ball.distance > 0.7 && model.ball.distance <= 10.0) {
                //int[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
                int[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
                return retint;
            } else {
                //int[] retint = {0, 0, 0, 0, 0, 1, 1, 1, 0};
                int[] retint = {0, 0, 0, 0, 0, 1, 1, 1, 0};
                return retint;
            }

        } else {
            int[] retint = {1, 0, 0, 0, 0, 0, 0, 0, 0};
            return retint;
        }

    }

    public void run() {

        String msg;
        if (!f.goalie) {
            msg = "(init " + model.getTeam() + " (version " + VERSION + "))";
        } else {
            msg = "(init " + model.getTeam() + " (version " + VERSION + ") (goalie))";
        }
        com.send(msg);
        f.setSide(model.field_side);
        msg = move(f.x, f.y);
        int lastTime = -1;
        com.send(msg);
        model.time(-1);
        while (com.send(msg)) {
            msg = null;
            if (lastTime != model.time) {
                lastTime = model.time;

                if (!commands.isEmpty()) {
                    msg = commands.poll();
                    continue;
                }

                int[] actionW = getWeights();
                int[] actionScore = new int[actionW.length];

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

                int maxScore = 0;
                int maxInt = -1;
                for (int i = 0; i < actionScore.length; i++) {
                    System.err.print(actionScore[i] + " ");
                    if (actionScore[i] > maxScore) {
                        maxScore = actionScore[i];
                        maxInt = i;
                    }
                }
                System.err.println();
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

            }
        }
        com.quit();
    }

    /**
     * HoldBall(): Remain stationary while keeping possession of the ball in a
     * position that is as far away from the opponents as possible.
     */
    private String holdBall() {
        //System.err.println("Holding ball");
        return kick(15.0, 45.0); // TODO
    }

    /**
     * PassBall(k): Kick the ball directly towards keeper k.
     *
     * @param k index of player to kick the ball towards.
     */
    private String passBall(int k) {
        return kick(50.0, model.players.get(k).direction); // TODO - obvious
    }

    /**
     * GetOpen(): Move to a position that is free from opponents and open for a
     * pass from the ball's current position (using SPAR (Veloso et al., 1999)).
     */
    private String getOpen() {
        if (!model.ballInVision) {
            return scanField();
        }
        if (model.ball.direction > 0) {
            return dash(50.0, model.ball.direction - 180);
        }
        return dash(50.0, 180 + model.ball.direction); // TODO - derp
    }

    /**
     * GoToBall(): (TODO - Intercept a moving ball) or move directly towards a
     * stationary ball.
     *
     * Only call when ball is visible TODO - predict expected ball position when
     * the ball is not in vision.
     */
    private void goToBall() { // TODO - Make this method not retarded.
        //if (!model.ballInVision) {
        //System.err.println("Cannot see ball. Don't call goToBall.");
        //  return scanField();
        //}
        //System.err.format("%s ball at %d degrees %n",model.team, model.ball.degree);
        //System.err.format("ball at %f distanceChange, our speed: %f %n", model.ball.distChange, model.body.amountOfSpeed);

        if (model.ball.direction > 5 || model.ball.direction < -5) {
            commands.add("(turn_neck " + (-model.body.headAngle) + ")");
            commands.add("(turn " + (model.ball.direction + model.body.headAngle) + ")");
            commands.add("(dash 75.0)");
            //return turn(model.ball.direction);
        } else {
            commands.add("(dash 75.0)");// "(dash 50.0 15.0)";
        }

    }

    /**
     * kick the ball directly at the goal. TODO - avoid goal keeper. Only use
     * when goal is visible
     *
     * @return
     */
    public String goalKick() {
        if (!model.goalInVision) {
            //System.err.println("Cannot see goal. Don't try to hit it!");
            return scanField();
        }
        return kick(100.0, model.goal.direction); // TODO
    }

    private String scanField() {
        return turn(70);
    }

    //private double wGoalKick = 1.0 - r.nextDouble();
    //private double wPass = 100.0 - r.nextDouble();
    private void decideAction() {
        // TODO check model
        // TODO make decision
        // TODO compile message to send
        //String retmsg = "";

        /*
         if (model.ballInVision) {
         System.err.println("balldist " + model.ball.distance);
         if (model.ball.distance > 45.0) {

         } else if (model.ball.distance > 20.0 && model.ball.distance <= 45.0) {

         } else if (model.ball.distance > 0.7) {
         if (Math.abs(model.ball.direction) > 5) {
         commands.add("(turn " + (model.ball.direction + model.body.headAngle) + ")");
         }
         commands.add("(dash 75)");
         if (Math.abs(model.body.headAngle) > 1) {
         commands.add("(turn_neck " + (-model.body.headAngle) + ")");
         }

         } else {
         if (model.goalInVision && model.goal.distance < 30) {
         FieldObject fot = model.flags.get("f g r t");
         FieldObject fob = model.flags.get("f g r b");
         if (fot != null && fob != null) {

         int pifog = 0;

         int[] opendir = new int[fob.direction - fot.direction];

         for (Player p : model.players) {
         if (p.direction < fob.direction && p.direction > fot.direction) {
         pifog++;
         opendir[p.direction - fot.direction]++;
         }
         }
         if (pifog / (fob.direction - fot.direction) < 1.0) {
         int smallest = 20;
         int degree = 0;
         for (int i = 0; i < opendir.length; i++) {
         if (opendir[i] < smallest) {
         degree = i;
         smallest = opendir[i];
         }
         }
         commands.add("(kick 100 " + model.goal.direction + ")");
         } else {
         int closeEnemy = 0;
         for (Player p : model.players) {
         if (p.distance < 10) {
         if (p.team != null && p.team.equals(model.team)) {
         continue;
         }
         closeEnemy++;
         }
         }
         if (closeEnemy > 5) {
         for (Player p : model.players) {
         if (p.team != null && p.team.equals(model.team)) {
         commands.add("(kick " + (p.distance * 2) + " " + p.direction + ")");
         break;
         }
         }
         } else {
         commands.add("(kick 30 " + (-model.goal.direction) + ")");
         commands.add("(turn " + (-model.goal.direction) + ")");
         commands.add("(dash 75)");
         }

         }

         } else {
         commands.add("(turn_neck " + model.goal.direction + ")");
         commands.add("(kick 40)");
         commands.add("(dash 70)");

         }

         }
         commands.add("(kick 30 30)");
         commands.add("(turn 30)");
         commands.add("(dash 45)");
         }
         } else {
         commands.add(scanField());
         }
      
         */
    }

    /**
     * BlockPass(k): Move to a position between the keeper with the ball and
     * keeper k.
     */
    /**
     * Only used before the game starts to place the players on their starting
     * locations.
     *
     * @param x
     * @param y
     */
    private String move(double x, double y) {
        return ("(move " + Double.toString(x) + " " + Double.toString(y) + ")");
    }

    /**
     *
     * @param moment degrees turning angle. 90 is 90 degrees right.
     */
    private String turn(int moment) {
        //System.err.println("Turn " + model.Unum + " " + model.team);
        avoidFKF = false;
        return ("(turn " + moment + ")");
    }

    /**
     * This is the main movement command used to move the players during a game.
     *
     * @param power Double check this: percentage power. 100 is max.
     */
    private String dash(double power) {
        //System.err.println("Dash " + model.Unum + " " + model.team);
        return ("(dash " + Double.toString(power) + ")");
    }

    private String dash(double power, double direction) {
        //System.err.println("Dash " + model.Unum + " " + model.team);
        return ("(dash " + Double.toString(power) + " "
                + Double.toString(direction) + ")");
    }

    private String kick(double power, double direction) {
        if (avoidFKF) {
            return faceBall();
        }
        //System.err.println("Kick " + model.Unum + " " + model.team);
        avoidFKF = true;
        return ("(kick " + Double.toString(power) + " "
                + Double.toString(direction) + ")");
    }

    private String faceBall() {
        //System.err.println("faceball " + model.ballInVision);
        if (model.ballInVision) {
            return turn(model.ball.direction);
        }
        return scanField();
    }

    public boolean hasBall() {
        return model.ball.distance <= 0.7;
    }

    private int calcscRepo() {
        int weight = 0;
        FieldObject fo;
        for (Threshold t : f.thresholds) {
            fo = model.flags.get(t.name);
            if (fo == null) {
                continue;
            }
            if (fo.distance < t.min) {
                weight -= 20;
            } else if (fo.distance > t.max) {
                weight += 20;
            }
        }
        return weight;
    }

    private void dashback() {
        commands.add("(turn 45)");
        commands.add("(dash 45)");
        if (model.body.headAngle != 0) {
            commands.add("(turn_neck " + (-model.body.headAngle) + ")");
        }
        commands.add("(dash 45)");
    }

    private void dashfrwd() {
        commands.add("(dash 45)");
        if (model.body.headAngle != 0) {
            commands.add("(turn_neck " + (-model.body.headAngle) + ")");
        }
        commands.add("(dash 45)");
    }

    private int holdEval() {
        int ret = 0;
        //String[] lines = {"l t", "l b", "l r", "l l"};
        double dist;
        for (Threshold th : f.thresholds) {
            FieldObject fo = model.flags.get(th.name);
            if (fo != null) {
                fo.distance = Math.abs(fo.distance * Math.sin(Math.toRadians(fo.direction)));
                if (fo.distance < th.min || fo.distance > th.max) {
                    ret++;
                }
            }
        }
        return ret;
    }

    private int coverPEval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private int coverGEval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private int getFreeEval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private int goToBallEval() {
        double dBallDist = 2 * model.ball.distance;
        int ret = 1;
        for (Player pl : model.players) {
            if (model.team.equals(pl.team)) {
                if (pl.distance < dBallDist) {
                    if (((dBallDist * Math.pow(Math.abs(pl.direction - model.ball.direction) / 90, 2.0))
                            + pl.distance) < dBallDist) {
                        return 0;
                    }
                }
            }
        }
        return ret;
    }

    private int dribbleEval() {
        int ret = 1;
        for (Player pl : model.players) {
            if (!model.team.equals(pl.team) && pl.distance < 5) {
                return 1;
            }
        }
        return 1;
    }

    private int kickEval() {
        if(model.goalInVision && model.goal.distance<30){
            return 2;
        }else{
            return 0;
        }
    }

    private int passEval() {
        for (Player pl : model.players) {
            if (model.team.equals(pl.team) && pl.distance<50) {
                return 2;
            }
        }
        return 0;
    }

    private int catchEval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void holdAction() {
        //String[] lines = {"l t", "l b", "l r", "l l"};
        //for (String fl : lines) {
        for (Threshold th : f.thresholds) {
            FieldObject fo = model.flags.get(th.name);
            if (fo != null) {

                if (fo.distance > th.max) {
                    //System.err.println(th.name + " dist " + fo.distance + " dir "
                    //       + fo.direction);
                    if (Math.abs(fo.direction) < 88) {
                        if (fo.direction < 0) {
                            commands.add("(turn " + (fo.direction + 90) + ")");

                            //break;
                        } else if (fo.direction > 0) {
                            commands.add("(turn " + (fo.direction - 90) + ")");

                            //break;
                        }
                    }
                    commands.add("(dash 75)");
                    break;
                } else if (fo.distance < th.min) {
                    //System.err.println(th.name + " dist " + fo.distance + " dir "
                    //        + fo.direction);
                    if (Math.abs(fo.direction) < 88) {
                        if (fo.direction < 0) {
                            commands.add("(turn " + (fo.direction - 90) + ")");

                            //break;
                        } else if (fo.direction > 0) {
                            commands.add("(turn " + (90 + fo.direction) + ")");

                            //break;
                        }
                    } else {
                        commands.add("(turn 180)");
                    }
                    commands.add("(dash 75)");
                    break;
                }

            }
        }
        if (commands.isEmpty()) {
            commands.add("(turn 0)");
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

    private void goToBallAction() {
        if (Math.abs(model.ball.direction) > 5 && model.ball.distance > 1.0) {
            //System.err.println("dir " + model.ball.direction + " dirChange " + model.ball.dirChange);
            if (model.ball.dirChange != null) {
                commands.add("(turn " + (model.ball.direction + model.ball.dirChange) + ")");
            } else {
                commands.add("(turn " + model.ball.direction + ")");
            }
        }
        if (model.ball.distance > 0.7) {
            commands.add("(dash " + 95 + ")");
        }
    }

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
        }else{
            //FieldObject fot = ;
            //FieldObject fob = model.flags.get("l b");
            
            if(model.flags.get("l t")!= null){
                if(model.field_side == 'l'){
                    rightScore = 10;
                }else{
                    leftScore = 10;
                }
            }else if(model.flags.get("l b")!= null){
                if(model.field_side == 'l'){
                    leftScore = 10;
                }else{
                    rightScore = 10;
                }
            }
            leftDegree = -60;
            rightDegree = 60;
        }
        for (Player p : model.players) {
            if (!model.team.equals(p.team)) {
                if (Math.abs(p.direction) < 15) {
                    middleScore -= (10 / p.distance);
                    
                } else {
                    if (p.direction < 0) {
                        leftScore -= (10/p.distance);
                    } else {
                        rightScore -= (10/p.distance);
                    }
                    
                }
            }
        }
        if (middleScore > leftScore && middleScore > rightScore) {
            //go left
            commands.add("(kick 15 0)");
            
        } else {
            if(leftScore > rightScore ){
                commands.add("(kick 10 " + (leftDegree)+ ")");
                commands.add("(turn " + (leftDegree) + ")");
            }else{
                commands.add("(kick 10 " + (rightDegree)+ ")");
                commands.add("(turn " + (rightDegree) + ")");
            }     
        }
    }

    private void kickAction() {
        commands.add("(kick 100 "+ (model.goal.direction + (r.nextInt(20)-10) )+")");
    }

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
        double power = (2.5*distance[rand]);
        commands.add("(kick " + (power>100.0 ? 100.0 : power) + " " + degrees[rand] + ")");
                
    }

    private void catchAction() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void lookForBallAction() {
        //System.err.println("lookForBall");
        commands.add(faceBall());
    }
}
