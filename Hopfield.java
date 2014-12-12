
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Hopfield {
	private int[][] weights;
	private int[] state;
	private int[] thresholds; // Do we need doubles?
	private int K;
	private boolean areThresholdsSet = false;
	public static int[][] patterns;

	// Constructor with setting all thresholds
	public Hopfield(int K, int threshold) {
		this.K = K;
		weights = new int[K][K];
		for(int i=0; i<K; i++)
			Arrays.fill(weights[i], 0);
		state = new int[K];
		thresholds = new int[K];
		setThresholds(threshold);
	}

	// Just create the network, set thresholds later
	public Hopfield(int K) {
		this.K = K;
		weights = new int[K][K];
		for(int i=0; i<K; i++)
			Arrays.fill(weights[i], 0);
		state = new int[K];
		thresholds = new int[K];
	}

//	for random taking neuron
	private int[] shuffleNeuronIndecis() {
		int[] result = new int[K];
		for(int i=0; i<K; i++)
			result[i] = i;
		Collections.shuffle(Arrays.asList(result));
		return result;
	}

	public void recall(int[] pattern) {
		state = pattern;
		int[] previousState = new int[K];
//		on first step previous state is not equal to state
		Arrays.fill(previousState, -2);
		boolean converged = false;
		int neuronIndex = 0;
		int sum=0;
		int[] sequenseOfFiring = shuffleNeuronIndecis();
		while (!converged) {
			// 1. Choose the index at random
			neuronIndex = sequenseOfFiring[neuronIndex];

			// 2. Calculate weighted sum for this neuron
			sum = 0;
			for(int i=0; i<K; i++)
				sum += weights[i][neuronIndex]*pattern[i];

			// 3. Compare to threshold: change the state
			state[neuronIndex] = (sum > thresholds[neuronIndex]) ? 1 : -1;

			// 4. Print the state
			printState();

			// 5. Check if we are ready
			if (Arrays.equals(state, previousState)) {
				converged = true;
			}
			System.arraycopy(state, 0, previousState, 0, K);

			if(neuronIndex==K-1) {
				sequenseOfFiring = shuffleNeuronIndecis();
				neuronIndex = 0;
			}
			else
				neuronIndex ++;
		}

		System.out.println("Result (new converged state): "
				+ Arrays.toString(state));

	}

	public void learnPattern(int[] pattern) {
//		set thresholds to the first pattern
		if (!areThresholdsSet) {
			thresholds = pattern;
		}

//		learn the pattern
		for(int i=0; i<K; i++)
			for(int j=0; j<K; j++) {
				if(i!=j)
					weights[i][j] += pattern[i]*pattern[j];
			}
	}

	private void setThresholds(int thresholdValue) {
		Arrays.fill(thresholds, thresholdValue);
		areThresholdsSet = true;
	}

	private void printState() {
		if (K < 101) {
//			# stands for 1, _ stands for 0
			for (int i = 0; i < state.length; i++) {
				if (state[i] == 1) {
					System.out.print("#");
				} else {
					System.out.print("_");
				}
			}
		} else {
			System.out.println(getEnergy());
		}
		System.out.println();
	}

	private double getEnergy() {
		int sum1 = 0, sum2 = 0;
		for(int i=0; i<K; i++) {
			sum2 += thresholds[i]*state[i];
			for(int j=0; j<K; j++)
				sum1 += weights[i][j]*state[i]*state[j];
		}
		return -0.5*sum1 + sum2;
	}

	public static void main(String[] args) {
		/******************************
		 * Set parameters here
		 ******************************/
		// Number of neurons
		int K = 150;
//		value for threshold
		int thresholdValue = 0;
//		number of patterns
		int patternsCount = 10;
//		patterns
		Random rand = new Random();
		patterns = new int[patternsCount][K];
		for(int i=0; i<patternsCount; i++)
			for(int j=0; j<K; j++) {
				if(rand.nextBoolean())
					patterns[i][j] = 1;
				else
					patterns[i][j] = -1;
			}

		System.out.println("Patterns:");
		for(int[] pattern : patterns)
			System.out.println(Arrays.toString(pattern));
		System.out.println();

		/******************************
		 * Set parameters here
		 ******************************/
//		uncomment to get user defined values for threshold or 0
		Hopfield net = new Hopfield(K, thresholdValue);
//		uncomment to get threshold from the starting pattern
//		Hopfield net = new Hopfield(K);

//		learn to patterns
		for(int[] pattern : patterns)
			net.learnPattern(pattern);

//		recalling
//		change 3d part of the first pattern to check convergency
		int[] input = new int[K];
		System.arraycopy(patterns[0], 0, input, 0, K);
		for(int i=0; i<K/3; i++)
			input[i] *= -1;
		net.recall(input);

		System.out.println(Arrays.equals(patterns[0], net.state));
	}
}
