package soccer;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


/**
 * This is the main class of the RoboCup Soccer Agent
 * 
 * @author Gavelli, Viktor
 * @author Gomez, Alexander
 * 
 */
public class SoccerAgent extends Thread {

	private final Model model;
	private final Communicator com;

	// private final double DEGREE_DELTA = 20.0;
	private final Queue<String> commands;

	// private final boolean avoidFKF = false;
	private final Random r = new Random();

	private final Formation f;

	// int x;
	// int y;
	// boolean goalie;

	/**
	 * 
	 * @param args
	 * @param f
	 * @throws SocketException
	 * @throws UnknownHostException
	 * @throws NumberFormatException
	 */

	public SoccerAgent(String[] args, Formation f)
			throws NumberFormatException, UnknownHostException, SocketException {
		commands = new LinkedList();
		this.f = f;
		// x = f.x;
		// y = f.y;
		// goalie = f.goalie;
		model = new Model(args[0], f.goalie);

		com = new Communicator(args[1], Integer.parseInt(args[2]), model);

	}

	private double[] getWeights() {
		// int holdW, coverPW, coverGW, getFreeW,
		// goToBallW,dribbleW,kickW,passW, catchW;

		if (model.ballInVision) {
			model.ballNotInVisionCount = 0;

			// if (model.ball.distance > 35.0) {
			if (model.ball.distance > model.parameters[Model.FAR_BALL_DIST]) {

				return model.farArray;
				// int[] retint = {1, 0, 0, 0, 0, 0, 0, 0, 0};
				// return retint;

			} else if (model.ball.distance > model.parameters[Model.MED_BALL_DIST]) {
				// int[] retint = {0, 1, 0, 1, 0, 0, 0, 0, 0};
				return model.medArray;
				// double[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
				// return retint;
			} else if (model.ball.distance > 0.7) {
				// int[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
				return model.cloArray;
				// double[] retint = {0, 0, 0, 0, 1, 0, 0, 0, 0};
				// return retint;
			} else {
				// int[] retint = {0, 0, 0, 0, 0, 1, 1, 1, 0};
				return model.hasArray;
				// double[] retint = {0, 0, 0, 0, 0, 1, 1, 1, 0};
				// return retint;
			}
		} else {
			model.ballNotInVisionCount++;
			return model.nobArray;
			// double[] retint = {1, 0, 0, 0, 0, 0, 0, 0, 0};
			// return retint;
		}
	}

