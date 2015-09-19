/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import static org.apache.lucene.util.Version.LUCENE_44;

/**
 *
 * @author Ulysses_D
 */
class LexiconIndexer {
    private Directory list, dir;
    private Analyzer analyzer;
    private IndexWriterConfig cfg;
    private IndexWriter writer, listWriter;

    public IndexReader getReader() {
        return reader;
    }

    public IndexReader getListReader() {
        return listReader;
    }
    private IndexReader reader, listReader;
    
    public LexiconIndexer() throws IOException {
        File f = new File("list");
        System.out.println(f.exists());
        if(!f.exists()) list = new SimpleFSDirectory(f);
        else list = FSDirectory.open(f);
    
    }
    
    public void createIndex(String index) throws IOException
    {
        File f = new File("index/" + index);
        System.out.println(f.exists());
        if(!f.exists()) list = new SimpleFSDirectory(f);
        
    }
    
    public void openListReader() throws IOException
    {   
        listReader = DirectoryReader.open(list);
    }
    
    public void closeListReader() throws IOException
    {
        listReader.close();
    }
    
    public void openReader(String index) throws IOException
    {
        File f = new File(index);
        dir = FSDirectory.open(f);
        reader = DirectoryReader.open(dir);
    }
    
    public void closeReader() throws IOException
    {
        reader.close();
    }
    
    public void addIndex(String index) throws IOException
    {
        Document doc = new Document();
        doc.add(new StringField("index", index, Field.Store.YES));
        
        listWriter.addDocument(doc);
        listWriter.commit();
    }
    

    /*public void addDocument(long id, String term) throws IOException
>>>>>>> origin/master
    {
        Document doc = new Document();
        doc.add(new StringField("term", term, Field.Store.YES));
        doc.add(new LongField("ID", id, Field.Store.YES));
        doc.add(new TextField("tweet", text, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        
<<<<<<< HEAD
        //System.out.println("Added " + term + " to index");
=======
        System.out.println("Added " + term + " to index");
    }*/
    
