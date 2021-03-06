package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.AlphaTreeViewer;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.KMeans;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.FileChooserToolBox;

/*
 * Class de travail pour faires des tests sur les arbres
 */
public class Work {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		ByteImage lena = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		Viewer2D.exec(Detect2.exec(lena, "samples\\corpusWeka.crp"));		
		
//		lena = LabelsToColorByMeanValue.exec(KMeans.exec(lena, 6),lena); //Kmeans !!!
//		System.out.println("Fin kmeans");
//		AlphaTreeNodeDescriptorPerceptronWeka.init("samples\\corpusWeka.crp", 10, 10);
//		AlphaTreeNodeDescriptorSyntax.init(32, 32, 10, 5, 0.25,"samples\\symbols");
		
// a essayer avant -> reduire le nombre de couleur intelligement avec des kmeans (commencer par trouver l'algo)
// apr�s avoir essayer => moyen   : si le nombre de centre mobile est faible et a ce moment les descripteur alpha/omega serve a rien (trop d'�cart)
//									si le nombre de centre mobile est elev� ont peu utiliser alpha/omega mais les calcules sont beaucoup trop long
//					      par contre en g�n�ral les symboles ressortent vraiment bien donc avec de bon filterDescriptor on peut avoir de bon resultat
/**		Ressort les contours
 * 		Viewer2D.exec(ContrastStretch.exec(Sobel.exec(src)), "Sobel");

		Viewer2D.exec(GrayGradient.exec(src, FlatStructuringElement2D
			.createSquareFlatStructuringElement(3)), "Morpho");
 */
		
		//lena = LabelsToColorByMeanValue.exec(KMeans.exec(lena, 3),lena);
//		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
//		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
//		
//		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
//		//filterDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorArea.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorContourRatio.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorPerceptron.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorPerceptronWeka.class);
//		long t = System.currentTimeMillis();
//		AlphaTree result = AlphaTreeBuilder.exec(lena,cutDescriptors, filterDescriptors);
//		t=System.currentTimeMillis()-t;
//		System.out.println("Alpha-tree creation time : "+t+"ms");
//		AlphaTreeViewer.exec(result);
//		AlphaTreeViewWekaTrainer.path = "samples\\corpusWeka.crp";
//		AlphaTreeViewWekaTrainer aTV = new AlphaTreeViewWekaTrainer(result);
		
		System.out.println("Fin");
	}
}
