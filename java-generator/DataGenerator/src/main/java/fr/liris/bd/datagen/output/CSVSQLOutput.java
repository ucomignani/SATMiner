package fr.liris.bd.datagen.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVSQLOutput extends AbstractSQLOutput {
    
    private Writer _outputW;
    private CSVWriter _csv;
    
    public CSVSQLOutput(Writer out) {
        _outputW = out;
        _csv = new CSVWriter(_outputW);
    }
    
    public CSVSQLOutput(OutputStream out) {
        this(new OutputStreamWriter(out));
    }
    
    @Override
    public void createTable() {
        String [] nextLine = new String[_specs.size()];
        for(int i = 0; i < _specs.size(); ++i) {
            nextLine[i] = _specs.get(i).getName();
        }
        _csv.writeNext(nextLine);
    }

    @Override
    public void execInsert(List<Object> data) {
        String [] nextLine = new String[data.size()];
        for(int i = 0; i < data.size(); ++i) {
            nextLine[i] = data.get(i).toString();
        }
        _csv.writeNext(nextLine);
    }

    @Override
    public void close() throws IOException {
        _csv.close();
        _outputW.close();
    }
    
}
