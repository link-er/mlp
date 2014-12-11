import java.util.Arrays;
import java.util.Random;

public class Hopfield {

	private int[][] weights;
	private int[] state;
	private int[] thresholds; // Do we need doubles?
	private int K;
	private boolean areThresholdsSet = false;

	// Constructor with setting all thresholds
	public Hopfield(int K, int threshold) {
		this.K = K;
		weights = new int[K][K];
		Arrays.fill(weights, 0);
		state = new int[K];
		thresholds = new int[K];
		setThresholds(threshold);
	}

	// Just create the network, set thresholds later
	public Hopfield(int K) {
		this.K = K;
		weights = new int[K][K];
		Arrays.fill(weights, 0);
		state = new int[K];
		thresholds = new int[K];
	}

	public void recall(int[] pattern) {
		System.arraycopy(pattern, 0, state, 0, K);
		int[] previousState = new int[K];
		boolean converged = false;
		int neuronIndex;
		Random rand = new Random();
		while (!converged) {
			// 0. Save the state before changing
			System.arraycopy(state, 0, previousState, 0, K);

			// 1. Choose the index at random
			neuronIndex = rand.nextInt(K);
			// 2. Calculate weighted sum for this neuron

			// 3. Compare to threshold: change the state

			// 4. Print the state
			printState();

			// 5. Check if we are ready
			if (Arrays.equals(state, previousState)) {
				converged = true;
			}
		}

		System.out.println("Result (new converged state): "
				+ Arrays.toString(state));

	}

	public void learnPattern(int[] pattern) {
		if (!areThresholdsSet) {
			thresholds = pattern;
		}
		// Do something with weight matrix
	}

	private void setThresholds(int thresholdValue) {
		Arrays.fill(thresholds, thresholdValue);
		areThresholdsSet = true;
	}

	private void printState() {
		if (K < 101) {
			for (int i = 0; i < state.length; i++) {
				if (state[i] == 1) {
					System.out.print("#");
				} else {
					System.out.print(".");
				}
			}
		} else {
			System.out.println(getEnergy());
		}
		System.out.println();
	}

	private double getEnergy() {

	}

	private public static void main(String[] args) {

		/******************************
		 * Set parameters here
		 ******************************/
		// Number of neurons
		int K;

		/******************************
		 * Set parameters here
		 ******************************/

	}

}
