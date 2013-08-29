rm -rf build/java
mkdir build
mkdir build/java

cd java
mvn -DskipTests=true clean package
mvn jar:test-jar
cd ..
cp java/target/*.jar build/java/
cp lib/*.jar build/java

cd tests/java
javac -classpath ../../lib/netty-3.5.7.Final.jar:../../build/java/top-link-1.1.1-SNAPSHOT.jar RemotingServerTest.java
javac -classpath ../../lib/netty-3.5.7.Final.jar:../../build/java/top-link-1.1.1-SNAPSHOT.jar RemotingClientTest.java
javac -classpath ../../build/java/top-link-1.1.1-SNAPSHOT.jar RemotingClientEmbeddedTest.java
cp *.class ../../build/java
cp *.bat ../../build/java
cp *.sh ../../build/java
cd ../../


