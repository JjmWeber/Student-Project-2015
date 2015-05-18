package students.desanlis_pierrel.alphaTree.ui;

import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import main.alphaTree.data.AlphaTree;
import main.alphaTree.ui.AlphaTreeView;
import fr.unistra.pelican.IntegerImage;

/**
 * Class permettant de créer simplement un corpus d'apprentissage basé sur de vraie zone
 * L'utilisateur click sur une zone segmentée pour indiquer si c'est un symbole ou non
 * click gauche c'est un symbole, click droit sinon
 * Une ligne est inséré dans le corpus contenant la taille x et y de la zone, la liste
 * des points qui la compose et sa class (Oui/Non).
 * 
 * La sauvegarde dans le corpus se fait en utilisant la taille réel de la zone, la taille
 * effectivement utilisée sera définie lors de l'apprentissage (plus ou moins grande suivant
 * la précision souhaité)
 *
 */
public class AlphaTreeViewWekaTrainer extends AlphaTreeView{

	private static final long serialVersionUID = -1384072623434760608L;
	public static String path; //Emplacement du fichier contenant l'instance (ici vu que le constructeur depasse pas la premiere ligne)


	public AlphaTreeViewWekaTrainer(AlphaTree alphaTree)
			throws InstantiationException, IllegalAccessException {
		super(alphaTree);
	}

	@Override
	public void mouseClicked(MouseEvent e){
		System.out.println("Début ajout zone");
		IntegerImage img = this.getSegmentedImage();
		int val = img.getPixelXYInt(e.getX(), e.getY()); //Entier représentant la zone
		if (val != -1){
			FileWriter writer;
			try {
				writer = new FileWriter(path, true);			
				String lstPoint = "";
				int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
				LinkedList<Integer> listX = new LinkedList<Integer>();
				LinkedList<Integer> listY = new LinkedList<Integer>();
				for (int x = 0 ; x < img.xdim ; x++)
					for (int y = 0 ; y < img.ydim ; y++)
						if(img.getPixelXYInt(x, y) == val){
							listX.push(x);
							listY.push(y);
							if (x < minX)
								minX = x;
							if (x > maxX)
								maxX = x;
							if (y < minY)
								minY = y;
							if (y > maxY)
								maxY = y;
						}

				lstPoint += (maxX - minX + 1) + " ";
				lstPoint += (maxY - minY + 1) + " ";
				//lstPoint += listX.size() + " ";
				while (listX.size() > 0){ //Ecriture de la liste des points qui compose la zone
					lstPoint += (listX.removeFirst() - minX) + " ";
					lstPoint += (listY.removeFirst() - minY) + " ";
				}

				if(e.getButton() == MouseEvent.BUTTON1) //Oui
					lstPoint += "1\n";

				if(e.getButton() == MouseEvent.BUTTON3) //Non
					lstPoint += "0\n";
				
				writer.write(lstPoint);
				writer.close();
				System.out.println("Zone ajouté " + lstPoint.length());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
