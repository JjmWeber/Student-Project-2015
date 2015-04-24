package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.AlphaTreeViewer;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorHeightWidthRatio;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.edge.Sobel;
import fr.unistra.pelican.algorithms.histogram.ContrastStretch;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.KMeans;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.gui.FileChooserToolBox;
import students.desanlis_pierrel.descriptor.*;


public class Work {

	public static void main(String[] args) {
		ByteImage lena = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		//AlphaTreeNodeDescriptorSyntax.apprentissage(FileChooserToolBox.openOpenDirectoryChooser(null).getAbsolutePath());
//		AlphaTreeNodeDescriptorSyntax.init(100, 100, 5, 50, FileChooserToolBox.openOpenDirectoryChooser(null).getAbsolutePath());
		AlphaTreeNodeDescriptorPerceptron.init(16, 16, "C:\\Users\\Florian\\git\\Student-Project-2015\\Student-Project-2015\\samples\\perceptron");
// a essayer avant -> reduire le nombre de couleur intelligement avec des kmeans (commencer par trouver l'algo)
// après avoir essayer => moyen   : si le nombre de centre mobile est faible et a ce moment les descripteur alpha/omega serve a rien (trop d'écart)
//									si le nombre de centre mobile est elevé ont peu utiliser alpha/omega mais les calcules sont beaucoup trop long
//					      par contre en général les symboles ressortent vraiment bien donc avec de bon filterDescriptor on peut avoir de bon resultat
/**		Ressort les contours
 * 		Viewer2D.exec(ContrastStretch.exec(Sobel.exec(src)), "Sobel");

		Viewer2D.exec(GrayGradient.exec(src, FlatStructuringElement2D
			.createSquareFlatStructuringElement(3)), "Morpho");
 */
		//lena = LabelsToColorByMeanValue.exec(KMeans.exec(lena, 32),lena);
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
		//cutDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		//filterDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorSR.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class);
		
		long t = System.currentTimeMillis();
		AlphaTree result = AlphaTreeBuilder.exec(lena,cutDescriptors, filterDescriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		AlphaTreeViewer.exec(result);
	}
}
