package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class CoachCom{

    private String msg;

    private final InetAddress host;
    private int port;
    private final CoachModel model;

    private final DatagramSocket socket;
    private DatagramPacket packet;

    public CoachCom(String host, int port, CoachModel model)
            throws UnknownHostException, SocketException {
        
        this.port = port;
        this.host = InetAddress.getByName(host);
	this.model = model;

        System.err.println("Connecting to " + host + ":" + port);

        socket = new DatagramSocket();
        socket.setSoTimeout(3000);
    }

    boolean send(String msg) {
        if (socket.isClosed()) {
            return false;
        }
        if (msg != null) {
            System.err.println(msg);
            msg += (char) 0;
            packet = new DatagramPacket(msg.getBytes(), msg.length(), host,
                    port);
            try {
                socket.send(packet);
            } catch (IOException e) {
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
        } catch (IOException e) {
            socket.close();
            return false;
        }
        port = packet.getPort();
        return true;
    }

    private void parse() {
        
        msg = msg.trim();
        if (msg.startsWith("(see_global")) {
            seeMsg();
        } else if (msg.startsWith("(init")) {
            //initMsg();
        } else if (msg.startsWith("(server_param")) {

            //System.err.println(msg);
        } else if (msg.startsWith("(hear")) {
            System.err.println(msg);
            hearMsg();
        } else if(msg.startsWith("(ok team_names")){
            model.teamSide = msg.charAt(msg.indexOf(model.teamName)-2);
            
        }else{
            //System.err.println(msg);
        }
    }

    private void seeMsg() {
        model.time(Integer.parseInt(msg.substring(12, msg.indexOf(' ', 12))));
        
        if(msg.contains(" k)")){
            String tmp = msg.substring(msg.indexOf("(("), msg.indexOf(" k)"));
            tmp = tmp.substring(tmp.lastIndexOf("(("));
            
            if(tmp.contains("\"" + model.teamName + "\"")){
                model.score++;
            }else{
                model.score--;
            }
            System.err.println(msg);
        }
    }
    
    private void hearMsg(){
        
        if(msg.contains("goal_"+model.teamSide)){
            model.score += model.goalScore;
        }else if(msg.contains("goal_")){
            model.score += model.oppGoalScore;
        }else if(msg.contains("free_kick_fault_"+model.teamSide) ||
                msg.contains("back_pass_"+model.teamSide)){
            model.score--;
        }
        
        //(hear 88 referee goal_l_1)
    }
}
