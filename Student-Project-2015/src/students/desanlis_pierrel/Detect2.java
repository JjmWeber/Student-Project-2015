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
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorArea2;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorDecisionTree;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorHeightWidthRatio2;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorPerceptronWeka;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSurfaceRatio;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorSyntax;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.KMeans;
import fr.unistra.pelican.algorithms.segmentation.labels.LabelsToColorByMeanValue;
import fr.unistra.pelican.gui.FileChooserToolBox;

public class Detect2 {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException {
		ByteImage img = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		
		System.out.println("Debut calcul kmeans");
		img = LabelsToColorByMeanValue.exec(KMeans.exec(img, 12),img); //Kmeans !!!
		System.out.println("Fin kmeans");
//		AlphaTreeNodeDescriptorPerceptronWeka.init("C:\\corpusWeka.crp", 10, 10);
//		AlphaTreeNodeDescriptorSyntax.init(32, 32, 10, 5, 0.25,"C:\\Users\\Florian\\git\\Student-Project-2015\\Student-Project-2015\\samples\\symbols");
		
		//lena = LabelsToColorByMeanValue.exec(KMeans.exec(lena, 3),lena);
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
		
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors.add(AlphaTreeNodeDescriptorSurfaceRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio2.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorSyntax.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorArea2.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorContourRatio.class);
		//filterDescriptors.add(AlphaTreeNodeDescriptorPerceptron.class);
//		filterDescriptors.add(AlphaTreeNodeDescriptorPerceptronWeka.class);
		AlphaTreeNodeDescriptorDecisionTree.init(filterDescriptors, "C:\\corpusWeka.crp");
		
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors2 = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors2.add(AlphaTreeNodeDescriptorDecisionTree.class);
		long t = System.currentTimeMillis();
		AlphaTree result = AlphaTreeBuilder.exec(img,cutDescriptors, filterDescriptors2);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		AlphaTreeViewer.exec(result);
		
		System.out.println("Fin");
	}
}
