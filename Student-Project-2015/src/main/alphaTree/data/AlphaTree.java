package main.alphaTree.data;

import java.util.ArrayList;

import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;

/**
 * Alpha-Tree data structure
 * 
 * Cannot be computed from this class, need AlphaTreeBuilder. 
 * 
 * @author Jonathan Weber
 *
 */
public class AlphaTree {
	
	/**
	 * Nodes of the tree
	 */
	private ArrayList<AlphaTreeNode> nodes;
	
	/**
	 * Original Image
	 */
	private Image originalImage;
	
	/**
	 * Connected Components Image
	 */
	private IntegerImage cCImage;
	
	/**
	 * Tree Node Descriptors List
	 */
	private ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptorList;
	
	/**
	 * Number of Leaves
	 */
	private int numberOfLeaves;
	
	/**
	 * Max Alpha
	 */
	private int maxAlpha=0;

	public AlphaTree(Image originalImage, IntegerImage cCImage, ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptorList)
	{
		this.originalImage=originalImage;
		this.cCImage=cCImage;
		this.descriptorList=descriptorList;
		numberOfLeaves=cCImage.getNumberOfUsedLabels();
		nodes = new ArrayList<AlphaTreeNode> ();
	}
	
	public void addNode(AlphaTreeNode node)
	{
		nodes.add(node);
		if(node.alpha>maxAlpha)
		{
			maxAlpha=node.alpha;
		}
	}
	
	public AlphaTreeNode getNode(int index)
	{
		return nodes.get(index);
	}
	
	public Image getOriginalImage()
	{
		return originalImage;
	}
	
	public int getMaxAlpha()
	{
		return maxAlpha;
	}
	
	public double[] getMaxDescriptorValues()
	{
		double[] maxValues = new double[descriptorList.size()];
		for(int i=0;i<descriptorList.size();i++)
		{
			maxValues[i]=getRoot().descriptors[i].getValue();
		}		
		return maxValues;
	}
	
	public IntegerImage getCCImage()
	{
		return cCImage;		
	}
	
	public ArrayList<Class<? extends AlphaTreeNodeDescriptor>> getDescriptorList()
	{
		return descriptorList;
	}
	
	public int getNumberOfLeaves()
	{
		return numberOfLeaves;
	}
	
	public AlphaTreeNode getRoot()
	{
		return nodes.get(nodes.size()-1);
	}
	
	public ArrayList<AlphaTreeNode> getLeaves()
	{
		return (ArrayList<AlphaTreeNode>) nodes.subList(0, numberOfLeaves-1);
	}
	
	public ArrayList<AlphaTreeNode> getNodes()
	{
		return nodes;
	}
	
	public IntegerImage getSegmentationFromCut(int alpha, double[] descriptorValues)
	{
		ArrayList<AlphaTreeNode> cutNodes = getRoot().getCutNodes(alpha, descriptorValues);
		int[] lookUpTable=new int[numberOfLeaves];
		for(AlphaTreeNode cutNode : cutNodes)
		{
			ArrayList<AlphaTreeNode> leaves = cutNode.getLeaves();
			for(AlphaTreeNode leave : leaves)
			{
				lookUpTable[leave.id]=cutNode.id;
			}
		}
		IntegerImage segmentationFromCut = cCImage.copyImage(false);
		int imageSize=segmentationFromCut.size();
		for(int index=0;index<imageSize;index++)
		{
			segmentationFromCut.setPixelInt(index, lookUpTable[cCImage.getPixelInt(index)]);
		}
		return segmentationFromCut;
	}
	
}
