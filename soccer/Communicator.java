package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Communicator {
	
	InetAddress host;
	int port ;
	Model model;

	DatagramSocket socket;
	DatagramPacket packet;
	
	public Communicator(String host, int port, Model model) throws UnknownHostException, SocketException{
		this.port = port;
		this.host = InetAddress.getByName(host);
		this.model = model;
		System.err.println("Connecting to " + host + ":" + port);
		
		socket = new DatagramSocket();
		socket.setSoTimeout(1000);
	}
	
	
	boolean send(String msg){
		packet = new DatagramPacket(msg.getBytes(), msg.length(), host, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			socket.close();
			return false;
		}
		return receive();
	}
	/**
	 * TODO should update model, and return true if the server still responds.
	 * @return
	 */
	boolean receive(){
		byte[] buffer = new byte[4096];
		packet = new DatagramPacket(buffer, 4096, host, port);
		try {
			socket.receive(packet);
			
			
			
			
			
			System.err.println(new String(buffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			socket.close();
			return false;
		}
		port = packet.getPort();
		return true;
	}
	void quit(){
		socket.close();
	}
}
