package superpixels;

import java.util.ArrayList;
import java.util.HashSet;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.morphology.connected.ConnectedComponentMap;
import fr.unistra.pelican.util.connectivityTrees.connectivity.Connectivity3D;
import fr.unistra.pelican.util.connectivityTrees.connectivity.FlatConnectivity;
import fr.unistra.pelican.util.connectivityTrees.connectivity.TrivialConnectivity;

/**
 * Implementation of an abstract class to regroup methods shared by all SLIC classes
 * 
 * 
 * @author Thomas Delesalle
 */

public class AbstractSLIC extends Algorithm {
	
	public Image inputImage;
	public int numberOfSuperpixels;
	public boolean fuseSuperpixels;

	public IntegerImage superpixels;

	public AbstractSLIC()
	{
		super();
		super.inputs="inputImage,numberOfSuperpixels,fuseSuperpixels";
		super.outputs = "superpixels";
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
		int xDim = label.xdim;
		int yDim = label.ydim;
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
			components[i] = new Component(new ArrayList<Pixel>());
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
	public IntegerImage fuse(IntegerImage label, Image lab, ArrayList<int[]> finalSuperpixelsCore, int sampleSize, double labDifference, int step){
		System.out.println("we now (try) fuse the similar superpixels");
		Pixel[] superpixels = new Pixel[finalSuperpixelsCore.size()];
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
		for(int y=0;y<label.ydim;y++){
			for(int x=0;x<label.xdim;x++){
				for(int i = 0; i < superpixels.length; i++){
					if(label.getPixelXYInt(x,y) == superpixels[i].oldLabel){
						label.setPixelXYInt(x,y,superpixels[i].label);
					}
				}
			}
		}

		return label;

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
	}

	private class Component{
		public ArrayList<Pixel> pixels;
		public double xMean;
		public double yMean;
		public double minimalDistanceToSuperpixelCore;
		public int bestXSuperpixel;
		public int bestYSuperpixel;


		public Component(ArrayList<Pixel> pixels){
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

	@Override
	public void launch() throws AlgorithmException {
		// TODO Auto-generated method stub
		
	}
}
