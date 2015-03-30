package main.alphaTree.descriptor;

import fr.unistra.pelican.util.PointVideo;

public abstract class AlphaTreeNodeDescriptor {
	
	public static final int TYPE_INT = 0;
	public static final int TYPE_DOUBLE = 1;
	
	public AlphaTreeNodeDescriptor()
	{
		
	}
	public abstract void addPixel(int[] values, PointVideo coord);
	public abstract void mergeWith(AlphaTreeNodeDescriptor descriptor);
	public abstract double getValue();
	public abstract String getDescriptorName();
	public abstract AlphaTreeNodeDescriptor clone();
	public abstract int getType();
	public abstract double getMin();
	public abstract double getMax();

}
