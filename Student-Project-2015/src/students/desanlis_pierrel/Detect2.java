package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.AlphaTreeViewer;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorArea2;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorContourRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorDecisionTree;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorHeightWidthRatio2;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorPerceptronWeka;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSurfaceRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSyntax;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.KMeans;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.FileChooserToolBox;

public class Detect2 {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		String pathCorpus = "samples\\corpusWeka.crp";
		ByteImage img = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		int[][] result = new int[img.xdim][img.ydim];
		
		System.out.println("Debut calcul kmeans");
		img = LabelsToColorByMeanValue.exec(KMeans.exec(img, 12),img); //Kmeans !!!
		System.out.println("Fin kmeans");
		AlphaTreeNodeDescriptorPerceptronWeka.init(pathCorpus, 10, 10);
//		AlphaTreeNodeDescriptorSyntax.init(32, 32, 10, 5, 0.25,"samples\\symbols");
		
		//lena = LabelsToColorByMeanValue.exec(KMeans.exec(lena, 3),lena);
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
		
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio2.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorArea2.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorContourRatio.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorPerceptron.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorPerceptronWeka.class);
		AlphaTreeNodeDescriptorDecisionTree.init(filterDescriptors, pathCorpus);
		
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors2 = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors2.add(AlphaTreeNodeDescriptorDecisionTree.class);
		long t = System.currentTimeMillis();
		AlphaTree resultAT = AlphaTreeBuilder.exec(img,cutDescriptors, filterDescriptors2);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		//
//		AlphaTreeViewer.exec(resultAT);
		
		//Detection auto en faisant varier les parametres de coupe
		double maxFilteringValues[] = new double[1]; maxFilteringValues[0] = 1;
		double minFilteringValues[] = new double[1];
		for (double i = 0.0 ; i <= 1.0 ; i+=0.1){
			double[] cutDescriptorValues = new double[1];
			cutDescriptorValues[0] = 1; //Avec les kmeans pas besoins d'aller plus loins
			minFilteringValues[0] = i;
			IntegerImage res = resultAT.getSegmentationFromCutAndFiltering(1, cutDescriptorValues, minFilteringValues, maxFilteringValues);
			for (int x = 0 ; x < res.xdim ; x++)
				for (int y = 0 ; y < res.ydim ; y++){
					if (res.getPixelXYInt(x, y) != -1)
						result[x][y]++;												
				}
		}
		int max = 0;
		for (int x = 0 ; x < img.xdim ; x++)
			for (int y = 0 ; y < img.ydim ; y++)
				if (result[x][y] > max)
					max = result[x][y];
		max++;
		//Resultat sous formes d'image, plus c'est blanc plus c'est censé être du texte 
		ByteImage res = new ByteImage(img.xdim, img.ydim,1,1,1);
		for (int x = 0 ; x < img.xdim ; x++)
			for (int y = 0 ; y < img.ydim ; y++)
				res.setPixelXYByte(x, y, (result[x][y]*255)/max);
		
		Viewer2D.exec(res);
		//On ne fait apparaitre que les points les plus elevé
//		int total = 0;
//		for (int x = 0 ; x < res.xdim ; x++)
//			for (int y = 0 ; y < res.ydim ; y++)			
//				total += res.getPixelXYByte(x, y);
//		//int moyen = total/(res.xdim*res.ydim);
//		for (int x = 0 ; x < res.xdim ; x++ )
//			for (int y = 0 ; y < res.ydim ; y++ ){
//				if (res.getPixelXYByte(x, y) > 0.5*max)
//					res.setPixelXYByte(x, y, 0);
//				else
//					res.setPixelXYByte(x, y, 255);
//			}
//		
//		Viewer2D.exec(res);
		
		System.out.println("Fin");
	}
}
