/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

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
        //Inizializza file JSON
        FileWriter file = new FileWriter("data.json");
        JSONObject obj = new JSONObject();
        final JSONArray data = new JSONArray();  
        obj.put("data", data);
        file.write(obj.toJSONString());
        file.flush();
        file.close();
          
        SimpleDateFormat currentDate = new SimpleDateFormat(); 
        currentDate.applyPattern("dd-MM-yyyy");  
        final String currentDateStr = currentDate.format(new Date()); 
       
        
        ConfigurationBuilder cfg = new ConfigurationBuilder();
        cfg.setOAuthAccessToken("3065669171-9Hp3VZbz7f0BCsvWWFfgywgqimSIp1AlT98745S");
        cfg.setOAuthAccessTokenSecret("AUmg0AdhHzMXisnP1WV7Wnsw5amWFQPyIojI5aBG5qV4A");
        cfg.setOAuthConsumerKey("arieQRhL2WwgRFfXFLAJp5Hkw");
        cfg.setOAuthConsumerSecret("NvmWqgN1UKKPUWoh9d9Z2PuQobOah8IR5faqX2WjDGBL053sWE");
        
         StatusListener listener;
        listener = new StatusListener() {
            @Override
            public void onStatus(Status status){
                SimpleDateFormat tweetDate = new SimpleDateFormat();
                tweetDate.applyPattern("dd-MM-yyyy");
                String tweetDateStr = tweetDate.format(status.getCreatedAt());
                
                    try {
                        indexer.openWriter(currentDateStr); 
                    } catch (IOException ex) {
                        Logger.getLogger(TwitterStreamAnalizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    //CHIUDO se cambio giorno ma non ho raggiunto l'1%
                    if(!tweetDateStr.equals(currentDateStr)){
                        try {
                            indexer.closeWriter();
                        } catch (IOException ex) {
                            Logger.getLogger(TwitterStreamAnalizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        writeOnJson(currentDateStr);
                        System.out.println("Giorno successivo, completato senza aver raggiunto 1%");
                        System.exit(0);
                    }
                    tot_count++;
                    
                    
                    if(status.getLang().equals("it")){
                        it_count++;
                        try {
                            indexer.addTweet(id,status.getText());
                        } catch (IOException ex) {
                            Logger.getLogger(TwitterStreamAnalizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                    try {
                        indexer.closeWriter();
                    } catch (IOException ex) {
                        Logger.getLogger(TwitterStreamAnalizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
               
            }
            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
            }
            @Override
            public void onTrackLimitationNotice(int i) {
            }
            @Override
            public void onScrubGeo(long l, long l1) {
            }
            @Override
            public void onStallWarning(StallWarning sw) {
            }
            @Override
            public void onException(Exception excptn) {
                TwitterException exc = (TwitterException)excptn;
                if(exc.exceededRateLimitation()){
                    try {
                        indexer.closeWriter();
                    } catch (IOException ex) {
                        Logger.getLogger(TwitterStreamAnalizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    writeOnJson(currentDateStr);
                    System.out.println("1% raccolto, dati raccolti. Amen");
                }
            }
        
        };
          
        TwitterStream twitterStream = new TwitterStreamFactory(cfg.build()).getInstance();
        twitterStream.addListener(listener);
        
        indexer.addIndex(currentDateStr);
        indexer.openWriter(currentDateStr);
        twitterStream.sample();
        
          
      }
      
      
    void writeOnJson(String currentDate){
         JSONParser parser = new JSONParser();
 
        try {
 
            Object obj = parser.parse(new FileReader("data.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray dataList = (JSONArray) jsonObject.get("data");
            
            JSONObject jsonObject2 = new JSONObject();
            jsonObject2.put("Tw", tot_count);
            jsonObject2.put("Ti", it_count);
            jsonObject2.put("index", currentDate);
            dataList.add(jsonObject2);
            
            JSONObject jsonObject3 = new JSONObject();
            jsonObject3.put("data", dataList);
            FileWriter file = new FileWriter("data.json");
            file.write(jsonObject3.toJSONString());
            file.flush();
            file.close();
            
            
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void parseTextStream() throws IOException { //Da eliminare
        
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
