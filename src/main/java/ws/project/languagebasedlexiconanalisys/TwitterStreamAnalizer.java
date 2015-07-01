/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Ulysses_D
 */
class TwitterStreamAnalizer {

    public TwitterStreamAnalizer() {
    
    }  

    void parseStream() {
        LexiconIndexer indexer = new LexiconIndexer();
        
        try (BufferedReader br = new BufferedReader(new FileReader("tweets/15giu.txt")))
	{
            String currentLine;
            
            while ((currentLine = br.readLine()) != null)
            {
		String[] parts = currentLine.split(" \\: ", 2);
                if(parts.length == 1) continue;
                String[] infos = parts[0].split(", ");
                
                //System.out.println(infos.length);
                if(infos.length != 1)
                {
                    if(!"it".equals(infos[1])) continue;
                    
                    //Il tweet è in "Itagliano"
                    System.out.println(parts[1]);
                    String[] words = parts[1].split(" ");
                    for (String w: words)
                    {
                        //Rimuoviamo parole tipo "@qualcosa", "#qualcosa", "http(s)://", caratteri speciali (è?), "RT";
                        if(w.matches("\\W*@.*")) continue;
                        if(w.matches("\\W*#.*")) continue;
                        if(w.matches("\\W*http(s)?\\://.*")) continue;
                        if(w.matches("\\W*")) continue;
                        if(w.matches("RT")) continue;
                        
                        w = w.replaceAll("(\\w+)(\\W+)(\\w+)", "$1 $3");
                        w = w.replaceAll("(\\W*)(\\w*)(\\W*)", "$2");
                        System.out.println(w);
                        
                    }
                }
            }
        }
        catch (IOException e)
        {
        }
    }
}
