package Sudoku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

public class Sudoku {

	public static void main(String[] args) throws IOException {

		TreeSet<Integer> plot_x = new TreeSet<Integer>(); //keeps all the num of initial values in sorted order
		HashMap<Integer, Integer> plot_y_sum = new HashMap<Integer, Integer>(); //keeps the overall sum 
		HashMap<Integer, Integer> plot_y_num = new HashMap<Integer, Integer>(); //keeps the number of times that num of init values occurred

		int startFolder = 1;
		int startInstance = 1;
		int endFolder = 72;
		int endInstance = 11;

		for(int folder = startFolder; folder < endFolder; folder++) {
			for(int instance = startInstance; instance < endInstance; instance++) {

				System.out.println("folder: " + folder + " | instance: " + instance);
				int[][] grid = new int[9][9];
				int[][] closed = new int[9][9];
				int numInitial = 0;

				//read grid here
				BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Jayson\\workspace\\cs486_A1\\src\\problems\\" + folder + "\\" + instance + ".sd")));

				for(int i = 0; i < grid.length; i++) {
					String line = reader.readLine();
					String[] row = line.split(" ");
					String[] closedRow = line.replaceAll("[1-9]", "-1").split(" ");
					closed[i] = Arrays.asList(closedRow).stream().mapToInt(Integer::parseInt).toArray();
					grid[i] = Arrays.asList(row).stream().mapToInt(Integer::parseInt).toArray();
				}

				reader.close();

				for(int i = 0; i < closed.length; i++) {
					for(int k = 0; k < closed.length; k++) {
						if(closed[i][k] == -1)
							numInitial++;
					}
				}

				plot_x.add(numInitial);

				//print grid here
				/*System.out.println("Num of initial values: " + numInitial);
				for(int i = 0; i < grid.length; i++) {
					for(int k = 0; k < grid[i].length; k++) {
						System.out.print(grid[i][k] + " ");
					}
					System.out.println("");
				}*/

				Agent a = new Agent();
				if(!VersionA(grid, closed, a, "b"))
					System.out.println("Error. There were likely more than 10,000 variable assignments.");

				//print grid here
				System.out.println("");
				System.out.println("SOLUTION: ");
				for(int i = 0; i < grid.length; i++) {
					for(int k = 0; k < grid[i].length; k++) {
						System.out.print(grid[i][k] + " ");
					}
					System.out.println("");
				}

				Integer currentSum, currentNum = plot_y_num.get(numInitial);
				if((currentSum = plot_y_sum.get(numInitial)) == null) {
					currentSum = 0;
					currentNum = 0;
				}
				plot_y_sum.put(numInitial, currentSum + a.var_assign);
				plot_y_num.put(numInitial, currentNum + 1);

				System.out.println("variable assignments: " + a.var_assign);
				System.out.println("");

			}
		}

		System.out.println("Plot data");

		TreeSet<Integer> plot_x_temp = new TreeSet<Integer>(plot_x);

		while(!plot_x_temp.isEmpty()) {
			Integer plotNumInitial = plot_x_temp.first();
			plot_x_temp.remove(plotNumInitial);
			System.out.println(plotNumInitial);

		}
		while(!plot_x.isEmpty()) {
			Integer plotNumInitial = plot_x.first();
			plot_x.remove(plotNumInitial);
			double avg = plot_y_sum.get(plotNumInitial) / (double)plot_y_num.get(plotNumInitial);
			System.out.println(avg);

		}

	}

	public static boolean VersionA(int[][] grid, int[][] closed, Agent a, String version) {
		
		while(a.r < grid.length) {
			while(a.c < grid[a.r].length) {

				//find next empty position (in orderly fashion)
				if(closed[a.r][a.c] == -1) {
					a.c ++;
					continue;
				}

				//choose next possible number from constrained list
				if(nextValueA(grid, a, version) == 0) {
					//if no number exists, backtrack
					if(!backtrackA(closed, grid, a)) {
						//No solution found
						return false;
					}

				}else {
					a.c ++;
				}

				if(a.var_assign > 10000) {
					return false;
				}
			}

			a.r ++; a.c = 0;
		}

		return true;

	}

