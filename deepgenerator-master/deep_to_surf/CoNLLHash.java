/**
 * 
 */
package deep_to_surf;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Miguel Ballesteros
 * Universitat Pompeu Fabra
 * 
 * 
 * CoNLL 2009 format.
 * ID FORM LEMMA PLEMMA POS PPOS FEAT PFEAT HEAD PHEAD DEPREL PDEPREL FILLPRED PRED APREDs
 */
public class CoNLLHash {

	// Maps words to an array that has their features
    private HashMap<String, ArrayList<String>> hash;
    private HashMap<String, String> relMap;
    private HashMap<String, HashSet<String>> beforeMap; // Maps words to a list of all the words that are before it in a sentence
    
    

    public CoNLLHash() {
        hash = new HashMap<String, ArrayList<String>>();
        relMap = new HashMap<String, String>();
        beforeMap= new HashMap<String, HashSet<String>>();
        
        relMap.put("SBJ", "before");
        relMap.put("DET", "before");
        relMap.put("NMOD", "before");
        relMap.put("OBJ", "after");
        relMap.put("TITLE", "before");
        relMap.put("P", "after");
    }
    
    // Adds a word and its features to hash.
    public void addLine(String aux) {

        StringTokenizer st = new StringTokenizer(aux);
        ArrayList <String> columns = new ArrayList <String>();
        if (st.hasMoreTokens()) {
            String id = st.nextToken("\t");
            beforeMap.put(id, new HashSet<String>());
            columns.add(id);
            hash.put(id, columns);
            while (st.hasMoreTokens()) {
                columns.add(st.nextToken("\t"));
            }
        }
    }
    
    // All of the getter methods take a word's ID as input
    public String getId(String idW) {
    	
        return hash.get(idW.toString()).get(0);
    }

    public String getForm(String idW) {
    	if (hash.get(idW.toString())==null) return "";
        return hash.get(idW.toString()).get(1);
    }
    
    public String getLemma(String idW) {

        return hash.get(idW.toString()).get(2);
    }
    
    public String getPLemma(String idW) {

        return hash.get(idW.toString()).get(3);
    }
    
    public String getPOS(String idW) {

    	//System.out.println(idW);
        return hash.get(idW.toString()).get(4);
    }
    
    public String getPPOS(String idW) {

        return hash.get(idW.toString()).get(5);
    }
    
    public String getFEAT(String idW) {
    	
    	if (hash.get(idW.toString())==null) return "";
        return hash.get(idW.toString()).get(6);
    }
    
    public String getPFEAT(String idW) {

        return hash.get(idW.toString()).get(7);
    }
    
    public String getHead(String idW) {

        return hash.get(idW.toString()).get(8);
    }
    
    public String getPHead(String idW) {

        return hash.get(idW.toString()).get(9);
    }

    public String getDeprel(String idW) {
    	//if (hash.get(idW.toString())!=null)
    	//System.out.println(this.getIds());
    	return hash.get(idW.toString()).get(10);
    	//return "";
    }
    
    public String getPDeprel(String idW) {

        return hash.get(idW.toString()).get(11);
    }
    
    public String getFullSentence(){

    	return hash.toString();
    }
    
    /**
     * 
     * @return: the length of the sentence represented by the CoNLLHash object.
     */
    public int sentenceLength(){
    	return hash.size();
    }
    //THE SEMANTIC ATTRIBUTES WILL BE INCLUDED IN THE FUTURE.

    public boolean hasLine(String id) {
        return hash.containsKey(id);
        
    }

    // Returns an array list of all of the IDs in increasing order
    public ArrayList<String> getIds() {
        Set<String> keys = hash.keySet();
        ArrayList<String> keysInt = new ArrayList<String>();
        Iterator<String> it = keys.iterator();
        Integer max = 0;
        while (it.hasNext()) {
            //keysInt.add(it.next());
        	String s = it.next();
        	Integer sI = Integer.parseInt(s);
        	if (sI > max) max = sI;
        }
        for(int i=1;i<=max;i++){
        	keysInt.add(""+i);
        	
        }
        //System.out.println(keysInt);
        return keysInt;
    }
    
