package fr.liris.bd.datagen.output;

import fr.liris.bd.datagen.generator.ValueGenerator;

public class ColumnSpec {

    private String _name;
    private ValueGenerator _valueGenerator;

    public ColumnSpec(String name, ValueGenerator generator) {
        _name = name;
        _valueGenerator = generator;
    }

    public String sqlColumnDescription() {
        return _name+" "+_valueGenerator.getSQLType();
    }
    
    public Object getValue() {
        return _valueGenerator.getValue();
    }
    
    public String getName() {
        return _name;
    }
}