	private static boolean forwardCheck(int[][] grid, Agent a) {

		int r = a.r; int c = a.c;
		boolean retVal = true;
		
		//check if any of the updated cells have no more moves left
		//check row
		for(int i = 0; i < 9; i++) {
			if(grid[i][c] == 0) {
				if(checkConstraints(grid, i, c, 0) == 0) {
					retVal = false;
				}
			}
		}

		//check column
		for(int i = 0; i < 9; i++) {
			if(grid[r][i] == 0) {
				if(checkConstraints(grid, r, i, 0) == 0) {
					retVal = false;
				}
			}
		}

		//check block
		int sR = (int) Math.floor(r/3.0);
		int sC = (int) Math.floor(c/3.0);
		for(int k = sR * 3; k < (sR + 1) * 3; k++) {
			for(int j = sC * 3; j < (sC + 1) * 3; j++) {
				if(grid[k][j] == 0) {
					if(checkConstraints(grid, k, j, 0) == 0) {
						retVal = false;
					}
				}
			}

		}
		
		return retVal;
	}


	//returns the next value a cell can take. 
	//if no moves are available, returns 0.
	public static int checkConstraints(int[][] grid, int r, int c, int startValue) {
		constraintCheck:
			for(int i = startValue + 1; i <= 9; i++) {
				//check if this value is possible

				//constraint 1: check horizontal cells
				for(int k = 0; k < grid[r].length; k++) {
					if(grid[r][k] == i) {
						continue constraintCheck;
					}
				}

				//constraint 2: check vertical cells
				for(int k = 0; k < grid.length; k++) {
					if(grid[k][c] == i) {
						continue constraintCheck;
					}
				}

				//constraint 3: check cell block
				//get subdivision
				int sR = (int) Math.floor(r/3.0);
				int sC = (int) Math.floor(c/3.0);
				for(int k = sR * 3; k < (sR + 1) * 3; k++) {
					for(int j = sC * 3; j < (sC + 1) * 3; j++) {
						if(grid[k][j] == i) {
							continue constraintCheck;
						}
					}
				}

				//all constraints passed. This value is acceptable atm.
				return i;

			}
		return 0;
	}

	//generates the next possible value for a cell given the set constraints
	//if no value is found, return 0
	public static int nextValueA(int[][] grid, Agent a, String version) {

		int r = a.r;
		int c = a.c;
		int currentValue = grid[r][c];
		int originalValue = currentValue;
		int nextValue = 0;

		do {
			nextValue = checkConstraints(grid, r, c, currentValue);
			currentValue = nextValue;
		}while(nextValue != 0 && version.equals("b") && !forwardCheck(grid, a));
		
		if(originalValue != nextValue)
			a.var_assign++;
		
		grid[r][c] = nextValue;
		return nextValue;
	}

	public static boolean backtrackA(int[][] closed, int[][] grid, Agent a) {
		stepback(a); //step back from current cell

		while(a.r >= 0) {
			while(a.c >= 0) {

				int r = a.r; int c = a.c;

				if(closed[r][c] == -1) {
					stepback(a); //cell is closed, keep going back
					break;
				}
				if(grid[r][c] == 9) {
					resetCell(grid, a);
					stepback(a); //cell has no more options, keep going back
					break;
				}

				//an empty cell is found
				return true;

			}
		}

		return false;
	}

	public static void resetCell(int[][] grid, Agent a) {
		int r = a.r; int c = a.c;
		grid[r][c] = 0;
		a.var_assign++;
	}

	public static void stepback(Agent a) {
		a.c--;
		if(a.c < 0) {
			a.r --;
			a.c = 8;
		}
	}

	//Represents the cell you are currently checking
	public static class Agent {
		public Integer r, c;
		public int var_assign = 0;

		public Agent() {
			r = 0;
			c = 0;
		}

		public Agent(int r, int c) {
			this.r = r;
			this.c = c;
		}
		
	}

}
