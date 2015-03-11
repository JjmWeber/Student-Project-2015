package students.delesalle_kemberg;

import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.AlphaTreeViewer;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.conversion.RGBAToGray;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.segmentation.ManualThresholding;

public class Work {

	public static void main(String[] args) {
				ByteImage image = (ByteImage)ImageLoader.exec("pelican/samples/foot.png");
				
				//Prétraitements
				ManualThresholding threshold = new ManualThresholding();
				Image image2 = RGBAToGray.exec(image);
				Image BinaryImage = threshold.exec(image2,150);
				ByteImage img = new ByteImage(BinaryImage);
				
				
				ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptors = new ArrayList<Class<? extends AlphaTreeNodeDescriptor>> ();
				descriptors.add(AlphaTreeNodeDescriptorOmega.class);
				descriptors.add(AlphaTreeNodeDescriptorArea.class);		
				long t = System.currentTimeMillis();
				AlphaTree result = AlphaTreeBuilder.exec(img,descriptors);
				t=System.currentTimeMillis()-t;
				System.out.println("Alpha-tree creation time : "+t+"ms");
				AlphaTreeViewer.exec(result);
	}

	}