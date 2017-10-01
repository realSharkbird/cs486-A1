package TSP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

public class TSP {

	public static void main(String[] args) throws NumberFormatException, IOException{

		//statistics for plotting
		TreeSet<Integer> plot_x = new TreeSet<Integer>(); 
		HashMap<Integer, Integer> plot_y_sum = new HashMap<Integer, Integer>(); 
		HashMap<Integer, Integer> plot_y_num = new HashMap<Integer, Integer>(); 
		
		int startFolder = 1;
		int startInstance = 1;
		int endFolder = 17;
		int endInstance = 11;

		for(int folder = startFolder; folder < endFolder; folder++) {
			for(int instance = startInstance; instance < endInstance; instance++) {

				int numCities;
				ArrayList<City> cities = new ArrayList<City>();
				SearchNode result = null;

				//read nodes here
				BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Jayson\\workspace\\cs486_A1\\src\\randTSP\\" + folder + "\\instance_" + instance + ".txt")));
				numCities = Integer.parseInt(reader.readLine());
				for(int i = 0; i < numCities; i++) {
					String line = reader.readLine();
					String[] words = line.split(" ");
					String letter = words[0];
					int x = Integer.parseInt(words[1]);
					int y = Integer.parseInt(words[2]);
					City c = new City(letter, x, y);
					cities.add(c);
				}
				reader.close();

				plot_x.add(numCities);
				System.out.println("Num cities: " + numCities);

				SearchNodeTracker snt = new SearchNodeTracker();
				
				//do A* search here
				if((result = SolveTSP(cities, snt)) == null) {
					System.out.println("error");
				}else {
					//print results here

					System.out.println("SOLUTION: ");
					SearchNode resultBackup = result;
					while(result != null) {
						System.out.print(result.city.getLetter() + " ");
						if(result.cameFrom != null) {
							//System.out.println("Edge cost: " + getCost(result.city, result.cameFrom.city));
						}
						result = result.cameFrom;
					}
					System.out.println();
					System.out.println("Total Distance: " + resultBackup.gScore);

				}
				
				Integer currentSum, currentNum = plot_y_num.get(numCities);
				if((currentSum = plot_y_sum.get(numCities)) == null) {
					currentSum = 0;
					currentNum = 0;
				}
				plot_y_sum.put(numCities, currentSum + snt.searchNodes);
				plot_y_num.put(numCities, currentNum + 1);
				
				System.out.println("Search Nodes: " + snt.searchNodes);
				System.out.println();
			}
		}
		
		while(!plot_x.isEmpty()) {
			Integer plotNumCities = plot_x.first();
			plot_x.remove(plotNumCities);
			double avg = plot_y_sum.get(plotNumCities) / (double)plot_y_num.get(plotNumCities);
			System.out.println("Num of cities: " + plotNumCities);
			System.out.println("Average number of search nodes: " + avg);

		}
		
	}

	//get minimum salesman path
	//return false if no path is found
	public static SearchNode SolveTSP(ArrayList<City> cities, SearchNodeTracker snt){

		int searchNodes = 0;
		
		City start = cities.get(0);

		//represents all the current paths being visited in the search tree
		TreeSet<SearchNode> open = new TreeSet<SearchNode>(new Comparator<SearchNode>() {

			@Override
			public int compare(SearchNode n1, SearchNode n2) {
				Double fScore1 = n1.fScore;
				Double fScore2 = n2.fScore;
				if(fScore1 < fScore2) return -1;
				if(fScore1 > fScore2) return 1;

				return 0;
			}

		});

		searchNodes++;
		
		SearchNode searchNode = new SearchNode(start, cities);
		searchNode.gScore = 0;
		searchNode.fScore = getHeuristic(cities);
		open.add(searchNode);

		while(!open.isEmpty()){

			SearchNode node = open.first();
			City city = node.city;

			open.remove(node);
			node.visit(city);

			if(node.visited.size() == cities.size()) {

				if(snt != null)
					snt.searchNodes = searchNodes;
				
				return node;

			}

			for(City neighbour: node.open_cities) {

				searchNodes++;
				SearchNode child = new SearchNode(neighbour, node);

				//this is here so we end where we started
				if(!child.city.equals(start) && child.visited.size() == cities.size() - 1) {
					child.unVisit(start);
				}

				double tentative_gScore = node.gScore + getCost(city, neighbour);

				child.cameFrom = node;
				child.gScore = tentative_gScore;
				
				if(!(child.city.equals(start) && child.visited.size() == cities.size() - 1)) {
					//we have to include the final city in the heuristic
					//so temporarily add it
					child.open_cities.add(start);
					child.fScore = child.gScore + getHeuristic(child.open_cities);
					child.open_cities.remove(start);
				}else {
					child.fScore = child.gScore + getHeuristic(child.open_cities);
				}

				open.add(child);
			}

		}

		if(snt != null)
			snt.searchNodes = searchNodes;
		
		return null;
	}
	
