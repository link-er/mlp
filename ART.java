

import java.util.Arrays;
import java.util.Random;

public class ART {

	private final int N;
	private final int M;
	private double[][] forwardWeights; //
	private int[][] backwardWeights; //
	private int[] g;

	public ART(int N, int M) {
		this.N = N;
		this.M = M;
		forwardWeights = new double[M][N];
		backwardWeights = new int[N][M];
		g = new int[N];
		initializeRandomWeights();
	}

	private void initializeRandomWeights() {
		// Initialize forward matrix at random
		Random rand = new Random();
		for (int i = 0; i < M; i++) {
			for (int j = 0; j < N; j++) {
				forwardWeights[i][j] = (rand.nextDouble() - 0.5) * 2; // from -1
																		// to 1
			}
		}
		// Initialize backward matrix
		// TODO
		for (int i = 0; i < N; i++) {
			Arrays.fill(backwardWeights[i], 0);
		}
		Arrays.fill(g, 1);
	}

	private boolean isOnlyOneSet(int[] array) {
		int counter = 0;
		int sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
			if (array[i] == 0) {
				counter++;
			}
		}

		if (counter == array.length - 1 && sum == 1) {
			return true;
		}
		return false;
	}

	private int[] getComparisonLayerOutput(int[] input, int[] net) {
		int[] comparisonLayerOutput = new int[N];
		for (int i = 0; i < N; i++) {
			if (input[i] * net[i] == 1 || input[i] * g[i] == 1 || g[i] * net[i] == 1) {
				comparisonLayerOutput[i] = 1;
			} else {
				comparisonLayerOutput[i] = 0;
			}

		}
		return comparisonLayerOutput;
	}

	private double[] getRecognitionLayerOutput(int[] comparisonLayerOutput) {
		// 1. Calculate weighted sum for all neurons - to array
		double[] weightedSums = new double[M];
		for(int i=0; i<M; i++) {
			weightedSums[i] = 0;
			for(int j=0; j<N; j++)
				weightedSums[i] += forwardWeights[i][j] * comparisonLayerOutput[j];
		}
		return weightedSums;
	}

	private int[] winnerTakesAll(double[] weightedSums) {
		// 2. Determine the max value
		double max = weightedSums[0];
		int maxIndex = 0;
		for(int i=0; i<M; i++) {
			if(weightedSums[i] > max) {
				max = weightedSums[i];
				maxIndex = i;
			}
		}
		// 3. return the array with only one '1'
		int[] result = new int[M];
		for(int i=0; i<M; i++) {
			if(i==maxIndex)
				result[i]=1;
			else
				result[i]=0;
		}
		return result;
	}

	private int[] getComparisonInputFromRecognition(int[] recognitionLayerOutput) {
		int[] weightedSums = new int[N];
		for(int i=0; i<N; i++) {
			weightedSums[i] = 0;
			for(int j=0; j<M; j++)
				weightedSums[i] += backwardWeights[i][j] * recognitionLayerOutput[j];
		}
		return weightedSums;
	}

	public int[] run(int[] input) {
		double[] weightedSums = new double[M];
		int[] output = new int[M];
		int[] comparisonLayerOutput = new int[N];
		int[] net = new int[N];
		comparisonLayerOutput = Arrays.copyOf(input, N);

		boolean isStable = false;
		int[] prevComparisonOutput = new int[N];
		Arrays.fill(prevComparisonOutput, 0);
		int[] prevOutput = new int[M];
		Arrays.fill(prevOutput, 0);
		while (!isStable) {
//			move input through W to recognition layer
			weightedSums = getRecognitionLayerOutput(comparisonLayerOutput);
			hebbianLearn(input, weightedSums);
//			apply WTA strategy to get array with one 1
			output = winnerTakesAll(weightedSums);
			learnBackward(input, output);
//			move WTA result up by V
			net = getComparisonInputFromRecognition(output);
//			count result by 2/3 strategy
			comparisonLayerOutput = getComparisonLayerOutput(input, net);

			if (Arrays.equals(prevComparisonOutput, comparisonLayerOutput) &&
					Arrays.equals(prevOutput, output)) {
				isStable = true;
			}
			else {
				prevComparisonOutput = Arrays.copyOf(comparisonLayerOutput, N);
				prevOutput = Arrays.copyOf(output, M);
			}
		}

		return output;
	}

//	TODO
	private void hebbianLearn(int[] input, double[] sums) {
//		update W by hebbian rule
	}

//	TODO
	private void learnBackward(int[] input, int[] output) {
//		update V by asserting so that input = output*V
	}

	public int[] recover(int[] recognitionVector) {
		if (isOnlyOneSet(recognitionVector)) {
			int[] weightedSums = new int[N];
			for(int i=0; i<N; i++) {
				weightedSums[i] = 0;
				for(int j=0; j<M; j++)
					weightedSums[i] += backwardWeights[i][j] * recognitionVector[j];
			}
			return weightedSums;
		} else {
			System.out.println("Wrong input.");
			return null;
		}
	}

	public void printWeights() {
		System.out.println("Forward:");
		for (int i = 0; i < M; i++) {
			System.out.println("");
			System.out.println("Output neuron " + i);
			for (int j = 0; j < N; j++) {
				System.out.println(forwardWeights[i][j]);
			}
		}
		System.out.println("Backward");
		for (int i = 0; i < N; i++) {
			System.out.println("");
			System.out.println("Input neuron " + i);
			for (int j = 0; j < M; j++) {
				System.out.println(backwardWeights[i][j]);
			}
		}
	}

	public static void main(String[] args) {
		ART art = new ART(20, 5);
		art.printWeights();

		Random rand = new Random();
		int[][] patterns = new int[100][art.N];
		for(int i=0; i<100; i++) {
			for(int j=0; j<art.N; j++) {
				if(rand.nextBoolean())
					patterns[i][j] = 1;
				else
					patterns[i][j] = 0;
			}
			art.run(patterns[i]);
		}

		art.printWeights();

		int[] result = new int[art.N];
		int[][] checkUps = new int[100][art.M];
		int indexNotZero;
		for(int i=0; i<100; i++) {
			indexNotZero = rand.nextInt(art.M);
			for(int j=0; j<art.M; j++) {
				if(j == indexNotZero)
					checkUps[i][j] = 1;
				else
					checkUps[i][j] = 0;
			}
			System.out.println("Check up is: " + Arrays.toString(checkUps[i]));
			result = art.recover(checkUps[i]);
			System.out.println("Result is: " + Arrays.toString(result));
		}
	}

}
