package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.AlphaTreeViewer;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorHeightWidthRatio;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import main.alphaTree.util.LabelsToColorByMeanValue;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorContourRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorOTeinte;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSurfaceRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSyntax;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.KMeans;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.FileChooserToolBox;

public class Detect {

	public static void main(String[] args) {
		ByteImage img = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		int[][] result = new int[img.xdim][img.ydim];
		//		Viewer2D.exec(resultImg);

		//		img = LabelsToColorByMeanValue.exec(KMeans.exec(img, 12),img); //Kmeans

		AlphaTreeNodeDescriptorSyntax.init(32, 32, 10, 5, 0.25,"C:\\Users\\Florian\\git\\Student-Project-2015\\Student-Project-2015\\samples\\symbols");
		//		AlphaTreeNodeDescriptorPerceptron.init(16, 16, "C:\\Users\\Florian\\git\\Student-Project-2015\\Student-Project-2015\\samples\\perceptron");

		// a essayer avant -> reduire le nombre de couleur intelligement avec des kmeans (commencer par trouver l'algo)
		// après avoir essayer => moyen   : si le nombre de centre mobile est faible et a ce moment les descripteur alpha/omega serve a rien (trop d'écart)
		//									si le nombre de centre mobile est elevé ont peu utiliser alpha/omega mais les calcules sont beaucoup trop long
		//					      par contre en général les symboles ressortent vraiment bien donc avec de bon filterDescriptor on peut avoir de bon resultat
		/**		Ressort les contours
		 * 		Viewer2D.exec(ContrastStretch.exec(Sobel.exec(src)), "Sobel");

		Viewer2D.exec(GrayGradient.exec(src, FlatStructuringElement2D
			.createSquareFlatStructuringElement(3)), "Morpho");
		 */

		//lena = LabelsToColorByMeanValue.exec(KMeans.exec(lena, 3),lena);
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);

		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();		
		filterDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorArea.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorContourRatio.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorPerceptron.class);

		double[] minFilteringValues = new double[filterDescriptors.size()];
		double[] maxFilteringValues = new double[filterDescriptors.size()];		

		//remplissage du tableau (plus facile pour enlever/rajouter/regler des descripteur pdt les test
		for (int i = 0 ; i < filterDescriptors.size() ; i++){
			if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorSurfaceRatio.class.getName()) == 0){
				minFilteringValues[i] = 0.0;
				maxFilteringValues[i] = 0.5;
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorHeightWidthRatio.class.getName()) == 0){
				minFilteringValues[i] = 0.1;
				maxFilteringValues[i] = 0.8;
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
			else
				System.out.println("ERREUR: descripteur inconnu");
		}


		long t = System.currentTimeMillis();
		AlphaTree resultAT = AlphaTreeBuilder.exec(img,cutDescriptors, filterDescriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		//				AlphaTreeViewer.exec(result);

		//Pour l'instant on passe sur toute les valeurs de alpha et omega
		//et sur tout les pixel, prévoir une pause café a chaque test
		for(int alpha = 0 ; alpha < resultAT.getMaxAlpha() ; alpha+=10){
			for (int i = 0 ; i < resultAT.getMaxAlpha() ; i+=10 ){
				if (i==0)
					System.out.println("etape " + (i + alpha*resultAT.getMaxAlpha()) + "/" + resultAT.getMaxAlpha()*resultAT.getMaxAlpha());
				double[] cutDescriptorValues = new double[1];
				cutDescriptorValues[0] = i;				
				IntegerImage res = resultAT.getSegmentationFromCutAndFiltering(alpha, cutDescriptorValues, minFilteringValues, maxFilteringValues);
				//				Viewer2D.exec(res);
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
		Viewer2D.exec(res);
		AlphaTreeViewer.exec(resultAT);

	}
}
