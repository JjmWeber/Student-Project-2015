package students.desanlis_pierrel.descriptor;

import java.util.LinkedList;

import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;

public class AlphaTreeNodeDescriptorArea2 extends AlphaTreeNodeDescriptorArea{
	public static double calcValueList(LinkedList<PointVideo> listPts){
		return (double)listPts.size();
	}
}
