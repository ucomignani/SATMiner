package fr.liris.bd.datagen.output;

import java.util.List;

public interface SQLOutput extends AutoCloseable {

    void setTableName(String name);
    void setAttSpec(List<ColumnSpec> specs);
    void createTable();
    void execInsert();
    void execInsert(List<Object> data);
    
}
