package students.desanlis_pierrel.descriptor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class AlphaTreeNodeDescriptorPerceptronWeka extends AlphaTreeNodeFilterDescriptor {
	private static int taille_x, taille_y;
	private static MultilayerPerceptron classifier = new MultilayerPerceptron();
	private static int min = Integer.MAX_VALUE;
	private static int max = Integer.MIN_VALUE;
	
	public static String corpusToARFF(String path, int taille_x, int taille_y){
		String arrfPath = path + ".arff";
		try {
			FileWriter ARRF = new FileWriter(arrfPath);
			ARRF.write("@RELATION symbol\n");
			for (int i = 0 ; i < taille_x*taille_y ; i++)
				ARRF.write("@ATTRIBUTE pix" + i + " NUMERIC\n");
			ARRF.write("@ATTRIBUTE class {Yes,No}\n");
			ARRF.write("@DATA\n");
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null){
				String[] split = line.split(" ");
				int xdim = Integer.parseInt(split[0]);
				int ydim = Integer.parseInt(split[1]);
				boolean[][] zqp = new boolean[xdim][ydim]; //zone plate initial
				boolean[][] zqp2 = new boolean[taille_x][taille_y];
				int i = 2;
				while (i < split.length - 1){
					int x = Integer.parseInt(split[i]); i++;
					int y = Integer.parseInt(split[i]); i++;
					zqp[x][y] = true;
				}
				double coeff_x = (double)taille_x / xdim;
				double coeff_y = (double)taille_y / ydim;
				for (int x = 0 ; x < taille_x ; x++)
					for (int y = 0 ; y < taille_y ; y++){
						if (zqp[(int)(x/coeff_x)][(int)(y/coeff_y)])
							zqp2[x][y] = true;
						else
							zqp2[x][y] = false;
					}			
				for (int x = 0 ; x < taille_x ; x++)
					for (int y = 0 ; y < taille_y ; y++){
						if (zqp2[x][y])
							ARRF.write("1.0,");
						else
							ARRF.write("0.0,");
					}
				if (split[split.length-1].compareTo("1") == 0)
					ARRF.write("Yes\n");
				else
					ARRF.write("No\n");
			}
			ARRF.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return arrfPath;
	}
	
	public static void init(String path, int taille_x, int taille_y){
		System.out.println("Début apprentissage perceptron");
		AlphaTreeNodeDescriptorPerceptronWeka.taille_x = taille_x;
		AlphaTreeNodeDescriptorPerceptronWeka.taille_y = taille_y;
		String corpusARRF = corpusToARFF(path,taille_x, taille_y);
		
		//Apprentissage
		DataSource source;
		try {
			source = new DataSource(corpusARRF);
			Instances data = source.getDataSet();
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes()-1);
			classifier.buildClassifier(data);
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(classifier, data, 10,new Random(1));
			System.out.println("Taux d'erreur:"+ eval.errorRate());
		} catch (Exception e) {
			e.printStackTrace();
		}		 
		System.out.println("Fin apprentissage");		
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
		return AlphaTreeNodeDescriptor.TYPE_DOUBLE;
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
