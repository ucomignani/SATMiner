/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/sql/Select.java

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ecoquery
 */
public class Select implements Iterable<Select.Entry>, SQLRenderer {

    private List<Entry> _entries = new ArrayList<Entry>();
	private boolean _hasStar = false;

    public Select() {
    }

    public List<Entry> getEntries() {
        return _entries;
    }

    @Override
    public Iterator<Entry> iterator() {
        return _entries.iterator();
    }
    
    public void addEntry(SQLValue value, String name) {
        _entries.add(new Entry(value, name));
    }
    
    public void addStar() {
    	this._hasStar = true;
    }

    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append("SELECT ");
        boolean first = true;
        if (_hasStar) {
        	output.append("*");
        	first = false;
        }
        for(Entry e : _entries) {
            if (first) {
                first = false;
            } else {
                output.append(", ");
            }
            e.getValue().buildSQLQuery(output);
            if (e.getName() != null) {
                output.append(" as ");
                output.append(e.getName());
            }
        }
    }
    
    public class Entry {

        private final String _name;
        private final SQLValue _value;

        public Entry(SQLValue value, String name) {
            this._name = name;
            this._value = value;
        }

        public String getName() {
            return _name;
        }

        public SQLValue getValue() {
            return _value;
        }
    }
}
