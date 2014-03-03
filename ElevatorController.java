

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * This is the controller for one elevator. It gets relevant input for this elevator.
 * 
 * @author Alexander Gomez
 *
 */
public class ElevatorController extends Thread {
	int position = 0;
	int elevator;
	boolean betweenFloors = false;
	boolean moving = false;
	LinkedBlockingQueue<String[]> in;
	Socket socket;
	boolean[] stops = new boolean[7];
	boolean up = true;
	String[] input;
	ControllerFilter filter;
	/**
	 * The load is the total distance this elevator is scheduled to travel.
	 */
	int load = 0;

	/**
	 * 
	 * @param controllerFilter the filter that has the out-pipe
	 * @param elevator the number of the elevator this controller is associated with
	 */
	public ElevatorController(ControllerFilter controllerFilter, int elevator) {

		this.elevator = elevator;
		in = new LinkedBlockingQueue<String[]>();
		filter = controllerFilter;
		System.out.println("created a controller");

	}

	public void run() {
		try {
			boolean stateChange = false;
			input = in.take();
			while (input.length > 0) {
				// the elevator has a new position
				if (input[0].equals("f") && moving) {
					position = Integer.valueOf(input[2]);
					// new floor do we stop?
					stateChange = arrivedAtFloor();
					// what should we do now?
					if(stateChange){
						nowWhat();
					}
				} else
				// a button is pressed
				if (input[0].equals("p") || input[0].equals("b")) {
					int buttonPressed = Integer.valueOf(input[2]);
					if(buttonPressed>3000){
						//System.out.println("emergency stop");
						emergencyStop();
						buttonPressed = Integer.valueOf(input[2]);
						
					}
					if (input[0].equals("b")) {
						buttonPressed = Integer.valueOf(input[1]);
					}
					stops[buttonPressed] = true;
					if (position == buttonPressed && !betweenFloors) {
						arrivedAtFloor();
					}
					if (!moving) {
						nowWhat();//?
					}

				}
				/*
				System.out.print("Controller: ");
				for(int i = 0; i < input.length; i++){
					System.out.print(" " + input[i]);
				}
				System.out.println();
				*/
				input = in.take();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
	/**
	 * when the stop is pressed we stop the elevator and wait for another
	 * button to be pressed.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void emergencyStop() throws IOException, InterruptedException{
		filter.pipe("m " + elevator + " 0");
		moving = false;
		betweenFloors = true;
		filter.pipe("d " + elevator + " 1");
		sleep(2000);
		//System.out.println("Controller: " + input[0]);
		input = in.take();
		while(input[0].equals("f") || (input[0].equals("p") && Integer.valueOf(input[2])>3000)){
			//System.out.println("Controller: " + input[0]);
			input = in.take();
		}
		
		filter.pipe("d " + elevator + " -1");
		
	}
	/**
	 * This method decides where to move the elevator
	 * @throws IOException
	 */
	private void nowWhat() throws IOException {
		
		int upPresses, downPresses, downDistance, upDistance;
		boolean down;
		down = true;
		upPresses = downPresses = downDistance = upDistance = 0;
		for (int i = 0; i < stops.length; i++) {
			if (stops[i]) {
				if (i < position) {
					if (down) {
						downDistance = position - i;
						down = false;
					}
					downPresses++;
				} else if (i > position) {
					upDistance = i - position;
					upPresses++;
				}
			}
		}
		/**
		 * Calculate most presses/distance to get the highest 
		 * throughput
		 */
		if (upPresses > 0 && downPresses > 0) {
			if ((upPresses / upDistance) < (downPresses / downDistance)) {
				filter.pipe("m " + elevator + " -1");
				betweenFloors = true;
				up = false;
			} else {
				filter.pipe("m " + elevator + " 1");
				betweenFloors = true;
				up = true;
			}
			moving = true;
		} else if (downPresses > 0) {
			filter.pipe("m " + elevator + " -1");
			betweenFloors = true;
//			if(downPresses == 1){
//				downDistance = 0;
//			}
			moving = true;
			up = false;
		} else if (upPresses > 0) {
			filter.pipe("m " + elevator + " 1");
			betweenFloors = true;
//			if(upPresses == 1){
//				upDistance = 0;
//			}
			moving = true;
			up = true;
		}

		load = upDistance + downDistance;
	}

	/**
	 * Decides if we need to stop or do nothing
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean arrivedAtFloor() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		betweenFloors = false;
		if (stops[position]) {
			filter.pipe("m " + elevator + " 0");
			stops[position] = false;
			moving = false;
			openDoors();
			return true;
		}
		return false;
	}

	/**
	 * open doors and sleeps for 2 seconds
	 * close doors and sleep for .7 seconds so it doesn't move
	 * until the doors close.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void openDoors() throws IOException, InterruptedException {
		filter.pipe("d " + elevator + " 1");
		sleep(2000);
		filter.pipe("d " + elevator + " -1");
		sleep(700);
	}
	/**
	 * The input for this elevator-controller that is relevant for the corresponding elevator.
	 * @param offering the input from the view
	 * @return if succeeded 
	 */
	public boolean offer(String[] offering) {
		return in.offer(offering);
	}
}