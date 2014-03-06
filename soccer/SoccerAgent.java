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
		int count = 0;
		com.send(msg);
		int x = -4;
		int y = 4;
		if (model.field_side == 'r') {
			x = 4;
		}
		msg = "(move " + x + " " + y + ")";
		while (com.send(msg)) {
			msg = null;
			// TODO check model
			// TODO make decision
			// TODO compile message to send
			if (model.time > 0) {
				msg = "(move -3 4)";
			}
			goToBall();

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
	public void goToBall() { // TODO - Make this method not retarded. Changed it
								// for testing purposes since the agent doesn's
								// see the ball
		if (model.ballInVision) {
			// com.turn(model.ball.degree);
			com.dash(50.0); // TODO - smart power
		} else {
			com.turn(-11.9);
			com.dash(50.0);
		}
	}

	/**
	 * BlockPass(k): Move to a position between the keeper with the ball and
	 * keeper k.
	 */
}
