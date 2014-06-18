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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.problem.satql.jdbc.DriverCapabilities;

public class SingleStatementBitSetFetcher implements BitSetFetcher {

    private static final Logger LOG = LoggerFactory
            .getLogger(SingleStatementBitSetFetcher.class);
    public static final char TRUE_C = '1';
    public static final char FALSE_C = '0';

    private Connection _connection;
    private From _from;
    private Where _where;
    private Select _select;
    private ResultSet _cursor;
    private boolean _finished;
    private BitSetWithRowNumbers _current;
    private int _nbCond;
    private int _nbTuplesVariables;
    private ArrayList<Integer> _sizeNUplets = new ArrayList<Integer>();
    private boolean _singleAttribute = true;
    private boolean _singleForced = false;

    public SingleStatementBitSetFetcher(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("null connection");
        }
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
    public BitSetWithRowNumbers getBitSet() throws SQLException {
        if (_current == null) {
                _current = fromMultiple(_cursor, _nbCond, _nbTuplesVariables, _sizeNUplets);
        }
        return _current;
    }

    private static BitSetWithRowNumbers fromMultiple(ResultSet rs, int nbCond, int nbTuplesVariables, ArrayList<Integer> sizeNUplets)
            throws SQLException {
        BitSetWithRowNumbers res = new BitSetWithRowNumbers();
        int cmpt = nbCond +1;
        
        for (int i = 0; i < nbCond; ++i) {
            if (rs.getBoolean(i + 1)) {
                res.getBitSet().set(i);
            }
        }
        for(int j=0; j<sizeNUplets.size();j++){
        	res.getRowNumbersList().add(new ArrayList<Integer>());
        	for(int k = 0; k < sizeNUplets.get(j); k++){
            	res.getRowNumbersList().getLast().add(rs.getInt(cmpt));
        		LOG.debug("taille =" + cmpt + " valeur = "+rs.getInt(cmpt));
        		cmpt++;
        	}
        }        
        return res;
    }

    @Override
    public void setSelect(List<SQLBooleanValue> conditionsToTest) {
        _nbCond = conditionsToTest.size();
        if (!_singleForced) {
            _singleAttribute = !DriverCapabilities.supportsNBooleanAtts(
                    _connection, _nbCond);
        }
        if (_singleAttribute) {
            _select = translateSingle(conditionsToTest);
        } else {
            _select = translateMultiple(conditionsToTest);
        }
    }

    private static Select translateSingle(List<SQLBooleanValue> conditionsToTest) {
        Select result = new Select();
        StringBuilder concatCaseStrings = new StringBuilder("''");
        for (SQLBooleanValue condition : conditionsToTest) {
            concatCaseStrings.append(" || ");
            appendCase(concatCaseStrings, condition);
        }
        result.addEntry(new SQLDelegatedValue(concatCaseStrings.toString()),
                "res");
        return result;
    }

    private static final String SELECT_BOOLEAN_CASE_ENCODING_START = " ( CASE WHEN ";
    private static final String SELECT_BOOLEAN_CASE_ENCODING_END = " THEN '"
            + TRUE_C + "' ELSE '" + FALSE_C + "' END ) ";

    private static void appendCase(StringBuilder sb, SQLBooleanValue value) {
        sb.append(SELECT_BOOLEAN_CASE_ENCODING_START);
        value.buildSQLQuery(sb);
        sb.append(SELECT_BOOLEAN_CASE_ENCODING_END);
    }

    private static Select translateMultiple(
            List<SQLBooleanValue> conditionsToTest) {
        Select result = new Select();
        int i = 0;
        for (SQLBooleanValue condition : conditionsToTest) {
            StringBuilder sb = new StringBuilder();
            sb.append("( CASE WHEN ");
            condition.buildSQLQuery(sb);
            sb.append(" THEN 1 ELSE 0 END )");
            result.addEntry(new SQLDelegatedValue(sb.toString()), "q" + i);
            i++;
        }
        return result;
    }

    @Override
    public void setNbTuplesVariables(int nbTuplesVariables) {
    	_nbTuplesVariables = nbTuplesVariables;
    }
    
    @Override
    public void setSizeNUplets(List<NamedFromQuantifiedExpression> quantifiers){
    	
    	for(NamedFromQuantifiedExpression quantifier: quantifiers){
    		_sizeNUplets.add(quantifier.getNamesNUplet().size());
    	}
    }

    
    @Override
    public void setFrom(From from) {
        _from = from;
    }

    @Override
    public void setWhere(Where where) {
        _where = where;
    }

    public void forceSingleAttribute(boolean single) {
        _singleForced = true;
        _singleAttribute = single;
    }

}
