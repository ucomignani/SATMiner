

SAT generator for frequent sequence mining problem and RLT/RQL
- can output DIMACS format
- can output solutions using SAT4J 
Usage:
java -jar /path/to/SATMiner-xxxxx-jar-with-dependecies.jar -help
or (to use with jdbc for rql queries)
java -cp yourjdbcdriver.jar:/path/to/SATMiner-xxxxx-jar-with-dependecies.jar dag.satmining.run.Main -help

Compiling:
- requires Java 1.5+
- install maven (http://maven.apache.org/)
- run "mvn package" from command line in this directory
- the full packaged jar is in SATMiner/target/
