package superPixels;


import fr.unistra.pelican.Image;
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
		
		System.out.println("WaterPixel test is starting...");
		Image input = ImageLoader.exec("C:/Users/Thomas/git/pelican/samples/macaws.png");
		
		int numberOfSuperpixels = 200;//number of superpixels we want (approximately)
		double margin = 0.8;// the margin to avoid having seeds too close from each other
		//since we work directly with the spatially regularized gradient the margin is quite useless  =|
		double k = 0.08;// the bigger is k, the more we enhance spatially regularization
		int preprocessingStep = 0; //set this value to 1 if you want to add a morphologic preprocessing step
		
		Image output = WaterPixel.exec(input,numberOfSuperpixels,margin,k,preprocessingStep);
		Viewer2D.exec(output, "watershed input, k = "+k);
		
		Image finalResult = MarkerBasedWatershed.exec(output);
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(finalResult)),"Waterpixels : "+numberOfSuperpixels+" , margin = "+margin+" , k = "+k+" preprocess step : "+preprocessingStep);

		System.out.println("WaterPixel test finished");

	}

}
