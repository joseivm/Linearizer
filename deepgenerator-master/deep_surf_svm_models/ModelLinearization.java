package deep_surf_svm_models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import deep_to_surf.CoNLLHash;
import edu.berkeley.nlp.lm.AbstractContextEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.ContextEncodedNgramLanguageModel;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.StupidBackoffLm;
import edu.berkeley.nlp.lm.io.ComputeLogProbabilityOfTextStream;
import edu.berkeley.nlp.lm.io.LmReaders;

/**
 * ModelLinearization is a class that is used to create training and testing files for dependency relation classifiers. 
 * Each dependency relation has a classifier that, given two words and additional information, outputs their order. 
 * 
 * @author Jose Ignacio Velarde
 *
 */
public class ModelLinearization {

	int nextRelation = 0; 
	int nextFeat = 4;
	int nextSideFeat = 4;
	int nextSideFeatPOS = 4;
	int nextSideFeatDep = 1;
	int nextSideFeatHeadDeprel = 1;
	
	private HashMap <String, String> sideFeatureTranslation = new HashMap <String, String>(); // A dictionary mapping features to their index in the sibling classifier.
	private HashMap <String, String> featureTranslation = new HashMap <String, String>(); // A dictionary mapping features to their index in the dependency relations classifier.
	private HashMap <String, BufferedWriter> trainWriters; // A dictionary mapping each dependency relation to the BufferedWriter that will write the training file for its classifier.
	private HashMap <String, BufferedWriter> testWriters; // A dictionary mapping each dependency relation to the BufferedWriter that will write the test file for its classifier.
	private HashMap <String, String> featureTranslationNumToWord = new HashMap <String, String>();
	private ArrayList <String> relations = new ArrayList <String>(); // An ArrayList containing the dependency relations for which classifiers were created.
	private ArrayList<String> testRelations = new ArrayList<String>(); // An ArrayList containing the dependency relations for which test files were created.
	public HashMap<String, String> wordToCluster;
	
	private ArrayList <String> sibRelationsDep = new ArrayList<String>();
	private ArrayList<String> testSibRelationsDep = new ArrayList<String>();
	private HashMap <String, String> sideFeatureTranslationDep = new HashMap <String, String>();
	private HashMap <String, String> sideFeatureTranslationDepNumToWord = new HashMap <String, String>();
	
	private ArrayList<String> sibHeadPOS = new ArrayList<String>();
	private ArrayList <String> testSibHeadPOS = new ArrayList<String>();
	private HashMap <String, String> sideFeatureTranslationPOS = new HashMap <String, String>();
	private HashMap <String, String> sideFeatureTranslationPOSNumToWord = new HashMap <String, String>();
	
	private ArrayList<String> sibHeadDeprel = new ArrayList<String>();
	private ArrayList <String> testSibHeadDeprel = new ArrayList<String>();
	private HashMap <String, String> sideFeatureTranslationHeadDeprel = new HashMap <String, String>();
	private HashMap <String, String> sideFeatureTranslationHeadDeprelNumToWord = new HashMap <String, String>();
	
	private HashMap <String, Integer> relationsNumbers = new HashMap <String,Integer>(); 

	//TODO: ADD BACKWARDS FEATURE DICTIONARIES!!!!!!
	public ModelLinearization() {
		
		trainWriters = new HashMap <String, BufferedWriter>();
		testWriters = new HashMap <String, BufferedWriter>();
		
	}
	
	
	/**
	 * @return an array list of strings containing the relations for which classifiers were trained.
	 */
	public ArrayList<String> getRelations(){
		return new ArrayList<String>(this.relations);
	}
	
	/**
	 * @return an array list of strings containing the relations for which test files were created.
	 */
	public ArrayList<String> getTestRelations(){
		return new ArrayList<String>(this.testRelations);
	}
	
	/**
	 * @return a HashMap that maps a feature to its index in the feature vector.
	 */
	public HashMap<String, String> getFeaturesDict(){
		return featureTranslation;
	}
	
	public HashMap<String,String> getBackwardsFeatureDict(){
		return featureTranslationNumToWord;
	}
	
	/**
	 * @return a HashMap that maps a feature to its index in the feature vector for the sibling classifier.
	 */
	public HashMap<String, String> getSideFeaturesDict(){
		return this.sideFeatureTranslation;
	}
	
	public ArrayList<String> getHeadDeprels(){
		return new ArrayList<String>(this.sibHeadDeprel);
	}
	
	public ArrayList<String> getTestHeadDeprels(){
		return new ArrayList<String>(this.testSibHeadDeprel);
	}
	
	public ArrayList<String> getSibRelations(){
		return new ArrayList<String>(this.sibRelationsDep);
	}
	
	public HashMap<String, String> getBackwardsMapHeadDeprel(){
		return this.sideFeatureTranslationHeadDeprelNumToWord;
	}

