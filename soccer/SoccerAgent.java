package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This is the main class of the RoboCup Soccer Agent
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
	public SoccerAgent(String[] args) throws NumberFormatException, UnknownHostException, SocketException {

		model = new Model(args[0]);
		com = new Communicator(args[1],Integer.parseInt(args[2]),model);
		
	}

	public void run() {
		String msg = "(init " + model.getTeam()+ " (version " + VERSION+ "))";
		int count = 0;
		com.send(msg);
		int x = -4;
		int y = 4;
		if(model.field_side == 'r'){
			x = 4;
		}
		msg = "(move " + x +" "+ y+")";
		while(com.send(msg)){
			msg = null;
			//TODO check model
			//TODO make decision
			//TODO compile message to send
			if(model.time>0){
				msg = "(move -3 4)";
			}
			
			
		}
		com.send("(bye)");
		com.quit();
	}

	private String runToBall() {
		if(model.ballInVision()) {
			com.move(model.ballPosX, model.ballPosX);
		} else {
			com.turn()
		}
	}
}
