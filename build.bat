@echo off
REM Compila o projeto (Java 17+). Saida em .\out
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac --release 17 -encoding UTF-8 -d out @sources.txt
del sources.txt
echo Compilacao concluida: .\out
