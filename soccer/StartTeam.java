package soccer;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class StartTeam {

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

    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win/rcssserver.exe", null, new File("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win"));
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win/rcssmonitor.exe", null, new File("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win"));
        

        String[] first = {"MyTeam", "localhost", "6000"};
        String[] second = {"MyTheme", "localhost", "6000"};
		//String[] first = { "MyTeam", "192.168.1.4", "6000" };
        //String[] second = { "MyTheme", "192.168.1.4", "6000"  };
        if (args.length > 0) {
            first[0] = args[0];
        }
        try {
            /*
             for(Formation f : Formation.values()){
             (new SoccerAgent(first,  f)).start();
             (new SoccerAgent(second, f)).start();
             }
             */
                    (new SoccerAgent(first,  Formation.FC)).start();
//                    (new SoccerAgent(second, Formation.GOALIE)).start();
//                    (new SoccerAgent(first,  Formation.HBC)).start();
//                    (new SoccerAgent(second, Formation.HBC)).start();

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

    }

}
