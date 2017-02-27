import java.io.*;
import java.util.*;

public class eclat 
{
    static List<int[]> output;  // final frequent itemsets
    static List<int[]> itemsets;  // intermediate k-itemsets
    static String input_file; 
    static int num_of_items; 
    static int num_of_transactions; 
    static double minSup;   // relative
    static Map<List<Integer>,List<Integer>> verticaldb;

    //  to print itemsets
    static void print(List<int[]> itemsets)
    {
        for(int i=0;i<itemsets.size();i++)
            System.out.print(Arrays.toString(itemsets.get(i))+" ");
        System.out.println();
    }

    private void printmap()
    {
        for (Map.Entry<List<Integer>, List<Integer>> entry : verticaldb.entrySet()) 
        {
            List<Integer> key = entry.getKey();
            List<Integer> value = entry.getValue();
            System.out.println("key, " + key + " value " + value);
        }
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
        
        initializeVerticalDB(input_file);
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

    static void initializeVerticalDB(String input_file) throws Exception
    {
        verticaldb = new HashMap<List<Integer>,List<Integer>>();
        BufferedReader data_in = new BufferedReader(new FileReader(input_file));

        for(int i=0;i<num_of_items;i++)
        {
            List<Integer> tra_no = new ArrayList<Integer>();
            List<Integer> item_no = new ArrayList<Integer>();
            item_no.add(i);
            verticaldb.put(item_no,tra_no);                        
        }      

        while (data_in.ready()) 
        {           
            String line=data_in.readLine();
            if (line.matches("\\s*")) 
                continue;               // be friendly with empty lines
            String[] sarray = line.split(" |,");
            
            //System.out.println();
            //printmap();
            for(int i=1;i<sarray.length;i++)
            {

                if(sarray[i].equals("1"))
                {
                    List<Integer> item_no = new ArrayList<Integer>();
                    item_no.add(i-1);
                    
                    if(!verticaldb.containsKey(item_no))
                    {
                        List<Integer> tra_no = new ArrayList<Integer>();
                        tra_no.add(Integer.parseInt(sarray[0]));
                        verticaldb.put(item_no,tra_no);
                    }
                    else
                    {
                        List<Integer> tra_no = verticaldb.get(item_no);
                        tra_no.add(Integer.parseInt(sarray[0]));
                        verticaldb.put(item_no,tra_no);    
                    }
                }
            }
        }
    }
            
    static List<Integer> intersection(List<Integer> a, List<Integer> b)
    {
        List<Integer> toreturn = new ArrayList<Integer>();
        for(int i: a)
        {
            if(b.contains(i))
                toreturn.add(i);
        }
        return toreturn;
    }

    static boolean issubset(List<Integer> a, List<Integer> b)  // a is subset of b
    {
        for(int i : a)
        {
            if(b.contains(i))
                continue;
            else
                return false;
        }
        return true;
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

        //  updating vertical database

        Map<List<Integer>,List<Integer>> tempverticaldb = new HashMap<List<Integer>,List<Integer>>();
        
        //  adding itemsets in temporary vertical database
        for(int k=0;k<itemsets.size();k++)
        {
            List<Integer> to_add_vdb = new ArrayList<Integer>();
            for(int j=0;j<itemsets.get(k).length;j++)
                to_add_vdb.add(itemsets.get(k)[j]);

            tempverticaldb.put(to_add_vdb,new ArrayList<Integer>());
        }

        List<List<Integer>> tointersect;
        List<Integer> tra_no;

        for (Map.Entry<List<Integer>, List<Integer>> entry : tempverticaldb.entrySet()) 
        {
            tointersect = new ArrayList<List<Integer>>();
            tra_no = new ArrayList<Integer>();
            List<Integer> key = entry.getKey();
            for(Map.Entry<List<Integer>, List<Integer>> preventry : verticaldb.entrySet())
            {
                List<Integer> prevkey = preventry.getKey();
                if(issubset(prevkey,key))
                {
                    List<Integer> prevvalue = preventry.getValue();
                    tointersect.add(prevvalue);
                }
            }
            tra_no = tointersect.get(0);               
            for(int a=1;a<tointersect.size();a++)
            {
                tra_no = intersection(tointersect.get(a),tra_no);
            }
            tempverticaldb.put(key,tra_no);
        }

        verticaldb = tempverticaldb;        
    }

    static void removeInfrequentItemSets() throws Exception
    {
        System.out.println("Candidate "+itemsets.get(0).length+"-itemset  -->  "+itemsets.size());

        List<int[]> frequentCandidates = new ArrayList<int[]>(); 

        for (int i=0; i<itemsets.size(); i++) 
        {
            List<Integer> k_itemset = new ArrayList<Integer>();
            
            for(int k=0;k<itemsets.get(i).length;k++)
                k_itemset.add(itemsets.get(i)[k]);

            if(verticaldb.containsKey(k_itemset))
            {
                List<Integer> num_of_transactions_for_k_itemset = verticaldb.get(k_itemset);
                if ((num_of_transactions_for_k_itemset.size() / (double) (num_of_transactions)) >= minSup) 
                {
                    frequentCandidates.add(itemsets.get(i));
                }
                else
                    verticaldb.remove(k_itemset);
            }
        }
        itemsets = frequentCandidates;
        output.addAll(itemsets);
    }
}