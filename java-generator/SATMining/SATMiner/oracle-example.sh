#!/bin/bash
java -server -cp /Users/ecoquery/Library/java/ojdbc14.jar:target/SATMiner-1.0-SNAPSHOT-jar-with-dependencies.jar dag.satmining.run.Main -satql -i src/test/resources/funct_deps_studios.satql -jdbc jdbc:oracle:thin:ecoquery/oramdp@localhost:1521:orcl -driver oracle.jdbc.driver.OracleDriver -debug -sat4j -o funct_depts_test.output
