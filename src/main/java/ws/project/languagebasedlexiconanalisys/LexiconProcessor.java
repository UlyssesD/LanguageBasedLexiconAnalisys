/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

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
    
    public void calculateSetCover() throws IOException, ParseException
    {
        indexer.openReader("13");
        indexer.calculateSetCover();
    }
    
     public void printTweets() throws IOException, ParseException
    {
        indexer.openReader("13");
        indexer.printTweets();
    }
    
   
}
