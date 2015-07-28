package testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import deep_to_surf.CoNLLHash;

/**
 * HyperNode is a class representing hypernodes in a dependency tree. It is used in the linearizer class to linearize a sentence.
 * @author Jose Ignacio Velarde
 *
 */

public class HyperNode {

	ArrayList<String> form;
	private boolean ordered;
	private CoNLLHash sentence;
	private String mainWord;
	
	/**
	 * 
	 * @param word: main word of the hypernode.
	 * @param sentence: CoNLLHash object representing the sentence where the main word is found.
	 * @param isOrdered: boolean representing the whether or not a hypernode is ordered.
	 */
	public HyperNode(String word, CoNLLHash sentence, boolean isOrdered){
		this.mainWord = word;
		this.form = new ArrayList<String>();
		this.form.add(word);
		this.sentence = sentence;
		
	}
	
	/**
	 * Adds the input word to the beginning of the current hypernode.
	 * @param word: word to be added to the hypernode.
	 */
	public void addBefore(String word){
		this.form.add(0, word);
	}
	
	/**
	 * Adds the input hypernode to the beginning of the current hypernode.
	 * @param node: hypernode to be added to the current hypernode:
	 */
	public void addBefore(HyperNode node){
		this.form.addAll(0, node.form);
	}
	
	/**
	 * Adds the input hypernode at the end of the current hypernode.
	 * @param word: word to be added to the hypernode
	 */
	public void addAfter(String word){
		this.form.add(word);
	}
	
	/**
	 * Adds the input hypernode at the end of the current hypernode.
	 * @param node: hypernode to be added to the hypernode
	 */
	public void addAfter(HyperNode node){
		this.form.addAll(node.form);
	}
	
	/**
	 * Marks a node as either ordered or unordered.
	 * @param order: boolean representing the state of the current hypernode. If true, then the hypernode is marked ordered.
	 */
	public void order(boolean order){
		this.ordered = order;
	}
	
	/**
	 * 
	 * @return boolean representing whether or not the hypernode is ordered.
	 */
	public boolean isOrdered(){
		return this.ordered;
	}
	
	/**
	 * 
	 * @param map: a dictionary mapping words to their hypernode objects.
	 * @return the ID of the child of the current hypernode that has not been ordered.
	 */
	public String getUncheckedChild(HashMap<String, HyperNode> map){
		ArrayList<String> children = this.sentence.getChilds(mainWord);
		Iterator<String> it = children.iterator();
		while(it.hasNext()){
			String child = it.next();
			HyperNode childHyperNode = map.get(child);
			if(!childHyperNode.isOrdered()){
				return child;
			}
		}
		return "error";
	}
	
	/**
	 * @return string representation of the hypernode, which is an array list containing all of the hypernode's elements in their predicted order. 
	 * Word IDs are shown instead of the actual words.
	 */
	public String toString(){
		return this.form.toString();
	}
	
	/**
	 * 
	 * @return string representation of the main word of the hypernode.
	 */
	public String mainWordString(){
		return this.sentence.getForm(mainWord);
	}
	
	/**
	 * 
	 * @return string representation of the hypernode, which is an array list containing all the words that are in the hypernode.
	 */
	public String formString(){
		ArrayList<String> form = this.form;
		Iterator<String> it = form.iterator();
		ArrayList<String> tempArray = new ArrayList<String>();
		while(it.hasNext()){
			String word = it.next();
			String wordForm = sentence.getForm(word);
			tempArray.add(wordForm);
		}
		return tempArray.toString();
	}
	
	/**
	 * 
	 * @param map: dictionary mapping words to their hypernode objects.
	 * @return boolean representing whether or not all of the current hypernode's children have been ordered.
	 */
	public boolean areChildrenOrdered(HashMap<String, HyperNode> map){
		ArrayList<String> children = this.sentence.getChilds(this.mainWord);
		Iterator<String> it = children.iterator();
		while(it.hasNext()){
			String child = it.next();
			HyperNode childHyperNode = map.get(child);
			if(!childHyperNode.isOrdered()){
				return false;
			}
		}
		return true;
	}
}
