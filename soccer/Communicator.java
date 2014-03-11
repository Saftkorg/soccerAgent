package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Communicator {

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
                    //System.err.println(model.team + " " +model.Unum+ ": " +msg);
			seeMsg();
		}else if(msg.startsWith("(player")){
                    //System.err.println(msg);
			
		}else if(msg.startsWith("(init")){
			initMsg();
		}else if(msg.startsWith("(sense_body")){
                    
                }else{
                    //System.err.println(msg);
                }
		
	}

	private void seeMsg() {
		msg = msg.trim();
		model.time(Integer.parseInt(msg.substring(5, msg.indexOf(' ', 5))));
                msg = msg.substring(msg.indexOf('(', 3)+2,msg.length()-2);
                String[] msgA = msg.split("\\)\\s\\(\\(");
                String[] msgTmp;
		for(String s: msgA){
                    msgTmp = s.split("\\)\\s");
                    if(msgTmp.length > 1){
                        model.addObject(msgTmp[0],msgTmp[1]);
                    }
                }
	}
 
	private void initMsg() {
		String[] msgA = msg.split("\\s+");
		model.setFieldSide(msgA[1].charAt(0));
		model.setUnum(Integer.parseInt(msgA[2]));
	}


	void quit() {
                send("(bye)");
		socket.close();
	}
}
