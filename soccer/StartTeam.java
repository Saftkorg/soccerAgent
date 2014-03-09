package soccer;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;


public class StartTeam {

	

	/**
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win/rcssserver.exe", null, new File("C:/Users/Viktor/rcssserver/rcssserver-15.0.0-win"));
//		Runtime.getRuntime().exec("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win/rcssmonitor.exe", null, new File("C:/Users/Viktor/rcssserver/rcssmonitor-14.1.0-win"));

		String[] first = { "MyTeam", "localhost", "6000" };
		String[] second = { "MyTheme", "localhost", "6000"  };
		//String[] first = { "MyTeam", "192.168.1.4", "6000" };
		//String[] second = { "MyTheme", "192.168.1.4", "6000"  };
		if(args.length > 0){
                    first[0] = args[0];
                }
                try {
			(new SoccerAgent(first)).start();
			(new SoccerAgent(second)).start();
			(new SoccerAgent(first)).start();
			(new SoccerAgent(second)).start();
			(new SoccerAgent(first)).start();
			(new SoccerAgent(second)).start();
			
			
			
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
