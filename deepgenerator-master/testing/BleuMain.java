package testing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BleuMain {

	public static void main(String[] args) {

	    /**
	     * @param args the command line arguments
	     */
		
		


	        BleuMeasurer bm;
	        BufferedReader inRef = null, inCand = null;
	        boolean eof = false;
	        int lineCtr = 0;

	        // parameters check
	        if (args.length != 2){
	            System.err.println("Input parameters: reference_file candidate_file");
	            System.exit(1);
	        }

	        // initialization, opening the files
	        bm = new BleuMeasurer();

	        try {
	            inRef = new BufferedReader(new FileReader(args[0]));
	            inCand = new BufferedReader(new FileReader(args[1]));
	        }
	        catch(FileNotFoundException e){
	            System.err.println(e.getMessage());
	            System.exit(2);
	        }

	        // read sentence by sentence
	        while (!eof){

	            String candLine = null, refLine = null;
	            String [] candTokens;
	            String [] refTokens;

	            try {
	                refLine = inRef.readLine();
	                candLine = inCand.readLine();
	            }
	            catch (IOException ex) {
	                System.err.println(ex.getMessage());
	                System.exit(1);
	            }

	            // test for EOF
	            if (candLine == null && refLine == null){
	                break;
	            }
	            if (candLine == null || refLine == null){
	                System.err.println("The files are of different lengths.");
	                System.exit(1);
	            }

	            // split to tokens by whitespace
	            candLine.trim(); refLine.trim();
	            candTokens = candLine.split("\\s+");
	            refTokens = refLine.split("\\s+");

	            // add sentence to stats
	            bm.addSentence(refTokens, candTokens);
	            if (lineCtr % 100 == 0){
	                System.err.print(".");
	            }
	            lineCtr++;
	        }

	        // print the result
	        System.err.println("Total:" + lineCtr + " sentences.");
	        System.out.println("BLEU score: " + bm.bleu());
	    }


	}


