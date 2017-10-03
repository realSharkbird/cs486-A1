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
		int endFolder = 2;
		int endInstance = 2;

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
				if(!VersionB(grid, closed, a))
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

	public static boolean VersionA(int[][] grid, int[][] closed, Agent a) {

		while(a.r < grid.length) {
			while(a.c < grid[a.r].length) {

				//find next empty position (in orderly fashion)
				if(closed[a.r][a.c] == -1) {
					a.c ++;
					continue;
				}

				//choose next possible number from constrained list
				if(nextValueA(grid, a) == 0) {
					//if no number exists, backtrack
					if(!backtrackA(closed, grid, a)) {
						//No solution found
						return false;
					}
				}else {
					a.c ++;
				}

				if(a.var_assign > 10000) {
					//return false;
				}
			}

			a.r ++; a.c = 0;
		}

		return true;

	}


	public static boolean VersionB(int[][] grid, int[][] closed, Agent a) {

		int[][][] forwardCheck = new int[9][9][9];
		for(int i = 0; i < forwardCheck.length; i++) {
			for(int k = 0; k < forwardCheck[i].length; k++) {
				for(int j = 0; j < forwardCheck[i][k].length; j++) {
					forwardCheck[i][k][j] = 0;
				}
			}
		}

		while(a.r < grid.length) {
			while(a.c < grid[a.r].length) {

				//find next empty position (in orderly fashion)
				if(closed[a.r][a.c] == -1) {
					a.c ++;
					continue;
				}

				//choose next possible number from constrained list
				if(nextValueB(forwardCheck, grid, a) == 0) {
					//if no solution exists, backtrack
					if(!backtrackB(forwardCheck, closed, grid, a)) {
						//No solution found
						return false;
					}
				}else {
					a.c ++;
				}

				if(a.var_assign > 10000) {
					//return false;
				}
			}

			a.r ++; a.c = 0;
		}

		return true;
	}

	//generates the next possible value for a cell given the set constraints
	//if no value is found, return 0
	public static int nextValueA(int[][] grid, Agent a) {

		int r = a.r;
		int c = a.c;
		int currentValue = grid[r][c];
		int nextValue = 0;

		constraintCheck:
			for(int i = currentValue + 1; i <= 9; i++) {
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
				nextValue = i;
				break;

			}

		a.var_assign++;
		grid[r][c] = nextValue;
		return nextValue;
	}

	public static boolean backtrackA(int[][] closed, int[][] grid, Agent a) {
		return backtrackB(null, closed, grid, a);
	}

	public static void resetCell(int[][][] forwardCheck, int[][] grid, Agent a) {
		int r = a.r; int c = a.c;
		if(forwardCheck != null) {
			forwardUpdate(forwardCheck, grid, r, c, -1);
		}
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

	public static int nextValueB(int[][][] forwardCheck, int[][] grid, Agent a) {

		int r = a.r;
		int c = a.c;
		int currentValue = grid[r][c];
		int nextValue = 0;

		for(int j = currentValue; j < forwardCheck[r][c].length; j++) {
			int valid = forwardCheck[r][c][j];
			if(valid == 0) {
				nextValue = j + 1;
				grid[r][c] = nextValue;
				if(!forwardUpdate(forwardCheck, grid, r, c, 1)) {
					nextValue = 0;
					grid[r][c] = nextValue;
					forwardUpdate(forwardCheck, grid, r, c, -1);
					a.var_assign++;
					return nextValue;
				}
				break;
			}
		}

		grid[r][c] = nextValue;
		a.var_assign++;
		return nextValue;

	}

	public static boolean forwardUpdate(int[][][] forwardCheck, int[][] grid, int r, int c, int increment){
		int var = grid[r][c] - 1;

		if(var < 0) return false;

		//check row
		for(int i = 0; i < 9; i++) {
			forwardCheck[i][c][var] += increment;
		}

		//check column
		for(int i = 0; i < 9; i++) {
			forwardCheck[r][i][var] += increment;
		}

		//check block
		int sR = (int) Math.floor(r/3.0);
		int sC = (int) Math.floor(c/3.0);
		for(int k = sR * 3; k < (sR + 1) * 3; k++) {
			for(int j = sC * 3; j < (sC + 1) * 3; j++) {
				forwardCheck[k][j][var] += increment;
			}
		}

		boolean retVal = true;
		
		//check if any of the updated cells have no more moves left
		//check row
		for(int i = 0; i < 9; i++) {
			retVal = retVal && checkRemainingMoves(grid, forwardCheck, i, c);
		}

		//check column
		for(int i = 0; i < 9; i++) {
			retVal = retVal && checkRemainingMoves(grid, forwardCheck, r, i);
		}

		//check block
		for(int k = sR * 3; k < (sR + 1) * 3; k++) {
			for(int j = sC * 3; j < (sC + 1) * 3; j++) {
				retVal = retVal && checkRemainingMoves(grid, forwardCheck, k, j);
			}
		}

		return retVal;
	}

	public static boolean checkRemainingMoves(int[][] grid, int[][][] forwardCheck, int r, int c) {

		if(grid[r][c] != 0)
			return true;

		for(int i = 0; i < forwardCheck[r][c].length; i++) {
			if(forwardCheck[r][c][i] <= 0) { //there is a remaining move for this cell
				return true;
			}
		}
		return false;
	}

	public static boolean backtrackB(int[][][] forwardCheck, int[][] closed, int[][] grid, Agent a) {
		stepback(a); //step back from current cell

		while(a.r >= 0) {
			while(a.c >= 0) {

				int r = a.r; int c = a.c;

				if(closed[r][c] == -1) {
					stepback(a); //cell is closed, keep going back
					break;
				}
				if(grid[r][c] == 9) {
					resetCell(forwardCheck, grid, a);
					stepback(a); //cell has no more options, keep going back
					break;
				}

				//an empty cell is found
				return true;

			}
		}

		return false;
	}

	//Represents the cell you are currently checking
	public static class Agent {
		public Integer r, c;
		public int var_assign = 0;

		public Agent() {
			r = 0;
			c = 0;
		}

	}

}
