package superpixels;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.segmentation.superpixel.SLIC;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class SLICTest {

	public static void main(String[] args) {
		double start = System.currentTimeMillis();
		System.out.println("SLIC test starting...");

		Image input = ImageLoader.exec("C:/Users/Thomas/git/pelican/samples/curious.png");
		int numberOfSuperPixels = 500;
		int m = 20;
		
		Image output = SLIC.exec(input, numberOfSuperPixels,m);
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(output)),"SLIC : "+numberOfSuperPixels+" superpixels, m = "+m);
	//	Viewer2D.exec(current);
		
		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");

	}
}
