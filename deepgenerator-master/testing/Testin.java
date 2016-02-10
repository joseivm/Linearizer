package testing;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

//import deep_surf_svm_models.FileReader;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;


public class Testin {

	public static void main(String[] args) throws IOException {
//		ArrayList<CoNLLHash> CoNLLArray = CoNLLTreeConstructor.storeTreebank("Sentences/CoNLL Sentences");
		BufferedReader br = new BufferedReader(new FileReader("Clusters/100 Clusters.rtf"));
		while(br.ready()){
			String line = br.readLine();
			System.out.println(line);
			String news = line.replaceAll("\\s+", "_");
			String[] parts = news.split("_");
			if(parts.length>1){
				String name = parts[1];
				String number = parts[0];
				System.out.println(number);
//				Pattern.matches("[a-zA-Z]+", number);
//				System.out.println(number.contains("\\D"));
				if(number.matches("[0-9]+")){
					int nnumber = Integer.parseInt(number, 2);
					System.out.println(parts[0]+ " "+ nnumber);
				}
				
			}

//			System.out.println(news+" "+parts[0]);
		}
//		CoNLLHash sentence = CoNLLArray.get(0);
//		
//		ArrayList<String> ids = sentence.getIds();
//		Iterator<String> it = ids.iterator();
//		while(it.hasNext()){
//			String id = it.next();
//			System.out.println(id);
//
//		}
//		
//		ArrayList<String> leaves = sentence.getLeaves();
//		
//		String childID = leaves.get(0);
//		
//		String headID = sentence.getHead(childID);
//		
//		String relation = sentence.getDeprel(childID);
//		System.out.println(relation);
//		
//		svm_node[] node = new svm_node[2]; // 6 = number of features a pair of words has.
//		
//		
//		String numberOfChildren = ""+sentence.getChilds(headID).size();
//		node[0] = new svm_node();
//		node[0].index = 1;
//		node[0].value = Double.valueOf(numberOfChildren).doubleValue();
//		
//		
//		String childHeight = ""+sentence.getDepth(childID);
//		node[1] = new svm_node();
//		node[1].index = 2;
//		node[1].value = Double.valueOf(childHeight).doubleValue();
//		
//		
//		svm_model model = svm.svm_load_model("linearization_svm_"+relation+".svm.model");
//		
//		double prediction = svm.svm_predict(model, node);
//		System.out.println(prediction);
}
	
}