    public ArrayList<String> getUnsortedIds() {
        Set<String> keys = hash.keySet();
        ArrayList<String> keysInt = new ArrayList<String>();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            keysInt.add(it.next());
        }
        return keysInt;
    }
    
    // Returns an array list containing the siblings of the input
	public ArrayList<String> getSiblings(String head) {
		// TODO Auto-generated method stub
		ArrayList<String> ids=this.getIds();
		ArrayList<String> siblings=new ArrayList<String>();
		Iterator<String> it=ids.iterator();
		while(it.hasNext()) {
			String id=it.next();
			String headId=this.getHead(id);
			if (headId.equals(head)) siblings.add(id);
		}
		return siblings;
	}
	
	/**
	 * 
	 * @param wordID: ID of the word you want to get the siblings for
	 * @return an array list containing all of the words that have the same head has the input word.
	 */
	public ArrayList<String> getSiblingss(String wordID){
		ArrayList<String> ids=this.getIds();
		ArrayList<String> siblings=new ArrayList<String>();
		Iterator<String> it=ids.iterator();
		String headID = this.getHead(wordID);
		while(it.hasNext()) {
			String id=it.next();
			String testHeadID=this.getHead(id);
			if (testHeadID.equals(headID)&&!wordID.equals(id)) siblings.add(id);
		}
		return siblings;
	}
	
	/**
	 * 
	 * @param id: ID of the word whose children you want to get.
	 * @return an array list containing the ID's of the children of the input node.
	 */
	public ArrayList<String> getChilds(String id){
		ArrayList<String> childs=new ArrayList<String>();
		ArrayList<String> ids=this.getIds();
		Iterator<String> it=ids.iterator();
		while(it.hasNext()) {
			String idC=it.next();
			String headId=this.getHead(idC);
			if (headId.equals(id)) childs.add(idC);
		}
		return childs;
	}
	  
	/**
	 * 
	 * @return an ArrayList of the ID's of the nodes that don't have children.
	 */
	public ArrayList<String> getLeaves(){

		ArrayList<String> IDs = this.getIds();
		Iterator<String> it = IDs.iterator();
		ArrayList<String> leaves = new ArrayList<String>();
		while(it.hasNext()){
			String tempID = it.next();
			if(this.getChilds(tempID).size() == 0){
				leaves.add(tempID);
			}
		}
		return leaves;
	}
	
	/**
	 * 
	 * @param ID: the ID of the word whose depth you want to find.
	 * @return: returns the length of the path between the input node and its farthest descendant. Returns 0 if the node has no children.
	 */
	public int getDepth(String ID){
		HashMap<String, String> parents = new HashMap<String, String>();
		HashMap<String, Integer> depth = new HashMap<String, Integer>();
		parents.put(ID, "None");
		depth.put(ID, new Integer(0));
		Integer maxDepth = new Integer(0);
		ArrayDeque<String> queue = new ArrayDeque<String>();
		queue.addFirst(ID);
		while(!queue.isEmpty()){
			String nextNode = (String) queue.removeFirst();
			ArrayList <String> children = this.getChilds(nextNode);
			Iterator<String> it = children.iterator();
			while(it.hasNext()){
				String child = it.next();
				if(!parents.containsKey(child)){
					Integer currentDepth = depth.get(nextNode)+1;
					parents.put(child, nextNode);
					depth.put(child, currentDepth);
					queue.addLast(child);
					if(currentDepth > maxDepth){
						maxDepth = new Integer(currentDepth.intValue());
					}
				}
			}
		}
		return maxDepth.intValue();
	}
	
	/**
	 * 
	 * @return the ID of the Root of the sentence represented by the CoNLLHash object.
	 */
	public String getRoot(){
		ArrayList<String> ids = this.getIds();
		Iterator<String> idIt = ids.iterator();
		while(idIt.hasNext()){
			String word = idIt.next();
			if(this.getDeprel(word).equals("ROOT")){
				return word;
			}
		}
		return "error";
	}
	
	/**
	 * 
	 * @param wordID: ID of the word whose child you want to find.
	 * @return the ID of the descendant of the input node that is at the farthest distance from the input node.
	 */
	public String getMaxDepthChild(String wordID){
		HashMap<String, String> parents = new HashMap<String, String>();
		HashMap<String, Integer> depth = new HashMap<String, Integer>();
		parents.put(wordID, "None");
		depth.put(wordID, new Integer(0));
		Integer maxDepth = new Integer(0);
		ArrayDeque<String> queue = new ArrayDeque<String>();
		String maxDepthNode = "";
		queue.addFirst(wordID);
		while(!queue.isEmpty()){
			String nextNode = (String) queue.removeFirst();
			ArrayList <String> children = this.getChilds(nextNode);
			Iterator<String> it = children.iterator();
			while(it.hasNext()){
				String child = it.next();
				if(!parents.containsKey(child)){
					Integer currentDepth = depth.get(nextNode)+1;
					parents.put(child, nextNode);
					depth.put(child, currentDepth);
					queue.addLast(child);
					if(currentDepth > maxDepth){
						maxDepth = new Integer(currentDepth.intValue());
						maxDepthNode = child;
					}
				}
			}
		}
		return maxDepthNode;
	}
	
	/**
	 * 
	 * @return the node in the tree with the maximum depth, ie distance from the root.
	 */
	public String getMaxDepthNode(){
		HashMap<String, String> parents = new HashMap<String, String>();
		HashMap<String, Integer> depth = new HashMap<String, Integer>();
		String root = this.getRoot();
		parents.put(root, "None");
		depth.put(root, new Integer(0));
		Integer maxDepth = new Integer(0);
		ArrayDeque<String> queue = new ArrayDeque<String>();
		String maxDepthNode = "";
		queue.addFirst(root);
		while(!queue.isEmpty()){
			String nextNode = (String) queue.removeFirst();
			ArrayList <String> children = this.getChilds(nextNode);
			Iterator<String> it = children.iterator();
			while(it.hasNext()){
				String child = it.next();
				if(!parents.containsKey(child)){
					Integer currentDepth = depth.get(nextNode)+1;
					parents.put(child, nextNode);
					depth.put(child, currentDepth);
					queue.addLast(child);
					if(currentDepth > maxDepth){
						maxDepth = new Integer(currentDepth.intValue());
						maxDepthNode = child;
					}
				}
			}
		}
		return maxDepthNode;
	}
	
	public String sentenceToString(){
		int sentenceLength = this.sentenceLength();
		String sentence = "";
		for(int i=1;i<sentenceLength+1;i++){      // Creates the ideal form of the sentence, in both array-ID form, and String form.
			String word = this.getForm(""+i);
			sentence+=" "+word;
		}
		return sentence;
	}
	// returns an array list with the words in their final 
	public ArrayList<String> linearize(){
		ArrayList<String> properOrder = new ArrayList<String>();
		ArrayList<String> IDs = this.getIds();
		Iterator<String> it = IDs.iterator();
		while(it.hasNext()){
			String wordID = it.next();
			if(this.beforeMap.get(wordID).size() > properOrder.size()){
				properOrder.add(wordID);
			}else{
			properOrder.add(this.beforeMap.get(wordID).size(), wordID);
			}
		}
		return properOrder;
	}
	
	public HashMap<String, HashSet<String>> getBeforeMap(){
		return this.beforeMap;
	}
	
	// Given a set of features, feat, it returns the value of the specified subfeature, subFeat.
	public static String getSubFeat(String feats, String subFeat) {
		String id0 = "";
		StringTokenizer st = new StringTokenizer(feats);
		while(st.hasMoreTokens()) {
			String tok = st.nextToken("|");
			if (tok.startsWith(subFeat+"=")) {
				String[] split = tok.split("=");
				id0 = split[1];
			}
		}
		
		return id0;
	}
	

   
}

