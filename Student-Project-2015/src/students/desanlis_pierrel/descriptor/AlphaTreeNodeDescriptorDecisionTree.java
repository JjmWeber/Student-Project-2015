package students.desanlis_pierrel.descriptor;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import fr.unistra.pelican.util.PointVideo;

/*
 * Pourquoi s'embeter à chercher les bonnes valeurs de filtre quand Weka pour le faire
 */
public class AlphaTreeNodeDescriptorDecisionTree extends AlphaTreeNodeFilterDescriptor{
	private static J48 classifier = new J48();
	private static double min = Double.MAX_VALUE;
	private static double max = Double.MIN_VALUE;
	private static double ratioMaj = 0.10; //% de nouveau pixel avant recalcule
	private static int minPix = 100; //Nb de pixel dans un zone avant de faire la maj.
	private int nouveauPts = 0;
	private double value = 0;
	private LinkedList<PointVideo> listPixel = new LinkedList<PointVideo>();
	private static Instances data;

	private static ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors;

	private static String corpusToARFF(String path){
		String arrfPath = path + "Tree.arff";
		try {
			LinkedList<PointVideo> listPts = new LinkedList<PointVideo>();
			FileWriter ARRF = new FileWriter(arrfPath);
			ARRF.write("@RELATION symbol\n");
			for (int i = 0 ; i < filterDescriptors.size(); i++)
				ARRF.write("@ATTRIBUTE desc" + i + " NUMERIC\n");
			ARRF.write("@ATTRIBUTE class {Yes,No}\n");
			ARRF.write("@DATA\n");
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null){
				listPts.clear();
				String[] split = line.split(" ");

				int i = 2;
				while (i < split.length - 1){
					int x = Integer.parseInt(split[i]); i++;
					int y = Integer.parseInt(split[i]); i++;
					listPts.add(new PointVideo(x,y,0));
				}

				for (int q = 0 ; q < filterDescriptors.size() ; q++){
					if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorSurfaceRatio.class.getName()) == 0){
						ARRF.write(Double.toString(AlphaTreeNodeDescriptorSurfaceRatio.calcValueList(listPts)) + ",");
					}
					else if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorHeightWidthRatio2.class.getName()) == 0){
						ARRF.write(Double.toString(AlphaTreeNodeDescriptorHeightWidthRatio2.calcValueList(listPts))+",");
					}
					else if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorSyntax.class.getName()) == 0){
						ARRF.write(Double.toString(AlphaTreeNodeDescriptorSyntax.calcValueList(listPts))+",");
					}
					else if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorArea2.class.getName()) == 0){
						ARRF.write(Double.toString(AlphaTreeNodeDescriptorArea2.calcValueList(listPts))+",");
					}
					else if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorOTeinte.class.getName()) == 0){
						ARRF.write(0 + ",");
					}
					else if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorContourRatio.class.getName()) == 0){
						ARRF.write(Double.toString(AlphaTreeNodeDescriptorContourRatio.calcValueList(listPts))+",");
					}
					else if (filterDescriptors.get(q).getName().compareTo(AlphaTreeNodeDescriptorPerceptronWeka.class.getName()) == 0){
						ARRF.write(Double.toString(AlphaTreeNodeDescriptorPerceptronWeka.calcValueList(listPts))+",");
					}
					else
						System.out.println("ERREUR: filtre inconnu");
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

	public static void init(ArrayList<Class<? extends AlphaTreeNodeFilterDescriptor>> filterDescriptors, String path){
		AlphaTreeNodeDescriptorDecisionTree.filterDescriptors = filterDescriptors;
		System.out.println("Début apprentissage J48");
		String corpusARRF = corpusToARFF(path);
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
		// display classifier
		try {
			TreeVisualizer tv = new TreeVisualizer(null,
					classifier.graph(),
					new PlaceNode2());
			final javax.swing.JFrame jf = 
					new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
			jf.setSize(800,600);
			jf.getContentPane().setLayout(new BorderLayout());
			
			jf.getContentPane().add(tv, BorderLayout.CENTER);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					jf.dispose();
				}
			});
			jf.setVisible(true);
			tv.fitToScreen();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Fin apprentissage");		
	}

	public double calcValue(){
		double value = 0;
//		Instance instance = new Instance(filterDescriptors.size() + 1);
//		FastVector attributes = new FastVector(filterDescriptors.size());
//		for (int i = 0; i < filterDescriptors.size(); i++)
//			attributes.addElement(new weka.core.Attribute("desc" + i));
//		FastVector classValues = new FastVector(1);
//		classValues.addElement("Yes");
//		classValues.addElement("No");
//		attributes.addElement(new weka.core.Attribute("Symbol", classValues));
//		Instances dataset = new Instances("dataset", attributes, 0);
//		dataset.setClassIndex(attributes.size() - 1);
		Instance instance = new Instance(data.numAttributes());
		instance.setDataset(data); 
		//Leger manque de temps pour faire de l'heritage propre (un peu honteux ce code)
		for (int i = 0 ; i < filterDescriptors.size() ; i++){
			if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorSurfaceRatio.class.getName()) == 0){
				instance.setValue(i, AlphaTreeNodeDescriptorSurfaceRatio.calcValueList(listPixel));
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorHeightWidthRatio2.class.getName()) == 0){
				instance.setValue(i, AlphaTreeNodeDescriptorHeightWidthRatio2.calcValueList(listPixel));
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorSyntax.class.getName()) == 0){
				instance.setValue(i, AlphaTreeNodeDescriptorSyntax.calcValueList(listPixel));
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorArea2.class.getName()) == 0){
				instance.setValue(i, AlphaTreeNodeDescriptorArea2.calcValueList(listPixel));
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorOTeinte.class.getName()) == 0){
				instance.setValue(i, 0);
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorContourRatio.class.getName()) == 0){
				instance.setValue(i, AlphaTreeNodeDescriptorContourRatio.calcValueList(listPixel));
			}
			else if (filterDescriptors.get(i).getName().compareTo(AlphaTreeNodeDescriptorPerceptronWeka.class.getName()) == 0){
				instance.setValue(i, AlphaTreeNodeDescriptorPerceptronWeka.calcValueList(listPixel));
			}
			else
				System.out.println("ERREUR: filtre inconnu");
		}

		try {
			double[] res = classifier.distributionForInstance(instance);
			value = res[0];
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
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
			value = calcValue();

		if (value < min)
			min = value;
		if (value > max)
			max = value;

	}

	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {
		AlphaTreeNodeDescriptorDecisionTree desc = (AlphaTreeNodeDescriptorDecisionTree)descriptor;
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
			value = calcValue();

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
		return "Decision Tree";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorDecisionTree clone = new AlphaTreeNodeDescriptorDecisionTree();
		clone.listPixel = new LinkedList<PointVideo>();
		clone.listPixel.addAll(this.listPixel);
		//		clone.listPixel = this.listPixel;
		clone.nouveauPts = this.nouveauPts;
		clone.value = this.value;
		return clone;
	}

	@Override
	public int getType() {
		return AlphaTreeNodeFilterDescriptor.TYPE_DOUBLE;
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
