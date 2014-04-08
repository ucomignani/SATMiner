package fr.liris.bd.datagen.generator;

public interface ValueGenerator {

    Object getValue();
    String getSQLType();
    int getJDBCSQLType();

}
