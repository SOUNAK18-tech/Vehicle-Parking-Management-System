@echo off
cd /d "%~dp0"
"C:\Program Files\Android\Android Studio\jbr\bin\javaw.exe" --enable-native-access=ALL-UNNAMED -cp ".;sqlite-jdbc-3.53.2.1.jar" Main
