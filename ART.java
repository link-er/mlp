import java.util.Arrays;
import java.util.Random;

public class ART {

	private final int N;
	private final int M;
	private double[][] forwardWeights; //
	private int[][] backwardWeights; //

	public ART(int N, int M) {
		this.N = N;
		this.M = M;
		forwardWeights = new double[M][N];
		backwardWeights = new int[N][M];
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
			if (input[i] * net[i] == 1) {
				comparisonLayerOutput[i] = 1;
			} else {
				comparisonLayerOutput[i] = 0;
			}

		}
		return comparisonLayerOutput;
	}

	// TODO
	private int[] getRecognitionLayerOutput(int[] comparisonLayerOutput) {
		// 1. Calculate weighted sum for all neurons - to array
		// 2. Determine the max value
		// 3. return the array with only one '1'
		return null;
	}

	// TODO
	public int[] run(int[] input) {
		int[] output = new int[M];
		int[] comparisonLayerOutput = new int[N];
		double[][] weightedSum = new double[M][N];
		Arrays.fill(output, 0);
		Arrays.fill(comparisonLayerOutput, 0);
		boolean isStable = false;
		while (!isStable) {
			comparisonLayerOutput = getComparisonLayerOutput(input, output);
			output = getRecognitionLayerOutput(comparisonLayerOutput);
			if (42 == 42) {
				isStable = true;
			}
		}

		return output;
	}

	// TODO
	public int[] recover(int[] recognitionVector) {
		if (isOnlyOneSet(recognitionVector)) {
			return null;
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

	}

}
