#!/bin/bash
javac -cp "lib/ap.jar" -sourcepath src -d classes $@ `find src -name "*.java"`
cd classes
java -cp ../lib/ap.jar:. Duniya
cd ..

