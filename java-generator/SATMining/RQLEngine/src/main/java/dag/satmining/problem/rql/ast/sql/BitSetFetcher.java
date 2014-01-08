package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.List;

public interface BitSetFetcher {
	
	boolean next() throws SQLException, IOException;
	BitSet getBitSet() throws SQLException, IOException;
	void setSelect(List<SQLBooleanValue> conditionsToTest);
	void setFrom(From from);
	void setWhere(Where where);
	
}
