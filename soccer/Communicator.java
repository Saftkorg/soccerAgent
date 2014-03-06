package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Communicator {

	InetAddress host;
	int port;
	Model model;

	DatagramSocket socket;
	DatagramPacket packet;

	public Communicator(String host, int port, Model model)
			throws UnknownHostException, SocketException {
		this.port = port;
		this.host = InetAddress.getByName(host);
		this.model = model;
		System.err.println("Connecting to " + host + ":" + port);

		socket = new DatagramSocket();
		socket.setSoTimeout(1000);
	}

	boolean send(String msg) {
		packet = new DatagramPacket(msg.getBytes(), msg.length(), host, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			socket.close();
			return false;
		}
		return receive();
	}

	/**
	 * TODO should update model, and return true if the server still responds.
	 * 
	 * @return
	 */
	boolean receive() {
		byte[] buffer = new byte[4096];
		packet = new DatagramPacket(buffer, 4096, host, port);
		try {
			socket.receive(packet);

			System.err.println(new String(buffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			socket.close();
			return false;
		}
		port = packet.getPort();
		return true;
	}

	void quit() {
		socket.close();
	}

	/**
	 * Only used before the game starts to place the players on their starting
	 * locations.
	 * 
	 * @param x
	 * @param y
	 */
	public void move(double x, double y) {
		send("(move " + Double.toString(x) + " " + Double.toString(y) + ")");
	}

	/**
	 * 
	 * @param moment
	 *            degrees turning angle. 90 is 90 degrees right.
	 */
	public void turn(double moment) {
		send("(turn " + Double.toString(moment) + ")");
	}

	/**
	 * This is the main movement command used to move the players during a game.
	 * 
	 * @param power
	 *            Double check this: percentage power. 100 is max.
	 */
	public void dash(double power) {
		send("(dash " + Double.toString(power) + ")");
	}

	public void kick(double power, double direction) {
		send("(kick " + Double.toString(power) + " "
				+ Double.toString(direction) + ")");
	}
}
