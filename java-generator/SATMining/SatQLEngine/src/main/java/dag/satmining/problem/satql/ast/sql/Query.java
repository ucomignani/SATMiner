/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/sql/Query.java

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

/**
 * 
 * @author ecoquery
 */
public class Query implements FromExpression {

    private Select _select;
    private From _from;
    private Where _where;

    public Query(Select select, From from, Where where) {
        this._select = select;
        this._from = from;
        this._where = where;
    }

    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append("(");
        buildSQL(output);
        output.append(")");
    }

    private void buildSQL(StringBuilder output) {
        _select.buildSQLQuery(output);
        output.append(" ");
        _from.buildSelectQuantifierSQLQuery(output); //tuple row numbers and last quantifier filter
        output.append(" ");
        _from.buildSQLQuery(output);
        output.append(" ");
        _where.buildSQLQuery(output);
    }

    public String asSQL() {
        StringBuilder sb = new StringBuilder();
        buildSQL(sb);
        return sb.toString();
    }

    @Override
    public void buildSQLQueryNoName(StringBuilder output) {
        buildSQLQuery(output);
    }

}
