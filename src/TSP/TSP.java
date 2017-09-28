package TSP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

public class TSP {

	public static void main(String[] args) throws NumberFormatException, IOException{

		int numCities;
		ArrayList<City> cities = new ArrayList<City>();
		SearchNode result = null;
		
		//read nodes here
		BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Users\\Jayson\\workspace\\cs486_A1\\src\\randTSP\\16\\instance_1.txt")));
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
		
		//do A* search here
		if((result = SolveTSP(cities)) == null) {
			System.out.println("error");
		}else {
			//print results here
			while(result != null) {
				System.out.println(result.city.getLetter());
				result = result.cameFrom;
			}
			
		}
	}

	//get minimum salesman path
	//return false if no path is found
	public static SearchNode SolveTSP(ArrayList<City> cities){

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

		SearchNode searchNode = new SearchNode(start, cities);
		searchNode.gScore = 0;
		searchNode.fScore = getHeuristic(start, start);
		open.add(searchNode);
		
		while(!open.isEmpty()){

			SearchNode node = open.first();
			City city = node.city;

			open.remove(node);
			node.visit(city);
			
			if(node.visited.size() == cities.size()) {
				
				return node;

			}
			
			for(City neighbour: node.open_cities) {

				SearchNode child = new SearchNode(neighbour, node);
				
				if(!child.city.equals(start) && child.visited.size() == cities.size() - 1) {
					child.unVisit(start);
				}
				
				double tentative_gScore = node.gScore + getCost(city, neighbour);

				child.cameFrom = node;
				child.gScore = tentative_gScore;
				child.fScore = child.gScore + getHeuristic(neighbour, start);

				open.add(child);
			}

		}

		return null;
	}



	//heuristic function
	//which generates the min span tree
	public static double getHeuristic(City n1, City n2) {
		return 0;
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
			
			//this is pretty much the sucessor function
			this.open_cities.addAll(open_cities);
		}
		
		public SearchNode(City n, SearchNode searchNode) {
			this.city = n;
			
			//this is pretty much the sucessor function
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

}
