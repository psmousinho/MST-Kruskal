import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.omg.CORBA.NVList;

class Vertex {
	private int cod;
	private double x, y;
	
	Vertex(int cod, double x, double y) {
		this.x = x;
		this.y = y;
		this.cod = cod;
	}
	
	public int getCod() {
		return cod;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	static double calculateDist(Vertex v1, Vertex v2) {
		return Math.sqrt( (v1.getX() - v2.getX()) * (v1.getX() - v2.getX())  + (v1.getY() - v2.getY()) * (v1.getY() - v2.getY()));
	}
	
}

class Edge {
	private Vertex v1, v2;
	private int dist;
	
	public Edge(Vertex v1, Vertex v2) {
		this.v1 = v1;
		this.v2 = v2;
		this.dist = (int) Vertex.calculateDist(v1, v2);
	}
	
	public Vertex getV1() {
		return v1;
	}
	
	public Vertex getV2() {
		return v2;
	}
	
	public int getDist() {
		return dist;
	}

}

class DisjointSets {
	private int nSets;
	private int[] parent, ranks;

	DisjointSets(int nSets) {
		this.nSets = nSets;
		parent = new int[nSets];
		ranks = new int[nSets];

		for (int i = 0; i < nSets; i++) {
			parent[i] = i;
		}
	}

	int findSet(int vertice) {
		if (vertice != parent[vertice])
			parent[vertice] = findSet(parent[vertice]);
		return parent[vertice];
	}

	void mergeSets(int parentSet1, int parentSet2) {
		parentSet1 = findSet(parentSet1); 
		parentSet2 = findSet(parentSet2); 

		if (ranks[parentSet1] > ranks[parentSet2]) 
			parent[parentSet2] = parentSet1; 
		else 
			parent[parentSet1] = parentSet2; 
	  
		if (ranks[parentSet1] == ranks[parentSet2]) 
			ranks[parentSet2]++; 
	}
	
}

class Graph {

	private int nVertices;
	private Vertex[] vertices;
	private Edge[] edges;
	public enum SortType {
		HEAP, COUNTING;
	}

	public Graph(int nVertices, Vertex[] vertices, Edge[] edges) {
		this.nVertices = nVertices;
		this.vertices = vertices;
		this.edges = edges;
	}
	
	public int getNVector() {
		return this.nVertices;
	}
	
	public Vertex[] getVetices() {
		return this.vertices;
	}

	public Edge[] getEdges() {
		return this.edges;
	}

	public ArrayList<Edge> kruskalMST(SortType sortType) {
		ArrayList<Edge> result = new ArrayList<>();
		
		 if(sortType == SortType.HEAP) 
			 heapSort();
		else 
			countingSort();

		DisjointSets sets = new DisjointSets(nVertices);
		
		for(Edge e : edges) {
			int setV1, setV2;
			setV1 = sets.findSet(e.getV1().getCod());
			setV2 = sets.findSet(e.getV2().getCod());
			
			if(setV1 != setV2) {
				result.add(e);
				sets.mergeSets(setV1, setV2);
			}
			
		}
					

		return result;
	}

	private void countingSort() {
		int max = 0, min = Integer.MAX_VALUE;
		for(Edge e : edges) {
			if(e.getDist() > max)
				max = e.getDist();
			if(e.getDist() < min)
				min = e.getDist();
		}
		
		int[] count = new int[max - min + 1];
		
		for(int i = 0 ; i < edges.length; i++) {
			count[edges[i].getDist() - min]++;
		}
		
		for(int i = 1; i < count.length; i++) {
			count[i] +=count[i-1];
		}
		
		Edge[] orderEdges = new Edge[edges.length];
		for(int i = edges.length - 1; i >=0; i--) {
			orderEdges[count[edges[i].getDist() - min] -1] = edges[i];
			count[edges[i].getDist()- min]--;
		}
		edges = orderEdges;
	}
	
	private void heapSort() {
		for(int i = edges.length/2 - 1; i >= 0; i--) {
			maxheapify(edges, i, edges.length);
		}
		
		for(int i = edges.length-1; i >= 0; i--) {
			Edge aux = edges[i];
			edges[i] = edges[0];
			edges[0] = aux;
			maxheapify(edges, 0, i-1);
		}
	}
	
	private void maxheapify(Edge[] arr, int i, int n) {
		int L =  2 * i + 1;
		int R =  2 * i + 2;
		int largest = i;
		
		if (L < n && arr[L].getDist() > arr[i].getDist()) 
			largest = L;
		if( R < n && arr[R].getDist() > arr[largest].getDist())
			largest = R;
		if(largest != i) {
			Edge aux = arr[i];
			arr[i] = arr[largest];
			arr[largest] = aux;
			maxheapify(arr, largest, n);
		}
			
	}
	
	
	public static void main(String args[]) {
		try {
			Scanner sc = new Scanner(new File(args[0]));
			
			sc.next(); //NAME:
			String name = sc.next();
			sc.next(); //DIMENSION
			int dimension = Integer.parseInt(sc.next());
			sc.next(); //DISPLAY_DATA_SECTION

			Vertex[] vertices = new Vertex[dimension];			
			String line = null;
			for(int i = 0; i < dimension; i++) {
				//index
				sc.next();
				//x
				line = sc.next();				
				double x = Double.parseDouble(line);
				//y
				line = sc.next();
				double y = Double.parseDouble(line);
											
				vertices[i] =  new Vertex(i, x, y);
			}
			
			Edge[] edges = new Edge[dimension*(dimension-1) / 2];
			int index = 0;
			for(int i = 0; i < dimension; i++) {
				for(int j = i+1; j < dimension; j++) {
					edges[index++] = new Edge(vertices[i], vertices[j]);
				}
			}
			
			Graph graph;
			ArrayList<Edge> mstH = null;
			ArrayList<Edge> mstC = null;
			
			long timeSum = 0;
			for(int i = 0; i <10; i++) {
				long beguining = System.currentTimeMillis();
				graph = new Graph(dimension, vertices, edges.clone());
				mstH = graph.kruskalMST(Graph.SortType.HEAP);
				timeSum += System.currentTimeMillis() - beguining;
			}
			
			int costSum = 0;
			for(Edge e : mstH) {
				costSum += e.getDist();
			}
			System.out.println("Instancia: " + name);
			System.out.println("Arvore geradora produzida por ordenação via HeapSort");
			System.out.println("Preço Total: " + costSum);
			System.out.println("Media dos tempos de Execução: " + timeSum/10 + "\n");
			
			timeSum = 0;
			for(int i = 0; i <10; i++) {
				long beguining = System.currentTimeMillis();
				graph = new Graph(dimension, vertices, edges.clone());
				mstC = graph.kruskalMST(Graph.SortType.COUNTING);
				timeSum += System.currentTimeMillis() - beguining;
			}
			
			costSum = 0;
			for(Edge e : mstC) {
				costSum += e.getDist();
			}
			System.out.println("Instancia: " + name);
			System.out.println("Arvore geradora produzida por ordenação via CountingSort");
			System.out.println("Preço Total: " + costSum);
			System.out.println("Media dos tempos de Execução: " + timeSum/10 + "\n");
 			
			sc.close();

		} catch (FileNotFoundException e) {
			System.err.println("Erro ao Abrir o Arquivo");
			e.printStackTrace();
		}

	}
	
}
