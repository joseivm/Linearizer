package testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MergeSort {

	 public static void main(String[] args) throws IOException{
	       int arraySize = 35;
	       ArrayList<String> inputArray = new ArrayList<String>();
	        
	        mergeSort(inputArray);

	   
	    }

	    static void mergeSort(ArrayList<String> sentence) {
	        if (sentence.size() > 1) {
	            int q = sentence.size()/2;

	            ArrayList<String> leftArray = new ArrayList<String>(sentence.subList(0, q));
	            ArrayList<String> rightArray = new ArrayList<String>(sentence.subList(q, sentence.size()));

	            mergeSort(leftArray);
	            mergeSort(rightArray);

	            merge(sentence,leftArray,rightArray);
	        }
	    }

	    static ArrayList<String> merge(ArrayList<String> array, ArrayList<String> leftArray, ArrayList<String> rightArray) {
	        int totElem = leftArray.size() + rightArray.size();
	        int i,li,ri;
	        i = li = ri = 0;
	        while ( i < totElem) {
	            if ((li < leftArray.size()) && (ri<rightArray.size())) {
	                if (leftArray.get(i).contains(rightArray.get(i))) {
	                    array.add(i, leftArray.get(li));
	                    i++;
	                    li++;
	                }
	                else {
	                	array.add(i, rightArray.get(ri));
	                    i++;
	                    ri++;
	                }
	            }
	            else {
	                if (li >= leftArray.size()) {
	                    while (ri < rightArray.size()) {
	                    	array.add(i, rightArray.get(ri));
	                        i++;
	                        ri++;
	                    }
	                }
	                if (ri >= rightArray.size()) {
	                    while (li < leftArray.size()) {
	                    	array.add(i, leftArray.get(li));
	                        li++;
	                        i++;
	                    }
	                }
	            }
	        }
	        return array;

	    }
	
}
