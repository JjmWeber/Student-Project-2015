package superpixels;

import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.RGBToXYZ;
import fr.unistra.pelican.algorithms.conversion.XYZToLAB;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaOpening;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.Point;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Implementation of Watershed superpixels
 * 
 * 
 * V. Machairas, E. Decenci�re, T. Walter
 * Waterpixels : superpixels based on the watershed transformation
 * 978-1-4799-5751-4/14/$31.00 �2014 IEEE
 * 
 * @author Jonathan Weber, Thomas Delesalle
 */

public class CopyOfWaterPixelWithVolume_JW extends Algorithm {

	public Image inputImage;
	public int numberOfSuperpixels;
	public IntegerImage outputImage;
	public double margin;
	public double k;
	public int preprocessStep = 0;



	public CopyOfWaterPixelWithVolume_JW()
	{
		super();
		super.inputs="inputImage,numberOfSuperpixels";
		super.options="margin,k,preprocessStep";
		super.outputs = "outputImage";

	}

	@Override
	public void launch() throws AlgorithmException {
		int xDim = inputImage.xdim;
		int yDim = inputImage.ydim;

		Image inputSmoothed=inputImage.copyImage(false);
		
		int step = (int) Math.sqrt(xDim*yDim/numberOfSuperpixels);
		
		//do we want the slow preprocess step ?
				if(preprocessStep == 1){
					System.out.println("size of area closing/opening : "+((step*step)/16));
					for(int b=0;b<3;b++)
					{
						Image temp = GrayAreaOpening.exec(inputImage.getImage4D(b, Image.B),(step*step)/16);
						temp = GrayAreaClosing.exec(temp,(step*step)/16);
						inputSmoothed.setImage4D(temp, b, Image.B);
					}
					Viewer2D.exec(inputSmoothed, "input smoothed by closing and opening");
				}
		
		/*		
		// Convert smoothed image to Lab color space
		inputSmoothed = XYZToLAB.exec(RGBToXYZ.exec(inputSmoothed));
						
		*/
				
		//TODO : Perform gradient on Lab space		
		// Perform gradient				
		Image gradient =  MultispectralEuclideanGradient.exec(inputSmoothed, FlatStructuringElement2D.createSquareFlatStructuringElement(3));
		Viewer2D.exec(gradient, "gradient picture");


		// Compute and display useful values
		int cellSide = (int) (step*margin);
		System.out.println("step = " +step);
		System.out.println("margin = " +margin);
		System.out.println("k = " +k);
		System.out.println("number of pixels in one cell = "+(cellSide*cellSide));


		// Introducing spatial regularization on gradient
		ArrayList<Point> cores = new ArrayList<Point>();
		for(int y=step/2;y<yDim;y+=step){
			for(int x=step/2;x<xDim;x+=step){
				cores.add(new Point(x,y));
				int yMin=(int) Math.max(0, y-0.5*step);
				int yMax=(int) Math.min(yDim-1, y+0.5*step);
				int xMin=(int) Math.max(0, x-0.5*step);
				int xMax=(int) Math.min(xDim-1, x+0.5*step);
				for(int locY = yMin; locY < yMax; locY++)
					for(int locX = xMin; locX < xMax; locX++)
					{
						int dx=Math.abs(x-locX);
						int dy=Math.abs(y-locY);
						int d;
						if(dx>dy)
							d=dx;
						else
							d=dy;
						gradient.setPixelXYByte(locX,locY,(int)Math.min(255,gradient.getPixelXYByte(locX,locY)+1+(k*2*d/step)));						
					}
			}
		}
		System.out.println("Effective number of superpixels : "+cores.size());
				
		Viewer2D.exec(gradient,"My Gradient !!!");
		
		//Determining marker based on minima of maximum volume extinction region
		//Not sure it's the most efficient way to implement volume extinction but it works !
		
		IntegerImage ws = Watershed.exec(gradient);
		int nbRegion = ws.maximumInt();
		int[] volume = new int[nbRegion+1];
		Arrays.fill(volume, 0);
		for(int y=step/2;y<yDim;y+=step){
			for(int x=step/2;x<xDim;x+=step){
				int yMin=(int) Math.max(0, y-0.5*step*margin);
				int yMax=(int) Math.min(yDim-1, y+0.5*step*margin);
				int xMin=(int) Math.max(0, x-0.5*step*margin);
				int xMax=(int) Math.min(xDim-1, x+0.5*step*margin);
				ArrayList<Integer> presentRegions = new ArrayList<Integer>();
				for(int locY = yMin; locY < yMax; locY++)
					for(int locX = xMin; locX < xMax; locX++)
					{
						int regionLabel = ws.getPixelXYInt(locX, locY);
						volume[regionLabel]+=255-gradient.getPixelXYByte(locX,locY);
						if(!presentRegions.contains(regionLabel))
							presentRegions.add(regionLabel);
					}
				//If there is only one region, minima is set to the center of the cell
				if(presentRegions.size()==1)
				{
					gradient.setPixelXYByte(x, y, 0);
				}
				else
				{
					int maxVolume = 0;
					int regionOfMaxVolume=-1;
					for(Integer regionLabel : presentRegions)
					{
						if(volume[regionLabel]>maxVolume)
						{
							maxVolume = volume[regionLabel];
							regionOfMaxVolume = regionLabel;
						}
					}
					int minGrad=256;
					int minGradX=-1,minGradY=-1;
					for(int locY = yMin; locY < yMax; locY++)
						for(int locX = xMin; locX < xMax; locX++)
						{
							if(ws.getPixelXYInt(locX, locY)==regionOfMaxVolume)
							{
								if(gradient.getPixelXYByte(locX, locY)<minGrad)
								{
									minGrad = gradient.getPixelXYByte(locX, locY);
									minGradX=locX;
									minGradY=locY;
								}
							}
						}
					gradient.setPixelXYByte(minGradX, minGradY, 0);
				}
				for(Integer regionLabel : presentRegions)
				{
					volume[regionLabel]=0;
				}
			}
		}
		outputImage=(IntegerImage) MarkerBasedWatershed.exec(gradient);
		
		Viewer2D.exec(DrawFrontiersOnImage.exec(inputImage, FrontiersFromSegmentation.exec(outputImage)),"Waterpixels_JW with volume : margin = "+margin+" , k = "+k+" preprocess step : "+preprocessStep);
		

	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @return  Waterpixel superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels)
	{
		return (IntegerImage) new CopyOfWaterPixelWithVolume_JW().process(inputImage, numberOfSuperpixels);
	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @param margin margin parameter
	 * @param k spatially regularization parameter
	 * @return  Waterpixel superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, double margin, double k)
	{
		return (IntegerImage) new CopyOfWaterPixelWithVolume_JW().process(inputImage, numberOfSuperpixels,margin, k);
	}

	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, double margin, double k, int preprocessingStep)
	{
		return (IntegerImage) new CopyOfWaterPixelWithVolume_JW().process(inputImage, numberOfSuperpixels,margin, k, preprocessingStep);
	}




}
