package testing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import svm_utils.svm_predict;
import svm_utils.svm_train;
import deep_surf_svm_models.ModelLinearization;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;

/**
 * ModelTrainer is a class that is used to create testing and training files as well as to train and test classifiers for
 * dependency relations and siblings. Each dependency relation has a classifier that is used to determine the word order 
 * between a word and its governor, depending on their dependency relation. Word order amongst siblings is determined using
 * the sibling classifier.
 *  
 * @author Jose Ignacio Velarde
 *
 */
public class ModelTrainer {

	private String trainingFile;
	private String testFile;
	private ModelLinearization linModel;
	
	public ModelTrainer(String filePath){
		this.trainingFile = filePath;
		linModel = new ModelLinearization();
	}
	
	/**
	 * 
	 * @param filePath: name of the file that will be used to create the training files.
	 * @param testPath: name of the file that will be used to create the test files.
	 */
	public ModelTrainer(String filePath, String testPath){
		this.trainingFile = filePath;
		this.testFile = testPath;
		linModel = new ModelLinearization();
	}
	
	public static void main(String [] args){
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		
		ModelTrainer transducer = new ModelTrainer("TrainingSet","TestingSet");
		System.out.println("Start Time: "+sdf.format(cal.getTime()));
		
		
		System.out.println("Starting evaluation");
		transducer.sideTesting();

		Calendar endCal = Calendar.getInstance();
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
		
		System.out.println("End Time: "+sdf1.format(endCal.getTime()));
		System.out.println("Finished");
		
		
	}
	
	/**
	 * Creates files to train the dependency relation classifiers.
	 */
	public void generateTrainingFiles(){
		ArrayList<CoNLLHash> sentences = CoNLLTreeConstructor.storeTreebank(trainingFile);
		boolean train = true;
		Iterator<CoNLLHash> sentenceIt = sentences.iterator();
		int sentenceNum = 0;
		while(sentenceIt.hasNext()){
			CoNLLHash sentence = sentenceIt.next();
			ArrayList<String> leaves = sentence.getLeaves();
			ArrayDeque<String> queue = new ArrayDeque<String>();
			ArrayList<String> checkedNodes = new ArrayList<String>();
			queue.addAll(leaves);
			while(!queue.isEmpty()){
				String nextNode = (String) queue.removeFirst();
				if(!sentence.getDeprel(nextNode).equals("ROOT")){
					String head = sentence.getHead(nextNode);
					linModel.addLine(nextNode, sentence, train);
					
					if(!checkedNodes.contains(head)){
						queue.addLast(head);
					}
					checkedNodes.add(nextNode);
				}
				
			}
			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" training sentences");
		linModel.closeBuffers(train);
	}
	
	/**
	 * Creates test files to evaluate the accuracy of the dependency relation classifiers.
	 */
	public void generateTestingFiles(){
		
		ArrayList<CoNLLHash> sentences = CoNLLTreeConstructor.storeTreebank(testFile);
		boolean train = false;
		Iterator<CoNLLHash> sentenceIt = sentences.iterator();
		int sentenceNum = 0;
		while(sentenceIt.hasNext()){
			CoNLLHash sentence = sentenceIt.next();
			ArrayList<String> leaves = sentence.getLeaves();
			ArrayDeque<String> queue = new ArrayDeque<String>();
			ArrayList<String> checkedNodes = new ArrayList<String>();
			queue.addAll(leaves);
			while(!queue.isEmpty()){
				String nextNode = (String) queue.removeFirst();
				if(!sentence.getDeprel(nextNode).equals("ROOT")){
					String head = sentence.getHead(nextNode);
						linModel.addLine(nextNode, sentence, train);
					if(!checkedNodes.contains(head)){
						queue.addLast(head);
					}
					checkedNodes.add(nextNode);
				}
				
			}
			
			sentenceNum++;
			
		}
		String numOfSent = ""+sentenceNum;
		System.out.println(numOfSent+" testing sentences");
		linModel.closeBuffers(train);
		
		
	}
	
