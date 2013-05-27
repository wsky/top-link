mkdir build\java
mkdir build\java
rm -rf build\java

cd java
mvn -DskipTests=true clean package
mvn jar:test-jar
cd ..
xcopy java\target\*.jar build\java /Y /E /I /R
xcopy lib\*.jar build\java /Y /E /I /R

cd tests\java
javac -classpath "../../lib/netty-3.5.7.Final.jar;../../build/java/top-link-1.1-SNAPSHOT.jar" RemotingServerTest.java
javac -classpath "../../lib/netty-3.5.7.Final.jar;../../build/java/top-link-1.1-SNAPSHOT.jar" RemotingClientTest.java
javac -classpath "../../build/java/top-link-1.1-SNAPSHOT.jar" RemotingClientEmbeddedTest.java
xcopy *.class ..\..\build\java /Y /E /I /R
xcopy *.bat ..\..\build\java /Y /E /I /R
xcopy *.sh ..\..\build\java /Y /E /I /R
cd ..\..\


