package com.algo.word;
  
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
  
public class NagaoAlgorithm {
    
  private int N;
    
  private List<String> leftPTable;
  private int[] leftLTable;
  private List<String> rightPTable;
  private int[] rightLTable;
  private double wordNumber;
    
  private Map<String, TFNeighbor> wordTFNeighbor;
    
  private final static String stopwords = "的很了么呢是嘛个都也比还这于不与才上用就好在和对挺去后没说";
    
  private NagaoAlgorithm(){
    N = 5;
    leftPTable = new ArrayList<String>();
    rightPTable = new ArrayList<String>();
    wordTFNeighbor = new HashMap<String, TFNeighbor>();
  }
  
  private String reverse(String phrase) {
    StringBuilder reversePhrase = new StringBuilder();
    for (int i = phrase.length() - 1; i >= 0; i--)
      reversePhrase.append(phrase.charAt(i));
    return reversePhrase.toString();
  }
 
  private int coPrefixLength(String s1, String s2){
    int coPrefixLength = 0;
    for(int i = 0; i < Math.min(s1.length(), s2.length()); i++){
      if(s1.charAt(i) == s2.charAt(i))  coPrefixLength++;
      else break;
    }
    return coPrefixLength;
  }

  private void addToPTable(String line){
    String[] phrases = line.split("[^\u4E00-\u9FA5]+|["+stopwords+"]");
    for(String phrase : phrases){
      for(int i = 0; i < phrase.length(); i++)
        rightPTable.add(phrase.substring(i));
      String reversePhrase = reverse(phrase);
      for(int i = 0; i < reversePhrase.length(); i++)
        leftPTable.add(reversePhrase.substring(i));
      wordNumber += phrase.length();
    }
  }
    
