package testing;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;

/**
 * Linearizer is a class that is used to linearize sentences taken from an annotated corpus. The algorithm used to linearize sentences has an asymptotic runtime
 * of O(n²). 
 * @author Jose Ignacio Velarde
 *
 */
public class Linearizer {

	private String filepath;
	private ArrayList<CoNLLHash> testSentences;
	private String featureMapf;
	private String sideMapf;
	private String sideMapDepf;
	private String sideRelListf;
	private ArrayList<String> sideRelList;
	private HashMap<String,String> featureMap;
	private HashMap<String, String> sideFeatureMap;
	private HashMap<String, String> sideFeatureMapDep;
	
	/**
	 * 
	 * @param sentenceFile: name of the file that contains the annotated sentences to be linearized.
	 * @param featureMapFile: name of the serializable file that contains the HashMap that was created to train the dependency relation classifiers.
	 * @param sideFeatFile: name of the serializeable file that contains the HashMap that was created to train the sibling classifier.
	 */
	public Linearizer(String sentenceFile, String featureMapFile, String sideFeatFile){

		this.filepath = sentenceFile;
		this.featureMapf = featureMapFile;
		this.sideMapf = sideFeatFile;
//		this.sideMapDepf = sideFeatDepFile;
//		this.sideRelListf = sideRelListFile;
		
	}
	
