#!/usr/bin/env bash
# Roda os cenários de demonstração com entradas pré-gravadas (demo/*.txt).
# Usa uma cópia temporária dos dados e fixa o código SMS em 1234 (-Dsms.codigo=1234).
# Uso: ./demo.sh            -> roda todos
#      ./demo.sh 01         -> roda apenas o cenário que começa com 01
cd "$(dirname "$0")"
DADOS=$(mktemp -d)
cp data/planos.csv data/pratos.csv "$DADOS"/
for f in demo/${1:-[0-9]}*.txt; do
  echo "################################################################"
  echo "# CENÁRIO: $(basename "$f" .txt)"
  echo "################################################################"
  java -Dfile.encoding=UTF-8 -Dsms.codigo=1234 -Ddata.dir="$DADOS" -cp out br.com.minharefeicao.Main < "$f"
done
echo
echo "================ ARQUIVOS GRAVADOS (CSV) ================"
for a in assinantes assinaturas itens_pedido; do echo "--- $a.csv"; cat "$DADOS/$a.csv"; done
echo "--- protocolo.txt (último protocolo gerado)"; cat "$DADOS/protocolo.txt"; echo
