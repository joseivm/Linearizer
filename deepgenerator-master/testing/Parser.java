package testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import deep_to_surf.CoNLLHash;

public class Parser {

	String dictionaryFile;
	String testFile;
	String outputFile;
	String listFile;
	ArrayList<String> classifierList;
	HashMap<String, String> featureMap;
	HashMap<String, BufferedWriter> fileWriters;
	
	
	public Parser(String dictionaryFileName, String listName){
		this.dictionaryFile = dictionaryFileName;
//		this.testFile = testFileName;
//		this.outputFile = outputFileName;
		this.listFile = listName;
		this.fileWriters = new HashMap<String, BufferedWriter>();
		
	}
	
	public void startup(){
		try
	      {
	         FileInputStream featureMapInputStream = new FileInputStream(dictionaryFile);
	         FileInputStream classifierListInputStream = new FileInputStream(listFile);
	         
	         ObjectInputStream featMapObjectStream = new ObjectInputStream(featureMapInputStream);
	         ObjectInputStream classifierListObjectStream = new ObjectInputStream(classifierListInputStream);
	         
	         this.featureMap = (HashMap) featMapObjectStream.readObject();
	         this.classifierList = (ArrayList) classifierListObjectStream.readObject();
	         
	         featMapObjectStream.close();
	         featureMapInputStream.close();
	         
	         
	         
	      }catch(IOException ioe)
	      {
	         ioe.printStackTrace();
	         return;
	      } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Iterator<String> it = classifierList.iterator();
//		while(it.hasNext()){
//			String classifier = (String) it.next();
//			BufferedWriter bw;
//			try {
//				bw = new BufferedWriter(new FileWriter(classifier+"_info"));
//				fileWriters.put(classifier,bw);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
	}
	
	public void worker() throws IOException{
		BufferedWriter mainWriter = new BufferedWriter(new FileWriter("Misclassification Info POS+Deprel")); 
		Iterator<String> classIt = this.classifierList.iterator();
		while(classIt.hasNext()){
			String classifier = classIt.next();
			mainWriter.write("\n"+"Sibling Rels = "+classifier+"\n");
			
			String testFilee = "Sibling Dep Files/Testing Files/"+classifier+"_test.svm";
			String outputFilee = "Sibling Dep Files/Output Files/"+classifier + "_output";
			
				File tfi = new File(testFilee);
				File ofi = new File(outputFilee);
				
			if(tfi.exists() && ofi.exists()){	
				
				BufferedReader testFileReader = new BufferedReader(new FileReader(testFilee));
				BufferedReader outputFileReader = new BufferedReader(new FileReader(outputFilee));
				
				
					int i = 0;
					
					while((testFileReader.ready() && outputFileReader.ready())&&i<10) {
						String line = "";
						String features = testFileReader.readLine();
						String[] featureList = features.split(" ");
						
						String guess = outputFileReader.readLine();
						Double numberGuess = new Double(guess);

						
						String correctAnswer = featureList[0];
						Double numberAnswer = new Double(correctAnswer);
						
	                    if (!numberGuess.equals(numberAnswer)) {
	                    	
	                    	for(int j=1;j<featureList.length;j++){
	                    		String feature = featureList[j];
//	                    		String[] splitFeat = feature.split(":");
//	                    		String featureNum = splitFeat[0];
	                    		
//	                    		Integer featureNumm = new Integer(featureNum);
	                    		String featureDescription="";
//	                    		if(featureNumm<4){
//	                    			if(featureNumm.equals(new Integer(1))){
//	                    				
//	                    				featureDescription = "number of parent children = "+splitFeat[1];
//	                    				
//	                    			} else if( featureNumm.equals(new Integer(2))){
//	                    				featureDescription = "first node height = "+splitFeat[1]; 
//	                    			} else if( featureNumm.equals(new Integer(3))){
//	                    				featureDescription = "sibling height = "+splitFeat[1];
//	                    			}
//	                    			line+= " "+featureDescription+';';
//	                    		} else{
	                    		featureDescription = this.featureMap.get(feature);
	                    		line+= " "+featureDescription+';';
//	                    		}
	                    	}
	                    	
	                    	
	                    	mainWriter.write(line+"\n");
	                    	mainWriter.write("\n");
	                        i++;
	                    }
	                    

	                }

				
				
			}
		
				}
		mainWriter.close();
		
		
	}

	public void closeBuffers(){
		Iterator<String> it = fileWriters.keySet().iterator();
		while(it.hasNext()){
			BufferedWriter bw= fileWriters.get(it.next());
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Parser parser = new Parser("Sibling Dep Files/backwardsMapDep092215.ser","Sibling Dep Files/sibRelationPairsDepList.ser");
		parser.startup();
		parser.worker();
		
		

	}

}
