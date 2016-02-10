package testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import deep_surf_svm_models.ModelLinearization;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.ComputeLogProbabilityOfTextStream;
import edu.berkeley.nlp.lm.io.LmReaders;

/**
 * Linearizer is a class that is used to linearize sentences taken from an annotated corpus. The algorithm used to linearize sentences has an asymptotic runtime
 * of O(n²). 
 * @author Jose Ignacio Velarde
 *
 */
public class Linearizer {

	
//	TODO: INVESTIGATE NEW_SIBLING CLASSIFIER, TEST AGAIN
	private String filepath;
	private ArrayList<CoNLLHash> testSentences;
	private String featureMapf;
	private String sideMapf;
	private String sideMapDepf;
	private String sideRelListf;
	private String clusterf;
	private ArrayList<String> sideRelList;
	private HashMap<String,String> featureMap;
	private HashMap<String, String> sideFeatureMap;
	private HashMap<String, String> sideFeatureMapDep;
	private HashMap<String, String> clusterDict;
	
	/**
	 * 
	 * @param sentenceFile: name of the file that contains the annotated sentences to be linearized.
	 * @param featureMapFile: name of the serializable file that contains the HashMap that was created to train the dependency relation classifiers.
	 * @param sideFeatFile: name of the serializeable file that contains the HashMap that was created to train the sibling classifier.
	 */
	public Linearizer(String sentenceFile, String featureMapFile, String sideFeatFile, String sideFeatDictDep, String sideRelListFile){

		this.filepath = sentenceFile;
		this.featureMapf = featureMapFile;
		this.sideMapf = sideFeatFile;
		this.sideMapDepf = sideFeatDictDep;
//		this.sideMapDepf = sideFeatDepFile;
		this.sideRelListf = sideRelListFile;
		
	}
	public Linearizer(String sentenceFile, String featureMapFile, String sideFeatFile, HashMap<String,String>dict){

		this.filepath = sentenceFile;
		this.featureMapf = featureMapFile;
		this.sideMapf = sideFeatFile;
		this.clusterDict = dict;
//		this.sideMapDepf = sideFeatDepFile;
//		this.sideRelListf = sideRelListFile;
		
	}

	
	public Linearizer(String sentenceFile, String featureMapFile, String sideFeatFile, String clusterFile){

		this.filepath = sentenceFile;
		this.featureMapf = featureMapFile;
		this.sideMapf = sideFeatFile;
		this.clusterf = clusterFile;
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
	         FileInputStream clusterMapInputStream = new FileInputStream(clusterf);
//	         FileInputStream sideRelListInputStream = new FileInputStream(sideRelListf);
//	         FileInputStream sideRelDepMapInputStream = new FileInputStream(sideMapDepf);
	         
	         ObjectInputStream featMapObjectStream = new ObjectInputStream(featureMapInputStream);
	         ObjectInputStream sideFeatMapObjectStream = new ObjectInputStream(sideFeatMapInputStream);
	         ObjectInputStream clusterMapObjectStream = new ObjectInputStream(clusterMapInputStream);
//	         ObjectInputStream sideRelListObjectStream = new ObjectInputStream(sideRelListInputStream);
//	         ObjectInputStream sideRelDepMapObjectStream = new ObjectInputStream(sideRelDepMapInputStream);
	         
	         this.featureMap = (HashMap) featMapObjectStream.readObject();
	         this.sideFeatureMap = (HashMap) sideFeatMapObjectStream.readObject();
	         this.clusterDict = (HashMap) clusterMapObjectStream.readObject();
//	         this.sideRelList = (ArrayList) sideRelListObjectStream.readObject();
//	         this.sideFeatureMapDep = (HashMap) sideRelDepMapObjectStream.readObject();
	         
	         featMapObjectStream.close();
	         featureMapInputStream.close();
	         
	         sideFeatMapObjectStream.close();
	         sideFeatMapInputStream.close();
	         
	         clusterMapObjectStream.close();
	         clusterMapInputStream.close();
//	         sideRelListObjectStream.close();
//	         sideRelListInputStream.close();
	         
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
	
	public void orderVert1Complex(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map) throws IOException{
		String relation = sentence.getDeprel(childID);
		
		svm_node[] node = new svm_node[5]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		
		String childForm = sentence.getForm(childID);
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		String headForm = sentence.getForm(headID);
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		String headChildren = ""+sentence.getChilds(headID).size();
		String childChildren = ""+sentence.getChilds(childID).size();
		String firstFeatureNumber = featureExtractor("headWeight= "+headChildren+" childWeight= "+childChildren);
		if(firstFeatureNumber.equals("none")){
			node[4] = new svm_node();
			node[4].index = nonFeat;
			node[4].value = 0;
		} else{
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(firstFeatureNumber);
			node[4].value = 1;
		}	
		
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/1000 Cluster POS Complex/Classifiers/"+relation+".svm.model");
		
		double prediction = svm.svm_predict(model, node);
		
		Integer headint = new Integer(headID);
		Integer childint = new Integer(childID);
		
		if(prediction<0){ // -1 means child goes after head.
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addAfter(childHype);
			
//			if(childint<headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

			
		} else{ // 1 means that child goes before the head. 
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addBefore(childHype);
			
//			if(childint>headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

		}
	}
	
	public void orderVert1POS(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map) throws IOException{
		String relation = sentence.getDeprel(childID);
		svm_node[] node = new svm_node[2]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/POS/Classifiers/"+relation+".svm.model");
		
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
	
	public void orderVert1LM(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map) throws IOException{
		String relation = sentence.getDeprel(childID);
		String headForm = sentence.getForm(headID);
		String childForm = sentence.getForm(childID);
		
		svm_node[] node = new svm_node[5]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		
		
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		
		NgramLanguageModel<String>  lm;
		lm = LmReaders.readLmBinary("eng.blm");
		String option1 = childForm+" "+headForm;
		List<String> list= Arrays.asList(option1.trim().split("\\s+"));
		float logProb1 = lm.scoreSentence(list);
	
		String option2 = childForm+" "+headForm;
		List<String> list2 = Arrays.asList(option2.trim().split("\\s+"));
		float logProb2 = lm.scoreSentence(list2);
//		
		
		if(logProb1 > logProb2){ // child goes before head
			node[4] = new svm_node();
			node[4].index = new Integer(1);
			node[4].value = 1;
			
		} else{
			node[4] = new svm_node();
			node[4].index = new Integer(1);
			node[4].value = 0;
		}
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/1000 Cluster POS + lm/Classifiers/"+relation+".svm.model");
		
		double prediction = svm.svm_predict(model, node);
		
		Integer headint = new Integer(headID);
		Integer childint = new Integer(childID);
		
		if(prediction<0){ // -1 means child goes after head.
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addAfter(childHype);
			
//			if(childint<headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

			
		} else{ // 1 means that child goes before the head. 
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addBefore(childHype);
			
//			if(childint>headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

		}
	}
	
	public void orderVert1Single(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map) throws IOException{
		
		svm_node[] node = new svm_node[5]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		
		String childForm = sentence.getForm(childID);
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		String headForm = sentence.getForm(headID);
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		
		String childRel = sentence.getDeprel(childID);
		String ninthFeatureNumber = featureExtractor("childRel= "+childRel);
		if(ninthFeatureNumber.equals("none")){
			node[4] = new svm_node();
			node[4].index = nonFeat;
			node[4].value = 0;
		} else{
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(ninthFeatureNumber);
			node[4].value = 1;
		}
		
		svm_model model = svm.svm_load_model("December Vertical Files/vertical.svm.model");
		
		double prediction = svm.svm_predict(model, node);
		
		Integer headint = new Integer(headID);
		Integer childint = new Integer(childID);
		
		if(prediction<0){ // -1 means child goes after head.
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addAfter(childHype);
			
//			if(childint<headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

			
		} else{ // 1 means that child goes before the head. 
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addBefore(childHype);
			
//			if(childint>headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

		}
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
	public void orderVert1Cluster(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map) throws IOException{
		String relation = sentence.getDeprel(childID);
		
		svm_node[] node = new svm_node[4]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
//		String numberOfChildren = ""+sentence.getWeight(headID);
//		node[0] = new svm_node();
//		node[0].index = 1;
//		node[0].value = Double.valueOf(numberOfChildren).doubleValue();
//		
//		
//		String childHeight = ""+sentence.getWeight(childID);
//		node[1] = new svm_node();
//		node[1].index = 2;
//		node[1].value = Double.valueOf(childHeight).doubleValue();
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
//		String childLemma = sentence.getLemma(childID);
//		String fifthFeatureNumber = featureExtractor("childLemma= "+childLemma);
//		if(fifthFeatureNumber.equals("none")){
//			node[2] = new svm_node();
//			node[2].index = nonFeat;
//			node[2].value = 0;
//		} else{
//			node[2] = new svm_node();
//			node[2].index = Integer.parseInt(fifthFeatureNumber);
//			node[2].value = 1;
//		}
//		
//		String headLemma = sentence.getLemma(headID);
//		String sixthFeatureNumber = featureExtractor("headLemma= "+headLemma);
//		if(sixthFeatureNumber.equals("none")){
//			node[3] = new svm_node();
//			node[3].index = nonFeat;
//			node[3].value = 0;
//		} else{
//			node[3] = new svm_node();
//			node[3].index = Integer.parseInt(sixthFeatureNumber);
//			node[3].value = 1;
//		}
		
		String childForm = sentence.getForm(childID);
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		String headForm = sentence.getForm(headID);
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/1000 Cluster POS projectivized/Classifiers/"+relation+".svm.model");
		
		double prediction = svm.svm_predict(model, node);
		
		Integer headint = new Integer(headID);
		Integer childint = new Integer(childID);
		
		if(prediction<0){ // -1 means child goes after head.
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addAfter(childHype);
			
//			if(childint<headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

			
		} else{ // 1 means that child goes before the head. 
			
			HyperNode headHype = map.get(headID);
			HyperNode childHype = map.get(childID);
			headHype.addBefore(childHype);
			
//			if(childint>headint){
//				System.out.println("wrong!");
//			} else{
//				System.out.println("correct");
//			}

		}
	}

	public String orderVert2Complex(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		String relation = sentence.getDeprel(childID);
		
		svm_node[] node = new svm_node[5]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		
		String childForm = sentence.getForm(childID);
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		String headForm = sentence.getForm(headID);
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		String headChildren = ""+sentence.getChilds(headID).size();
		String childChildren = ""+sentence.getChilds(childID).size();
		String firstFeatureNumber = featureExtractor("headWeight= "+headChildren+" childWeight= "+childChildren);
		if(firstFeatureNumber.equals("none")){
			node[4] = new svm_node();
			node[4].index = nonFeat;
			node[4].value = 0;
		} else{
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(firstFeatureNumber);
			node[4].value = 1;
		}	
		
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/1000 Cluster POS Complex/Classifiers/"+relation+".svm.model");
		
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
	}
	
	public String orderVert2POS(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		String relation = sentence.getDeprel(childID);
		svm_node[] node = new svm_node[2]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/POS/Classifiers/"+relation+".svm.model");
		
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
		
	}
	
	public String orderVert2New(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		String relation = sentence.getDeprel(childID);
		String headForm = sentence.getForm(headID);
		String childForm = sentence.getForm(childID);
		
		svm_node[] node = new svm_node[5]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
	
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		
		
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		
		NgramLanguageModel<String>  lm;
		lm = LmReaders.readLmBinary("eng.blm");
		String option1 = childForm+" "+headForm;
		List<String> list= Arrays.asList(option1.trim().split("\\s+"));
		float logProb1 = lm.scoreSentence(list);
	
		String option2 = childForm+" "+headForm;
		List<String> list2 = Arrays.asList(option2.trim().split("\\s+"));
		float logProb2 = lm.scoreSentence(list2);

		
		if(logProb1 > logProb2){ // child goes before head
			node[4] = new svm_node();
			node[4].index = new Integer(1);
			node[4].value = 1;
			
		} else{
			node[4] = new svm_node();
			node[4].index = new Integer(1);
			node[4].value = 0;
		}
		
		svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/1000 Cluster POS + lm/Classifiers/"+relation+".svm.model");
		
		double prediction = svm.svm_predict(model, node);
		
		Integer headint = new Integer(headID);
		Integer childint = new Integer(childID);
		
		if(prediction<0){ // -1 means child goes after head, so head is added to child's before map. 
			
//			if(childint<headint){
//				System.out.println("wrong! 2");
//			} else{
//				System.out.println("correct 2");
//			}
			
			beforeMap.get(childID).add(headID);
			beforeMap.get(childID).addAll(beforeMap.get(headID));
			return "before";

			
		} else{ // 1 means that child goes before the head, so child is added to head's beforemap. 
			
//			if(childint>headint){
//				System.out.println("wrong! 2");
//			} else{
//				System.out.println("correct 2");
//			}
			
			beforeMap.get(headID).add(childID);
			beforeMap.get(headID).addAll(beforeMap.get(childID));
			return "after";
		}
	 
	}

	public String orderVert2Single(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		svm_node[] node = new svm_node[5]; // 6 = number of features a pair of words has.
		
		int nonFeat = featureMap.size()+1;
		
		
		
		String childPOS = sentence.getPOS(childID);
		String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
		if(thirdFeatureNumber.equals("none")){
			node[0] = new svm_node();
			node[0].index = nonFeat;
			node[0].value = 0;
		} else{
			node[0] = new svm_node();
			node[0].index = Integer.parseInt(thirdFeatureNumber);
			node[0].value = 1;
		}		
		
		String headPOS = sentence.getPOS(headID);
		String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
		if(fourthFeatureNumber.equals("none")){
			node[1] = new svm_node();
			node[1].index = nonFeat;
			node[1].value = 0;
		} else{
			node[1] = new svm_node();
			node[1].index = Integer.parseInt(fourthFeatureNumber);
			node[1].value = 1;
		}
		
		
		String childForm = sentence.getForm(childID);
		String childCluster = this.clusterDict.get(childForm);
		String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
		if(seventhFeatureNumber.equals("none")){
			node[2] = new svm_node();
			node[2].index = nonFeat;
			node[2].value = 0;
		} else{
			node[2] = new svm_node();
			node[2].index = Integer.parseInt(seventhFeatureNumber);
			node[2].value = 1;
		}
		
		String headForm = sentence.getForm(headID);
		String headCluster = this.clusterDict.get(headForm);
		String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
		if(eighthFeatureNumber.equals("none")){
			node[3] = new svm_node();
			node[3].index = nonFeat;
			node[3].value = 0;
		} else{
			node[3] = new svm_node();
			node[3].index = Integer.parseInt(eighthFeatureNumber);
			node[3].value = 1;
		}
		
		
		String childRel = sentence.getDeprel(childID);
		String ninthFeatureNumber = featureExtractor("childRel= "+childRel);
		if(ninthFeatureNumber.equals("none")){
			node[4] = new svm_node();
			node[4].index = nonFeat;
			node[4].value = 0;
		} else{
			node[4] = new svm_node();
			node[4].index = Integer.parseInt(ninthFeatureNumber);
			node[4].value = 1;
		}
		
		svm_model model = svm.svm_load_model("December Vertical Files/vertical.svm.model");
		
		double prediction = svm.svm_predict(model, node);
		
		Integer headint = new Integer(headID);
		Integer childint = new Integer(childID);
		
		if(prediction<0){ // -1 means child goes after head, so head is added to child's before map. 
			
//			if(childint<headint){
//				System.out.println("wrong! 2");
//			} else{
//				System.out.println("correct 2");
//			}
			
			beforeMap.get(childID).add(headID);
			beforeMap.get(childID).addAll(beforeMap.get(headID));
			return "before";
			
			

			
		} else{ // 1 means that child goes before the head, so child is added to head's beforemap. 
			
//			if(childint>headint){
//				System.out.println("wrong! 2");
//			} else{
//				System.out.println("correct 2");
//			}
			
			beforeMap.get(headID).add(childID);
			beforeMap.get(headID).addAll(beforeMap.get(childID));
			return "after";
		}
	 
	}
	
	public String orderVert2Cluster(String headID, String childID, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		String relation = sentence.getDeprel(childID);
		if(!((beforeMap.get(headID).contains(childID))||(beforeMap.get(childID).contains(headID)))){
			
			svm_node[] node = new svm_node[4]; // 6 = number of features a pair of words has.
			
			int nonFeat = featureMap.size()+1;
			
//			String numberOfChildren = ""+sentence.getChilds(headID).size();
//			node[0] = new svm_node();
//			node[0].index = 1;
//			node[0].value = Double.valueOf(numberOfChildren).doubleValue();
//			
//			
//			String childHeight = ""+sentence.getDepth(childID);
//			node[1] = new svm_node();
//			node[1].index = 2;
//			node[1].value = Double.valueOf(childHeight).doubleValue();
			
			
			String childPOS = sentence.getPOS(childID);
			String thirdFeatureNumber = featureExtractor("childPOS= "+childPOS);
			if(thirdFeatureNumber.equals("none")){
				node[0] = new svm_node();
				node[0].index = nonFeat;
				node[0].value = 0;
			} else{
				node[0] = new svm_node();
				node[0].index = Integer.parseInt(thirdFeatureNumber);
				node[0].value = 1;
			}		
			
			String headPOS = sentence.getPOS(headID);
			String fourthFeatureNumber = featureExtractor("headPOS= "+headPOS);
			if(fourthFeatureNumber.equals("none")){
				node[1] = new svm_node();
				node[1].index = nonFeat;
				node[1].value = 0;
			} else{
				node[1] = new svm_node();
				node[1].index = Integer.parseInt(fourthFeatureNumber);
				node[1].value = 1;
			}
			
//			String childLemma = sentence.getLemma(childID);
//			String fifthFeatureNumber = featureExtractor("childLemma= "+childLemma);
//			if(fifthFeatureNumber.equals("none")){
//				node[2] = new svm_node();
//				node[2].index = nonFeat;
//				node[2].value = 0;
//			} else{
//				node[2] = new svm_node();
//				node[2].index = Integer.parseInt(fifthFeatureNumber);
//				node[2].value = 1;
//			}
//			
//			String headLemma = sentence.getLemma(headID);
//			String sixthFeatureNumber = featureExtractor("headLemma= "+headLemma);
//			if(sixthFeatureNumber.equals("none")){
//				node[3] = new svm_node();
//				node[3].index = nonFeat;
//				node[3].value = 0;
//			} else{
//				node[3] = new svm_node();
//				node[3].index = Integer.parseInt(sixthFeatureNumber);
//				node[3].value = 1;
//			}
			
			String childForm = sentence.getForm(childID);
			String childCluster = this.clusterDict.get(childForm);
			String seventhFeatureNumber = featureExtractor("childCluster= "+childCluster);
			if(seventhFeatureNumber.equals("none")){
				node[2] = new svm_node();
				node[2].index = nonFeat;
				node[2].value = 0;
			} else{
				node[2] = new svm_node();
				node[2].index = Integer.parseInt(seventhFeatureNumber);
				node[2].value = 1;
			}
			
			String headForm = sentence.getForm(headID);
			String headCluster = this.clusterDict.get(headForm);
			String eighthFeatureNumber = featureExtractor("headCluster= "+headCluster);
			if(eighthFeatureNumber.equals("none")){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else{
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(eighthFeatureNumber);
				node[3].value = 1;
			}
			
			
			svm_model model = svm.svm_load_model("December Vertical Files/Non Singleton/1000 Cluster POS projectivized/Classifiers/"+relation+".svm.model");
			
			double prediction = svm.svm_predict(model, node);
			
			Integer headint = new Integer(headID);
			Integer childint = new Integer(childID);
			
			if(prediction<0){ // -1 means child goes after head, so head is added to child's before map. 
				
//				if(childint<headint){
//					System.out.println("wrong! 2");
//				} else{
//					System.out.println("correct 2");
//				}
				
				beforeMap.get(childID).add(headID);
				beforeMap.get(childID).addAll(beforeMap.get(headID));
				return "before";
				
				

				
			} else{ // 1 means that child goes before the head, so child is added to head's beforemap. 
				
//				if(childint>headint){
//					System.out.println("wrong! 2");
//				} else{
//					System.out.println("correct 2");
//				}
				
				beforeMap.get(headID).add(childID);
				beforeMap.get(headID).addAll(beforeMap.get(childID));
				return "after";
			}
		} return "none";
		
	}
	
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
			
			Integer headint = new Integer(headID);
			Integer childint = new Integer(childID);
			
			if(prediction<0){ // -1 means child goes after head, so head is added to child's before map. 
				
//				if(childint<headint){
//					System.out.println("wrong! 2");
//				} else{
//					System.out.println("correct 2");
//				}
				
				beforeMap.get(childID).add(headID);
				beforeMap.get(childID).addAll(beforeMap.get(headID));
				return "before";
				
				
			} else{ // 1 means that child goes before the head, so child is added to head's beforemap. 
				
//				if(childint>headint){
//					System.out.println("wrong! 2");
//				} else{
//					System.out.println("correct 2");
//				}
				
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
			
			Integer headint = new Integer(headID);
			Integer childint = new Integer(childID);
			
			if(prediction<0){ // -1 means child goes after head.
				
				HyperNode headHype = map.get(headID);
				HyperNode childHype = map.get(childID);
				headHype.addAfter(childHype);
				
//				if(childint<headint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				
				
			} else{ // 1 means that child goes before the head. 
				
				HyperNode headHype = map.get(headID);
				HyperNode childHype = map.get(childID);
				headHype.addBefore(childHype);
				
//				if(childint>headint){
//					System.out.println("wrong! 2");
//				} else{
//					System.out.println("correct 2");
//				}

			}
		
		
	}
	
	public String sideOrderNoHead(String firstNode, String sibling, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		if ((!beforeMap.get(sibling).contains(firstNode))&&(!beforeMap.get(firstNode).contains(sibling))){
			String headID = sentence.getHead(firstNode);
			
			svm_node[] node = new svm_node[6];
			
			int nonFeat = sideFeatureMap.size()+1;
			
//			String firstNodeWeight = ""+sentence.getWeight(firstNode);
//			node[0] = new svm_node();
//			node[0].index = 1;
//			node[0].value = Double.valueOf(firstNodeWeight).doubleValue();
//
//			
//			String siblingWeight = ""+sentence.getWeight(firstNode);
//			node[1] = new svm_node();
//			node[1].index = 2;
//			node[1].value = Double.valueOf(siblingWeight).doubleValue();

			
			String firstNodePOS = sentence.getPOS(firstNode);
			String fourthFeatureNumber = sideFeatureExtractor("firstPOS= "+firstNodePOS);
			if(fourthFeatureNumber.isEmpty()){
				node[2] = new svm_node();
				node[2].index = nonFeat;
				node[2].value = 0;
			} else{
				node[2] = new svm_node();
				node[2].index = Integer.parseInt(fourthFeatureNumber);
				node[2].value = 1;
			}
			
			String siblingPOS = sentence.getPOS(sibling);
			String fifthFeatureNumber = sideFeatureExtractor("siblingPOS= "+siblingPOS);
			if(fifthFeatureNumber.isEmpty()){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else {
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fifthFeatureNumber);
				node[3].value = 1;
			}
			
			String firstNodeLemma = sentence.getLemma(firstNode);
			String sixthFeatureNumber = sideFeatureExtractor("firstLemma= "+firstNodeLemma);
			if(sixthFeatureNumber.isEmpty()){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
			} else{
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(sixthFeatureNumber);
				node[4].value = 1;
			}
			
			String siblingLemma = sentence.getLemma(sibling);
			String seventhFeatureNumber = sideFeatureExtractor("siblingLemma= "+siblingLemma);
			if(seventhFeatureNumber.isEmpty()){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
			} else {
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(seventhFeatureNumber);
				node[5].value = 1;
			}
			
			String firstNodeRel = sentence.getDeprel(firstNode);
			String eighthFeatureNumber = sideFeatureExtractor("firstNodeRel= "+firstNodeRel);
			if(eighthFeatureNumber.isEmpty()){
				node[0] = new svm_node();
				node[0].index = nonFeat;
				node[0].value = 0;
			} else{
				node[0] = new svm_node();
				node[0].index = Integer.parseInt(eighthFeatureNumber);
				node[0].value = 1;
			}
			
			String siblingRel = sentence.getDeprel(sibling);
			String ninthFeatureNumber = sideFeatureExtractor("siblingRel= "+siblingRel);
			if(ninthFeatureNumber.isEmpty()){
				node[1] = new svm_node();
				node[1].index = nonFeat;
				node[1].value = 0;
			} else {
				node[1] = new svm_node();
				node[1].index = Integer.parseInt(ninthFeatureNumber);
				node[1].value = 1;
			}
			
			
			svm_model model = svm.svm_load_model("December Horizontal Files/No Head projectivized/siblings.svm.model");
//        	svm_model model = svm.svm_load_model("new_siblings.svm.model");
			
			double prediction = svm.svm_predict(model, node);
			Integer firstNodeint = new Integer(firstNode);
			Integer sibint = new Integer(sibling);
			
			if(prediction<0){ // -1 means firstNode goes after sibling.
//				if(firstNodeint<sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "before";
				
			} else{ // 1 means that firstNode goes before the sibling.
//				if(firstNodeint>sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "after";
			}
			
		} return "none";
	}
	
	public String sideOrderNoLemma(String firstNode, String sibling, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		if ((!beforeMap.get(sibling).contains(firstNode))&&(!beforeMap.get(firstNode).contains(sibling))){
			String headID = sentence.getHead(firstNode);
			
			svm_node[] node = new svm_node[6];
			
			int nonFeat = sideFeatureMap.size()+1;
			
//			String firstNodeWeight = ""+sentence.getWeight(firstNode);
//			node[0] = new svm_node();
//			node[0].index = 1;
//			node[0].value = Double.valueOf(firstNodeWeight).doubleValue();
//
//			
//			String siblingWeight = ""+sentence.getWeight(firstNode);
//			node[1] = new svm_node();
//			node[1].index = 2;
//			node[1].value = Double.valueOf(siblingWeight).doubleValue();
			
			
			
			String firstNodePOS = sentence.getPOS(firstNode);
			String fourthFeatureNumber = sideFeatureExtractor("firstPOS= "+firstNodePOS);
			if(fourthFeatureNumber.isEmpty()){
				node[2] = new svm_node();
				node[2].index = nonFeat;
				node[2].value = 0;
			} else{
				node[2] = new svm_node();
				node[2].index = Integer.parseInt(fourthFeatureNumber);
				node[2].value = 1;
			}
			
			String siblingPOS = sentence.getPOS(sibling);
			String fifthFeatureNumber = sideFeatureExtractor("siblingPOS= "+siblingPOS);
			if(fifthFeatureNumber.isEmpty()){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
			} else {
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fifthFeatureNumber);
				node[3].value = 1;
			}
			
			String firstNodeRel = sentence.getDeprel(firstNode);
			String sixthFeatureNumber = sideFeatureExtractor("firstNodeRel= "+firstNodeRel);
			if(sixthFeatureNumber.isEmpty()){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
			} else{
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(sixthFeatureNumber);
				node[4].value = 1;
			}
			
			String siblingRel = sentence.getDeprel(sibling);
			String seventhFeatureNumber = sideFeatureExtractor("siblingRel= "+siblingRel);
			if(seventhFeatureNumber.isEmpty()){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
			} else {
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(seventhFeatureNumber);
				node[5].value = 1;
			}
			
			String headPOS = sentence.getPOS(headID);
			String eigthFeatureNumber = sideFeatureExtractor("headPOS= "+headPOS);
			if(eigthFeatureNumber.isEmpty()){
				node[0] = new svm_node();
				node[0].index = nonFeat;
				node[0].value = 0;
			} else{
				node[0] = new svm_node();
				node[0].index = Integer.parseInt(eigthFeatureNumber);
				node[0].value = 1;
			}
			
			String headRel = sentence.getDeprel(headID);
			String ninthFeatureNumber = sideFeatureExtractor("headRel= "+headRel);
			if(ninthFeatureNumber.isEmpty()){
				node[1] = new svm_node();
				node[1].index = nonFeat;
				node[1].value = 0;
			} else{
				node[1] = new svm_node();
				node[1].index = Integer.parseInt(ninthFeatureNumber);
				node[1].value = 1;
			}
			
//			svm_model model = svm.svm_load_model("Classifiers/linearization_svm_siblings.svm.model");
//        	svm_model model = svm.svm_load_model("new_siblings.svm.model");
			svm_model model = svm.svm_load_model("December Horizontal Files/No Lemma projectivized/siblings.svm.model");
			
			double prediction = svm.svm_predict(model, node);
			Integer firstNodeint = new Integer(firstNode);
			Integer sibint = new Integer(sibling);
			
			if(prediction<0){ // -1 means firstNode goes after sibling.
//				if(firstNodeint<sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "before";
				
			} else{ // 1 means that firstNode goes before the sibling.
//				if(firstNodeint>sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "after";
			}
			
		} return "none";
	}
	
	//TODO: FIX DICTIONARY CALLS IE FEATURE EXTRACTOR AND MAKE SURE EVERYTHING IS CALLING THE RIGHT DICT.
	public String sideOrder2(String sib1, String sib2, CoNLLHash sentence, HashMap<String, HyperNode> map, HashMap<String, HashSet<String>> beforeMap) throws IOException{
		if((!beforeMap.get(sib1).contains(sib2))&&(!beforeMap.get(sib2).contains(sib1))){
			
			String firstRel = sentence.getDeprel(sib1);
			String secondRel = sentence.getDeprel(sib2);
			String relPair = firstRel+secondRel;
			String otherRelPair = secondRel+firstRel;
			String firstNode = sib1;
			String sibling = sib2;
			
			File option1 = new File("Sibling Dep Files/Deprel/Classifiers/"+relPair+"_siblings.svm.model");
			File option2 = new File("Sibling Dep Files/Deprel/Classifiers/"+otherRelPair+"_siblings.svm.model");
			
			if(option2.exists()){
				firstNode = sib2;
				sibling = sib1;
				relPair = secondRel+firstRel;
			} else if(!option1.exists()){
//				System.out.println("sideorder");
				return this.sideOrder(sib1, sib2, sentence, map, beforeMap);
			}
			
			
			String headID = sentence.getHead(sib1);
			
			int numberOfSiblings = sentence.getSiblingss(sib1).size();
			
			svm_node[] node = new svm_node[7];
			
			int nonFeat = sideFeatureMap.size()+1;
			
			
			String firstNodePOS = sentence.getPOS(firstNode);
			String firstFeatureNumber = sideFeatureDepExtractor("firstPOS= "+firstNodePOS);
			if(firstFeatureNumber.isEmpty()){
				node[0] = new svm_node();
				node[0].index = nonFeat;
				node[0].value = 0;
//				System.out.println("not a feat");
			} else{
				node[0] = new svm_node();
				node[0].index = Integer.parseInt(firstFeatureNumber);
				node[0].value = 1;
			}
			
			String siblingPOS = sentence.getPOS(sibling);
			String secondFeatureNumber = sideFeatureDepExtractor("siblingPOS= "+siblingPOS);
			if(secondFeatureNumber.isEmpty()){
				node[1] = new svm_node();
				node[1].index = nonFeat;
				node[1].value = 0;
//				System.out.println("not a feat");
			} else {
				node[1] = new svm_node();
				node[1].index = Integer.parseInt(secondFeatureNumber);
				node[1].value = 1;
			}
			
			String headPOS = sentence.getPOS(headID);
			String thirdFeatureNumber = sideFeatureDepExtractor("headPOS= "+headPOS);
			if(thirdFeatureNumber.isEmpty()){
				node[2] = new svm_node();
				node[2].index = nonFeat;
				node[2].value = 0;
//				System.out.println("not a feat");
			} else{
				node[2] = new svm_node();
				node[2].index = Integer.parseInt(thirdFeatureNumber);
				node[2].value = 1;
			}
			
			String firstNodeLemma = sentence.getLemma(firstNode);
			String fourthFeatureNumber = sideFeatureDepExtractor("firstLemma= "+firstNodeLemma);
			if(fourthFeatureNumber.isEmpty()){
				node[3] = new svm_node();
				node[3].index = nonFeat;
				node[3].value = 0;
//				System.out.println("not a feat");
			} else{
				node[3] = new svm_node();
				node[3].index = Integer.parseInt(fourthFeatureNumber);
				node[3].value = 1;
			}
			
			String siblingLemma = sentence.getLemma(sibling);
			String fifthFeatureNumber = sideFeatureDepExtractor("siblingLemma= "+siblingLemma);
			if(fifthFeatureNumber.isEmpty()){
				node[4] = new svm_node();
				node[4].index = nonFeat;
				node[4].value = 0;
//				System.out.println("not a feat");
			} else {
				node[4] = new svm_node();
				node[4].index = Integer.parseInt(fifthFeatureNumber);
				node[4].value = 1;
			}
			
			
			String headLemma = sentence.getLemma(headID);
			String sixthFeatureNumber = sideFeatureDepExtractor("headLemma= "+headLemma);
			if(sixthFeatureNumber.isEmpty()){
				node[5] = new svm_node();
				node[5].index = nonFeat;
				node[5].value = 0;
//				System.out.println("not a feat");
			} else{
				node[5] = new svm_node();
				node[5].index = Integer.parseInt(sixthFeatureNumber);
				node[5].value = 1;
			}
			
			
			String headRel = sentence.getDeprel(headID);
			String seventhFeatureNumber = sideFeatureDepExtractor("headRel= "+headRel);
			if(seventhFeatureNumber.isEmpty()){
				node[6] = new svm_node();
				node[6].index = nonFeat;
				node[6].value = 0;
//				System.out.println("not a feat");
			} else{
				node[6] = new svm_node();
				node[6].index = Integer.parseInt(seventhFeatureNumber);
				node[6].value = 1;
			}
			
			int currentNode = 7;
			
//			ArrayList<String> otherSiblings = sentence.getSiblingss(sib1);
//			Iterator<String> sibIt = otherSiblings.iterator();
//			while(sibIt.hasNext()){
//				String otherSib = sibIt.next();
//				if(!otherSib.equals(sib2)){
//					String otherSibRel = sentence.getDeprel(otherSib);
//					String featureNumber = sideFeatureDepExtractor("other sibling rel = "+otherSibRel);
//					if(featureNumber.isEmpty()){
//						node[currentNode] = new svm_node();
//						node[currentNode].index = nonFeat;
//						node[currentNode].value = 0;
////						System.out.println("not a feat");
//					} else{
//						node[currentNode] = new svm_node();
//						node[currentNode].index = Integer.parseInt(featureNumber);
//						node[currentNode].value = 1;
//					}
//					currentNode++;
//				}
//			}
//			
//			while(currentNode<numberOfSiblings*2+7){
//				node[currentNode] = new svm_node();
//				node[currentNode].index = nonFeat;
//				node[currentNode].value = 0;
//				currentNode++;
//			}
			
//			ArrayList<String> otherSiblings = sentence.getSiblingss(sib1);
//			Iterator<String> osibIt = otherSiblings.iterator();
//			while(osibIt.hasNext()){
//				String otherSib = sibIt.next();
//				if(!otherSib.equals(sib2)){
//					String otherSibPOS = sentence.getPOS(otherSib);
//					String featureNumber = sideFeatureDepExtractor("other sibling POS = "+otherSibPOS);
//					if(featureNumber.isEmpty()){
//						node[currentNode] = new svm_node();
//						node[currentNode].index = nonFeat;
//						node[currentNode].value = 0;
//					} else{
//						node[currentNode] = new svm_node();
//						node[currentNode].index = Integer.parseInt(featureNumber);
//						node[currentNode].value = 1;
//					}
//					currentNode++;
//				}
//			}
			
			
			svm_model model = svm.svm_load_model("Sibling Dep Files/Deprel/Classifiers/"+relPair+"_siblings.svm.model");
			double prediction = svm.svm_predict(model, node);
//			System.out.println(prediction);
			
			Integer firstNodeint = new Integer(firstNode);
			Integer sibint = new Integer(sibling);
//			System.out.println("first Node place: "+firstNodeint);
//			System.out.println("sibling place: "+sibint);
			if(prediction<0){ // -1 means firstNode goes after sibling.
//				System.out.println("prediction: first node goes after sibling");
//				System.out.println("first node is: "+sentence.getForm(firstNode));
//				System.out.println("sibling is: "+sentence.getForm(sibling));
//				if(firstNodeint<sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "before"; //original
//				return "after";
				
			} else{ // 1 means that firstNode goes before the sibling.
//				System.out.println("prediction: first node goes before sibling");
//				System.out.println("first node is: "+sentence.getForm(firstNode));
//				System.out.println("sibling is: "+sentence.getForm(sibling));
//				if(firstNodeint>sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "after"; //original
//				return "before";
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
			
			svm_model model = svm.svm_load_model("Classifiers/linearization_svm_siblings.svm.model");
//        	svm_model model = svm.svm_load_model("new_siblings.svm.model");
			
			double prediction = svm.svm_predict(model, node);
			Integer firstNodeint = new Integer(firstNode);
			Integer sibint = new Integer(sibling);
			
			if(prediction<0){ // -1 means firstNode goes after sibling.
//				if(firstNodeint<sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
				return "before";
				
			} else{ // 1 means that firstNode goes before the sibling.
//				if(firstNodeint>sibint){
//					System.out.println("wrong!");
//				} else{
//					System.out.println("correct");
//				}
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
			String headWRTChild = this.orderVert2Complex(head, child, sentence, hyperNodeMap, beforeMap); // headWRTChild = head with respect to child, ie before or after.
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
			
			//TODO: REASON ABOUT AND TEST WHETHER MAKE TRANSITIVE FUNCTION WOULD HELP IF PUT IN FIRST FOR LOOP
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
				String result = this.sideOrderNoLemma(child, sibling, sentence, hyperNodeMap, beforeMap);
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
				} else{
					this.makeTransitive(sentence, beforeMap);
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
				this.orderVert1Complex(head, child, sentence, hyperNodeMap);
			} else{

				if(sentence.getDeprel(child).equals("ROOT")){
					HyperNode rootHyperNode = hyperNodeMap.get(child);
					return rootHyperNode.form;
				}
				//				System.out.println("head: "+sentence.getForm(head));
				//				System.out.println("child: "+sentence.getForm(child));
				this.orderVert1Complex(head, child, sentence, hyperNodeMap);
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

//		Linearizer liner = new Linearizer("Sentences/Evaluation Set","Current Best/relmap.ser","December Horizontal Files/No Head Big/sideRelMapDec.ser");
//		Linearizer liner = new Linearizer("Sentences/Evaluation Set","Current Best/relmap.ser","Current Best/sideRelMap.ser");
//		Linearizer liner = new Linearizer("Sentences/Evaluation Set","Current Best/relmap.ser","Current Best/sideRelMap.ser","Sibling Dep Files/Deprel/Object Files/sideFeatureDictDep101815.ser", "Sibling Dep Files/Deprel/Object Files/sibRelationPairsDepList.ser");
//		Linearizer liner = new Linearizer("Sentences/New Evaluation Set","December Vertical Files/Non Singleton/1000 Cluster POS projectivized/relmapDec.ser","Current Best/sideRelMap.ser","December Vertical Files/Non Singleton/1000 Cluster POS projectivized/clusterMapDec.ser");
//		Linearizer liner = new Linearizer("Sentences/New Evaluation Set","December Vertical Files/Non Singleton/1000 Cluster POS projectivized/relmapDec.ser","December Horizontal Files/No Lemma projectivized/sideRelMapDec.ser","December Vertical Files/Non Singleton/1000 Cluster POS projectivized/clusterMapDec.ser");
		Linearizer liner = new Linearizer("Sentences/Evaluation Set","December Vertical Files/Non Singleton/1000 Cluster POS Complex/relmapDec.ser","December Horizontal Files/No Lemma projectivized/sideRelMapDec.ser","December Vertical Files/Non Singleton/1000 Cluster POS Complex/clusterMapDec.ser");
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

//				System.out.println(goldStandardForm);
//				System.out.println(predictionFormm+"\n");

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
