javac -encoding UTF-8 -d bin -classpath lib/* -sourcepath src src/app/App.java
cd ./src/tests
copy /y *.txt ..\..\bin\tests\
cd ../../
java -classpath lib/*;bin app.App
timeout /t -1