	public HashMap<String, String> getSideFeaturesDictHeadDeprel(){
		return this.sideFeatureTranslationHeadDeprel;
	}
	
	public ArrayList<String> getTestSibRelations(){
		return new ArrayList<String>(this.testSibRelationsDep);
	}
	
	public HashMap<String, String> getSideFeaturesDictDep(){
		return sideFeatureTranslationDep;
	}
	
	public HashMap<String, String> getBackwardsMapDep(){
		return this.sideFeatureTranslationDepNumToWord;
	}
	
	public ArrayList<String> getSibHeadPOS(){
		return new ArrayList<String>(this.sibHeadPOS);
	}
	
	public ArrayList<String> getTestSibHeadPOS(){
		return new ArrayList<String>(this.testSibHeadPOS);
	}
	
	public HashMap<String, String> getSideFeaturesDictPOS(){
		return sideFeatureTranslationPOS;
	}
	
	public HashMap<String, String> getBackwardsMapPOS(){
		return this.sideFeatureTranslationPOSNumToWord;
	}
	
	public void setSideFeatMap(HashMap<String, String> map){
		this.sideFeatureTranslationDep = map;
	}
	
	public void setSibRelationsDep(ArrayList<String>list){
		this.sibRelationsDep = list;
	}

	public void clusters(String filename) throws IOException{
		this.wordToCluster = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		while(br.ready()){
			String line = br.readLine();
			String news = line.replaceAll("\\s+", "_");
			String[] parts = news.split("_");
			if(parts.length>1){
				String word = parts[1];
				String number = parts[0];
				if(this.wordToCluster.get(word)==null){
					if(number.matches("[0-9]+")){
						int nnumber = Integer.parseInt(number, 2);
						this.wordToCluster.put(word, ""+nnumber);
					}
					
				}
				
			}
	}
	}
	
