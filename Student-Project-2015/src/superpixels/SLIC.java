package superpixels;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.RGBToXYZ;
import fr.unistra.pelican.algorithms.conversion.XYZToLAB;
import fr.unistra.pelican.algorithms.morphology.connected.ConnectedComponentMap;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.FlatConnectivity;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Implementation of SLIC superpixels
 * 
 * Radhakrishna Achanta, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine Süsstrunk,
 *  SLIC Superpixels Compared to State-of-the-art Superpixel Methods, 
 * IEEE Transactions on Pattern Analysis and Machine Intelligence, vol. 34, num. 11, p. 2274 - 2282, May 2012.
 * 
 * @author Jonathan Weber
 */

public class SLIC extends Algorithm {

	public Image inputImage;
	public int numberOfSuperpixels;
	public boolean fuseSuperpixels;


	//Between 1 and 40, low value to respect boundary, high value to respect compactness
	public double m;

	public IntegerImage superpixels;

	public SLIC()
	{
		super();
		super.inputs="inputImage,numberOfSuperpixels,m,fuseSuperpixels";
		super.outputs = "superpixels";
	}

	@Override
	public void launch() throws AlgorithmException {
		int xDim = inputImage.xdim;
		int yDim = inputImage.ydim;


		// Convert image to Lab color space
		Image lab = XYZToLAB.exec(RGBToXYZ.exec(inputImage));

		// Perform gradient on RGB image
		Image gradient = MultispectralEuclideanGradient.exec(inputImage, FlatStructuringElement2D.createSquareFlatStructuringElement(3));

		//Compute step value
		int step = (int) Math.round(Math.sqrt(((double)gradient.size())/numberOfSuperpixels));

		//Initiate cluster
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		for(int y=step/2;y<yDim;y+=step)
			for(int x=step/2;x<xDim;x+=step)
			{
				clusters.add(new Cluster(lab.getPixelXYBDouble(x, y, 0),lab.getPixelXYBDouble(x, y, 1),lab.getPixelXYBDouble(x, y, 2),x,y));
			}

		final ArrayList<int[]> finalSuperpixelsCore = new ArrayList<int[]>(clusters.size());


		//Move cluster center to lowest gradient in 3x3 neighbourhood
		for(Cluster c : clusters)
		{
			int lowestGradient=Integer.MAX_VALUE;
			int bestX=0;
			int bestY=0;
			for(int y=c.y-1;y<=c.y+1;y++)
				for(int x=c.x-1;x<=c.x+1;x++)
				{
					if(gradient.getPixelXYByte(x, y)<lowestGradient)
					{
						lowestGradient=gradient.getPixelXYByte(x, y);
						bestX=x;
						bestY=y;
					}
				}
			c.x=bestX;
			c.y=bestY;
			c.l=lab.getPixelXYBDouble(bestX, bestY, 0);
			c.a=lab.getPixelXYBDouble(bestX, bestY, 1);
			c.b=lab.getPixelXYBDouble(bestX, bestY, 2);
		}

		// initialize label and distance image
		IntegerImage label = new IntegerImage(xDim,yDim,1,1,1);
		label.fill(-1);
		DoubleImage distance = new DoubleImage(xDim,yDim,1,1,1);
		distance.fill(Double.MAX_VALUE);

		double error=0;
		int loop=0;
		do
		{			
			loop++;
			System.out.println("Loop "+loop+" started !");

			// Pixel assignment to cluster
			for(int k=0;k<clusters.size();k++)
			{
				Cluster c = clusters.get(k);
				int yMin=Math.max(0, c.y-step);
				int yMax=Math.min(yDim-1, c.y+step);
				int xMin=Math.max(0, c.x-step);
				int xMax=Math.min(xDim-1, c.x+step);
				for(int y=yMin;y<=yMax;y++)
					for(int x=xMin;x<=xMax;x++)
					{
						double l=lab.getPixelXYBDouble(x,y,0);
						double a=lab.getPixelXYBDouble(x,y,1);
						double b=lab.getPixelXYBDouble(x,y,2);
						double dc=Math.sqrt((l-c.l)*(l-c.l)+(a-c.a)*(a-c.a)+(b-c.b)*(b-c.b));
						double ds=Math.sqrt((x-c.x)*(x-c.x)+(y-c.y)*(y-c.y));
						double d = Math.sqrt(dc*dc+((ds/step)*(ds/step))*(m*m));
						if(d<distance.getPixelXYDouble(x, y))
						{
							distance.setPixelXYDouble(x,y,d);
							label.setPixelXYInt(x, y, k);
						}
					}						
			}
			// Cluster center update and error computation
			error=0;
			int[][] newClusterCenter = new int [clusters.size()][2];
			for(int[] val : newClusterCenter)
			{
				val[0]=0;
				val[1]=0;
			}
			int[] newClusterPixelCount = new int[clusters.size()];
			Arrays.fill(newClusterPixelCount, 0);
			for(int y=0;y<yDim;y++)
				for(int x=0;x<xDim;x++)
				{
					int labelValue = label.getPixelXYInt(x,y);
					if(labelValue!=-1)
					{
						newClusterCenter[labelValue][0]+=x;
						newClusterCenter[labelValue][1]+=y;
						newClusterPixelCount[labelValue]++;
					}
				}
			finalSuperpixelsCore.clear();
			for(int i=0;i<newClusterCenter.length;i++)
			{
				int newX = Math.round(((float) newClusterCenter[i][0])/newClusterPixelCount[i]);
				int newY = Math.round(((float) newClusterCenter[i][1])/newClusterPixelCount[i]);

				error+=Math.sqrt((newX-clusters.get(i).x)*(newX-clusters.get(i).x)+(newY-clusters.get(i).y)*(newY-clusters.get(i).y));
				clusters.get(i).x=newX;
				clusters.get(i).y=newY;
				clusters.get(i).l=lab.getPixelXYBDouble(newX, newY, 0);
				clusters.get(i).a=lab.getPixelXYBDouble(newX, newY, 1);
				clusters.get(i).b=lab.getPixelXYBDouble(newX, newY, 2);

				int[] coordinates = new int[3];
				coordinates[0] = newX;
				coordinates[1] = newY;
				finalSuperpixelsCore.add(i,coordinates);

			}			
			System.out.println("Loop "+loop+" done ! Residual error : "+error);
		} while (error!=0);


		label = enforceConnectivity(label,finalSuperpixelsCore);
		if(fuseSuperpixels)
		label = fuse(label,lab,finalSuperpixelsCore,9,5);

		//Segmentation result
		superpixels = label;



	}

