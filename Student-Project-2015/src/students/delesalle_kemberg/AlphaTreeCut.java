package students.delesalle_kemberg;

import main.alphaTree.data.AlphaTree;
import main.alphaTree.data.AlphaTreeNode;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;

/**
 * Cut useless AlphaTreeNodes which cannot be a letter
 * 
 */

public class AlphaTreeCut extends Algorithm {
	
	/**
	 * Input parameter.
	 */
	public AlphaTree input;

	/**
	 * Output parameter.
	 */
	public AlphaTree output; //AlphaTree input dont le tableau des noeuds actifs a été modifié
	
	/**
	 *  Minimal ZQ area
	 */
	public double min_area;
	
	/**
	 *  Maximal ZQ area
	 */
	public double max_area;
	
	/**
	 * Constructor
	 * 
	 */
	public AlphaTreeCut() {
		super.inputs = "input";
		super.outputs = "output";
		this.min_area=Double.MIN_VALUE; //Pour le moment ...
		this.max_area=Double.MAX_VALUE;
	}

	@Override
	public void launch() throws AlgorithmException {
		this.output=this.input;
		
		for(AlphaTreeNode node : output.getNodes()){		
			//Parmi les descripteurs du noeud qu'on observe, on cherche le 'Area'
			for(int i=0 ; i<node.getDescriptors().length ; i++){
				if(node.getDescriptors()[i] instanceof AlphaTreeNodeDescriptorArea){
					//Une fois trouve, on regarde sa valeur
					double val = node.getDescriptors()[i].getValue();
					
					// On verifie si cette valeur est en dehors de l'intervalle des aires des
					// zones qui peuvent etre du texte
					if(val > max_area || val < min_area){
						//int idf = node.getId();
						
						// On modifie le tableau des noeuds (ou l'attribut du noeud en question
						// en transformant le noeud en question en un noeud inactif
						
					}
					
				}
			}
			
		}

	}
	
	/**
	 * 
	 * @param input original AlphaTree
	 * @return input with useless Nodes (for text detection) disabled 
	 */
	public static AlphaTree exec (AlphaTree input) {
		return (AlphaTree) new AlphaTreeCut().process(input);
	}

}
