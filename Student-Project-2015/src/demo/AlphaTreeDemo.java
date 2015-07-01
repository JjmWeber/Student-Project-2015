package demo;

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

public class AlphaTreeDemo {
	
	public static void main(String[] args) {
		//Test NdG
		//Image lena = ImageLoader.exec("/home/weber/Documents/git/pelican/samples/lennaGray256.png"); 
		//Test Color
		//ByteImage test = (ByteImage)ImageLoader.exec("/home/weber/Documents/git/pelican/samples/lenna512.png");
		ByteImage test = (ByteImage)ImageLoader.exec("C:/Users/Thomas/pelican/samples/foot.png");
		
		ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptors = new ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> ();
		cutDescriptors.add(AlphaTreeNodeDescriptorOmega.class);
		ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors = new ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> ();
		filterDescriptors.add(AlphaTreeNodeDescriptorArea.class);
		filterDescriptors.add(AlphaTreeNodeDescriptorHeightWidthRatio.class);
		
		long t = System.currentTimeMillis();
		AlphaTree result = AlphaTreeBuilder.exec(test,cutDescriptors, filterDescriptors);
		t=System.currentTimeMillis()-t;
		System.out.println("Alpha-tree creation time : "+t+"ms");
		AlphaTreeViewer.exec(result);
	}

}
