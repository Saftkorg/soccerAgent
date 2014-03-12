package soccer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import soccer.StartTeam.Formation;

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
	
	private boolean avoidFKF = false;
	
	Random r = new Random();

        int x;
        int y;
        boolean goalie;
	/**
	 * 
	 * @param args
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws NumberFormatException
	 */
	public SoccerAgent(String[] args, Formation f) throws NumberFormatException,
			UnknownHostException, SocketException {

            x = f.x;
            y = f.y;
            goalie = f.goalie;
		model = new Model(args[0]);
		com = new Communicator(args[1], Integer.parseInt(args[2]), model);

	}

	public void run() {
            String msg;
            if(!goalie){
                msg = "(init " + model.getTeam() + " (version " + VERSION + "))";
            }else{
                msg = "(init " + model.getTeam() + " (version " + VERSION + ") (goalie))";
            }
		
		// int count = 0;
		com.send(msg);
		

		/*
		 * if(model.field_side == 'r'){ x = 20; y = 10; }
		 */
		msg = move(x, y);
		int lastTime = 0;
		while (com.send(msg)) {
			msg = null;
			// TODO check model
			// TODO make decision
			// TODO compile message to send
			if (model.time > 0 && lastTime != model.time) {

				lastTime = model.time;

				msg = decideAction();

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
	 * @param k
	 *            index of player to kick the ball towards.
	 */
	private String passBall(int k) {
		return kick(50.0, model.players.get(k).direction); // TODO - obvious
	}

	/**
	 * GetOpen(): Move to a position that is free from opponents and open for a
	 * pass from the ball's current position (using SPAR (Veloso et al., 1999)).
	 */
	private String getOpen() {
		if(!model.ballInVision)
			return scanField();
		if(model.ball.direction > 0) {
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
	private String goToBall() { // TODO - Make this method not retarded.
		if (!model.ballInVision) {
			//System.err.println("Cannot see ball. Don't call goToBall.");
			return scanField();
		}
		 //System.err.format("%s ball at %d degrees %n",model.team, model.ball.degree);
		if (model.ball.direction > 20 || model.ball.direction < -20) {
			return turn(model.ball.direction);
		} else {
			return dash(75.0);// "(dash 50.0 15.0)";
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
		return turn(45.0);
	}

	private double wGoalKick = 1.0 - r.nextDouble();
	private double wPass = 100.0 - r.nextDouble();
	private String decideAction() {
		if (model.ballInVision) {
			int k = model.closestFriendlyPlayer();
			if (hasBall()) {
				double goalKickValue = -1.0;
				if(model.goalInVision)
					goalKickValue = wGoalKick*model.goal.distance;
				double passValue = -1.0;
				if(!(k == -1))
					passValue = wPass*model.players.get(k).distance;
				
				if(passValue == -1.0 && goalKickValue == -1.0) 
					return holdBall();
				if(passValue > goalKickValue) 
					return passBall(k);
				return goalKick();
				/*if (model.goalInVision) {
					return goalKick();
				}
				int k = model.closestFriendlyPlayer();
				if(k != -1) {
					passBall(k);
				}*/
			}
			if(k != -1) {
				double[] distances = model.playersBallDistance();
				if(distances[model.friendlyPlayerClosestToBall(distances)] < model.ball.distance) {
					return getOpen();
				}
			}
			return goToBall();
		}
		return scanField();
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
	 * @param moment
	 *            degrees turning angle. 90 is 90 degrees right.
	 */
	private String turn(double moment) {
		//System.err.println("Turn " + model.Unum + " " + model.team);
		avoidFKF = false;
		return ("(turn " + Double.toString(moment) + ")");
	}

	/**
	 * This is the main movement command used to move the players during a game.
	 * 
	 * @param power
	 *            Double check this: percentage power. 100 is max.
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
		if(avoidFKF) 
			return faceBall();
		//System.err.println("Kick " + model.Unum + " " + model.team);
		avoidFKF = true;
		return ("(kick " + Double.toString(power) + " "
				+ Double.toString(direction) + ")");
	}

	private String faceBall() {
		if(model.ballInVision)
			return turn(model.ball.direction);
		return scanField();
	}
	
	public boolean hasBall() {
		return model.ball.distance <= 0.7;
	}
}
