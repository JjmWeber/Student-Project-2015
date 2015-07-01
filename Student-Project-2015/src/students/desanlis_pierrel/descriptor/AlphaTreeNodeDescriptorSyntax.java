package students.desanlis_pierrel.descriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import main.alphaTree.descriptor.AlphaTreeNodeDescriptor;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;
import fr.unistra.pelican.BooleanImage;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.util.PointVideo;

/*
 * Ce descripteur réalise une empreinte de la zone et la compare à une base d'empreinte de symbole connus
 * La valeur de ce descripteur correspond à la plus faible distance de Levenstein trouvé
 * 1. On normalise la taille de la zone
 * 2. On part du point le plus a gauche a mi-hauteur 
 * 3. On parcours le contour du symbole en affectant une lettre a chaque direction prise (Nord, Nord-Est, Est...)
 * 4. La valeur du descripteur correspond a la ressemblance de la chaine avec une base de symbole
 * 
 */

public class AlphaTreeNodeDescriptorSyntax extends AlphaTreeNodeFilterDescriptor{
	private static double min=Double.POSITIVE_INFINITY;
	private static double max=Double.NEGATIVE_INFINITY;

	private LinkedList<PointVideo> listPixel = new LinkedList<PointVideo>(); // Point constituant la zone

	//Evite de reparcourir la liste a chaque fois pour connaitre la taille de l'imagie a créer
	private int minX = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxY = Integer.MIN_VALUE;

	//Parametres par defaut
	private int nouveauPts; // nouveaux points depuis le dernier calcule de la valeur
	private static double ratioMaj = 0.10; // nombre de nouveau pixel avant recalcule
	private static int rayon = 5; // Rayon du cercle utilisé pour determiner le contour
	private static int taille_x = 100; //Dimension du carré utilisé pour redimensionner l'image
	private static int taille_y = 100;
	private static LinkedList<String> base = new LinkedList<String>();	
	private static int minPix = 100;

	private int value;
	private static ArrayList<PointCercle> listCercle; // liste des points qui compose le cercle (en static et calculer qu'une fois)
	private enum Direction {haut, bas, gauche, droite};

	/*
	 * Initialisation de la base d'empreinte et des paramétres du descripteur
	 */
	public static void init (int taille_x, int taille_y, int minPix, int rayon, double ratioMaj, String path){
		AlphaTreeNodeDescriptorSyntax.taille_x = taille_x;
		AlphaTreeNodeDescriptorSyntax.taille_y = taille_y;
		AlphaTreeNodeDescriptorSyntax.ratioMaj = ratioMaj;
		AlphaTreeNodeDescriptorSyntax.rayon = rayon;	
		AlphaTreeNodeDescriptorSyntax.minPix = minPix;

		//Calcul des valeurs du cercle
		listCercle = new ArrayList<PointCercle>();
		//Bresenham(http://fr.wikipedia.org/wiki/Algorithme_de_trac%C3%A9_d'arc_de_cercle_de_Bresenham)
		int x, y, p;
		x = 0;
		y = rayon;
		AlphaTreeNodeDescriptorSyntax a = new AlphaTreeNodeDescriptorSyntax();
		listCercle.add(a.new PointCercle(0,-y));
		p = 3-(2*rayon);
		for (x = 0 ; x <= y ; x++){
			if (p<0)
				p = (p+(4*x)+6);
			else{
				y = y - 1;
				p=p+((4*(x-y)+10));
			}
			//On pourrait augmenter le nombre de lettre pour etre plus précis
			listCercle.add(a.new PointCercle(+x,-y)); //s
			listCercle.add(a.new PointCercle(-x,-y)); //s
			listCercle.add(a.new PointCercle(+x,+y)); //n 
			listCercle.add(a.new PointCercle(-x,+y)); //n
			listCercle.add(a.new PointCercle(+y,-x)); //e
			listCercle.add(a.new PointCercle(+y,+x)); //e
			listCercle.add(a.new PointCercle(-y,-x)); //o
			listCercle.add(a.new PointCercle(-y,+x)); //o	
		}

		Collections.sort(listCercle);

		//Suppression des points perdu
		for (int i = 1 ; i < listCercle.size()-1 ; i++)
			if (listCercle.get(i-1).label == listCercle.get(i+1).label && listCercle.get(i-1).label != listCercle.get(i).label && listCercle.get(i+1).label != listCercle.get(i).label)
			{
				listCercle.remove(i);
				i--;
			}
		//Suppression des doublons
		for (int i = 0 ; i < listCercle.size()-2 ; i++)
			if (listCercle.get(i).x == listCercle.get(i+1).x && listCercle.get(i).y == listCercle.get(i+1).y)
				listCercle.remove(i);

		for (PointCercle pp : listCercle)
			System.out.println(pp.x + " " + pp.y + " " + pp.angle + " " + pp.label);

		File dir = new File(path);
		File files[] = dir.listFiles();
		System.out.println("Debut de l'apprentissage");		
		int n = 0;
		for (File f : files){
			if (f.getName().compareTo(".") != 0 && f.getName().compareTo("..") != 0){
				n++;
				System.out.println("Fichier " + n + "/" + files.length + ":" + f.getName());
				LinkedList<PointVideo> list = new LinkedList<PointVideo>();
				ByteImage image = (ByteImage)ImageLoader.exec(f.getAbsolutePath());			
				for (int x2 = 0 ; x2 < image.xdim ; x2++)
					for(int y2 = 0 ; y2 < image.ydim ; y2++)		
						if (image.getPixelXYBByte(x2, y2, 0) == 0)
							list.add(new PointVideo(x2, y2, 0));
				calcValue(list, true);
			}
		}
	}

