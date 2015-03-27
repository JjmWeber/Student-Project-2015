package students.desanlis_pierrel.descriptor;

import demo.AlphaTreeDemo;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;

/**
 * Essai creation de descripteur type Omega bas� sur la teinte 
 * @author Desanlis/Pierrel
 *
 */
public class AlphaTreeNodeDescriptorOTeinte extends AlphaTreeNodeCutDescriptor {
	private int min;
	private int max;
	private int oTeinte;	
	
	/**
	 * @param values Les trois composantes de la couleur
	 * @return La teinte correspondantes dans l'espace HSV
	 */
	private int rvb2t(int[] values){
		int composante_max = 0;
		int val_min = 256;
		int val_max = -1;
		int t = 0;
		for (int i = 0 ; i < 3 ; i++){ //Recherche de la composante max
			if (values[i] > val_max){
				composante_max = i;
				val_max = values[i];
			}
			if (values[i] < val_min)
				val_min = values[i];
		}
		if (val_max == val_min)
			return 0;
		
		if (composante_max == 0)
			t = (60*((values[1]-values[2])/(val_max-val_min))+360)%360;
		else if (composante_max == 1)
			t = (60*((values[2]-values[0])/(val_max-val_min))+120)%360;
		else if (composante_max == 2)
			t = (60*((values[0] - values[1])/(val_max-val_min))+240)%360;
		
		return t;
	}
	
	public void AlphaTreeNodeDescriptorTeinte() {
		min = 0;
		max = 360;
		oTeinte = -1;
	}
	
	@Override
	public void addPixel(int[] values) {
		int val = rvb2t(values);
		if (val < min)
			min = val;
		if (val > max)
			max = val;
		if (max - min > oTeinte) //maj
			oTeinte = max-min;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorOTeinte desc = (AlphaTreeNodeDescriptorOTeinte)descriptor;
		if (desc.min < min)
			min = desc.min;
		if (desc.max > max)
			max = desc.max;
		if (max-min > oTeinte)
			oTeinte = max-min;
	}

	@Override
	public boolean check(double value) {
		return value>=oTeinte;
	}

	@Override
	public double getValue() {
		return oTeinte;
	}

	@Override
	public String getDescriptorName() {
		return "OTeinte";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorOTeinte clone = new AlphaTreeNodeDescriptorOTeinte();
		clone.min = min;
		clone.max = max;
		clone.oTeinte=oTeinte;
		return clone;
	}
}