	/**
	 * Closes the buffers of either the testWriters or the trainWriters. testWriters write the test files while 
	 * trainWriters write the training files.
	 * @param train: boolean that determines whether we are closing the buffers of the training files or of the test files.
	 */
	public void closeBuffers(boolean train){
		try {
			if (train) {
				Iterator<String> it=trainWriters.keySet().iterator();
				while(it.hasNext()){
					BufferedWriter bw=trainWriters.get(it.next());
					bw.close();
				}
				
			}
			else {
				Iterator<String> it=testWriters.keySet().iterator();
				while(it.hasNext()){
					BufferedWriter bw=testWriters.get(it.next());
					bw.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Maps the new feature to 1:1 if its the first feature, 2:1 if its the second feature
	 * 3:1 if its the third feature and so on. The features are mapped to that ratio in the 
     * sideFeatureTranslation dictionary. Increases nextFeat by one.
	 * 
	 * @param feature string representing the feature to be added. 
	 */
	private void addNewSideFeature (String feature){
		
		String svmFeat=sideFeatureTranslation.get(feature); // Checks if the feature is in the featureTranslation list.
		if (svmFeat==null) {
			nextSideFeat++;
			sideFeatureTranslation.put(feature,""+nextSideFeat+":1");
		}
		
	}
	
	private void addNewSideFeaturePOS (String feature){
		String svmFeat=sideFeatureTranslationPOS.get(feature); // Checks if the feature is in the featureTranslation list.
		if (svmFeat==null) {
			nextSideFeatPOS++;
			sideFeatureTranslationPOS.put(feature,""+nextSideFeatPOS+":1");
			sideFeatureTranslationPOSNumToWord.put(""+nextSideFeatPOS+":1", feature);
		}
		
	}
	
	private void addNewSideFeatureDep(String feature){
		String svmFeat=sideFeatureTranslationDep.get(feature); // Checks if the feature is in the featureTranslation list.
		if (svmFeat==null) {
			nextSideFeatDep++;
			sideFeatureTranslationDep.put(feature,""+nextSideFeatDep+":1");
			sideFeatureTranslationDepNumToWord.put(""+nextSideFeatDep+":1", feature);
		}
		
	}
	
	private void addNewSideFeatureHeadDeprel(String feature){
		String svmFeat=sideFeatureTranslationHeadDeprel.get(feature); // Checks if the feature is in the featureTranslation list.
		if (svmFeat==null) {
			nextSideFeatHeadDeprel++;
			sideFeatureTranslationHeadDeprel.put(feature,""+nextSideFeatHeadDeprel+":1");
			sideFeatureTranslationHeadDeprelNumToWord.put(""+nextSideFeatHeadDeprel+":1", feature);
		}
	}
	
	/**
	 * Maps the new feature to 1:1 if its the first feature, 2:1 if its the second feature
	 * 3:1 if its the third feature and so on. The features are mapped to that ratio in the 
     * featureTranslation dictionary. Increases nextFeat by one.
	 * 
	 * @param feature string representing the feature to be added. 
	 */
	private void addNewFeature (String feature){
		
		String svmFeat=featureTranslation.get(feature); // Checks if the feature is in the featureTranslation list.
		if (svmFeat==null) {
			nextFeat++;
			featureTranslation.put(feature,""+nextFeat+":1");
			featureTranslationNumToWord.put(""+nextFeat+":1",feature);
		}
		
	}
	
	public void addSideLinePOS(String sib1, String sib2, CoNLLHash sentence, boolean train){
		//String relation = sentence.getDeprel(childID);
		String headID = sentence.getHead(sib1);
		String headPOS = sentence.getPOS(headID);
		if (train && trainWriters.get(headPOS)==null){
			sibHeadPOS.add(headPOS);
			
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter(headPOS+"_train.svm"));
				trainWriters.put(headPOS,bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get(headPOS)==null ){
			testSibHeadPOS.add(headPOS);
			
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter(headPOS+"_test.svm"));
				testWriters.put(headPOS,bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				// Classification of data point.
				String line = "";
				// +1 means that the sib1 goes before sib2, -1 means that the sib1 goes after sib2
				
				if (new Integer(sib1) < new Integer(sib2)) {
					line += "1";
				} else {
					line += "-1";
				}
				
				
				String numberOfChildren = ""+sentence.getChilds(headID).size();
				line+= " 1:"+numberOfChildren;
				
				String firstNodeHeight = ""+sentence.getDepth(sib1);
				line+= " 2:"+firstNodeHeight;
				
				String siblingHeight = ""+sentence.getDepth(sib2);
				line+= " 3:"+siblingHeight;
				
				
				String firstNodePOS = sentence.getPOS(sib1);
				this.addNewSideFeaturePOS("firstPOS= "+firstNodePOS);
				line+= " "+ sideFeatureTranslationPOS.get("firstPOS= "+firstNodePOS);
				
				
				String siblingPOS = sentence.getPOS(sib2);
				this.addNewSideFeaturePOS("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslationPOS.get("siblingPOS= "+siblingPOS);
				
				
				String firstNodeLemma = sentence.getLemma(sib1);
				this.addNewSideFeaturePOS("firstLemma= "+firstNodeLemma);
				line+=" "+sideFeatureTranslationPOS.get("firstLemma= "+firstNodeLemma);
				
				
				String siblingLemma = sentence.getLemma(sib2);
				this.addNewSideFeaturePOS("siblingLemma= "+siblingLemma);
				line+= " "+sideFeatureTranslationPOS.get("siblingLemma= "+siblingLemma);
				
				
				String headLemma = sentence.getLemma(headID);
				this.addNewSideFeaturePOS("headLemma= "+headLemma);
				line+=" "+sideFeatureTranslationPOS.get("headLemma= "+headLemma);
				
				
				String headRel = sentence.getDeprel(headID);
				this.addNewSideFeaturePOS("headRel= "+headRel);
				line+= " "+sideFeatureTranslationPOS.get("headRel= "+headRel);
				
				
				String firstNodeRel = sentence.getDeprel(sib1);
				this.addNewSideFeaturePOS("firstNodeRel= "+firstNodeRel);
				line+= " "+ sideFeatureTranslationPOS.get("firstNodeRel= "+firstNodeRel);
				
				
				String secondNodeRel = sentence.getDeprel(sib2);
				this.addNewSideFeaturePOS("secondNodeRel= "+ secondNodeRel);
				line+= " "+ sideFeatureTranslationPOS.get("secondNodeRel= "+ secondNodeRel);
				

				if (!train){
					String firstWord = sentence.getForm(sib1);
					this.addNewSideFeaturePOS("firstWord = "+ firstWord);
					line+= " "+sideFeatureTranslationPOS.get("firstWord = "+ firstWord);
					
					String secondWord = sentence.getForm(sib2);
					this.addNewSideFeaturePOS("secondWord = " + secondWord);
					line+= " "+sideFeatureTranslationPOS.get("secondWord = " + secondWord);
					
					String sentenceForm = sentence.sentenceToString();
					this.addNewSideFeaturePOS("sentence = " + sentenceForm);
					line+= " "+sideFeatureTranslationPOS.get("sentence = " + sentenceForm);
				}
				
				
					
					try {
						
						if (train){	
							
							BufferedWriter bw=trainWriters.get(headPOS);
							bw.write(line+"\n");
						}
						else {
							BufferedWriter bw2=testWriters.get(headPOS);
							bw2.write(line+"\n");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
	}

	public void addSideLineHeadDeprel(String sib1, String sib2, CoNLLHash sentence, boolean train){
		String headID = sentence.getHead(sib1);
		String headDeprel = sentence.getDeprel(headID);
		if (train && trainWriters.get(headDeprel)==null){
			sibHeadDeprel.add(headDeprel);
			
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("Sibling HeadDeprel Files/Training Files/"+headDeprel+"_train.svm"));
				trainWriters.put(headDeprel,bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get(headDeprel)==null ){
			testSibHeadDeprel.add(headDeprel);
			
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("Sibling HeadDeprel Files/TestingFiles/"+headDeprel+"_test.svm"));
				testWriters.put(headDeprel,bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				// Classification of data point.
				String line = "";
				// +1 means that the sib1 goes before sib2, -1 means that the sib1 goes after sib2
				
				if (new Integer(sib1) < new Integer(sib2)) {
					line += "1";
				} else {
					line += "-1";
				}
				
				String headPOS = sentence.getPOS(headID);
				this.addNewSideFeatureHeadDeprel("headPOS= "+headPOS);
				line+= " "+ sideFeatureTranslationHeadDeprel.get("headPOS= "+headPOS);
				
				String firstNodePOS = sentence.getPOS(sib1);
				this.addNewSideFeatureHeadDeprel("firstPOS= "+firstNodePOS);
				line+= " "+ sideFeatureTranslationHeadDeprel.get("firstPOS= "+firstNodePOS);
				
				
				String siblingPOS = sentence.getPOS(sib2);
				this.addNewSideFeatureHeadDeprel("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslationHeadDeprel.get("siblingPOS= "+siblingPOS);
				
				
				String firstNodeLemma = sentence.getLemma(sib1);
				this.addNewSideFeatureHeadDeprel("firstLemma= "+firstNodeLemma);
				line+=" "+sideFeatureTranslationHeadDeprel.get("firstLemma= "+firstNodeLemma);
				
				
				String siblingLemma = sentence.getLemma(sib2);
				this.addNewSideFeatureHeadDeprel("siblingLemma= "+siblingLemma);
				line+= " "+sideFeatureTranslationHeadDeprel.get("siblingLemma= "+siblingLemma);
				
				
				String headLemma = sentence.getLemma(headID);
				this.addNewSideFeatureHeadDeprel("headLemma= "+headLemma);
				line+=" "+sideFeatureTranslationHeadDeprel.get("headLemma= "+headLemma);
				
				
				
				String firstNodeRel = sentence.getDeprel(sib1);
				this.addNewSideFeatureHeadDeprel("firstNodeRel= "+firstNodeRel);
				line+= " "+ sideFeatureTranslationHeadDeprel.get("firstNodeRel= "+firstNodeRel);
				
				
				String secondNodeRel = sentence.getDeprel(sib2);
				this.addNewSideFeatureHeadDeprel("secondNodeRel= "+ secondNodeRel);
				line+= " "+ sideFeatureTranslationHeadDeprel.get("secondNodeRel= "+ secondNodeRel);
				

				if (!train){
					String firstWord = sentence.getForm(sib1);
					this.addNewSideFeatureHeadDeprel("firstWord = "+ firstWord);
					line+= " "+sideFeatureTranslationHeadDeprel.get("firstWord = "+ firstWord);
					
					String secondWord = sentence.getForm(sib2);
					this.addNewSideFeatureHeadDeprel("secondWord = " + secondWord);
					line+= " "+sideFeatureTranslationHeadDeprel.get("secondWord = " + secondWord);
					
					String sentenceForm = sentence.sentenceToString();
					this.addNewSideFeatureHeadDeprel("sentence = " + sentenceForm);
					line+= " "+sideFeatureTranslationHeadDeprel.get("sentence = " + sentenceForm);
				}
				
				
					
					try {
						
						if (train){	
							
							BufferedWriter bw=trainWriters.get(headDeprel);
							bw.write(line+"\n");
						}
						else {
							BufferedWriter bw2=testWriters.get(headDeprel);
							bw2.write(line+"\n");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	}
	
//	TODO: MODIFY FILE PATHS FOR TESTING AND TRAINING FILES
	public void addSideLineDep(String sib1, String sib2, CoNLLHash sentence, boolean train){
		String firstNode = sib1;
		String sibling = sib2;
		String firstRel = sentence.getDeprel(sib1);
		String secondRel = sentence.getDeprel(sib2);
		if (train && (trainWriters.get(firstRel+secondRel+"_siblings")==null && trainWriters.get(secondRel+firstRel+"_siblings")==null)){ // name of key in map/ name of relation
			sibRelationsDep.add(firstRel+secondRel+"_siblings");
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("Sibling Dep Files/POS/Training Files/"+firstRel+secondRel+"_siblings.svm")); // name of file
				trainWriters.put(firstRel+secondRel+"_siblings",bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && (testWriters.get(firstRel+secondRel+"_siblings")==null && testWriters.get(secondRel+firstRel+"_siblings")==null)){
			testSibRelationsDep.add(firstRel+secondRel+"_siblings");
			
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("Sibling Dep Files/POS/Testing Files/"+firstRel+secondRel+"_siblings_test.svm"));
				testWriters.put(firstRel+secondRel+"_siblings",bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(train){
			if(trainWriters.containsKey(secondRel+firstRel+"_siblings")){
				firstNode = sib2;
				sibling = sib1;
			}
		} else {
			if(testWriters.containsKey(secondRel+firstRel+"_siblings")){
				firstNode = sib2;
				sibling = sib1;
			}
		}
				// Classification of data point.
				String line = "";
				// +1 means that the firstNode goes before the sibling, -1 means that the firstNode goes after the sibling
				if (new Integer(firstNode) < new Integer(sibling)) {
						line += "1";
				} else {
						line += "-1";
					}
				 
				// Number of children of the head
				String headID = sentence.getHead(firstNode);
//				String numberOfChildren = ""+sentence.getChilds(headID).size();
//				line+= " 1:"+numberOfChildren;
				
//				String firstNodeHeight = ""+sentence.getDepth(firstNode);
//				line+= " 2:"+firstNodeHeight;
//				
//				String siblingHeight = ""+sentence.getDepth(sibling);
//				line+= " 3:"+siblingHeight;
//				
				
				String firstNodePOS = sentence.getPOS(firstNode);
				this.addNewSideFeatureDep("firstPOS= "+firstNodePOS);
				line+= " "+ sideFeatureTranslationDep.get("firstPOS= "+firstNodePOS);
				
				
				String siblingPOS = sentence.getPOS(sibling);
				this.addNewSideFeatureDep("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslationDep.get("siblingPOS= "+siblingPOS);
				
				
				String headPOS = sentence.getPOS(headID);
				this.addNewSideFeatureDep("headPOS= "+headPOS);
				line+= " "+sideFeatureTranslationDep.get("headPOS= "+headPOS);
				
				
				String firstNodeLemma = sentence.getLemma(firstNode);
				this.addNewSideFeatureDep("firstLemma= "+firstNodeLemma);
				line+=" "+sideFeatureTranslationDep.get("firstLemma= "+firstNodeLemma);
				
				
				String siblingLemma = sentence.getLemma(sibling);
				this.addNewSideFeatureDep("siblingLemma= "+siblingLemma);
				line+= " "+sideFeatureTranslationDep.get("siblingLemma= "+siblingLemma);
				
				
				String headLemma = sentence.getLemma(headID);
				this.addNewSideFeatureDep("headLemma= "+headLemma);
				line+=" "+sideFeatureTranslationDep.get("headLemma= "+headLemma);
				
				
				String headRel = sentence.getDeprel(headID);
				this.addNewSideFeatureDep("headRel= "+headRel);
				line+= " "+sideFeatureTranslationDep.get("headRel= "+headRel);
				
				
				String firstNodeRel = sentence.getDeprel(firstNode);
				this.addNewSideFeatureDep("firstNodeRel = "+firstNodeRel);
				line+= " "+sideFeatureTranslationDep.get("firstNodeRel = "+firstNodeRel);
				
				
				String siblingRel = sentence.getDeprel(sibling);
				this.addNewSideFeatureDep("siblingRel = "+ siblingRel);
				line+= " "+sideFeatureTranslationDep.get("siblingRel = "+ siblingRel);
				
				
//				ArrayList<String> otherSibs = sentence.getSiblingss(firstNode);
//				Iterator<String> sibIt = otherSibs.iterator();
//				while(sibIt.hasNext()){
//					String nextSib = sibIt.next();
//					if(!nextSib.equals(sibling)){
//						String otherSibRel = sentence.getDeprel(nextSib);
//						this.addNewSideFeatureDep("other sibling rel = " + otherSibRel);
//						if(!line.contains(sideFeatureTranslationDep.get("other sibling rel = " + otherSibRel))){
//							line+= " "+sideFeatureTranslationDep.get("other sibling rel = " + otherSibRel);
//						}
//					}
//				}
				
				
				ArrayList<String> otherSibs = sentence.getSiblingss(firstNode);
				Iterator<String> osibIt = otherSibs.iterator();
				while(osibIt.hasNext()){
					String nextSib = osibIt.next();
					if(!nextSib.equals(sibling)){
						String otherSibPOS = sentence.getPOS(nextSib);
						this.addNewSideFeatureDep("other sibling POS = " + otherSibPOS);
						if(!line.contains(sideFeatureTranslationDep.get("other sibling POS = " + otherSibPOS))){
							line+= " "+sideFeatureTranslationDep.get("other sibling POS = " + otherSibPOS);
						}
					}
				}
//				

				if (!train){
					String firstWord = sentence.getForm(firstNode);
					this.addNewSideFeatureDep("firstWord = "+ firstWord);
					line+= " "+sideFeatureTranslationDep.get("firstWord = "+ firstWord);
					
					String secondWord = sentence.getForm(sibling);
					this.addNewSideFeatureDep("secondWord = " + secondWord);
					line+= " "+sideFeatureTranslationDep.get("secondWord = " + secondWord);
					
					String sentenceForm = sentence.sentenceToString();
					this.addNewSideFeatureDep("sentence = " + sentenceForm);
					line+= " "+sideFeatureTranslationDep.get("sentence = " + sentenceForm);
				}
				
				try {
					
					if (train){	
						if(trainWriters.containsKey(firstRel+secondRel+"_siblings")){

							BufferedWriter bw=trainWriters.get(firstRel+secondRel+"_siblings");
							bw.write(line+"\n");

						} else{
							
							BufferedWriter bw=trainWriters.get(secondRel+firstRel+"_siblings");
							bw.write(line+"\n");
						}
					}
					else {
						
						if(testWriters.containsKey(firstRel+secondRel+"_siblings")){
							
							BufferedWriter bw2=testWriters.get(firstRel+secondRel+"_siblings");
							bw2.write(line+"\n");
							
						} else{
							
							BufferedWriter bw2=testWriters.get(secondRel+firstRel+"_siblings");
							bw2.write(line+"\n");
						}
						
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
			}
		
	public void addLineSingle(String childID, CoNLLHash sentence, boolean train, String filepath){
		String headID = sentence.getHead(childID);
		String childForm = sentence.getForm(childID);
		String headForm = sentence.getForm(headID);
		if (train && trainWriters.get("vertical")==null){
			relations.add("vertical");
			relationsNumbers.put("vertical",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter(filepath+"vertical.svm"));
				trainWriters.put("vertical",bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get("vertical")==null ){
			testRelations.add("vertical");
			relationsNumbers.put("vertical",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter(filepath+"vertical_test.svm"));
				testWriters.put("vertical",bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				// Classification of data point.
				String line = "";
				// +1 means that the child goes before the head, -1 means that the child goes after the head
				
				if (new Integer(childID) < new Integer(headID)) {
					line += "1";
				} else {
					line += "-1";
				}
				
//				NgramLanguageModel<String>  lm;
//				lm = ComputeLogProbabilityOfTextStream.readBinary(true, "vocab_cs", "eng.blm");
//				String option1 = childForm+" "+headForm;
//				List<String> list= Arrays.asList(option1.trim().split("\\s+"));
//				float logProb1 = lm.scoreSentence(list);
//				
//				NgramLanguageModel<String>  lm2;
//				lm = ComputeLogProbabilityOfTextStream.readBinary(true, "vocab_cs", "eng.blm");
//				String option2 = headForm+" "+childForm;
//				List<String> list2= Arrays.asList(option2.trim().split("\\s+"));
//				float logProb2 = lm.scoreSentence(list2);
				
//				if(logProb1 > logProb2){ // child goes before head
//					line+= " 1:1";
//				} else{
//					line+= " 1:0";
//				}
				
//				String numberOfChildren = ""+sentence.getWeight(headID);
//				featureTranslation.put("headDescendants= "+numberOfChildren,"1:"+numberOfChildren);
//				featureTranslationNumToWord.put("1:"+numberOfChildren,"headDescendants= "+numberOfChildren);
//				line+= " 1:"+numberOfChildren;
//				
//				String childWeight = ""+sentence.getWeight(childID);
//				featureTranslation.put("childWeight= "+numberOfChildren,"2:"+childWeight);
//				featureTranslationNumToWord.put("2:"+childWeight,"childWeight= "+numberOfChildren);
//				line+= " 2:"+childWeight;
				
				String childPOS = sentence.getPOS(childID);
				this.addNewFeature("childPOS= "+childPOS);
				line+= " "+ featureTranslation.get("childPOS= "+childPOS);
				
				String headPOS = sentence.getPOS(headID);
				this.addNewFeature("headPOS= "+headPOS);
				line+= " "+featureTranslation.get("headPOS= "+headPOS);
				
				
				String childRel = sentence.getDeprel(childID);
				this.addNewFeature("childRel = "+childRel);
				line+= " " + featureTranslation.get("childRel = "+childRel);


				
				
//				String childLemma = sentence.getLemma(childID);
//				this.addNewFeature("childLemma= "+childLemma);
//				line+=" "+featureTranslation.get("childLemma= "+childLemma);
////				
//				String headLemma = sentence.getLemma(headID);
//				this.addNewFeature("headLemma= "+headLemma);
//				line+= " "+featureTranslation.get("headLemma= "+headLemma);
				
//				String headRel = sentence.getDeprel(headID);
//				this.addNewFeature("headRel= "+headRel);
//				line+= " "+featureTranslation.get("headRel= "+headRel);
//				
				String childCluster = this.wordToCluster.get(childForm);
				this.addNewFeature("childCluster= "+childCluster);
				line+= " "+featureTranslation.get("childCluster= "+childCluster);
				
				String headCluster = this.wordToCluster.get(headForm);
				this.addNewFeature("headCluster= "+headCluster);
				line+= " "+featureTranslation.get("headCluster= "+headCluster);
				
//				ArrayList<String> otherSibs = sentence.getSiblingss(childID);
//				Iterator<String> sibIt = otherSibs.iterator();
//				while(sibIt.hasNext()){
//					String nextSib = sibIt.next();
//					String otherSibRel = sentence.getDeprel(nextSib);
//					this.addNewSideFeatureDep("other sibling rel = " + otherSibRel);
//					if(!line.contains(sideFeatureTranslationDep.get("other sibling rel = " + otherSibRel))){
//						line+= " "+sideFeatureTranslationDep.get("other sibling rel = " + otherSibRel);
//						
//					}
//				}
				try {
					
					if (train){	
						
						BufferedWriter bw=trainWriters.get("vertical");
						bw.write(line+"\n");
					}
					else {
						BufferedWriter bw2=testWriters.get("vertical");
						bw2.write(line+"\n");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	}
	
	
	/**
	 * Adds a line to the training/testing file that will be used to train/test the sibling classifier.
	 * 
	 * @param firstNode: the first word in the word pair that will be ordered.
	 * @param sibling: the second word in the word pair that will be ordered.
	 * @param sentence: the CoNLLHash object representing the sentence that the word pair appears in.
	 * @param train: boolean representing whether we are creating a training file or a test file.
	 */
	public void addSideLine(String firstNode, String sibling, CoNLLHash sentence, boolean train, String filepath){
		String headID = sentence.getHead(firstNode);
		String firstNodeForm = sentence.getForm(firstNode);
		String siblingForm = sentence.getForm(sibling);
		if (train && trainWriters.get("new_siblings")==null){
//			sibRelations.add("new_siblings");
			relationsNumbers.put("new_siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter(filepath+"siblings.svm"));
				trainWriters.put("new_siblings",bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//TODO: FIX THIS, IT CURRENTLY POINTS TO SIDEFEATUREMAP2
		} else if(!train && testWriters.get("new_siblings")==null ){
//			testSibRelations.add("new_siblings");
			relationsNumbers.put("new_siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter(filepath+"siblings_test.svm"));
				testWriters.put("new_siblings",bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				// Classification of data point.
				String line = "";
				// +1 means that the firstNode goes before the sibling, -1 means that the firstNode goes after the sibling
				if (new Integer(firstNode) < new Integer(sibling)) {
						line += "1";
				} else {
						line += "-1";
					}
				 
				
//				String firstNodeWeight = ""+sentence.getWeight(firstNode);
//				line+= " 1:"+firstNodeWeight;
//				
//				String siblingWeight = ""+sentence.getWeight(sibling);
//				line+= " 2:"+siblingWeight;
//				
				
				String firstNodePOS = sentence.getPOS(firstNode);
				this.addNewSideFeature("firstPOS= "+firstNodePOS);
				line+= " "+ sideFeatureTranslation.get("firstPOS= "+firstNodePOS);
				
				
				String siblingPOS = sentence.getPOS(sibling);
				this.addNewSideFeature("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslation.get("siblingPOS= "+siblingPOS);
//				
				String headPOS = sentence.getDeprel(headID);
				this.addNewSideFeature("headPOS= "+headPOS);
				line+= " "+sideFeatureTranslation.get("headPOS= "+headPOS);
				
//				String firstNodeLemma = sentence.getLemma(firstNode);
//				this.addNewSideFeature("firstLemma= "+firstNodeLemma);
//				line+=" "+sideFeatureTranslation.get("firstLemma= "+firstNodeLemma);
//				
//				
//				String siblingLemma = sentence.getLemma(sibling);
//				this.addNewSideFeature("siblingLemma= "+siblingLemma);
//				line+= " "+sideFeatureTranslation.get("siblingLemma= "+siblingLemma);
				
				
//				String headLemma = sentence.getLemma(headID);
//				this.addNewSideFeature("headLemma= "+headLemma);
//				line+=" "+sideFeatureTranslation.get("headLemma= "+headLemma);
				
				
				String firstNodeRel = sentence.getDeprel(firstNode);
				this.addNewSideFeature("firstNodeRel= "+firstNodeRel);
				line+= " "+sideFeatureTranslation.get("firstNodeRel= "+firstNodeRel);
//				
//				
				String siblingRel = sentence.getDeprel(sibling);
				this.addNewSideFeature("siblingRel= "+siblingRel);
				line+= " "+sideFeatureTranslation.get("siblingRel= "+siblingRel);
				
				
				String headRel = sentence.getDeprel(headID);
				this.addNewSideFeature("headRel= "+headRel);
				line+= " "+sideFeatureTranslation.get("headRel= "+headRel);
				
				
//				String firstNodeCluster = this.wordToCluster.get(firstNodeForm);
//				this.addNewSideFeature("firstNodeCluster= "+firstNodeCluster);
//				line+= " "+sideFeatureTranslation.get("firstNodeCluster= "+firstNodeCluster);
//				
//				String siblingCluster = this.wordToCluster.get(siblingForm);
//				this.addNewSideFeature("siblingCluster= "+siblingCluster);
//				line+= " "+sideFeatureTranslation.get("siblingCluster= "+siblingCluster);
//				
				

				try {
					
					if (train){	
						
						BufferedWriter bw=trainWriters.get("new_siblings");
						bw.write(line+"\n");
					}
					else {
						BufferedWriter bw2=testWriters.get("new_siblings");
						bw2.write(line+"\n");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				
			}
	
	/**
	 * Adds a line to the training/testing file that will be used to train/test a dependency relation classifier.
	 * 
	 * @param childID: the ID of the child in the child-head pair to be ordered.
	 * @param sentence: the CoNLLHash object representing the sentence that the child-head pair appears in.
	 * @param train: boolean used to determine whether we are creating a training file or a test file.
	 */
	public void addLine(String childID, CoNLLHash sentence, boolean train, String filepath){
		String relation = sentence.getDeprel(childID);
		String headID = sentence.getHead(childID);
		String childForm = sentence.getForm(childID);
		String headForm = sentence.getForm(headID);
		if (train && trainWriters.get(relation)==null){
			relations.add(relation);
			relationsNumbers.put(relation,nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter(filepath+"Training Files/"+relation+".svm"));
				trainWriters.put(relation,bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get(relation)==null ){
			testRelations.add(relation);
			relationsNumbers.put(relation,nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter(filepath+"Testing Files/"+relation+".svm"));
				testWriters.put(relation,bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
				// Classification of data point.
				String line = "";
				// +1 means that the child goes before the head, -1 means that the child goes after the head
				
				if (new Integer(childID) < new Integer(headID)) {
					line += "1";
				} else {
					line += "-1";
				}
				
				String headChildren = ""+sentence.getChilds(headID).size();
				String childChildren = ""+sentence.getChilds(childID).size();
				this.addNewFeature("headWeight= "+headChildren+" childWeight= "+childChildren);
				line+= " "+ featureTranslation.get("headWeight= "+headChildren+" childWeight= "+childChildren);
				
				
//				NgramLanguageModel<String>  lm;
//				lm = LmReaders.readLmBinary("eng.blm");
//				String option1 = childForm+" "+headForm;
//				List<String> list= Arrays.asList(option1.trim().split("\\s+"));
//				float logProb1 = lm.scoreSentence(list);
//			
//				String option2 = childForm+" "+headForm;
//				List<String> list2 = Arrays.asList(option2.trim().split("\\s+"));
//				float logProb2 = lm.scoreSentence(list2);
////				
//				if(logProb1 > logProb2){ // child goes before head
//					line+= " 1:1";
//				} else{
//					line+= " 1:0";
//				}
				
				
				
				
				String childPOS = sentence.getPOS(childID);
				this.addNewFeature("childPOS= "+childPOS);
				line+= " "+ featureTranslation.get("childPOS= "+childPOS);
				
				String headPOS = sentence.getPOS(headID);
				this.addNewFeature("headPOS= "+headPOS);
				line+= " "+featureTranslation.get("headPOS= "+headPOS);
				
				

				
				
				
				
//				String childLemma = sentence.getLemma(childID);
//				this.addNewFeature("childLemma= "+childLemma);
//				line+=" "+featureTranslation.get("childLemma= "+childLemma);
////				
//				String headLemma = sentence.getLemma(headID);
//				this.addNewFeature("headLemma= "+headLemma);
//				line+= " "+featureTranslation.get("headLemma= "+headLemma);
				
//				String headRel = sentence.getDeprel(headID);
//				this.addNewFeature("headRel= "+headRel);
//				line+= " "+featureTranslation.get("headRel= "+headRel);
//				
				String childCluster = this.wordToCluster.get(childForm);
				this.addNewFeature("childCluster= "+childCluster);
				line+= " "+featureTranslation.get("childCluster= "+childCluster);
				
				String headCluster = this.wordToCluster.get(headForm);
				this.addNewFeature("headCluster= "+headCluster);
				line+= " "+featureTranslation.get("headCluster= "+headCluster);
				
//				ArrayList<String> otherSibs = sentence.getSiblingss(childID);
//				Iterator<String> sibIt = otherSibs.iterator();
//				while(sibIt.hasNext()){
//					String nextSib = sibIt.next();
//					String otherSibRel = sentence.getDeprel(nextSib);
//					this.addNewSideFeatureDep("other sibling rel = " + otherSibRel);
//					if(!line.contains(sideFeatureTranslationDep.get("other sibling rel = " + otherSibRel))){
//						line+= " "+sideFeatureTranslationDep.get("other sibling rel = " + otherSibRel);
//						
//					}
//				}
				try {
					
					if (train){	
						
						BufferedWriter bw=trainWriters.get(relation);
						bw.write(line+"\n");
					}
					else {
						BufferedWriter bw2=testWriters.get(relation);
						bw2.write(line+"\n");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	}
}
			
				
			
	
	

