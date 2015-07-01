package students.desanlis_pierrel.descriptor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import fr.unistra.pelican.util.PointVideo;

/*
 * Descripteur filtre représentant la probabilité que la zone soit image pour le perceptron multicouche de la lib Weka
 * Une phase d'initialisation avec un corpus créé avec la class AlphaTreeViewWekaTrainer est nécessaire
 */
public class AlphaTreeNodeDescriptorPerceptronWeka extends AlphaTreeNodeFilterDescriptor {
	private static int taille_x, taille_y; // Dimension de la zone passée en entrée du perceptron
	private static MultilayerPerceptron classifier = new MultilayerPerceptron();
	private static double min = Double.MAX_VALUE;
	private static double max = Double.MIN_VALUE;
	private static double ratioMaj = 0.10; //% de nouveaux pixels avant recalcule
	private static int minPix = 100; //Nb de pixel dans un zone avant de faire la maj.
	private int nouveauPts = 0; // Nombre de nouveaux points depuis la dernière maj.
	private double value = 0; // Valeur du descritpeur
	private LinkedList<PointVideo> listPixel = new LinkedList<PointVideo>(); //Liste des points de la zone
	private static Instances data;

	/*
	 * Cette méthode sert a créer un fichier corpus utilisable par la librairie Weka
	 * path : chemin vers un corpus créer à l'aide de la librairie Weka
	 * taille_x/taille_y : dimension de la zone passé en entrée du perceptron
	 */
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

	/*
	 * Méthode à appeler impérativement avant d'utiliser le perceptron, il s'agit de class qui réalise l'apprentissage
	 * path : chemin vers un corpus créer avec la class AlphaTreeViewWekaTrainer
	 * taille_x / taille_y : dimension de la zone passé en entrée du perceptron
	 */
	public static void init(String path, int taille_x, int taille_y){
		System.out.println("Début apprentissage perceptron");
		AlphaTreeNodeDescriptorPerceptronWeka.taille_x = taille_x;
		AlphaTreeNodeDescriptorPerceptronWeka.taille_y = taille_y;
		String corpusARRF = corpusToARFF(path,taille_x, taille_y);

		//Apprentissage
		DataSource source;
		try {
			source = new DataSource(corpusARRF);
			data = source.getDataSet();
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
	
	public static double calcValueList(LinkedList<PointVideo> listPts){
		return calcValue(listPts);
	}

	private static double calcValue(LinkedList<PointVideo> listPixel){
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		double value = 0;
		for (PointVideo p : listPixel){
			if (p.x < minX) minX = p.x;
			if (p.x > maxX) maxX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.y > maxY) maxY = p.y;
		}
		if ((maxX-minX)>(minPix/20) && (maxY-minY)>(minPix/20)){
			boolean[][] zqp = new boolean[taille_x][taille_y];
			int xdim = maxX - minX +1;
			int ydim = maxY - minY +1;
			double coeff_x = (double)taille_x / xdim;
			double coeff_y = (double)taille_y / ydim;
			for (PointVideo p : listPixel){
				int x2 = (int) (p.x*coeff_x);
				int y2 = (int) (p.y*coeff_y);
				if (x2 >= 0 && x2 < taille_x && y2 >= 0 && y2 < taille_y)
					zqp[x2][y2] = true;
			}

			Instance instance = new Instance(data.numAttributes());
			instance.setDataset(data);
			int i = 0;
			for (int x = 0 ; x < taille_x ; x++)
				for (int y = 0 ; y < taille_y ; y++){
					if (zqp[x][y])
						instance.setValue(i, 1);
					else
						instance.setValue(i, 0);
					i++;
				}
			
			try {
				double[] res = classifier.distributionForInstance(instance);
				value = res[0];
			} catch (Exception e) {
				e.printStackTrace();
			}
			return value;
		}
		return 0;
	}

	@Override
	public boolean check(double minValue, double maxValues) {
		if (value >= minValue && value <= maxValues)
			return true;
		return false;
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		listPixel.add(coord);
		nouveauPts++;
		if ((double)nouveauPts/listPixel.size() > ratioMaj && listPixel.size() > minPix)
			value = calcValue(listPixel);

		if (value < min)
			min = value;
		if (value > max)
			max = value;
	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorPerceptronWeka desc = (AlphaTreeNodeDescriptorPerceptronWeka)descriptor;
		if (desc.listPixel.size() > listPixel.size()){
			LinkedList<PointVideo> buff = this.listPixel;
			this.listPixel = desc.listPixel;
			this.nouveauPts = desc.nouveauPts;
			this.value = desc.value;
			this.nouveauPts += buff.size();
			this.listPixel.addAll(buff);
		}
		else{
			listPixel.addAll(desc.listPixel);
			nouveauPts+=desc.listPixel.size();
		}

		if ((double)nouveauPts/listPixel.size() > ratioMaj && listPixel.size() > minPix)
			value = calcValue(listPixel);
		
		if (value < min)
			min = value;
		if (value > max)
			max = value;
	}

	@Override
	public double getValue() {
		return value;
	}

	@Override
	public String getDescriptorName() {
		return "Weka perceptron";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorPerceptronWeka clone = new AlphaTreeNodeDescriptorPerceptronWeka();
//		clone.listPixel = new LinkedList<PointVideo>();
//		clone.listPixel.addAll(this.listPixel);
		clone.listPixel = this.listPixel;
		clone.nouveauPts = this.nouveauPts;
		clone.value = this.value;
		return clone;
	}

	@Override
	public int getType() {
		return AlphaTreeNodeDescriptor.TYPE_DOUBLE;
	}

	@Override
	public double getMin() {
		return min;
	}

	@Override
	public double getMax() {
		return max;
	}
}
