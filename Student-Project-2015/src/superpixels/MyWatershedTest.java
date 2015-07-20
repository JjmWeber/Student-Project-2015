package superpixels;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.io.ImageSave;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaOpening;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.algorithms.segmentation.MarkerBasedWatershed;
import fr.unistra.pelican.algorithms.segmentation.Watershed;
import fr.unistra.pelican.algorithms.segmentation.labels.DrawFrontiersOnImage;
import fr.unistra.pelican.algorithms.segmentation.labels.FrontiersFromSegmentation;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToRandomColors;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

public class MyWatershedTest {

	public static void main(String[] args) {
		double start = System.currentTimeMillis();


		System.out.println("test is starting...");
		Image input = ImageLoader.exec("C:/Users/Thomas/git/pelican/samples/macaws.png");
		Image gradient =  MultispectralEuclideanGradient.exec(input, FlatStructuringElement2D.createSquareFlatStructuringElement(3));
		Viewer2D.exec(gradient, "Gradient");
		
		//pre-treatment
		gradient = GrayAreaOpening.exec(gradient,70);
		gradient = GrayAreaClosing.exec(gradient,70);
		Viewer2D.exec(gradient, "Gradient opened and closed");
		
		Image watershededImage = Watershed.exec(gradient);
		Viewer2D.exec(watershededImage, "notre segmentation visualisée");
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(watershededImage)),"My cute segmentation watershededImage");
		Viewer2D.exec(LabelsToRandomColors.exec(gradient));

		ArrayList<Integer> labels = new ArrayList<Integer>();
		for(int y = 0; y < watershededImage.ydim; y++){
			for(int x = 0; x < watershededImage.xdim; x++){
				//System.out.println("surprise("+x+","+y+") :"+gradient.getPixelXYInt(x, y));
				labels.add(watershededImage.getPixelXYInt(x, y));
			}
		}

		/*
		Collections.sort(labels);
		for(Integer i : labels){
			System.out.println(i);
		}
		 */

		Set<Integer> listOfLabels = new HashSet<>();
		listOfLabels.addAll(labels);
		System.out.println("Nombre de labels : "+listOfLabels.size());
		ArrayList<Integer> salve = new ArrayList(labels);
		labels.clear();
		labels.addAll(listOfLabels);

		int compteurFind = 0;
		int c = 0;
		
		for(int y = 0; y < watershededImage.ydim; y++){
			for(int x = 0; x < watershededImage.xdim; x++){
				c++;
				if(watershededImage.getPixelXYInt(0, 0) == salve.get(x+y*watershededImage.ydim)){
					System.out.println(compteurFind +"pixel de bon label trouvé ! "+x+","+y);
					compteurFind++;
				}
			}
		}
		System.out.println("nombre total d'itérations, c++ :"+c);



		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");
		System.out.println("test finished");
	}

}
