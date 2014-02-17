/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/sql/NestedLoopBitSetFetcher.java

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
import java.sql.SQLException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NestedLoopBitSetFetcher implements BitSetFetcher {

	private static final Logger LOG = LoggerFactory
			.getLogger(NestedLoopBitSetFetcher.class);

	private TupleFetcher[] _fetchers;
	private Map<String, Integer> _tupleIdx;
	private Tuple[] _tuples;
	private List<SQLBooleanValue> _selectExprs;
	private SQLBooleanValue _filter;
	private boolean _evalSetupDone = false;
	private final Connection _connection;
	private boolean _finished = false;
	private From _from;

	public NestedLoopBitSetFetcher(Connection connection) {
		this._connection = connection;
	}

	private boolean nextRaw() throws SQLException, IOException {
		evalSetup();
		if (_tuples == null && !_finished) {
			_tuples = new Tuple[_fetchers.length];
			for (int i = 0; i < _tuples.length; i++) {
				if (_fetchers[i].next()) {
					_tuples[i] = _fetchers[i].getTuple();
				} else {
					LOG.warn("Fetcher {} as no tuple", i);
					_tuples = null;
					_finished = true;
					return false;
				}
			}
			return true;
		}
		if (!_finished) {
			for (int i = 0; i < _tuples.length; i++) {
				if (_fetchers[i].next()) {
					for (int j = 0; j < i; j++) {
						_fetchers[j].reset();
						_fetchers[j].next();
						_tuples[j] = _fetchers[j].getTuple();
					}
					_tuples[i] = _fetchers[i].getTuple();
					return true;
				}
			}
			_finished = true;
		}
		return false;
	}

	protected TupleFetcher getTupleFetcher(FromExpression expr) throws SQLException, IOException {
		return expr.getTupleFetcher(_connection);
	}
	
	private void evalSetup() throws SQLException, IOException {
		if (!_evalSetupDone) {
			List<NamedFromExpression> fList = _from.getFromList();
			_fetchers = new TupleFetcher[fList.size()];
			_tupleIdx = new HashMap<String, Integer>();
			for (int i = 0; i < fList.size(); i++) {
				_fetchers[i] = getTupleFetcher(fList.get(i));
				_tupleIdx.put(fList.get(i).getName().toUpperCase(), i);
			}
			@SuppressWarnings("unchecked")
			Map<String, Integer>[] schemas = new Map[_fetchers.length];
			for (int i = 0; i < schemas.length; i++) {
				schemas[i] = _fetchers[i].getSchema();
			}
			for (SQLBooleanValue sbv : _selectExprs) {
				sbv.setupEval(_tupleIdx, schemas);
			}
			_filter.setupEval(_tupleIdx, schemas);
			_evalSetupDone = true;
		}
	}

	@Override
	public BitSet getBitSet() throws SQLException {
		if (_tuples != null) {
			BitSet data = new BitSet();
			for (int i = 0; i < _selectExprs.size(); i++) {
				if (_selectExprs.get(i).eval(_tuples)) {
					data.set(i);
				}
			}
			return data;
		} else {
			return null;
		}
	}

	@Override
	public void setSelect(List<SQLBooleanValue> conditionsToTest) {
		_selectExprs = conditionsToTest;
		_evalSetupDone = false;
	}

	@Override
	public void setFrom(From from) {
		this._from = from;
		_evalSetupDone = false;
	}

	@Override
	public void setWhere(Where where) {
		_filter = where.getExpr();
		_evalSetupDone = false;
	}

	@Override
	public boolean next() throws SQLException, IOException {
		while (nextRaw() && !_filter.eval(_tuples)) {
			// do nothing;
		}
		return !_finished;
	}
}
