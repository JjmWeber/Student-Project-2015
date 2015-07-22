package superpixels;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
		Viewer2D.exec(DrawFrontiersOnImage.exec(input, FrontiersFromSegmentation.exec(watershededImage)),"My cute segmentation watershededImage");
		Viewer2D.exec(LabelsToRandomColors.exec(gradient));


		//we run through the image and put every label in the ArrayList rawLabels
		ArrayList<Integer> rawLabels = new ArrayList<Integer>();
		for(int y = 0; y < watershededImage.ydim; y++){
			for(int x = 0; x < watershededImage.xdim; x++){
				rawLabels.add(watershededImage.getPixelXYInt(x, y));
			}
		}

		//we will  delete duplicates from listOfLabels
		ArrayList<Integer> listOfLabels = new ArrayList(rawLabels);




		//we use a HashSet to delete duplicates in listOfLabels (is it a good idea ?)
		Set<Integer> temporaryHashSet = new HashSet<>();
		temporaryHashSet.addAll(listOfLabels);
		System.out.println("Nombre de label distincts : "+temporaryHashSet.size());
		listOfLabels.clear();
		listOfLabels.addAll(temporaryHashSet);



		//we count the frequency of each label using rawLabels and listOfLabels, results are kept in labelsFrequency
		//first field contains the label
		//second field contains the frequency
		Integer labelsFrequency[][] = new Integer[listOfLabels.size()][2];
		for(int i = 0; i < listOfLabels.size(); i++){
			labelsFrequency[i][0] = (Integer) listOfLabels.get(i);
			labelsFrequency[i][1] = (Integer) Collections.frequency(rawLabels, listOfLabels.get(i));
		}

		//now we sort labelsFrequency by frequency
		Arrays.sort(labelsFrequency, new Comparator<Integer[]>() {
			@Override
			public int compare(Integer[] s1, Integer[] s2) {
				Integer num1 = s1[1];
				Integer num2 = s2[1];
				return num1-num2;
			}
		});
		
		
		for(int i= 0; i < listOfLabels.size(); i++){
			System.out.println("label : "+labelsFrequency[i][0]+", occures "+labelsFrequency[i][1]+" times");
		}
		
		double end = System.currentTimeMillis();
		System.out.println("Execution time = "+(end - start)+"ms");
		System.out.println("test finished");
	}





}
