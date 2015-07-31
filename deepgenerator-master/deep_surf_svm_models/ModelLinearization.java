package deep_surf_svm_models;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import deep_to_surf.CoNLLHash;

/**
 * ModelLinearization is a class that is used to create training and testing files for dependency relation classifiers. 
 * Each dependency relation has a classifier that, given two words and additional information, outputs their order. 
 * 
 * @author Jose Ignacio Velarde
 *
 */
public class ModelLinearization {

	int nextRelation = 0; 
	int nextFeat = 3;
	int nextSideFeat = 4;
	
	private HashMap <String, String> sideFeatureTranslation = new HashMap <String, String>(); // A dictionary mapping features to their index in the sibling classifier.
	private HashMap <String, String> featureTranslation = new HashMap <String, String>(); // A dictionary mapping features to their index in the dependency relations classifier.
	private HashMap <String, BufferedWriter> trainWriters; // A dictionary mapping each dependency relation to the BufferedWriter that will write the training file for its classifier.
	private HashMap <String, BufferedWriter> testWriters; // A dictionary mapping each dependency relation to the BufferedWriter that will write the test file for its classifier.
	private ArrayList <String> relations = new ArrayList <String>(); // An ArrayList containing the dependency relations for which classifiers were created.
	private ArrayList <String> sibRelations = new ArrayList<String>();
	private ArrayList<String> testRelations = new ArrayList<String>(); // An ArrayList containing the dependency relations for which test files were created.
	private ArrayList<String> testSibRelations = new ArrayList<String>();
	private HashMap <String, Integer> relationsNumbers = new HashMap <String,Integer>(); 
	private HashMap <String, String> sideFeatureTranslation2 = new HashMap <String, String>();
	
	public ModelLinearization() {
		
		trainWriters = new HashMap <String, BufferedWriter>();
		testWriters = new HashMap <String, BufferedWriter>();
		
	}
	
	
	/**
	 * @return an array list of strings containing the relations for which test files were created.
	 */
	public ArrayList<String> getTestRelations(){
		return new ArrayList<String>(this.testRelations);
	}
	
	/**
	 * @return an array list of strings containing the relations for which classifiers were trained.
	 */
	public ArrayList<String> getRelations(){
		return new ArrayList<String>(this.relations);
	}
	
	public ArrayList<String> getTestSibRelations(){
		return new ArrayList<String>(this.testSibRelations);
	}
	
	public void setSideFeatMap(HashMap<String, String> map){
		this.sideFeatureTranslation2 = map;
	}
	
	public ArrayList<String> getSibRelations(){
		return new ArrayList<String>(this.sibRelations);
	}
	
	/**
	 * @return a HashMap that maps a feature to its index in the feature vector.
	 */
	public HashMap<String, String> getFeaturesDict(){
		return featureTranslation;
	}
	
