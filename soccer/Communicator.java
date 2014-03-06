package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Communicator {

	
	private int index;
	private String msg;
	
	private InetAddress host;
	private int port;
	private Model model;

	private DatagramSocket socket;
	private DatagramPacket packet;

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
		if (msg != null) {
			packet = new DatagramPacket(msg.getBytes(), msg.length(), host,
					port);
			try {
				socket.send(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				socket.close();
				return false;
			}
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

			this.msg = new String(buffer);
			parse();

			//System.err.println(new String(buffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			socket.close();
			return false;
		}
		port = packet.getPort();
		return true;
	}

	private void parse() {

		// String msg = "(init l 1 before_kick_off)";
		if(msg.startsWith("(see")){
			seeMsg();
		}else if(msg.startsWith("(player")){
			
		}else if(msg.startsWith("(init")){
			initMsg();
		}
		
	}

	private void seeMsg() {
		
		index = 5;
		
		model.time(getInt());
		
		while(index < msg.length() && msg.charAt(index)=='('){
			index++;
			String tmp = getString();
			switch(tmp.charAt(0)){
				case('g'):
				case('l'):
				case('f'):
					addFlag(tmp);
					break;
				case('p'):
					addPlayer(tmp);
					break;
				case('b'):
					addBall();
					break;
				default:
			}
		}
		
	}
 
	private void addBall() {
		// TODO Auto-generated method stub
		Ball ball = new Ball();
		ball.distance = getInt();
		ball.degree = getInt();
		ball.distanceChange = getInt();
		ball.degreeChange = getInt();
		model.addBall(ball);
		System.err.println("ball added");
	}

	private void addPlayer(String playerProperties) {
		// TODO Auto-generated method stub
		String[] props = playerProperties.split("\\s\"|\\s|\"\\s");
		Player player = new Player();
		player.team = props[1];
		player.Unum = Integer.parseInt(props[2]);
		player.distance = getInt();
		player.degree = getInt();
		player.distanceChange = getInt();
		player.degreeChange = getInt();
		model.addPlayer(player);
		
	}

	private void addFlag(String flagName) {
		String[] props = flagName.split("\\s+");
		Flag f = new Flag(props[0].charAt(0));
		
		for(int i = 1; i < props.length; i++){
			f.set(props[i]);
		}
		f.distance = getInt();
		f.degree = getInt();
		model.addFlag(f);
	}

	private String getString(){
		index++;
		String ret = "";
		while(msg.charAt(index)!=')'){
			ret+= msg.charAt(index);
			index++;
		}
		index+=2;
		return ret;
	}
	private int getInt(){
		String inte = "";
		while(msg.charAt(index)!=' ' && msg.charAt(index)!=')'){
			inte += msg.charAt(index);
			index++;
		}
		if(msg.charAt(index)==')'){
			index++;
		}
		index++;
		if(inte.contains(".")){
			return Math.round(Float.valueOf(inte));
		}
		return Integer.parseInt(inte);
	}
	
	private double getDouble(){
		String dobe = "";
		while(msg.charAt(index)!=' '){
			dobe += msg.charAt(index);
			index++;
		}
		index++;
		return Double.parseDouble(dobe);
	}

	private void initMsg() {
		String[] msgA = msg.split("\\s+");
		model.setFieldSide(msgA[1].charAt(0));
		model.setUnum(Integer.parseInt(msgA[2]));
	}

	void quit() {
		socket.close();
	}
}
