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
			
		
		
		
		ProcessBuilder pb = new ProcessBuilder("rcsoccersim");
		Process server = pb.start();

		try {
			Thread.sleep(1 * 1 * 500);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		ProcessBuilder pb5 = new ProcessBuilder("./opuci_player",
				"--player-config", "player.conf", "--config_dir",
				"formations-dt", "-h", "localhost", "-p", "6000", "-t",
				"opuci", "-g");
		pb5.directory(new File(
				"/home/alexander/Skola/kex/opuci_2d-robocup2010/src"));

		Process server5 = pb5.start();
		try {
			Thread.sleep(1 * 1 * 500);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		Process[] pList = new Process[10];
		ProcessBuilder pb6 = new ProcessBuilder("./opuci_player",
				"--player-config", "player.conf", "--config_dir",
				"formations-dt", "-h", "localhost", "-p", "6000", "-t", "opuci");
		pb6.directory(new File(
				"/home/alexander/Skola/kex/opuci_2d-robocup2010/src"));

		for (int i = 0; i < 10; i++) {
			pList[i] = pb6.start();

			try {
				Thread.sleep(1 * 1 * 500);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		/*
		 * ProcessBuilder pb = new ProcessBuilder(
		 * "C:\\Users\\Alexander\\Documents\\Alex Skola\\kex\\rcssserver-15.0.0-win\\rcssserver.exe"
		 * ); pb.directory(new File(
		 * "C:\\Users\\Alexander\\Documents\\Alex Skola\\kex\\rcssserver-15.0.0-win\\"
		 * )); Process server = pb.start();
		 * 
		 * 
		 * Process monitor = new ProcessBuilder(
		 * "C:\\Users\\Alexander\\Documents\\Alex Skola\\kex\\rcssmonitor-14.1.0-win\\rcssmonitor.exe"
		 * ).start();
		 */

		String[] first = { "Learn", "localhost", "6000" };
		// String[] second = {"Dumb", "localhost", "6000"};
		String[] coach = { "Learn", "localhost", "6001" };
		// String[] first = { "MyTeam", "192.168.1.4", "6000" };
		// String[] second = { "MyTheme", "192.168.1.4", "6000" };
		if (args.length > 0) {
			first[0] = args[0];
		}

		Coach coachAgent;
		try {

			Thread.sleep(1 * 2 * 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		try {

			for (Pos p : Pos.values()) {
				(new SoccerAgent(first, new Formation(p))).start();
				// (new SoccerAgentDumb(second, new Formation(p))).start();
			}
			coachAgent = new Coach(coach);
			coachAgent.start();
			coachAgent.join();

		} catch (NumberFormatException | UnknownHostException | SocketException
				| InterruptedException e) {

		}

		server.destroy();

		ProcessBuilder pb3 = new ProcessBuilder("pkill", "rcssmonitor");

		Process server3 = pb3.start();

		ProcessBuilder pb2 = new ProcessBuilder("pkill", "rcssserver");
		Process server2 = pb2.start();

		ProcessBuilder pb1 = new ProcessBuilder("pkill", "rcsoccersim");

		Process server1 = pb1.start();

		try {

			Thread.sleep(1 * 5 * 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		server5.destroy();
		for (int i = 0; i < 10; i++) {
			pList[i].destroy();
		}
		server2.destroy();

		server1.destroy();
		server3.destroy();

		System.err.println("startteam quit");
		}
	}

}
