package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public interface TupleFetcher {

	boolean next() throws SQLException, IOException;
	
	Tuple getTuple() throws SQLException, IOException;
	
	void reset() throws SQLException, IOException;
	
	Map<String,Integer> getSchema() throws SQLException, IOException;
	
}