	@Override
	public void run() {


		String msg;
		int connectCount = 0;
		for (; connectCount < 2; connectCount++) {
			if (com.send(model.initMsg)) {
				break;
			}
		}
		if (connectCount == 2) {
			com.quit();
			System.err.println("NOPE!");
			return;
		}

		f.setSide(model.field_side);
		msg = "(move " + f.x + " " + f.y + ")";
		int lastTime = 0;
		com.send(msg);
		model.time(0);
		while (com.send(msg)) {
			msg = null;
			if (model.freeMove) {
				msg = "(move " + f.x + " " + f.y + ")";
				model.freeMove = false;
			} else if (lastTime != model.time) {
				lastTime = model.time;

				if (!commands.isEmpty()) {
					msg = commands.poll();
					continue;
				}

				if (model.kickoff || model.kickIn) {
					if (!model.ballInVision) {
						lookForBallAction();
					} else {
						if (goToBallEval() > 0 && model.ball.distance > 0.7) {
							goToBallAction();
						} else if (model.ball.distance <= 0.7) {
							if (passEval() > 0) {
								passAction();
							} else {
								commands.add("(dash 10 " + model.ball.direction+ ")");
								commands.add("(turn -45)");
							}
						}
					}
					continue;
				}else if(model.otherKick){
					if(!model.ballInVision){
						lookForBallAction();
					}
					continue;
				}

				double[] actionW = getWeights();
				double[] actionScore = new double[actionW.length];

				// int holdW, coverPW, coverGW, getFreeW,
				// goToBallW,dribbleW,kickW,passW, catchW;
				for (int i = 0; i < actionW.length; i++) {

					if (actionW[i] != 0 && model.ballNotInVisionCount < 10) {

						switch (i) {
						case (0):
							actionScore[i] = actionW[i] * holdEval();

							break;
						case (1):
							actionScore[i] = actionW[i] * coverPEval();

							break;
						case (2):
							actionScore[i] = actionW[i] * coverGEval();

							break;
						case (3):
							actionScore[i] = actionW[i] * getFreeEval();

							break;
						case (4):
							actionScore[i] = actionW[i] * goToBallEval();

							break;
						case (5):
							actionScore[i] = actionW[i] * dribbleEval();

							break;
						case (6):
							actionScore[i] = actionW[i] * kickEval();

							break;
						case (7):
							actionScore[i] = actionW[i] * passEval();

							break;
						case (8):
							actionScore[i] = actionW[i] * catchEval();

							break;
						}
					} else {
						actionScore[i] = 0;
					}
				}

				double maxScore = 0;
				int maxInt = -1;
				for (int i = 0; i < actionScore.length; i++) {
					
					if (actionScore[i] > maxScore) {
						maxScore = actionScore[i];
						maxInt = i;
					}
				}
				// System.err.print(model.team+model.unum);
				if (maxInt != -1) {
					switch (maxInt) {
					case (0):
						// System.err.println(" hold");
						holdAction();
						break;
					case (1):
						// System.err.println(" cover");
						coverPAction();
						break;
					case (2):
						coverGAction();
						break;
					case (3):
						// System.err.println(" getFree");
						getFreeAction();
						break;
					case (4):
						// System.err.println(" gotoball");
						goToBallAction();
						break;
					case (5):
						// System.err.println(" dribble");
						dribbleAction();
						break;
					case (6):
						// System.err.println(" kcik");
						kickAction();
						break;
					case (7):
						// System.err.println(" pass");
						passAction();
						break;
					case (8):
						catchAction();
						break;
					}
				} else {
					// System.err.println(" lookforball");
					lookForBallAction();
				}
				if (!commands.isEmpty()) {
					msg = commands.poll();
				}

			}
		}
		com.quit();
		System.err.println("quitting agent");
	}

	/**
	 * evaluates if it should hold position
	 * 
	 * @return
	 */
	private double holdEval() {
		double ret = 0;
		// String[] lines = {"l t", "l b", "l r", "l l"};

		// double dist;
		
		int flagsee = 0;
		
		for (Threshold th : f.thresholds) {

			FieldObject fo = model.flags.get(th.name);
			if (fo != null) {
				flagsee++;
				// System.err.println(th.name + " dist: " + fo.distance +
				// " dir: " + fo.direction);
				fo.distance = Math.abs(fo.distance
						* Math.sin(Math.toRadians(fo.direction)));

				if (th.name.charAt(th.name.length() - 1) == model.opp_field_side) {
					if (model.ballInVision) {
						model.threshold_adjuster = (model.ball.distance - f.away_from_ball) > 0 ? (model.ball.distance - f.away_from_ball)
								: 0;

					}
					if (fo.distance < th.min - model.threshold_adjuster
							|| fo.distance > th.max - model.threshold_adjuster) {
						ret += (Math
								.max(((th.min - model.threshold_adjuster) - fo.distance),
										0) + Math.max(fo.distance
								- (th.max - model.threshold_adjuster), 0));
					}

				} else if (th.name.charAt(th.name.length() - 1) == model.field_side) {

					if (model.ballInVision) {
						model.threshold_adjuster = 0;

					}
					if (fo.distance < th.min + model.threshold_adjuster
							|| fo.distance > th.max + model.threshold_adjuster) {
						ret += (Math
								.max(((th.min + model.threshold_adjuster) - fo.distance),
										0) + Math.max(fo.distance
								- (th.max + model.threshold_adjuster), 0));
						;
					}

				} else if (fo.distance < th.min || fo.distance > th.max) {
					ret += Math.max(th.min - fo.distance, 0)
							+ Math.max(fo.distance - th.max, 0);
				}
			}
		}

		return Math.log10((ret/flagsee) + 1)*0.7;
	}

