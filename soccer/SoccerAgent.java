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
		String msg = "(init " + model.team + ")";
		while(com.send(msg)){
			//TODO check model
			//TODO make decision
			//TODO compile message to send
			msg = "(move -3 4)";
			break;
		}
		com.quit();
	}

}
