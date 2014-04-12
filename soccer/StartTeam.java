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

		// Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win/rcssserver.exe",
        // null, new File("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win"));
        // Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win/rcssmonitor.exe",
        // null, new File("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win"));
        for(int j = 0; j <10; j++){
        ProcessBuilder pb = new ProcessBuilder("C:\\Users\\Alexander\\Documents\\Skola\\kex\\rcssserver-15.0.0-win\\rcssserver.exe");
        pb.directory(new File("C:\\Users\\Alexander\\Documents\\Skola\\kex\\rcssserver-15.0.0-win\\"));
        Process server = pb.start();

        try {

            Thread.sleep(1 * 1 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        Process monitor = new ProcessBuilder("C:\\Users\\Alexander\\Documents\\Skola\\kex\\rcssmonitor-14.1.0-win\\rcssmonitor.exe").start();

        RoboCup rc = new RoboCup();
        rc.start();
        
      //    try {
        //      Thread.sleep(1 * 10 * 1000);
        //  } catch (InterruptedException ex) {
        //      Thread.currentThread().interrupt();
        //  }
        String[] first = {"Learn", "localhost", "6000"};
        // String[] second = {"Dumb", "localhost", "6000"};
        String[] coach = {"Learn", "localhost", "6001"};
        // String[] first = { "MyTeam", "192.168.1.4", "6000" };
        // String[] second = { "MyTheme", "192.168.1.4", "6000" };
        if (args.length > 0) {
            first[0] = args[0];
        }

        
        try {

            Thread.sleep(1 * 3 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        try {

            for (Pos p : Pos.values()) {
                (new SoccerAgent(first, new Formation(p))).start();
                // (new SoccerAgentDumb(second, new Formation(p))).start();
            }
            Coach coachAgent = new Coach(coach);
            coachAgent.start();
            
          coachAgent.join();

        } catch (NumberFormatException | UnknownHostException | SocketException | InterruptedException e) {

        }
        
        
        try {

            Thread.sleep(1 * 5 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        server.destroy();
    
        monitor.destroy();
                try {

            Thread.sleep(1 * 5 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        
        
        
        if(rc.isAlive()){  
            rc.run= false;
        }
       
        }
    }

    

}
