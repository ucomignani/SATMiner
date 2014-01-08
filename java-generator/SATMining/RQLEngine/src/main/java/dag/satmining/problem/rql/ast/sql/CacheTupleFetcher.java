package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CacheTupleFetcher implements TupleFetcher {

	private final Tuple[] _data;
	private final int _size;
	private int _current;
	private final Map<String,Integer> _schema;
	
	public CacheTupleFetcher(TupleFetcher backend) throws SQLException, IOException {
		this._schema = backend.getSchema();
		List<Tuple> data = new ArrayList<Tuple>();
		while(backend.next()) {
			data.add(backend.getTuple());
		}
		_size = data.size();
		_data = data.toArray(new Tuple[_size]);
		reset();
	}
	
	@Override
	public boolean next() throws SQLException, IOException {
		_current++;
		return _current < _size;
	}

	@Override
	public Tuple getTuple() throws SQLException, IOException {
		return _data[_current];
	}

	@Override
	public void reset() throws SQLException, IOException {
		_current = -1;
	}

	@Override
	public Map<String, Integer> getSchema() throws SQLException, IOException {
		return _schema;
	}

}
