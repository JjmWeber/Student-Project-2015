package students.desanlis_pierrel.descriptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;



public class AlphaTreeNodeDescriptorPerceptron extends AlphaTreeNodeFilterDescriptor{
	private static double min=Double.POSITIVE_INFINITY;
	private static double max=Double.NEGATIVE_INFINITY;

	private static int taille_x;
	private static int taille_y;

	/**
	 * 
	 * @param taille_x largeur des images (& zone) en entrée
	 * @param taille_y hauteur des images (& zone) en entrée
	 * @param path dossier contenant les symboles pour l'apprentissage: deux sous dossier Learn et Test
	 * 	chacun contenant deux sous dossier OK NOK contenant des symboles N/B (dimmension libre)
	 * @throws IOException 
	 */

	public static void init (int taille_x, int taille_y, String path){
		AlphaTreeNodeDescriptorPerceptron.taille_x = taille_x;
		AlphaTreeNodeDescriptorPerceptron.taille_y = taille_y;		
		createCorpus(path);
	}

	private static String createCorpus(String path){
		File corpus = new File("corpus.crp");
		FileWriter writer = null;
		try{
			writer = new FileWriter(corpus);
			writer.write("#Symbol corpus\n");
			writer.write("Symbol\n");
			writer.write("Classification\n");
			for (int i = 0 ; i < taille_x*taille_y ; i++) //Type des colonnes
				writer.write("Numerical,");
			writer.write("Label,Target,Target\n");
			for (int i = 0 ; i < taille_x*taille_y ; i++) //Nom des colonnes (sert a rien)
				writer.write("pix" + i +",");
			writer.write("Symbol,OK,NOK\n");
			writer.write(dirToCrp(path + "\\learn\\ok",true,true));
			//writer.write(dirToCrp(path + "\\learn\\nok",true,false));
			//writer.write(dirToCrp(path + "\\test\\ok",false,true));
			//writer.write(dirToCrp(path + "\\test\\nok",false,false));
			writer.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}

		if (writer != null)
			return corpus.getAbsolutePath();
		else return null;
	}

	private static String dirToCrp(String path, boolean learn, boolean ok){
		String crp = "";
		File dir = new File(path);
		File files[] = dir.listFiles();

		for (File f : files){
			if (f.getName().compareTo(".") != 0 && f.getName().compareTo("..") != 0){
				ByteImage image = (ByteImage)ImageLoader.exec(f.getAbsolutePath());	
				double coeff_x = (double)taille_x/image.getXDim(); 
				double coeff_y = (double)taille_y/image.getYDim();
				for (int x = 0 ; x < image.xdim ; x++)
					for (int y = 0 ; y < image.ydim ; y++)
					{
						int x2 = (int)(Math.floor(x/coeff_x));
						int y2 = (int)(Math.floor(y/coeff_y));
						if (x2 < image.xdim  && x2 >= 0 && y2 < image.ydim && y2 >= 0) //TODO Faire un calcul correct pour eviter le if
							if (image.getPixelXYByte(x2,y2) == 0)
								crp += "1,";
							else
								crp += "0,";
					}
				if (ok)
					crp += "OK,1.0,0.0,";
				else
					crp += "NOK,0.0,1.0,";
				if (learn)
					crp += "(Learn)\n";
				else
					crp += "(Test)\n";
			}
		}

		return crp;
	}

	@Override
	public boolean check(double minValue, double maxValues) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void addPixel(int[] values, PointVideo coord) {
		// TODO Auto-generated method stub

	}
	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		// TODO Auto-generated method stub

	}
	@Override
	public double getValue() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getDescriptorName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public AlphaTreeNodeDescriptor clone() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public double getMin() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public double getMax() {
		// TODO Auto-generated method stub
		return 0;
	}

}
