package testing;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;

public class Linearizer {

	private String filepath;
	private ArrayList<CoNLLHash> testSentences;
	private String featureMapf;
	private String sideMapf;
	private HashMap<String,String> featureMap;
	private HashMap<String, String> sideFeatureMap;
	
	public Linearizer(String sentenceFile, String featureMapFile, String sideFeatFile){

		this.filepath = sentenceFile;
		this.featureMapf = featureMapFile;
		this.sideMapf = sideFeatFile;
		
	}
	
	/**
	 * Creates the featureMap and sideFeatureMap from .ser files.
	 */
	public void startUp(){
		this.testSentences = CoNLLTreeConstructor.storeTreebank(filepath);
		

	      try
	      {
	         FileInputStream featureMapInputStream = new FileInputStream(featureMapf);
	         FileInputStream sideFeatMapInputStream = new FileInputStream(sideMapf);
	         
	         ObjectInputStream featMapObjectStream = new ObjectInputStream(featureMapInputStream);
	         ObjectInputStream sideFeatMapObjectStream = new ObjectInputStream(sideFeatMapInputStream);
	         
	         this.featureMap = (HashMap) featMapObjectStream.readObject();
	         this.sideFeatureMap = (HashMap) sideFeatMapObjectStream.readObject();
	         
	         featMapObjectStream.close();
	         featureMapInputStream.close();
	         sideFeatMapObjectStream.close();
	         sideFeatMapInputStream.close();
	         
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
	 * it would return "1".
	 * @param feature: String representing a specific feature.
	 * @return the index that feature corresponds to in the feature vector for ordering nodes vertically.
	 */
	public String featureExtractor(String feature){
		String featureNumber = featureMap.get(feature).split(":")[0];
		return featureNumber;
	}
	
	/**
	 * This method return the index of the given feature in the feature vector. If the feature
	 * vector is of the form [POS, Lemma, DEPREL] and this function is given "Lemma" as input,
	 * it would return "1".
	 * @param feature: String representing a specific feature.
	 * @return the index that feature corresponds to in the feature vector for ordering nodes horizontally.
	 */
	public String sideFeatureExtractor(String feature){
		String sideFeatNumber = sideFeatureMap.get(feature).split(":")[0];
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
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(thirdFeatureNumber);
			node[2].value = 1;
					
			
			String headPOS = sentence.getPOS(headID);
			String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(fourthFeatureNumber);
			node[3].value = 1;
			
			
			String childLemma = sentence.getLemma(childID);
			String fifthFeatureNumber = featureExtractor("childLemma= "+childLemma);
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(fifthFeatureNumber);
			node[4].value = 1;
			
			
			String headLemma = sentence.getLemma(headID);
			String sixthFeatureNumber = featureExtractor("headLemma= "+headLemma);
			node[5] = new svm_node();
			node[5].index = Integer.parseInt(sixthFeatureNumber);
			node[5].value = 1;
			
			svm_model model = svm.svm_load_model("linearization_svm_"+relation+".svm.model");
			
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
//		System.out.println("head is: "+sentence.getForm(headID));
//		System.out.println("child is: "+sentence.getForm(childID));
		
			svm_node[] node = new svm_node[6]; // 6 = number of features a pair of words has.
			
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
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(thirdFeatureNumber);
			node[2].value = 1;
					
			
			String headPOS = sentence.getPOS(headID);
			String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(fourthFeatureNumber);
			node[3].value = 1;
			
			
			String childLemma = sentence.getLemma(childID);
			String fifthFeatureNumber = featureExtractor("childLemma= "+childLemma);
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(fifthFeatureNumber);
			node[4].value = 1;
			
			
			String headLemma = sentence.getLemma(headID);
			String sixthFeatureNumber = featureExtractor("headLemma= "+headLemma);
			node[5] = new svm_node();
			node[5].index = Integer.parseInt(sixthFeatureNumber);
			node[5].value = 1;
			
			svm_model model = svm.svm_load_model("linearization_svm_"+relation+".svm.model");
			
			double prediction = svm.svm_predict(model, node);
			
			if(prediction<0){ // -1 means child goes after head, so .
				
				HyperNode headHype = map.get(headID);
				HyperNode childHype = map.get(childID);
				headHype.addAfter(childHype);
				

				
			} else{ // 1 means that child goes before the head. 
				
				HyperNode headHype = map.get(headID);
				HyperNode childHype = map.get(childID);
				headHype.addBefore(childHype);
				

			}
		
		
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
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(fourthFeatureNumber);
			node[3].value = 1;
			
			
			String siblingPOS = sentence.getPOS(sibling);
			String fifthFeatureNumber = sideFeatureExtractor("siblingPOS= "+siblingPOS);
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(fifthFeatureNumber);
			node[4].value = 1;
			
			
			String firstNodeLemma = sentence.getLemma(firstNode);
			String sixthFeatureNumber = sideFeatureExtractor("firstLemma= "+firstNodeLemma);
			node[5] = new svm_node();
			node[5].index = Integer.parseInt(sixthFeatureNumber);
			node[5].value = 1;
			
			
			String siblingLemma = sentence.getLemma(sibling);
			String seventhFeatureNumber = sideFeatureExtractor("siblingLemma= "+siblingLemma);
			node[6] = new svm_node();
			node[6].index = Integer.parseInt(seventhFeatureNumber);
			node[6].value = 1;
			
			
			String headPOS = sentence.getPOS(headID);
			String eigthFeatureNumber = sideFeatureExtractor("headPOS= "+headPOS);
			node[7] = new svm_node();
			node[7].index = Integer.parseInt(eigthFeatureNumber);
			node[7].value = 1;
			
			
			String headLemma = sentence.getLemma(headID);
			String ninthFeatureNumber = sideFeatureExtractor("headLemma= "+headLemma);
			node[8] = new svm_node();
			node[8].index = Integer.parseInt(ninthFeatureNumber);
			node[8].value = 1;
			
			svm_model model = svm.svm_load_model("linearization_svm_siblings.svm.model");
			
			double prediction = svm.svm_predict(model, node);
			
			if(prediction<0){ // 1 means that firstNode goes before the sibling. -1 means firstNode goes after sibling.
				// TODO: CLEAN UP
//				beforeMap.get(firstNode).add(sibling);
//				beforeMap.get(firstNode).addAll(beforeMap.get(sibling));
				return "before";
				
			} else{
				//TODO: CLEAN UP
//				beforeMap.get(sibling).add(firstNode);
//				beforeMap.get(sibling).addAll(beforeMap.get(firstNode));
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
			if(this.orderVert2(head, child, sentence, hyperNodeMap, beforeMap).equals("before")){
				beforeMap.get(child).add(head);
				beforeMap.get(child).addAll(beforeMap.get(head));
			} else if (this.orderVert2(head, child, sentence, hyperNodeMap, beforeMap).equals("after")){
				beforeMap.get(head).add(child);
				beforeMap.get(head).addAll(beforeMap.get(child));
			} 
			
			
		}
		
		this.makeTransitive(sentence, beforeMap); 
		
		Iterator<String> sideit = children.iterator(); // Orders children with respect to each other.
		while(sideit.hasNext()){
			String child = sideit.next();
			ArrayList<String> siblings = sentence.getSiblingss(child);
			Iterator<String> sibit = siblings.iterator();
			while(sibit.hasNext()){
				String sibling = sibit.next();
				if(this.sideOrder(child, sibling, sentence, hyperNodeMap, beforeMap).equals("before")){
					beforeMap.get(child).add(sibling);
					beforeMap.get(child).addAll(beforeMap.get(sibling));
				} else if (this.sideOrder(child, sibling, sentence, hyperNodeMap, beforeMap).equals("after")){
					beforeMap.get(sibling).add(child);
					beforeMap.get(sibling).addAll(beforeMap.get(child));
				}
				
			} 
			
		} // ITS ALREADY IN THE HYPERNODE FORM!!!!!!
		
		this.makeTransitive(sentence, beforeMap);
		
		ArrayList<String> properOrder = this.linearize(sentence, beforeMap); 
		ArrayList<String> finalForm = new ArrayList<String>();
//		System.out.println(beforeMap);
//		System.out.println(properOrder+"\n");
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
	
	
	public ArrayList<String> linearize(CoNLLHash sentence, HashMap<String, HashSet<String>> beforeMap){
		ArrayList<String> properOrder = new ArrayList<String>();
		Set<String> words = beforeMap.keySet();
		Iterator<String> it = words.iterator();
		while(it.hasNext()){
			String wordID = it.next();
			if(beforeMap.get(wordID).size() > properOrder.size()){
				properOrder.add(wordID);
			}else{
			properOrder.add(beforeMap.get(wordID).size(), wordID);
			}
		}
		return properOrder;
	}
	
	
		

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
				this.orderOwn(sentence, child, hyperNodeMap);
				
//				System.out.println("hyperHead is: "+childHyperNode.mainWordString());
//				System.out.println("hyperNode form is: "+childHyperNode.formString());
				if(sentence.getDeprel(child).equals("ROOT")){
					HyperNode rootHyperNode = hyperNodeMap.get(child);
					return rootHyperNode.form;
				} 
				this.orderVert1(head, child, sentence, hyperNodeMap);
			} else{
				
//				System.out.println("head: "+sentence.getForm(head));
//				System.out.println("child: "+sentence.getForm(child));
				this.orderVert1(head, child, sentence, hyperNodeMap);
//				System.out.println("hyperHead is: "+childHyperNode.mainWordString());
//				System.out.println("hyperNode form is: "+childHyperNode.formString());
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

	public static void main(String[] args) throws IOException {
		
		BufferedWriter candidateFile = new BufferedWriter(new FileWriter("candidateSentences"));
		BufferedWriter referenceFile = new BufferedWriter(new FileWriter("referenceSentences"));
		
		int sentences = 0;
		Linearizer liner = new Linearizer("znotsentences","relmap.ser","sideRelMap.ser");
		liner.startUp();
		Iterator<CoNLLHash> sentenceIt = liner.testSentences.iterator();
		while(sentenceIt.hasNext()){
			CoNLLHash sentence = sentenceIt.next();
			sentences++;
			System.out.println(sentences);
			ArrayList<String> result = liner.treeTraverser(sentence);
			
			int sentenceLength = sentence.getIds().size();
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
		
		candidateFile.close();
		referenceFile.close();
		
		String[] argss = new String[2];
		argss[0] = "referenceSentences";
		argss[1] = "candidateSentences";
		
		BleuMain.main(argss);
	}
}
