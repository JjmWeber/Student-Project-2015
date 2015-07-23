package superpixels;

import java.util.ArrayList;
import java.util.Arrays;

import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
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

public class WaterPixel2 extends Algorithm {

	public Image inputImage;
	public int numberOfSuperpixels;
	public IntegerImage outputImage;
	public double margin;
	public double k;
	public int preprocessStep = 0;



	public WaterPixel2()
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

		// Perform gradient on RGB image
		Image gradient =  MultispectralEuclideanGradient.exec(inputImage, FlatStructuringElement2D.createSquareFlatStructuringElement(3));
		Viewer2D.exec(gradient, "gradient picture");


		// Compute and display useful values
		int step = (int) Math.sqrt(((double)gradient.size())/numberOfSuperpixels);
		int cellSide = (int) (step*margin);
		System.out.println("step = " +step);
		System.out.println("margin = " +margin);
		System.out.println("k = " +k);
		System.out.println("number of pixels in one cell = "+(cellSide*cellSide));


		//do we want the slow preprocess step ?
		if(preprocessStep == 1){
			System.out.println("size of area closing/opening : "+((step*step)/16));
			gradient = GrayAreaOpening.exec(gradient,(step*step)/16);
			gradient = GrayAreaClosing.exec(gradient,(step*step)/16);
			Viewer2D.exec(gradient, "gradient picture after closing and opening");
		}


		// Initialization of the markers in the middle of the cells
		ArrayList<Pixel> cores = new ArrayList<Pixel>();
		int currentLine = 0;
		int currentColumn = 0;
		int superPixelCounter = 0;
		for(int y=step/2;y<yDim;y+=step){
			for(int x=step/2;x<xDim;x+=step){
				superPixelCounter++;
				cores.add(new Pixel(x,y,currentLine,currentColumn));
				currentColumn++;
			}
			currentLine++;
			currentColumn = 0;
		}
		System.out.println("Effective number of superpixels : "+superPixelCounter);
		//around each core
		ArrayList<Image> puzzle = new ArrayList<Image>();
		int coreCounter = -1;
		for(Pixel c : cores)
		{
			coreCounter++;
			//System.out.println("core position = "+"("+c.x+";"+c.y+")");
			int bestX=0;
			int bestY=0;
			int yMin=(int) Math.max(0, c.y-0.5*step);
			int yMax=(int) Math.min(yDim-1, c.y+0.5*step);
			int xMin=(int) Math.max(0, c.x-0.5*step);
			int xMax=(int) Math.min(xDim-1, c.x+0.5*step);

			//we create a small image square
			IntegerImage square = new IntegerImage(Math.abs(xMin-xMax)+1,Math.abs(yMin-yMax)+1, 1, 1, 1);
			puzzle.add(square);
			for(int y = yMin; y < yMax; y++)
				for(int x = xMin; x < xMax; x++)
				{//for each pixel of the current cell
					// We add the spatial regularization
					double regularization = (2*Math.sqrt((x-c.x)*(x-c.x)+(y-c.y)*(y-c.y))*k/step);
					double normalGradient = (double) gradient.getPixelXYByte(x, y)/256;
					double spatialGradient = regularization + normalGradient;

					//we must be sure that a pixel close to white will not turn black because of being > 1
					if(spatialGradient > 1){
						spatialGradient = 1;
					}

					//we update the gradient image with the spatial parameter
					gradient.setPixelXYDouble(x,y,spatialGradient);

					//we fill the small image square
					puzzle.get(coreCounter).setPixelXYDouble(x-(step*c.column-1),y-(step*c.line-1),gradient.getPixelXYBDouble(x, y, 0));



				}
		}

		//We prepare our future outputImage
		IntegerImage watershedInput = new IntegerImage(gradient);

		/*	//watershedInput is slightly whiter than gradient (+1)
		for(int y = 0; y < watershedInput.ydim; y++){
			for(int x = 0; x < watershedInput.xdim; x++){
				double valueWithout0 = (double) (watershedInput.getPixelXYByte(x,y)+1)/256;
				if(valueWithout0 > 1){
					valueWithout0 = 1;
				}
				watershedInput.setPixelXYDouble(x, y,valueWithout0);
			}
		}*/
		
		
		WatershedsForVolume goutte = new WatershedsForVolume(puzzle);
		goutte.findVolume();


		outputImage = watershedInput;
	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @return  Waterpixel superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels)
	{
		return (IntegerImage) new WaterPixel2().process(inputImage, numberOfSuperpixels);
	}

	/**
	 * @param inputImage  image to compute
	 * @param numberOfSuperpixels desired number of superpixels
	 * @param m compactness parameter
	 * @return  Waterpixel superpixels image
	 */
	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, double margin, double k)
	{
		return (IntegerImage) new WaterPixel2().process(inputImage, numberOfSuperpixels,margin, k);
	}

	public static IntegerImage exec(Image inputImage, int numberOfSuperpixels, double margin, double k, int preprocessingStep)
	{
		return (IntegerImage) new WaterPixel2().process(inputImage, numberOfSuperpixels,margin, k, preprocessingStep);
	}


	private class Pixel
	{
		public double l;
		public double a;
		public double b;
		public int x;
		public int y;
		public int line;
		public int column;

		public Pixel(double l, double a, double b, int x, int y)
		{
			this.l=l;
			this.a=a;
			this.b=b;
			this.x=x;
			this.y=y;
		}

		public Pixel(int x, int y)
		{
			this.x=x;
			this.y=y;
		}
		public Pixel(int x, int y, int line, int column){
			this.x=x;
			this.y=y;
			this.line = line;
			this.column = column;
		}
	}

}
