#!/usr/bin/env bash
# Executa o sistema (interativo). Use após ./build.sh
cd "$(dirname "$0")"
java -Dfile.encoding=UTF-8 -Ddata.dir=data -cp out br.com.minharefeicao.Main
