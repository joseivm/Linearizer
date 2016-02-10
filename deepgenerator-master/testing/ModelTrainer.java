package testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
	private String filepath;
	private ModelLinearization linModel;
	private ModelLinearization linModelPOS;
	private ModelLinearization linModelDep;
	private ModelLinearization linModelHeadDeprel;
	private ArrayList<String> classList;
	private HashMap<String,String> trainingSetDict; 
	private HashMap<String,Integer> countingDict;
	private HashMap<String,String> testingSetDict;
	
	
//	TODO: CHANGE FILE SETTINGS OF FILES

	public ModelTrainer(String filePath){
		this.trainingFile = filePath;
		linModel = new ModelLinearization();
	}
	
	/**
	 * 
	 * @param filePath: name of the file that will be used to create the training files.
	 * @param testPath: name of the file that will be used to create the test files.
	 */
	public ModelTrainer(String filePath, String testPath,String location){
		this.trainingFile = filePath;
		this.testFile = testPath;
		this.filepath = location;
		linModel = new ModelLinearization();
		linModelPOS = new ModelLinearization();
		linModelDep = new ModelLinearization();
		linModelHeadDeprel = new ModelLinearization();
		
	}
	
	public static void main(String [] args) throws IOException{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
		ModelTrainer trainer = new ModelTrainer("Sentences/New Bigger Training Set","Sentences/New Small Testing Set", "December Vertical Files/Non Singleton/1000 Cluster POS Complex/");
//		ModelTrainer transducer = new ModelTrainer("Sentences/TrainingSetHalf","Sentences/Smaller Testing Set");
		System.out.println("Start Time: "+sdf.format(cal.getTime()));
		System.out.println("Starting Training");
//		transducer.linModelDep;
		
		trainer.training();
		
		System.out.println("Starting Testing");
//		transducer.sideTestingDep();
		
		trainer.testing();

		Calendar endCal = Calendar.getInstance();
		SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
		
		System.out.println("End Time: "+sdf1.format(endCal.getTime()));
		System.out.println("Finished");
		
		
	}
	
	public void help(){
		try
	      {
	         FileInputStream headPOSListInputStream = new FileInputStream("Sibling POS Files/headPOSList.ser");
	         
	         ObjectInputStream headPOSListObjectStream = new ObjectInputStream(headPOSListInputStream);
	         
	         ArrayList<String> headPOSList = (ArrayList) headPOSListObjectStream.readObject();
	         classList = headPOSList;
	         
	         headPOSListObjectStream.close();
	         headPOSListInputStream.close();
	         
	      }catch(IOException ioe)
	      {
	         ioe.printStackTrace();
	         return;
	      } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void generateSideTrainingFileDep(){
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
						linModelDep.addSideLineDep(word, sibling, sentence, train);
						checkedPairs.add(sibling+word);
						checkedPairs.add(word+sibling);
					}
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" training sentences");
		linModelDep.closeBuffers(train);
	}
	
	public void generateSideTrainingFilePOS(){
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
						linModelPOS.addSideLinePOS(word, sibling, sentence, train);
						checkedPairs.add(sibling+word);
						checkedPairs.add(word+sibling);
					}
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" training sentences");
		linModelPOS.closeBuffers(train);
	}
	
	public void generateSideTrainingFileHeadDeprel(){
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
						linModelHeadDeprel.addSideLineHeadDeprel(word, sibling, sentence, train);
						checkedPairs.add(sibling+word);
						checkedPairs.add(word+sibling);
					}
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" training sentences");
		linModelHeadDeprel.closeBuffers(train);
	}
	
	public void generateSideTestingFileDep(){
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
					linModelDep.addSideLineDep(word, sibling, sentence, train);
					
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" side testing sentences");
		linModelDep.closeBuffers(train);
	}
	
	public void generateSideTestingFilePOS(){
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
					linModelPOS.addSideLinePOS(word, sibling, sentence, train);
					
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" side testing sentences");
		linModelPOS.closeBuffers(train);
	}

	public void generateSideTestingFileHeadDeprel(){
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
					linModelHeadDeprel.addSideLineHeadDeprel(word, sibling, sentence, train);
					
				}
			}
			
			

			
			sentenceNum++;
			
		}
		String output = ""+sentenceNum;
		System.out.println(output+" side testing sentences");
		linModelHeadDeprel.closeBuffers(train);
	}
	
	public void sideTrainingDep(){
		this.generateSideTrainingFileDep();
		System.out.println("Done generating training files, starting training");
		ArrayList<String> sibRelations = this.linModelDep.getSibRelations();
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
			 
			args[27]= "Sibling Dep Files/POS/Training Files/"+relation+".svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
		
	}
	
	public void sideTrainingPOS(){
		this.generateSideTrainingFilePOS();
		System.out.println("Done generating training files, starting training");
		ArrayList<String> sibHeadPOS = this.linModelPOS.getSibHeadPOS();
		Iterator<String> it = sibHeadPOS.iterator();
		while(it.hasNext()){
			String headPOS = it.next();
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
			 
			args[27]= headPOS+"_train.svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
	}
	
	public void sideTrainingHeadDeprel(){
		this.generateSideTrainingFileHeadDeprel();
		System.out.println("Done generating training files, starting training");
		ArrayList<String> headDeprels = this.linModelHeadDeprel.getHeadDeprels();
		Iterator<String> it = headDeprels.iterator();
		while(it.hasNext()){
			String headDeprel = it.next();
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
			 
			args[27]= "Sibling HeadDeprel Files/Training Files/"+headDeprel+"_train.svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}
	
	public void sideTestingDep(){
		this.generateSideTestingFileDep();
		ArrayList<String> sibTrainRelations = this.linModelDep.getSibRelations();
		ArrayList<String> testRelations = this.linModelDep.getTestSibRelations();
		Iterator<String> relIt = testRelations.iterator();
		System.out.println("Number of Classifiers: "+testRelations.size());
		while(relIt.hasNext()){
			String relationPair = relIt.next();
			File tempFile = new File("Sibling Dep Files/POS/Training Files/"+relationPair+".svm");
			if(tempFile.exists()){
				System.out.println(relationPair);
				String[] args = new String[4];
				args[0] = "Sibling Dep Files/POS/Testing Files/"+relationPair+"_test.svm"; //testing file
				args[1] =  relationPair+".svm.model"; // classifier file
				args[2] = "Sibling Dep Files/POS/Output Files/"+relationPair+"_output"; //output file
				try {
					svm_predict.main(args);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		HashMap<String, String> backwardsSideRels = linModelDep.getBackwardsMapDep();
		HashMap<String,String> sideRels = linModelDep.getSideFeaturesDictDep();
	    try
        {
               FileOutputStream fos = new FileOutputStream("Sibling Dep Files/POS/Object Files/sideFeatureDictDep101815.ser");
               FileOutputStream fos2 = new FileOutputStream("Sibling Dep Files/POS/Object Files/backwardsMapDep101815.ser");
               
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
               
               oos.writeObject(sideRels);
               oos2.writeObject(backwardsSideRels);
               oos.close();
               oos2.close();
               fos.close();
               fos2.close();
               System.out.println("Serialized HashMap data is saved in sideFeatureDictDep101815.ser");
               System.out.println("Serialized backwards HashMap data is saved in backwardsMapDep101815.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		
		ArrayList<String> relationPairList = linModelDep.getTestSibRelations();
	    try
        {
               FileOutputStream fos = new FileOutputStream("Sibling Dep Files/POS/Object Files/sibRelationPairsDepList.ser");
               
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               
               oos.writeObject(relationPairList);
               oos.close();
               fos.close();
               System.out.println("Serialized List data is saved in sibRelationPairsDepList.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		
	}
	
	public void sideTestingPOS(){
		this.generateSideTestingFilePOS();
		ArrayList<String> sibTrainHeadPOS = this.linModelPOS.getSibHeadPOS();
		ArrayList<String> testSibHeadPOS = this.linModelPOS.getTestSibHeadPOS();
		
		Iterator<String> relIt = testSibHeadPOS.iterator();
		System.out.println(testSibHeadPOS.toString());
		while(relIt.hasNext()){
			String headPOS = relIt.next();
			if(testSibHeadPOS.contains(headPOS)&&sibTrainHeadPOS.contains(headPOS)){
				System.out.println(headPOS);
				String[] args = new String[4];
				args[0] = headPOS+"_test.svm"; //testing file
				args[1] =  headPOS+"_train.svm.model"; // classifier file
				args[2] = "Sibling POS Files/Output Files/"+headPOS+"_output"; //output file
				try {
					svm_predict.main(args);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		HashMap<String, String> backwardsMap = linModelPOS.getBackwardsMapPOS();
		HashMap<String,String> sideRels = linModelPOS.getSideFeaturesDictPOS();
	    try
        {
               FileOutputStream fos = new FileOutputStream("sideFeatureDictPOS092215.ser");
               FileOutputStream fos2 = new FileOutputStream("backwardsMapPOS092215.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
               oos.writeObject(sideRels);
               oos2.writeObject(backwardsMap);
               oos.close();
               oos2.close();
               fos.close();
               fos2.close();
               System.out.println("Serialized HashMap data is saved in sideFeatureDictPOS092215.ser");
               System.out.println("Serialized backwards HashMap data is saved in backwardsMapPOS092215.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		
		
		ArrayList<String> headPOSList = linModelPOS.getTestSibHeadPOS();
	    try
        {
               FileOutputStream fos = new FileOutputStream("headPOSList.ser");
               
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               
               oos.writeObject(headPOSList);
               oos.close();
               fos.close();
               System.out.println("Serialized List data is saved in headPOSList.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		
	}
	
	public void sideTestingHeadDeprel(){
		this.generateSideTestingFileHeadDeprel();
		ArrayList<String> sibTrainHeadDeprel = this.linModelHeadDeprel.getHeadDeprels();
		ArrayList<String> testSibHeadDeprel = this.linModelHeadDeprel.getTestHeadDeprels();
		
		Iterator<String> relIt = testSibHeadDeprel.iterator();
		System.out.println(testSibHeadDeprel.toString());
		while(relIt.hasNext()){
			String headDeprel = relIt.next();
			if(testSibHeadDeprel.contains(headDeprel)&&sibTrainHeadDeprel.contains(headDeprel)){
				System.out.println(headDeprel);
				String[] args = new String[4];
				args[0] = headDeprel+"_test.svm"; //testing file
				args[1] =  headDeprel+"_train.svm.model"; // classifier file
				args[2] = "Sibling POS Files/Output Files/"+headDeprel+"_output"; //output file
				try {
					svm_predict.main(args);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		HashMap<String, String> backwardsMap = linModelHeadDeprel.getBackwardsMapHeadDeprel();
		HashMap<String,String> sideRels = linModelHeadDeprel.getSideFeaturesDictHeadDeprel();
	    try
        {
               FileOutputStream fos = new FileOutputStream("sideFeatureDictHeadDeprel.ser");
               FileOutputStream fos2 = new FileOutputStream("backwardsMapHeadDeprel.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
               oos.writeObject(sideRels);
               oos2.writeObject(backwardsMap);
               oos.close();
               oos2.close();
               fos.close();
               fos2.close();
               System.out.println("Serialized HashMap data is saved in sideFeatureDictHeadDeprel.ser");
               System.out.println("Serialized backwards HashMap data is saved in backwardsMapHeadDeprel.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
		
		
		ArrayList<String> headDeprelList = linModelHeadDeprel.getTestHeadDeprels();
	    try
        {
               FileOutputStream fos = new FileOutputStream("headDeprelsList.ser");
               
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               
               oos.writeObject(headDeprelList);
               oos.close();
               fos.close();
               System.out.println("Serialized List data is saved in headDeprelList.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
	}
	
	/**
	 * Creates a file to train the sibling classifier.
	 * @throws IOException 
	 */
	public void generateSideTrainingFile() throws IOException{
		linModel.clusters("Clusters/1000 Clusters.rtf");
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
						linModel.addSideLine(word, sibling, sentence, train, this.filepath);
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
					linModel.addSideLine(word, sibling, sentence, train, this.filepath);
					
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
	 * @throws IOException 
	 */
	public void sideTraining() throws IOException{
		this.generateSideTrainingFile();
		System.out.println("Done generating training files, starting training");
//		ArrayList<String> sibRelations = this.linModel.getSibRelations();
//		Iterator<String> it = sibRelations.iterator();
//		while(it.hasNext()){
//			String relation = it.next();
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
			 
			args[27]= this.filepath+"siblings.svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
//		}
		HashMap<String,String> sideRels = linModel.getSideFeaturesDict();
		HashMap<String,String> clusterDict = linModel.wordToCluster;
	    try
        {
               FileOutputStream fos = new FileOutputStream(this.filepath+"sideRelMapDec.ser");
//               FileOutputStream fos2 = new FileOutputStream(this.filepath+clusterMapDec.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
//               ObjectOutputStream oos2 = new ObjectOutputStream(fos);
               oos.writeObject(sideRels);
//               oos2.writeObject(clusterDict);
               oos.close();
//               oos2.close();
               fos.close();
//               fos2.close();
               System.out.println("Serialized HashMap data is saved in sideRelMapDec.ser");
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
//		ArrayList<String> sibTrainRelations = this.linModel.getSibRelations();
//		ArrayList<String> testRelations = this.linModel.getTestSibRelations();
//		Iterator<String> relIt = testRelations.iterator();
//		System.out.println(testRelations.toString());
//		while(relIt.hasNext()){
//			String relation = relIt.next();
//			if(sibTrainRelations.contains(relation)){
				System.out.println("Siblings");
				String[] args = new String[4];
				args[0] = this.filepath+"siblings_test.svm";
				args[1] = "siblings.svm.model";
				args[2] = this.filepath+"siblings_output.svm";
				try {
					svm_predict.main(args);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//		}
//	}
	
	/**
	 * Creates files to train the dependency relation classifiers.
	 * @throws IOException 
	 */
	public void generateTrainingFiles() throws IOException{
		linModel.clusters("Clusters/1000 Clusters.rtf");
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
				String relation = sentence.getDeprel(nextNode);
				if(!sentence.getDeprel(nextNode).equals("ROOT")){
					String head = sentence.getHead(nextNode);
						
						linModel.addLine(nextNode, sentence, train, this.filepath);
						
					
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
		ArrayList<String> relations = this.linModel.getRelations();
		HashMap<String,String>numToWord = this.linModel.getBackwardsFeatureDict();
		this.trainingSetDict = numToWord;
//		Singleton single = new Singleton(relations,"Base",numToWord,true);
//		single.countFeatures();
//		single.writeFiles();
//		this.countingDict = single.getCountDict();
//		single.checker();
//		single.fixer();
	}
	
	/**
	 * Creates test files to evaluate the accuracy of the dependency relation classifiers.
	 * @throws IOException 
	 */
	public void generateTestingFiles() throws IOException{
		
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
				String relation = sentence.getDeprel(nextNode);
				if(!sentence.getDeprel(nextNode).equals("ROOT")){
					String head = sentence.getHead(nextNode);
					
						linModel.addLine(nextNode, sentence, train, this.filepath);
					
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
		ArrayList<String> relationss = linModel.getTestRelations();
//		this.testingSetDict = linModel.getBackwardsFeatureDict();
//		Singleton single = new Singleton(relationss,"Base",this.trainingSetDict,this.countingDict,false);
//		single.writeTestFiles(this.testingSetDict);
		
		
	}
	
	/**
	 * This method evaluates the performance of all of the classifiers for which test files were made.
	 * It prints out the accuracy of each classifier in classifying the given test set. It also prints
	 * all the relations for which test files were made
	 * @throws IOException 
	 */
	public void testing() throws IOException{

		this.generateTestingFiles();
		ArrayList<String> trainRelations = this.linModel.getRelations();
		ArrayList<String> relations = this.linModel.getTestRelations();
		Iterator<String> relIt = relations.iterator();
		System.out.println(relations.toString());
		while(relIt.hasNext()){
			String relation = relIt.next();
			if(trainRelations.contains(relation)){
				System.out.println(relation);
				String[] args = new String[4];
				args[0] = this.filepath+"Testing Files/"+relation+".svm";
				args[1] = relation+".svm.model";
				args[2] = this.filepath+"Output Files/"+relation+"_output.svm";
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
	 * @throws IOException 
	 */
	public void training() throws IOException{
		this.generateTrainingFiles();
		
		ArrayList<String> relations = this.linModel.getRelations();
		Iterator<String> it = relations.iterator();
		while(it.hasNext()){
			String relation = it.next();
			System.out.println(relation);
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
			 
//			args[27]= "December Vertical Files/Non Singleton/100 Cluster/Training Files/"+relation+".svm";
			args[27]= this.filepath +"Training Files/"+relation+".svm";
//			args[27]= this.filepath +"vertical.svm";
			
			try {
				svm_train.main(args);
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		HashMap<String,String> rels = linModel.getFeaturesDict();
		ArrayList<String> relationss = this.linModel.getRelations();
		HashMap<String, String> clusterDict = linModel.wordToCluster;
		
	    try
        {
//               FileOutputStream fos = new FileOutputStream("December Vertical Files/Non Singleton/100 Cluster/relmapDec.ser");
//               FileOutputStream fos2 = new FileOutputStream("December Vertical Files/Non Singleton/100 Cluster/relListDec.ser");
               FileOutputStream fos = new FileOutputStream(this.filepath+"relmapDec.ser");
               FileOutputStream fos2 = new FileOutputStream(this.filepath+"relListDec.ser");
               FileOutputStream fos3 = new FileOutputStream(this.filepath+"clusterMapDec.ser");
               ObjectOutputStream oos = new ObjectOutputStream(fos);
               ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
               ObjectOutputStream oos3 = new ObjectOutputStream(fos3);
               oos.writeObject(rels);
               oos2.writeObject(relationss);
               oos3.writeObject(clusterDict);
    
               oos.close();
               oos2.close();
               oos3.close();
               fos.close();
               fos2.close();
               fos3.close();
               System.out.println("Serialized HashMap data is saved in relmapDec.ser");
               System.out.println("Serialized List data is saved in relListDec.ser");
        }catch(IOException ioe)
         {
               ioe.printStackTrace();
         }
	}
}
