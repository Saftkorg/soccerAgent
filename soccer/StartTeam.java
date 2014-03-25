package soccer;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;

import soccer.Formation.Pos;

public class StartTeam {
    /*
     public enum Formation {

     GOALIE(-50, 0, true),
     FBB(-35, 20),
     FBCB(-35, 7),
     FBCT(-35, -7),
     FBT(-35, -20),
     HBB(-20, 14),
     HBT(-20, -14),
     HBC(-20, 0),
     FT(-6, -17),
     FC(-10, 0),
     FB(-6, 17);
     int x;
     int y;
     boolean goalie;

     Formation(int x, int y) {
     this.x = x;
     this.y = y;
     this.goalie = false;
     }

     Formation(int x, int y, boolean goalie) {
     this.x = x;
     this.y = y;
     this.goalie = goalie;
     }
     }
     */

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win/rcssserver.exe", null, new File("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win"));
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win/rcssmonitor.exe", null, new File("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win"));

        final Process server = new ProcessBuilder("C:\\Users\\Alexander\\Documents\\Alex Skola\\kex\\rcssserver-15.0.0-win\\rcssserver.exe").start();
        InputStream serverIS = server.getInputStream();
        serverIS.read();
        serverIS.close();

        final Process monitor = new ProcessBuilder("C:\\Users\\Alexander\\Documents\\Alex Skola\\kex\\rcssmonitor-14.1.0-win\\rcssmonitor.exe").start();

        String[] first = {"MyTeam", "localhost", "6000"};
        String[] second = {"MyTheme", "localhost", "6000"};
        //String[] first = { "MyTeam", "192.168.1.4", "6000" };
        //String[] second = { "MyTheme", "192.168.1.4", "6000"  };
        if (args.length > 0) {
            first[0] = args[0];
        }
        try {

            for (Pos p : Pos.values()) {
                (new SoccerAgent(first, new Formation(p))).start();
                (new SoccerAgent(second, new Formation(p))).start();
            }
       //     (new SoccerAgent(first, new Formation(Pos.FBT))).start();
      //      (new SoccerAgent(first, new Formation(Pos.FBB))).start();
     //              (new SoccerAgent(second, new Formation(Pos.GOALIE))).start();
     //               (new SoccerAgent(first,  new Formation(Pos.FC))).start();
    //               (new SoccerAgent(second, new Formation(Pos.FBT))).start();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

      

        try {
            
            Thread.sleep(1 * 20 * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
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
