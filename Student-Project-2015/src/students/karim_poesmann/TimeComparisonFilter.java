package students.karim_poesmann;

import fr.unistra.pelican.ByteImage;

public class TimeComparisonFilter 
{
	protected int xShift, yShift, frameShift, nbFrames, precisionB, precisionXY;
	protected boolean precFrames, nextFrames;
	
	public TimeComparisonFilter()
	{
		xShift = 0; // Décalage en X du pixel à analyser
		yShift = 0; // Décalage en Y du pixel à analyser
		frameShift = 1; // Décalage d'image à analyser
		nbFrames = 1; // Nombre d'images à analyser
		precFrames = true; // Doit-on analyser les images précédentes
		nextFrames = true; // Doit-on analyser les images suivantes
		precisionB = 0; // Approximation sur chaque composante de la couleur
		precisionXY = 0; // Approximation spatiale pour la comparaison du pixel
	}
	
	public TimeComparisonFilter(int xShift, int yShift)
	{
		this();
		this.xShift = xShift;
		this.yShift = yShift;
	}
	
	public TimeComparisonFilter(int xShift, int yShift, int frameShift)
	{
		this(xShift, yShift);
		this.frameShift = frameShift;
	}
	
	public TimeComparisonFilter(int xShift, int yShift, int frameShift, int nbFrames)
	{
		this(xShift, yShift, frameShift);
		this.nbFrames = nbFrames;
	}
	
	public TimeComparisonFilter(int xShift, int yShift, int frameShift, int nbFrames, boolean precFrames, boolean nextFrames)
	{
		this(xShift, yShift, frameShift, nbFrames);
		this.precFrames = precFrames;
		this.nextFrames = nextFrames;
	}
	
	public TimeComparisonFilter(int xShift, int yShift, int frameShift, int nbFrames, boolean precFrames, boolean nextFrames, int precisionB, int precisionXY)
	{
		this(xShift, yShift, frameShift, nbFrames, precFrames, nextFrames);
		this.precisionB = precisionB;
		this.precisionXY = precisionXY;
	}
	
	public boolean isBlurCorrect(int byteSrc, int byteDst) 
	{ // retourne vrai si les composantes couleur sont identiques +- precisionB
		return (byteSrc <= byteDst + precisionB && byteSrc >= byteDst - precisionB);
	}
	
	public void filter(ByteImage input, int currentFrame)
	{ // Filtre l'image en retirant les pixels n'étant pas identiques à leurs homologues sur les images précédentes et suivantes
		for(int shift = frameShift ; shift <= nbFrames ; shift += frameShift)
		{ // On explore les frames précédentes et suivantes
			for(int i = xShift*shift ; i < input.getXDim()-(xShift*shift)-precisionXY ; i++)
			{ // On explore les pixels en x
				for(int j = yShift*shift ; j < input.getYDim()-(yShift*shift)-precisionXY ; j++)
				{ // On explore les pixels en y
					boolean correct2 = false;
					
					if(precFrames)
					{ // On analyse les images précédentes
						for(int l = 0 ; l <= precisionXY ; l++)
						{ // On analyse le voisinage du pixel visé en x
							for(int m = 0 ; m <= precisionXY ; m++)
							{ // On analyse le voisinage du pixel visé en y
								boolean correct = true;
																
								for(int k = 0; k < input.getBDim() ; k++) // On analyse chaque byte du pixel
									if(!isBlurCorrect(input.getPixelByte(i, j, 0, currentFrame, k), input.getPixelByte(i-(xShift*shift)-l, j-(yShift*shift)-m, 0, currentFrame-shift, k)))
										correct = false;
								
								if(correct) // Si au moins un pixel de la zone correspond sur toutes ses composantes au pixel de base
									correct2 = true;
							}
						}
					} else
						correct2 = true;
									
					if(nextFrames && correct2)
					{ // On analyse les images suivantes
						correct2 = false;
						for(int l = 0 ; l <= precisionXY ; l++)
						{ // On analyse le voisinage du pixel visé en x
							for(int m = 0 ; m <= precisionXY ; m++)
							{ // On analyse le voisinage du pixel visé en y
								boolean correct = true;
																
								for(int k = 0; k < input.getBDim() ; k++) // On analyse chaque byte du pixel
									if(!isBlurCorrect(input.getPixelByte(i, j, 0, currentFrame, k), input.getPixelByte(i+(xShift*shift)+l, j+(yShift*shift)+m, 0, currentFrame+shift, k)))
										correct = false;
								
								if(correct) // Si au moins un pixel de la zone correspond sur toutes ses composantes au pixel de base
									correct2 = true;
							}
						}
					}					
					
					for(int k = 0 ; k < input.getBDim() ; k++) // Si le pixel ne respecte pas le contrat, on le peint en noir
						if(!correct2)
							input.setPixelByte(i, j, 0, currentFrame, k, 0);
				}
			}
		}
	}

	public int getxShift() {
		return xShift;
	}

	public void setxShift(int xShift) {
		this.xShift = xShift;
	}

	public int getyShift() {
		return yShift;
	}

	public void setyShift(int yShift) {
		this.yShift = yShift;
	}

	public int getFrameShift() {
		return frameShift;
	}

	public void setFrameShift(int frameShift) {
		this.frameShift = frameShift;
	}

	public int getNbFrames() {
		return nbFrames;
	}

	public void setNbFrames(int nbFrames) {
		this.nbFrames = nbFrames;
	}

	public int getPrecisionB() {
		return precisionB;
	}

	public void setPrecisionB(int precisionB) {
		this.precisionB = precisionB;
	}

	public int getPrecisionXY() {
		return precisionXY;
	}

	public void setPrecisionXY(int precisionXY) {
		this.precisionXY = precisionXY;
	}

	public boolean isPrecFrames() {
		return precFrames;
	}

	public void setPrecFrames(boolean precFrames) {
		this.precFrames = precFrames;
	}

	public boolean isNextFrames() {
		return nextFrames;
	}

	public void setNextFrames(boolean nextFrames) {
		this.nextFrames = nextFrames;
	}

}