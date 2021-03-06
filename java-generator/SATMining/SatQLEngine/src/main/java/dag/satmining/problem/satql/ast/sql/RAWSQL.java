/* SatQLEngine/src/main/java/dag/satmining/problem/satql/ast/sql/RAWSQL.java

   Copyright (C) 2014 Emmanuel Coquery.

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

public abstract class RAWSQL implements SQLRenderer {

    private String _expression;

    public RAWSQL(String parsed, boolean fromParsing) {
        if (fromParsing) {
            parsed = parsed.trim();
            // remove first ( '{' ) and last ( '}' ) characters
            System.out.println("remove {}");
            parsed = parsed.substring(1, parsed.length() - 1);
            parsed = parsed.replace("{{", "{").replace("}}", "}");
        }
        _expression = parsed;
    }
    
    public RAWSQL(String expression) {
        this(expression,false);
    }

    public String getExpression() {
        return _expression;
    }

    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append('(');
        output.append(getExpression());
        output.append(')');
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_expression == null) ? 0 : _expression.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RAWSQL other = (RAWSQL) obj;
        if (_expression == null) {
            if (other._expression != null)
                return false;
        } else if (!_expression.equals(other._expression))
            return false;
        return true;
    }

}
