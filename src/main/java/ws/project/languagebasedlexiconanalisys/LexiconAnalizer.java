/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;
import twitter4j.JSONException;

/**
 *
 * @author Ulysses_D
 */
public class LexiconAnalizer {
   
    public static TwitterStreamAnalizer analizer; 
    
    public static void main(String[] args) throws IOException, ParseException, JSONException, org.json.simple.parser.ParseException{
        //analizer = new TwitterStreamAnalizer();
        //analizer.parseStream();
        
        LexiconProcessor processor = new LexiconProcessor();

        //processor.calculateTermFreq();
        //processor.calculateSetCover();
        //processor.computeSimilarity();
        processor.computeTimeSeries();
        //processor.printTweets();
        // processor.calculateSetCover();
   }

}
