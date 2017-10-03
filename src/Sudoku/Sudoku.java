package Sudoku;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeSet;

public class Sudoku {

	public static void main(String[] args) throws IOException {

		//variables for plot purposes. Homework specific.
		TreeSet<Integer> plot_x = new TreeSet<Integer>(); //keeps all the num of initial values in sorted order
		HashMap<Integer, Integer> plot_y_sum = new HashMap<Integer, Integer>(); //keeps the overall sum 
		HashMap<Integer, Integer> plot_y_num = new HashMap<Integer, Integer>(); //keeps the number of times that num of init values occurred

		int startFolder = 1;
		int startInstance = 1;
		int endFolder = 72;
		int endInstance = 11;

		for(int folder = startFolder; folder < endFolder; folder++) {
			for(int instance = startInstance; instance < endInstance; instance++) {

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

				//get num of initial values
				for(int i = 0; i < closed.length; i++) {
					for(int k = 0; k < closed.length; k++) {
						if(closed[i][k] == -1)
							numInitial++;
					}
				}

				plot_x.add(numInitial);

				System.out.println("folder: " + folder + " , instance: " + instance);
				Agent a = new Agent();
				if(!SolveSudoku(grid, closed, a, "b")) {
					System.out.println("Solve Sudoku stopped due to error.");
				}else {
					//print grid solution here
					System.out.println("SOLUTION: ");
					for(int i = 0; i < grid.length; i++) {
						for(int k = 0; k < grid[i].length; k++) {
							System.out.print(grid[i][k] + " ");
						}
						System.out.println("");
					}
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

		//print plot data here
		while(!plot_x.isEmpty()) {
			Integer plotNumInitial = plot_x.first();
			plot_x.remove(plotNumInitial);
			double avg = plot_y_sum.get(plotNumInitial) / (double)plot_y_num.get(plotNumInitial);
			System.out.println(plotNumInitial + " " + avg);
		}

	}

	public static boolean SolveSudoku(int[][] grid, int[][] closed, Agent a, String version) {

		Stack<SearchNode> order = new Stack<SearchNode>(); //keeps order of cells to be visited
		Stack<SearchNode> visited = new Stack<SearchNode>(); //keep history of cells searched for backtracking purposes

		//configure cells to be searched
		for(int i = 8; i >= 0; i--) {
			for(int j = 8; j >= 0; j--) {
				SearchNode sc = new SearchNode(i, j);
				order.push(sc);
			}
		}
		
		//most constrained variable heuristic implemented here
		if(version.equals("c")) {
			order.sort(new Comparator<SearchNode>() {

				@Override
				public int compare(SearchNode s1, SearchNode s2) {
					
					int c1 = numMoves(grid, s1.r, s1.c);
					int c2 = numMoves(grid, s2.r, s2.c);
					
					if(c2 < c1) return -1;
					if(c2 > c1) return 1;
					
					return 0;
				}

			});
		}

		while(!order.isEmpty()) {

			SearchNode sc = order.pop();
			visited.push(sc);
			a.r = sc.r;
			a.c = sc.c;

			//this cell is an initial value, find next empty position
			if(closed[a.r][a.c] == -1) {
				continue;
			}

			//assign domain to current cell and check constraints
			if(move(grid, a, version) == 0) {
				//if no number exists, backtrack
				if(!backtrack(closed, grid, a, order, visited)) {
					//No solution found
					System.out.println("err. no soln");
					return false;
				}

			}

			//terminate if num of var assignments is greater than 10000
			if(a.var_assign > 10000) {
				System.out.println("err. more than 10000 assigns.");
				return false;
			}
		}


		return true;

	}
	
	//checks the number of available moves a cell has
	public static int numMoves(int[][] grid, int r, int c) {

		int constraints = 0;
		int num = 0;
		for(int i = 7; i >= -1; i--) {
			int nextNum = nextMove(grid, r, c, i);
			
			if(nextNum != num) {
				num = nextNum;
				constraints++;
			}
			
		}
				
		return constraints;
	}

	/*	checks all the cells in a row, column and block
	* 	returns false if no moves are available for any of these cells
	*/	
	private static boolean forwardCheck(int[][] grid, Agent a) {

		int r = a.r; int c = a.c;
		boolean retVal = true;
		
		//check row
		for(int i = 0; i < 9; i++) {
			if(grid[i][c] == 0) {
				if(nextMove(grid, i, c, 0) == 0) {
					retVal = false;
				}
			}
		}

		//check column
		for(int i = 0; i < 9; i++) {
			if(grid[r][i] == 0) {
				if(nextMove(grid, r, i, 0) == 0) {
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
					if(nextMove(grid, k, j, 0) == 0) {
						retVal = false;
					}
				}
			}

		}

		return retVal;
	}


	//returns the next move for a cell given the set constraints
	//if no moves are available, returns 0.
	public static int nextMove(int[][] grid, int r, int c, int startValue) {

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

	
	/*	generates the next move for a cell filtered by forward checking if enabled
	*	if no value is found, return 0
	*/
	public static int move(int[][] grid, Agent a, String version) {

		int r = a.r;
		int c = a.c;
		int currentValue = grid[r][c];
		int originalValue = currentValue;
		int nextValue = 0;

		do {
			nextValue = nextMove(grid, r, c, currentValue);
			currentValue = nextValue;
			grid[r][c] = nextValue;
		}while(nextValue != 0 && (version.equals("b") || version.equals("c")) && !forwardCheck(grid, a));

		if(originalValue != nextValue)
			a.var_assign++;

		return nextValue;
	}

	//backtrack to an available previous cell
	public static boolean backtrack(int[][] closed, int[][] grid, Agent a, Stack<SearchNode> order, Stack<SearchNode> visited) {

		stepback(order, visited); //skip the current cell

		while(!order.isEmpty()) {

			SearchNode sc = order.pop();
			visited.push(sc);
			a.r = sc.r;
			a.c = sc.c;
			int r = a.r; int c = a.c;

			if(closed[r][c] == -1) {
				stepback(order, visited); //cell is closed, keep going back
				continue;
			}
			if(grid[r][c] == 9) {
				resetCell(grid, a);
				stepback(order, visited); //cell has no more options, keep going back
				continue;
			}

			order.push(visited.pop()); //an available cell is found
			return true;
		}

		return false;
	}

	public static void resetCell(int[][] grid, Agent a) {
		int r = a.r; int c = a.c;
		grid[r][c] = 0;
		a.var_assign++;
	}

	public static void stepback(Stack<SearchNode> order, Stack<SearchNode> visited) {
		order.push(visited.pop());
		order.push(visited.pop());
	}

	//keeps track of the order of cells searched
	public static class SearchNode {
		int r, c;
		public SearchNode(int r, int c) {
			// TODO Auto-generated constructor stub
			this.r = r;
			this.c = c;
		}
	}

	//keeps track of current cell searched and num of variable assignments
	public static class Agent {
		public Integer r = 0, c = 0;
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
