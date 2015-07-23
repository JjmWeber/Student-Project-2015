package superpixels;


import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaClosing;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class WaterPixelTest {

	public static void main(String[] args) {
		double start = System.currentTimeMillis();

		
		System.out.println("WaterPixel test is starting...");
		Image input = ImageLoader.exec("C:/Users/Thomas/git/pelican/samples/macaws.png");
		
		int numberOfSuperpixels = 40;//number of superpixels we want (approximately)
		System.out.println("number of wanted superPixels : "+numberOfSuperpixels);
		double margin = 0.8;// the margin to avoid having seeds too close from each other
		double k = 0.1;// the bigger is k, the more we enhance spatially regularization
		int preprocessingStep = 0; //set this value to 1 if you want to add a morphologic preprocessing step
		
		Image output = WaterPixel.exec(input,numberOfSuperpixels,margin,k,preprocessingStep);
		Viewer2D.exec(output, "watershed input, k = "+k);
		
		Image finalResult = MarkerBasedWatershed.exec(output);
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(finalResult)),"Waterpixels : margin = "+margin+" , k = "+k+" preprocess step : "+preprocessingStep);
		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");
		System.out.println("WaterPixel test finished");


		
	}

}
