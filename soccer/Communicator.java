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
	Model model;

	private DatagramSocket socket;
	private DatagramPacket packet;


	public Communicator(String host, int port, Model model)
			throws UnknownHostException, SocketException {
		this.port = port;
		this.host = InetAddress.getByName(host);
		this.model = model;
		
		System.err.println("Connecting to " + host + ":" + port);

		socket = new DatagramSocket();
		socket.setSoTimeout(3000);
	}

	boolean send(String msg) {
		if (msg != null) {
                        msg+=(char)0;
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
			//System.err.println(msg);
			parse();

			
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
				case('p'):
					addPlayer(tmp);
					break;
				case('b'):
					addBall();
					break;
				default:
                                    addFlag(tmp);
                                    break;
			}
		}
		
	}
 
	private void addBall() {
		// TODO Auto-generated method stub
		Ball ball = new Ball();
		ball.distance = getInt();
		ball.degree = getInt();
		if(msg.charAt(index-2)!=')'){
			ball.distanceChange = getInt();
		}
		if(msg.charAt(index-2)!=')'){
			ball.degreeChange = getInt();
		}
		model.addBall(ball);
		
	}

	private void addPlayer(String playerProperties) {
		// TODO Auto-generated method stub
		String[] props = playerProperties.split("\\s\"|\\s|\"\\s");
		Player player = new Player();
		if(props.length>1){
		player.team = props[1];
		}
		if(props.length>2){
			player.Unum = Integer.parseInt(props[2]);
		}
		
		player.distance = getInt();
		player.degree = getInt();
		if(msg.charAt(index-2)!=')'){
                    if(msg.charAt(index)=='k'){
                        index+=3;
                    }else{
                        player.distanceChange = getInt();
                    }
		}
		if(msg.charAt(index-2)!=')'){
			player.degreeChange = getInt();
		}
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
		if(msg.charAt(index-2)!=')'){
			f.distanceChange = getInt();
		}
		if(msg.charAt(index-2)!=')'){
			f.degreeChange = getInt();
		}
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