	//heuristic function
	//which generates the min span tree
	public static double getHeuristic(ArrayList<City> open_cities) {

		ArrayList<ArrayList<City>> forest = new ArrayList<ArrayList<City>>(); //keep track of disjointed nodes
		ArrayList<Edge> MST = new ArrayList<Edge>();

		//kruskals algorithm
		//create list of edges
		TreeSet<Edge> edges = new TreeSet<Edge>(new Comparator<Edge>() {

			@Override
			public int compare(Edge e1, Edge e2) {

				//keep list sorted by length
				double dist1 = getCost(e1.c1, e1.c2);
				double dist2 = getCost(e2.c1, e2.c2);

				if(dist1 < dist2) return -1;
				if(dist1 > dist2) return 1;

				return 0;
			}

		});

		for(int i = 0; i < open_cities.size(); i++) {
			for(int k = i + 1; k < open_cities.size(); k++) {
				Edge e = new Edge(open_cities.get(i), open_cities.get(k));
				edges.add(e);
			}
		}


		while(!edges.isEmpty()) {

			Edge shortest = edges.first(); 	//pick the shortest edge to check first
			edges.remove(shortest);

			int merge = -1;

			for(int i = 0; i < forest.size(); i++) {
				ArrayList<City> tree = forest.get(i);

				//there is a cycle, don't add this edge to the MST
				if(tree.contains(shortest.c1) && tree.contains(shortest.c2)) {
					merge = -2;
					break;
				}

				City merger = null;
				if(tree.contains(shortest.c1)) {
					merger = shortest.c1;
					tree.add(shortest.c2);
				}

				if(tree.contains(shortest.c2)) {				
					merger = shortest.c2;
					tree.add(shortest.c1);
				}

				//
				if(merger != null) {
					if(merge != -1) {

						//merge 2 disjointed sets together
						ArrayList<City> previous = forest.get(merge);
						previous.addAll(tree);
						previous.remove(shortest.c1);
						previous.remove(shortest.c2);
						forest.remove(tree);
						break;

					}else{

						MST.add(shortest);
						merge = i;
					}
				}

			}

			//if edge does not connect to any current edges, create a new disjointed set
			if(merge == -1) {
				ArrayList<City> tree = new ArrayList<City>();
				tree.add(shortest.c1);
				tree.add(shortest.c2);
				forest.add(tree);
				MST.add(shortest);
			}
		}

		double heuristic = 0;
		for(int i = 0; i < MST.size(); i++) {
			heuristic += MST.get(i).distance;
		}

		return heuristic;
	}

	//real cost function
	public static double getCost(City n1, City n2) {
		double dx = n1.getX() - n2.getX();
		double dy = n1.getY() - n2.getY();
		return Math.hypot(dx, dy);
	}

	public static class SearchNode {

		public City city; //current node being searched
		public double fScore = Double.POSITIVE_INFINITY, gScore = Double.POSITIVE_INFINITY; //f and g score of this path
		public SearchNode cameFrom; //to rebuild the path if solution is found

		//self-explanatory
		public ArrayList<City> visited = new ArrayList<City>();
		public ArrayList<City> open_cities = new ArrayList<City>();

		public SearchNode(City n, ArrayList<City> open_cities) {
			this.city = n;

			this.open_cities.addAll(open_cities);
		}

		public SearchNode(City n, SearchNode searchNode) {
			this.city = n;

			this.open_cities.addAll(searchNode.open_cities);
			this.visited.addAll(searchNode.visited);
		}

		public void visit(City c) {
			visited.add(c);
			open_cities.remove(c);
		}

		public void unVisit(City c) {
			visited.remove(c);
			open_cities.add(c);
		}

	}

	public static class Edge {

		public City c1, c2;
		public double distance = 0;

		public Edge(City c1, City c2) {
			this.c1 = c1;
			this.c2 = c2;

			distance = getCost(c1, c2);
		}

	}
	
	public static class SearchNodeTracker{
		
		public int searchNodes = 0;
		
	}

}