     public void addTweet(long id, String tweet) throws IOException
    {
        Document doc = new Document();
        doc.add(new TextField("tweet", tweet, Field.Store.YES));
        doc.add(new LongField("ID", id, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        
        System.out.println("Added " + tweet + " to index");
    }
     
    public void printTweets() throws IOException{
        /*Document doc = null;
        for(int i = 0; i < reader.maxDoc(); i++){
            doc = reader.document(i);
            System.out.println(doc.getField("tweet"));
            //System.out.println(doc.getField("tweet").tokenStream(analyzer));
        }*/
        Terms terms = MultiFields.getTerms(reader,"tweet");
        TermsEnum termsEnum = terms.iterator(null);
        while (termsEnum.next()!=null){
            String word = termsEnum.term().utf8ToString();
            System.out.println(word + ", df: " + termsEnum.docFreq());
            
            IndexSearcher searcher = new IndexSearcher(reader);
        
            Query q = new TermQuery(new Term("tweet", word));
        
            
            TopDocs top = searcher.search(q, reader.numDocs());
            ScoreDoc[] hits = top.scoreDocs;
            Document res = null;
            System.out.println("Hits Size: " + hits.length);
            for(ScoreDoc entry: hits){
                res = searcher.doc(entry.doc);
            
                System.out.println(res.get("ID") + ": " + res.get("tweet"));
        }
        }
        
        Map m = constructTermMap(getTw());
    }
    
    public void saveTi(float t) throws IOException
    {
        Document doc = new Document();
        doc.add(new StringField("term", "_Ti", Field.Store.YES));
        doc.add(new FloatField("ID", t, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
    }
    
    public void saveTw(float t) throws IOException
    {
        Document doc = new Document();
        doc.add(new StringField("term", "_Tw", Field.Store.YES));
        doc.add(new FloatField("ID", t, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
    }
    
    
    public void openListWriter() throws IOException
    {
        analyzer = new SimpleAnalyzer(LUCENE_44);
        cfg = new IndexWriterConfig(LUCENE_44, analyzer);
        listWriter = new IndexWriter(list,cfg);
    }
    
    public void closeListWriter() throws IOException
    {
        listWriter.close();
    }
    
    public void openWriter(String index) throws IOException
    {
        File f = new File(index);
        dir = FSDirectory.open(f);
        
        analyzer = new SimpleAnalyzer(LUCENE_44);
        System.out.println(ItalianAnalyzer.getDefaultStopSet());
        cfg = new IndexWriterConfig(LUCENE_44, analyzer);
        writer = new IndexWriter(dir,cfg);
    }
    
    public void closeWriter() throws IOException
    {
        writer.close();
    }
    
    public void getDocumentFrequency(String s) throws IOException
    {
        System.out.println("docFreq for term " + s + ": " +  reader.docFreq(new Term("term", s)));
    }
    
    public float getTi() throws IOException
    {
        IndexSearcher searcher = new IndexSearcher(reader);
        
        Query q = new TermQuery(new Term("term", "_Ti"));
        
        TopDocs top = searcher.search(q, 1);
        ScoreDoc[] hits = top.scoreDocs;
        Document doc = searcher.doc(hits[0].doc);
            
        System.out.println(doc.get("ID"));
        
        return Float.parseFloat(doc.get("ID"));
        
    }
    
    public float getTw() throws IOException
    {
        IndexSearcher searcher = new IndexSearcher(reader);
        
        Query q = new TermQuery(new Term("term", "_Tw"));
        
        TopDocs top = searcher.search(q, 1);
        ScoreDoc[] hits = top.scoreDocs;
        Document doc = searcher.doc(hits[0].doc);
            
        System.out.println(doc.get("ID"));
        
        return Float.parseFloat(doc.get("ID"));
        
    }
    

    void calculateTermFreq() throws IOException{
        
       //for(int i = 0; i < reader.maxDoc(); i++){
            IndexSearcher searcher = new IndexSearcher(reader);
            Query q = new TermQuery(new Term("term","e"));
        
            TopDocs top = searcher.search(q,reader.numDocs());
            ScoreDoc[] hits = top.scoreDocs;
            System.out.println(hits.length);
            /*
            int docid = hits[0].doc;    
            System.out.println(docid);
            Terms termVector = reader.getTermVector(docid, "tweet");
            System.out.println(termVector.size());
            */
            
            Document doc = null;
        
            System.out.println("Results for query: " + q.toString());
            for(ScoreDoc entry: hits){
                doc = searcher.doc(entry.doc);
            
                System.out.println("doc: " + entry.doc + ", ID: " + doc.get("ID") + " -> " + doc.get("tweet"));
            }
            
            /*if(termVector!=null)
                System.out.println(termVector.toString());
            else
                System.out.println("fuck");*/
            //TermsEnum itr = termVector.iterator(null);
            //BytesRef term = null;



            /*
            while ((term = itr.next()) != null) {              
                String termText = term.utf8ToString();
                Term termInstance = new Term("term", term);                              
                long termFreq = reader.totalTermFreq(termInstance);
                long docCount = reader.docFreq(termInstance);

                System.out.println("term: "+termText+", termFreq = "+termFreq+", docCount = "+docCount);
            }
            */
       //}

    }
    
    
    /*public int getDocNum(int docNo) throws IOException{
        IndexSearcher searcher = new IndexSearcher(reader);
        
        Query q = new TermQuery(new Term("term"));
        
        TopDocs top = searcher.search(q, 1);
        ScoreDoc[] hits = top.scoreDocs;
        return hits[docNo].doc;
    }*/
    
    public Map<String, HashSet<String>> constructTermMap(float t_w) throws IOException
    {
        Map<String, HashSet<String>> map = new HashMap();
        Terms terms = MultiFields.getTerms(reader,"tweet");
        TermsEnum termsEnum = terms.iterator(null);
        
        while (termsEnum.next()!=null){
            
            String word = termsEnum.term().utf8ToString();
            
            /*if(word.equals("null"))
            {
                System.out.println("Fanculo");
                continue;
            }*/
            
            float treshold = (float) ((Math.pow(termsEnum.docFreq(), 2) * Math.pow(10, 4)) / (Math.pow(t_w, 2)));
            
            //System.out.println(word + ", treshold" + treshold);
            
            if(treshold >=1 ){
                System.out.println("Removing " + word + " from analysis;");
                continue;
            }
            
            //System.out.println("Adding " + word + " to map;");
            
            IndexSearcher searcher = new IndexSearcher(reader);
            Query q = new TermQuery(new Term("tweet", word));
            TopDocs top = searcher.search(q, reader.numDocs());
            ScoreDoc[] hits = top.scoreDocs;
            Document res = null;
            
            for(ScoreDoc entry: hits){
                res = searcher.doc(entry.doc);
                
                if(map.containsKey(word)) map.get(word).add(res.get("ID"));
                else
                {
                    map.put(word, new HashSet());
                    map.get(word).add(res.get("ID"));
                }
            }
        }
        
        return map;
    }
    
    public HashSet constructUniverse() throws IOException
    {
        HashSet u = new HashSet();
        Terms terms = MultiFields.getTerms(reader,"ID");
        TermsEnum termsEnum = terms.iterator(null);
        
        Document doc = null;
        
        for(int i = 0; i < reader.maxDoc(); i++)
        {
            if(!(reader.document(i).get("term") == null) && (reader.document(i).get("term").equals("_Ti") || reader.document(i).get("term").equals("_Tw"))) continue;
            if(!u.contains(reader.document(i).get("ID"))) u.add(reader.document(i).get("ID"));
        }
        return u;
    }
    
    public TreeMap constructSortedMap(Map<String, HashSet<String>> m)
    {
        Map<String, HashSet<String>> temp = new HashMap();
        ValueComparator comp = new ValueComparator(temp);
        TreeMap<String,HashSet<String>> sorted = new TreeMap(comp);
        
        for(Entry<String, HashSet<String>> e: m.entrySet())
            temp.put(e.getKey(), e.getValue());
        
        sorted.putAll(m);
        
        return sorted;
    }
    
    void printDocs() throws IOException{
        Document d = null;
        for (int i = 0; i < reader.maxDoc(); i++)
        {
            d = reader.document(i);
            System.out.println(d.get("ID") + ", " + d.get("tweet") + ", " + d.get("term"));
        }
    }
    
    void calculateSetCover(String index, float tw) throws IOException
    {
        //printDocs();
        //float tot_count = getTw();
        
        HashSet min_cover = new HashSet(), covered = new HashSet(), universe = constructUniverse();
        
        Map<String, HashSet<String>> terms = constructTermMap(tw);
        TreeMap<String,HashSet<String>> sorted = constructSortedMap(terms);
        HashSet riprova = (HashSet) universe.clone();
        
        //System.out.println(sorted.toString());
        System.out.println("Universe: " + universe.size() + ", Docs: " + reader.numDocs());
        
        Entry<String, HashSet<String>> e = sorted.firstEntry();
        sorted.remove(e.getKey());
        min_cover.add(e.getKey());
        covered.addAll(e.getValue());
        universe.removeAll(e.getValue());
        
        
        
        while(!universe.isEmpty())
        {
            String best = null; 
            int intersection = 0;
            
            for(String key: sorted.navigableKeySet())
            {
                HashSet inter = (HashSet) universe.clone();
                inter.retainAll(terms.get(key));
                if(inter.size() > intersection)
                {
                    best = key;
                    intersection = inter.size();
                }
            }
            System.out.println("Adding "+ best + " to minimum set cover");
            //Abbiamo trovato elemento successivo
            //System.out.println("Universe: " + universe.size() + ", Best: " + sorted.size());
            if(best == null)
            {
                System.out.println("STOCAZZO. Fuori: " + universe.toString());
                break;
            }
            sorted.remove(best);
            min_cover.add(best);
            covered.addAll(terms.get(best));
            universe.removeAll(terms.get(best));
            
            intersection = 0;
        }
        
        System.out.println(min_cover);
        
        Iterator it = min_cover.iterator();
        
        
        System.out.println("Terms: " + terms.size() + ", Cover: " + min_cover.size());
        while(it.hasNext()) riprova.removeAll(terms.get(it.next()));
        System.out.println("Universe - U2 = " + riprova.size());
        
        HashSet overlapping = new HashSet();
        String[] mc = (String[]) min_cover.toArray(new String[min_cover.size()]);
        
        for(int i = 0; i < mc.length || overlapping.size() == mc.length; i++)
            for(int j = i + 1; j < mc.length || overlapping.size() == mc.length; j++)
            {
                HashSet<String> set = terms.get(mc[i]);
                set.retainAll(terms.get(mc[j]));
                if(!set.isEmpty())
                {
                    overlapping.add(mc[i]);
                    overlapping.add(mc[j]);
                            
                }
            }
        
        System.out.println("Quality: " + overlapping.size() + "/" + mc.length);
        
        PrintWriter writer = new PrintWriter(new File(index + ".txt"), "UTF-8");
        Arrays.sort(mc);
        for (int i = 0; i < mc.length; i++)
            writer.write(mc[i] + ";");
        writer.close();
    }
    
}

class ValueComparator implements Comparator<String> {

    Map<String, HashSet<String>> base;
    public ValueComparator(Map<String,HashSet<String>> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a).size() >= base.get(b).size()) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }

}
