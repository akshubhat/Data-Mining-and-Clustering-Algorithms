import java.io.*;
import java.util.*;

public class Apriori 
{
	static List<int[]> output;  // final frequent itemsets
    static List<int[]> itemsets;  // intermediate k-itemsets
    static String input_file; 
    static int num_of_items; 
    static int num_of_transactions; 
    static double minSup;   // relative

    //  to print itemsets
    static void print(List<int[]> itemsets)
    {
        for(int i=0;i<itemsets.size();i++)
            System.out.print(Arrays.toString(itemsets.get(i))+" ");
        System.out.println();
    }

    public static void main(String[] args) throws Exception 
    {
    	Scanner sc = new Scanner(System.in);
    	output = new ArrayList<int[]>();

    	try
    	{
    		System.out.println("Enter name of data file --");
	    	input_file = sc.nextLine();

	        System.out.println("Enter minimum support(relative) --");
	        minSup = Double.valueOf(sc.nextLine());
		
	        if (minSup>1 || minSup<0) 
	        {
	            System.out.println("ERROR : minimum support should be in between 0 and 1");
	            System.exit(0);
	        }    
	    	
	    	num_of_items = 0;
	    	num_of_transactions=0;
	    	BufferedReader br = new BufferedReader(new FileReader(input_file));
	    
	        String first_line = "";
	        boolean f_line = false;
	    	while (br.ready()) 
	        {    		
	    		String line=br.readLine();
	            if(!f_line)
	            {
	                f_line = true;
	                first_line = line;
	            }
	    		if (line.matches("\\s*")) 
	                continue;              
	    		num_of_transactions++;
	    	}
	        String sarray[] = first_line.split(" |,");
	        num_of_items = sarray.length-1;  
    	}
	    catch(Exception e)
	    {
	    	System.out.println("ERROR : Unable to find file");
	    	System.exit(0);
	    }
    	
		System.out.println("number of items : "+num_of_items);
		System.out.println("number of transactions : "+num_of_transactions);
		System.out.println("minimum support : "+minSup*100+"%  "+"(absolute "+Math.round(num_of_transactions*minSup)+" transactions)");
        
        execute();        
    }

    static void execute() throws Exception 
    {
    	//  generating candidate 1-itemset
		itemsets = new ArrayList<int[]>();
        int i = 0;
        while(i<num_of_items)
        {
        	int[] temp_candidates = {i};
        	itemsets.add(temp_candidates);
        	i++;
        }

        int itemsetNumber=1; 
        int total_frequent_sets=0;
        
        while (itemsets.size()>0)
        {
            removeInfrequentItemSets();

            if(itemsets.size() == 0)
            	break;
            else
            {
                total_frequent_sets = total_frequent_sets + itemsets.size();
                System.out.println("Frequent  "+itemsetNumber+"-itemset  -->  "+itemsets.size());
                //print(itemsets);                
                generaeCandidatesFromPrevious();
            }
            itemsetNumber++;
        } 

    	System.out.println("total number of frequent itemsets : "+total_frequent_sets);
    	//print(output);
    }
			
    static void generaeCandidatesFromPrevious()
    {
    	int curr_itemset_size = itemsets.get(0).length;
    		
    	HashMap<String, int[]> tempCandidates = new HashMap<String, int[]>(); 
    	
        for(int i=0; i<itemsets.size(); i++)
        {
            for(int j=i+1; j<itemsets.size(); j++)
            {
                int[] left = itemsets.get(i);
                int[] right = itemsets.get(j);

                int [] new_candidate = new int[curr_itemset_size+1];
                
                for(int l=0; l<new_candidate.length-1; l++) 
                   	new_candidate[l] = left[l];
                    
                int diff = 0;
                for(int r=0; r<right.length; r++)
                {
                	boolean flag = false;
                	for(int l=0; l<left.length; l++) 
                	{
                    	if (left[l]==right[r]) 
                    	{ 
                    		flag = true;
                    		break;
                    	}
                	}
                	if (!flag)
                	{
                		diff++;
                		new_candidate[new_candidate.length -1] = right[r];
                	}
                	if(diff>1)
                		break;
            	}
                
                if (diff==1) 
                {
                	Arrays.sort(new_candidate);
                	tempCandidates.put(Arrays.toString(new_candidate),new_candidate);
                }
            }
        }
        
        itemsets = new ArrayList<int[]>(tempCandidates.values());
    }

    static void removeInfrequentItemSets() throws Exception
    {
        System.out.println("Candidate "+itemsets.get(0).length+"-itemset  -->  "+itemsets.size());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input_file)));
        
        List<int[]> frequentCandidates = new ArrayList<int[]>();
		int count[] = new int[itemsets.size()];
		boolean[] presence = new boolean[num_of_items];  //  stores presence of particular item for each transaction
        boolean flag;  //  is true if all items of given itemset is present in transaction
		
		int i=0;
		while(i<num_of_transactions)
		{
			String line = br.readLine();
		    Arrays.fill(presence, false);

	        String sarray[] = line.split(" |,");
	        for(int j=1;j<sarray.length;j++)
	        {
	            int val = Integer.parseInt(sarray[j]);
				if(val == 1)
	                presence[j-1] = true;
		    }

			int c=0;
			while(c<itemsets.size())
            {
				flag = true; 
				int[] cand = itemsets.get(c);
				for (int xx : cand) 
                {
					if (!presence[xx]) 
                    {
						flag = false;
						break;
					}
				}
				if(flag) 
                	count[c]++;
				c++;
			}
			i++;
		}
		br.close();
	
		for (i = 0; i < itemsets.size(); i++) 
        {
			if ((count[i] / (double) (num_of_transactions)) >= minSup) 
            {
				frequentCandidates.add(itemsets.get(i));
			}
		}
        itemsets = frequentCandidates;
        output.addAll(itemsets);
    }
}