

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This program is the controller for the elevator-program. The only parameter
 * it takes is the ip-address to the rest of the elevator-program. It uses sockets
 * to communicate with the view and model. A ElevatorController is created for each existing
 * elevator as separated threads. This filter decides to which thread the input should go. 
 * The communication with the threads is handled over LinkedBlockingQueues on each ElevatorController.
 * The communication to the model and view is done over a common pipe that is implemented with an
 * a buffered output-writer that has a lock on it that is fair.
 * 
 * 
 * @author Alexander Gomez
 * 
 */

public class ControllerFilter extends Thread {

	InetAddress host;
	static int port = 6000; 

	ElevatorController ec;

	List<ElevatorController> ecList;
	List<ElevatorController> ecForSort;

	DatagramSocket socket;
	DatagramPacket packet;
	ReentrantLock rLock;
	String response;
	String[] input;
	private final static boolean FAIR = true;

	/**
	 * Creates an instance of <code>ControllerFilter</code> to run in a separate
	 * thread
	 */
	public ControllerFilter(String[] args) {
		//if no parameters use localhost
		try {
			String uNit = args[0];
			//host = InetAddress.getByName((args.length > 0) ? args[0] : "localhost");
			host = InetAddress.getByName("localhost");
			System.err.println("Connecting to "+host + ":" + port);
			socket = new DatagramSocket();

			String msg = "(reconnect MyTeam " + uNit + ")";
			
			packet = new DatagramPacket(msg.getBytes(),msg.length(), host, port);

			socket.send(packet);
			byte[] buffer = new byte[4096];
			
			
			for(int i = 0; i <100;i++){
				packet = new DatagramPacket(buffer, 4096, host, port);
				socket.receive(packet);
				port = packet.getPort();
				System.err.println(new String(packet.getData()));
				
				msg = "(move -3 -"+ uNit +")";
				packet = new DatagramPacket(msg.getBytes(),msg.length(), host, port);

				socket.send(packet);
				
				
				
			}
			
			
			
			socket.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			return;
		}
		
	}


	/**
	 * The entry point of the <code>ControllerFilter</code> application. Create
	 * a <code>ControllerFilter</code> thread and start it.
	 */
	public static void main(String[] args) {
		//(new ControllerFilter(args)).start();
		new ControllerFilter(args);
	}

}
