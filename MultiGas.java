

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

public class MultiGas {
	private double[][] patterns;
	private double[][][] centers;
	private final int M;
	private final int N;
	private int[] structure;
	private final double nInit = 0.6;
	private final double nFin = 0;
	private final double gaussianSize = 0.5;
	private int tMax;

	public MultiGas(int M, int N, int tMax, int[] structure) {
		checkInput(M, structure);
		this.structure = structure;
		this.M = M;
		this.N = N;
		this.tMax = tMax;
		findPatterns();
		initializeRandomCenters();
	}

	public MultiGas(int M, int N, String filename, int[] structure) {
		checkInput(M, structure);
		this.structure = structure;
		this.M = M;
		this.N = N;
		readPatterns(filename);
		initializeCenters();
	}

	private void checkInput(int M, int[] structure) {
		if (M != structure.length) {
			System.out.println("Wrong params: structure.length is not equal M");
			System.exit(-1);
		}
	}

	private void readPatterns(String inputFilename) {
		try {
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

	private void initializeRandomCenters() {
		centers = new double[M][][];
		for (int i = 0; i < M; i++) {
			centers[i] = new double[structure[i]][N];
			// Use these indices to initialize centers
			for (int j = 0; j < structure[i]; j++) {
				centers[i][j] = drawRandomPoint(0,1);
			}
		}
	}

	// Initialize centers as a random subset of the patterns
	private void initializeCenters() {
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

//	rate of learning that is changing with time from initial value to the final value
	private double rate(int t) {
		return nInit * Math.pow((nFin / nInit), (t / tMax));
	}

//	neighborhood function with fixed size
	private double gaussian(int distance) {
		return Math.exp(-Math.pow(distance, 2)
				/ (2 * Math.pow(gaussianSize, 2)));
	}

//	counting delta for changing the center
	private double[] deltaCenter(int t, int sortedIndex, double[] center) {
		double[] result = new double[N];
		for(int i=0; i< N; i++) {
			result[i] = rate(t) * gaussian(sortedIndex) * (patterns[t][i] - center[i]);
		}
		return result;
	}

	private double euclidianMetric(double[] center, double[] stimulus) {
		double sum = 0;
		for (int i = 0; i < N; i++) {
			sum += Math.pow(stimulus[i] - center[i], 2);
		}
		return Math.sqrt(sum);
	}

	private void learnByStimulus(int t) {
		int gasIndex = 0;
		int currentWinnerGasIndex = 0;
		int centerIndex;
		double minDistance = euclidianMetric(centers[gasIndex][0], patterns[t]);
		double currentDistance;
		// find distances by euclidianMetrics
//		and remember the gas with minimal distance
		List<List<DataPair>> responces = new ArrayList<List<DataPair>>(M);
		for(gasIndex = 0; gasIndex<M; gasIndex++) {
			centerIndex = 0;
			responces.add(new ArrayList<DataPair>(structure[gasIndex]));
			for(double[] center : centers[gasIndex]) {
				currentDistance = euclidianMetric(center, patterns[t]);
				responces.get(gasIndex).add(new DataPair(centerIndex, currentDistance));
				centerIndex++;
				if(currentDistance < minDistance) {
					minDistance = currentDistance;
					currentWinnerGasIndex = gasIndex;
				}
			}
		}
//		sort winner gas by distances
		Collections.sort(responces.get(currentWinnerGasIndex), new CustomDataPairComparator());

//		find deltas for changing the winner gas centers
		List<DataPair> sortedWinner = responces.get(currentWinnerGasIndex);
		for(int i=0; i<structure[currentWinnerGasIndex]; i++) {
			int currentCenter = sortedWinner.get(i).index;
			double[] delta = deltaCenter(t, i, centers[currentWinnerGasIndex][currentCenter]);
			for(int j=0; j<N; j++)
				centers[currentWinnerGasIndex][currentCenter][j] += delta[j];
		}
	}

	private void writeCentersToFile(String outFilename) {
		try {
			PrintWriter fout = new PrintWriter(new BufferedWriter(
					new FileWriter(outFilename)));
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
		int K = 17;
		// number of partners
		int M = 4;
		int[] structure = { 5, 10, 3, 7 }; // the length of this array should
											// be always equal M
											// and sum of elements is supposed to be equal K
		// input dimension
		int N = 2;
		String patternsFilename = "train_PA-E.dat";
		int tMax = 10000000;

//		uncomment for random generated patterns
		MultiGas gas = new MultiGas(M, N, tMax, structure);
//		uncomment for reading patterns from file
//		MultiGas gas = new MultiGas(M, N, patternsFilename, structure);
		gas.printParams();

		// For testing
		// gas.writePatternsToFile();

		for(int t=0; t<gas.tMax; t++)
			gas.learnByStimulus(t);

		gas.writeCentersToFile("PA-E.net");
	}

	public class DataPair {
		public int index;
		public Double distance;

		public DataPair(int index, double distance) {
			this.index = index;
			this.distance = distance;
		}
	}

	public class CustomDataPairComparator implements Comparator<DataPair> {
		@Override
		public int compare(DataPair object1, DataPair object2) {
			return object1.distance.compareTo(object2.distance);
		}
	}
}
