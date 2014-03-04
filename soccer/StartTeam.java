package soccer;

import java.net.SocketException;
import java.net.UnknownHostException;

public class StartTeam {


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		String msg = "(init l 1 before_kick_off)";
		byte[] buffer = msg.getBytes();
		
		
		
		System.err.println(buffer[0]);
		*/
		

		String[] first = { "MyTeam", "localhost", "6000" };
		String[] second = { "MyTheme", "localhost", "6000"  };
		try {
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
