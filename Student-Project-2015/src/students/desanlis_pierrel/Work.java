package students.desanlis_pierrel;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.AlphaTreeViewer;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.gui.FileChooserToolBox;
import students.desanlis_pierrel.descriptor.*
;

public class Work {

	public static void main(String[] args) {
		ByteImage lena = (ByteImage)ImageLoader.exec(FileChooserToolBox.openOpenFileChooser(null).getAbsolutePath());
		ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptors = new ArrayList<Class<? extends AlphaTreeNodeDescriptor>> ();
		//descriptors.add(AlphaTreeNodeDescriptorOmega.class);
		descriptors.add(AlphaTreeNodeDescriptorArea.class);		
		descriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		long t = System.currentTimeMillis();
		AlphaTree result = AlphaTreeBuilder.exec(lena,descriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		AlphaTreeViewer.exec(result);
	}
}
