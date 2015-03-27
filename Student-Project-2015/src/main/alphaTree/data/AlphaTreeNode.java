package main.alphaTree.data;

import java.util.ArrayList;

import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;

/**
 * 
 * @author Jonathan Weber
 *
 */
public class AlphaTreeNode {
	
	int id;
	int alpha;
	AlphaTreeNode parent=null;
	ArrayList<AlphaTreeNode> children=null;
	AlphaTreeNodeCutDescriptor[] cutDescriptors=null;
	AlphaTreeNodeFilterDescriptor[] filterDescriptors=null;
	
	public AlphaTreeNodeCutDescriptor[] getCutDescriptors() {
		return cutDescriptors;
	}
	
	public AlphaTreeNodeFilterDescriptor[] getFilterDescriptors() {
		return filterDescriptors;
	}

	public AlphaTreeNode(int id, int alpha, ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptorList, ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptorList)
	{
		this.id=id;
		this.alpha=alpha;
		children=new ArrayList<AlphaTreeNode>();
		cutDescriptors=new AlphaTreeNodeCutDescriptor[cutDescriptorList.size()];
		try {
			for(int i=0;i<cutDescriptors.length;i++)
			{
				cutDescriptors[i]=(AlphaTreeNodeCutDescriptor) cutDescriptorList.get(i).newInstance();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		filterDescriptors=new AlphaTreeNodeFilterDescriptor[filterDescriptorList.size()];
		try {
			for(int i=0;i<filterDescriptors.length;i++)
			{
				filterDescriptors[i]=(AlphaTreeNodeFilterDescriptor) filterDescriptorList.get(i).newInstance();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public AlphaTreeNode(int id, int alpha, AlphaTreeNode child1, AlphaTreeNode child2)
	{
		this.id=id;
		this.alpha=alpha;
		children=new ArrayList<AlphaTreeNode>();
		children.add(child1);
		children.add(child2);
		child1.parent=this;
		child2.parent=this;
		cutDescriptors= new AlphaTreeNodeCutDescriptor[child1.cutDescriptors.length];
		for(int i=0;i<cutDescriptors.length;i++)
		{
			cutDescriptors[i]=(AlphaTreeNodeCutDescriptor) child1.cutDescriptors[i].clone();
		}
		for(int i=0;i<cutDescriptors.length;i++)
		{
			cutDescriptors[i].mergeWith(child2.cutDescriptors[i]);
		}
		filterDescriptors= new AlphaTreeNodeFilterDescriptor[child1.filterDescriptors.length];
		for(int i=0;i<filterDescriptors.length;i++)
		{
			filterDescriptors[i]=(AlphaTreeNodeFilterDescriptor) child1.filterDescriptors[i].clone();
		}
		for(int i=0;i<filterDescriptors.length;i++)
		{
			filterDescriptors[i].mergeWith(child2.filterDescriptors[i]);
		}
	}
	
	public void mergeWith(AlphaTreeNode node)
	{
		for(AlphaTreeNode child : node.children)
		{
			child.parent=this;
		}
		for(int i=0;i<cutDescriptors.length;i++)
		{
			cutDescriptors[i].mergeWith(node.cutDescriptors[i]);
		}
		for(int i=0;i<filterDescriptors.length;i++)
		{
			filterDescriptors[i].mergeWith(node.filterDescriptors[i]);
		}
		children.addAll(node.children);
	}
	
	public void addChild(AlphaTreeNode node)
	{
		children.add(node);
		node.parent=this;
		for(int i=0;i<cutDescriptors.length;i++)
		{
			cutDescriptors[i].mergeWith(node.cutDescriptors[i]);
		}
		for(int i=0;i<filterDescriptors.length;i++)
		{
			filterDescriptors[i].mergeWith(node.filterDescriptors[i]);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id=id;
	}
	
	public int getAlpha() {
		return alpha;
	}
	
	public boolean isLeaf()
	{
		return children.size()==0;
	}
	
	public boolean isRoot()
	{
		return parent==null;
	}
	
	public boolean isParentOf(AlphaTreeNode node)
	{
		if(this==node)
			return true;
		for(AlphaTreeNode child : children)
		{
			if(child.isParentOf(node))
				return true;
		}
		return false;
	}
	
	public AlphaTreeNode getRoot()
	{
		if(isRoot())
		{
			return this;
		}
		else
		{
			return parent.getRoot();
		}
	}
	
	public ArrayList<AlphaTreeNode> getLeaves()
	{
		ArrayList<AlphaTreeNode> leaves=new ArrayList<AlphaTreeNode>();
		if(isLeaf())
		{
			leaves.add(this);
		}
		else
		{
			for(AlphaTreeNode child : children)
				leaves.addAll(child.getLeaves());
		}
		return leaves;
	}
	//TODO : This one seems to be parralelizable ...
	public ArrayList<AlphaTreeNode> getCutNodes(int cutAlpha, double[] cutDescriptorValues)
	{
		ArrayList<AlphaTreeNode> cutNodes=new ArrayList<AlphaTreeNode>();
		boolean isOK=cutAlpha>=this.alpha;
		int i=0;
		while(isOK&&i<cutDescriptorValues.length)
		{
			isOK=((AlphaTreeNodeCutDescriptor)cutDescriptors[i]).check(cutDescriptorValues[i]);
			i++;
		}
		//isLeaf() is here to deal with special case when even the leaf didn't check the condition (for exemple area = 1)
		if(isOK||isLeaf())
		{
			cutNodes.add(this);
		}
		else
		{
			for(AlphaTreeNode child : children)
				cutNodes.addAll(child.getCutNodes(cutAlpha, cutDescriptorValues));
		}
		return cutNodes;
	}
	
	public void addPixel(int[] values)
	{
		for(int i=0;i<cutDescriptors.length;i++)
		{
			cutDescriptors[i].addPixel(values);
		}
		for(int i=0;i<filterDescriptors.length;i++)
		{
			filterDescriptors[i].addPixel(values);
		}
	}

}
