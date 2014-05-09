/* src/main/java/fr/liris/bd/dag/linestochars/App.java

   Copyright (C) 2014 Emmanuel Coquery.

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

package fr.liris.bd.dag.linestochars;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private boolean _doStats = false;
	
	private char _next = 'A';
	
	private int _count = 0;
	
	private HashMap<String,Character> _map = new HashMap<String, Character>(); 
	
	private BufferedReader _input = new BufferedReader(new InputStreamReader(System.in));
	
	private PrintStream _output = System.out;
	
	private char getChar(String line) {
		if (_map.containsKey(line)) {
			return _map.get(line);
		} else {
			_map.put(line,_next);
			char c = _next;
			if (_next == Character.MAX_VALUE) {
				System.err.println("reached last char: too many different lines");
				System.exit(-1);
			} else {
				_next ++;
				_count ++;
			}
			return c;
		}
	}
	
	public void run() throws IOException {
		String line;
		do {
			line = _input.readLine();
			if (line != null) {
				char c = getChar(line);
				_output.append(c);
			}
		} while (line != null);
		if (_doStats) {
			System.err.println("# characters: "+_count);
		}
		_input.close();
		_output.close();
	}
	
	public void parseArgs(String [] args) {
        int i = 0;
        while (i < args.length) {
        	if ("--stats".equals(args[i])) {
        		_doStats = true;
        	} else if ("--startAt".equals(args[i])) {
        		if (i+1 < args.length) {
        			i++;
        			_next = args[i].charAt(0);
        		} else {
        			usage();
        		}
        	} else {
        		usage();
        	}
        	i++;
        }
	}
	
	private void usage() {
		System.err.println("Usage: java -jar LinesToChars-xxx.jar [--help] [--stats] [--startAt c] < origFile > newFile");
		System.err.println("  --stats: prints the number of diferent characters in newFile");
		System.err.println("  --startAt c: the first character to start with is c (defaults to 'A')");
		System.err.println("  --help: prints this help message and exits");
		System.exit(1);
	}
	
    public static void main( String[] args ) throws IOException
    {
    	App app = new App();
    	app.parseArgs(args);
    	app.run();
    }
}
