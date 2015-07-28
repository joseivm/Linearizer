/**
 * 
 */
package deep_surf_svm_models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import deep_to_surf.CoNLLHash;


/**
 * * @author Miguel Ballesteros
 * Universitat Pompeu Fabra. 
 *
 */
public class ModelBubbleDetection {
	
	int next = 0;
	int nextBubble = 0;
	
	private HashMap <String, String> featureTranslation = new HashMap <String,String>();
	private HashMap <String, String> bubbleTranslation = new HashMap <String,String>();
	
	private HashMap <String,BufferedWriter> trainWriters;
	private HashMap <String,BufferedWriter> testWriters;

	// Creator method. Each member of the class has two dictionaries which map strings to 
	// buffered writers. Takes in an array of strings and maps each member of the array to a 
	// buffered writer in the train set and in the test set.
	public ModelBubbleDetection(ArrayList<String> listPosDeep) {
		trainWriters = new HashMap<String,BufferedWriter>();
		testWriters = new HashMap<String,BufferedWriter>();
		try {
			Iterator<String> it=listPosDeep.iterator();
			
			while(it.hasNext()){
				String s = it.next();
				BufferedWriter bw = new BufferedWriter(new FileWriter("bubble_svm_"+s+".svm"));
				trainWriters.put(s,bw);
				BufferedWriter bw2 = new BufferedWriter(new FileWriter("bubble_svm_test_"+s+".svm"));
				testWriters.put(s,bw2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// This takes a POS tag, removes it from the test dictionary, and puts it in again
	// with a different buffered writer.
	public void generateTestingFile (String posDeep) {
		BufferedWriter bw2;
		try {
			bw2 = new BufferedWriter(new FileWriter("bubble_svm_test_"+posDeep+".svm"));
			testWriters.remove(posDeep);
			testWriters.put(posDeep,bw2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// returns whatever bubble is mapped to itself in bubbleTranslation. Returns the name of the bubble that corresponds to that SVM file (maybe)
	public String getSVMBubble(String svmBubble) {
		//it is the other way around.. we should find the bubble that corresponds to the svm label that maps bubbles (svmBubble). Hashmap process backwards.
		Set <String> bubbles = bubbleTranslation.keySet();
		Iterator <String> dpIt = bubbles.iterator();
		while (dpIt.hasNext()) {
			String bubble = dpIt.next();
			String svmAux = bubbleTranslation.get(bubble);
			if (svmAux.equals(svmBubble)) return bubble; 
		}
		return null;
	}
	
	// Iterates through the bubbleTranslation keys and returns a bubble that 
	// is equal to what it maps to.
	public String getSVMBubbleSingleFile(String file) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String svmBubble="";
			while(br.ready()){
				svmBubble=br.readLine();
			}
			// above code makes svmBubble a String that is identical to the file that is read.
			br.close();
			Double bubb=Double.parseDouble(svmBubble); // double representation of svmBubble
			Integer bubbleInt=bubb.intValue(); 
			Set<String> bubbles=bubbleTranslation.keySet();
			Iterator<String> dpIt=bubbles.iterator();
			while (dpIt.hasNext()) {
				String bubble=dpIt.next();
				String svmAux=bubbleTranslation.get(bubble);
				if (svmAux.equals(bubbleInt.toString())) return bubble; 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	// Closes the buffers in either trainWriters or testWriters depending on the input
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
	
	// Maps the new feature to 1:1 if its the first feature, 2:1 if its the second feature
    // 3:1 if its the third feature and so on. The features are mapped to that ratio in the 
	// feature translation dictionary. Increases next by one.
	private void addNewFeature (String feature){
		
		String svmFeat = featureTranslation.get(feature);
		if (svmFeat == null) {
			next++;
			featureTranslation.put(feature,""+next+":1");
		}
		
	}
	
	// Maps the new bubble to a string containing the input, or the new bubble, and the number the 
	// bubble corresponds to. Thus, if it is the first bubble and is named x, it will map to  "1", 
	// if it is the second bubble and is named y, it will map to "2" and so on. Increases nextBubble by 1.
	private void addNewBubble (String bubble){
		
		String svmFeat = bubbleTranslation.get(bubble);
		if (svmFeat == null) {
			nextBubble++;
			bubbleTranslation.put(bubble,""+nextBubble);
			//System.out.println(deprelTranslation);
		}
		
	}
	
	/**
	 * 
	 * 
	 * @param form
	 * @param lemma
	 * @param pos
	 * @param feats
	 * @param deprel
	 * @param bubble
	 * @param surfaceSentence 
	 * @param surfaceId 
	 * @param hypernode
	 * @param train
	 */
	// Creates a line using deepSentence and deepID, it appends the numbers that the many of the features of deepSentence map to 
	// and then writes this line to one of the writers in either test or train writers.
	public void addLine(String bubble, String deepId, CoNLLHash deepSentence, boolean train, String deepPOS) {
		
		//System.out.println(bubble);
		String line = "";
			boolean write = false;
			//if (true) {
			if (train) {
				addNewBubble(bubble);
				line+=bubbleTranslation.get(bubble);
			}
			else {
				line+="1";
			}
			StringTokenizer st = new StringTokenizer(deepSentence.getFEAT(deepId));
			
			
			while(st.hasMoreTokens()) {
				String s = st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("pos+s="+s+deepSentence.getPOS(deepId));
					line += " "+featureTranslation.get("pos+s="+s+deepSentence.getPOS(deepId));
				}
			}
			
	
			if (deepPOS.equals("V")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("sent_type")) {
						this.addNewFeature("st="+s);
						line+=" "+featureTranslation.get("st="+s);
					}
				}
			}
			
			
			
			//aux_rel
			
			if (deepPOS.equals("V")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("aux_rel")) {
						this.addNewFeature("auxrel="+s);
						line+=" "+featureTranslation.get("auxrel="+s);
					}
				}
			}
			
			
			//definiteness
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("definiteness")) {
					this.addNewFeature("def="+s);
					line+=" "+featureTranslation.get("def="+s);
				}
			}
			
			//finiteness
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("finiteness") && !s.contains("definiteness")) {
					this.addNewFeature("fin="+s);
					line+=" "+featureTranslation.get("fin="+s);
				}
			}
			
			
			//DEPREL
			if (!deepPOS.equals("Adv")){
				this.addNewFeature("dep="+deepSentence.getDeprel(deepId));
				line+=" "+featureTranslation.get("dep="+deepSentence.getDeprel(deepId));
			}
			
