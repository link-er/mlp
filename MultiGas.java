import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MultiGas {
	private double[][] patterns;
	private double[][][] centers;
	private final int M;
	private final int N;
	// private int K;
	private final double nInit = 0.6;
	private final double nFin = 0;
	private final double gaussianSize = 0.5;
	private int tMax;

	public MultiGas(int K, int M, int N, int tMax, int[] structure) {
		checkInput(M, structure);
		// this.K = K;
		this.M = M;
		this.N = N;
		this.tMax = tMax;
		findPatterns();
		initializeCenters(structure);
	}

	public MultiGas(int K, int M, int N, String filename, int[] structure) {
		checkInput(M, structure);
		// this.K = K;
		this.M = M;
		this.N = N;
		readPatterns(filename);
		initializeCenters(structure);
	}

	private void checkInput(int M, int[] structure) {
		if (M != structure.length) {
			System.out.println("Wrong params: structure.length is not equal M");
			System.exit(-1);
		}
	}

	private void readPatterns(String inputFilename) {
		try {
			BufferedReader br = new BufferedReader(
					new FileReader(inputFilename));
			List<String> lines = Files.readAllLines(Paths.get(inputFilename),
					Charset.defaultCharset());
			patterns = new double[lines.size()][N];
			tMax = lines.size();
			int j = 0;
			for (String line : lines) {
				String[] allValues = line.split("  ");
				for (int i = 0; i < N; i++) {
					patterns[j][i] = Double.parseDouble(allValues[i]);
				}
				j++;
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	// Initialize centers as a random subset of the patterns
	private void initializeCenters(int[] structure) {
		System.out.println(patterns.length);
		centers = new double[M][][];
		for (int i = 0; i < M; i++) {
			// Contains unique indices for centers (chosen from the inputData)
			List<Integer> indices = new ArrayList<Integer>();
			// Generate K unique indices
			Random rand = new Random();
			while (indices.size() < structure[i]) {
				int randomIndex = rand.nextInt(patterns.length);
				if (indices.contains(randomIndex)) {
					continue;
				}
				indices.add(randomIndex);
			}
			centers[i] = new double[structure[i]][N];
			// Use these indices to initialize centers
			for (int j = 0; j < structure[i]; j++) {
				centers[i][j] = patterns[indices.get(j)];
			}
		}
	}

	// Patterns drawn randomly, equally distributed
	// from 3 non-overlapping areas within the unit cube
	private void findPatterns() {
		patterns = new double[tMax][N];
		// double[] randomPattern = new double[N];
		Random rand = new Random();
		for (int i = 0; i < tMax; i++) {
			// Choose one out of 3 areas at random
			int area = rand.nextInt(99) % 3;
			// Get a random point from this area
			patterns[i] = getRandomPoint(area);
		}
	}

	// Given a number of the area return a random point from this area
	private double[] getRandomPoint(int area) {
		// Split into 3 non-overlapping areas:
		// [0.0, 0.3), [0.35, 0.65), [0.7, 1.0) for each coordinate
		double[] randomPoint = new double[N];
		switch (area) {
		case 0:
			randomPoint = drawRandomPoint(0.0, 0.3);
			break;
		case 1:
			randomPoint = drawRandomPoint(0.35, 0.65);
			break;
		case 2:
			randomPoint = drawRandomPoint(0.7, 1.0);
			break;
		}
		return randomPoint;
	}

	private double[] drawRandomPoint(double minValue, double maxValue) {
		double[] randomPoint = new double[N];
		Random rand = new Random();
		for (int i = 0; i < N; i++) {
			double randCoordinate = minValue + (maxValue - minValue)
					* rand.nextDouble();
			randomPoint[i] = randCoordinate;
		}
		return randomPoint;
	}

	private double rate(int t) {
		return nInit * Math.pow((nFin / nInit), (t / tMax));
	}

	private double gaussian(int distance) {
		return Math.exp(-Math.pow(distance, 2)
				/ (2 * Math.pow(gaussianSize, 2)));
	}

	private void sortCenters(int winnerGas, double[] response) {
		// TODO for winner Gas from first index reorder by second index
	}

	private void changeWinnerCenters(int winnerGas, int t) {
		// TODO adjust centers
	}

	private double euclidianMetric(double[] center, double[] stimulus) {
		double sum = 0;
		for (int i = 0; i < N; i++) {
			sum += Math.pow(stimulus[i] - center[i], 2);
		}
		return Math.sqrt(sum);
	}

	private Winner applyStimulus(int t) {
		// TODO find nearest center by euclidianMetrics and return index of Gas
		// and center; call SortCenters
		return null;
	}

	private void writeCentersToFile(String outFilename) {
		try {
			PrintWriter fout = new PrintWriter(new BufferedWriter(
					new FileWriter(outFilename)));
			for (int i = 0; i < tMax; i++) {
				for (int j = 0; j < N; j++) {
					fout.printf("%f ", patterns[i][j]);
				}
				fout.printf("\n");
			}
			fout.close();
		} catch (IOException e) {
			// if any I/O error occurs
			e.printStackTrace();
		}
	}

	// Test random sampling
	public void writePatternsToFile() {
		try {
			PrintWriter fout = new PrintWriter(new BufferedWriter(
					new FileWriter("patterns.dat")));
			for (int i = 0; i < M; i++) {
				for (int j = 0; j < centers[i].length; j++) {
					for (int k = 0; k < N; k++) {
						fout.printf("%f ", centers[i][j][k]);
					}
					fout.printf("\n");
				}
			}
			fout.close();
		} catch (IOException e) {
			// if any I/O error occurs
			e.printStackTrace();
		}
	}

	public void printParams() {
		System.out.println("=============================");
		for (int i = 0; i < M; i++) {
			System.out.println("Neron Gas " + String.valueOf(i));
			for (int j = 0; j < centers[i].length; j++) {
				System.out.println(Arrays.toString(centers[i][j]));
			}
			System.out.println("");
		}
		System.out.println("=============================");
	}

	public static void main(String[] args) {
		// number of neurons
		int K = 10;
		// number of partners
		int M = 5;
		int[] structure = { 2, 2, 3, 3, 7 }; // the length of this array should
												// be always equal M
		// input dimension
		int N = 3;
		String patternsFilename = "train_PA-E.dat";
		int tMax = 1000;

		MultiGas gas = new MultiGas(K, M, N, tMax, structure);
		gas.printParams();
		gas.writeCentersToFile("PA-E.net");
		// For testing
		// gas.writePatternsToFile();
		Winner currentWinner;
		// for(int t=0; t<gas.tMax; t++) {
		// currentWinner = gas.applyStimulus(t);
		// gas.changeWinnerCenters(currentWinner.gasIndex, t);
		// }

	}

	public class Winner {
		public int gasIndex;
		public int centerIndex;

		public Winner(int gas, int center) {
			gasIndex = gas;
			centerIndex = center;
		}
	}
}