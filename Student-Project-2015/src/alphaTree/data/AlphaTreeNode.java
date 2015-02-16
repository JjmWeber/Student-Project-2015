package alphaTree.data;

import java.util.ArrayList;

import alphaTree.descriptor.AlphaTreeNodeDescriptor;

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
	AlphaTreeNodeDescriptor[] descriptors=null;
	
	public AlphaTreeNode(int id, int alpha, ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptorList)
	{
		this.id=id;
		this.alpha=alpha;
		children=new ArrayList<AlphaTreeNode>();
		descriptors=new AlphaTreeNodeDescriptor[descriptorList.size()];
		try {
			for(int i=0;i<descriptors.length;i++)
			{
				descriptors[i]=(AlphaTreeNodeDescriptor) descriptorList.get(i).newInstance();
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
		descriptors= new AlphaTreeNodeDescriptor[child1.descriptors.length];
		for(int i=0;i<descriptors.length;i++)
		{
			descriptors[i]=(AlphaTreeNodeDescriptor) child1.descriptors[i].clone();
		}
		for(int i=0;i<descriptors.length;i++)
		{
			descriptors[i].mergeWith(child2.descriptors[i]);
		}
	}
	
	public void mergeWith(AlphaTreeNode node)
	{
		for(AlphaTreeNode child : node.children)
		{
			child.parent=this;
		}
		for(int i=0;i<descriptors.length;i++)
		{
			descriptors[i].mergeWith(node.descriptors[i]);
		}
		children.addAll(node.children);
	}
	
	public void addChild(AlphaTreeNode node)
	{
		children.add(node);
		node.parent=this;
		for(int i=0;i<descriptors.length;i++)
		{
			descriptors[i].mergeWith(node.descriptors[i]);
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
	public ArrayList<AlphaTreeNode> getCutNodes(int cutAlpha, double[] descriptorValues)
	{
		ArrayList<AlphaTreeNode> cutNodes=new ArrayList<AlphaTreeNode>();
		boolean isOK=cutAlpha>=this.alpha;
		int i=0;
		while(isOK&&i<descriptorValues.length)
		{
			isOK=descriptors[i].check(descriptorValues[i]);
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
				cutNodes.addAll(child.getCutNodes(cutAlpha, descriptorValues));
		}
		return cutNodes;
	}
	
	public void addPixel(int[] values)
	{
		for(int i=0;i<descriptors.length;i++)
		{
			descriptors[i].addPixel(values);
		}
	}

}
