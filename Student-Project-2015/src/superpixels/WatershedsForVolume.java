package superpixels;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.segmentation.Watershed;


public class WatershedsForVolume {

	ArrayList<Image> puzzle;

	public WatershedsForVolume(ArrayList<Image> puzzle){

		this.puzzle = puzzle;

	}

	public int[][] findSeedsWithVolume() {

		int bestLabels[]= new int[puzzle.size()];

		int[][] seedsCoordinates = new int[puzzle.size()][2];

		for(int pieceCounter = 0; pieceCounter <puzzle.size();pieceCounter++){
			//System.out.println("piece n°"+pieceCounter);
			//Viewer2D.exec(puzzle.get(pieceCounter), "petit morceau n°"+pieceCounter);

			int xDim = puzzle.get(pieceCounter).xdim;

			Image watershededImage = Watershed.exec(puzzle.get(pieceCounter));
			//Viewer2D.exec(DrawFrontiersOnImage.exec(puzzle.get(pieceCounter), FrontiersFromSegmentation.exec(watershededImage)),"petit morceau watersheded n°"+pieceCounter);

			//we run through the image and put every label in the ArrayList rawLabels
			ArrayList<Integer> rawLabels = new ArrayList<Integer>();
			for(int y = 0; y < watershededImage.ydim; y++){
				for(int x = 0; x < watershededImage.xdim; x++){
					rawLabels.add(watershededImage.getPixelXYInt(x, y));

				}
			}
			//System.out.println("rawLabels.size() = "+rawLabels.size());



			//we use a HashSet to delete duplicates in listOfLabels (is it a good idea ?)
			ArrayList<Integer> listOfLabels = new ArrayList(rawLabels);
			Set<Integer> temporaryHashSet = new HashSet<>();
			temporaryHashSet.addAll(listOfLabels);
			//System.out.println("Nombre de labels distincts : "+temporaryHashSet.size());
			listOfLabels.clear();
			listOfLabels.addAll(temporaryHashSet);

			//we count the frequency of each label using rawLabels and listOfLabels, results are kept in labelsWithFrequency
			//first field of second column contains the number of the label 
			//second field of second column contains the frequency
			//System.out.println("listOfLabels.size() = "+listOfLabels.size());
			Integer labelsWithFrequency[][] = new Integer[listOfLabels.size()][2];
			for(int i = 0; i < listOfLabels.size(); i++){
				labelsWithFrequency[i][0] = (Integer) listOfLabels.get(i);
				labelsWithFrequency[i][1] = (Integer) Collections.frequency(rawLabels, listOfLabels.get(i));
			}



			//now we sort labelsWithFrequency by frequency
			Arrays.sort(labelsWithFrequency, new Comparator<Integer[]>() {
				@Override
				public int compare(Integer[] s1, Integer[] s2) {
					return s2[1]-s1[1];
				}
			});

			//	for(int i = 0; i < listOfLabels.size(); i++){
			//		System.out.println(labelsWithFrequency[i][1]);
			//	}



			//limitlabel is here to be sure we will not have an indexOutOfBonds with labelsWithFrequency
			int limitLabel = 0;
			if(listOfLabels.size() <= 10){
				limitLabel = listOfLabels.size();
			}else{
				limitLabel = 10;
			}

			int biggestVolume = 0;

			for(int j = 0; j < limitLabel; j++){//for the most represented labels
				int volume = 0;
				for(int i = 0; i < rawLabels.size();i++){// for each pixel of the small image
					if(rawLabels.get(i).equals(labelsWithFrequency[j][0])){// if the label of the considered pixel is the label we consider now
						int x = i%xDim;
						int y = (i-x)/xDim;
						volume = volume +Math.abs(255-puzzle.get(pieceCounter).getPixelXYByte(x,y));
					}
				}
				if(volume > biggestVolume){
					biggestVolume = volume;
					bestLabels[pieceCounter] = labelsWithFrequency[j][0];
				}
			}


			//we now select our seed for this piece of image
			int bestX = 0;
			int bestY = 0;
			double lowerValue = Double.MAX_VALUE;
			for(int i = 0; i < rawLabels.size();i++){// for each pixel of the small image
				if(rawLabels.get(i).equals(bestLabels[pieceCounter])){// if the label of the considered pixel is the best label
					int x = i%xDim;
					int y = (i-x)/xDim;
					if((puzzle.get(pieceCounter).getPixelXYByte(x, y)) < lowerValue){
						lowerValue = puzzle.get(pieceCounter).getPixelXYByte(x, y);
						bestX = x;
						bestY = y;
					}
				}
			}

			seedsCoordinates[pieceCounter][0] = bestX;
			seedsCoordinates[pieceCounter][1] = bestY;

			//System.out.println("volume n°"+pieceCounter+" = "+biggestVolume);

		}

		//	for(int i = 0; i < puzzle.size();i++){
		//		System.out.println("bestLabels n°"+i+" : "+bestLabels[i]);
		//	}

	/*	for(int i = 0; i < puzzle.size();i++){
			System.out.println("seed n°"+i+" : "+"("+seedsCoordinates[i][0]+";"+seedsCoordinates[i][1]+")");
		}*/


		return seedsCoordinates;
	}





}
