@echo off
REM ============================================================================
REM  CineMax - Script di build (Windows)
REM  Compila i tre moduli Maven e copia i due eseguibili in bin\
REM ============================================================================

echo Compilazione di cinemax-common, cinemax-server, cinemax-client...
call mvn -pl src\cinemax-common,src\cinemax-server,src\cinemax-client -am package
if errorlevel 1 (
    echo.
    echo BUILD FALLITA - controlla gli errori sopra.
    exit /b 1
)

echo.
echo Copio i jar eseguibili in bin\...
if not exist bin mkdir bin
copy /Y src\cinemax-server\target\CineMaxServer.jar bin\CineMaxServer.jar
copy /Y src\cinemax-client\target\CineMaxClient.jar bin\CineMaxClient.jar

echo.
echo Fatto. Eseguibili disponibili in bin\CineMaxServer.jar e bin\CineMaxClient.jar
