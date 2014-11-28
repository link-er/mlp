package machine_learning4;

import java.util.*;
import java.io.*;

public class MLP{
	private static int[] transferFunctions;
	private static double[] rates;
	
	private int numberOfLayers;
	private int numberOfInputs;
	private int numberOfOutputs;
	private double[][] inputDataEpoch;
	private double[][][] weights;
	private double[][] deltas;
	private double[][][] weightChanges;
	private double[][] teacher;
	private double[][] outputsByNeurons;
	
	private void setInitialWeights(int[] numberOfNeurons) {
//		- 1 because we do not have weights for input layer
		weights = new double[numberOfLayers-1][][];
		for(int i=0; i<numberOfLayers-1; i++){
//			for each layer we get particular number of neurons with number of inputs + BIAS
			weights[i] = new double[numberOfNeurons[i+1]][numberOfNeurons[i]+1];
		}
		outputsByNeurons = new double[numberOfLayers-1][];
		for(int i=0; i<numberOfLayers-1; i++){
//			for each layer we get particular number of neurons with number of inputs + BIAS
			outputsByNeurons[i] = new double[numberOfNeurons[i+1]];
		}
		deltas = new double[numberOfLayers-1][];
		for(int i=0; i<numberOfLayers-1; i++){
//			for each layer we get particular number of neurons with number of inputs + BIAS
			deltas[i] = new double[numberOfNeurons[i+1]];
		}
//		initialize weight changes with zeroes - so momentum and cumulated weights change for the first step is 0
		weightChanges = new double[numberOfLayers-1][][];
		for(int i=0; i<numberOfLayers-1; i++){
			weightChanges[i] = new double[numberOfNeurons[i+1]][numberOfNeurons[i]+1];
			for(double[] neuronWeights : weightChanges[i])
				Arrays.fill(neuronWeights, 0.0);
		}
	}

	public MLP(int[] numberOfNeurons, int seed){
		this.numberOfLayers = numberOfNeurons.length;
		this.numberOfInputs = numberOfNeurons[0];
		this.numberOfOutputs = numberOfNeurons[numberOfNeurons.length - 1];
		setInitialWeights(numberOfNeurons);
		initializeRandomWeights(seed);
	}
	
	public MLP(int[] numberOfNeurons, String fileWithWeights){
		this.numberOfLayers = numberOfNeurons.length;
		this.numberOfInputs = numberOfNeurons[0];
		this.numberOfOutputs = numberOfNeurons[numberOfNeurons.length - 1];
		setInitialWeights(numberOfNeurons);
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
		catch (Exception e) {
			System.out.println("Wrong dimensions in file!");
		}
	}

	//Initialize all weights using a seed for random generator
	private void initializeRandomWeights(int seed){
		//weights = new double[numberOfLayers-1][numberOfNeurons][numberOfNeurons+1];
		for (int i=0; i<numberOfLayers-1; i++){
			for (int j=0; j<weights[i].length; j++){
				for (int k=0; k<weights[i][j].length; k++){
					Random rand = new Random(seed+k*5);
					weights[i][j][k] = -5 + 10 * rand.nextDouble();
				}				
			}
		}
	}

	//Transfer function given an input and number of layer
	private double transfer(double net, int layerNumber){
		switch (transferFunctions[layerNumber]){
			case 0: return net;
			case 1: return (Math.exp(net) - Math.exp(-net))/(Math.exp(net) + Math.exp(-net));
			case 2: return 1/(1 + Math.exp(-net));
			default: return -42;
		}
	}
	
	private double deriviative(int layerNumber, int neuronNumber) {
		switch (transferFunctions[layerNumber]){
			case 0: return 1;
			case 1: return 1 - Math.pow(transfer(outputsByNeurons[layerNumber][neuronNumber], layerNumber), 2);
			case 2: return transfer(outputsByNeurons[layerNumber][neuronNumber], layerNumber)*
					(1 - transfer(outputsByNeurons[layerNumber][neuronNumber], layerNumber));
			default: return -42;
		}
	}

	//Given the input for the layer and the number of layer
	//returns the output of layer
	private double[] getLayerOutput(double[] inputs, int layerNumber){
//		layerNumber is always smaller than real layer index, as we count only calculative layers
		double[] outputs = new double[weights[layerNumber].length];
		for(int neuronIndex=0; neuronIndex < outputs.length; neuronIndex++){
			double net = 1*weights[layerNumber][neuronIndex][0]; //BIAS
			for (int j = 1; j < weights[layerNumber][neuronIndex].length; j++){
				net += inputs[j-1] * weights[layerNumber][neuronIndex][j];
			}
			outputsByNeurons[layerNumber][neuronIndex] = net;
			outputs[neuronIndex] = transfer(net, layerNumber);
		}
		return outputs;
	}

	//Reads file and sets an array of input values for the MLP
	private void getInputsFromFile(String inputFilename){
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFilename));
			String line;
			int numberOfPatterns = 4;
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
	
