package main.alphaTree.data;

import java.util.ArrayList;
import java.util.Arrays;

import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
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
	 * Tree Node Cut Descriptors List
	 */
	private ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptorList;
	
	/**
	 * Tree Node Filter Descriptors List
	 */
	private ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptorList;
	
	/**
	 * Number of Leaves
	 */
	private int numberOfLeaves;
	
	/**
	 * Max Alpha
	 */
	private int maxAlpha=0;

	public AlphaTree(Image originalImage, IntegerImage cCImage, ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptorList, ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptorList)
	{
		this.originalImage=originalImage;
		this.cCImage=cCImage;
		this.cutDescriptorList=cutDescriptorList;
		this.filterDescriptorList=filterDescriptorList;
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
	
	public double[] getMaxCutDescriptorValues()
	{
		double[] maxValues = new double[cutDescriptorList.size()];
		for(int i=0;i<cutDescriptorList.size();i++)
		{
			maxValues[i]=getRoot().cutDescriptors[i].getValue();
		}		
		return maxValues;
	}
	
	//TODO : Reimplement because filtering descriptors can be non croissant
	public double[] getMaxFilterDescriptorValues()
	{
		double[] maxValues = new double[filterDescriptorList.size()];
		for(int i=0;i<filterDescriptorList.size();i++)
		{
			maxValues[i]=getRoot().filterDescriptors[i].getValue();
		}		
		return maxValues;
	}
	
	public IntegerImage getCCImage()
	{
		return cCImage;		
	}
	
	public ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> getCutDescriptorList()
	{
		return cutDescriptorList;
	}
	
	public ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> getFilterDescriptorList()
	{
		return filterDescriptorList;
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
	
	public IntegerImage getSegmentationFromCutAndFiltering(int alpha, double[] cutDescriptorValues, double[] minFilteringValues, double[] maxFilteringValues)
	{
		ArrayList<AlphaTreeNode> cutNodes = getRoot().getCutNodes(alpha, cutDescriptorValues);
		int[] lookUpTable=new int[numberOfLeaves];
		Arrays.fill(lookUpTable, -1);
		for(AlphaTreeNode cutNode : cutNodes)
		{			
			boolean selected=true;
			AlphaTreeNodeFilterDescriptor[] descriptors = cutNode.getFilterDescriptors();
			for(int i=0;i<descriptors.length;i++)
			{
				if(descriptors[i].getValue()<minFilteringValues[i] || descriptors[i].getValue()>maxFilteringValues[i])
				{
					selected=false;
					break;
				}
			}
			if(selected)
			{
				ArrayList<AlphaTreeNode> leaves = cutNode.getLeaves();
				for(AlphaTreeNode leave : leaves)
				{
					lookUpTable[leave.id]=cutNode.id;
				}
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
