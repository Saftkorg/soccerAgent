package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
                if(socket.isClosed()){
                    return false;
                }
		if (msg != null) {
                        //System.err.print(model.time);
                        //System.err.print("Sending ");
                        //System.err.println(msg);
                        msg+=(char)0;
			packet = new DatagramPacket(msg.getBytes(), msg.length(), host,
					port);
			//boolean suc=true;
                        //while(suc){
                            try {
				socket.send(packet);
                                //suc = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
                                //if(!socket.isConnected()){
                                //    suc = true;
                                //    continue;
                                //}
                                System.err.println("not send");
				socket.close();
				return false;
			}
                        //}
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
                        System.err.println("not receive");
                        
			socket.close();
                        if(socket.isClosed()){
                            System.err.println("socket closed");
                        }
			return false;
		}
		port = packet.getPort();
		return true;
	}

	private void parse() {

		// String msg = "(init l 1 before_kick_off)";
                msg = msg.trim();
                //System.err.println(msg);
		if(msg.startsWith("(see")){
                    //System.err.println(model.team + " " +model.unum+ ": " +msg);
			seeMsg();
		}else if(msg.startsWith("(player")){
                    //System.err.println(msg);
			
		}else if(msg.startsWith("(init")){
			initMsg();
		}else if(msg.startsWith("(sense_body")){
                    //System.err.println(msg);
                    senseMsg();
                    
                }else if(msg.startsWith("(server_param")){
                    
                    //System.err.println(msg);
                }else{
                    //System.err.println(msg);
                }
		
                
	}

	private void seeMsg() {
                
                if(msg.length()<11){
                   model.time(Integer.parseInt(msg.substring(5, msg.indexOf(')', 5))));
                    return;
                }
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
                if(!socket.isClosed()){
                    send("(bye)");
                    socket.close();
                }
	}

    private void senseMsg() {
        //(sense_body 36 (view_mode high normal) (stamina 7850 1 129025) (speed 0.27 1) (head_angle 0) ...ne)) 
        msg = msg.substring(msg.indexOf("(view_mode "), msg.indexOf(" (kick "));
        String msgA[] = msg.split("\\(|\\)\\s\\(|\\)");
        for(String s: msgA){
            if(s.length()>0){
                model.body.change(s);
            }
        }
    }
}
