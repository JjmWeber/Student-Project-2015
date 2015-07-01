package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorHeightWidthRatio;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorContourRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorOTeinte;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorPerceptronWeka;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSurfaceRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSyntax;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.FileChooserToolBox;

/*
 * Premiere methode de detection consistant a parcourir l'arbre et chercher les points qui restent present le plus longtemps
 * Une image résultat est créé, plus le point est blanc plus il est probable que la zone contienne du texte 
 */
public class Detect {
	public static void main(String[] args) {
		ByteImage img = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		int[][] result = new int[img.xdim][img.ydim];

		AlphaTreeNodeDescriptorSyntax.init(32, 32, 10, 5, 0.25,"samples\\symbols");
//		AlphaTreeNodeDescriptorPerceptronWeka.init("samples\\corpusWeka.crp", 10, 10);		

		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);

		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();		
		//Ajout des filtres que l'on veut utiliser
		filterDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorArea.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorContourRatio.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorPerceptronWeka.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorPerceptron.class);

		double[] minFilteringValues = new double[filterDescriptors.size()];
		double[] maxFilteringValues = new double[filterDescriptors.size()];		

		//remplissage des paramètres de coupe
		for (int i = 0 ; i < filterDescriptors.size() ; i++){
			if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorSurfaceRatio.class.getName()) == 0){
				minFilteringValues[i] = 0.0;
				maxFilteringValues[i] = 0.5;
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorHeightWidthRatio.class.getName()) == 0){
				minFilteringValues[i] = 0.1;
				maxFilteringValues[i] = 1.0;
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorSyntax.class.getName()) == 0){
				minFilteringValues[i] = 0.0;
				maxFilteringValues[i] = 12.0;
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorArea.class.getName()) == 0){
				minFilteringValues[i] = 100.0;
				maxFilteringValues[i] = (img.xdim*img.ydim);
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorOTeinte.class.getName()) == 0){
				minFilteringValues[i] = 0.0;
				maxFilteringValues[i] = 250.0;
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorContourRatio.class.getName()) == 0){
				minFilteringValues[i] = 0.0;
				maxFilteringValues[i] = 10.0;
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorPerceptronWeka.class.getName()) == 0){
				minFilteringValues[i] = 0.5;
				maxFilteringValues[i] = 1.0;
			}
			else
				System.out.println("ERREUR: descripteur inconnu");
		}


		long t = System.currentTimeMillis();
		AlphaTree resultAT = AlphaTreeBuilder.exec(img,cutDescriptors, filterDescriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		
		//On parcours l'arbre et on cherche les zones qui restent le plus longtemps présentent
 		for(int alpha = 0 ; alpha < resultAT.getMaxAlpha() ; alpha+=10){
			for (int i = 0 ; i < resultAT.getMaxAlpha() ; i+=10 ){
				if (i==0)
					System.out.println("etape " + (i + alpha*resultAT.getMaxAlpha()) + "/" + resultAT.getMaxAlpha()*resultAT.getMaxAlpha());
				double[] cutDescriptorValues = new double[1];
				cutDescriptorValues[0] = i;				
				IntegerImage res = resultAT.getSegmentationFromCutAndFiltering(alpha, cutDescriptorValues, minFilteringValues, maxFilteringValues);
				for (int x = 0 ; x < res.xdim ; x++)
					for (int y = 0 ; y < res.ydim ; y++){
						if (res.getPixelXYInt(x, y) != -1)
							result[x][y]++;												
					}
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
		
		//Affichage du resultat
		Viewer2D.exec(res);
	}
}
