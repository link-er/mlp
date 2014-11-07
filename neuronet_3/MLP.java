package neuronet_3;

import java.util.*;
import java.io.*;

public class MLP{

	private int numberOfLayers;
	private double[][][] weights;
	private int[] transferFunctions;
	private List<Double> errorCurvePoints;
	private double[][] inputDataEpoch;
	private double[][] teacher;
	private double[][][] deltas;
	private double[][][] weightChanges;
	private double[][][] cumulativeWeightChanges;
	private double[][] outputs;
	private static double[] rates;
	private static double[] momentum;

	public MLP(int[] numberOfNeurons, int seed, int[] transferFunctions){
		this.numberOfLayers = numberOfNeurons.length;
		this.transferFunctions = transferFunctions;

		weights = new double[numberOfLayers-1][][];
		for(int i=0; i<numberOfLayers-1; i++){
			weights[i] = new double[numberOfNeurons[i+1]][numberOfNeurons[i]+1];
		}
		initializeRandomWeights(seed);
	}


	//The second constructor which initializes weights from file
	public MLP(int[] numberOfNeurons, String fileWithWeights, int[] transferFunctions){
		this.numberOfLayers = numberOfNeurons.length;
		this.transferFunctions = transferFunctions;

		weights = new double[numberOfLayers-1][][];
		for(int i=0; i<numberOfLayers-1; i++){
			weights[i] = new double[numberOfNeurons[i+1]][numberOfNeurons[i]+1];
		}
		initializeWeightsFromFile(fileWithWeights);

	}

	private void initializeWeightsFromFile(String filename){
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			for(int i=0; i<numberOfLayers-1; i++){
			//+ 1 for BIAS
				for(int j=0; j<weights[i].length; j++){
					for(int k=0; k<weights[i][j].length; k++){
						weights[i][j][k] = scanner.nextDouble();
					}
				}
			}
			scanner.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("File with weights not found!");
		}
	}

	//Initialize all weights using a seed for random generator
	private void initializeRandomWeights(int seed){
		Random rand = new Random(seed);
		//weights = new double[numberOfLayers-1][numberOfNeurons][numberOfNeurons+1];
		double rangeMin = -2.0;
		double rangeMax = 2.0;
		for (int i=0; i<numberOfLayers-1; i++){
			for (int j=0; j<weights[i].length; j++){
				for (int k=0; k<weights[i][j].length; k++){
					weights[i][j][k] = rangeMin + (rangeMax - rangeMin) * rand.nextDouble();
				}				
			}
		}
	}

	//Transfer function given an input and number of layer
	private double transfer(double net, int layerNumber){
		switch (transferFunctions[layerNumber]){
			case 0: return net;
			case 1: return 1/(1 + Math.exp(-net));
			case 2: return (Math.exp(net) - Math.exp(-net))/(Math.exp(net) + Math.exp(-net));
			default: return -42;
		}
	}

	//Given the input for the layer and the number of layer
	//returns the output of layer
	private double[] getLayerOutput(double[] inputs, int layerNumber){
		double[] outputs = new double[weights[layerNumber].length];
		//System.out.println(weights[layerNumber].length);
		//System.out.println(inputs.length);
		for(int i=0; i < outputs.length; i++){
			double net = 1*weights[layerNumber][i][0];
			//System.out.println(weights[layerNumber][i].length);
			for (int j = 0; j < weights[layerNumber][i].length-1; j++){
				//System.out.println(j);
				net += inputs[j] * weights[layerNumber][i][j+1];
			}
			outputs[i] = transfer(net, layerNumber);
		}
		return outputs;
	}

	//Write all outputs to file
	private void writeToFile(double[][] outputs, String outputFilename){
		PrintWriter fout;
		try{
			fout = new PrintWriter(outputFilename);

			for(int i=0; i<outputs.length; i++){
				for(int j=0; j<outputs[i].length; j++){
					fout.printf("%f  ",outputs[i][j]);
				}
				fout.printf("\n");
			}
			fout.close();
		}
		catch (FileNotFoundException e){
			System.out.println( e.getMessage() );

		} 
		
	}

	//Reads file and returns an array of input values for the MLP
	private void getInputsFromFile(String inputFilename){
		int numberOfInputs = weights[0][1].length - 1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFilename));
			String line;
			line = br.readLine();
			line = br.readLine();
			String stringWithNumberOfPatterns = line.split(" ")[1];
			int numberOfPatterns = Integer.parseInt(stringWithNumberOfPatterns.substring(2));
			inputDataEpoch = new double[numberOfPatterns][numberOfInputs];
			int j=0;
			while ((line = br.readLine()) != null) {
   				if (line.startsWith("#")){
   					continue;
   				} else {
   					String[] allValues = line.split(" ");
   					double[] row = new double[numberOfInputs];
   					for(int i=0; i<numberOfInputs; i++){
   						row[i]= Double.parseDouble(allValues[i]);
   					}
   					inputDataEpoch[j] = row;
   					j++;
   				}
   				
			}
			br.close();
		}
		catch(IOException e){
			System.out.println( e.getMessage() );
		}
	}

	//Print out the weights for each layer and each neuron
	public void printWeights(){
		for (int i=0; i<numberOfLayers-1; i++){
			System.out.println("");
			System.out.println("Weights of the layer" + String.valueOf(i+2) + ":");
			for (int j=0; j<weights[i].length; j++){
				System.out.println("Weights of the neuron" + String.valueOf(j+1) + ":");
				for (int k=0; k<weights[i][j].length; k++){
					System.out.println(weights[i][j][k]);
				}				
			}
		}
	}

	//Read input from inputFilename and 
	//saves the output of the MLP to the outputFilename
	public void run(String inputFilename, String outputFilename){
		//Read file line by line, parse, get inputs
		double[][] netOutputs = new double[inputDataEpoch.length][weights[numberOfLayers-2].length];
		//Run for all layers for every output
		double[] previousOutput;
		for(int i=0; i<inputDataEpoch.length; i++){
			previousOutput = inputDataEpoch[i];
			for(int j=0; j<numberOfLayers-1; j++){
				previousOutput = getLayerOutput(previousOutput, j);
			}
			netOutputs[i] = previousOutput;
		}
		// Write the whole output to the file
		writeToFile(netOutputs, outputFilename);
	}
	
	private void readTeacherOutput(String filename) {
//		read teacher output to teacher array
	}
	
