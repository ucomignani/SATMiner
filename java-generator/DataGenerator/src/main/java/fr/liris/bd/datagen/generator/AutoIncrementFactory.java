package fr.liris.bd.datagen.generator;

public class AutoIncrementFactory implements ValueGeneratorFactory {

    @Override
    public ValueGenerator getGenerator() {
        return new AutoIncrementGenerator();
    }

}
