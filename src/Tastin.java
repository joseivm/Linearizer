import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;


public class Tastin {

	public static void main(String[] args) {
		/*
		 * 
		 * HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
		StringTokenizer st = new StringTokenizer("Hello	my	name	is	pepe	nice	to	meet	you");
        ArrayList<String> columns = new ArrayList<String>();
        if (st.hasMoreTokens()) {
            String id = st.nextToken("\t");
            columns.add(id);
            hash.put(id, columns);
            System.out.println("Columns: " + columns.toString());
            System.out.println("Hash: " + hash.toString());
            while (st.hasMoreTokens()) {
                columns.add(st.nextToken("\t"));
                System.out.println("Columns (in while loop): " + columns.toString());
            }
        }
        System.out.println("Hash: " + hash.toString());

		 */
		
		
//		HashMap<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
//		for (int i = 0; i < 6; i++){
//        	ArrayList<String> testList = new ArrayList<String>();
//        	Integer num = new Integer(i);
//        	hash.put(num.toString(), testList );
//        }
//		ArrayList<String> testList = new ArrayList<String>();
//		Integer num = new Integer(50);
//		hash.put(num.toString(), testList);
//		Set<String> keys = hash.keySet();
//        ArrayList<String> keysInt = new ArrayList<String>();
//        Iterator<String> it = keys.iterator();
//        Integer max = 0;
//        
//        System.out.println(hash.toString());
//        
//        while (it.hasNext()) {
//            //keysInt.add(it.next());
//        	String s = it.next();
//        	Integer sI = Integer.parseInt(s);
//        	if (sI > max) max = sI;
//        }
//        
//        for(int i=1;i<=max;i++){
//        	keysInt.add(""+i);
//        	
//        }
		String id0 = "";
		StringTokenizer st = new StringTokenizer("H=ello my name is Jose");
		while(st.hasMoreTokens()) {
			String tok = st.nextToken();
			if (tok.startsWith("H"+"=")) {
				String[] split = tok.split("=");
				id0 = split[1];
			}
		}
		
		System.out.println(id0);
   
	}

}
