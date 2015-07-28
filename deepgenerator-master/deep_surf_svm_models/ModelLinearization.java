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
	private ArrayList<String> testRelations = new ArrayList<String>(); // An ArrayList containing the dependency relations for which test files were created.
	private HashMap <String, Integer> relationsNumbers = new HashMap <String,Integer>(); 
	
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
		if (train && trainWriters.get("siblings")==null){
			relations.add("siblings");
			relationsNumbers.put("siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("linearization_svm_siblings.svm"));
				trainWriters.put("siblings",bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get("siblings")==null ){
			testRelations.add("siblings");
			relationsNumbers.put("siblings",nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("test_"+"siblings"+".svm"));
				testWriters.put("siblings",bwTest);
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
				line+= " "+ sideFeatureTranslation.get("firstPOS= "+firstNodePOS);
				
				String siblingPOS = sentence.getPOS(sibling);
				this.addNewSideFeature("siblingPOS= "+siblingPOS);
				line+= " "+sideFeatureTranslation.get("siblingPOS= "+siblingPOS);
				
				String firstNodeLemma = sentence.getLemma(firstNode);
				this.addNewSideFeature("firstLemma= "+firstNodeLemma);
				line+=" "+sideFeatureTranslation.get("firstLemma= "+firstNodeLemma);
				
				String siblingLemma = sentence.getLemma(sibling);
				this.addNewSideFeature("siblingLemma= "+siblingLemma);
				line+= " "+sideFeatureTranslation.get("siblingLemma= "+siblingLemma);
				
				String headPOS = sentence.getPOS(headID);
				this.addNewSideFeature("headPOS= "+headPOS);
				line+=" "+sideFeatureTranslation.get("headPOS= "+headPOS);
				
				String headLemma = sentence.getLemma(headID);
				this.addNewSideFeature("headLemma= "+headLemma);
				line+=" "+sideFeatureTranslation.get("headLemma= "+headLemma);

				try {
					
					if (train){	
						
						BufferedWriter bw=trainWriters.get("siblings");
						bw.write(line+"\n");
					}
					else {
						BufferedWriter bw2=testWriters.get("siblings");
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
	public void addLine3(String childID, CoNLLHash sentence, boolean train){
		String relation = sentence.getDeprel(childID);
		String headID = sentence.getHead(childID);
		if (train && trainWriters.get(relation)==null){
			relations.add(relation);
			relationsNumbers.put(relation,nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTrain = new BufferedWriter(new FileWriter("linearization_svm_"+relation+".svm"));
				trainWriters.put(relation,bwTrain);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(!train && testWriters.get(relation)==null ){
			testRelations.add(relation);
			relationsNumbers.put(relation,nextRelation);
			nextRelation++;
			try {
				BufferedWriter bwTest=new BufferedWriter(new FileWriter("test_"+relation+".svm"));
				testWriters.put(relation,bwTest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Classification of data point.
				String line = "";
				// +1 means that the child goes before the head, -1 means that the child goes after the head
				if (train || !train) {
					if (new Integer(childID) < new Integer(headID)) {
						line += "1";
					} else {
						line += "-1";
					}
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
			
				
			
	
	

