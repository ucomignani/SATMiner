/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

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
