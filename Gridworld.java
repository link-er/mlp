package neuronet_7;

import java.util.Arrays;
import java.util.Random;

public class Gridworld {
	private int[] world = new int[11];
	public Gridworld() {
		Arrays.fill(world, 0);
	}
	
	private static int numberOfIterations = 10000;
	
	public static void main(String[] args) {
		Gridworld ourWorld = new Gridworld();
		Random rand = new Random();
		int initial;
		boolean move;
		int current;
		int movesCount;
		for(int i=0; i<numberOfIterations; i++) {
			initial = rand.nextInt(10) + 1;
			current = initial;
			movesCount = 0;
			while(current!=0) {
				move = rand.nextBoolean();
				movesCount++;
				if(move && current==10)
					current--;
				else
					if(move)
						current++;
					else
						current--;
			}
			ourWorld.world[initial] -= movesCount;
		}
		System.out.println(Arrays.toString(ourWorld.world));
	}
}