//	for getting teacher values from file
	private void readTeacherOutput(String inputFilename) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputFilename));
			String line;
			int numberOfPatterns = 4;
			teacher = new double[numberOfPatterns][numberOfOutputs];
			int j=0;
			while ((line = br.readLine()) != null) {
   				if (line.startsWith("#")){
   					continue;
   				} else {
   					String[] allValues = line.replaceAll("\\s+", " ").split(" ");
   					double[] row = new double[numberOfOutputs];
   					for(int i=numberOfInputs; i<numberOfInputs + numberOfOutputs; i++){
   						row[i - numberOfInputs] = Double.parseDouble(allValues[i]);
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
	
//	count error by standard formula
	private double error(double[] output, double[] teacher) {
		double sum = 0.0;
		for(int i=0; i<numberOfOutputs; i++)
			sum += Math.pow(output[i] - teacher[i], 2);
		return 0.5*sum;
	}
	
	private double[] run(double[] input) {
//		go by layers and apply transferfunctions to get output
//		save outputs for each neuron
		double[] previousOutput = input;
//		start from the first hidden layer
		for(int j=0; j<numberOfLayers-1; j++){
			previousOutput = getLayerOutput(previousOutput, j);
		}
		return previousOutput;
	}

	private double singleStepLearning(double[] input, int patternNumber) {
		double[] result = run(input);
		
		int neuronIndex = 0, weightIndex = 0;
		for(int layerIndex = numberOfLayers-2; layerIndex>=0; layerIndex-- ) {
			for(double[] neuron : weights[layerIndex]) {
				double delta = delta(patternNumber, layerIndex, neuronIndex);
				for(double weight : neuron) {
					weightChanges[layerIndex][neuronIndex][weightIndex] = 
							weightChange(layerIndex, neuronIndex, weightIndex, delta);
					weightIndex ++;
				}
				neuronIndex ++;
				weightIndex = 0;
			}
			neuronIndex = 0;
		}
		applyWeightChanges();
		
		return error(result, teacher[patternNumber]);
	}
	
	private void applyWeightChanges() {
		int layerIndex = 0, neuronIndex = 0, weightIndex = 0;
		for(double[][] layer : weights) {
			for(double[] neuron : layer) {
				for(double weight : neuron) {
					weights[layerIndex][neuronIndex][weightIndex] += weightChanges[layerIndex][neuronIndex][weightIndex];
					weightIndex ++;
				}
				neuronIndex ++;
				weightIndex = 0;
			}
			layerIndex ++;
			neuronIndex = 0;
		}
	}
	
	private double delta(int patternNumber, int layer, int neuron) {
		double delta = 0;
//		last layer, because we save deltas only for "computative" layers
		if(layer == numberOfLayers - 2) {
			delta = (teacher[patternNumber][neuron] - transfer(outputsByNeurons[layer][neuron], layer))*
					deriviative(layer, neuron);
		}
		else {
			double sumDeltas = 0.0;
			double[] belowLayerDeltas = deltas[layer + 1];
			double[][] belowLayerWeights = weights[layer + 1];
			int belowLayerNeuron = 0;
			for(double[] belowNeuronWeights : belowLayerWeights) {
				sumDeltas += belowLayerDeltas[belowLayerNeuron] * belowNeuronWeights[neuron];
				belowLayerNeuron++;
			}
			delta = sumDeltas * deriviative(layer, neuron);
		}
		deltas[layer][neuron] = delta;
		return delta;
	}
	
	private double weightChange(int layer, int neuron, int weight, double delta) {
		return rates[layer]*delta*transfer(outputsByNeurons[layer][neuron], layer);
	}
	
	public static void main1(String[] args){
		
		/******************************
		Set parameters here
		******************************/
		//Number of neurons in each layer
		int[] configuration = {2, 2, 1};
		
		//rates of learning for every hidden performing calculations layer
		double[] userRates = {0.2, 0.2};
		rates = userRates;
		
		//Seed for random generator
		int seed = 42;
		
		// Trnsfer functions for each layer:
		//0 - identity, 1 - tahn, 2 - logistic
		int[] userTransferFunctions = {2, 2};
		transferFunctions = userTransferFunctions;
		
		//Should always be true:
		//configuration.length == transferFunctions.length + 1
		String inputFilename = "training.dat";

		/******************************
		Set parameters here
		******************************/

		MLP mlp = new MLP(configuration, seed);
//		MLP mlp = new MLP(configuration, "weights.dat");
		
		mlp.printWeights();
		mlp.getInputsFromFile(inputFilename);
		mlp.readTeacherOutput(inputFilename);
		
		double currentError = 100;
		int indexPattern;
		double sum = 0;
		long startTime = System.nanoTime();
		for(int i=0; i<100000; i++) {
			indexPattern = 0;
			for(double[] input : mlp.inputDataEpoch) {
				sum += mlp.singleStepLearning(mlp.inputDataEpoch[indexPattern], indexPattern);
				indexPattern ++;
			}
			//mlp.printWeights();
			currentError = sum/mlp.inputDataEpoch.length;
			sum = 0;
		}
		long endTime = System.nanoTime();

		System.out.print("Duration (in ms): ");
		System.out.println((endTime - startTime) / 1000000.0);
		
		mlp.printWeights();
		System.out.println();
		System.out.println();
		double[] inp = {0,0};
		System.out.println(mlp.run(inp)[0]);
		inp[1] = 1;
		System.out.println(mlp.run(inp)[0]);
		inp[0] = 1;
		inp[1] = 0;
		System.out.println(mlp.run(inp)[0]);
		inp[1] = 1;
		System.out.println(mlp.run(inp)[0]);
	}
}
