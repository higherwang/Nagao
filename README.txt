javac编译多个文件
javac -d ./out ./*.java

java调用编译好的
java -cp ./out com.algo.word.Main ./test.txt out.txt stoplist.txt 