  private void countLTable(){
    Collections.sort(rightPTable);
    rightLTable = new int[rightPTable.size()];
    for(int i = 1; i < rightPTable.size(); i++)
      rightLTable[i] = coPrefixLength(rightPTable.get(i-1), rightPTable.get(i));
      
    Collections.sort(leftPTable);
    leftLTable = new int[leftPTable.size()];
    for(int i = 1; i < leftPTable.size(); i++)
      leftLTable[i] = coPrefixLength(leftPTable.get(i-1), leftPTable.get(i));
      
    System.out.println("Info: [Nagao Algorithm Step 2]: having sorted PTable and counted left and right LTable");
  }
  private void countTFNeighbor(){
    for(int pIndex = 0; pIndex < rightPTable.size(); pIndex++){
      String phrase = rightPTable.get(pIndex);
      for(int length = 1 + rightLTable[pIndex]; length <= N && length <= phrase.length(); length++){
        String word = phrase.substring(0, length);
        TFNeighbor tfNeighbor = new TFNeighbor();
        tfNeighbor.incrementTF();
        if(phrase.length() > length)
          tfNeighbor.addToRightNeighbor(phrase.charAt(length));
        for(int lIndex = pIndex+1; lIndex < rightLTable.length; lIndex++){
          if(rightLTable[lIndex] >= length){
            tfNeighbor.incrementTF();
            String coPhrase = rightPTable.get(lIndex);
            if(coPhrase.length() > length)
              tfNeighbor.addToRightNeighbor(coPhrase.charAt(length));
          }
          else break;
        }
        wordTFNeighbor.put(word, tfNeighbor);
      }
    }
    for(int pIndex = 0; pIndex < leftPTable.size(); pIndex++){
      String phrase = leftPTable.get(pIndex);
      for(int length = 1 + leftLTable[pIndex]; length <= N && length <= phrase.length(); length++){
        String word = reverse(phrase.substring(0, length));
        TFNeighbor tfNeighbor = wordTFNeighbor.get(word);
        if(phrase.length() > length)
          tfNeighbor.addToLeftNeighbor(phrase.charAt(length));
        for(int lIndex = pIndex + 1; lIndex < leftLTable.length; lIndex++){
          if(leftLTable[lIndex] >= length){
            String coPhrase = leftPTable.get(lIndex);
            if(coPhrase.length() > length)
              tfNeighbor.addToLeftNeighbor(coPhrase.charAt(length));
          }
          else break;
        }
      }
    }
    System.out.println("Info: [Nagao Algorithm Step 3]: having counted TF and Neighbor");
  }
  private double countMI(String word){
    if(word.length() <= 1)  return 0;
    double coProbability = wordTFNeighbor.get(word).getTF()/wordNumber;
    List<Double> mi = new ArrayList<Double>(word.length());
    for(int pos = 1; pos < word.length(); pos++){
      String leftPart = word.substring(0, pos);
      String rightPart = word.substring(pos);
      double leftProbability = wordTFNeighbor.get(leftPart).getTF()/wordNumber;
      double rightProbability = wordTFNeighbor.get(rightPart).getTF()/wordNumber;
      mi.add(coProbability/(leftProbability*rightProbability));
    }
    return Collections.min(mi);
  }
  private void saveTFNeighborInfoMI(String out, String stopList, String[] threshold){
    try {
      Set<String> stopWords = new HashSet<String>();
      BufferedReader br = new BufferedReader(new FileReader(stopList));
      String line;
      while((line = br.readLine()) != null){
        if(line.length() > 1)
          stopWords.add(line);
      }
      br.close();
      BufferedWriter bw = new BufferedWriter(new FileWriter(out));
      for(Map.Entry<String, TFNeighbor> entry : wordTFNeighbor.entrySet()){
        if( entry.getKey().length() <= 1 || stopWords.contains(entry.getKey()) ) continue;
        TFNeighbor tfNeighbor = entry.getValue();
          
          
        int tf, leftNeighborNumber, rightNeighborNumber;
        double mi;
        tf = tfNeighbor.getTF();
        leftNeighborNumber = tfNeighbor.getLeftNeighborNumber();
        rightNeighborNumber = tfNeighbor.getRightNeighborNumber();
        mi = countMI(entry.getKey());
        if(tf > Integer.parseInt(threshold[0]) && leftNeighborNumber > Integer.parseInt(threshold[1]) &&
            rightNeighborNumber > Integer.parseInt(threshold[2]) && mi > Integer.parseInt(threshold[3]) ){
          StringBuilder sb = new StringBuilder();
          sb.append(entry.getKey());
          sb.append(",").append(tf);
          sb.append(",").append(leftNeighborNumber);
          sb.append(",").append(rightNeighborNumber);
          sb.append(",").append(tfNeighbor.getLeftNeighborEntropy());
          sb.append(",").append(tfNeighbor.getRightNeighborEntropy());
          sb.append(",").append(mi).append("\n");
          bw.write(sb.toString());
        }
      }
      bw.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Info: [Nagao Algorithm Step 4]: having saved to file");
  }
  public static void applyNagao(String[] inputs, String out, String stopList){
    NagaoAlgorithm nagao = new NagaoAlgorithm();
    String line;
    for(String in : inputs){
      try {
        BufferedReader br = new BufferedReader(new FileReader(in));
        while((line = br.readLine()) != null){
          nagao.addToPTable(line);
        }
        br.close();
      } catch (IOException e) {
        throw new RuntimeException();
      }
    }
    System.out.println("Info: [Nagao Algorithm Step 1]: having added all left and right substrings to PTable");
    nagao.countLTable();
    nagao.countTFNeighbor();
    nagao.saveTFNeighborInfoMI(out, stopList, "20,3,3,5".split(","));
  }
  public static void applyNagao(String[] inputs, String out, String stopList, int n, String filter){
    NagaoAlgorithm nagao = new NagaoAlgorithm();
    nagao.setN(n);
    String[] threshold = filter.split(",");
    if(threshold.length != 4){
      System.out.println("ERROR: filter must have 4 numbers, seperated with ',' ");
      return;
    }
    String line;
    for(String in : inputs){
      try {
        BufferedReader br = new BufferedReader(new FileReader(in));
        while((line = br.readLine()) != null){
          nagao.addToPTable(line);
        }
        br.close();
      } catch (IOException e) {
        throw new RuntimeException();
      }
    }
    System.out.println("Info: [Nagao Algorithm Step 1]: having added all left and right substrings to PTable");
    nagao.countLTable();
    nagao.countTFNeighbor();
    nagao.saveTFNeighborInfoMI(out, stopList, threshold);
  }
  private void setN(int n){
    N = n;
  }
    
  public static void main(String[] args) {
    String[] ins = {"E://test//ganfen.txt"};
    applyNagao(ins, "E://test//out.txt", "E://test//stoplist.txt");
  }
  
}
