/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 *
 * @author Ulysses_D
 */
public class LexiconProcessor {
    private LexiconIndexer indexer;
    
    public LexiconProcessor() throws IOException {
        indexer = new LexiconIndexer();
        indexer.openListReader();
    }
    
    public void calculateSetCover() throws IOException, ParseException, org.json.simple.parser.ParseException
    {
        JSONParser parser = new JSONParser();
        JSONObject data =  (JSONObject) parser.parse(new FileReader("data.json"));
        
        JSONArray array = (JSONArray) data.get("data");
        for (int i = 0; i < array.size(); i++)
        {
            JSONObject o = (JSONObject) array.get(i);
            System.out.println(o);
            indexer.openReader((String) o.get("index"));
            //indexer.calculateSetCover((String) o.get("index"), Float.parseFloat(o.get("Tw").toString()));
            indexer.printDocs();
            indexer.closeReader();
        }
        
        
        /*for(int i = 0; i < indexer.getListReader().maxDoc(); i++)
        {
            String idx = indexer.getListReader().document(i).get("index");
            indexer.openReader(idx);
            System.out.println("Calculating Set Cover for hour " + idx + ":00");
            indexer.calculateSetCover(idx);
            indexer.closeReader();
        }*/
        

        //indexer.openReader("13");
        //indexer.calculateSetCover("16");
    }
    
    public void computeSimilarity() throws IOException, org.json.simple.parser.ParseException
    {
        JSONParser parser = new JSONParser();
        JSONObject data =  (JSONObject) parser.parse(new FileReader("data.json"));
        
        JSONArray array = (JSONArray) data.get("data");
        for (int i = 0; i < (array.size() - 1); i++)
        {
            String[] t1 = retrieveTermVector((String) ((JSONObject) array.get(i)).get("index"));
            String[] t2 = retrieveTermVector((String) ((JSONObject) array.get(i + 1)).get("index"));
            
            System.out.println((String) ((JSONObject) array.get(i)).get("index") + ": " + Arrays.toString(t1));
            System.out.println((String) ((JSONObject) array.get(i + 1)).get("index") + ": " + Arrays.toString(t2));
            
            Set u = new HashSet();
            
            u.addAll(Arrays.asList(t1));
            u.addAll(Arrays.asList(t2));
            
            String[] temp = new String[u.size()];
            u.toArray(temp);
            Arrays.sort(temp);
            
            System.out.println(Arrays.toString(temp));
            System.out.println("");
            
            int[] v1 = new int[temp.length], v2 = new int[temp.length];
            
            ArrayList l = new ArrayList();
            
            for(int el = 0; el < temp.length; el++)
            {
                v1[el] = (Arrays.binarySearch(t1, temp[el]) >= 0 ? 1 : 0);
                v2[el] = (Arrays.binarySearch(t2, temp[el]) >= 0 ? 1 : 0);
            }
            
            System.out.println("v1: " + Arrays.toString(v1));
            System.out.println("v2: " + Arrays.toString(v2));
            
            float dot = dot(v1, v2);
            float cosine = (float) (dot / (Math.sqrt(v1.length) * Math.sqrt(v2.length))); // giustificato dal fatto che per ogni x in v1 o v2, v[i] = {0,1};
            
            System.out.println("cosine(v1,v): " + cosine);
            System.out.println("");
            
        }
    }
    
    public void computeTimeSeries() throws FileNotFoundException, IOException, org.json.simple.parser.ParseException
    {
        Map<String, HashSet<String>> terms = new HashMap();
        String[] days;
        JSONParser parser = new JSONParser();
        JSONObject data =  (JSONObject) parser.parse(new FileReader("data.json"));
        
        JSONArray array = (JSONArray) data.get("data");
        days = new String[array.size()];
        
        for (int i = 0; i < array.size(); i++)
        {
            JSONObject o = (JSONObject) array.get(i);
            String[] v = retrieveTermVector((String) o.get("index"));
            
            days[i] = (String) o.get("index");
            
            for (String t : v) {
                if (!terms.containsKey(t))
                {
                    terms.put(t, new HashSet());
                    terms.get(t).add((String) o.get("index"));
                }
                else
                {
                    terms.get(t).add((String) o.get("index"));
                }
            }
        }
        
        TreeMap<String, HashSet<String>> sorted = constructSortedMap(terms);
        //Arrays.sort(days);
        System.out.println(sorted.toString());
        writeTimeSeries(sorted, days);
    }
    
    public void writeTimeSeries(TreeMap<String, HashSet<String>> terms, String[] u) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        PrintWriter writer = new PrintWriter(new File("time_series.csv"), "UTF-8");
        
        writer.write("term;");
        for(String d: u)
        {
            writer.write(d + ";");
        }
        writer.write("\n");
       
        
        for(int i = 0; i < 100 || terms.isEmpty(); i++)
        {
            Entry<String, HashSet<String>> e = terms.pollFirstEntry();
            writer.write(e.getKey() + ";");
            for(String d: u)
            {
                if(e.getValue().contains(d))
                {
                    indexer.openReader((String) d);
                    writer.write(indexer.getReader().docFreq(new Term("tweet", e.getKey())) + ";");
                    indexer.closeReader();
                }
                else writer.write("0;");
            }
            writer.write("\n");
        } 
        
        writer.close();
    }
    
    public TreeMap constructSortedMap(Map<String, HashSet<String>> m)
    {
        Map<String, HashSet<String>> temp = new HashMap();
        ValueComparator comp = new ValueComparator(temp);
        TreeMap<String,HashSet<String>> sorted = new TreeMap(comp);
        
        for(Map.Entry<String, HashSet<String>> e: m.entrySet())
            temp.put(e.getKey(), e.getValue());
        
        sorted.putAll(m);
        
        return sorted;
    }
    
    public String[] retrieveTermVector(String s) throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader (new FileReader(s + ".txt"));
        String [] res = br.readLine().split(";");
        br.close();
        return res;
    }
    
    public float dot(int[] v1, int[] v2)
    {
        float res = 0;
        
        for(int i = 0; i < v1.length; i++)
            res+= v1[i] * v2[i];
        
        return res;
    }
    
    public void printTweets() throws IOException, ParseException
    {
        //indexer.openReader("16");
        //indexer.calculateTermFreq();
        //indexer.calculateSetCover("16");

        indexer.openReader("13");
        indexer.printTweets();

    }
    
   
}