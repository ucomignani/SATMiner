/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/sql/SingleStatementBitSetFetcher.java

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

package dag.satmining.problem.satql.ast.sql;

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
