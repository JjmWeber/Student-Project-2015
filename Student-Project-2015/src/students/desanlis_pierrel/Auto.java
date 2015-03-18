package students.desanlis_pierrel;

import java.io.File;
import java.util.ArrayList;

import main.alphaTree.AlphaTreeBuilder;
import main.alphaTree.data.AlphaTree;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorArea;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptorOmega;
import students.desanlis_pierrel.descriptor.AlphaTreeNodeDescriptorOTeinte;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Descriptor;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.gui.FileChooserToolBox;

/**
 * 
 * @author Pierrel/Desanlis
 *
 */
public class Auto {
	
	public static void main(String[] args){
		File dir = FileChooserToolBox.openOpenDirectoryChooser(null);
		File[] images = dir.listFiles();
		int nb_step = 5; // Nombre d'images par descripteur
		
		
		ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptors = new ArrayList<Class<? extends AlphaTreeNodeDescriptor>> ();
		descriptors.add(AlphaTreeNodeDescriptorOmega.class);
		descriptors.add(AlphaTreeNodeDescriptorArea.class);		
		descriptors.add(AlphaTreeNodeDescriptorOTeinte.class);
		
		for (File f : images){
			double desc_val[] = new double[descriptors.size()];
			ByteImage img = (ByteImage)ImageLoader.exec(f.getAbsolutePath());
			long t = System.currentTimeMillis();
			AlphaTree result = AlphaTreeBuilder.exec(img,descriptors);
			t=System.currentTimeMillis()-t;
			System.out.println("Alpha-tree creation time : "+t+"ms");
			
			imgSaver(f,result,descriptors, desc_val, 0,nb_step);
		}
	}
	
	/**
	 * Methode recursive pour enregistrer toute les possibilitées et les comparer
	 * @param f File correspondant a l'image
	 * @param arbre Arbre Alpha !!!
	 * @param descriptors 
	 * @param i Position dans la liste des descripteurs
	 * @param nb_step // Nombre d'image par descripteur
	 */
	private static void imgSaver(File f, AlphaTree arbre, ArrayList<Class<? extends AlphaTreeNodeDescriptor>> descriptors, double[] desc_val, int level, int nb_step){
		if (level == descriptors.size()-1) //On a toucher le fond, on enregistre
			; //TODO Sauvergarder l'image pour certaine val. de descripteur
		else{
			for (int i = 0 ; i < nb_step; i++ ){
				double[] val = desc_val.clone();
				val[level] = (arbre.getMaxDescriptorValues()[level]/nb_step)*(double)i;
				imgSaver(f, arbre, descriptors, val, level+1, nb_step);
			}
		}
			
	}
}
