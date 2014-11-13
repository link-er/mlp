import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RBF {
	private int N; //inputs
	private int K; //RBF
	private int M; //outputs
	private double[][] weights;
	private double[][] centers;
	private double[] sizes;
	private double learningRate;
	private double[][] teacher;
	private List<Double> errors = new ArrayList<>();
	private double[][] inputData;
	private double[] rbfOutput;


	public RBF(int[] configuration, int seed, double learningRate, String inputFilename) {
		N = configuration[0];
		K = configuration[1];
		M = configuration[2];
		weights = new double[M][K];
		this.learningRate = learningRate;
		rbfOutput = new double[K];

		initializeRandomWeights(seed);
		initializeRBFParams(inputFilename);
	}

	//Given an input returns the output of specific RBF neuron
	private double gaussian(double[] input, int neuron) {
		double sum = 0;
		double[] center = centers[neuron];
		double size = sizes[neuron];
		for(int i=0; i<input.length; i++) {
			sum += Math.pow(input[i]-center[i], 2);
		}
		return Math.exp(-Math.pow(Math.sqrt(sum), 2)/2*Math.pow(size, 2));
	}

	//Initialize weights -0.5..0.5
	private void initializeRandomWeights(int seed) {
		Random rand = new Random(seed);
		weights = new double[M][K];
		for (int i=0; i<M; i++){
			for (int j=0; j<K; j++){
				//between -0.5 and 0.5
				weights[i][j] = rand.nextDouble() - 0.5;
			}
		}
	}

	//Given the input save output of the RBF layer to rbfOutput
	private void getRBFOutput(double[] input) {
		for(int i=0; i<N; i++){
			rbfOutput[i] = gaussian(input, i);
		}
	}

	//TODO Use clustering
	private void initializeRBFParams(String inputFilename) {
		centers = new double[K][N];
		sizes = new double[K];
		//TODO clustering algorithm
		for (double[] row: centers){
			Arrays.fill(row, 2.0);
		}
    	Arrays.fill(sizes, 1.0);
	}

	//Saves inputs to inputData
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
   				if (line.startsWith("#") || line.equals("")){
   					continue;
   				} else {
   					String[] allValues = line.substring(1).replaceAll("\\s+", " ").split(" ");
   					//System.out.println(allValues[3]);
   					double[] row = new double[N];
   					//i=1 because each line starts with a space...
   					//TO FIX 
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

	//Saves teacher outputs to teacher
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
   				if (line.startsWith("#") || line.equals("")){
   					continue;
   				} else {
   					String[] allValues = line.substring(1).replaceAll("\\s+", " ").split(" ");
   					double[] row = new double[M];
   					for(int i=N; i<M + N; i++){
   						row[i - N] = Double.parseDouble(allValues[i]);
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

	//Returns the error
	private double error(double[] output, double[] teacher) {
		double sum = 0.0;
		for(int i=0; i<M; i++)
			sum += Math.pow(output[i] - teacher[i], 2);
		return 0.5*sum;
	}

	//Returns an output of the whole RBF network given an input array
	private double[] getOutput(double[] input) {
		getRBFOutput(input);
		double[] output = new double[M];
		double sum;
		for (int i=0; i<M; i++){
			sum = 0;
			for(int j=0; j<K; j++){
				sum += weights[i][j]*rbfOutput[j];
			}
			output[i] = sum;
		}
		return output;
	}

	//Employs the Widrow-Hoff learing rule
	private double singleStepLearning(double[] input, int patternNumber) {
		double[] output = getOutput(input); //outputs of RBF layer are already in rbfOutput
		double singleError = error(output, teacher[patternNumber]);
		for(int i=0; i<M; i++){
			for(int j=0; j<K; j++){
				weights[i][j] += learningRate*(teacher[patternNumber][i] - output[i])*rbfOutput[j];			}
		}
		return singleError;
	}

	//Print the configuration of the network
	public void printParams(){
		System.out.println("----RBF-layer----");
		for (int i=0; i<K; i++){
			System.out.println("Center and size of the RBF-neuron " + String.valueOf(i+1) + ":");
			System.out.println(Arrays.toString(centers[i]));
			System.out.println(sizes[i]);
		}
		System.out.println("");
		System.out.println("----Output-layer----");
		for (int i=0; i<M; i++){
			System.out.println("Weights of the neuron " + String.valueOf(i+1) + ":");
			for (int j=0; j<K; j++){
				System.out.println(weights[i][j]);
			}				
		}
	}

	//Write error to the file
	public void printOutErrorCurve(String errorFilename){
		PrintWriter fout;
		try{
			fout = new PrintWriter(errorFilename);
			for(int i=0; i<errors.size(); i++){
				fout.printf("%d %f ", i, errors.get(i));
				fout.printf("\n");
			}
			fout.close();
		}
		catch (FileNotFoundException e){
			System.out.println( e.getMessage() );
		}
	}

	//Test for correct reading from file
	public void test(){
		for(int i=0; i<teacher.length; i++){
			System.out.println(Arrays.toString(teacher[i]));
		}
		
	}

	public static void main(String[] args){

		/******************************
		Set parameters here
		******************************/
		//Number of neurons in each layer
		int[] configuration = {1, 4, 1};

		//rates of learning for every hidden performing calculations layer
		double learningRate = 0.1;
		
		//seed for initializing random
		int seed = 42;

		String inputFilename = "trainRBF.pat-2";
		String errorFilename = "learning.curve";

		/******************************
		Set parameters here
		******************************/

		RBF rbf = new RBF(configuration, seed, learningRate, inputFilename);

		rbf.printParams();

		rbf.getInputsFromFile(inputFilename);
		rbf.readTeacherOutput(inputFilename);
		
		//Learn!
		int indexPattern = 0;
		for(double[] input : rbf.inputData) {
			rbf.errors.add(rbf.singleStepLearning(input, indexPattern));
			indexPattern ++;
		}

		//put errors to file for gnuplot
		rbf.printOutErrorCurve(errorFilename);
	}
}
