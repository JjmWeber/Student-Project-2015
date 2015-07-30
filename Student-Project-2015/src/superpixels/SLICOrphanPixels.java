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
 * Implementation of SLICOrphanPixels superpixels
 * 
 * Radhakrishna Achanta, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine SÃ¼sstrunk,
 *  SLICOrphanPixels Superpixels Compared to State-of-the-art Superpixel Methods, 
 * IEEE Transactions on Pattern Analysis and Machine Intelligence, vol. 34, num. 11, p. 2274 - 2282, May 2012.
 * 
 * @author Jonathan Weber
 */

public class SLICOrphanPixels extends Algorithm {

	public Image inputImage;
	public int numberOfSuperpixels;

	//Between 1 and 40, low value to respect boundary, high value to respect compactness
	public double m;

	public IntegerImage superpixels;

	public SLICOrphanPixels()
	{
		super();
		super.inputs="inputImage,numberOfSuperpixels";
		super.options="m";
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
		int xDim = inputImage.xdim;
		int yDim = inputImage.ydim;
		int sizeCriteria = ((xDim*yDim/finalSuperpixelsCore.size())/4);
		System.out.println("size criteria = "+sizeCriteria);
		
		//we create an IntegerImage to store the informations about connexe components
		Connectivity3D con = new FlatConnectivity(label, TrivialConnectivity.getFourNeighbourhood());
		IntegerImage labelsMapImage = ConnectedComponentMap.exec(label,con);

		//we adjust the labelsMapImage to suitable labels
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

		//labels store the number of pixels for each connexe component
		int[] labels = new int[distinctComponents.size()];
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++){
				int labelValue = labelsMapImage.getPixelXYInt(x,y);
				labels[labelValue]++;
			}

		//orphanLabels contains only the labels of the small components
		//badLabel[0] contains the label number
		//badLabel[1] contains the x mean
		//badLabel[2] contains the y mean
		//badLabel[3] contains the number of pixels
		ArrayList<int[]> orphanLabels = new ArrayList<int[]>();
		for(int i = 0; i < labels.length; i++){
			if(labels[i] < sizeCriteria){
				int[] badLabel = new int[4];
				badLabel[0] = i;
				badLabel[3] = labels[i];
				orphanLabels.add(badLabel);
			}
		}

		//we grab all the coordinates for each badLabel, and then compute the means
		for(int i = 0; i < orphanLabels.size(); i++){
			for(int y=0;y<yDim;y++)
				for(int x=0;x<xDim;x++){
					if(labelsMapImage.getPixelXYInt(x, y) == orphanLabels.get(i)[0]){
						orphanLabels.get(i)[1] += x;
						orphanLabels.get(i)[2] += y;
					}
				}
		}
		for(int i = 0; i < orphanLabels.size(); i++){
			orphanLabels.get(i)[1] = (int) orphanLabels.get(i)[1]/orphanLabels.get(i)[3];
			orphanLabels.get(i)[2] = (int) orphanLabels.get(i)[2]/orphanLabels.get(i)[3];
		}

		
		//we compute the minimal distance between our small components and the superpixels
		//computationArray[0] : the n° of the badLabel
		//computationArray[1] : the n° of the best superpixel core for fusion
		//computationArray[2] : the distance, we need it only for computation
		double[][] computationArray = new double[orphanLabels.size()][3];
		for (double[] row : computationArray)
			Arrays.fill(row,Double.MAX_VALUE);
		for(int i = 0; i < orphanLabels.size(); i++){
			for(int j = 0; j < finalSuperpixelsCore.size(); j++){
				double xDiff = orphanLabels.get(i)[1]-finalSuperpixelsCore.get(j)[0];
				double yDiff = orphanLabels.get(i)[2]-finalSuperpixelsCore.get(j)[1];
				double d = Math.sqrt(xDiff*xDiff+yDiff*yDiff);
				if( d < computationArray[i][2]){
					computationArray[i][0] = orphanLabels.get(i)[0];
					computationArray[i][1] = j;
					computationArray[i][2] = d;
				}
			}
		}

		//orphanLabelsToSuperPixelLabel is here to have a direct mapping between orphanLabels and their legitimate belonging superpixel
		ArrayList<Integer> orphanLabelsToSuperPixelLabel = new ArrayList<Integer>(labels.length);
		for(int i = 0 ; i < labels.length; i++){
			orphanLabelsToSuperPixelLabel.add(i,0);
		}
		for(int i = 0 ; i < computationArray.length; i++){
			orphanLabelsToSuperPixelLabel.set(orphanLabels.get(i)[0],(int) computationArray[i][1]);
		}
		
		
		//we grab the label of each superpixel core
		for(int i = 0; i <finalSuperpixelsCore.size();i++ ){
			finalSuperpixelsCore.get(i)[2] = label.getPixelXYInt(finalSuperpixelsCore.get(i)[0],finalSuperpixelsCore.get(i)[1]);
		}
		
		
		//here it is, for each pixel of the IntegerImage label, we change the label if needed
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++){
				for(int i = 0; i < orphanLabels.size(); i++){
					if(labelsMapImage.getPixelXYInt(x, y) == orphanLabels.get(i)[0]){//if the current label is a bad label
						label.setPixelXYInt(x, y, orphanLabelsToSuperPixelLabel.get(orphanLabels.get(i)[0]));
					}
				}
			}

		return label;
	}













	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @return  SLICOrphanPixels superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels)
	{
		return (IntegerImage) new SLICOrphanPixels().process(inputImage, numberOfSuperpixels);
	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @param m compactness parameter
	 * @return  SLICOrphanPixels superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, double m)
	{
		return (IntegerImage) new SLICOrphanPixels().process(inputImage, numberOfSuperpixels,m);
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

	private class Component{
		public ArrayList<int[]> listOfCoordinates;
	}

}
