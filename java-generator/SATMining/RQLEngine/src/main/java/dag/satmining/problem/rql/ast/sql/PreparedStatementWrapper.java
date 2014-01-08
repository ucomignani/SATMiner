package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreparedStatementWrapper implements TupleFetcher {

	private static final Logger LOG = LoggerFactory
			.getLogger(PreparedStatementWrapper.class);

	private ResultSet _rs;
	private PreparedStatement _pstat;
	private Map<String, Integer> _schema;
	private Tuple _current = null;

	public PreparedStatementWrapper(Connection connection, String query)
			throws SQLException {
		_pstat = connection.prepareStatement(query);
		reset();
		ResultSetMetaData metadata = _rs.getMetaData();
		_schema = new HashMap<String, Integer>();
		for (int i = 1; i <= metadata.getColumnCount(); i++) {
			_schema.put(metadata.getColumnName(i).toUpperCase(), i-1);
		}
	}

	private void fetchCurrent() throws SQLException {
		try {
			if (_current == null && _rs.next()) {
				_current = new Tuple(_schema);
				for (int i = 0; i < _schema.size(); i++) {
					_current.setValue(i, _rs.getString(i+1));
				}
			}
		} catch (SQLException e) {
			LOG.error("Error while fetching tuple", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean next() throws SQLException {
		_current = null;
		fetchCurrent();
		return _current != null;
	}

	@Override
	public Tuple getTuple() throws SQLException, IOException {
		fetchCurrent();
		return _current;
	}

	@Override
	public void reset() throws SQLException {
		_rs = _pstat.executeQuery();
		_current = null;
	}

	@Override
	public Map<String, Integer> getSchema() {
		return _schema;
	}

}
