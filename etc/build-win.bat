setlocal

chcp 65001
cd %~dp0..

set JAVA_HOME=%JAVA17_HOME%

rem call mvn clean package -DskipTests

"%JAVA_HOME%\bin\jpackage.exe" ^
    --type exe ^
    --input target\out\lib ^
    --dest target\distr\win ^
    --main-jar zooinspector-n-1.1.RC1.jar ^
    --main-class org.apache.zookeeper.inspector.ZooInspector
    
 
endlocal
