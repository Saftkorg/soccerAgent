package soccer;

public class Learning {
	/**
	 * evaluate an episode
	 * 
	 * @return points
	 */

	public static int EPISODE = 600; // an episode of one minute. Allowing for
										// 10 episodes (a full PGRL iteration)
										// over a game

	public int evaluate() {
		// if goal + X points
		// if opponent goal - X points
		// reward points for having the ball
		// punish for loosing the ball
		//
		return 0;
	}
}
