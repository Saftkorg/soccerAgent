package soccer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Communicator {

    private String msg;

    private final InetAddress host;
    private int port;
    private final Model model;
    private int missedStates;

    private final DatagramSocket socket;
    private DatagramPacket packet;

    public Communicator(String host, int port, Model model)
            throws UnknownHostException, SocketException {
        this.port = port;
        this.host = InetAddress.getByName(host);
        this.model = model;
        missedStates = 0;
        System.err.println("Connecting to " + host + ":" + port);

        socket = new DatagramSocket();
        socket.setSoTimeout(100);
    }

    boolean send(String msg) {
        if (socket.isClosed()) {
            return false;
        }
        if (msg != null) {
        	
        		//System.err.println(model.team + model.unum+msg);
        	
        	
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
            missedStates = 0;
            this.msg = new String(buffer);
            parse();
        } catch (IOException e) {
        	missedStates++;
        	if(socket.isClosed()){
        		return false;
        	}else if(missedStates>30){
        		socket.close();
        		return false;
        	}else{
        		model.time++;
        		return true;
        	}
        }
        port = packet.getPort();
        return true;
    }

    private void parse() {
        msg = msg.trim();
        if (msg.startsWith("(see")) {
            seeMsg();
        } else if (msg.startsWith("(player")) {
        } else if (msg.startsWith("(init")) {
            initMsg();
        } else if (msg.startsWith("(sense_body")) {
            senseMsg();
        } else if (msg.startsWith("(server_param")) {

        } else if (msg.startsWith("(hear")) {
            //System.err.println(msg);
 
               hearMsg(); 
            
        } else {

        }
    }
    /**
     * 
     */
    private void seeMsg() {
        if (msg.length() < 11) {
            model.time(Integer.parseInt(msg.substring(5, msg.indexOf(')', 5))));
            return;
        }
        model.time(Integer.parseInt(msg.substring(5, msg.indexOf(' ', 5))));
        msg = msg.substring(msg.indexOf('(', 3) + 2, msg.length() - 2);
        String[] msgA = msg.split("\\)\\s\\(\\(");
        String[] msgTmp;
        for (String s : msgA) {
            msgTmp = s.split("\\)\\s");
            if (msgTmp.length > 1) {
                model.addObject(msgTmp[0], msgTmp[1]);
            }
        }
        
        if(model.lastKickCertainty && !model.ballInVision){
            model.lastKickCertainty = false;
        }
    }
    /**
     * 
     */
    private void initMsg() {
        String[] msgA = msg.split("\\s+");

        model.setFieldSide(msgA[1].charAt(0));
        model.setUnum(Integer.parseInt(msgA[2]));
    }

    void quit() {
        if (!socket.isClosed()) {
            send("(bye)");
            socket.close();
        }
    }

    private void senseMsg() {
        //(sense_body 36 (view_mode high normal) (stamina 7850 1 129025) (speed 0.27 1) (head_angle 0) ...ne)) 
        msg = msg.substring(msg.indexOf("(view_mode "), msg.indexOf(" (kick "));
        String msgA[] = msg.split("\\(|\\)\\s\\(|\\)");
        for (String s : msgA) {
            if (s.length() > 0) {
                model.body.change(s);
            }
        }
    }

    private void hearMsg() {
        
        if(msg.contains(" coach ")){
            
            String[] msgA = msg.substring(msg.indexOf('"')+1, msg.lastIndexOf('"')).split("\\+");
            int i = 0;
            for( ; i < 5 ; i++){
                model.farArray[i] = Double.parseDouble(msgA[i]);
            }
            
            for(int j = 0 ; j < 5 ; j++){
                model.medArray[j] = Double.parseDouble(msgA[i]);
                //System.err.println("med " + j + " " + model.medArray[j]);
                i++;
            }
            for(int j = 0 ; j < 5 ; j++){
                model.cloArray[j] = Double.parseDouble(msgA[i]);
                //System.err.println("close " + j + " " + model.cloArray[j]);
                i++;
            }
            
            for(int j = 5 ; j < 8 ; j++){
                model.hasArray[j] = Double.parseDouble(msgA[i]);
                i++;
            }
            for(int j = 0 ; j < 4 ; j++){
                model.nobArray[j] = Double.parseDouble(msgA[i]);
                i++;
            }
            model.parameters[Model.FAR_BALL_DIST] = Double.parseDouble(msgA[i]);
            i++;
            model.parameters[Model.MED_BALL_DIST] = Double.parseDouble(msgA[i]);
            i++;
            model.parameters[Model.DRB_DIST_EVAL] = Double.parseDouble(msgA[i]);
            i++;
            model.parameters[Model.KCK_GOALDIST_EVAL] = Double.parseDouble(msgA[i]);
            i++;
            model.parameters[Model.PAS_PLYRDIST_EVAL] = Double.parseDouble(msgA[i]);
            i++;
            model.parameters[Model.GTB_TURNBALLDIR] = Double.parseDouble(msgA[i]);
            
        }else if(msg.contains("goal_")){
            model.freeMove = true;
        }else if(msg.contains("kick_off_"+model.field_side)){
        	model.kickoff = true;
        	
        }else if(msg.contains("play_on")){
        	model.kickoff = false;
        	model.freeMove = false;
        	model.kickIn = false;
        	model.otherKick = false;
        }else if(msg.contains("kick_in_"+model.field_side) ||msg.contains("free_kick_"+model.field_side)){
        	model.kickIn = true;
        }else if(msg.contains("kick_in_"+model.opp_field_side) || msg.contains("free_kick_"+model.opp_field_side) ||
        		msg.contains("kick_off_"+model.opp_field_side) ||  msg.contains("goal_kick_"+model.opp_field_side) ){
        	model.otherKick = true;
        }else if(msg.contains("time_over")){
        	quit();
        }
        
        
        
        //if(model.team.equals("Learn") && model.unum == 4){
        //	System.err.println(msg);
        //}
        
    }
}
