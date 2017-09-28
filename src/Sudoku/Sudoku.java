package Sudoku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Sudoku {

	public static void main(String[] args) throws IOException {

		int[][] grid = new int[9][9];
		int[][] closed = new int[9][9];

		//read grid here
		BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Jayson\\workspace\\cs486_A1\\src\\problems\\71\\10.sd")));

		for(int i = 0; i < grid.length; i++) {
			String line = reader.readLine();
			String[] row = line.split(" ");
			String[] closedRow = line.replaceAll("[1-9]", "-1").split(" ");
			closed[i] = Arrays.asList(closedRow).stream().mapToInt(Integer::parseInt).toArray();
			grid[i] = Arrays.asList(row).stream().mapToInt(Integer::parseInt).toArray();
		}

		reader.close();

		//print grid here
		System.out.println("BEFORE: ");
		for(int i = 0; i < grid.length; i++) {
			for(int k = 0; k < grid[i].length; k++) {
				System.out.print(grid[i][k] + " ");
			}
			System.out.println("");
		}

		if(!VersionA(grid, closed))
			System.out.println("Error.");

		//print grid here
		System.out.println("");
		System.out.println("AFTER: ");
		for(int i = 0; i < grid.length; i++) {
			for(int k = 0; k < grid[i].length; k++) {
				System.out.print(grid[i][k] + " ");
			}
			System.out.println("");
		}
	}

	public static boolean VersionA(int[][] grid, int[][] closed) {

		Agent a = new Agent();

		while(a.r < grid.length) {
			while(a.c < grid[a.r].length) {

				//find next empty position (in orderly fashion)
				if(closed[a.r][a.c] == -1) {
					a.c ++;
					continue;
				}

				//choose next possible number from constrained list
				if(nextValue(grid, a) == 0) {
					//if no number exists, backtrack
					if(!backtrack(closed, grid, a)) {
						//No solution found
						return false;
					}
				}else {
					a.c ++;
				}
			}

			a.r ++; a.c = 0;
		}

		return true;

	}
	
	public static boolean VersionB(int[][] grid, int[][] closed) {
		
		return true;
	}

	//generates the next possible value for a cell given the set constraints
	//if no value is found, return 0
	public static int nextValue(int[][] grid, Agent a) {
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

		grid[r][c] = nextValue;
		return nextValue;
	}

	public static boolean backtrack(int[][] closed, int[][] grid, Agent a) {

		stepback(a); //step back from current cell

		while(a.r >= 0) {
			while(a.c >= 0) {

				int r = a.r; int c = a.c;

				if(closed[r][c] == -1) {
					stepback(a); //cell is closed, keep going back
					break;
				}
				if(grid[r][c] == 9) {
					grid[r][c] = 0; //reset cell
					stepback(a); //cell has no more options, keep going back
					break;
				}
				
				//an empty cell is found
				return true;

			}
		}

		return false;
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

		public Agent() {
			r = 0;
			c = 0;
		}
	}

}
