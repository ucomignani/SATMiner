package dag.satmining.backend.dimacs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import boolvar.output.CnfProblem;
import dag.satmining.NoSolutionException;
import dag.satmining.output.PatternConverter;
import dag.satmining.output.SolutionWriter;

/**
 * Write dimacs output to a file. Similar to {@link CnfProblem} for file based
 * output
 * 
 * @author ecoquery
 * 
 */
public class FileDimacsBackend extends DimacsBackend implements SolutionWriter {

	private static final Logger LOG = LoggerFactory.getLogger(FileDimacsBackend.class);

	private File _file;
	private File _temp;
	private PrintWriter _tmpOut;
	private long _nbClauses;

	/**
	 * This constructor does not initialize the final file to write to. A call
	 * to {@link #setOutputFile(File)} is required before {@link #endProblem()}
	 * is called.
	 * 
	 * @throws IOException
	 */
	public FileDimacsBackend() throws IOException {
		this(null);
	}

	public FileDimacsBackend(File file) throws IOException {
		_file = file;
		_temp = File.createTempFile("dimacs_intermediate_", null);
		_tmpOut = new PrintWriter(_temp);
		_nbClauses = 1; // for the backdoor clause
	}

	@Override
	public void addClause(DimacsLiteral[] l) throws NoSolutionException {
		for (int i = 0; i < l.length; i++) {
			_tmpOut.print(l[i].intRepr());
			_tmpOut.print(' ');
		}
		_tmpOut.println("0");
		LOG.debug("wrote {}",Arrays.deepToString(l));
		_nbClauses++;
	}

	@Override
	public void endProblem() throws NoSolutionException {
		try {
			_tmpOut.close();
			_tmpOut = null;
			PrintWriter pw = new PrintWriter(_file);
			pw.print("p cnf ");
			pw.print(getNbVariables());
			pw.print(' ');
			pw.println(_nbClauses);
			if (_strongBackdoorClause.size() > 0) {
				pw.print("b ");
				for (int v : _strongBackdoorClause) {
					pw.print(v);
					pw.print(' ');
				}
				pw.println("0");
			}
			BufferedReader tmpIn = new BufferedReader(new FileReader(_temp));
			String line;
			while ((line = tmpIn.readLine()) != null) {
				pw.println(line);
			}
			tmpIn.close();
			_temp.delete();
			_temp = null;
			pw.close();
		} catch (IOException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public SolutionWriter getCNFWriter() {
		return this;
	}

	@Override
	public void setOutput(String outputFile) throws IOException {
		if ("-".equals(outputFile)) {
			throw new IllegalArgumentException("Pure dimacs backend does not write to stdout");
		}
		this._file = new File(outputFile);
	}

	@Override
	public void writeSolution(PatternConverter converter) throws IOException {
	}
}
