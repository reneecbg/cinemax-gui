#!/bin/bash
# ============================================================================
#  CineMax - Script di build (macOS / Linux)
#  Compila i tre moduli Maven e copia i due eseguibili in bin/
# ============================================================================
set -e

echo "Compilazione di cinemax-common, cinemax-server, cinemax-client..."
mvn -pl src/cinemax-common,src/cinemax-server,src/cinemax-client -am package

echo
echo "Copio i jar eseguibili in bin/..."
mkdir -p bin
cp src/cinemax-server/target/CineMaxServer.jar bin/CineMaxServer.jar
cp src/cinemax-client/target/CineMaxClient.jar bin/CineMaxClient.jar

echo
echo "Fatto. Eseguibili disponibili in bin/CineMaxServer.jar e bin/CineMaxClient.jar"
