package students.desanlis_pierrel.descriptor;

import java.util.LinkedList;

import main.alphaTree.descriptor.AlphaTreeNodeDescriptorHeightWidthRatio;
import fr.unistra.pelican.util.PointVideo;

/*
 * Ajout d'une méthode static au descripteur AlphaTreeNodeDescriptorHeightWidth 
 * pour permettre son utilisation dans l'arbre de décision
 */
public class AlphaTreeNodeDescriptorHeightWidthRatio2 extends AlphaTreeNodeDescriptorHeightWidthRatio{
	public static double calcValueList(LinkedList<PointVideo> listPts){
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		for (PointVideo p : listPts){
			if (p.x < minX ) minX = p.x;
			if (p.x > maxX ) maxX = p.x;
			if (p.y < minY ) minY = p.y;
			if (p.y > maxY ) maxY = p.y;
		}
		
		return ((double) (maxX-minX+1)/ (double)(maxY-minY+1));
	}
}