	private int coverPEval() {
		int ret = 0;

		if (model.lastKickCertainty && !model.ourTeamLastKick) {

			for (Player pl : model.players) {
				if (model.team != null && !model.team.equals(pl.team)
						&& pl.distance < 20) {

					if (pl.distance < 3) {
						return 2; // TODO some other value
					} else {
						double dPlD = pl.distance * 1.8;
						for (Player pl1 : model.players) {
							if (model.team.equals(pl.team)) {
								if (pl.distance < dPlD
										&& proximityHelp(pl1.distance,
												pl1.direction, dPlD,
												pl.direction)) {

								} else {
									ret++;
								}
							}
						}
					}
				}
			}

		}
		return ret;
	}

	private int coverGEval() {
		if (model.ballInVision && model.goalie && model.ball.distance < 40)
			return 1;
		return 0;
	}

	private int getFreeEval() {
		int ret = 0;
		if (model.lastKickCertainty && model.ourTeamLastKick) {
			for (Player pl : model.players) {
				if (pl.team != null && !model.team.equals(pl.team)) {
					if (model.ballInVision
							&& pl.distance < model.ball.distance
							&& Math.abs(pl.direction - model.ball.direction) < 5) {
						ret++;
					}
				}
			}
		}
		return ret;
	}

	private double goToBallEval() {

		double dBallDist = 1.8 * model.ball.distance;// 1.9 *
														// model.ball.distance;
														// //double ball
														// distance
		double ret = -(Math.pow(model.ball.distance, 2.1) / 10000) + 1.2;// 1.2;
		if (model.ball.distChange != null && model.ball.distChange < -0.1) {
			return ret;
		}

		for (Player pl : model.players) {
			if (model.team.equals(pl.team)) {
				if (pl.distance < dBallDist) {
					if (proximityHelp(pl.distance, pl.direction, dBallDist,
							model.ball.direction)) {

						return 0; // TODO should make ret smaller rather than
									// returning zero
					}
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private double dribbleEval() {
		double ret = 0.0;
		for (Player pl : model.players) {
			if (pl.team != null && !model.team.equals(pl.team)
					&& pl.distance < model.parameters[model.DRB_DIST_EVAL]) {
				ret += pl.distance;
			}
		}
		return 1 / (ret + 1);
	}

	/**
	 * good to shoot but maybe need some more analysis of opponent players
	 * 
	 * @return
	 */
	private double kickEval() {

		if (model.goalInVision
				&& model.goal.distance < model.parameters[model.KCK_GOALDIST_EVAL]
				&& model.ball.distance < 0.8) {
			FieldObject top = model.flags.get("f g " + model.opp_field_side
					+ " t");
			FieldObject bot = model.flags.get("f g " + model.opp_field_side
					+ " b");
			double topDegree = 0;
			double botDegree = 0;
			if (top != null && bot != null) {
				topDegree = top.direction;
				botDegree = bot.direction;

			} else {
				double degree = Math.toDegrees(Math
						.asin(9 / model.goal.distance));
				topDegree = model.goal.direction - degree;
				botDegree = model.goal.direction + degree;
			}
			int topCount = 0;
			int bottomCount = 0;
			for (Player pl : model.players) {
				if (pl.team != null && !model.team.equals(pl.team)
						&& pl.direction > topDegree && pl.direction < botDegree) {
					if (pl.direction > model.goal.direction) {
						bottomCount++;
					} else {
						topCount++;
					}
				}
			}
			
			return Math.max(((botDegree - model.ball.direction) / (Math.max(
					bottomCount, 1))), (model.ball.direction - topDegree)
					/ Math.max(topCount, 1));
		} else {
			
			return 0;
		}
	}

	/**
	 * evaluates if its good to pass
	 * 
	 * @return
	 */
	private int passEval() {
		// int ret = 0;
		if (!model.ballInVision || model.ball.distance > 0.7) {
			return 0;
		}
		for (Player pl : model.players) {
			if (model.team.equals(pl.team)
					&& pl.distance < model.parameters[model.PAS_PLYRDIST_EVAL]) {
				boolean goodToPass = true;
				for (Player pl1 : model.players) {
					if (!model.team.equals(pl1.team)) {
						if (pl1.distance < pl.distance
								&& Math.abs(pl1.direction - pl.direction) < 2) {
							goodToPass = false;
							break;
						}
					}
				}
				if (goodToPass) {
					return 1;
				}
			}
		}
		return 0;
	}

	/**
	 * TODO:evaluates if we should catch
	 * 
	 * @return
	 */
	private int catchEval() {
		return 0;
	}

	/**
	 * Hold position consistent with the formation thresholds, angels given by
	 * the server for the lines aren't to the closest point
	 */
	private void holdAction() {
		// String[] lines = {"l t", "l b", "l r", "l l"};
		// for (String fl : lines) {
		for (Threshold th : f.thresholds) {
			FieldObject fo = model.flags.get(th.name);
			if (fo != null) {
				double adj = 0;
				if (th.name.charAt(th.name.length() - 1) == model.opp_field_side) {
					adj = model.threshold_adjuster;
				} else if (th.name.charAt(th.name.length() - 1) == model.field_side) {
					adj = -1 * model.threshold_adjuster;
				}

				if (fo.distance > th.max - adj) {
					if (Math.abs(fo.direction) < 88) {
						if (fo.direction < 0) {
							commands.add("(turn " + (fo.direction + 90) + ")");
						} else if (fo.direction > 0) {
							commands.add("(turn " + (fo.direction - 90) + ")");
						}
					}
					commands.add("(dash 75)");
					break;
				} else if (fo.distance < th.min - adj) {
					if (Math.abs(fo.direction) < 88) {
						if (fo.direction < 0) {
							commands.add("(turn " + (fo.direction - 90) + ")");
						} else if (fo.direction > 0) {
							commands.add("(turn " + (90 + fo.direction) + ")");
						}
					} else {
						commands.add("(turn 180)");
					}
					commands.add("(dash 75)");
					break;
				}
			}
		}
	}

	private void coverPAction() {
		Player coverMe = null;
		for (Player pl : model.players) {
			if (pl.team != null && !model.team.equals(pl.team)
					&& pl.distance < 20) {
				if (pl.distance < 3) {
					coverMe = pl;
					break;
				} else {
					double dPlD = pl.distance * 1.8;
					for (Player pl1 : model.players) {
						if (model.team.equals(pl.team)
								&& pl.distance < dPlD
								&& proximityHelp(pl1.distance, pl1.direction,
										dPlD, pl.direction)) {
							coverMe = pl;
							break;
						}
					}
				}
			}
		}
		if (coverMe != null && coverMe.distance > 0.7) {
			commands.add("(dash 30 " + coverMe.direction + ")");
		}

	}

	private void coverGAction() {
		char s = model.field_side;

		if (model.flags.containsKey("f p " + s + " c")) { // center penalty

			if (model.flags.containsKey("f c")) {

				FieldObject fpxc = model.flags.get("f p " + s + " c");

				FieldObject fc = model.flags.get("f c");

				//System.err.println("f p x c dist " + fpxc.distance + " dir "
				//		+ fpxc.direction + ", f c dist " + fc.distance
				//		+ " dir " + fc.direction);

				if (fc.direction - fpxc.direction > 1.0)
					commands.add("(dash 50 -90)");

				else if (fc.direction - fpxc.direction < -1.0)
					commands.add("(dash 50 90)");

				else if (fc.distance < 49)
					commands.add("(dash 50 180)");

				else if (fc.distance > 50)
					commands.add("(dash 50 )");

				return;

			}

		}

		else if (model.flags.containsKey("f p " + s + " b")) { // bottom penalty

			/*
			 * if(model.flags.containsKey("f " + s + " b")) {
			 * 
			 * 
			 * 
			 * } else
			 */if (model.flags.containsKey("f b " + s + " 20")) {

				FieldObject fpxb = model.flags.get("f p " + s + " b");

				FieldObject fbx20 = model.flags.get("f b " + s + " 20");

				//System.err.println("f p x b dist " + fpxb.distance + " dir "
				//		+ fpxb.direction + ", f b x 20 dist " + fbx20.distance
				//		+ " dir " + fbx20.direction);

				if (fbx20.direction - fpxb.direction > 1.0)
					commands.add("(dash 50 -90)");

				else if (fbx20.direction - fpxb.direction < -1.0)
					commands.add("(dash 50 90)");

				else if (fbx20.distance < 46)
					commands.add("(dash 50 180)");

				else if (fbx20.distance > 48)
					commands.add("(dash 50 0)");

				return;

			}

		}

		else if (model.flags.containsKey("f p " + s + " t")) { // top penalty

			/*
			 * if(model.flags.containsKey("f " + s + " t")) {
			 * 
			 * 
			 * 
			 * } else
			 */if (model.flags.containsKey("f t " + s + " 20")) {

				FieldObject fpxt = model.flags.get("f p " + s + " t");

				FieldObject ftx20 = model.flags.get("f t " + s + " 20");

				//System.err.println("f p x t dist " + fpxt.distance + " dir "
				//		+ fpxt.direction + ", f t x 20 dist " + ftx20.distance
				//		+ " dir " + ftx20.direction);

				if (ftx20.direction - fpxt.direction > 1.0)
					commands.add("(dash 50 -90)");

				else if (ftx20.direction - fpxt.direction < -1.0)
					commands.add("(dash 50 90)");

				else if (ftx20.distance < 46)
					commands.add("(dash 50 180)");

				else if (ftx20.distance > 48)
					commands.add("(dash 50 0)");

				return;

			}

		}

		//holdAction();

		// System.err.println("Goalie Cover Action");
	}

	private void getFreeAction() {
		int opLeft = 0;
		int opRight = 0;
		for (Player pl : model.players) {
			if (pl.team != null && !model.team.equals(pl.team)
					&& pl.distance < model.ball.distance) {
				if (pl.direction > model.ball.direction) {
					opRight++;
				} else {
					opLeft++;
				}
			}
		}
		FieldObject top = model.flags.get("l t");
		FieldObject bot = model.flags.get("l b");
		if (opRight > opLeft) { // go left
			if (top != null) {
				if (top.direction > 0) {
					commands.add("(turn " + (top.direction - 180) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(top.direction - 180)) + ")");
				} else {
					commands.add("(turn " + (top.direction) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(top.direction)) + ")");
				}

			} else if (bot != null) { // go left

				if (bot.direction > 0) {
					commands.add("(turn " + (bot.direction - 180) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(bot.direction - 180)) + ")");
				} else {
					commands.add("(turn " + (bot.direction) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(bot.direction)) + ")");
				}
			}
		} else {
			if (top != null) { // go right
				if (top.direction > 0) {
					commands.add("(turn " + (top.direction) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(top.direction)) + ")");
				} else {
					commands.add("(turn " + (top.direction + 180) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(top.direction + 180)) + ")");
				}

			} else if (bot != null) { // go right

				if (bot.direction > 0) {
					commands.add("(turn " + bot.direction + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-bot.direction) + ")");
				} else {
					commands.add("(turn " + (bot.direction + 180) + ")");
					commands.add("(dash 40)");
					commands.add("(turn " + (-(bot.direction + 180)) + ")");
				}
			}
		}
	}

	/**
	 * goes to the ball , will also somewhat intercept
	 */
	private void goToBallAction() {
		if (Math.abs(model.ball.direction) > model.parameters[model.GTB_TURNBALLDIR] /* 20 */) {

			if (model.ball.dirChange != null) {
				commands.add("(turn "
						+ (model.ball.direction + (model.ball.dirChange)) + ")");
			} else {
				commands.add("(turn " + model.ball.direction + ")");
			}
		} else if (model.ball.distance > 0.7) {
			if (model.ball.dirChange != null
					&& Math.abs(model.ball.dirChange) > 0.1
					&& Math.abs(model.ball.distChange) > 0.1) {
				double turnThis;
				if (model.ball.distChange < -0.1) {
					turnThis = (model.ball.direction + 2 * (Math.toDegrees(Math
							.asin((model.ball.distance * Math.sin(Math
									.toRadians(model.ball.dirChange)))
									/ Math.abs(model.ball.distChange)))));
				} else {
					turnThis = (model.ball.direction + (Math.toDegrees(Math
							.asin((model.ball.distance * Math.sin(Math
									.toRadians(model.ball.dirChange)))
									/ Math.abs(model.ball.distChange)))));
				}
				if (Double.isNaN(turnThis)) {
					turnThis = model.ball.direction;
				} else if (Math.abs(turnThis) > 90) {
					turnThis = Math.signum(turnThis) * 90;
				}
				commands.add("(dash " + 100 + " " + turnThis + ")");

			} else {
				commands.add("(dash 95)");
			}
		}

		/*
		 * if (model.ball.distance>30 && Math.abs(model.ball.direction)>3) {
		 * commands.add("(turn " +(model.ball.direction*(1+
		 * (model.ball.distance/60))) + ")"); model.ball.direction = 0;
		 * commands.add("(dash 100)"); } else if (model.ball.distance > 0.7) {
		 * 
		 * if (model.ball.dirChange != null && Math.abs(model.ball.dirChange) >
		 * 0.1 && Math.abs(model.ball.distChange) > 0.1) { double turnThis; if
		 * (model.ball.distChange < -0.1) { turnThis = (model.ball.direction +
		 * (Math.toDegrees(Math.asin((model.ball.distance *
		 * Math.sin(Math.toRadians(model.ball.dirChange))) /
		 * Math.abs(model.ball.distChange))))); } else { turnThis =
		 * (model.ball.direction +
		 * (Math.toDegrees(Math.asin((model.ball.distance *
		 * Math.sin(Math.toRadians(model.ball.dirChange))) /
		 * Math.abs(model.ball.distChange))))); } if (Double.isNaN(turnThis)) {
		 * turnThis = model.ball.direction; } else if (Math.abs(turnThis) > 90)
		 * { turnThis = Math.signum(turnThis) * 90; turnThis = turnThis /2;
		 * commands.add("(turn "+ turnThis + ")"); model.ball.direction = (int)
		 * (model.ball.direction -turnThis); }
		 * if((model.ball.distance+model.ball.distChange)>1){
		 * commands.add("(dash " + 100 + " " + turnThis + ")");
		 * model.ball.distance-= 0.4; } } else { if(model.body.amountOfSpeed<0.1
		 * && Math.abs(model.ball.direction)>3){
		 * commands.add("(turn "+(model.ball.direction) + ")");
		 * model.ball.direction = 0; } commands.add("(dash 100)");
		 * model.ball.distance-= 0.4; } }
		 */
	}

	/**
	 * dribbles with the ball
	 */
	private void dribbleAction() {

		int leftDegree = -30;
		int rightDegree = 30;
		int backDegree = -180;
		int leftScore = 0;
		int middleScore = 0;
		int rightScore = 0;
		int backScore = 0;
		
		if (model.goalInVision) {
			if (Math.abs(model.goal.direction) < 15) {
				middleScore = 5;
			} else {
				if (model.goal.direction < 0) {
					leftScore = 5;
				} else {
					rightScore = 5;
				}
			}
		} else {
			if (model.flags.get("l t") != null) {
				if (model.field_side == 'l') {
					rightScore = 10;
				} else {
					leftScore = 10;
				}
				if(Math.abs(model.flags.get("l t").distance - model.ball.distance)<2){
					backScore = 10;
				}
			} else if (model.flags.get("l b") != null) {
				if (model.field_side == 'l') {
					leftScore = 10;
				} else {
					rightScore = 10;
				}
				if(Math.abs(model.flags.get("l b").distance - model.ball.distance)<2){
					backScore = 10;
				}
			} 
			if(model.flags.get("l " + model.field_side)!=null){
				backScore = 10;
			}
			leftDegree = -60;
			rightDegree = 60;
		}
		for (Player p : model.players) {
			if (!model.team.equals(p.team)) {
				if (Math.abs(p.direction) < 15) {
					middleScore -= (10 / (p.distance + 1));

				} else {
					if (p.direction < 0) {
						leftScore -= (10 / (p.distance + 1));
					} else {
						rightScore -= (10 / (p.distance + 1));
					}

				}
			}
		}
		if (middleScore > leftScore && middleScore > rightScore && middleScore > backScore) {
			// go forward

			commands.add("(kick 30 " + (-model.ball.direction / 8) + ")");

		} else {
			if (leftScore > rightScore && leftScore > backScore) {
				commands.add("(kick 20 " + (leftDegree) + ")");
				commands.add("(turn " + (leftDegree) + ")");
			} else {
				if(rightScore > backScore){
					commands.add("(kick 20 " + (rightDegree) + ")");
					commands.add("(turn " + (rightDegree) + ")");
				}else{
					commands.add("(kick 20 " + (backDegree) + ")");
					commands.add("(turn " + (backDegree) + ")");
				}
				
			}
		}
	}

	/**
	 * not very intelligent
	 */
	private void kickAction() {
		FieldObject top = model.flags.get("f " + model.opp_field_side + " t");
		FieldObject bot = model.flags.get("f " + model.opp_field_side + " b");
		double topDegree = 0;
		double botDegree = 0;
		if (top != null && bot != null) {
			topDegree = top.direction;
			botDegree = bot.direction;


		} else {
			double degree = Math.toDegrees(Math.asin(9 / model.ball.distance));
			topDegree = model.goal.direction - degree;
			botDegree = model.goal.direction + degree;
		}
		int topCount = 0;
		int bottomCount = 0;
		for (Player pl : model.players) {
			if (pl.team != null && !model.team.equals(pl.team)
					&& pl.direction > topDegree && pl.direction < botDegree) {
				if (pl.direction > model.goal.direction) {
					bottomCount++;
				} else {
					topCount++;
				}
			}
		}
		if (bottomCount > topCount) { // shoot up
			commands.add("(kick 100 "
					+ (model.goal.direction - (r.nextInt(10))) + ")");
		} else { // shoot down
			commands.add("(kick 100 "
					+ (model.goal.direction + (r.nextInt(10))) + ")");
		}

	}

	/**
	 * not intelligent
	 */
	private void passAction() {

		boolean far = model.flags.containsKey("l " + model.opp_field_side);
		boolean left = false;
		boolean right = false;
		if (!far) {
			left = model.flags.containsKey("l "
					+ (model.field_side == 'l' ? 'b' : 't'));
			if (!left) {
				right = model.flags.containsKey("l "
						+ (model.field_side == 'l' ? 't' : 'b'));
			}
		}

		int[] degrees = new int[11];
		double[] distance = new double[11];
		int count = 0;
		for (Player pl : model.players) {
			if (model.team.equals(pl.team) && pl.distance < 50) {
				boolean goodToPass = true;
				for (Player pl1 : model.players) {
					if (!model.team.equals(pl1.team)) {
						if (pl1.distance < pl.distance
								&& Math.abs(pl1.direction - pl.direction) < 2) {
							goodToPass = false;
							break;
						}
					}
				}
				if (goodToPass) {
					degrees[count] = pl.direction;
					distance[count] = pl.distance;
					count++;
				}
				
			}
		}
		int dir = 0;
		double dist = 0;
		if (count > 0) {
			dir = degrees[0];
			dist = distance[0];
			for (int i = 1; i < count; i++) {
				if (far) {
					if (distance[i] > dist) {
						dist = distance[i];
						dir = degrees[i];
					}
				} else if (left) {
					if (dir > degrees[i]) {
						dir = degrees[i];
						dist = distance[i];
					}
				} else {
					if (dir < degrees[i]) {
						dir = degrees[i];
						dist = distance[i];
					}
				}
			}
		}

		double power = (2.5 * dist);

		
		commands.add("(kick " + (power > 100.0 ? 100.0 : power) + " " + dir
				+ ")");

	}

	/**
     *
     */
	private void catchAction() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
     *
     */
	private void lookForBallAction() {
		if (model.ballInVision) {
			commands.add("(turn " + model.ball.direction + ")");
		} else {
			commands.add("(turn 144)");
		}
	}

	private boolean proximityHelp(double objDist, double objAngle,
			double toObjDist, double toObjDir) {
		return (((toObjDist * Math.pow(Math.abs(objAngle - toObjDir) / 90, 2.0)) + objDist) < toObjDist);

	}
}
