/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.PrintWriter;

/**
 *
 * @author Ulysses_D
 */
class TwitterStreamAnalizer {
    
    private LexiconIndexer indexer;
    private long id = 0;
    private int start = 25;
    private float tot_count = 0.0f;
    private float it_count = 0.0f;
    
    public TwitterStreamAnalizer() throws IOException {
        indexer = new LexiconIndexer();
        indexer.openListWriter();
    }  

     public static List<String> tokenizeString(Analyzer analyzer, String string) {
    List<String> result = new ArrayList<String>();
    try {
      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
      stream.reset();
      while (stream.incrementToken()) {
        result.add(stream.getAttribute(CharTermAttribute.class).toString());
      }
    } catch (IOException e) {
      // not thrown b/c we're using a string reader...
      throw new RuntimeException(e);
    }
    return result;
  }
    
    void parseStream() throws IOException {
        
        try (BufferedReader br = new BufferedReader(new FileReader("tweets/22giu.txt")))
	{
            String currentLine;
            
            while ((currentLine = br.readLine()) != null)
            {
                
                if(!currentLine.matches("\\d+\\:\\d+\\:\\d+.*")) continue;
		String[] parts = currentLine.split(" \\: ", 2);
                if(parts.length == 1) continue;
                String[] infos = parts[0].split(", ");
                
                //System.out.println(infos.length);
                if(infos.length != 1)
                {
                    //System.out.println(infos[0]);
                    String[] time = infos[0].split(":");
                    if(start == 25)
                    {
                        start = Integer.parseInt(time[0]);
                        indexer.addIndex(time[0]);
                        System.out.println("Start: " + start);
                        indexer.openWriter(time[0]);
                    }
                    /*
                    if(Integer.parseInt(time[0]) > start)
                    {
                        System.out.println("Hour " + start + ", Total Tweets: " + tot_count + ", Tweets in Italian: " + it_count);
                        start = 25;
                        indexer.saveTi(it_count);
                        indexer.saveTw(tot_count);
                        tot_count = it_count = 0;
                        indexer.closeWriter();
                    }*/
                    tot_count++;
                    if(!"it".equals(infos[1])) continue;
                    it_count++;
                    indexer.addTweet(id,parts[1]);
                    /*
                    //Il tweet è in "Itagliano"
                    //System.out.println(parts[1]);
                    String[] words = parts[1].split(" ");
                    for (String w: words)
                    {
                        //Rimuoviamo parole tipo "@qualcosa", "#qualcosa", "http(s)://", caratteri speciali (è?), "RT";
                        if(w.matches("\\W*@.*")) continue;
                        //if(w.matches("\\W*#.*")) continue;
                        if(w.matches("\\W*http(s)?\\://.*")) continue;
                        if(w.matches("[^\\p{L}\\p{Nd}]*")) continue;
                        if(w.matches("(R|r)(T|t)")) continue;
                        
                        if(w.matches("(\\w+)([^\\p{L}\\p{Nd}]+)(\\w+)([^\\p{L}\\p{Nd}]*)"))
                        {
                            String[] temp = w.replaceAll("(\\w+)([^\\p{L}\\p{Nd}]+)(\\w+)([^\\p{L}\\p{Nd}]*)", "$1 $3").split(" ");
                            for( String sw: temp)
                            {
                                sw = sw.replaceAll("([^\\p{L}\\p{Nd}]*)(\\w*)([^\\p{L}\\p{Nd}]*)", "$2");
                                if(sw.matches("\\d+")) continue;
                                if(sw.matches("")) continue;
                                indexer.addDocument(id, sw.toLowerCase(), parts[1]);  
                            }
                        }
                        else
                        {
                            w = w.replaceAll("([^\\p{L}\\p{Nd}]*)(\\w*)([^\\p{L}\\p{Nd}]*)", "$2");
                            if(w.matches("\\d+")) continue;
                            if(w.matches("")) continue;
                            indexer.addDocument(id, w.toLowerCase(), parts[1]);
                        }
                    }
                    */
                }
                id++;
            }
            indexer.saveTi(it_count);
            indexer.saveTw(tot_count);
            indexer.closeListWriter();
            indexer.closeWriter();
            System.out.println("Hour " + start + ", Total Tweets: " + tot_count + ", Tweets in Italian: " + it_count);
            
        }
        catch (IOException e)
        {
        }
    }
    
}