			//LEMMA
			if (!deepPOS.equals("V") && !deepPOS.equals("N")){
				this.addNewFeature("lemma="+deepSentence.getForm(deepId));
				line+=" "+featureTranslation.get("lemma="+deepSentence.getForm(deepId));
			}
			
			ArrayList<String> childs=deepSentence.getChilds(deepId);
			Iterator<String> itCh=childs.iterator();
			while(itCh.hasNext()) {
				String ch=itCh.next();
				if (!ch.equals(deepId)) {
					String sibChild=deepSentence.getDeprel(ch);
					
					String sibLemma=deepSentence.getForm(ch);						
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(ch));
					
					//SPOS of child
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						
					}

				}
			}
			
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("lemmaH=root");
					line+=" "+featureTranslation.get("lemmaH=root");
				}
				else {
						//SIBLINGS
						ArrayList<String> siblings=deepSentence.getSiblings(headId);
						Iterator<String> itSib=siblings.iterator();
						while(itSib.hasNext()) {
							String sib=itSib.next();
							if (!sib.equals(deepId)) {
								String sibDeprel=deepSentence.getDeprel(sib);
								
								String sibLemma=deepSentence.getForm(sib);						

								StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(sib));
								
								//SPOS of sib
								while(st2.hasMoreTokens()) {
									String s=st2.nextToken("|");
									if (deepPOS.equals("N")){
										if (s.contains("spos")) {
											this.addNewFeature("spossib="+s);
											line+=" "+featureTranslation.get("spossib="+s);
										}
									}
									
									
								}

							}
						}
					
					//LEMMA of head
					if (deepPOS.equals("V")){
						this.addNewFeature("lemmaH="+deepSentence.getForm(headId));
						line+=" "+featureTranslation.get("lemmaH="+deepSentence.getForm(headId));
					}
					
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(headId));
					
					//SPOS of head
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						if (s.contains("spos")) {
							this.addNewFeature("sposH="+s);
							line+=" "+featureTranslation.get("sposH="+s);
						}
					}
					
					//tense
					st=new StringTokenizer(deepSentence.getFEAT(headId));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("tense")) {
							this.addNewFeature("tenseh="+s);
							line+=" "+featureTranslation.get("tenseh="+s);
						}
					}
					
					//definiteness
					st=new StringTokenizer(deepSentence.getFEAT(headId));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("definiteness")) {
							this.addNewFeature("definh="+s);
							line+=" "+featureTranslation.get("definh="+s);
						}
					}
					
					//voice
					st=new StringTokenizer(deepSentence.getFEAT(headId));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("voice")) {
							this.addNewFeature("voiceh="+s);
							line+=" "+featureTranslation.get("voiceh="+s);
						}
					}
				}
			}
			
			try {
				
				if (train){	
					BufferedWriter bw=trainWriters.get(deepPOS);
					bw.write(line+"\n");
				}
				else {
					BufferedWriter bw2=testWriters.get(deepPOS);
					bw2.write(line+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
					
	}
	
public void addLineForDebugging(String bubble, String deepId, CoNLLHash deepSentence, boolean train, String deepPOS) {
		
		//System.out.println(bubble);
		String line="";
			boolean write=false;
			if (true) {
			//if (train) {
				addNewBubble(bubble);
				line+=bubbleTranslation.get(bubble);
			}
			else {
				line+="1";
			}
			StringTokenizer st=new StringTokenizer(deepSentence.getFEAT(deepId));
			
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("spos")) {
					this.addNewFeature("pos+s="+s+deepSentence.getPOS(deepId));
					line+=" "+featureTranslation.get("pos+s="+s+deepSentence.getPOS(deepId));
				}
			}
			
			
			if (deepPOS.equals("V")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("sent_type")) {
						this.addNewFeature("st="+s);
						line+=" "+featureTranslation.get("st="+s);
					}
				}
			}
			
			
			
			//aux_rel
			
			if (deepPOS.equals("V")){
				st=new StringTokenizer(deepSentence.getFEAT(deepId));
				while(st.hasMoreTokens()) {
					String s=st.nextToken("|");
					if (s.contains("aux_rel")) {
						this.addNewFeature("auxrel="+s);
						line+=" "+featureTranslation.get("auxrel="+s);
					}
				}
			}
			
			
			//definiteness
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("definiteness")) {
					this.addNewFeature("def="+s);
					line+=" "+featureTranslation.get("def="+s);
				}
			}
			
			//finiteness
			st=new StringTokenizer(deepSentence.getFEAT(deepId));
			while(st.hasMoreTokens()) {
				String s=st.nextToken("|");
				if (s.contains("finiteness") && !s.contains("definiteness")) {
					this.addNewFeature("fin="+s);
					line+=" "+featureTranslation.get("fin="+s);
				}
			}
			
			
			//DEPREL
			if (!deepPOS.equals("Adv")){
				this.addNewFeature("dep="+deepSentence.getDeprel(deepId));
				line+=" "+featureTranslation.get("dep="+deepSentence.getDeprel(deepId));
			}
			
			//LEMMA
			if (!deepPOS.equals("V") && !deepPOS.equals("N")){
				this.addNewFeature("lemma="+deepSentence.getForm(deepId));
				line+=" "+featureTranslation.get("lemma="+deepSentence.getForm(deepId));
			}
			
			ArrayList<String> childs=deepSentence.getChilds(deepId);
			Iterator<String> itCh=childs.iterator();
			while(itCh.hasNext()) {
				String ch=itCh.next();
				if (!ch.equals(deepId)) {
					String sibChild=deepSentence.getDeprel(ch);
				
					String sibLemma=deepSentence.getForm(ch);						

					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(ch));
					
					//SPOS of child
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");

						
					}

				}
			}
			
			
			
			String headId=deepSentence.getHead(deepId);
			if (headId!=null){
				if (headId.equals("0")){
					this.addNewFeature("lemmaH=root");
					line+=" "+featureTranslation.get("lemmaH=root");
				}
				else {
						//SIBLINGS
						ArrayList<String> siblings=deepSentence.getSiblings(headId);
						Iterator<String> itSib=siblings.iterator();
						while(itSib.hasNext()) {
							String sib=itSib.next();
							if (!sib.equals(deepId)) {
								String sibDeprel=deepSentence.getDeprel(sib);
								
								String sibLemma=deepSentence.getForm(sib);						

								StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(sib));
								
								//SPOS of sib
								while(st2.hasMoreTokens()) {
									String s=st2.nextToken("|");
									if (deepPOS.equals("N")){
										if (s.contains("spos")) {
											this.addNewFeature("spossib="+s);
											line+=" "+featureTranslation.get("spossib="+s);
										}
									}
							
									
								}

							}
						}
					
					//LEMMA of head
					if (deepPOS.equals("V")){
						this.addNewFeature("lemmaH="+deepSentence.getForm(headId));
						line+=" "+featureTranslation.get("lemmaH="+deepSentence.getForm(headId));
					}
					
					StringTokenizer st2=new StringTokenizer(deepSentence.getFEAT(headId));
					
					//SPOS of head
					while(st2.hasMoreTokens()) {
						String s=st2.nextToken("|");
						if (s.contains("spos")) {
							this.addNewFeature("sposH="+s);
							line+=" "+featureTranslation.get("sposH="+s);
						}
					}
					
					//tense
					st=new StringTokenizer(deepSentence.getFEAT(headId));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("tense")) {
							this.addNewFeature("tenseh="+s);
							line+=" "+featureTranslation.get("tenseh="+s);
						}
					}
					
					//definiteness
					st=new StringTokenizer(deepSentence.getFEAT(headId));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("definiteness")) {
							this.addNewFeature("definh="+s);
							line+=" "+featureTranslation.get("definh="+s);
						}
					}
					
					//voice
					st=new StringTokenizer(deepSentence.getFEAT(headId));
					while(st.hasMoreTokens()) {
						String s=st.nextToken("|");
						if (s.contains("voice")) {
							this.addNewFeature("voiceh="+s);
							line+=" "+featureTranslation.get("voiceh="+s);
						}
					}
				}
			}
			
			try {
				
				if (train){	
					BufferedWriter bw=trainWriters.get(deepPOS);
					bw.write(line+"\n");
				}
				else {
					BufferedWriter bw2=testWriters.get(deepPOS);
					bw2.write(line+"\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
					
	}

				
		
	
	private ArrayList<String> getSiblings(String head, CoNLLHash surfaceSentence) {
		// TODO Auto-generated method stub
		return null;
	}

	public String toString() {
		return this.featureTranslation.toString() + bubbleTranslation.toString();
	
	}

}
