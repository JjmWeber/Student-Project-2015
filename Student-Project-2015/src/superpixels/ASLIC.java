package superpixels;


import java.util.ArrayList;
import java.util.Arrays;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.RGBToXYZ;
import fr.unistra.pelican.algorithms.conversion.XYZToLAB;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Implementation of ASLIC superpixels (SLIC without parameter)
 * 
 * Radhakrishna Achanta, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine Süsstrunk,
 *  ASLIC Superpixels Compared to State-of-the-art Superpixel Methods, 
 * IEEE Transactions on Pattern Analysis and Machine Intelligence, vol. 34, num. 11, p. 2274 - 2282, May 2012.
 * 
 * @author Jonathan Weber
 */

public class ASLIC extends AbstractSLIC {

	public Image inputImage;
	public int numberOfSuperpixels;
	public boolean fuseSuperpixels;

	public IntegerImage superpixels;

	public ASLIC()
	{
		super();
		super.inputs="inputImage,numberOfSuperpixels,fuseSuperpixels";
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
				clusters.add(new Cluster(lab.getPixelXYBDouble(x, y, 0),lab.getPixelXYBDouble(x, y, 1),lab.getPixelXYBDouble(x, y, 2),x,y,20,10));
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
						double d = Math.sqrt((dc/c.mc)*(dc/c.mc)+((ds/c.ms)*(ds/c.ms)));
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

			//now that we have our new centers, we compute the maximal color distance mc and spatial distance ms for each center
			for(int y=0;y<yDim;y++)
				for(int x=0;x<xDim;x++)
				{
					int labelValue = label.getPixelXYInt(x,y);
					if(labelValue!=-1)
					{
						double l=lab.getPixelXYBDouble(x,y,0);
						double a=lab.getPixelXYBDouble(x,y,1);
						double b=lab.getPixelXYBDouble(x,y,2);
						Cluster c = clusters.get(labelValue);
						double newDs = Math.sqrt((x-c.x)*(x-c.x)+(y-c.y)*(y-c.y));
						double newDc = Math.sqrt((l-c.l)*(l-c.l)+(a-c.a)*(a-c.a)+(b-c.b)*(b-c.b));
						if( newDc > c.mc){
							c.mc = newDc;
						}
						if( newDs > c.ms){
							c.ms = newDs;
						}
					}
				}

			System.out.println("Loop "+loop+" done ! Residual error : "+error);
		} while (error!=0);

		label = enforceConnectivity(label,finalSuperpixelsCore);
		if(fuseSuperpixels)
		label = fuse(label,lab,finalSuperpixelsCore,9,5,step);
		//Segmentation result
		
		superpixels = label;

	}


	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @return  ASLIC superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels)
	{
		return (IntegerImage) new ASLIC().process(inputImage, numberOfSuperpixels);
	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @param m compactness parameter
	 * @return  ASLIC superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, boolean fuseSuperpixels)
	{
		return (IntegerImage) new ASLIC().process(inputImage, numberOfSuperpixels,fuseSuperpixels);
	}

	private class Cluster
	{
		public double l;
		public double a;
		public double b;
		public int x;
		public int y;
		public double mc;
		public double ms;

		public Cluster(double l, double a, double b, int x, int y, int mc, int ms)
		{
			this.l=l;
			this.a=a;
			this.b=b;
			this.x=x;
			this.y=y;
			this.mc=mc;
			this.ms=ms;
		}
	}

}
