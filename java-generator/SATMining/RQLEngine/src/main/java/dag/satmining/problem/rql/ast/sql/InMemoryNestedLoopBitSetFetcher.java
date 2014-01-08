package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class InMemoryNestedLoopBitSetFetcher extends NestedLoopBitSetFetcher {

	public InMemoryNestedLoopBitSetFetcher(Connection connection) {
		super(connection);
	}

	@Override
	protected TupleFetcher getTupleFetcher(FromExpression expr) throws SQLException, IOException {
		return new CacheTupleFetcher(super.getTupleFetcher(expr));
	}
}
