import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Scanner;

public class MLP {
	private static int[] transferFunctions;
	private static double[] rates;

	private int numberOfLayers; // Including input layer
	private int numberOfInputs;
	private int numberOfOutputs;
	private double[][] inputDataEpoch;
	private double[][][] weights;
	private double[][] deltas; // All deltas for all neurons
	private double[][][] weightChanges;
	private double[][] teacher;
	private double[][] weightedSums; // Weighted sums for all neurons,
										// weightedSums[0] is the input!

	// Initialize the arrays
	private void setInitialWeights(int[] numberOfNeurons) {
		// - 1 because we do not have weights for input layer
		weights = new double[numberOfLayers - 1][][];
		for (int i = 0; i < numberOfLayers - 1; i++) {
			// for each layer we get particular number of neurons with number of
			// inputs + BIAS
			weights[i] = new double[numberOfNeurons[i + 1]][numberOfNeurons[i] + 1];
		}
		weightedSums = new double[numberOfLayers][];
		for (int i = 0; i < numberOfLayers; i++) {
			// for each layer we get particular number of neurons with number of
			// inputs + BIAS
			weightedSums[i] = new double[numberOfNeurons[i]];
		}
		deltas = new double[numberOfLayers - 1][];
		for (int i = 0; i < numberOfLayers - 1; i++) {
			// for each layer we get particular number of neurons with number of
			// inputs + BIAS
			deltas[i] = new double[numberOfNeurons[i + 1]];
		}
		// initialize weight changes with zeroes - so momentum and cumulated
		// weights change for the first step is 0
		weightChanges = new double[numberOfLayers - 1][][];
		for (int i = 0; i < numberOfLayers - 1; i++) {
			weightChanges[i] = new double[numberOfNeurons[i + 1]][numberOfNeurons[i] + 1];
			for (double[] neuronWeights : weightChanges[i])
				Arrays.fill(neuronWeights, 0.0);
		}
	}

	// Constructor for random weights
	public MLP(int[] numberOfNeurons, int seed) {
		this.numberOfLayers = numberOfNeurons.length;
		this.numberOfInputs = numberOfNeurons[0];
		this.numberOfOutputs = numberOfNeurons[numberOfNeurons.length - 1];
		setInitialWeights(numberOfNeurons);
		initializeRandomWeights(seed);
	}

	// Constructor for reading weights from file
	public MLP(int[] numberOfNeurons, String fileWithWeights) {
		this.numberOfLayers = numberOfNeurons.length;
		this.numberOfInputs = numberOfNeurons[0];
		this.numberOfOutputs = numberOfNeurons[numberOfNeurons.length - 1];
		setInitialWeights(numberOfNeurons);
		initializeWeightsFromFile(fileWithWeights);
	}