	public AlphaTreeNodeDescriptorSyntax(){}

	public static double calcValueList(LinkedList<PointVideo> listPts){
		return calcValue(listPts,false);
	}
	private static int calcValue(LinkedList<PointVideo> listPixel, boolean apprentissage){		
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		Direction direction;
		String result = "";
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

		//On redimensionne pour toujours comparer la même chose
		BooleanImage norm = new BooleanImage(taille_x,taille_y,1,1,1);
		double coeff_x = (double)taille_x/im.getXDim(); 
		double coeff_y = (double)taille_y/im.getYDim();
		for (int x = 0 ; x < taille_x ; x++)
			for (int y = 0 ; y < taille_y ; y++)
			{	

				if (im.getPixelXYBoolean((int)(Math.floor(x/coeff_x)), (int)(Math.floor(y/coeff_y))))
					norm.setPixelBoolean(x, y, 0, 0, 0, true);
			}
		int deb_x = 0, deb_y = 0; 
		int x = 0, y ,old_x=-1,old_y=-1;
		int podo = 0;
		boolean avance, tourne;
		//On part point au milieu le plus a gauche
		y = taille_y/2;
		for ( x = 0 ; x < taille_x ; x++) 
			if (norm.getPixelXYBoolean(x, y) )
			{			
				break;
			}
		x--; // on se decale a gauche pour etre a droite du mur
		deb_x = x; deb_y = y;
		old_x = x; old_y = y;


		direction = Direction.haut;
		boolean fin = false;
		while (!fin){
			avance = false;
			tourne = false;
			//Si on peu avancer on avance
			switch (direction) {
			case haut:
				if (y > 0 && x >= 0)					
					if (!norm.getPixelXYBoolean(x, y-1)){
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
					if (!norm.getPixelXYBoolean(x+1, y)){
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
					if (!norm.getPixelXYBoolean(x, y+1)){
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
					if (!norm.getPixelXYBoolean(x-1, y)){
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
					if (!norm.getPixelXYBoolean(x+1, y)){
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
					if (!norm.getPixelXYBoolean(x, y+1)){
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
					if (!norm.getPixelXYBoolean(x-1, y)){
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
					if (!norm.getPixelXYBoolean(x, y-1)){
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

			//m.a.j de la distance parcourue
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
			if (podo > rayon - 1){ // On s'approche du cercle
				//On regarde la direction qu'on a prise depuis la derniere prise de position
				for (PointCercle p : listCercle){
					if (x-old_x == p.x && y-old_y == p.y){
						result+=p.label;
						old_x = x;
						old_y = y;
						podo = 0;
					}
				}
			}
			//Test si on a fini le tour
			if (x == deb_x && y == deb_y)
				fin = true;
		}
		if (apprentissage){
			System.out.println(result);
			base.add(result);
			return -1;
		}
		else{
			int val = Integer.MAX_VALUE;
			int n;
			for (String s : base){
				n = LevenshteinDistance(result,s);
				if (n < val)
					val = n;
			}
			return val;
		}			
	}
	
	private static int LevenshteinDistance(String str1, String str2){ //http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];        
		 
        for (int i = 0; i <= str1.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= str2.length(); j++)                                 
            distance[0][j] = j;                                                  
 
        for (int i = 1; i <= str1.length(); i++)                                 
            for (int j = 1; j <= str2.length(); j++)                             
                distance[i][j] = Math.min(Math.min(distance[i - 1][j] + 1,distance[i][j - 1] + 1),                                  
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
 
        return distance[str1.length()][str2.length()];     
	}

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
		if ((double)nouveauPts/listPixel.size() > ratioMaj && listPixel.size() > minPix)
			value = calcValue(listPixel,false);
		
		if (value < min)
			min = value;
		if (value > max)
			max = value;
	}


	@Override
	public void mergeWith(AlphaTreeNodeDescriptor descriptor) {		
		AlphaTreeNodeDescriptorSyntax desc = (AlphaTreeNodeDescriptorSyntax) descriptor;
		if (desc.minX < this.minX) this.minX = desc.minX;
		if (desc.maxX > this.maxX) this.maxX = desc.maxX;
		if (desc.minY < this.minY) this.minY = desc.minY;
		if (desc.maxY > this.maxY) this.maxY = desc.maxY;

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
			value = calcValue(listPixel,false);
		
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
		return "AlphaTreeNodeDescriptorDL";
	}

	@Override
	public AlphaTreeNodeDescriptor clone() {
		AlphaTreeNodeDescriptorSyntax clone = new AlphaTreeNodeDescriptorSyntax();
//		clone.listPixel = new LinkedList<PointVideo>();
//		clone.listPixel.addAll(this.listPixel);
		clone.listPixel = this.listPixel;
		clone.nouveauPts = this.nouveauPts;
		clone.value = this.value;
		clone.maxX = this.maxX;
		clone.maxY = this.maxY;
		clone.minX = this.minX;
		clone.maxX = this.maxX;
		return clone;
	}

	@Override
	public int getType() {
		return AlphaTreeNodeDescriptor.TYPE_INT;
	}

	@Override
	public double getMin() {
		return min;
	}

	@Override
	public double getMax() {
		return max;
	}

	class PointCercle implements Comparable<Object>{
		int x, y;
		char label; //n,s,e,o
		double angle;
		public PointCercle(int x, int y){
			this.x = x;
			this.y = y;
			angle = Math.atan2((double)y, (double)x);
			if (angle < 0 )
				angle += 2*Math.PI;			
			//Affecte le label suivant l'angle
			//Si le rayon est trop petit il reste que nord,sud,est,west
			if (angle < Math.PI/6 || angle >= (11*Math.PI)/6)
				label = 'e';
			else if (angle > Math.PI/6 && angle <= Math.PI/3)
				label = 'g';
			else if (angle > Math.PI/3 && angle <= (2*Math.PI)/3)
				label = 'n';
			else if (angle > (2*Math.PI)/3 && angle <= (5*Math.PI)/6)
				label = 'm';
			else if (angle > (5*Math.PI)/6 && angle <= (7*Math.PI)/6)
				label = 'w';
			else if (angle > (7*Math.PI)/6 && angle <= (4*Math.PI)/3)
				label = 't';
			else if (angle > (4*Math.PI)/3 && angle <= (5*Math.PI)/3)
				label = 's';
			else if (angle > (5*Math.PI)/3 && angle <= (11*Math.PI)/6) 
				label = 'i';
		}		
		@Override
		public int compareTo(Object arg0) {
			if (this.angle > ((PointCercle)arg0).angle)
				return 1;
			else if (this.angle < ((PointCercle)arg0).angle)
				return -1;
			return 0;
		}
	}
}
