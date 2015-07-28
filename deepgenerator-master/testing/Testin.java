package testing;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import deep_to_surf.CoNLLHash;
import deep_to_surf.CoNLLTreeConstructor;


public class Testin {

	public static void main(String[] args) {
		
		ArrayList<CoNLLHash> CoNLLArray = CoNLLTreeConstructor.storeTreebank("zSentences5-13");
		CoNLLHash hash = CoNLLArray.get(2);
		
		ArrayList<String> ids = hash.getIds();
		Iterator<String> it = ids.iterator();
		while(it.hasNext()){
			String id = it.next();
			System.out.println(id);
		// Column Heads
//		System.out.printf("%-22s%-22s%-22s%-22s%-22s%-22s%-22s%-22s\n","Head: Child", "HeadPOS", "ChildPOS", "HeadHead","HeadLemma","ChildLemma", "# of Head Deps","Child Height");
//		System.out.println(" ");
//		
//		// This iterates through all the words in a sentence and pairs every word with its head. It then prints out features of both the child and the head.
//		for(int i = 1;i < hash.sentenceLength();i++){
//				
//				String childID = i+"";
//				String child = hash.getForm(childID);
//				String childPOS = hash.getPOS(childID);
//				String childLemma = hash.getLemma(childID);
//				int childDescendants = hash.getDepth(childID);
//				
//				String headID = hash.getHead(childID);
//				String headPOS;
//				String headOfHead;
//				String headLemma;
//				String head;
//				int numOfHeadDeps = hash.getChilds(headID).size();
//				
//				// Special cases
//				if (headID.equals("0")){
//					head = "None";
//					headPOS = "None";
//					headOfHead = "None";
//					headLemma = "None";
//					
//				}
//				else {
//					head = hash.getForm(headID);
//					headPOS = hash.getPOS(headID);
//					headOfHead = hash.getHead(headID);
//					headLemma = hash.getLemma(headID);
//				}
//				
//				if (headOfHead.equals("0") || headOfHead.equals("None")) {
//					headOfHead = "None";
//				}
//			
//				else headOfHead = hash.getForm(headOfHead);
//				
//				
//				System.out.printf("%-22s%-22s%-22s%-22s%-22s%-22s%-22s%-22s\n",head+": "+child, headPOS, childPOS, headOfHead,headLemma,childLemma, numOfHeadDeps+"",childDescendants+"");
//				
//				
			
		}
		Iterator<String> oit = ids.iterator();
		
		while(oit.hasNext()){
			String id = oit.next();
			System.out.println(id +"2");
		}
		
}
	
}
