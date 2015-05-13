package main.alphaTree.descriptor;

import fr.unistra.pelican.util.PointVideo;

public class AlphaTreeNodeDescriptorHeightWidthRatio extends
		AlphaTreeNodeFilterDescriptor {
	
	private static double min=Double.POSITIVE_INFINITY;
	private static double max=Double.NEGATIVE_INFINITY;
	
	private int minX = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;

	@Override
	public boolean check(double minValue, double maxValues) {
		double ratio = getValue();
		return (minValue<=ratio && maxValues>= ratio);
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		if(minX>coord.x)
		{
			minX=coord.x;
		}
		if(maxX<coord.x)
		{
			maxX=coord.x;
		}
		if(minY>coord.y)
		{
			minY=coord.y;
		}
		if(maxY<coord.y)
		{
			maxY=coord.y;
		}
		double ratio=getValue();
		if(ratio<min)
			min=ratio;
		if(ratio>max)
			max=ratio;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorHeightWidthRatio desc = (AlphaTreeNodeDescriptorHeightWidthRatio) descriptor;
		minX = minX<desc.minX?minX:desc.minX;
		maxX = maxX>desc.maxX?maxX:desc.maxX;
		minY = minY<desc.minY?minY:desc.minY;
		maxY = maxY>desc.maxY?maxY:desc.maxY;
		double ratio=getValue();
		if(ratio<min)
			min=ratio;
		if(ratio>max)
			max=ratio;
	}

	@Override
	public double getValue() {
		return ((double) (maxX-minX+1)/ (double)(maxY-minY+1));
	}

	@Override
	public String getDescriptorName() {
		return "Height/Width Ratio";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorHeightWidthRatio clone = new AlphaTreeNodeDescriptorHeightWidthRatio();
		clone.minX=minX;
		clone.maxX=maxX;
		clone.minY=minY;
		clone.maxY=maxY;
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
