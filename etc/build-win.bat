setlocal

chcp 65001
cd %~dp0..

set JAVA_HOME=%JAVA17_HOME%

call mvn clean package -DskipTests

"%JAVA_HOME%\bin\jpackage.exe" @target\jpackage-win.args --type app-image
"%JAVA_HOME%\bin\jpackage.exe" @target\jpackage-win.args --type exe
    
 
endlocal
