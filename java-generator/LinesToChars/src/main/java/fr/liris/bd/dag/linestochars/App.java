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
