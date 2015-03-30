package main.alphaTree;

import java.util.ArrayList;

import main.alphaTree.data.AlphaTree;
import main.alphaTree.data.AlphaTreeNode;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.PelicanException;
import fr.unistra.pelican.algorithms.segmentation.flatzones.ColorFlatZones;
import fr.unistra.pelican.util.Point4D;
import fr.unistra.pelican.util.PointVideo;
import fr.unistra.pelican.util.neighbourhood.Neighbourhood4D;

/**
 *  This class builds an Alpha-Tree. 
 *  Algorithm is mine and probably not as efficient as one from Ouzounis and Soille.
 * 
 *  Designed for color images. Grey-Level images will be artificially converted to color Images.
 *  
 *  TODO : Implement Connected Components and node initialization in one pass ?
 *  TOOD : Check if Ouzounis and Soille's Algorithm is compatible with this generic way of doing things
 * 
 * @author Jonathan Weber
 *
 */
public class AlphaTreeBuilder extends Algorithm {

	/**
	 * Image to be processed
	 */
	public ByteImage inputImage;
	
	
	/**
	 * Cut Descriptor ArrayList
	 * Class must extends AlphaTreeNodeCutDescriptor
	 * Descriptors will be instantiated from class (reflexivity baby !)
	 */
	public ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptorList;
	
	/**
	 * Filter Descriptor ArrayList
	 * Class must extends AlphaTreeNodeFilterDescriptor
	 * Descriptors will be instantiated from class (reflexivity baby !)
	 */
	public ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptorList;
	
	
	/**
	 * Output Alpha Tree
	 */
	public AlphaTree alphaTree;
	
	/**
	 * Current number of nodes in the alpha-tree
	 */
	private int currentNbNodes;
	/**
	 * Current number of active nodes in the alpha-tree
	 * An active node is a node without parent
	 */
	private int nbActiveNodes;
	/**
	 * The current value of alpha
	 */
	private int currentAlpha;
	
	/**
	 * Alpha-Connections between leaves node
	 */
	ArrayList<ArrayList<AlphaEdge>> alphaConnections;
	
	public AlphaTreeBuilder() {
		super.inputs = "inputImage,cutDescriptorList,filterDescriptorList";
		super.outputs = "alphaTree";
	}
	
	
	@Override
	public void launch() throws AlgorithmException {
		//Init variables
		currentAlpha=0;
		//If grey level converts input image to color image
		if(inputImage.bdim==1)
		{
			ByteImage temp = new ByteImage(inputImage.xdim,inputImage.ydim,1,1,3);
			for(int b=0;b<3;b++)
			{
				temp.setImage4D(inputImage, b, Image.B);
			}
			inputImage=temp;
			System.out.println("inputImage converted to color image");
		}
		if(inputImage.bdim!=3)
		{
			throw new PelicanException("Alpha Tree Builder only deals with Grey-Level and Color Image. Your image has "+inputImage.bdim+" bands.");
		}
		
		
		//First, we compute the CC
		IntegerImage connectedComponents;
		long t=System.currentTimeMillis();
		connectedComponents = ColorFlatZones.exec(inputImage, Neighbourhood4D.get8Neighboorhood());
		t=System.currentTimeMillis()-t;
		System.out.println("Connected components computed in "+t+"ms.");
		//Create the Alpha-Tree
		alphaTree = new AlphaTree(inputImage,connectedComponents, cutDescriptorList, filterDescriptorList);
		//Second, we create leaf nodes and put them in alpha tree
		currentNbNodes=connectedComponents.maximum()+1;
		nbActiveNodes=currentNbNodes;
		for(int i=0;i<currentNbNodes;i++)
		{
			alphaTree.addNode(new AlphaTreeNode(i,currentAlpha, cutDescriptorList, filterDescriptorList));
			
		}
		//Third, compute alpha link between adjacent CCs
		t=System.currentTimeMillis();
		computeAdjacentEdgesAndInitDescriptors(inputImage,connectedComponents);
		t=System.currentTimeMillis()-t;
		System.out.println("Adjacent Edges computed in "+t+"ms ("+currentNbNodes+" nodes).");
		//Fourth, let's compute the different Alpha Level
		while(nbActiveNodes>1)
		{
			currentAlpha++;
			t=System.currentTimeMillis();
			computeAlphaLevel();
			t=System.currentTimeMillis()-t;
			System.out.println("Alpha "+currentAlpha+" computed ("+currentNbNodes+" nodes and "+nbActiveNodes+" actives nodes) in "+t+"ms.");
		}
		//Alpha Tree is computed !
		
		
	}
	
	
	/**
	 * This method computes each alpha level
	 */
	private void computeAlphaLevel() {
		ArrayList<AlphaEdge> aAE = alphaConnections.get(currentAlpha);
		ArrayList<AlphaTreeNode> newNodes = new ArrayList<AlphaTreeNode>();
		for(AlphaEdge aE : aAE)
		{
			AlphaTreeNode node1 = aE.node1.getRoot();
			AlphaTreeNode node2 = aE.node2.getRoot();
			//Check if node's root are different if they are, merge is already made
			if(node1!=node2)
			{
				//Special case, both root are nodes of the current Alpha
				if(node1.getAlpha()==currentAlpha&&node2.getAlpha()==currentAlpha)
				{
					node1.mergeWith(node2);
					newNodes.remove(node2);
					nbActiveNodes--;
				}
				//Special case, one root is a node of the current Alpha
				else if(node1.getAlpha()==currentAlpha)
				{
					node1.addChild(node2);
					nbActiveNodes--;
				}
				else if(node2.getAlpha()==currentAlpha)
				{
					node2.addChild(node1);
					nbActiveNodes--;
				}
				//Normal case ? No root is a node of current Alpha
				else
				{
					//Check if one root is in other root offspring
					if(!(node1.getAlpha()==node2.getAlpha()))
					{
						if(node2.getAlpha()>node1.getAlpha())
						{
							AlphaTreeNode pivot = node1;
							node1=node2;
							node2=pivot;
						}
						if(!node1.isParentOf(node2))
						{
							newNodes.add(new AlphaTreeNode(-1, currentAlpha, node1, node2));
							nbActiveNodes--;
						}
					}
					else
					{
						newNodes.add(new AlphaTreeNode(-1, currentAlpha, node1, node2));
						nbActiveNodes--;
					}
				}
			}
			
		}
		for(AlphaTreeNode node : newNodes)
		{
			node.setId(currentNbNodes);
			currentNbNodes++;
			alphaTree.addNode(node);
		}
	}