	// Read weights from file
	private void initializeWeightsFromFile(String filename) {
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			for (int i = 0; i < numberOfLayers - 1; i++) {
				// + 1 for BIAS
				for (int j = 0; j < weights[i].length; j++) {
					for (int k = 0; k < weights[i][j].length; k++) {
						weights[i][j][k] = scanner.nextDouble();
					}
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println("File with weights not found!");
		} catch (Exception e) {
			System.out.println("Wrong dimensions in file!");
		}
	}

	// Initialize all weights using a seed for random generator
	private void initializeRandomWeights(int seed) {
		Random rand = new Random(seed);
		for (int i = 0; i < numberOfLayers - 1; i++) {
			for (int j = 0; j < weights[i].length; j++) {
				for (int k = 0; k < weights[i][j].length; k++) {
					weights[i][j][k] = (rand.nextDouble() - 0.5) * 4;
				}
			}
		}
	}

	// Transfer function. given an input and the number of layer
	private double transfer(double net, int layerNumber) {
		switch (transferFunctions[layerNumber]) {
		case 0:
			return net;
		case 1:
			return (Math.exp(net) - Math.exp(-net))
					/ (Math.exp(net) + Math.exp(-net));
		case 2:
			return 1 / (1 + Math.exp(-net));
		default:
			return -42;
		}
	}

	// The derivative, given an input and number of layer
	private double deriviative(double net, int layerNumber) {
		switch (transferFunctions[layerNumber]) {
		case 0:
			return 1;
		case 1:
			return 1 - Math.pow(transfer(net, layerNumber), 2);
		case 2:
			return transfer(net, layerNumber)
					* (1 - transfer(net, layerNumber));
		default:
			return -42;
		}
	}

	// Reads file and sets an array of input values for the MLP
	private void getInputsFromFile(String inputFilename) {
		try {
			List<String> content = Files.readAllLines(Paths.get(inputFilename),
					Charset.defaultCharset());
			List<String> patterns = new ArrayList<String>();
			for (String line : content) {
				if (line.startsWith("#")) {
					continue;
				}
				patterns.add(line);
			}
			inputDataEpoch = new double[patterns.size()][numberOfInputs];
			int j = 0;
			for (String line : patterns) {
				String[] allValues = line.split(" ");
				for (int i = 0; i < numberOfInputs; i++) {
					inputDataEpoch[j][i] = Double.parseDouble(allValues[i]);
				}
				j++;
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

	}

	// Print out the weights for each layer and each neuron
	public void printWeights() {
		for (int i = 0; i < numberOfLayers - 1; i++) {
			System.out.println("");
			System.out.println("Weights of the layer" + String.valueOf(i + 2)
					+ ":");
			for (int j = 0; j < weights[i].length; j++) {
				System.out.println("Weights of the neuron"
						+ String.valueOf(j + 1) + ":");
				for (int k = 0; k < weights[i][j].length; k++) {
					System.out.println(weights[i][j][k]);
				}
			}
		}
	}

	// Read teacher values from the file
	private void readTeacherOutput(String inputFilename) {
		try {
			List<String> content = Files.readAllLines(Paths.get(inputFilename),
					Charset.defaultCharset());
			List<String> teacherRawValues = new ArrayList<String>();
			for (String line : content) {
				if (line.startsWith("#")) {
					continue;
				}
				teacherRawValues.add(line);
			}
			teacher = new double[teacherRawValues.size()][numberOfOutputs];
			int j = 0;
			for (String line : teacherRawValues) {
				String[] allValues = line.split(" +");
				for (int i = numberOfInputs; i < numberOfInputs
						+ numberOfOutputs; i++) {
					teacher[j][i - numberOfInputs] = Double
							.parseDouble(allValues[i]);
				}
				j++;
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	// Count error by standard formula
	private double error(double[] output, double[] teacher) {
		double sum = 0.0;
		for (int i = 0; i < numberOfOutputs; i++)
			sum += Math.pow(output[i] - teacher[i], 2);
		return 0.5 * sum;
	}

	// Given the number of the layer and an input prints the output
	private void printOutputOfLayer(double[] input, int layerNumber) {
		System.out.println("Input: " + Arrays.toString(input));
		System.out.println("Output of the layer " + layerNumber + ": "
				+ Arrays.toString(getOutputOfLayer(input, layerNumber)));
	}

	// Given the number of the layer and an input returns the output
	private double[] getOutputOfLayer(double[] input, int layerNumber) {
		calculateWeightedSums(input);
		double[] output = new double[weights[layerNumber - 2].length];
		for (int i = 0; i < output.length; i++) {
			output[i] = transfer(weightedSums[layerNumber - 1][i],
					layerNumber - 2);
		}
		return output;
	}

	// Calculate and save all weighted sums for given input
	private void calculateWeightedSums(double[] input) {

		double[] previousOutput = input;
		weightedSums[0] = input;
		for (int layerNumber = 1; layerNumber < numberOfLayers; layerNumber++) {
			List<Double> layerOutput = new ArrayList<Double>();
			// System.out.println(Arrays.toString(previousOutput));
			for (int neuronIndex = 0; neuronIndex < weights[layerNumber - 1].length; neuronIndex++) {
				// BIAS
				double net = weights[layerNumber - 1][neuronIndex][0];
				for (int weightIndex = 1; weightIndex < weights[layerNumber - 1][neuronIndex].length; weightIndex++) {
					net += previousOutput[weightIndex - 1]
							* weights[layerNumber - 1][neuronIndex][weightIndex];
				}
				weightedSums[layerNumber][neuronIndex] = net;
			}
			// Calculate transfer functions of weighted sums of the previous
			// layer and save to layerOutput

			for (int i = 0; i < weightedSums[layerNumber].length; i++) {
				layerOutput.add(transfer(weightedSums[layerNumber][i],
						layerNumber - 1));
			}
			previousOutput = new double[layerOutput.size()];
			for (int i = 0; i < layerOutput.size(); i++) {
				previousOutput[i] = layerOutput.get(i);
			}
			layerOutput.clear();

		}
	}

	// Calculate and save all deltas for given patternNumber (i.e. for teacher
	// value)
	private void calculateDeltas(int patternNumber) {
		// Output layer
		for (int i = 0; i < numberOfOutputs; i++) {
			// double teacher = teacher[patternNumber][i];
			deltas[numberOfLayers - 2][i] = (teacher[patternNumber][i] - transfer(
					weightedSums[numberOfLayers - 1][i], numberOfLayers - 2))
					* deriviative(weightedSums[numberOfLayers - 1][i],
							numberOfLayers - 2);
		}
		// All other layers
		for (int layerIndex = numberOfLayers - 3; layerIndex >= 0; layerIndex--) {
			double[] deltasBelow = deltas[layerIndex + 1];
			double[][] weightsBelow = weights[layerIndex + 1];
			for (int neuronIndex = 0; neuronIndex < weights[layerIndex].length; neuronIndex++) {
				double sumDeltas = 0.0;
				double delta = 0.0;
				// Calculate the sum
				for (int neuronBelowIndex = 0; neuronBelowIndex < weightsBelow.length; neuronBelowIndex++) {
					sumDeltas += deltasBelow[neuronBelowIndex]
							* weightsBelow[neuronBelowIndex][neuronIndex + 1];
				}
				// Calculate delta
				delta = sumDeltas
						* deriviative(
								weightedSums[layerIndex + 1][neuronIndex],
								layerIndex);
				deltas[layerIndex][neuronIndex] = delta;
			}

		}

	}

	// Change the weights and return an error for a specific input
	private double singleStepLearning(double[] input, int patternNumber) {
		// Calculate and save weighted sums for all neurons
		calculateWeightedSums(input);

		// Calculate and save all deltas
		calculateDeltas(patternNumber);

		// Calculate all changes at weights
		for (int layerIndex = 0; layerIndex < numberOfLayers - 1; layerIndex++) {
			for (int neuronIndex = 0; neuronIndex < weights[layerIndex].length; neuronIndex++) {
				// Change BIAS weight
				weightChanges[layerIndex][neuronIndex][0] = rates[layerIndex]
						* deltas[layerIndex][neuronIndex] * 1;
				// All the rest weights
				for (int weightIndex = 1; weightIndex < weights[layerIndex][neuronIndex].length; weightIndex++) {
					weightChanges[layerIndex][neuronIndex][weightIndex] = weightChange(
							layerIndex, neuronIndex, weightIndex);
				}
			}
		}
		// Apply it
		applyWeightChanges();
		// Calculate an output and return an error
		double result[] = new double[numberOfOutputs];
		for (int i = 0; i < result.length; i++) {
			result[i] = transfer(weightedSums[numberOfLayers - 1][i],
					numberOfLayers - 2);
		}
		return error(result, teacher[patternNumber]);
	}

	// Apply all changes at weights at once
	private void applyWeightChanges() {
		for (int layerIndex = 0; layerIndex < numberOfLayers - 1; layerIndex++) {
			for (int neuronIndex = 0; neuronIndex < weights[layerIndex].length; neuronIndex++) {
				for (int weightIndex = 0; weightIndex < weights[layerIndex][neuronIndex].length; weightIndex++) {
					weights[layerIndex][neuronIndex][weightIndex] += weightChanges[layerIndex][neuronIndex][weightIndex];
				}
			}
		}
	}

	// Calculate changes at weights given a layer, neuron and a weight that
	// changes
	private double weightChange(int layer, int neuron, int weight) {

		if (layer == 0) {
			return rates[layer] * deltas[layer][neuron]
					* (weightedSums[layer][neuron]);
		} else {
			return rates[layer] * deltas[layer][neuron]
					* transfer(weightedSums[layer][weight - 1], layer);
		}
	}

	// Run n (=5000) times learning, save errors to the file and print some nice
	// message
	public void singleLearning() {
		List<Double> errors = new ArrayList<Double>();
		int indexPattern;
		double sumError = 0;
		long startTime = System.nanoTime();
		for (int i = 0; i < 5000; i++) {
			indexPattern = 0;
			for (double[] input : inputDataEpoch) {
				sumError += singleStepLearning(input, indexPattern);
				indexPattern++;
			}
			errors.add(sumError);
			sumError = 0;
		}
		long endTime = System.nanoTime();
		System.out
				.print("Done! Errors are saved to the file 'errors.dat'. Duration (in ms): ");
		System.out.println((endTime - startTime) / 1000000.0);

		// Save errors to the file
		String fileName = "errors.dat";
		try {
			PrintWriter fout = new PrintWriter(new BufferedWriter(
					new FileWriter(fileName)));
			for (int i = 0; i < errors.size(); i++) {
				fout.printf("%d %f", i, errors.get(i));
				fout.printf("\n");
			}
			fout.close();
		} catch (IOException e) {
			// if any I/O error occurs
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		/******************************
		 * Set parameters here
		 ******************************/
		// Number of neurons in each layer
		int[] configuration = { 4, 4, 2 };

		// rates of learning for every hidden performing calculations layer
		double[] userRates = { 0.3, 0.01 };
		rates = userRates;

		// Seed for random generator
		int seed = 41;

		// Transfer functions for each layer:
		// 0 - identity, 1 - tahn, 2 - logistic
		int[] userTransferFunctions = { 1, 1 };
		transferFunctions = userTransferFunctions;

		// Should always be true:
		// configuration.length == transferFunctions.length + 1
		String inputFilename = "training.dat";

		/******************************
		 * Set parameters here
		 ******************************/

		MLP mlp = new MLP(configuration, seed);
		// MLP mlp = new MLP(configuration, "weights.dat");

		mlp.getInputsFromFile(inputFilename);
		mlp.readTeacherOutput(inputFilename);
		mlp.singleLearning();

		// Test and compare teacher values with the results. Sometimes looks
		// pretty nice.
		double[] testInput = { 0.1, 0.1, 0.1, 0.1 };
		// 3 is the output layer
		mlp.printOutputOfLayer(testInput, 3);

	}

}