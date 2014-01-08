package dag.satmining.backend.minisat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;

import dag.satmining.backend.AbstractModelReader;
import dag.satmining.backend.FileModelReader;
import java.io.*;

public class MinisatModelReader extends AbstractModelReader implements  FileModelReader {

	private BufferedReader _lineReader;

    public MinisatModelReader() {}
    
	public MinisatModelReader(Reader input) {
		if (input instanceof BufferedReader) {
			_lineReader = (BufferedReader) input;
		} else {
			_lineReader = new BufferedReader(input);
		}
	}

	private String nextModelRepresentation() throws IOException {
		String line = _lineReader.readLine();
		while (line != null
				&& (line.length() == 0 || (line.charAt(0) < '0' || line
						.charAt(0) > '9'))) {
			line = _lineReader.readLine();
		}
		return line;
	}

	private static BitSet parseModel(String representation) {
		BitSet model = new BitSet();
		int i = 0;
		int size = representation.length();
		while (i < size) {
			while (i < size && representation.charAt(i) == ' ') {
				++i;
			}
			int begin = i;
			while (i < size && representation.charAt(i) != ' ') {
				++i;
			}
			if (i != begin) {
				model.set(Integer.parseInt(representation.substring(begin, i)));
			}
		}
		return model;
	}

	@Override
	protected void parseModel() {
		try {
			String representation = nextModelRepresentation();
			if (representation == null) {
				setModel(null);
				_lineReader.close();
			} else {
				setModel(parseModel(representation));
			}
		} catch (IOException e) {
			setModel(null);
		}
	}

    public void open(File file) throws IOException {
        _lineReader = new BufferedReader(new FileReader(file));
    }
    
    public void close() throws IOException {
        if (_lineReader != null) {
            _lineReader.close();
        }
    }

}
