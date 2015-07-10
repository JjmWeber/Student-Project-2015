package superpixels;

import java.util.ArrayList;
import java.util.List;

import fr.unistra.pelican.Image;



public class GraphImage {
	public ArrayList<Node> nodes;
	public ArrayList<Edge> edges;
	public int xDim;
	public int yDim;

	public GraphImage(Image inputImage){
		xDim = inputImage.xdim;
		yDim = inputImage.ydim;
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
		Pixel currentLine[] = new Pixel[xDim];
		Pixel nextLine[] = new Pixel[xDim];


		//initialization of the currentLine
		for(int x = 0; x < xDim; x++){
			double l = inputImage.getPixelXYBDouble(x, 0, 0);
			double a = inputImage.getPixelXYBDouble(x, 0, 1);
			double b = inputImage.getPixelXYBDouble(x, 0, 2);
			Pixel p = new Pixel(l,a,b,x,0);
			currentLine[x] = p;
		}
		for(int x = 0; x < xDim - 1; x++){
			edges.add(new Edge(x,x+1,computePixelDistance(currentLine[x],currentLine[x+1])));
			//	System.out.println("initialif"+"("+x+", "+0+")"+" weight = "+computePixelDistance(currentLine[x],currentLine[x+1]));
		}


		System.out.println("Initialisation terminée");
		System.out.println("xDim = "+xDim);
		System.out.println("yDim = "+yDim);
		for(int y = 1; y < yDim; y++){
			for(int x = 0; x < xDim; x++){
				double l = inputImage.getPixelXYBDouble(x, y, 0);
				double a = inputImage.getPixelXYBDouble(x, y, 1);
				double b = inputImage.getPixelXYBDouble(x, y, 2);
				Pixel p = new Pixel(l,a,b,x,y);
				nextLine[x] = p;
				nodes.add(new Node(currentLine[x]));
				if(x < xDim - 1){
					edges.add(new Edge(x+y*xDim,x+y*xDim+1,computePixelDistance(currentLine[x],currentLine[x+1])));
					//	System.out.println("liaison à droite"+"("+x+", "+y+")"+" weight = "+computePixelDistance(currentLine[x],currentLine[x+1]));
				}
				if(y < yDim){
					edges.add(new Edge(x+y*xDim,x+(y+1)*xDim,computePixelDistance(currentLine[x],nextLine[x])));
					//	System.out.println("liaison en dessous"+"("+x+", "+y+")"+" weight = "+computePixelDistance(currentLine[x],nextLine[x]));
				}
			}
			System.arraycopy(nextLine, 0, currentLine, 0, xDim);
		}
		// case of the last line
		for(int x = 0; x < xDim; x++){
			nodes.add(new Node(currentLine[x]));
		}


		/*	for(int i = 0; i <nodes.size(); i++){
			nodes.get(i).pixels.get(0).displayPixel();
		}*/
	}


	public void displayNode(int n){
		System.out.println("Node number "+n+"; internal difference = "+this.nodes.get(n).internalDifference+", it contains "+this.nodes.get(n).pixels.size()+" pixel(s)");
	}



	//Definition of the classes Pixel, Node, and Edge
	public class Pixel{

		public double l;
		public double a;
		public double b;
		public int x;
		public int y;
		public int id;

		public Pixel(double l, double a, double b, int x, int y)
		{
			this.l=l;
			this.a=a;
			this.b=b;
			this.x=x;
			this.y=y;
			this.id = y*yDim + x;
		}
		public void displayPixel(){
			System.out.println("pixel : "+"("+x+","+y+")");
		}
	}

	public class Node{
		public int id;
		public double internalDifference;
		public ArrayList<Pixel> pixels;

		public Node(Pixel p){
			this.id = p.x+p.y*yDim;
			this.pixels = new ArrayList<Pixel>();
			this.pixels.add(p);
			internalDifference = 0;
		}
	}
	public static class Edge implements Comparable<Edge>{
		public final int start, end;
		public final double weight;
		public final int tiebreaker;
		public static int nextTiebreaker = 0;

		public Edge(int start, int end, double weight) {
			this.start = start;
			this.end = end;
			this.weight = weight;
			tiebreaker = nextTiebreaker++;
		}
		public int compareTo(Edge other) {
			if (weight < other.weight) return -1;
			if (weight > other.weight) return +1;
			//If they have equal weights, we use the tiebreaker to make our decision
			return tiebreaker - other.tiebreaker;
		}

		public void displayEdge(){
			System.out.println("("+start+","+end+")"+", weight = "+weight);
		}
		
	}




	public double computePixelDistance(Pixel p1, Pixel p2){
		double pixelDifference = Math.sqrt((p1.l-p2.l)*(p1.l-p2.l)+(p1.a-p2.a)*(p1.a-p2.a)+(p1.b-p2.b)*(p1.b-p2.b));
		return pixelDifference;
	}





}
