package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * @author Gavelli, Viktor
 * @author Gomez, Alexander
 * 
 */

public class SoccerAgent extends Thread {

	InetAddress host;
	static int port = 6000;

	DatagramSocket socket;
	DatagramPacket packet;
	String team;
	String uNit;

	String response;
	String[] input;

	/**
	 * 
	 * @param args
	 */
	public SoccerAgent(String[] args) {
		// if no parameters use localhost
		try {
			team = args[0];
			uNit = "0";
			// host = InetAddress.getByName((args.length > 0) ? args[0] :
			// "localhost");
			host = InetAddress.getLocalHost();

			System.err.println("Connecting to " + host + ":" + port);
			socket = new DatagramSocket();

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

	public void run() {
		String msg = "(init " + team + ")";

		packet = new DatagramPacket(msg.getBytes(), msg.length(), host, port);

		try {
			socket.send(packet);

			byte[] buffer = new byte[4096];
			for (int i = 0; i < 400; i++) {
				packet = new DatagramPacket(buffer, 4096, host, port);
				socket.receive(packet);
				port = packet.getPort();
				System.err.println(new String(packet.getData()));

				msg = "(move -3 -" + uNit + ")";
				packet = new DatagramPacket(msg.getBytes(), msg.length(), host,
						port);

				socket.send(packet);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket.close();
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// (new ControllerFilter(args)).start();
		String[] first = { "MyTeam" };
		(new SoccerAgent(first)).start();
		String[] second = { "MyTheme" };
		(new SoccerAgent(second)).start();
	}

}
