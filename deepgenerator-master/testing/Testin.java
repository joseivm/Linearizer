package testing;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;


public class Testin {

	public static void main(String[] args) throws IOException {
		
		ArrayList<CoNLLHash> CoNLLArray = CoNLLTreeConstructor.storeTreebank("zSentences5-13");
		CoNLLHash sentence = CoNLLArray.get(2);
		
		ArrayList<String> ids = sentence.getIds();
		Iterator<String> it = ids.iterator();
		while(it.hasNext()){
			String id = it.next();
			System.out.println(id);

		}
		
		ArrayList<String> leaves = sentence.getLeaves();
		
		String childID = leaves.get(0);
		
		String headID = sentence.getHead(childID);
		
		String relation = sentence.getDeprel(childID);
		System.out.println(relation);
		
		svm_node[] node = new svm_node[2]; // 6 = number of features a pair of words has.
		
		
		String numberOfChildren = ""+sentence.getChilds(headID).size();
		node[0] = new svm_node();
		node[0].index = 1;
		node[0].value = Double.valueOf(numberOfChildren).doubleValue();
		
		
		String childHeight = ""+sentence.getDepth(childID);
		node[1] = new svm_node();
		node[1].index = 2;
		node[1].value = Double.valueOf(childHeight).doubleValue();
		
		
		svm_model model = svm.svm_load_model("linearization_svm_"+relation+".svm.model");
		
		double prediction = svm.svm_predict(model, node);
		System.out.println(prediction);
}
	
}
