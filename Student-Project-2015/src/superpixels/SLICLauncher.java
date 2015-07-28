package superpixels;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class SLICLauncher {

	public static void main(String[] args) {
		System.out.println("SLIC and SLICO tests starting...");

		//Image input = ImageLoader.exec("/home/weber/Documents/git/pelican/samples/curious.png");
		Image input = ImageLoader.exec("C:/Users/Thomas/git/pelican/samples/curious.png");
		int numberOfSuperPixels = 500;
		int m = 40;




		//SLIC (m parameter to tune)
		double start = System.currentTimeMillis();
		Image outputSLIC = SLIC.exec(input, numberOfSuperPixels,m);
		double end = System.currentTimeMillis();
		double executionTimeSLIC = end - start;
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(outputSLIC)),"SLIC : "+numberOfSuperPixels+" superpixels, m = "+m);


		System.out.println("SLIC terminated, SLICO is starting");

	/*	//SLICO (no parameter to tune)
		start = System.currentTimeMillis();
		Image outputSLICO = SLICO.exec(input, numberOfSuperPixels);
		end = System.currentTimeMillis();
		double executionTimeSLICO = end - start;
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(outputSLICO)),"SLICO : "+numberOfSuperPixels+" superpixels");
*/

	/*	//ASLIC (no parameter to tune)
		start = System.currentTimeMillis();
		Image outputASLIC = ASLIC.exec(input, numberOfSuperPixels);
		end = System.currentTimeMillis();
		double executionTimeASLIC = end - start;
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(outputASLIC)),"ASLIC : "+numberOfSuperPixels+" superpixels");
	*/	
		
		
		
		System.out.println("Execution time for SLIC = "+executionTimeSLIC+"ms");

	//	System.out.println("Execution time for SLICO = "+executionTimeSLICO+"ms");

	//	System.out.println("Execution time for ASLIC = "+executionTimeASLIC+"ms");
		
		System.out.println("SLIC, SLICO and ASLIC tests terminated...");
		 

		//Image outputSLICO = SLICOWithPostProcess.exec(input, numberOfSuperPixels);
		//Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(outputSLICO)),"SLICO with post-process step : "+numberOfSuperPixels+" superpixels");
	}
}
