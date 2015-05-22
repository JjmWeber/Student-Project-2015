package students.delesalle_kemberg;

import java.util.ArrayList;

import main.alphaTree.data.AlphaTreeNode;
import main.alphaTree.descriptor.AlphaTreeNodeFilterDescriptor;



public class Word {

	/**
	 * List of letters
	 */
	private ArrayList<AlphaTreeNode> letters=null;

	/**
	 * y gap between centers of their rectangle
	 */
	private float y_gap_threshold = Integer.MAX_VALUE;

	/**
	 * x gap between centers of their rectangle
	 */
	private float x_gap_threshold = Integer.MAX_VALUE;


	/**
	 * Check if the letter can be a letter of our word
	 * @param letter
	 * @return true if it can be a letter of our word
	 */
	public boolean checkLetter(AlphaTreeNode letter){
		float y_max_gap_allowed = 2*y_gap_threshold;
		//float x_max_gap_allowed = 2*x_gap_threshold;

		if(this.letters==null){
			return true;
		} else {
			//Compare gaps
			float y_gap = this.getZoneRectangleDescriptor(letters.get(letters.size()-2)).getCenterY() - this.getZoneRectangleDescriptor(letter).getCenterY();
			//float x_gap = this.getZoneRectangleDescriptor(letters.get(letters.size()-2)).getCenterX() - this.getZoneRectangleDescriptor(letter).getCenterX();
			if(y_gap <= y_max_gap_allowed){
				return true;

			}
		}
		return false;
	}


	/**
	 * Add a letter in the word
	 * @param letter
	 */
	public void addLetter(AlphaTreeNode letter){

		if(this.checkLetter(letter)){
			letters.add(letter);
		}

	}

	/**
	 * Return the ZoneRectangle descriptor
	 * @param letter
	 * @return the descriptor
	 */
	public AlphaTreeNodeDescriptorZoneRectangle getZoneRectangleDescriptor(AlphaTreeNode letter){
		AlphaTreeNodeFilterDescriptor[] fds = letter.getFilterDescriptors();
		for(AlphaTreeNodeFilterDescriptor fd : fds){
			if(fd.getDescriptorName() == "Zone Rectangle"){
				return (AlphaTreeNodeDescriptorZoneRectangle) fd;
			}
		}
		return null;
	}


	/**
	 * Re-check the whole word and split into several words if gaps aren't checked anymore
	 * @return List of words
	 */
	public ArrayList<Word> splitWord(){
		ArrayList<Word> wordList = new ArrayList<Word>();
		int lastWordLimit = 0;
		for(int i=1; i<letters.size(); i--){
			if(this.getZoneRectangleDescriptor(letters.get(i)).getCenterY()-this.getZoneRectangleDescriptor(letters.get(i-1)).getCenterY()>y_gap_threshold){
				Word w = new Word();
				for(int j = lastWordLimit ; j<i ; j++){
					w.addLetter(letters.get(j));
				}
				lastWordLimit = i;
				wordList.add(w);
			}
		}
		
		//for the last word
		Word w = new Word();
		for(int j = lastWordLimit ; j <= letters.size() ; j++ ){
			w.addLetter(letters.get(j));
		}
		wordList.add(w);
		
		return wordList;
	}





}
