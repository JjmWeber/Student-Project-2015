package main.alphaTree.descriptor;

public abstract class AlphaTreeNodeFilterDescriptor extends AlphaTreeNodeDescriptor {

	public abstract boolean check(double minValue, double maxValues);
}
