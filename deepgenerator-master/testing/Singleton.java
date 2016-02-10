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

public class Singleton {
	
	HashMap<String,Integer> featureCounterTrain;
	HashMap<String,String>numToFeatTrain;
	HashMap<String,String>numToFeatTest;
	ArrayList<String> classifierList;
	String listFileName;
	ArrayList<String> badClassifiers;
	String location;
	boolean train;
	
	public Singleton(String listFile){
		this.listFileName = listFile;
	}
	
	public void startup(){
		try
	      {
	         FileInputStream classifierListInputStream = new FileInputStream(this.listFileName);
	         ObjectInputStream classifierListObjectStream = new ObjectInputStream(classifierListInputStream);
	         this.classifierList = (ArrayList) classifierListObjectStream.readObject();
	         
	         classifierListObjectStream.close();
	         classifierListInputStream.close();
	         
	         
	      }catch(IOException ioe)
	      {
	         ioe.printStackTrace();
	         return;
	      } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Singleton(ArrayList<String> classifiers, String location, HashMap<String,String> dict, boolean train){
		this.classifierList = classifiers;
		this.location = location;
		this.numToFeatTrain = dict;
		this.train = train;
	}
	
	public Singleton(ArrayList<String> classifiers, String location, HashMap<String,String> dict, HashMap<String,Integer> countDict, boolean train){
		this.classifierList = classifiers;
		this.location = location;
		this.numToFeatTrain = dict;
		this.featureCounterTrain = countDict;
		this.train = train;
	}
	
	public HashMap<String,Integer> getCountDict(){
		return this.featureCounterTrain;
	}
	
//	CHANGE HERE
	public void countFeatures() throws IOException{
		this.featureCounterTrain = new HashMap<String,Integer>();
		Iterator<String> classIt = this.classifierList.iterator();
		while(classIt.hasNext()){
			String classifier = classIt.next();
			String oldFilee = "December Vertical Files/Singleton/"+this.location+"/Bad Training Files/"+classifier+".svm";
			File ofi = new File(oldFilee);
				
			if(ofi.exists()){	
				
				BufferedReader oldFileReader = new BufferedReader(new FileReader(oldFilee));
				while(oldFileReader.ready()) {
						String features = oldFileReader.readLine();
						String[] featureList = features.split(" ");
						
						for(int i=1;i<featureList.length;i++){
							String feature = featureList[i];
							count(feature);
						}
				}
			}
		}
	}

	public void writeFiles() throws IOException{
		String unknownFeat = "3:1";
		String secondLocation;
		if(this.train){
			secondLocation = "Training";
		}else{
			secondLocation = "Testing";
		}
		Iterator<String> classIt = this.classifierList.iterator();
		while(classIt.hasNext()){
			String classifier = classIt.next();
			String oldFile = "December Vertical Files/Singleton/"+this.location+"/Bad " +secondLocation+" Files/"+classifier+".svm";
			BufferedWriter  newFile = new BufferedWriter(new FileWriter("December Vertical Files/Singleton/"+this.location+"/"+secondLocation+" Files/"+classifier+".svm"));
			
			File oldFilee = new File(oldFile);
			if(oldFilee.exists()){
				BufferedReader oldFileReader = new BufferedReader(new FileReader(oldFile));
				while(oldFileReader.ready()){
					String line = "";
					String features = oldFileReader.readLine();
					String[] featureList = features.split(" ");
					line += featureList[0];
					for(int i=1;i<featureList.length;i++){
						String feature = featureList[i];
						if(this.featureCounterTrain.get(feature)<2){
							if(this.numToFeatTrain.get(feature).contains("Lemma")){
								feature = unknownFeat;
							}
						}
						 line+= " "+feature;
					}
					newFile.write(line+"\n");
					
				}

			}
			newFile.close();
		}
	}

	public void checker() throws IOException{
		this.badClassifiers = new ArrayList<String>();
		Iterator<String> it = this.classifierList.iterator();
		while(it.hasNext()){
			String classifier = it.next();
			File newFile = new File("December Vertical Files/Singleton/"+this.location+"/Training Files/"+classifier+".svm");
			BufferedReader br = new BufferedReader(new FileReader("December Vertical Files/Singleton/"+this.location+"/Training Files/"+classifier+".svm"));
			if(newFile.exists()){
				if(!br.ready()){
					this.badClassifiers.add(classifier);
//					System.out.println(classifier+"bad");
//					System.out.println(classifier);
//					String oldFile = "December Vertical Files/Base/Training Files/"+classifier+".svm";
//					BufferedReader br2 = new BufferedReader(new FileReader("December Vertical Files/Base/Training Files/"+classifier+".svm"));
//					BufferedWriter bw = new BufferedWriter(new FileWriter("December Vertical Files/Base/New Training Files/"+classifier+".svm"));
//					while(br2.ready()){
//						String line = "";
//						String features = br2.readLine();
//						System.out.println(features);
//						line += features+"\n";
//						bw.write(line);
//					}
				}
			}
		}
	}
	
	public void fixer() throws IOException{
		Iterator<String> it = this.badClassifiers.iterator();
		while(it.hasNext()){
			String classifier = it.next();
//			System.out.println(classifier+"fixing");
			BufferedReader br2 = new BufferedReader(new FileReader("December Vertical Files/Singleton/"+this.location+"/Bad Training Files/"+classifier+".svm"));
			BufferedWriter bw = new BufferedWriter(new FileWriter("December Vertical Files/Singleton/"+this.location+"/Training Files/"+classifier+".svm"));
			while(br2.ready()){
				String line = "";
				String features = br2.readLine();
				line += features;
				bw.write(line+"\n");
			}
			bw.close();
		}
	}

	public void count(String feature){
		Integer count = this.featureCounterTrain.get(feature); // Checks if the feature is in the featureTranslation list.
		if (count==null) {
			this.featureCounterTrain.put(feature,new Integer(1));
		} else{
			this.featureCounterTrain.put(feature, count+1);
		}
		
	}

	public void writeTestFiles(HashMap<String,String>numToFeatTest) throws IOException{
		String unknownFeat = "3:1";
		String secondLocation;
		HashMap<String,String> numToFeatTes = numToFeatTest;
		if(this.train){
			secondLocation = "Training";
		}else{
			secondLocation = "Testing";
		}
		Iterator<String> classIt = this.classifierList.iterator();
		while(classIt.hasNext()){
			String classifier = classIt.next();
			String oldFile = "December Vertical Files/Singleton/"+this.location+"/Bad " +secondLocation+" Files/"+classifier+".svm";
			BufferedWriter  newFile = new BufferedWriter(new FileWriter("December Vertical Files/Singleton/"+this.location+"/"+secondLocation+" Files/"+classifier+".svm"));
			
			File oldFilee = new File(oldFile);
			if(oldFilee.exists()){
				BufferedReader oldFileReader = new BufferedReader(new FileReader(oldFile));
				while(oldFileReader.ready()){
					String line = "";
					String features = oldFileReader.readLine();
					String[] featureList = features.split(" ");
					line += featureList[0];
					for(int i=1;i<featureList.length;i++){
						String feature = featureList[i];
						if(this.numToFeatTrain.get(feature).contains("Lemma")){
							if(this.featureCounterTrain.get(feature)==null || this.featureCounterTrain.get(feature)<2){
								feature = unknownFeat;
							}
						}
						 line+= " "+feature;
					}
					newFile.write(line+"\n");
					
				}

			}
			newFile.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		Singleton single = new Singleton("December Vertical Files/Base/relListDec.ser");
		single.startup();
		single.countFeatures();
		single.writeFiles();
		single.checker();
		single.fixer();
		
	}

}