	/**
	 * @param label  the IntegerImage where to enforce connectivity
	 * @param finalSuperpixelsCore the array with coordinates of the superpixels centers
	 * @return label the IntegerImage with enforced connectivity
	 */
	public IntegerImage enforceConnectivity(IntegerImage label,ArrayList<int[]> finalSuperpixelsCore){
		System.out.println("We now enforce connectivity");
		//we grab the label of each superpixel core
		for(int i = 0; i <finalSuperpixelsCore.size();i++ ){
			finalSuperpixelsCore.get(i)[2] = label.getPixelXYInt(finalSuperpixelsCore.get(i)[0],finalSuperpixelsCore.get(i)[1]);
		}
		int xDim = inputImage.xdim;
		int yDim = inputImage.ydim;
		int sizeCriteria = ((xDim*yDim/finalSuperpixelsCore.size())/4);
		System.out.println("size criteria = "+sizeCriteria);

		//we create an IntegerImage called labelsMapImage to store the informations about connexe components
		Connectivity3D con = new FlatConnectivity(label, TrivialConnectivity.getHeightNeighbourhood());
		IntegerImage labelsMapImage = ConnectedComponentMap.exec(label,con);

		//------------we adjust the labelsMapImage to suitable labels by tightening them
		HashSet<Integer> distinctComponents = new HashSet<Integer>();
		ArrayList<Integer> correspondanceList = new ArrayList<Integer>();
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++){
				int componentLabelValue = labelsMapImage.getPixelXYInt(x,y);
				if(distinctComponents.add(componentLabelValue)){
					correspondanceList.add(componentLabelValue);
				}
			}
		System.out.println("numbers of connexe components : "+distinctComponents.size());
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++){
				for(int i = 0; i<correspondanceList.size(); i++){
					if(labelsMapImage.getPixelXYInt(x,y) == correspondanceList.get(i)){
						labelsMapImage.setPixelXYInt(x,y,i);
					}
				}
			}
		//--------------------------------------------------------------
		//we create and fill an array of components
		Component components[] = new Component[distinctComponents.size()];
		for(int i = 0; i < components.length; i++){
			components[i] = new Component(i,new ArrayList<Pixel>());
		}
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++){
				components[labelsMapImage.getPixelXYInt(x,y)].add(new Pixel(x,y));
			}

		//for each small component, we compute its mean coordinates
		for(int i = 0; i <components.length ; i++){
			if(components[i].size() < sizeCriteria){
				double xMean = 0;
				double yMean = 0;
				for(int j = 0; j < components[i].size(); j++){
					xMean+=components[i].getPixel(j).x;
					yMean+=components[i].getPixel(j).y;
				}
				components[i].xMean = xMean/components[i].size();
				components[i].yMean = yMean/components[i].size();
			}
		}

		//we look for the most suitable superPixel to fuse our small components
		for(int i = 0; i < components.length; i++){//for each component
			if(components[i].size() < sizeCriteria){//if the component is too small
				for(int j = 0; j < finalSuperpixelsCore.size(); j++){
					double xDiff = components[i].xMean-finalSuperpixelsCore.get(j)[0];
					double yDiff = components[i].yMean-finalSuperpixelsCore.get(j)[1];
					double d = Math.sqrt(xDiff*xDiff+yDiff*yDiff);
					if( d < components[i].minimalDistanceToSuperpixelCore){
						components[i].minimalDistanceToSuperpixelCore = d;
						components[i].bestXSuperpixel = finalSuperpixelsCore.get(j)[0];
						components[i].bestYSuperpixel = finalSuperpixelsCore.get(j)[1];
					}
				}
			}
		}

		//for each small component we now update the labels
		for(int i = 0; i < components.length; i++){
			if(components[i].size() < sizeCriteria){
				for(int j = 0; j < components[i].size(); j++){
					int currentX = components[i].getPixel(j).x;
					int currentY = components[i].getPixel(j).y;
					int superPixelLabel = label.getPixelXYInt(components[i].bestXSuperpixel, components[i].bestYSuperpixel);
					label.setPixelXYInt(currentX,currentY,superPixelLabel);
				}
			}
		}
		return label;
	}

	/**
	 * @param label  the IntegerImage containing the labels of the image where to fuse superpixels
	 * @param lab  the Image where to fuse superpixels
	 * @param finalSuperpixelsCore the array with coordinates of the superpixels centers
	 * @param sampleSize the size of the small square around the superpixel cores where to compute a mean used for fusions
	 * @param labDifference the maximum difference in l, a, and b to consider a fusion between two superpixels
	 * @return label the IntegerImage with fused superpixels
	 */
	public IntegerImage fuse(IntegerImage label, Image lab, ArrayList<int[]> finalSuperpixelsCore, int sampleSize, double labDifference){
		System.out.println("we now (try) fuse the similar superpixels");
		Pixel[] superpixels = new Pixel[finalSuperpixelsCore.size()];
		int step = (int) Math.round(Math.sqrt(((double)label.size())/numberOfSuperpixels));
		int treshold = 2*step;
		int sampleMidSide = (int) Math.sqrt(sampleSize)/2;
		
		//we fill the array of superpixels with a mean of a small square of pixels around the core
		for(int i = 0; i < superpixels.length; i++){
			int midX = finalSuperpixelsCore.get(i)[0];
			int midY = finalSuperpixelsCore.get(i)[1];
			double l=0;
			double a=0;
			double b=0;			
			for(int y=midY-sampleMidSide;y<=midY+sampleMidSide;y++){
				for(int x=midX-sampleMidSide;x<=midX+sampleMidSide;x++){
					l+=lab.getPixelXYBDouble(x,y,0);
					a+=lab.getPixelXYBDouble(x,y,1);
					b+=lab.getPixelXYBDouble(x,y,2);
				}
			}
			l = l/sampleSize;
			a = a/sampleSize;
			b = b/sampleSize;
			superpixels[i] = new Pixel(l,a,b,midX,midY);
			superpixels[i].label = label.getPixelXYInt(midX,midY);
			superpixels[i].oldLabel = label.getPixelXYInt(midX,midY);
		}

		//we look for superpixels to fuse
		for(int i = 0; i < superpixels.length; i++){
			for(int j = 0; j < superpixels.length; j++){
				if(superpixels[i].label != superpixels[j].label){
					double xDiff = superpixels[i].x-superpixels[j].x;
					double yDiff = superpixels[i].y-superpixels[j].y;
					double d = Math.sqrt(xDiff*xDiff+yDiff*yDiff);
					if( d < treshold){
						double lDiff = Math.abs(superpixels[i].l-superpixels[j].l);
						double aDiff = Math.abs(superpixels[i].a-superpixels[j].a);
						double bDiff = Math.abs(superpixels[i].b-superpixels[j].b);
						if(lDiff < labDifference && aDiff < labDifference && bDiff < labDifference ){
							superpixels[i].label = superpixels[j].label;
						}
					}
				}
			}
		}
		//we update the image with the fused labels
		for(int y=0;y<inputImage.ydim;y++){
			for(int x=0;x<inputImage.xdim;x++){
				for(int i = 0; i < superpixels.length; i++){
					if(label.getPixelXYInt(x,y) == superpixels[i].oldLabel){
						label.setPixelXYInt(x,y,superpixels[i].label);
					}
				}
			}
		}

		return label;

	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @return  SLIC superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels)
	{
		return (IntegerImage) new SLIC().process(inputImage, numberOfSuperpixels);
	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @param m compactness parameter
	 * @return  SLIC superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, double m,boolean fuseSuperpixels)
	{
		return (IntegerImage) new SLIC().process(inputImage, numberOfSuperpixels,m,fuseSuperpixels);
	}

	private class Cluster
	{
		public double l;
		public double a;
		public double b;
		public int x;
		public int y;

		public Cluster(double l, double a, double b, int x, int y)
		{
			this.l=l;
			this.a=a;
			this.b=b;
			this.x=x;
			this.y=y;
		}
	}
	private class Pixel{
		public int x;
		public int y;
		public double l;
		public double a;
		public double b;
		public int label;
		public int oldLabel;

		public Pixel(int x, int y){
			this.x = x;
			this.y = y;
		}

		public Pixel(double l, double a, double b, int x, int y){
			this.l = l;
			this.a = a;
			this.b = b;
			this.x = x;
			this.y = y;
		}

		public void display(){
			System.out.println("("+this.x+";"+this.y+")");
		}
	}

	private class Component{
		public int label;
		public ArrayList<Pixel> pixels;
		public double xMean;
		public double yMean;
		public double minimalDistanceToSuperpixelCore;
		public int bestXSuperpixel;
		public int bestYSuperpixel;


		public Component(int label, ArrayList<Pixel> pixels){
			this.label = label;
			this.pixels = pixels;
			this.minimalDistanceToSuperpixelCore = Double.MAX_VALUE;

		}

		public void add(Pixel p){
			pixels.add(p);
		}
		public Pixel getPixel(int i){
			return this.pixels.get(i);
		}

		public int size(){
			return this.pixels.size();
		}
	}


}
