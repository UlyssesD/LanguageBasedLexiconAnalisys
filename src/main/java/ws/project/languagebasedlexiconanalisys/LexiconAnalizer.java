/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.project.languagebasedlexiconanalisys;

/**
 *
 * @author Ulysses_D
 */
public class LexiconAnalizer {
   
   public static TwitterStreamAnalizer analizer; 
    
   public static void main(String[] args){
       analizer = new TwitterStreamAnalizer();
       analizer.parseStream();
   }
}
