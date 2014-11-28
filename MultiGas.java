package neuronet_5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	private int M;
	private int N;
	private int K;
	private double nInit = 0.6;
	private double nFin = 0;
	private int tMax;
	private double gaussianSize = 0.5;
	
	public MultiGas(int K, int M, int N, int[] structure) {
		this.K = K;
		this.M = M;
		this.N = N;
		findPatterns();
		initializeCenters();
	}
	
	public MultiGas(int K, int M, int N, String filename, int[] structure) {
		this.K = K;
		this.M = M;
		this.N = N;
		readPatterns(filename);
		initializeCenters();
	}
	
	private void readPatterns(String inputFilename) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFilename));
			 List<String> lines = Files.readAllLines(Paths.get(inputFilename),
	                    Charset.defaultCharset());
				patterns = new double[lines.size()][N];
				tMax = lines.size();
				int j = 0;
	            for (String line : lines) {
	            	String[] allValues = line.split(" ");
   					for(int i=0; i<N; i++){
   						patterns[j][i]= Double.parseDouble(allValues[i]);
   					}
   					j++;
	            }
		}
		catch(IOException e){
			System.out.println( e.getMessage() );
		}
	}
	
	private void initializeCenters() {
//		TODO rewrite
//		centers = new double[M][][N];
//		sizes = new double[K];
//
//		//Contains unique indices for centers (chosen from the inputData)
//		List<Integer> indices = new ArrayList<Integer>();
//		//Generate K unique indices
//		Random rand = new Random();
//		while(indices.size()<K){
//			int randomIndex = rand.nextInt(inputData.length);
//			if(indices.contains(randomIndex)){
//				continue;
//			}
//			indices.add(randomIndex);
//		}
//		
//		//Use these indices to initialize centers
//		for(int i=0; i<K; i++){
//			centers[i] = inputData[indices.get(i)];
//		}
//
//    	Arrays.fill(sizes, 0.5); //Use your intuition
	}
	
	private void findPatterns() {
//		TODO fill patterns and tMax = 1000 from non overlapping circles
	}
	
	private double rate(int t) {
		return nInit*Math.pow((nFin/nInit), (t/tMax));
	}
	
	private double gaussian(int distance) {
		return Math.exp(-Math.pow(distance, 2)/(2*Math.pow(gaussianSize, 2)));
	}
	
	private void sortCenters(int winnerGas, double[] response) {
//		TODO for winner Gas from first index reorder by second index
	}
	
	private void changeWinnerCenters(int winnerGas, int t) {
//		TODO adjust centers
	}
	
	private double euclidianMetric(double[] center, double[] stimulus) {
		double sum = 0;
		for(int i=0; i<N; i++) {
			sum += Math.pow(stimulus[i]-center[i], 2);
		}
		return Math.sqrt(sum);
	}
	
	private Winner applyStimulus(int t) {
//		TODO find nearest center by euclidianMetrics and return index of Gas and center; call SortCenters
		return null;
	}
	
	private void printOutCenters(String outFilename) {
//		TODO print centers by lines
	}
	
	public static void main(String[] args) {
//		number of neurons
		int K = 10;
//		number of partners
		int M = 4;
		int[] structure = {2,2,3,3};
//		input dimension
		int N = 3;
		
		MultiGas gas = new MultiGas(K, M, N, structure);
		
		Winner currentWinner;
		for(int t=0; t<gas.tMax; t++) {
			currentWinner = gas.applyStimulus(t);
			gas.changeWinnerCenters(currentWinner.gasIndex, t);
		}
		
		gas.printOutCenters("PA-E.net");
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
