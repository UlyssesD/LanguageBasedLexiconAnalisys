/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
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
import static org.apache.lucene.util.Version.LUCENE_41;

/**
 *
 * @author Ulysses_D
 */
class LexiconIndexer {
    private Directory list, dir;
    private Analyzer analyzer;
    private IndexWriterConfig cfg;
    private IndexWriter writer, listWriter;
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
    
    public void addDocument(long id, String term) throws IOException
    {
        Document doc = new Document();
        doc.add(new StringField("term", term, Field.Store.YES));
        doc.add(new LongField("ID", id, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
        
        System.out.println("Added " + term + " to index");
    }
    
    public void saveT(long t) throws IOException
    {
        Document doc = new Document();
        doc.add(new StringField("term", "_T", Field.Store.YES));
        doc.add(new LongField("ID", t, Field.Store.YES));
        writer.addDocument(doc);
        writer.commit();
    }
    
    public void openListWriter() throws IOException
    {
        analyzer = new StandardAnalyzer(LUCENE_41);
        cfg = new IndexWriterConfig(LUCENE_41, analyzer);
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
        
        analyzer = new StandardAnalyzer(LUCENE_41);
        cfg = new IndexWriterConfig(LUCENE_41, analyzer);
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
    
    public int getT() throws IOException
    {
        IndexSearcher searcher = new IndexSearcher(reader);
        
        Query q = new TermQuery(new Term("term", "_T"));
        
        TopDocs top = searcher.search(q, 1);
        ScoreDoc[] hits = top.scoreDocs;
        Document doc = searcher.doc(hits[0].doc);
            
        System.out.println(doc.get("ID"));
        
        return Integer.parseInt(doc.get("ID"));
        
    }
    
    void calculateSetCover() throws IOException
    {
        HashSet min_cover = new HashSet(), covered = new HashSet(), universe = new HashSet();
        Map<String, HashSet<String>> terms = new HashMap();
        ValueComparator comp = new ValueComparator(terms);
        TreeMap<String,HashSet<String>> sorted = new TreeMap(comp);
        
        int count = getT();
        Document doc = null;
        
        for(int i = 0; i < reader.maxDoc(); i++){
            doc = reader.document(i);
            String t = doc.get("term"), id = doc.get("ID");
            int f = reader.docFreq(new Term("term", t));
            
            if(t.equals("_T")) continue;
            if(f >= count)
            {
                System.out.println("Removing " + t + " from cover");
                continue;
            }
            if(terms.containsKey(t)) terms.get(t).add(id);
            else
            {
                terms.put(t, new HashSet());
                terms.get(t).add(id);
            }
            
            //System.out.println("t: " + t + ", f: " + f + ", id: " + id);
            //System.out.println("Term: " + doc.get("term") + ", docFreq: " + reader.docFreq(new Term("term", doc.get("term"))));
            if(!universe.contains(id)) universe.add(id);
        }
        sorted.putAll(terms);
        
        System.out.println(sorted.toString());
        
        Entry<String, HashSet<String>> e = sorted.firstEntry();
        sorted.remove(e.getKey());
        min_cover.add(e.getKey());
        covered.addAll(e.getValue());
        universe.removeAll(e.getValue());
        
        System.out.println(universe.size());
        
        HashSet riprova = (HashSet) universe.clone();
        
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
    }
    
}

class ValueComparator implements Comparator<String> {

    Map<String, HashSet<String>> base;
    public ValueComparator(Map<String,HashSet<String>> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a).size() <= base.get(b).size()) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }

}