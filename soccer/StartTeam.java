package soccer;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import soccer.Formation.Pos;

public class StartTeam {
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win/rcssserver.exe", null, new File("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win"));
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win/rcssmonitor.exe", null, new File("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win"));
      
        ProcessBuilder pb = new ProcessBuilder("C:\\Users\\Alexander\\Documents\\Skola\\kex\\rcssserver-15.0.0-win\\rcssserver.exe");
       pb.directory(new File("C:\\Users\\Alexander\\Documents\\Skola\\kex\\rcssserver-15.0.0-win\\"));
        Process server = pb.start();
//InputStream serverIS = server.getInputStream();
        //byte[] buffer = new byte[4096];
        //try {
        //    Thread.sleep(1 * 4 * 1000);
        //} catch (InterruptedException ex) {
        //    Thread.currentThread().interrupt();
        //}
        //serverIS.read(buffer);
        //System.err.println(new String(buffer));
        
       //ProcessBuilder builder = new ProcessBuilder(new String[] { "cmd.exe", "/C", "\"C:\\Users\\Alexander\\Documents\\Alex Skola\\kex\\rcssserver-15.0.0-win\\rcssserver.exe\"" });
        //ProcessBuilder builder = new ProcessBuilder(new String[] { "cmd.exe", "/C", "start" });
       //Process server = builder.start();
       
       
        
        Process monitor = new ProcessBuilder("C:\\Users\\Alexander\\Documents\\Skola\\kex\\rcssmonitor-14.1.0-win\\rcssmonitor.exe").start();

        String[] first = {"Learn", "localhost", "6000"};
        String[] second = {"Dumb", "localhost", "6000"};
        String[] coach = {"Learn", "localhost", "6001"};
        //String[] first = { "MyTeam", "192.168.1.4", "6000" };
        //String[] second = { "MyTheme", "192.168.1.4", "6000"  };
        if (args.length > 0) {
            first[0] = args[0];
        }
        
        
        try {
            
            Thread.sleep(1 * 2 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        
        try {

            for (Pos p : Pos.values()) {
                (new SoccerAgent(first, new Formation(p))).start();
                (new SoccerAgentDumb(second, new Formation(p))).start();
            }
            
            (new Coach(coach)).start();
            //(new SoccerAgent(first, new Formation(Pos.FBT))).start();
           //(new SoccerAgent(second, new Formation(Pos.FBB))).start();
            
            
     //              (new SoccerAgent(second, new Formation(Pos.GOALIE))).start();
     //               (new SoccerAgent(first,  new Formation(Pos.FC))).start();
    //               (new SoccerAgent(second, new Formation(Pos.FBT))).start();
           
           
        } catch (NumberFormatException | UnknownHostException | SocketException e) {
            
        }

        
        
        try {
            
            Thread.sleep(1 * 60 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        //serverIS.close();
        server.destroy();
        
        monitor.destroy();
        
        /*
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            public void run() {
                server.destroy();
                monitor.destroy();
                System.err.println("timer quit");

                //System.exit(0);
            }
        }, 1 * 10 * 1000);
*/
        System.err.println("startteam quit");
    }

}
