package students.desanlis_pierrel.descriptor;

import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;


/**
 * SR = surface ratio
 * Pourcentage occupé par les pixels du noeud par rapport a un carré de côté égale au plus grand côté du noeud
 * (TODO: trouver un nom de class plus explicite)
 */

public class AlphaTreeNodeDescriptorSR extends AlphaTreeNodeFilterDescriptor {
	private static double min=Double.POSITIVE_INFINITY;
	private static double max=Double.NEGATIVE_INFINITY;
	
	private int minX = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private int nb_pixel = 0;
	
	private double getRatio(){
		int surface;
		if (maxX-minX > maxY-minY){
			surface = (maxX-minX+1)*(maxX-minX+1);
		}
		else
			surface = (maxY-minY+1)*(maxY-minY+1);		

		return (double)(nb_pixel) / surface;
	}
	
	@Override
	public boolean check(double minValue, double maxValues) {
		double ratio = getRatio();
		if (ratio >= minValue && ratio <= maxValues)
			return true;
		return false;
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		if (coord.x < minX)
			minX = coord.x;
		if (coord.x > maxX)
			maxX = coord.x;
		if (coord.y < minY)
			minY = coord.y;
		if (coord.y > maxY)
			maxY = coord.y;
		
		nb_pixel++;
		double ratio = getRatio();
		if (ratio < min)
			min = ratio;
		if (ratio > max)
			max = ratio;
		
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorSR desc = (AlphaTreeNodeDescriptorSR) descriptor;
		if (desc.minX < this.minX) this.minX = desc.minX;
		if (desc.maxX > this.maxX) this.maxX = desc.maxX;
		if (desc.minY < this.minY) this.minY = desc.minY;
		if (desc.maxY > this.maxY) this.maxY = desc.maxY;
		
		this.nb_pixel+= desc.nb_pixel;	
		
		double ratio = getRatio();
		if (ratio < min)
			min = ratio;
		if (ratio > max)
			max = ratio;
	}

	@Override
	public double getValue() {
		return getRatio();
	}

	@Override
	public String getDescriptorName() {		
		return "Surface ratio";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorSR clone = new AlphaTreeNodeDescriptorSR();
		clone.minX = this.minX;
		clone.maxX = this.maxX;
		clone.minY = this.minY;
		clone.maxY = this.maxY;
		clone.nb_pixel = this.nb_pixel;
		
		return clone;
	}

	@Override
	public int getType() {
		return AlphaTreeNodeDescriptor.TYPE_DOUBLE;
	}

	@Override
	public double getMin() {
		return min;
	}

	@Override
	public double getMax() {		
		return max;
	}

}
