#!/bin/bash
echo java -server -cp /Users/ecoquery/Library/java/postgresql-9.0-801.jdbc4.jar:target/SATMiner-1.0-SNAPSHOT-jar-with-dependencies.jar dag.satmining.run.Main -satql -jdbc 'jdbc:postgresql:postgres?user=etudiant&password=etudiant' $*
java -server -cp /Users/ecoquery/Library/java/postgresql-9.0-801.jdbc4.jar:target/SATMiner-1.0-SNAPSHOT-jar-with-dependencies.jar dag.satmining.run.Main -satql -jdbc 'jdbc:postgresql:postgres?user=etudiant&password=etudiant' $*

