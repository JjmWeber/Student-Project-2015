package superpixels;


import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class WaterPixelWithVolumeLauncher {

	public static void main(String[] args) {
		double start = System.currentTimeMillis();

		
		System.out.println("WaterPixelWithVolume segmentation is starting...");
		Image input = ImageLoader.exec("C:/Users/Thomas/git/pelican/samples/curious.png");
		
		int numberOfSuperpixels = 150;//number of wanted superpixels (approximately)
		System.out.println("number of wanted superPixels : "+numberOfSuperpixels);
		double margin = 0.1;// the margin to avoid having seeds too close from each other
		double k = 0.3;// the bigger is k, the more we enhance spatially regularization
		int preprocessingStep = 0; //set this value to 1 if you want to add a morphologic preprocessing step
		
		Image markedImage = WaterPixelWithVolume.exec(input,numberOfSuperpixels,margin,k,preprocessingStep);
		Viewer2D.exec(markedImage, "watershed input, k = "+k);
		
		Image finalResult = MarkerBasedWatershed.exec(markedImage);
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(finalResult)),"Waterpixels with volume : margin = "+margin+" , k = "+k+" preprocess step : "+preprocessingStep);
		
		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");
		System.out.println("WaterPixel2 test finished");


		
	}

}