//	shuffle input arrays
	private static void shuffleInputData() {
//		use inputDataEpoch
	}
	
//	count error
	private static double error(double[] output, double[] teacher) {
		return 0.0;
	}

	private void printOutErrorCurve(){
//		write down error point to file for passing to gnuplot
	}
	
	private double[] singleInputRun(double[] input) {
//		go by layers and apply transferfunctions to get output
//		save outputs for each neuron
	}
	
	private List<Double> batchLearning() {
		List<Double> errors = new ArrayList<>();
		
		int patternNumber = 0;
		for(double[] input : inputDataEpoch) {
			double[] result = singleInputRun(input);
			
			int layerIndex = 0, neuronIndex = 0, weightIndex = 0;
			for(double[][] layer : weights) {
				for(double[] neuron : layer) {
					for(double weight : neuron) {
						weightChanges[layerIndex][neuronIndex][weightIndex] = weightChange(layerIndex, neuronIndex, weightIndex);
						cumulativeWeightChanges[layerIndex][neuronIndex][weightIndex] += 
								weightChanges[layerIndex][neuronIndex][weightIndex];
						weightIndex ++;
					}
					neuronIndex ++;
				}
				layerIndex ++;
			}
			errors.add(error(result, teacher[patternNumber]));
			patternNumber ++;
		}
		applyCumulativeWeightChanges();
		
		return errors;
	}

	private double singleStepLearning(double[] input, int patternNumber) {
		double[] result = singleInputRun(input);
		
		int layerIndex = 0, neuronIndex = 0, weightIndex = 0;
		for(double[][] layer : weights) {
			for(double[] neuron : layer) {
				for(double weight : neuron) {
					weightChanges[layerIndex][neuronIndex][weightIndex] = weightChange(layerIndex, neuronIndex, weightIndex);
					weightIndex ++;
				}
				neuronIndex ++;
			}
			layerIndex ++;
		}
		applyWeightChanges();
		
		return error(result, teacher[patternNumber]);
	}
	
	private void applyWeightChanges() {
//		just sum up changes with weights
	}
	
	private void applyCumulativeWeightChanges() {
//		just sum up cumulated changes with weights
	}
	
	private double weightChange(int layer, int neuron, int weight) {
//		count delta - by the corresponding rule if the layer is output or hidden
//		put delta into deltas for further use
//		count weight change
	}
	
	public static void main(String[] args){
		
		/******************************
		Set parameters here
		******************************/
		//Number of neurons in each layer
		int[] configuration = {4, 2, 2};
		
		rates = {0.1, 0.1, 0.1};
		
		momentum = {0.2, 0.2, 0.2};
		//Seed for random generator
		int seed = 42;
		// Trnsfer functions for each layer:
		//0 - identity, 1 - tahn, 2 - logistic
		int[] transferFunctions = {1,2,2};
		//Should always be true:
		//configuration.length == transferFunctions.length - 1
		String inputFilename = "training.dat";
		String outputFilename = "output.dat";

		/******************************
		Set parameters here
		******************************/

//		TODO check if the arrays length are the same
//		TODO check if weights data has the same dimensions

		//MLP mlp = new MLP(configuration, seed, transferFunctions);
		MLP mlp = new MLP(configuration, "weights.dat", transferFunctions);
		mlp.printWeights();
		mlp.run(inputFilename, outputFilename);
		
		mlp.readTeacherOutput(inputFilename);
		
//		TODO initialize weight changes with zeroes - so momentum for the first step is 0
		
		Scanner in = new Scanner(System.in);
		System.out.println("Enter type of learning (batch or single)");
		if(in.nextLine().equals("batch")) {
			int epochsCount;
			while(true){
				System.out.println("Enter number of epochs");
				epochsCount = in.nextInt();
				if (epochsCount > 0){
					break;
				}
			}
			for(int i=0; i<epochsCount; i++){
				shuffleInputData();
				for(double error : mlp.batchLearning()) {
					mlp.errorCurvePoints.add(error);
				}
			}	
		}
		else {
			int indexPattern = 0;
			for(double[] input : mlp.inputDataEpoch) {
				mlp.errorCurvePoints.add(mlp.singleStepLearning(input, indexPattern));
				indexPattern ++;
			}
		}
		
//		put errors to file for gnuplot
		mlp.printOutErrorCurve();
	}
}
