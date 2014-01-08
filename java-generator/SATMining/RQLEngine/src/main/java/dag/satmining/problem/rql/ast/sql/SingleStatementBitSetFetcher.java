package dag.satmining.problem.rql.ast.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleStatementBitSetFetcher implements BitSetFetcher {

	private static final Logger LOG = LoggerFactory.getLogger(SingleStatementBitSetFetcher.class);
	public static final char TRUE_C = '1';
	public static final char FALSE_C = '0';
	public static final SQLConstant TRUE_V = new SQLConstant("'" + TRUE_C + "'");
	public static final SQLConstant FALSE_V = new SQLConstant("'" + FALSE_C
			+ "'");

	private Connection _connection;
	private From _from;
	private Where _where;
	private Select _select;
	private ResultSet _cursor;
	private boolean _finished;
	private BitSet _current;
	private int _nbCond;

	public SingleStatementBitSetFetcher(Connection connection) {
		this._connection = connection;
		this._finished = false;
	}
	
	@Override
	public boolean next() throws SQLException {
		if (_finished) {
			return false;
		}
		if (_cursor == null) {
			_cursor = buildAndExecuteQuery();
		}
		_current = null;
		if (_cursor.next()) {
			return true;
		} else {
			_finished = true;
			_cursor.close();
			return false;
		}
	}

	private ResultSet buildAndExecuteQuery() throws SQLException {
		Statement stat = _connection.createStatement();
		Query query = new Query(_select, _from, _where);
		String sqlRepr = query.asSQL();
		LOG.debug("Generated SQL Query: {}", sqlRepr);
		return stat.executeQuery(sqlRepr);
	}

	@Override
	public BitSet getBitSet() throws SQLException {
		if (_current == null) {
			_current = new BitSet();
			String data = _cursor.getString(1);
			for(int i = 0; i < _nbCond; i++) {
				if (data.charAt(i)==TRUE_C) {
					_current.set(i);
				}
			}
		}
		return _current;
	}

	@Override
	public void setSelect(List<SQLBooleanValue> conditionsToTest) {
		_nbCond = conditionsToTest.size();
		_select = translate(conditionsToTest);
	}

	private static Select translate(List<SQLBooleanValue> conditionsToTest) {
		Select result = new Select();
		SQLValue builtString = new SQLConstant("''");
		for (SQLBooleanValue condition : conditionsToTest) {
			builtString = SQLBinOpValue.concat(builtString,
					translateCondition(condition));
		}
		result.addEntry(builtString, "res");
		return result;
	}

	private static SQLValue translateCondition(SQLBooleanValue condition) {
		CaseNoValue caseStatement = new CaseNoValue();
		caseStatement.addWhen(condition, TRUE_V);
		caseStatement.setElse(FALSE_V);
		return caseStatement;
	}

	@Override
	public void setFrom(From from) {
		_from = from;
	}

	@Override
	public void setWhere(Where where) {
		_where = where;
	}

}
