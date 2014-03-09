package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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

	/**
	 * 
	 * @param args
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws NumberFormatException
	 */
	public SoccerAgent(String[] args) throws NumberFormatException,
			UnknownHostException, SocketException {

		model = new Model(args[0]);
		com = new Communicator(args[1], Integer.parseInt(args[2]), model);

	}

	public void run() {
		String msg = "(init " + model.getTeam() + " (version " + VERSION + "))";
		//int count = 0;
		com.send(msg);
		int x = -20;
		int y = 4;

		if(model.field_side == 'r'){
			x = 20;
		}
		msg = "(move " + x +" "+ y+")";
		int lastTime = 0;
		while(com.send(msg)){
			msg = null;
			//TODO check model
			//TODO make decision
			//TODO compile message to send
			if(model.time>0 && lastTime != model.time){
				/*
				if(model.ballInVision){
					msg = "(turn -20)";//"(dash 50.0 15.0)";
				}else{
					msg = "(turn -11)";
				}
				lastTime = model.time;
				System.err.println("Sending " +model.getTeam() + " : "+ msg);
				*/
				msg = goToBall();
				//lastTime = model.time;
			}
		}
		com.send("(bye)");
		com.quit();
	}

	/**
	 * HoldBall(): Remain stationary while keeping pos- session of the ball in a
	 * position that is as far away from the opponents as possible.
	 */

	/**
	 * PassBall(k): Kick the ball directly towards keeper k. GetOpen(): Move to
	 * a position that is free from op- ponents and open for a pass from the
	 * ball's current position (using SPAR (Veloso et al., 1999)).
	 */

	/**
	 * GoToBall(): Intercept a moving ball or move di- rectly towards a
	 * stationary ball.
	 */
	public String goToBall() { // TODO - Make this method not retarded. Changed it
								// for testing purposes since the agent doesn's
								// see the ball
		if(model.ballInVision) {
			if (model.ball.distance < 0.1) {
				System.err.println("Ball distance: " + model.ball.distance);
				return com.kick(50.0, 0.0);
			}
			if(Math.abs(model.ball.degree) <= DEGREE_DELTA) {
				return com.turn(model.ball.degree);
			} else {
				return com.dash(50.0); // TODO - smart power
			}
		} else {
			 return com.turn(10.0);
			//com.dash(50.0);
		}
	}

	/**
	 * BlockPass(k): Move to a position between the keeper with the ball and
	 * keeper k.
	 */

}