	/**
	 * This method computes edges between adjacent CC. It also init descriptors values.
	 * @param inputImage
	 * @param cC
	 */
	private void computeAdjacentEdgesAndInitDescriptors(Image inputImage,IntegerImage cC) {
		int yDim=cC.ydim;
		int xDim=cC.xdim;
		Point4D[] semiNeighbourhood = Neighbourhood4D.getSemi8Neighboorhood();
		for(Point4D q : semiNeighbourhood)
		{
			q.setIndex(xDim, yDim, 1, 1);
		}
		//AlreadyComputedAlphaEdge
		ArrayList<ArrayList<AlphaEdge>> existingAdjacentEdges = new ArrayList<ArrayList<AlphaEdge>>(currentNbNodes);
		for(int i=0;i<currentNbNodes;i++)
		{
			existingAdjacentEdges.add(new ArrayList<AlphaEdge>());
		}
		//Init alphaConnections data structure
		alphaConnections = new ArrayList<ArrayList<AlphaEdge>>();
		for(int alpha=0;alpha<256;alpha++)
		{
			alphaConnections.add(new ArrayList<AlphaEdge>());
		}
		//compute alpha edges
		int index=0;
		int coloredIndex=0;
		for(int y=0;y<yDim;y++)
			for(int x=0;x<xDim;x++,index++,coloredIndex+=3)
			{
				AlphaTreeNode node1=alphaTree.getNode(cC.getPixelInt(index));
				int[] currentPixelValues = inputImage.getVectorPixelByte(coloredIndex);
				//For node's descriptor's initialization
				node1.addPixel(currentPixelValues, new PointVideo(x,y,0));
				for(Point4D q : semiNeighbourhood)
				{
					if(x+q.x >= 0 && x+q.x < xDim && y+q.y >= 0 && y+q.y < yDim)
					{
						AlphaTreeNode node2=alphaTree.getNode(cC.getPixelInt(index+q.index));
						int alpha=-1;
						for(int b=0;b<3;b++)
						{
							int localAlpha=Math.abs(currentPixelValues[b]-inputImage.getPixelByte(coloredIndex+(q.index*3)+b));
							if(localAlpha>alpha)
							{
								alpha=localAlpha;
							}
						}

						if(node1!=node2 && alpha>0)
						{
							if(node1.getId()>node2.getId())
							{
								AlphaTreeNode pivot = node1;
								node1=node2;
								node2=pivot;
							}
							boolean edgeAlreadyExist=false;
							ArrayList<AlphaEdge> aAE = existingAdjacentEdges.get(node1.getId());
							for(AlphaEdge edge : aAE)
							{
								if(edge.node2.getId()==node2.getId())
								{
									edgeAlreadyExist=true;
									if(alpha<edge.alpha)
									{
										edge.alpha=alpha;
									}
									break;
								}
							}							
							if(!edgeAlreadyExist)
							{
								alphaConnections.get(alpha).add(new AlphaEdge(node1,node2,alpha));
							}
						}
						
					}
					
				}
			}
		for(ArrayList<AlphaEdge> aAE : existingAdjacentEdges)
		{
			for(AlphaEdge edge : aAE)
			{
				alphaConnections.get(edge.alpha).add(edge);
			}
		}
	}
	
	/**
	 * This static method launch the building of the Alpha-tree
	 * @param img
	 * @param descriptorList
	 * @return
	 */
	public static AlphaTree exec(ByteImage img, ArrayList<Class<? extends AlphaTreeNodeCutDescriptor>> cutDescriptorList, ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptorList)
	{		
		return (AlphaTree) new AlphaTreeBuilder().process(img,cutDescriptorList, filterDescriptorList);
	}
	
	private class AlphaEdge
	{
		AlphaTreeNode node1;
		AlphaTreeNode node2;
		int alpha;

		public AlphaEdge(AlphaTreeNode node1, AlphaTreeNode node2, int alpha)
		{
			this.node1=node1;
			this.node2=node2;
			this.alpha=alpha;
		}
		
	}

}
