package main.alphaTree.descriptor;

import fr.unistra.pelican.util.PointVideo;

public class AlphaTreeNodeDescriptorArea extends AlphaTreeNodeFilterDescriptor {
	
	private int area;
	
	public AlphaTreeNodeDescriptorArea()
	{
		area=0;
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		area++;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		area+=((AlphaTreeNodeDescriptorArea)descriptor).area;
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

}
