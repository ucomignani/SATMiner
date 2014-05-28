/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/sql/From.java

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.satql.ast.sql;

import java.util.*;

/**
 *
 * @author ecoquery
 */
public class From implements Iterable<NamedFromExpression>, SQLRenderer {

    private List<NamedFromExpression> _queries = new ArrayList<NamedFromExpression>();
    private List<NamedFromQuantifiedExpression> _quantifiers = new ArrayList<NamedFromQuantifiedExpression>();
    private boolean _isMiningFrom;

    public From(boolean isMiningFrom) {
        this._isMiningFrom = isMiningFrom;
    }

    public void addName(String name) {
        addQueryName(null, name);
    }

    public void addQueryName(FromExpression origQuery, String name) {
    	FromExpression query = origQuery;
        if (query == null) {
            if (_isMiningFrom) {
                if (_queries.isEmpty()) {
                    throw new IllegalArgumentException("The first query cannot be null");
                } else {
                    query = _queries.get(_queries.size() - 1);
                }
            } else {
                query = new Relation(name);
            }
        }
        _queries.add(new NamedFromExpression(name, query));
    }
 
    public void addQuantifierName(QuantifierExpression origQuantifierQuery, FromExpression origFilterQuery, String name, boolean isFirstQuantifier) {
    	QuantifierExpression quantifierQuery = origQuantifierQuery;
    	FromExpression filterQuery = origFilterQuery;
        if (quantifierQuery == null) {
                if (_quantifiers.isEmpty()) {
                    throw new IllegalArgumentException("Need at least one tuple quantifier");
                } else {
                	quantifierQuery = _quantifiers.get(_quantifiers.size() - 1);
                }
            }
        _quantifiers.add(new NamedFromQuantifiedExpression(name, quantifierQuery, _queries, filterQuery, isFirstQuantifier));
    }
    
    public List<NamedFromExpression> getFromList() {
        return _queries;
    }

    @Override
    public Iterator<NamedFromExpression> iterator() {
        return _queries.iterator();
    }
     
    @Override
    public void buildSQLQuery(StringBuilder output) {
    	StringBuilder tupleFilter = new StringBuilder();
    	
        boolean first = true;
        for (NamedFromQuantifiedExpression expr : _quantifiers) {
            if (first) {
                output.append("FROM ");
                expr.buildSQLQuery(output, tupleFilter);
                
                first = false;
            } else {
                output.append(" LEFT OUTER JOIN ");
                expr.buildSQLQuery(output, tupleFilter);
            }
        }      
    }
}
