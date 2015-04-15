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
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.gui.FileChooserToolBox;
import students.desanlis_pierrel.descriptor.*
;

public class Work {

	public static void main(String[] args) {
		ByteImage lena = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
		//cutDescriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors.add(AlphaTreeNodeDescriptorArea.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorSR.class);
		
		
		long t = System.currentTimeMillis();
		AlphaTree result = AlphaTreeBuilder.exec(lena,cutDescriptors, filterDescriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		AlphaTreeViewer.exec(result);
	}
}
