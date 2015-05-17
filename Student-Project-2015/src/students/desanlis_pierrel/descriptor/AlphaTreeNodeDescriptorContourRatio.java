package students.desanlis_pierrel.descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.text.StyledEditorKit.BoldAction;

import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import fr.unistra.pelican.util.PointVideo;
import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;

/**
 * 2. On calcul la longueur du contour
 * 3. La valeur correspond au ratio Longeur du contour/Nombre de pixel
 * 
 */
public class AlphaTreeNodeDescriptorContourRatio extends AlphaTreeNodeFilterDescriptor{
	private static double min=Double.POSITIVE_INFINITY;
	private static double max=Double.NEGATIVE_INFINITY;

	private LinkedList<PointVideo> listPixel = new LinkedList<PointVideo>();

	//Evite de reparcourir la liste a chaque fois pour connaitre la taille de l'imagie a créer
	private int minX = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;

	private enum Direction {haut, bas, gauche, droite};
	private int nouveauPts = 0;
	private double ratioMaj = 0.10; //ratio de nouveau pts avant de proceder à une maj de la valeur
	private double value = 0;
	
	@Override
	public boolean check(double minValue, double maxValues) {
		double value = getValue();
		if (value >= minValue && value <= maxValues)
			return true;
		return false;
	}

	@Override
	public void addPixel(int[] values, PointVideo coord) {
		if (coord.x < minX)
			minX = coord.x;
		if (coord.x > maxX)
			maxX = coord.x;
		if (coord.y < minY)
			minY = coord.y;
		if (coord.y > maxY)
			maxY = coord.y;


		listPixel.add(coord);
		nouveauPts++;

		if ((double)nouveauPts/listPixel.size() > ratioMaj){			
			value = calcValue(listPixel); 
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}

	}


	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {		
		AlphaTreeNodeDescriptorContourRatio desc = (AlphaTreeNodeDescriptorContourRatio) descriptor;
		if (desc.minX < this.minX) this.minX = desc.minX;
		if (desc.maxX > this.maxX) this.maxX = desc.maxX;
		if (desc.minY < this.minY) this.minY = desc.minY;
		if (desc.maxY > this.maxY) this.maxY = desc.maxY;


		//Vers la fin il arrive qu'une très grande zone fusionne avec une toute petite, dans ce cas
		//on ajoute la petite a la grande
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
			nouveauPts+= desc.listPixel.size();
		}

		if ((double)nouveauPts/listPixel.size() > ratioMaj)
		{
			value = calcValue(listPixel); 
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}

	}

	public static double calcValueList(LinkedList<PointVideo> listPts){
		return calcValue(listPts);
	}
	
	private static double calcValue(LinkedList<PointVideo> listPixel){
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		if (listPixel.size() == 0)
			return 0;
		else
		{
			for (PointVideo p : listPixel){ //TODO trouver pq les valeur se mettent pas a jour
				if (p.x < minX) minX = p.x;
				if (p.x > maxX) maxX = p.x;
				if (p.y < minY) minY = p.y;
				if (p.y > maxY) maxY = p.y;
			}

			BooleanImage im = new BooleanImage(maxX-minX + 1, maxY-minY+1,1,1,1);
			for (PointVideo p : listPixel){
				im.setPixelBoolean(p.x-minX, p.y-minY, 0, 0, 0, true);
			}
			int taille_x = im.getXDim();
			int taille_y = im.getYDim();		

			int deb_x = 0, deb_y = 0; 
			int x = 0, y; 
			int podo = 0;
			boolean avance, tourne;
			Direction direction;
			//On part point au milieu le plus a gauche
			y = taille_y/2;
			for ( x = 0 ; x < taille_x ; x++) 
				if (im.getPixelXYBoolean(x, y) )
				{			
					break;
				}
			x--; // on se decale a gauche pour etre a droite du "mur"
			deb_x = x; deb_y = y;
			direction = Direction.haut;

			boolean fin = false;
			while (!fin){
				avance = false;
				tourne = false;
				//Si on peu avancer on avance
				//(surement moyen de rendre les swtich moins moche)
				switch (direction) {
				case haut:
					if (y > 0 && x >= 0)					
						if (!im.getPixelXYBoolean(x, y-1)){
							y--; avance=true;
							break;
						}
					if (y == 0 || x < 0){ //on est en haut -> forcement y'a rien pr bloquer
						y--;avance = true;
						break;
					}
					break;
				case droite:
					if (x < taille_x-1 && y >= 0)
						if (!im.getPixelXYBoolean(x+1, y)){
							x++; avance=true;
							break;
						}
					if (x == taille_x-1 || y < 0){
						x++;avance = true;
						break;
					}
					break;
				case bas:
					if (y < taille_y-1 && x < taille_x)
						if (!im.getPixelXYBoolean(x, y+1)){
							y++; avance=true;
							break;
						}
					if (y == taille_y-1 || x == taille_x){
						y++;avance=true;
						break;
					}
					break;
				case gauche:
					if (x > 0 && y < taille_y)
						if (!im.getPixelXYBoolean(x-1, y)){
							x--; avance=true;
							break;
						}
					if (x == 0 || y == taille_y){
						x--;avance=true;
						break;
					}
				default:
					break;
				}


				// si pas de mur a droite on tourne
				switch (direction) {
				case haut:
					if (y < taille_y && y >= 0){
						if (!im.getPixelXYBoolean(x+1, y)){
							direction = Direction.droite;
							tourne = true;
							break;
						}
					}else{ //Fatalement en dehors de l'image y'a pas de mur
						direction = Direction.droite;tourne = true;
						break;
					}				
					break;
				case droite:
					if (x >= 0 && x < taille_x){
						if (!im.getPixelXYBoolean(x, y+1)){
							direction = Direction.bas;tourne = true;
							break;
						}
					}else{
						direction = Direction.bas;tourne = true;
						break;
					}				
					break;
				case bas:
					if (y < taille_y && y >= 0){
						if (!im.getPixelXYBoolean(x-1, y)){
							direction = Direction.gauche;tourne = true;
							break;
						}
					}else{
						direction = Direction.gauche;tourne = true;
						break;
					}
					break;			
				case gauche:
					if (x >= 0 && x < taille_x){
						if (!im.getPixelXYBoolean(x, y-1)){
							direction = Direction.haut;tourne = true;
							break;
						}
					}else{
						direction = Direction.haut;tourne = true;
						break;
					}				
					break;
				default:
					break;
				}

				if (avance){
					podo++;
				}

				if (!avance && !tourne){
					switch (direction) {
					case haut:
						direction = Direction.gauche;
						break;
					case droite:
						direction = Direction.haut;
						break;
					case bas:
						direction = Direction.droite;
						break;
					case gauche:
						direction = Direction.bas;
						break;
					default:
						break;
					}
				}			
				//Test si on a fini le tour
				if (x == deb_x && y == deb_y)
					fin = true;

			}
			return (double)podo/(taille_x*taille_y);
		}
	}
	@Override
	public double getValue() {
		return value;
	}


	@Override
	public String getDescriptorName() {
		return "Contour Ratio";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorContourRatio clone = new AlphaTreeNodeDescriptorContourRatio();
//		clone.listPixel = new LinkedList<PointVideo>();
//		clone.listPixel.addAll(this.listPixel);
		clone.listPixel = this.listPixel;
		clone.maxX = this.maxX;
		clone.maxY = this.maxY;
		clone.minX = this.minX;
		clone.maxX = this.maxX;
		clone.value = this.value;
		clone.nouveauPts = this.nouveauPts;
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
