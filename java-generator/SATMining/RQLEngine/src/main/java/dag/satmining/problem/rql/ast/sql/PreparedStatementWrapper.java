/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/sql/PreparedStatementWrapper.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

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
