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
		//gradient = GrayAreaOpening.exec(gradient,1);
		//gradient = GrayAreaClosing.exec(gradient,1);
		//Viewer2D.exec(gradient, "Gradient opened and closed");

		Image watershededImage = Watershed.exec(gradient);
		Viewer2D.exec(watershededImage, "notre segmentation visualis�e");
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(watershededImage)),"My cute segmentation watershededImage");
		Viewer2D.exec(LabelsToRandomColors.exec(gradient));


		//we run through the image and put the listOfLabels in the ArrayList listOfLabels
		ArrayList<Integer> listOfLabels = new ArrayList<Integer>();
		for(int y = 0; y < watershededImage.ydim; y++){
			for(int x = 0; x < watershededImage.xdim; x++){
				//System.out.println("surprise("+x+","+y+") :"+gradient.getPixelXYInt(x, y));
				listOfLabels.add(watershededImage.getPixelXYInt(x, y));
			}
		}

		//rawlabels keeps the duplicates
		ArrayList<Integer> rawLabels = new ArrayList(listOfLabels);


		/*
		Collections.sort(listOfLabels);
		for(Integer i : listOfLabels){
			System.out.println(i);
		}
		 */

		//we use a HashSet to delete duplicates in listOfLabels
		Set<Integer> temporaryHashSet = new HashSet<>();
		temporaryHashSet.addAll(listOfLabels);
		System.out.println("Nombre de label distincts : "+temporaryHashSet.size());
		listOfLabels.clear();
		listOfLabels.addAll(temporaryHashSet);


		int labelsFrequency[][] = new int[listOfLabels.size()][2];

		//we count the frequency of each label
		for(int i = 0; i < listOfLabels.size(); i++){
			labelsFrequency[i][0] = listOfLabels.get(i);
			labelsFrequency[i][1] = Collections.frequency(rawLabels, listOfLabels.get(i));
		}

		int ultimateCounter = 0;
		for(int i= 0; i < listOfLabels.size(); i++){
			System.out.println("label : "+labelsFrequency[i][0]+", occures "+labelsFrequency[i][1]+" times");
			ultimateCounter = ultimateCounter + labelsFrequency[i][1];
		}

		System.out.println("ultimateCounter = "+ultimateCounter);

/*
		int compteurFind = 0;
		int c = 0;

		for(int y = 0; y < watershededImage.ydim; y++){
			for(int x = 0; x < watershededImage.xdim; x++){
				c++;
				if(watershededImage.getPixelXYInt(0, 0) == rawLabels.get(x+y*watershededImage.ydim)){
					System.out.println(compteurFind +"pixel de bon label trouv� ! "+x+","+y);
					compteurFind++;
				}
			}
		}
		System.out.println("nombre total d'it�rations, c++ :"+c);
*/


		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");
		System.out.println("test finished");
	}

}