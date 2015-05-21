package students.delesalle_kemberg;

import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;

public class AlphaTreeNodeDescriptorZoneRectangle extends AlphaTreeNodeFilterDescriptor{

	private int minX = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;
	
	private int centerX = minX;
	private int centerY = minY;
	
	
	@Override
	public boolean check(double minValue, double maxValues) {
		//TODO Trouver quoi mettre ...
		return true;
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		if(minX>coord.x) minX=coord.x;
		if(maxX<coord.x) maxX=coord.x;
		if(minY>coord.y) minY=coord.y;
		if(maxY<coord.y) maxY=coord.y;
		
		centerX = (minX - maxX)/2;
		centerY = (minY - maxY)/2;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorZoneRectangle desc = (AlphaTreeNodeDescriptorZoneRectangle) descriptor;
		minX = minX<desc.minX?minX:desc.minX;
		maxX = maxX>desc.maxX?maxX:desc.maxX;
		minY = minY<desc.minY?minY:desc.minY;
		maxY = maxY>desc.maxY?maxY:desc.maxY;
		
		centerX = (minX - maxX)/2;
		centerY = (minY - maxY)/2;
		
	}

	@Override
	public double getValue() {
		return centerY;
	}

	@Override
	public String getDescriptorName() {
		return "Zone Rectangle";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorZoneRectangle clone = new AlphaTreeNodeDescriptorZoneRectangle();
		clone.minX=minX;
		clone.maxX=maxX;
		clone.minY=minY;
		clone.maxY=maxY;
		clone.centerX=centerX;
		clone.centerY=centerY;
		return clone;
	}

	@Override
	public int getType() {
		//TODO Trouver quoi mettre ...
		return AlphaTreeNodeDescriptor.TYPE_DOUBLE;
	}

	@Override
	public double getMin() {
		//TODO Trouver quoi mettre ...
		return minY;
	}

	@Override
	public double getMax() {
		//TODO Trouver quoi mettre ...
		return maxY;
	}

	
}