	/**
	 * Creates the featureMap and sideFeatureMap from serializable files.
	 */
	public void startUp(){
		this.testSentences = CoNLLTreeConstructor.storeTreebank(filepath);

	      try
	      {
	         FileInputStream featureMapInputStream = new FileInputStream(featureMapf);
	         FileInputStream sideFeatMapInputStream = new FileInputStream(sideMapf);
//	         FileInputStream sideRelListInputStream = new FileInputStream(sideRelListf);
//	         FileInputStream sideRelDepMapInputStream = new FileInputStream(sideMapDepf);
	         
	         ObjectInputStream featMapObjectStream = new ObjectInputStream(featureMapInputStream);
	         ObjectInputStream sideFeatMapObjectStream = new ObjectInputStream(sideFeatMapInputStream);
//	         ObjectInputStream sideRelListObjectStream = new ObjectInputStream(sideRelListInputStream);
//	         ObjectInputStream sideRelDepMapObjectStream = new ObjectInputStream(sideRelDepMapInputStream);
	         
	         this.featureMap = (HashMap) featMapObjectStream.readObject();
	         this.sideFeatureMap = (HashMap) sideFeatMapObjectStream.readObject();
//	         this.sideRelList = (ArrayList) sideRelListObjectStream.readObject();
//	         this.sideFeatureMapDep = (HashMap) sideRelDepMapObjectStream.readObject();
	         
	         featMapObjectStream.close();
	         featureMapInputStream.close();
	         
	         sideFeatMapObjectStream.close();
	         sideFeatMapInputStream.close();
	         
//	         sideRelListObjectStream.close();
//	         sideRelListInputStream.close();
//	         
//	         sideRelDepMapObjectStream.close();
//	         sideRelDepMapInputStream.close();
	         
	      }catch(IOException ioe)
	      {
	         ioe.printStackTrace();
	         return;
	      } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method return the index of the given feature in the feature vector. If the feature
	 * vector is of the form [POS, Lemma, DEPREL] and this function is given "Lemma" as input,
	 * it would return "1". If the feature isn't a part of a normal feature vector, it will return "none".
	 * @param feature: String representing a specific feature.
	 * @return the index that feature corresponds to in the feature vector for ordering nodes vertically.
	 */
	public String featureExtractor(String feature){
		String featureNumber = "none";
		if(featureMap.containsKey(feature)){
			featureNumber = featureMap.get(feature).split(":")[0];
		}
		return featureNumber;
	}
	
	/**
	 * This method return the index of the given feature in the feature vector. If the feature
	 * vector is of the form [POS, Lemma, DEPREL] and this function is given "Lemma" as input,
	 * it would return "1". If the feature isn't a part of a normal feature vector, it will return an empty string.
	 * @param feature: String representing a specific feature.
	 * @return the index that feature corresponds to in the feature vector for ordering nodes horizontally.
	 */
	public String sideFeatureExtractor(String feature){
		String sideFeatNumber = "";
		if(sideFeatureMap.containsKey(feature)){
			sideFeatNumber = sideFeatureMap.get(feature).split(":")[0];
		} 
		return sideFeatNumber;
	}
	
	public String sideFeatureDepExtractor(String feature){
		String sideFeatNumber = "";
		if(sideFeatureMapDep.containsKey(feature)){
			sideFeatNumber = sideFeatureMapDep.get(feature).split(":")[0];
		} 
		return sideFeatNumber;
	}
	
	/**
	 * Orders a head-child  pair of hyperNodes. This is similar to orderVert1, except that it is used as a helper function in orderOwn. 
	 * @param headID: ID of the head in a head-child pair.
	 * @param childID: ID of the child in a head-child pair.
	 * @param sentence: CoNLLHash object representing the sentence where the head-child pair is.
	 * @param map: HashMap mapping words to their hyperNode representation.
	 * @param beforeMap: HashMap mapping words to a set of the words that go before them.
	 * @return returns a string, either "before" or "after" that tells you the order of the head with respect to the child. 
	 * @throws IOException
	 */
	public String orderVert2(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		String relation = sentence.getDeprel(childID);
		if(!((beforeMap.get(headID).contains(childID))||(beforeMap.get(childID).contains(headID)))){
			
			svm_node[] node = new svm_node[6]; // 6 = number of features a pair of words has.
			
			int nonFeat = featureMap.size()+1;
			
			String numberOfChildren = ""+sentence.getChilds(headID).size();
			node[0] = new svm_node();
			node[0].index = 1;
			node[0].value = Double.valueOf(numberOfChildren).doubleValue();
			
			
			String childHeight = ""+sentence.getDepth(childID);
			node[1] = new svm_node();
			node[1].index = 2;
			node[1].value = Double.valueOf(childHeight).doubleValue();
			
			
			String childPOS = sentence.getPOS(childID);
			String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
			if(thirdFeatureNumber.equals("none")){
				node[2] = new svm_node();
				node[2].index = nonFeat;
				node[2].value = 0;
			} else{
				node[2] = new svm_node();
				node[2].index = Integer.parseInt(thirdFeatureNumber);
				node[2].value = 1;
			}		
			
			String headPOS = sentence.getPOS(headID);
			String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
			if(fourthFeatureNumber.equals("none")){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else{
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fourthFeatureNumber);
				node[3].value = 1;
			}
			
			String childLemma = sentence.getLemma(childID);
			String fifthFeatureNumber = featureExtractor("childLemma= "+childLemma);
			if(fifthFeatureNumber.equals("none")){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
			} else{
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(fifthFeatureNumber);
				node[4].value = 1;
			}
			
			String headLemma = sentence.getLemma(headID);
			String sixthFeatureNumber = featureExtractor("headLemma= "+headLemma);
			if(sixthFeatureNumber.equals("none")){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
			} else{
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(sixthFeatureNumber);
				node[5].value = 1;
			}
			
			svm_model model = svm.svm_load_model("Classifiers/linearization_svm_"+relation+".svm.model");
			
			double prediction = svm.svm_predict(model, node);
			
			if(prediction<0){ // -1 means child goes after head, so head is added to child's before map. 
				beforeMap.get(childID).add(headID);
				beforeMap.get(childID).addAll(beforeMap.get(headID));
				return "before";
				
				
			} else{ // 1 means that child goes before the head, so child is added to head's beforemap. 
				
				beforeMap.get(headID).add(childID);
				beforeMap.get(headID).addAll(beforeMap.get(childID));
				return "after";
			}
		} return "none";
	}
	
	/**
	 * Orders a head-child pair of hyperNodes. It is used when the head only has one child.
	 * @param headID: ID of the head in the head-child pair to be ordered.
	 * @param childID: ID of the child in the head-child pair to be ordered.
	 * @param sentence: CoNLLHash object representing the sentence where the head-child pair appears.
	 * @param map: HashMap mapping words to their hyperNode representations.
	 * @throws IOException
	 */
	public void orderVert1(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map) throws IOException{
		String relation = sentence.getDeprel(childID);
		
			svm_node[] node = new svm_node[6]; // 6 = number of features a pair of words has.
			
			int nonFeat = featureMap.size()+1;
			
			String numberOfChildren = ""+sentence.getChilds(headID).size();
			node[0] = new svm_node();
			node[0].index = 1;
			node[0].value = Double.valueOf(numberOfChildren).doubleValue();
			
			
			String childHeight = ""+sentence.getDepth(childID);
			node[1] = new svm_node();
			node[1].index = 2;
			node[1].value = Double.valueOf(childHeight).doubleValue();
			
			
			String childPOS = sentence.getPOS(childID);
			String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
			if(thirdFeatureNumber.equals("none")){
				node[2] = new svm_node();
				node[2].index = nonFeat;
				node[2].value = 0;
			} else{
				node[2] = new svm_node();
				node[2].index = Integer.parseInt(thirdFeatureNumber);
				node[2].value = 1;
			}		
			
			String headPOS = sentence.getPOS(headID);
			String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
			if(fourthFeatureNumber.equals("none")){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else{
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fourthFeatureNumber);
				node[3].value = 1;
			}
			
			String childLemma = sentence.getLemma(childID);
			String fifthFeatureNumber = featureExtractor("childLemma= "+childLemma);
			if(fifthFeatureNumber.equals("none")){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
			} else{
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(fifthFeatureNumber);
				node[4].value = 1;
			}
			
			String headLemma = sentence.getLemma(headID);
			String sixthFeatureNumber = featureExtractor("headLemma= "+headLemma);
			if(sixthFeatureNumber.equals("none")){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
			} else{
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(sixthFeatureNumber);
				node[5].value = 1;
			}
			
			svm_model model = svm.svm_load_model("Classifiers/linearization_svm_"+relation+".svm.model");
			
			double prediction = svm.svm_predict(model, node);
			
			if(prediction<0){ // -1 means child goes after head.
				
				HyperNode headHype = map.get(headID);
				HyperNode childHype = map.get(childID);
				headHype.addAfter(childHype);
				

				
			} else{ // 1 means that child goes before the head. 
				
				HyperNode headHype = map.get(headID);
				HyperNode childHype = map.get(childID);
				headHype.addBefore(childHype);
				

			}
		
		
	}
	
	//TODO: FIX DICTIONARY CALLS IE FEATURE EXTRACTOR AND MAKE SURE EVERYTHING IS CALLING THE RIGHT DICT.
	public String sideOrder2(String sib1, String sib2, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		if((!beforeMap.get(sib1).contains(sib2))&&(!beforeMap.get(sib2).contains(sib1))){
			
			String firstRel = sentence.getDeprel(sib1);
			String secondRel = sentence.getDeprel(sib2);
			String relPair = firstRel+secondRel;
			String firstNode = sib1;
			String sibling = sib2;
			
			if(sideRelList.contains(secondRel+firstRel+"_siblings")){
				firstNode = sib2;
				sibling = sib1;
				relPair = secondRel+firstRel;
			} else if(!sideRelList.contains(firstRel+secondRel+"_siblings")){
				return this.sideOrder(sib1, sib2, sentence, map, beforeMap);
			}
			
			String headID = sentence.getHead(sib1);
			
			svm_node[] node = new svm_node[10];
			
			int nonFeat = sideFeatureMap.size()+1;
			
			String numberOfChildren = ""+sentence.getChilds(headID).size();
			node[0] = new svm_node();
			node[0].index = 1;
			node[0].value = Double.valueOf(numberOfChildren).doubleValue();

			
			String firstNodeHeight = ""+sentence.getDepth(firstNode);
			node[1] = new svm_node();
			node[1].index = 2;
			node[1].value = Double.valueOf(firstNodeHeight).doubleValue();
			
			
			String siblingHeight = ""+sentence.getDepth(sibling);
			node[2] = new svm_node();
			node[2].index = 3;
			node[2].value = Double.valueOf(siblingHeight).doubleValue();

			
			String firstNodePOS = sentence.getPOS(firstNode);
			String fourthFeatureNumber = sideFeatureDepExtractor("firstPOS= "+firstNodePOS);
			if(fourthFeatureNumber.isEmpty()){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else{
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fourthFeatureNumber);
				node[3].value = 1;
			}
			
			String siblingPOS = sentence.getPOS(sibling);
			String fifthFeatureNumber = sideFeatureDepExtractor("siblingPOS= "+siblingPOS);
			if(fifthFeatureNumber.isEmpty()){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
			} else {
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(fifthFeatureNumber);
				node[4].value = 1;
			}
			
			String headPOS = sentence.getPOS(headID);
			String sixthFeatureNumber = sideFeatureDepExtractor("headPOS= "+headPOS);
			if(sixthFeatureNumber.isEmpty()){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
			} else{
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(sixthFeatureNumber);
				node[5].value = 1;
			}
			
			String firstNodeLemma = sentence.getLemma(firstNode);
			String seventhFeatureNumber = sideFeatureDepExtractor("firstLemma= "+firstNodeLemma);
			if(seventhFeatureNumber.isEmpty()){
				node[6] = new svm_node();
				node[6].index = nonFeat;
				node[6].value = 0;
			} else{
				node[6] = new svm_node();
				node[6].index = Integer.parseInt(seventhFeatureNumber);
				node[6].value = 1;
			}
			
			String siblingLemma = sentence.getLemma(sibling);
			String eigthFeatureNumber = sideFeatureDepExtractor("siblingLemma= "+siblingLemma);
			if(eigthFeatureNumber.isEmpty()){
				node[7] = new svm_node();
				node[7].index = nonFeat;
				node[7].value = 0;
			} else {
				node[7] = new svm_node();
				node[7].index = Integer.parseInt(eigthFeatureNumber);
				node[7].value = 1;
			}
			
			
			String headLemma = sentence.getLemma(headID);
			String ninthFeatureNumber = sideFeatureDepExtractor("headLemma= "+headLemma);
			if(ninthFeatureNumber.isEmpty()){
				node[8] = new svm_node();
				node[8].index = nonFeat;
				node[8].value = 0;
			} else{
				node[8] = new svm_node();
				node[8].index = Integer.parseInt(ninthFeatureNumber);
				node[8].value = 1;
			}
			
			
			String headRel = sentence.getDeprel(headID);
			String tenthFeatureNumber = sideFeatureDepExtractor("headRel= "+headRel);
			if(tenthFeatureNumber.isEmpty()){
				node[9] = new svm_node();
				node[9].index = nonFeat;
				node[9].value = 0;
			} else{
				node[9] = new svm_node();
				node[9].index = Integer.parseInt(tenthFeatureNumber);
				node[9].value = 1;
			}
			
			svm_model model = svm.svm_load_model("Sibling Dep Models/"+relPair+"_siblings.svm.model");
//			System.out.println(relPair+"_siblings.svm.model");
			double prediction = svm.svm_predict(model, node);
			
			if(prediction<0){ // -1 means firstNode goes after sibling.
				
				return "before";
				
			} else{ // 1 means that firstNode goes before the sibling.
				
				return "after";
			}
		} return "none";
			
	}
	
	/**
	 * Orders a pair of siblings in a dependency tree.
	 * @param firstNode: ID of the first node in a sibling pair.
	 * @param sibling: ID of the second node in a sibling pair.
	 * @param sentence: CoNLLHash object representing the sentence where the sibling pair appears.
	 * @param map: HashMap mapping words to their hyperNode representations.
	 * @param beforeMap: HashMap mapping words to a set of the words that are before it in the sentence.
	 * @return String, either "before" or "after" that tells you the order of the first node with respect to its sibling.
	 * @throws IOException
	 */
	public String sideOrder(String firstNode, String sibling, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		if ((!beforeMap.get(sibling).contains(firstNode))&&(!beforeMap.get(firstNode).contains(sibling))){
			String headID = sentence.getHead(firstNode);
			
			svm_node[] node = new svm_node[9];
			
			int nonFeat = sideFeatureMap.size()+1;
			
			String numberOfChildren = ""+sentence.getChilds(headID).size();
			node[0] = new svm_node();
			node[0].index = 1;
			node[0].value = Double.valueOf(numberOfChildren).doubleValue();

			
			String firstNodeHeight = ""+sentence.getDepth(firstNode);
			node[1] = new svm_node();
			node[1].index = 2;
			node[1].value = Double.valueOf(firstNodeHeight).doubleValue();
			
			
			String siblingHeight = ""+sentence.getDepth(sibling);
			node[2] = new svm_node();
			node[2].index = 3;
			node[2].value = Double.valueOf(siblingHeight).doubleValue();

			
			String firstNodePOS = sentence.getPOS(firstNode);
			String fourthFeatureNumber = sideFeatureExtractor("firstPOS= "+firstNodePOS);
			if(fourthFeatureNumber.isEmpty()){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else{
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fourthFeatureNumber);
				node[3].value = 1;
			}
			
			String siblingPOS = sentence.getPOS(sibling);
			String fifthFeatureNumber = sideFeatureExtractor("siblingPOS= "+siblingPOS);
			if(fifthFeatureNumber.isEmpty()){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
			} else {
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(fifthFeatureNumber);
				node[4].value = 1;
			}
			
			String firstNodeLemma = sentence.getLemma(firstNode);
			String sixthFeatureNumber = sideFeatureExtractor("firstLemma= "+firstNodeLemma);
			if(sixthFeatureNumber.isEmpty()){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
			} else{
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(sixthFeatureNumber);
				node[5].value = 1;
			}
			
			String siblingLemma = sentence.getLemma(sibling);
			String seventhFeatureNumber = sideFeatureExtractor("siblingLemma= "+siblingLemma);
			if(seventhFeatureNumber.isEmpty()){
				node[6] = new svm_node();
				node[6].index = nonFeat;
				node[6].value = 0;
			} else {
				node[6] = new svm_node();
				node[6].index = Integer.parseInt(seventhFeatureNumber);
				node[6].value = 1;
			}
			
			String headPOS = sentence.getPOS(headID);
			String eigthFeatureNumber = sideFeatureExtractor("headPOS= "+headPOS);
			if(eigthFeatureNumber.isEmpty()){
				node[7] = new svm_node();
				node[7].index = nonFeat;
				node[7].value = 0;
			} else{
				node[7] = new svm_node();
				node[7].index = Integer.parseInt(eigthFeatureNumber);
				node[7].value = 1;
			}
			
			String headLemma = sentence.getLemma(headID);
			String ninthFeatureNumber = sideFeatureExtractor("headLemma= "+headLemma);
			if(ninthFeatureNumber.isEmpty()){
				node[8] = new svm_node();
				node[8].index = nonFeat;
				node[8].value = 0;
			} else{
				node[8] = new svm_node();
				node[8].index = Integer.parseInt(ninthFeatureNumber);
				node[8].value = 1;
			}
			
//			svm_model model = svm.svm_load_model("Classifiers/linearization_svm_siblings.svm.model");
			svm_model model = svm.svm_load_model("new_siblings.svm.model");
			
			double prediction = svm.svm_predict(model, node);
			
			if(prediction<0){ // -1 means firstNode goes after sibling.
				
				return "before";
				
			} else{ // 1 means that firstNode goes before the sibling.
				
				return "after";
			}
			
		} return "none";
	}
	
	/**
	 * Makes beforeMap fully transitive.
	 * @param sentence: CoNLLHash object representing the sentence where the words to be made transitive appear.
	 * @param beforeMap: HashMap mapping words to a set of the words that go before it in a sentence.
	 */
	public void makeTransitive(CoNLLHash sentence, HashMap<String, HashSet<String>> beforeMap){
		for(String wordID : beforeMap.keySet()){
			ArrayList<String> tempArray = new ArrayList<String>();
			HashSet<String> wordsBeforeWord = beforeMap.get(wordID);
			Iterator<String> it = wordsBeforeWord.iterator();
			while(it.hasNext()){
				String wordBefore = it.next();
				tempArray.addAll(beforeMap.get(wordBefore));
			}
			beforeMap.get(wordID).addAll(tempArray);
		}
	}
	
	/**
	 * Orders a hyperNode and its children.
	 * @param sentence: CoNLLHash object representing the sentence where the hyperNodes to be ordered appear.
	 * @param head: ID of the main word in the hyperNode to be ordered.
	 * @param hyperNodeMap: HashMap mapping words to their hyperNode representations.
	 * @throws IOException
	 */
	public void orderOwn(CoNLLHash sentence, String head, HashMap<String, HyperNode> hyperNodeMap) throws IOException{
		ArrayList<String> children = sentence.getChilds(head);
//		System.out.println("head: "+head);
//		System.out.println("children: "+children.toString());
		HashMap<String, HashSet<String>> beforeMap = new HashMap<String, HashSet<String>>();
		beforeMap.put(head, new HashSet<String>());
		
		Iterator<String> it = children.iterator(); // Initializes the beforeMap. Words --> Set of words before them
		while(it.hasNext()){
			String child = it.next();
			beforeMap.put(child, new HashSet<String>());
		}
		
		Iterator<String> otherIt = children.iterator(); // Orders every child and its parent
		while(otherIt.hasNext()){
			String child = otherIt.next();
			String headWRTChild = this.orderVert2(head, child, sentence, hyperNodeMap, beforeMap); // headWRTChild = head with respect to child, ie before or after.
			if(headWRTChild.equals("before")){
				beforeMap.get(child).add(head);
				beforeMap.get(child).addAll(beforeMap.get(head));
//				System.out.println(sentence.getForm(head)+" goes before "+sentence.getForm(child));
//				System.out.println(head+" goes before "+child);
			} else if (headWRTChild.equals("after")){
				beforeMap.get(head).add(child);
				beforeMap.get(head).addAll(beforeMap.get(child));
//				System.out.println(sentence.getForm(child)+" goes before "+sentence.getForm(head));
//				System.out.println(child+" goes before "+head);
			} 
			
			
		}
		
		this.makeTransitive(sentence, beforeMap); 
//		System.out.println("before sibit: "+ beforeMap.toString());
		
		Iterator<String> sideit = children.iterator(); // Orders children with respect to each other.
		while(sideit.hasNext()){
			String child = sideit.next();
			ArrayList<String> siblings = sentence.getSiblingss(child);
			Iterator<String> sibit = siblings.iterator();
			while(sibit.hasNext()){
				String sibling = sibit.next();
				String result = this.sideOrder(child, sibling, sentence, hyperNodeMap, beforeMap);
				if(result.equals("before")){
					
					beforeMap.get(child).add(sibling);
					beforeMap.get(child).addAll(beforeMap.get(sibling));
					this.makeTransitive(sentence, beforeMap);
//					System.out.println(sibling+" goes before "+child+" "+beforeMap.toString());
				} else if (result.equals("after")){
					
					beforeMap.get(sibling).add(child);
					beforeMap.get(sibling).addAll(beforeMap.get(child));
					this.makeTransitive(sentence, beforeMap);
//					System.out.println(child+" goes before "+sibling+" "+beforeMap.toString());
				}
				
			} 
			
		} 
		
		this.makeTransitive(sentence, beforeMap);
		
//		System.out.println(beforeMap.toString());
		
		ArrayList<String> properOrder = this.linearize(sentence, beforeMap); 
		ArrayList<String> finalForm = new ArrayList<String>();
//		System.out.println(properOrder.toString()+"\n");
		Iterator<String> wordIt = properOrder.iterator();
		while(wordIt.hasNext()){ // Replaces all the heads of the hypernodes with all of their surrounding nodes. So for example, if dog is the head of a hypernode of the form 
			String word = wordIt.next(); // [the,dog], and proper order contains [dog, ran, away], dog would be replaced in that array with [the, dog, ran, away]. 
			if(word.equals(head)){
				finalForm.add(head);
			}else{
			HyperNode wordHyperNode = hyperNodeMap.get(word);
			ArrayList<String> wordBubble = wordHyperNode.form;
			finalForm.addAll(wordBubble);
			
			}
			
		}
		HyperNode headHyperNode = hyperNodeMap.get(head);
		headHyperNode.form = finalForm;
		}
	
	/**
	 * This is a helper method for order own, it linearizes a set of words given which word goes before each one.
	 * @param sentence: CoNLLHash object representing the sentence the words to be linearized appear in.
	 * @param beforeMap: HashMap mapping words to a set containing the words that come before them.
	 * @return an ArrayList of the words in their predicted order.
	 */
	public ArrayList<String> linearize(CoNLLHash sentence, HashMap<String, HashSet<String>> beforeMap){
		ArrayList<String> properOrder = new ArrayList<String>();
		for(int i=0;i<beforeMap.size();i++){
			properOrder.add(" ");
			
		}
		Set<String> words = beforeMap.keySet();
		Iterator<String> it = words.iterator();
		while(it.hasNext()){
			String wordID = it.next();
			properOrder.set(beforeMap.get(wordID).size(), wordID);
//			if(beforeMap.get(wordID).size() > properOrder.size()){
//				properOrder.add(wordID);
//			}else{
//			properOrder.add(beforeMap.get(wordID).size(), wordID);
//			}
		}
		return properOrder;
	}

	/**
	 * This method is the main method used to traverse and linearize the dependency tree.
	 * @param sentence: CoNLLHash object representing the sentence to be linearized.
	 * @return an ArrayList containing the predicted order of the words in the sentence.
	 * @throws IOException
	 */
	public ArrayList<String> treeTraverser(CoNLLHash sentence) throws IOException{
		HashMap<String, HyperNode> hyperNodeMap = new HashMap<String, HyperNode>();
		ArrayList<String> words = sentence.getIds();
		Iterator<String> wordIt = words.iterator();
		String maxDepthNode = sentence.getMaxDepthNode();
		ArrayDeque<String> queue = new ArrayDeque<String>();
		queue.addFirst(maxDepthNode);
		while(wordIt.hasNext()){
			String word = wordIt.next();
			HyperNode wordHyperNode = new HyperNode(word, sentence, false);
			hyperNodeMap.put(word, wordHyperNode);
		}
		
		while(!queue.isEmpty()){
			String child = queue.removeFirst();
			String head = sentence.getHead(child);
			HyperNode childHyperNode = hyperNodeMap.get(child);
//			System.out.println("child is: "+childHyperNode.mainWordString());
			HyperNode headHyperNode = hyperNodeMap.get(head);
//			 System.out.println("head is: "+headHyperNode.mainWordString()+"\n");
			childHyperNode.order(true);
			if(sentence.getChilds(child).size()>1){
//				System.out.println("Head: "+childHyperNode.mainWordString());
				this.orderOwn(sentence, child, hyperNodeMap);
				
//				System.out.println("hyperHead is: "+childHyperNode.mainWordString());           // PRINT STATEMENTS HERE
//				System.out.println("hyperNode form is: "+childHyperNode.formString()+"\n");     // PRINT STATEMENTS HERE TOO
				if(sentence.getDeprel(child).equals("ROOT")){
					HyperNode rootHyperNode = hyperNodeMap.get(child);
					return rootHyperNode.form;
				} 
				this.orderVert1(head, child, sentence, hyperNodeMap);
			} else{
				
				if(sentence.getDeprel(child).equals("ROOT")){
					HyperNode rootHyperNode = hyperNodeMap.get(child);
					return rootHyperNode.form;
				}
//				System.out.println("head: "+sentence.getForm(head));
//				System.out.println("child: "+sentence.getForm(child));
				this.orderVert1(head, child, sentence, hyperNodeMap);
//				System.out.println("hyperHead is: "+childHyperNode.mainWordString());
//				System.out.println("hyperNode form is: "+childHyperNode.formString()+"\n");
			}
			
			if(headHyperNode.areChildrenOrdered(hyperNodeMap)){
				queue.addLast(head);
			}else{
				
				String uncheckedChild = headHyperNode.getUncheckedChild(hyperNodeMap);
				if(sentence.getDepth(uncheckedChild)>0){
					String deepestChild = sentence.getMaxDepthChild(uncheckedChild);
					queue.addLast(deepestChild);
				}else{
					queue.addLast(uncheckedChild);
				}
				
			}
			
			
		}
		
		return new ArrayList<String>();
	}

	/**
	 * This method is used to get all of the annotated sentences into CoNLLHash object form and they are then linearized. Their gold standard form is also given, 
	 * and the predicted linearized form is then used to compute the BLEU score for the linearizer.
	 * @param args: takes no arguments.
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		BufferedWriter candidateFile = new BufferedWriter(new FileWriter("candidateSentences"));
		BufferedWriter referenceFile = new BufferedWriter(new FileWriter("referenceSentences"));
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println("Start Time: "+sdf.format(cal.getTime())+"\n");
		
		int sentences = 0;
		Linearizer liner = new Linearizer("Sentences/Evaluation Set","relmap.ser","sideRelMapDep.ser");
		liner.startUp();
		
		Iterator<CoNLLHash> sentenceIt = liner.testSentences.iterator();
		while(sentenceIt.hasNext()){
			CoNLLHash sentence = sentenceIt.next();
			sentences++;
			System.out.println(sentences);                                
			int sentenceLength = sentence.getIds().size();
			if(sentenceLength>1){
				ArrayList<String> result = liner.treeTraverser(sentence);


				ArrayList<String> idealResult = new ArrayList<String>();

				String goldStandardForm = "";
				String predictionFormm = "";

				for(int i=1;i<sentenceLength+1;i++){      // Creates the ideal form of the sentence, in both array-ID form, and String form.
					idealResult.add(""+i);
					String word = sentence.getForm(""+i);
					goldStandardForm+=" "+word;

				}
				for(int i=0;i<sentenceLength;i++){                        // Converts the predicted sentence into String form.
					String prediction = result.get(i);
					String predForm = sentence.getForm(prediction);
					predictionFormm+=" "+predForm;
				}

				System.out.println(goldStandardForm);
				System.out.println(predictionFormm+"\n");

				candidateFile.write(predictionFormm+"\n");
				referenceFile.write(goldStandardForm+"\n");


			}
		}
			candidateFile.close();
			referenceFile.close();

			String[] argss = new String[2];
			argss[0] = "referenceSentences";
			argss[1] = "candidateSentences";

			BleuMain.main(argss);
			
			Calendar endCal = Calendar.getInstance();
			SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
			System.out.println("End Time: "+sdf1.format(endCal.getTime()));
		}
	}
