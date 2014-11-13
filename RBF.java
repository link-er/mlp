import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class RBF {
	private int N; //inputs
	private int K; //RBF
	private int M; //outputs
	private double[][] weights;
	private double[][] centers;
	private double[] sizes;
	private static double rate;
	private double[][] teacher;
	private List<Double> errors;
	private double[][] inputData;
	private double[] rbfOutput;

	private double gaussian(double[] input, int neuron) {
		double sum = 0;
		double[] center = centers[neuron];
		double size = sizes[neuron];
		for(int i=0; i<input.length; i++) {
			sum += Math.pow(input[i]-center[i], 2);
		}
		return Math.exp(-Math.pow(Math.sqrt(sum), 2)/2*Math.pow(size, 2));
	}

	private void initializeRandomWeights(int seed) {
		Random rand = new Random(seed);
		weights = new double[M][K];
		for (int i=0; i<M; i++){
			for (int j=0; j<K; j++){
//				between -0.5 and 0.5
				weights[i][j] = rand.nextDouble() - 0.5;
			}
		}
	}

	private void getRBFOutput(double[] input) {
//		TODO set rbfOutput
	}

	private void initializeRBFParams() {
//		TODO choose the way for initilizing senters and sizes
	}

	public RBF(int[] configuration, int seed) {
//		TODO set net
//		call initializeRandomWeights(seed);
//		call initializeRBFParams();
	}

	public void printParams(){
//		TODO print Ck, sk, wkm
	}

	private void getInputsFromFile(String inputFilename){
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFilename));
			String line;
			line = br.readLine();
			line = br.readLine();
			String stringWithNumberOfPatterns = line.replaceAll("\\s+", " ").split(" ")[1];
			int numberOfPatterns = Integer.parseInt(stringWithNumberOfPatterns.substring(2));
			inputData = new double[numberOfPatterns][M];
			int j=0;
			while ((line = br.readLine()) != null) {
   				if (line.startsWith("#")){
   					continue;
   				} else {
   					String[] allValues = line.split(" ");
   					double[] row = new double[N];
   					for(int i=0; i<N; i++){
   						row[i]= Double.parseDouble(allValues[i]);
   					}
   					inputData[j] = row;
   					j++;
   				}

			}
			br.close();
		}
		catch(IOException e){
			System.out.println( e.getMessage() );
		}
	}

	private void readTeacherOutput(String inputFilename) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFilename));
			String line;
			line = br.readLine();
			line = br.readLine();
			String stringWithNumberOfPatterns = line.split(" ")[1];
			int numberOfPatterns = Integer.parseInt(stringWithNumberOfPatterns.substring(2));
			teacher = new double[numberOfPatterns][M];
			int j=0;
			while ((line = br.readLine()) != null) {
   				if (line.startsWith("#")){
   					continue;
   				} else {
   					String[] allValues = line.replaceAll("\\s+", " ").split(" ");
   					double[] row = new double[M];
   					for(int i=M; i<M + N; i++){
   						row[i - M] = Double.parseDouble(allValues[i]);
   					}
   					teacher[j] = row;
   					j++;
   				}

			}
			br.close();
		}
		catch(IOException e){
			System.out.println( e.getMessage() );
		}
	}

	private double error(double[] output, double[] teacher) {
		double sum = 0.0;
		for(int i=0; i<M; i++)
			sum += Math.pow(output[i] - teacher[i], 2);
		return 0.5*sum;
	}

	private void printOutErrorCurve(){
//		write down error point to file for passing to gnuplot
		PrintWriter fout;
		String filename = "learning.curve";
		try{
			fout = new PrintWriter(filename);
			for(int i=0; i<errors.size(); i++)
				fout.printf("%d %f ", i, errors.get(i));
			fout.close();
		}
		catch (FileNotFoundException e){
			System.out.println( e.getMessage() );
		}
	}

	private double[] run(double[] input) {
//		TODO get output for given input
		return input;
	}

	private double singleStepLearning(double[] input, int patternNumber) {
//		TODO
		return 0.0;
	}

	public static void main(String[] args){

		/******************************
		Set parameters here
		******************************/
		//Number of neurons in each layer
		int[] configuration = {4, 2, 2};

		//rates of learning for every hidden performing calculations layer
		double userRate = 0.1;
		rate = userRate;

//		seed for initializing random
		int seed = 42;

		String inputFilename = "trainRBF1.pat";

		/******************************
		Set parameters here
		******************************/

		RBF rbf = new RBF(configuration, seed);

		rbf.printParams();

		rbf.getInputsFromFile(inputFilename);
		rbf.readTeacherOutput(inputFilename);

		int indexPattern = 0;
		for(double[] input : rbf.inputData) {
			rbf.errors.add(rbf.singleStepLearning(input, indexPattern));
			indexPattern ++;
		}

//		put errors to file for gnuplot
		rbf.printOutErrorCurve();
	}
}
