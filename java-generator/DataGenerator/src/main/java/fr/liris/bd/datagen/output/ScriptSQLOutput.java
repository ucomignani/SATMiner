package fr.liris.bd.datagen.output;

import java.io.PrintStream;
import java.util.List;

public class ScriptSQLOutput extends AbstractSQLOutput {


    private PrintStream _output;
    
    public ScriptSQLOutput(PrintStream output) {
        this._output = output;
    }

    @Override
    public void createTable() {
        _output.print("CREATE TABLE ");
        _output.print(_table);
        _output.print("(");
        boolean started = false;
        for(ColumnSpec s : _specs) {
            if (started) {
                _output.print(", ");
            } else {
                started = true;
            }
            _output.print(s.sqlColumnDescription());
        }
        _output.println(");");
        _output.flush();
    }
    
    public void execInsert(List<Object> data) {
        _output.print("INSERT INTO ");
        _output.print(_table);
        _output.print(" VALUES(");
        boolean started = false;
        for(Object o : data) {
            if (started) {
                _output.print(", ");
            } else {
                started = true;
            }
            _output.print(o.toString());
        }
        _output.println(");");        
    }
    
    @Override
    public void close() {
        _output.close();
    }

}
