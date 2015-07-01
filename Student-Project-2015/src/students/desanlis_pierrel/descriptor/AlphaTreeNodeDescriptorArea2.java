package students.desanlis_pierrel.descriptor;

import java.util.LinkedList;

import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import fr.unistra.pelican.util.PointVideo;

/*
 * Ajout d'une m�thode static au descripteur AlphaTreeNodeDescriptorArea 
 * pour permettre son utilisation dans l'arbre de d�cision
 */
public class AlphaTreeNodeDescriptorArea2 extends AlphaTreeNodeDescriptorArea{
	public static double calcValueList(LinkedList<PointVideo> listPts){
		return (double)listPts.size();
	}
}
