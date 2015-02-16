package alphaTree;

import alphaTree.data.AlphaTree;
import alphaTree.ui.AlphaTreeView;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;

public class AlphaTreeViewer extends Algorithm {
	
	public AlphaTree alphaTree;

	@Override
	public void launch() throws AlgorithmException {
		try {
			new AlphaTreeView(alphaTree);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public AlphaTreeViewer()
	{
		super();
		super.inputs="alphaTree";
		super.outputs = "";
	}
	
	public static void exec(AlphaTree alphaTree)
	{
		new AlphaTreeViewer().process(alphaTree);
	}

}
