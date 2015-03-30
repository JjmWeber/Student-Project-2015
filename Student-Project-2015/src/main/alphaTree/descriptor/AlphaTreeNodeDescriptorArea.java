package main.alphaTree.descriptor;

import fr.unistra.pelican.util.PointVideo;

public class AlphaTreeNodeDescriptorArea extends AlphaTreeNodeFilterDescriptor {
	
	private static double min=1;
	private static double max=Double.NEGATIVE_INFINITY;
	private int area;
	
	public AlphaTreeNodeDescriptorArea()
	{
		area=0;
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		area++;
		if(area>max)
			max=area;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		area+=((AlphaTreeNodeDescriptorArea)descriptor).area;
		if(area>max)
			max=area;
	}

	@Override
	public boolean check(double minValue, double maxValue) {
		return minValue<=area&&maxValue>=area;
	}

	@Override
	public double getValue() {
		return area;
	}

	@Override
	public String getDescriptorName() {
		return "Area";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorArea clone = new AlphaTreeNodeDescriptorArea();
		clone.area=area;
		return clone;
	}

	@Override
	public int getType() {
		return AlphaTreeNodeDescriptor.TYPE_INT;
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
