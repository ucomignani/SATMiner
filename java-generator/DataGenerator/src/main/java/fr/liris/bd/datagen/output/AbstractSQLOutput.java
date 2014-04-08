package fr.liris.bd.datagen.output;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSQLOutput implements SQLOutput {

    protected String _table; 
    protected List<ColumnSpec> _specs;
    
    @Override
    public void setTableName(String name) {
        _table = name;
    }

    @Override
    public void setAttSpec(List<ColumnSpec> specs) {
        _specs = specs;
    }

    @Override
    public void execInsert() {
        List<Object> data = new ArrayList<Object>(_specs.size());
        for(ColumnSpec s : _specs) {
            data.add(s.getValue());
        }
        execInsert(data);
    }

}
