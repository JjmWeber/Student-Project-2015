package main.alphaTree.descriptor;

public class AlphaTreeNodeDescriptorArea extends AlphaTreeNodeDescriptor {
	
	private int area;
	
	public AlphaTreeNodeDescriptorArea()
	{
		area=0;
	}

	@Override
	public void addPixel(int[] values) {
		area++;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		area+=((AlphaTreeNodeDescriptorArea)descriptor).area;
	}

	@Override
	public boolean check(double value) {
		return value>=area;
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
