setlocal

chcp 65001
cd %~dp0..\..

set JAVA_HOME=%JAVA17_HOME%

rem start mvn clean package -DskipTests

"%JAVA_HOME%\bin\jpackage.exe" ^
    --type exe ^
    --input target\distr\lib ^
    --main-jar zooinspector-n-1.1.RC1.jar ^
    --main-class org.apache.zookeeper.inspector.ZooInspector
    
 
endlocal
