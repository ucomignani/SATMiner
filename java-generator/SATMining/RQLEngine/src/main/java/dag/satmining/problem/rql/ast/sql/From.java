/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.util.*;

/**
 *
 * @author ecoquery
 */
public class From implements Iterable<NamedFromExpression>, SQLRenderer {

    private List<NamedFromExpression> _queries = new ArrayList<NamedFromExpression>();
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

    public List<NamedFromExpression> getFromList() {
        return _queries;
    }

    @Override
    public Iterator<NamedFromExpression> iterator() {
        return _queries.iterator();
    }

    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append("FROM ");
        boolean first = true;
        for (NamedFromExpression expr : _queries) {
            if (first) {
                first = false;
            } else {
                output.append(", ");
            }
            expr.buildSQLQuery(output);
        }
    }
}
