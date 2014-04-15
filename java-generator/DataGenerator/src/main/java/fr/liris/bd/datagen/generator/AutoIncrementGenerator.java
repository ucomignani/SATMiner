package fr.liris.bd.datagen.generator;

import java.sql.Types;

public class AutoIncrementGenerator implements ValueGenerator, ValueGeneratorFactory {

    private int _next = 0;
    
    @Override
    public Object getValue() {
        return _next++;
    }

    @Override
    public String getSQLType() {
        return "INTEGER";
    }

    @Override
    public int getJDBCSQLType() {
        return Types.INTEGER;
    }

    @Override
    public ValueGenerator getGenerator() {
        return this;
    }

}