	/**
	 * @return a HashMap that maps a feature to its index in the feature vector for the sibling classifier.
	 */
	public HashMap<String, String> getSideFeaturesDict(){
		return this.sideFeatureTranslation;
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
		}
		
	}
	
	public void addSideLineDep(String sib1, String sib2, CoNLLHash sentence, boolean train){
		String firstNode = sib1;
		String sibling = sib2;
		String firstRel = sentence.getDeprel(sib1);
		String secondRel = sentence.getDeprel(sib2);
		if (train && (trainWriters.get(firstRel+secondRel+"_siblings")==null && trainWriters.get(secondRel+firstRel+"_siblings")==null)){
			sibRelations.add(firstRel+secondRel+"_siblings");
			relationsNumbers.put(firstRel+secondRel+"_siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("Sibling Dep Training Files/"+firstRel+secondRel+"_siblings.svm"));
				trainWriters.put(firstRel+secondRel+"_siblings",bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && (testWriters.get(firstRel+secondRel+"_siblings")==null && testWriters.get(secondRel+firstRel+"_siblings")==null)){
			testSibRelations.add(firstRel+secondRel+"_siblings");
			relationsNumbers.put(firstRel+secondRel+"_siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("Sibling Dep Testing Files/"+firstRel+secondRel+"_siblings_test.svm"));
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
			if(testWriters.containsKey(secondRel+firstRel+"_siblings_test.svm")){
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
				String numberOfChildren = ""+sentence.getChilds(headID).size();
				line+= " 1:"+numberOfChildren;
				
				String firstNodeHeight = ""+sentence.getDepth(firstNode);
				line+= " 2:"+firstNodeHeight;
				
				String siblingHeight = ""+sentence.getDepth(sibling);
				line+= " 3:"+siblingHeight;
				
				
				String firstNodePOS = sentence.getPOS(firstNode);
				this.addNewSideFeature("firstPOS= "+firstNodePOS);
				line+= " "+ sideFeatureTranslation.get("firstPOS= "+firstNodePOS);
				
				
				String siblingPOS = sentence.getPOS(sibling);
				this.addNewSideFeature("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslation.get("siblingPOS= "+siblingPOS);
				
				
				String headPOS = sentence.getPOS(headID);
				this.addNewSideFeature("headPOS= "+headPOS);
				line+= " "+sideFeatureTranslation.get("headPOS= "+headPOS);
				
				
				String firstNodeLemma = sentence.getLemma(firstNode);
				this.addNewSideFeature("firstLemma= "+firstNodeLemma);
				line+=" "+sideFeatureTranslation.get("firstLemma= "+firstNodeLemma);
				
				
				String siblingLemma = sentence.getLemma(sibling);
				this.addNewSideFeature("siblingLemma= "+siblingLemma);
				line+= " "+sideFeatureTranslation.get("siblingLemma= "+siblingLemma);
				
				
				String headLemma = sentence.getLemma(headID);
				this.addNewSideFeature("headLemma= "+headLemma);
				line+=" "+sideFeatureTranslation.get("headLemma= "+headLemma);
				
				
				String headRel = sentence.getDeprel(headID);
				this.addNewSideFeature("headRel= "+headRel);
				line+= " "+sideFeatureTranslation.get("headRel= "+headRel);
				

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
		
	/**
	 * Adds a line to the training/testing file that will be used to train/test the sibling classifier.
	 * 
	 * @param firstNode: the first word in the word pair that will be ordered.
	 * @param sibling: the second word in the word pair that will be ordered.
	 * @param sentence: the CoNLLHash object representing the sentence that the word pair appears in.
	 * @param train: boolean representing whether we are creating a training file or a test file.
	 */
	public void addSideLine(String firstNode, String sibling, CoNLLHash sentence, boolean train){
		String headID = sentence.getHead(firstNode);
		if (train && trainWriters.get("new_siblings")==null){
			sibRelations.add("new_siblings");
			relationsNumbers.put("new_siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("new_siblings.svm"));
				trainWriters.put("new_siblings",bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//TODO: FIX THIS, IT CURRENTLY POINTS TO SIDEFEATUREMAP2
		} else if(!train && testWriters.get("new_siblings")==null ){
			testSibRelations.add("new_siblings");
			relationsNumbers.put("new_siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("new_siblings_test.svm"));
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
				 
				// Number of children of the head
				String numberOfChildren = ""+sentence.getChilds(headID).size();
				line+= " 1:"+numberOfChildren;
				
				String firstNodeHeight = ""+sentence.getDepth(firstNode);
				line+= " 2:"+firstNodeHeight;
				
				String siblingHeight = ""+sentence.getDepth(sibling);
				line+= " 3:"+siblingHeight;
				
				
				String firstNodePOS = sentence.getPOS(firstNode);
				this.addNewSideFeature("firstPOS= "+firstNodePOS);
				line+= " "+ sideFeatureTranslation2.get("firstPOS= "+firstNodePOS);
				
				
				String siblingPOS = sentence.getPOS(sibling);
				this.addNewSideFeature("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslation2.get("siblingPOS= "+siblingPOS);
				
				
				String firstNodeLemma = sentence.getLemma(firstNode);
				this.addNewSideFeature("firstLemma= "+firstNodeLemma);
				line+=" "+sideFeatureTranslation2.get("firstLemma= "+firstNodeLemma);
				
				
				String siblingLemma = sentence.getLemma(sibling);
				this.addNewSideFeature("siblingLemma= "+siblingLemma);
				line+= " "+sideFeatureTranslation2.get("siblingLemma= "+siblingLemma);
				
				
				String headLemma = sentence.getLemma(headID);
				this.addNewSideFeature("headLemma= "+headLemma);
				line+=" "+sideFeatureTranslation2.get("headLemma= "+headLemma);
				
				
				String firstNodeRel = sentence.getDeprel(firstNode);
				this.addNewSideFeature("firstNodeRel= "+firstNodeRel);
				line+= " "+sideFeatureTranslation2.get("firstNodeRel= "+firstNodeRel);
				
				
				String siblingRel = sentence.getDeprel(sibling);
				this.addNewSideFeature("siblingRel= "+siblingRel);
				line+= " "+sideFeatureTranslation2.get("siblingRel= "+siblingRel);
				
				
				String headRel = sentence.getDeprel(headID);
				this.addNewSideFeature("headRel= "+headRel);
				line+= " "+sideFeatureTranslation2.get("headRel= "+headRel);
				
				
				String headPOS = sentence.getDeprel(headID);
				this.addNewSideFeature("headPOS= "+headPOS);
				line+= " "+sideFeatureTranslation2.get("headPOS= "+headPOS);

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
	public void addLine(String childID, CoNLLHash sentence, boolean train){
		String relation = sentence.getDeprel(childID);
		String headID = sentence.getHead(childID);
		if (train && trainWriters.get(relation)==null){
			relations.add(relation);
			relationsNumbers.put(relation,nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("NewTraining/train_"+relation+".svm"));
				trainWriters.put(relation,bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get(relation)==null ){
			testRelations.add(relation);
			relationsNumbers.put(relation,nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("NewTesting/test_"+relation+".svm"));
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
				
				
				String numberOfChildren = ""+sentence.getChilds(headID).size();
				line+= " 1:"+numberOfChildren;
				
				String childHeight = ""+sentence.getDepth(childID);
				line+= " 2:"+childHeight;
				
				String childPOS = sentence.getPOS(childID);
				this.addNewFeature("childPOS= "+childPOS);
				line+= " "+ featureTranslation.get("childPOS= "+childPOS);
				
				String headPOS = sentence.getPOS(headID);
				this.addNewFeature("headPOS= "+headPOS);
				line+= " "+featureTranslation.get("headPOS= "+headPOS);
				
				String childLemma = sentence.getLemma(childID);
				this.addNewFeature("childLemma= "+childLemma);
				line+=" "+featureTranslation.get("childLemma= "+childLemma);
				
				String headLemma = sentence.getLemma(headID);
				this.addNewFeature("headLemma= "+headLemma);
				line+= " "+featureTranslation.get("headLemma= "+headLemma);
				
				ArrayList<String> siblings = sentence.getChilds(headID);
				siblings.remove(childID);
				Iterator<String> it = siblings.iterator();
				int sibNumber = 1;
				while(it.hasNext()){
					String sibling = it.next();
					String sibRelation = sentence.getDeprel(sibling);
					this.addNewFeature("sibling"+""+sibNumber+"= "+sibRelation);
					line+= " "+featureTranslation.get("sibling"+""+sibNumber+"= "+sibRelation);
				}
				
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
			
				
			
	
	

