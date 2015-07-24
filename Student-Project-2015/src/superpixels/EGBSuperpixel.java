package superpixels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import superpixels.GraphImage.Pixel;
import fr.unistra.pelican.Algorithm;
import fr.unistra.pelican.AlgorithmException;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.IntegerImage;
import fr.unistra.pelican.algorithms.conversion.RGBToXYZ;
import fr.unistra.pelican.algorithms.conversion.XYZToLAB;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaClosing;
import fr.unistra.pelican.algorithms.morphology.gray.GrayAreaOpening;
import fr.unistra.pelican.algorithms.morphology.vectorial.gradient.MultispectralEuclideanGradient;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.morphology.FlatStructuringElement2D;

/**
 * Implementation of Efficient Graph based Image Segmentation
 * 
 * 
 * 
 * @author Jonathan Weber, Thomas Delesalle
 */

public class EGBSuperpixel extends Algorithm {

	public Image inputImage;
	public IntegerImage outputImage;



	public EGBSuperpixel()
	{
		super();
		super.inputs="inputImage";
		super.outputs = "outputImage";
	}

	@Override
	public void launch() throws AlgorithmException {
		int xDim = inputImage.xdim;
		int yDim = inputImage.ydim;


		Image lab = XYZToLAB.exec(RGBToXYZ.exec(inputImage));
		GraphImage myGraphImage = new GraphImage(lab);
		System.out.println("GraphImage created");
		System.out.println("number of nodes = "+myGraphImage.nodes.size()+", difference = "+(xDim*yDim-myGraphImage.nodes.size()));
		System.out.println("number of edges = "+myGraphImage.edges.size()+", difference = "+(((xDim-1)*yDim+((yDim-1)*xDim))-myGraphImage.edges.size()));

		Collections.sort(myGraphImage.edges);

		System.out.println("sorted");

		//	for(int i = 0; i < myGraphImage.edges.size();i++){
		//		myGraphImage.edges.get(i).displayEdge();
		//	}

		System.out.println("We now start to work on the graph");

		computeGraph(myGraphImage);
	}


	public GraphImage computeGraph(GraphImage graph) {
		System.out.println("entering the computeGraph method");
		int father[] = new int[graph.nodes.size()];
		for(int i = 0; i < graph.nodes.size(); i++){
			father[i] = graph.nodes.get(i).pixels.get(0).id;
		}

		MyUF myCuteUF = new MyUF(father);

		//for each edge of our graph
		for(int i = 0; i < graph.edges.size(); i++){
			//graph.edges.get(i).displayEdge();
			if(myCuteUF.FindWithCompression(graph.edges.get(i).start) == myCuteUF.FindWithCompression(graph.edges.get(i).end)){
				//System.out.println("the considered pixels are already in the same zone");
			}
		}



		System.out.println("done...");
		return graph;

	}

	/**
	 * @param inputImage  image to compute

	 * @return  EGBSUperpixel image
	 */
	public static IntegerImage exec(Image inputImage)
	{
		return (IntegerImage) new EGBSuperpixel().process(inputImage);
	}

}
