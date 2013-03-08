mkdir build\java
rm -rf build\java

cd java
mvn -DskipTests=true clean package
cd ..
cp java\target\top-link-1.0-SNAPSHOT.jar build\java\top-link-1.0-SNAPSHOT.jar
xcopy lib\*.jar build\java /Y /E /I /R

cd tests\java
javac -classpath "../../lib/netty-3.5.7.Final.jar;../../build/java/top-link-1.0-SNAPSHOT.jar" RemotingServerTest.java
javac -classpath "../../lib/netty-3.5.7.Final.jar;../../build/java/top-link-1.0-SNAPSHOT.jar" RemotingClientTest.java
xcopy *.class ..\..\build\java /Y /E /I /R
xcopy *.bat ..\..\build\java /Y /E /I /R
xcopy *.sh ..\..\build\java /Y /E /I /R
cd ..\..\


