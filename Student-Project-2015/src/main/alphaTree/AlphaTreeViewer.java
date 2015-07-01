package main.alphaTree;

import main.alphaTree.data.AlphaTree;
import main.alphaTree.ui.AlphaTreeView;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.IntegerImage;

public class AlphaTreeViewer extends Algorithm {
	
	public AlphaTree alphaTree;
	
	public IntegerImage segmentation;

	@Override
	public void launch() throws AlgorithmException {
		try {
			AlphaTreeView aTV = new AlphaTreeView(alphaTree);
			segmentation = aTV.getSegmentedImage();
			
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public AlphaTreeViewer()
	{
		super();
		super.inputs="alphaTree";
		super.outputs = "segmentation";
	}
	
	public static IntegerImage exec(AlphaTree alphaTree)
	{
		return (IntegerImage) new AlphaTreeViewer().process(alphaTree);
	}

}
