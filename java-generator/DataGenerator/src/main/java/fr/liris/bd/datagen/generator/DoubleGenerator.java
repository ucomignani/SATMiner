package fr.liris.bd.datagen.generator;

import java.sql.Types;
import java.util.Random;

public class DoubleGenerator implements ValueGenerator, ValueGeneratorFactory {

    private Random _random = new Random();
    
    @Override
    public Object getValue() {
        return _random.nextDouble();
    }

    @Override
    public String getSQLType() {
        return "DOUBLE PRECISION";
    }

    @Override
    public int getJDBCSQLType() {
        return Types.DOUBLE;
    }

    @Override
    public ValueGenerator getGenerator() {
        return this;
    }
    
}
