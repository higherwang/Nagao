package com.algo.word;
  
public class Main {
  
  public static void main(String[] args) {
    
    System.out.println("Hello,World! test");
    if(args.length == 3)
      NagaoAlgorithm.applyNagao(args[0].split(","), args[1], args[2]);
    else if(args.length == 5)
      NagaoAlgorithm.applyNagao(args[0].split(","), args[1], args[2], Integer.parseInt(args[3]), args[4]);
    else
      System.out.println("need 3 or 4 args");   
      
  }
  
}
