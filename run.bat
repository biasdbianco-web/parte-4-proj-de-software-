@echo off
REM Executa o sistema (interativo). Use apos build.bat
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Ddata.dir=data -cp out br.com.minharefeicao.Main
