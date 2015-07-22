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

public class WatershedsForVolume {

	ArrayList<Image> puzzle;

	public WatershedsForVolume(ArrayList<Image> puzzle){

		this.puzzle = puzzle;

	}

	public void findVolume() {


		for(int pieceCounter = 0; pieceCounter <puzzle.size();pieceCounter++){
			System.out.println("petit morceau n°"+pieceCounter);

			int xDim = puzzle.get(pieceCounter).xdim;
			int yDim = puzzle.get(pieceCounter).ydim;

			//pre-treatment
			//puzzle.get(pieceCounter) = GrayAreaOpening.exec(puzzle.get(pieceCounter),1);
			//puzzle.get(pieceCounter) = GrayAreaClosing.exec(puzzle.get(pieceCounter),1);
			//Viewer2D.exec(puzzle.get(pieceCounter), "Gradient opened and closed");

			Image watershededImage = Watershed.exec(puzzle.get(pieceCounter));

			//we run through the image and put every label in the ArrayList rawLabels
			ArrayList<Integer> rawLabels = new ArrayList<Integer>();
			for(int y = 0; y < watershededImage.ydim; y++){
				for(int x = 0; x < watershededImage.xdim; x++){
					rawLabels.add(watershededImage.getPixelXYInt(x, y));

				}
			}
			System.out.println("rawLabels.size() = "+rawLabels.size());

			//we will  delete duplicates from listOfLabels
			ArrayList<Integer> listOfLabels = new ArrayList(rawLabels);
			System.out.println("listOfLabels.size() = "+listOfLabels.size());

			//we use a HashSet to delete duplicates in listOfLabels (is it a good idea ?)
			Set<Integer> temporaryHashSet = new HashSet<>();
			temporaryHashSet.addAll(listOfLabels);
			System.out.println("Nombre de labels distincts : "+temporaryHashSet.size());
			listOfLabels.clear();
			listOfLabels.addAll(temporaryHashSet);

			//we count the frequency of each label using rawLabels and listOfLabels, results are kept in labelsFrequency
			//first field contains the label
			//second field contains the frequency
			System.out.println("listOfLabels.size() = "+listOfLabels.size());
			Integer labelsFrequency[][] = new Integer[listOfLabels.size()][2];
			for(int i = 0; i < listOfLabels.size(); i++){
				labelsFrequency[i][0] = (Integer) listOfLabels.get(i);
				labelsFrequency[i][1] = (Integer) Collections.frequency(rawLabels, listOfLabels.get(i));
			}


			
			//now we sort labelsFrequency by frequency
			Arrays.sort(labelsFrequency, new Comparator<Integer[]>() {
				@Override
				public int compare(Integer[] s1, Integer[] s2) {
					return s2[1]-s1[1];
				}
			});

			int volume = 0;
			for(int i = 0; i < rawLabels.size();i++){
				if(rawLabels.get(i).equals(labelsFrequency[1][0])){
					int x = i%xDim;
					int y = (i-x)/xDim;
					volume = volume +Math.abs(255-puzzle.get(pieceCounter).getPixelXYByte(x,y));
				}
			}
			System.out.println("volume n°"+pieceCounter+" = "+volume);

		}
	}





}
