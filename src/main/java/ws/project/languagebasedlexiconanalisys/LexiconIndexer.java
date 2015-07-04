/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
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
    private Directory dir;
    private Analyzer analyzer;
    private IndexWriterConfig cfg;
    private IndexWriter writer;
    private IndexReader reader;
    private IndexSearcher searcher;
    
    public LexiconIndexer() throws IOException {
        File f = new File("index");
        System.out.println(f.exists());
        if(!f.exists()) dir = new SimpleFSDirectory(f);
        else dir = FSDirectory.open(f);
        
        //analyzer = new StandardAnalyzer(LUCENE_41);
        //cfg = new IndexWriterConfig(LUCENE_41, analyzer);
        //writer = new IndexWriter(dir,cfg);
        
        reader = DirectoryReader.open(dir);
        searcher = new IndexSearcher(reader);
        
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
    
    public void close() throws IOException
    {
        writer.close();
    }
    
    public void getDocumentFrequency(String s) throws IOException
    {
        System.out.println("docFreq for term " + s + ": " +  reader.docFreq(new Term("term", s)));
    }

    void calculateSetCover() throws IOException, ParseException
    {
        HashSet min_cover = new HashSet(), covered = new HashSet(), best = new HashSet();
        TreeMap terms = new TreeMap();
        
        Document doc = null;
        
        for(int i = 0; i < reader.maxDoc(); i++){
            doc = reader.document(i);
            String t = doc.get("term"), id = doc.get("ID");
            int f = reader.docFreq(new Term("term", t));
            
            System.out.println("t: " + t + ", f: " + f + ", id: " + id);
            
            if(terms.containsKey(t)) ((Lola) terms.get(t)).put(id);
            else terms.put(t, new Lola(f, id));
            
            //System.out.println("Term: " + doc.get("term") + ", docFreq: " + reader.docFreq(new Term("term", doc.get("term"))));
        
        }
        
        System.out.println(terms.toString());
        /*
        Document doc = null;
        
        for(ScoreDoc entry: hits){
            doc = searcher.doc(entry.doc);
            
            System.out.println("Term: " + doc.get("term") + ", docFreq: " + reader.docFreq(new Term("term", doc.get("term"))));
        
        }
        */
    }

    private class Lola {
        private int freq;
        private HashSet<String> ids;
        
        public Lola(int f, String s) {
           this.freq = f;
           this.ids = new HashSet<String>();
           this.put(s);
        }
        
        public void put(String s)
        {
            ids.add(s);
        }

        @Override
        public String toString() {
            return "Lola{" + "term=" + freq + ", ids=" + ids.toString() + '}';
        }
        
    }
}
