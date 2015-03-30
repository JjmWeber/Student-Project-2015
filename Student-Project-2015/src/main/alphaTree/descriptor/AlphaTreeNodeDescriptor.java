package main.alphaTree.descriptor;

import fr.unistra.pelican.util.PointVideo;

public abstract class AlphaTreeNodeDescriptor {
	
	public AlphaTreeNodeDescriptor()
	{
		
	}
	public abstract void addPixel(int[] values, PointVideo coord);
	public abstract void mergeWith(AlphaTreeNodeDescriptor descriptor);
	public abstract double getValue();
	public abstract String getDescriptorName();
	public abstract AlphaTreeNodeDescriptor clone();
	
	

}
