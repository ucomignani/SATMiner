package dag.satmining.run;

import java.io.IOException;
import java.io.Reader;

/**
 * A data generator for testing purposes.
 * @author ecoquery
 *
 */
public class DataInputGenerator extends Reader {

	private char[] _chars;
	
	private int _size;
	
	private int _current;
	
	private boolean _printToStdErr;
	
	/**
	 * Initializes the generator
	 * @param size the size of the sequence to generate
	 * @param nbChars the number of different characters to use
	 * @param print if true, prints the generated sequence to stderr
	 */
	public DataInputGenerator(int size, int nbChars, boolean print) {
		_size = size;
		_current = 0;
		_printToStdErr = print;
		_chars = new char[nbChars];
		for(int i = 0; i < nbChars; i++) {
			_chars[i] = (char) (i + 'a');
		}
	}
	
	/**
	 * Whether the sequence is not finished.
	 * @return false if the sequence is fully generated
	 */
	protected boolean hasNext() {
		return _current < _size; 
	}
	
	/**
	 * Generates the next character in the sequence.
	 * Outputs the character to stderr in configured.
	 * @return the generated character.
	 */
	protected char next() {
		char nxt = _chars[((++_current)+_chars.length-1) % _chars.length];
		if (_printToStdErr) {
			System.err.print(nxt);
		}
		return nxt;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int i = 0;
		while (i < len && hasNext()) {
				cbuf[off+i] = next();
				i++;
		}
		return i;
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		_current = _size;
		if (_printToStdErr) {
			System.err.println();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Reader#read()
	 */
	@Override
	public int read() {
		if (hasNext()) {
			return next();
		} else {
			if (_printToStdErr) {
				System.err.flush();
			}
			return -1;
		}
	}

}
