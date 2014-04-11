/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package soccer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 
 * @author Alexander
 */
public class Coach extends Thread {

	CoachCom cc;// = new CoachCom(coach[0], Integer.parseInt(coach[1]));
	CoachModel cm;
	private int matchNumber;
	private String directory = "/home/alexander/Skola/kex/game";

	// while(

	Coach(String[] teamAdrPrt) throws UnknownHostException, SocketException {
		cm = new CoachModel();
		cm.teamName = teamAdrPrt[0];
		cc = new CoachCom(teamAdrPrt[1], Integer.parseInt(teamAdrPrt[2]), cm);
		matchNumber = 0;
		File f = new File(directory+matchNumber+".txt");
		
		if (f.exists() && !f.isDirectory()) {
			for (int i = 1; i < 100; i++) {
				File ftmp = new File(directory + i
						+ ".txt");
				if (ftmp.exists() && !ftmp.isDirectory()) {
					f = ftmp;
					matchNumber = i;
				} else {
					break;
				}
			}
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = br.readLine();
				String[] lvalues = line.split("_");
				
				
				int count = 0;
				for (CoachModel.ParamElement pe : cm.paramList) {
					if (lvalues.length > count) {
						pe.value = Double.parseDouble(lvalues[count]);
						
						count++;
					}else{
						break;
					}
				}
				
				br.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			matchNumber--;
		}
		

	}

	@Override
	public void run() {
		String msg = cm.initMsg;
		try {

			Thread.sleep(1 * 2 * 1000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		cc.send(msg);
		msg = "(team_names)";
		cc.send(msg);

		msg = "(say " + cm.getValues() + ")";

		cc.send(msg);
		msg = "(start)";
		cc.send(msg);
		msg = "(ear on)";
		cc.send(msg);
		msg = "(eye on)";
		int update = cm.timeInterval - 1;
		int reward = cm.timeInterval;

		while (cc.send(msg)) {
			msg = null;
			if (cm.halfTime) {
				msg = "(start)";
				cm.halfTime = false;
				continue;
			}
			if (cm.time >= update) {
				// TODO make scores, scores are made in cc
				msg = "(say " + cm.getValues() + ")";

				update += cm.timeInterval;

			}
			if (cm.time >= reward) {

				cm.endTurn();
				
				reward += cm.timeInterval;
			}
			//if (cm.time >= 6000) {
			if (cm.time >= 1000) {
				cm.endPeriod();
				break;
			}
		}
		cc.quit();
		
		saveParams();
		
	}

	private void saveParams() {
		matchNumber++;
		File f = new File(directory+matchNumber+".txt");
		StringBuilder sb = new StringBuilder();
		for (CoachModel.ParamElement pe : cm.paramList) {
			sb.append(pe.value);
			sb.append("_");
		}
		sb.deleteCharAt(sb.lastIndexOf("_"));
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(sb.toString());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
