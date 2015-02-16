package demo;

import java.util.ArrayList;

import alphaTree.AlphaTreeBuilder;
import alphaTree.AlphaTreeViewer;
import alphaTree.data.AlphaTree;
import alphaTree.descriptor.AlphaTreeNodeDescriptor;
import alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;

public class AlphaTreeDemo {
	
	public static void main(String[] args) {
		//Test NdG
		//Image lena = ImageLoader.exec("/home/weber/Documents/git/pelican/samples/lennaGray256.png"); 
		//Test Color
		ByteImage lena = (ByteImage)ImageLoader.exec("/home/weber/Documents/git/pelican/samples/lenna512.png");
		ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptors = new ArrayList<Class<? extends AlphaTreeNodeDescriptor>> ();
		descriptors.add(AlphaTreeNodeDescriptorOmega.class);
		descriptors.add(AlphaTreeNodeDescriptorArea.class);		
		long t = System.currentTimeMillis();
		AlphaTree result = AlphaTreeBuilder.exec(lena,descriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		AlphaTreeViewer.exec(result);
	}

}
