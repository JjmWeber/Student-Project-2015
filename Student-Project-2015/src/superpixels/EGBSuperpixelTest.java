package superpixels;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaClosing;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class EGBSuperpixelTest {

	public static void main(String[] args) {

		double start = System.currentTimeMillis();
		System.out.println("EGBSuperpixel test starting...");
		Image input = ImageLoader.exec("samples/macaws.png");
		Image image = EGBSuperpixel.exec(input);
		//Viewer2D.exec(image);
		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");

		System.out.println("EGBSuperpixel test finished");

	}

}