	/**
	 * Creates a file to train the sibling classifier.
	 */
	public void generateSideTrainingFile(){
		ArrayList<CoNLLHash> sentences = CoNLLTreeConstructor.storeTreebank(trainingFile);
		boolean train = true;
		Iterator<CoNLLHash> sentenceIt = sentences.iterator();
		int sentenceNum = 0;
		while(sentenceIt.hasNext()){
			CoNLLHash sentence = sentenceIt.next();
			ArrayList<String> wordIDs = sentence.getIds();
			Iterator<String> wordIt = wordIDs.iterator();
			ArrayList<String> nonOnlyChilds = new ArrayList<String>();
			while(wordIt.hasNext()){ // Gets non only child nodes.
				String currentWord = wordIt.next();
				if(sentence.getSiblingss(currentWord).size()>0){
					nonOnlyChilds.add(currentWord);
				}
			}
			ArrayDeque<String> queue = new ArrayDeque<String>();
			queue.addAll(nonOnlyChilds);
			ArrayList<String> checkedPairs = new ArrayList<String>();
			while(!queue.isEmpty()){
				String word = (String) queue.removeFirst();
				ArrayList<String> siblings = sentence.getSiblingss(word);
				Iterator<String> sibIt = siblings.iterator();
				while(sibIt.hasNext()){
					String sibling = sibIt.next();
					if(!checkedPairs.contains(sibling+word)){
					linModel.addSideLine(word, sibling, sentence, train);
					checkedPairs.add(sibling+word);
					checkedPairs.add(word+sibling);
					}
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" training sentences");
		linModel.closeBuffers(train);
	}
	
	/**
	 * Creates a file to test the accuracy of the sibling classifier.
	 */
	public void generateSideTestingFile(){
		ArrayList<CoNLLHash> sentences = CoNLLTreeConstructor.storeTreebank(testFile);
		boolean train = false;
		Iterator<CoNLLHash> sentenceIt = sentences.iterator();
		int sentenceNum = 0;
		while(sentenceIt.hasNext()){
			CoNLLHash sentence = sentenceIt.next();
			ArrayList<String> wordIDs = sentence.getIds();
			Iterator<String> wordIt = wordIDs.iterator();
			ArrayList<String> nonOnlyChilds = new ArrayList<String>();
			while(wordIt.hasNext()){ // Gets non only child nodes.
				String currentWord = wordIt.next();
				if(sentence.getSiblingss(currentWord).size()>0){
					nonOnlyChilds.add(currentWord);
				}
			}
			Iterator<String> otherIt = nonOnlyChilds.iterator();
			while(otherIt.hasNext()){
				String word = otherIt.next();
				ArrayList<String> siblings = sentence.getSiblingss(word);
				Iterator<String> sibIt = siblings.iterator();
				while(sibIt.hasNext()){
					String sibling = sibIt.next();
					linModel.addSideLine(word, sibling, sentence, train);
					
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" side testing sentences");
		linModel.closeBuffers(train);
	}
	
	/**
	 * This method trains the sibling classifier.
	 */
	public void sideTraining(){
		this.generateSideTrainingFile();
		System.out.println("Done generating training files, starting training");
		ArrayList<String> sibRelations = this.linModel.getSibRelations();
		Iterator<String> it = sibRelations.iterator();
		while(it.hasNext()){
			String relation = it.next();
			String[] args = new String[28];
			args[0]="-s";
			args[1]="0";
			args[2]="-t";
			args[3]="1";
			args[4]="-d";
			args[5]="2";
			args[6]="-g";
			args[7]="0.2";
			args[8]="-r";
			args[9]="0.0";
			args[10]="-n";
			args[11]="0.5";
			args[12]="-n";
			args[13]="0.5";
			args[14]="-m";
			args[15]="100";
			args[16]="-c";
			args[17]="500.0";
			args[18]="-e";
			args[19]="1.0";
			args[20]="-p";
			args[21]="0.1";
			args[22]="-h";
			args[23]="1";
			args[24]="-b";
			args[25]="0";
			args[26]="-q";
			 
			args[27]= relation+".svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		HashMap<String,String> sideRels = linModel.getSideFeaturesDict();
	    try
        {
               FileOutputStream fos = new FileOutputStream("sideRelMap1.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(sideRels);
               oos.close();
               fos.close();
               System.out.printf("Serialized HashMap data is saved in sideRelMap1.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		
	}

	/**
	 * This method evaluates the performance of the sibling classifier.
	 */
	public void sideTesting(){
		this.generateSideTestingFile();
		ArrayList<String> sibTrainRelations = this.linModel.getSibRelations();
		ArrayList<String> testRelations = this.linModel.getTestSibRelations();
		Iterator<String> relIt = testRelations.iterator();
		System.out.println(testRelations.toString());
		while(relIt.hasNext()){
			String relation = relIt.next();
			if(sibTrainRelations.contains(relation)){
				System.out.println(relation);
				String[] args = new String[4];
				args[0] = relation+"_test.svm";
				args[1] = relation+".svm.model";
				args[2] = "linearization_output_"+relation+".svm";
				try {
					svm_predict.main(args);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method evaluates the performance of all of the classifiers for which test files were made.
	 * It prints out the accuracy of each classifier in classifying the given test set. It also prints
	 * all the relations for which test files were made
	 */
	public void testing(){
		ArrayList<String> trainRelations = this.linModel.getRelations();
		ArrayList<String> relations = this.linModel.getTestRelations();
		Iterator<String> relIt = relations.iterator();
		System.out.println(relations.toString());
		while(relIt.hasNext()){
			String relation = relIt.next();
			if(trainRelations.contains(relation)){
				System.out.println(relation);
				String[] args = new String[4];
				args[0] = "test_"+relation+".svm";
				args[1] = "linearization_svm_"+relation+".svm.model";
				args[2] = "linearization_output_"+relation+".svm";
				try {
					svm_predict.main(args);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method trains classifiers for all the relations that appear in a training set.
	 */
	public void training(){
		this.generateTrainingFiles();
		ArrayList<String> relations = this.linModel.getRelations();
		Iterator<String> it = relations.iterator();
		while(it.hasNext()){
			String relation = it.next();
			String[] args = new String[28];
			args[0]="-s";
			args[1]="0";
			args[2]="-t";
			args[3]="1";
			args[4]="-d";
			args[5]="2";
			args[6]="-g";
			args[7]="0.2";
			args[8]="-r";
			args[9]="0.0";
			args[10]="-n";
			args[11]="0.5";
			args[12]="-n";
			args[13]="0.5";
			args[14]="-m";
			args[15]="100";
			args[16]="-c";
			args[17]="500.0";
			args[18]="-e";
			args[19]="1.0";
			args[20]="-p";
			args[21]="0.1";
			args[22]="-h";
			args[23]="1";
			args[24]="-b";
			args[25]="0";
			args[26]="-q";
			 
			args[27]= "linearization_svm_"+relation+".svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		HashMap<String,String> rels = linModel.getFeaturesDict();
	    try
        {
               FileOutputStream fos = new FileOutputStream("relmap.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               oos.writeObject(rels);
               oos.close();
               fos.close();
               System.out.printf("Serialized HashMap data is saved in relmap.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		System.out.println(rels.toString());
	}
}
