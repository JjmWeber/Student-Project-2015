package main.alphaTree.descriptor;

public abstract class AlphaTreeNodeDescriptor {
	
	public AlphaTreeNodeDescriptor()
	{
		
	}
	public abstract void addPixel(int[] values);
	public abstract void mergeWith(AlphaTreeNodeDescriptor descriptor);
	public abstract double getValue();
	public abstract String getDescriptorName();
	public abstract AlphaTreeNodeDescriptor clone();
	
	

}
