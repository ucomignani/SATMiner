/* ./SATMiner/src/main/java/dag/satmining/run/DataInputGenerator.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

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
