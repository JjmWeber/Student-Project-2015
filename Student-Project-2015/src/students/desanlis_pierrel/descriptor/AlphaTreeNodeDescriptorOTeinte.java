package students.desanlis_pierrel.descriptor;

import demo.AlphaTreeDemo;
import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeCutDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;

/**
 * Essai creation de descripteur type Omega bas� sur la teinte 
 * @author Desanlis/Pierrel
 *
 */
public class AlphaTreeNodeDescriptorOTeinte extends AlphaTreeNodeFilterDescriptor {
	private static int min=Integer.MAX_VALUE;
	private static int max=Integer.MIN_VALUE;
	
	private int valMin;
	private int valMax;
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
		valMin = 0;
		valMax = 360;
		oTeinte = -1;
	}
	
	@Override
	public void addPixel(int[] values, PointVideo coord){
		int val = rvb2t(values);
		if (val < valMin)
			valMin = val;
		if (val > valMax)
			valMax = val;
		if (valMax - valMin > oTeinte) //maj
			oTeinte = valMax-valMin;
		
		if (oTeinte < min)
			min = oTeinte;
		if (oTeinte > max)
			max = oTeinte;
		
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorOTeinte desc = (AlphaTreeNodeDescriptorOTeinte)descriptor;
		if (desc.valMin < valMin)
			valMin = desc.valMin;
		if (desc.valMax > valMax)
			valMax = desc.valMax;
		if (valMax-valMin > oTeinte)
			oTeinte = valMax-valMin;
		
		if (oTeinte < min)
			min = oTeinte;
		if (oTeinte > max)
			max = oTeinte;
	}


	@Override
	public double getValue() {
		return oTeinte;
	}

	@Override
	public String getDescriptorName() {
		return "Ecart teinte";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorOTeinte clone = new AlphaTreeNodeDescriptorOTeinte();
		clone.valMin = valMin;
		clone.valMax = valMax;
		clone.oTeinte=oTeinte;
		return clone;
	}


	@Override
	public int getType() {
		return AlphaTreeNodeFilterDescriptor.TYPE_INT;
	}

	@Override
	public double getMin() {
		return min;
	}

	@Override
	public double getMax() {
		return max;
	}

	@Override
	public boolean check(double minValue, double maxValues) {
		if (oTeinte >= minValue && oTeinte <= maxValues)
			return true;
		return false;
	}
}
