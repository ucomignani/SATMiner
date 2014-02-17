

SAT generator for frequent sequence mining problem and RLT/SATQL
- can output DIMACS format
- can output solutions using SAT4J 
Usage:
java -jar /path/to/SATMiner-xxxxx-jar-with-dependecies.jar -help
or (to use with jdbc for satql queries)
java -cp yourjdbcdriver.jar:/path/to/SATMiner-xxxxx-jar-with-dependecies.jar dag.satmining.run.Main -help

Compiling:
- requires Java 1.6+
- install maven (http://maven.apache.org/)
- compile minisat-all-models (this can be skipped if you do not run
  unit tests by adding -DskipTests to the mvn package command below):
  run make in ../minisat_all_models/core and copy or link the minisat
  executable in SATMiner and satmining-backend directories
- run "mvn install" from command line in this directory
- the full packaged jar is in SATMiner/target/
