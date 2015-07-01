package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
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
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.KMeans;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.FileChooserToolBox;

/*
 * Algorithme de detection de texte utilisant un arbre alpha pour la segmentation et un arbre de decision
 * pour determiner quelle zone contient probablement du texte. 
 */
public class Detect2 extends Algorithm{
	public ByteImage inputImage;
	public ByteImage outputImage; //ByteImage représentant la probabilité que choique point appartienne a du texte
	public String corpus; //Chemin vers un corpus d'apprentissage créer avec la class AlphaTreeViewWekaTrainer
	public int nbMeans = 6;
	
	public Detect2(){
		super.inputs = "inputImage,corpus";
		super.options = "nbMeans";
		super.outputs = "outputImage";
	}
	
	@Override
	public void launch() throws AlgorithmException {
		int[][] result = new int[inputImage.xdim][inputImage.ydim];
		
		inputImage = LabelsToColorByMeanValue.exec(KMeans.exec(inputImage, nbMeans),inputImage);

		AlphaTreeNodeDescriptorPerceptronWeka.init(corpus, 10, 10);
//		AlphaTreeNodeDescriptorSyntax.init(32, 32, 10, 5, 0.25,corpus); //Descripteur interessant mais trop lent pour l'instant
		
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
		
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> decisionTreeDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		//Liste des descripteurs utilisé par l'arbre de decision
		decisionTreeDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
		decisionTreeDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio2.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class); //Trop lent pour l'instant
		decisionTreeDescriptors.add(AlphaTreeNodeDescriptorArea2.class);
		decisionTreeDescriptors.add(AlphaTreeNodeDescriptorContourRatio.class);
		decisionTreeDescriptors.add(AlphaTreeNodeDescriptorPerceptronWeka.class);
		AlphaTreeNodeDescriptorDecisionTree.init(decisionTreeDescriptors, corpus);
		
		//Filtre utilisé pour l'arbre alphe
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors2 = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors2.add(AlphaTreeNodeDescriptorDecisionTree.class);
		long t = System.currentTimeMillis();
		AlphaTree resultAT = AlphaTreeBuilder.exec(inputImage,cutDescriptors, filterDescriptors2);
		long t1=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t1+"ms");
		
		//Detection auto en faisant varier les parametres de coupe
		double maxFilteringValues[] = new double[1]; maxFilteringValues[0] = 1;
		double minFilteringValues[] = new double[1];
		for (double i = 0.0 ; i <= 1.0 ; i+=0.1){
			double[] cutDescriptorValues = new double[1];
			cutDescriptorValues[0] = 1; //Il serait intéressant de tester en faisant varier les valeurs d'omega
			minFilteringValues[0] = i;
			IntegerImage res = resultAT.getSegmentationFromCutAndFiltering(1, cutDescriptorValues, minFilteringValues, maxFilteringValues);
			for (int x = 0 ; x < res.xdim ; x++)
				for (int y = 0 ; y < res.ydim ; y++){
					if (res.getPixelXYInt(x, y) != -1)
						result[x][y]++;												
				}
		}
		int max = 0;
		for (int x = 0 ; x < inputImage.xdim ; x++)
			for (int y = 0 ; y < inputImage.ydim ; y++)
				if (result[x][y] > max)
					max = result[x][y];
		max++;
		
		//Resultat sous formes d'image, plus le point est blanc plus la probabilité que ce soit du texte augmente
		outputImage = new ByteImage(inputImage.xdim, inputImage.ydim,1,1,1);
		for (int x = 0 ; x < inputImage.xdim ; x++)
			for (int y = 0 ; y < inputImage.ydim ; y++)
				outputImage.setPixelXYByte(x, y, (result[x][y]*255)/max);
		long t2 = System.currentTimeMillis() - t;
		System.out.println("Total creation time : " + t2 + "ms");		
	}
	
	/**
	 * 
	 * @param inputImage
	 * @param nbMeans
	 * @return
	 */
	public static ByteImage exec (ByteImage inputImage, String corpus, int nbMeans){
		return (ByteImage) new Detect2().process(inputImage,corpus, nbMeans);
	}
	
	/**
	 * 
	 * @param inputImage
	 * @return
	 */
	public static ByteImage exec (ByteImage inputImage, String corpus){
		return (ByteImage) new Detect2().process(inputImage, corpus);
	}
}
