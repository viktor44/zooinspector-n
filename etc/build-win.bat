setlocal

chcp 65001
cd %~dp0..

set JAVA_HOME=%JAVA17_HOME%

call mvn clean package -DskipTests

"%JAVA_HOME%\bin\jpackage.exe" @target\jpackage-win.args --type app-image
"%JAVA_HOME%\bin\jpackage.exe" @target\jpackage-win.args ^
	--type exe ^
	--win-per-user-install ^
	--win-shortcut ^
	--win-menu ^
	--win-menu-group ZooInspector ^
	--win-upgrade-uuid a68e451c-3cae-4e4a-bf2b-eedf7c501414

rem	--win-dir-chooser ^
rem	--win-shortcut-prompt ^

endlocal
