package fr.liris.bd.datagen.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultipleSQLOutput implements SQLOutput {

    private Collection<SQLOutput> _subs = new ArrayList<SQLOutput>();
    private List<ColumnSpec> _spec;
    
    public void addOutput(SQLOutput o) {
        _subs.add(o);
    }

    public boolean hasOutput() {
        return _subs.size() > 0;
    }
    
    @Override
    public void close() throws Exception {
        for(SQLOutput o : _subs) {
            o.close();
        }
    }

    @Override
    public void setTableName(String name) {
        for(SQLOutput o : _subs) {
            o.setTableName(name);
        }
    }

    @Override
    public void setAttSpec(List<ColumnSpec> specs) {
        _spec = specs;
        for(SQLOutput o : _subs) {
            o.setAttSpec(specs);
        }
    }

    @Override
    public void createTable() {
        for(SQLOutput o : _subs) {
            o.createTable();
        }
    }

    @Override
    public void execInsert() {
        List<Object> data = new ArrayList<Object>(_spec.size());
        for(ColumnSpec s : _spec) {
            data.add(s.getValue());
        }
        execInsert(data);
    }

    @Override
    public void execInsert(List<Object> data) {
        for(SQLOutput o : _subs) {
            o.execInsert(data);
        }
    }

}
