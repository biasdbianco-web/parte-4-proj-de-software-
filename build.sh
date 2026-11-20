#!/usr/bin/env bash
# Compila o projeto (Java 17+). Saída em ./out
set -e
cd "$(dirname "$0")"
mkdir -p out
javac --release 17 -encoding UTF-8 -d out $(find src -name '*.java')
echo "Compilação concluída: ./out"
