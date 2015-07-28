package testing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;

public class Testin2 {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		ArrayList<CoNLLHash> CoNLLArray = CoNLLTreeConstructor.storeTreebank("Sentences5-13");
		CoNLLHash sentence = CoNLLArray.get(3);
		
		
		
		ArrayList<String> ids = sentence.getIds();
		Iterator<String> it = ids.iterator();
		while(it.hasNext()){
			String id = it.next();
			String form = sentence.getForm(id);
			System.out.println(id+": "+form);
		}
		
		BufferedWriter dataPoint = new BufferedWriter(new FileWriter("testFile1"));
		
		ArrayList<String> leaves = sentence.getLeaves();
		
		String firstLeaf = leaves.get(1);
		String parent = sentence.getHead(firstLeaf);
		String relation = sentence.getDeprel(firstLeaf);
		
		String childForm = sentence.getForm(firstLeaf);
		String parentForm = sentence.getForm(parent);
		
		System.out.println("child is "+childForm);
		System.out.println("parent is "+parentForm);
		System.out.println("relation is "+relation);
		
		String line = "?";
		
		String numberOfChildren = ""+sentence.getChilds(parent).size();
		line+= " 1:"+numberOfChildren;
		
		String childHeight = ""+sentence.getDepth(firstLeaf);
		line+= " 2:"+childHeight;
		
		dataPoint.write(line+"\n");
		dataPoint.close();
		
		svm_node[] x = new svm_node[2];
		x[0] = new svm_node();
		x[0].index = 1;
		x[0].value = Double.valueOf(numberOfChildren).doubleValue();
		
		x[1] = new svm_node();
		x[1].index = 2;
		x[1].value = Double.valueOf(childHeight).doubleValue();
		
		svm_model model = svm.svm_load_model("linearization_svm_"+relation+".svm.model");
		
		double v = svm.svm_predict(model,x);
		
		System.out.println("v equals "+""+v);
		
//		sentence.getVerticalOrder();
//		
//		System.out.println("after getVerticalOrder");
//		sentence.printBeforeMap();
//
//		sentence.makeTransitive();
//		
//		
//		System.out.println(" ");
//		
//		System.out.println("after make Transitive");
//		sentence.printBeforeMap();
//		sentence.getHorizontalOrder();
//		
//		System.out.println(" ");
//		System.out.println("after horizontal order");
//		sentence.printBeforeMap();
//		ArrayList<String>result = sentence.linearize();
//		
//		System.out.println(" ");
//		System.out.println("final order");
//		System.out.println(result.toString());
//		
		
		
		
		
		
		}
		
		
	}


