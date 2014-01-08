/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.ExternalSolverModelReader;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.dimacs.FileDimacsBackend;
import dag.satmining.backend.minisat.MinisatModelReader;
import dag.satmining.backend.pb.gen.CardNetworksPBFactory;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.FileModelOutput;
import dag.satmining.output.GeneratorOutput;

/**
 * 
 * @author ecoquery
 */
public class CompareSeqImplTest extends TestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(CompareSeqImplTest.class);

	private Set<String> runOnInput(String data, int maxSize, int minFreq,
			Generator<DimacsLiteral> gen) throws IOException,
			NoSolutionException, ParseException, UsageException {
		String[] args = { "-m", String.valueOf(maxSize), "-f",
				String.valueOf(minFreq) };
		gen.configure(new StringReader(data),
				new BasicParser().parse(gen.getOptions(), args));
		File cnfFile = File.createTempFile("satminer_junit_", ".cnf");
		cnfFile.deleteOnExit();
		LOG.info("Output CNF to {}", cnfFile);
		PBBuilder<DimacsLiteral> handler = new CardNetworksPBFactory<DimacsLiteral>(
				new FileDimacsBackend(cnfFile));
		File rawOutput = File.createTempFile("satminer_junit_seq_", ".txt");
		LOG.info("Output patterns to {}", rawOutput);
		GeneratorOutput output = new FileModelOutput(gen.getPatternConverter(),
				new ExternalSolverModelReader(new MinisatModelReader(),
						cnfFile, "./minisat.sh", "#in", "-o", "#out"),
				rawOutput);
		gen.buildModel(handler);
		handler.endProblem();
		output.writeOutput();
		Set<String> lines = new HashSet<String>();
		BufferedReader rawInput = new BufferedReader(new FileReader(rawOutput));
		String line = rawInput.readLine();
		while (line != null) {
			lines.add(line);
			line = rawInput.readLine();
		}
		rawInput.close();
		return lines;
	}

	private void checkSameOutput(String data, int maxSize, int minFreq)
			throws IOException, NoSolutionException, ParseException,
			UsageException {
		Generator<DimacsLiteral> gen1 = new SequenceMiningGenerator1<DimacsLiteral>();
		Set<String> res1 = runOnInput(data, maxSize, minFreq, gen1);
		Generator<DimacsLiteral> gen2 = GSProblem.newProblem(
				DimacsLiteral.class, GSProblem.Predefined.Char);
		Set<String> res2 = runOnInput(data, maxSize, minFreq, gen2);
		LOG.debug("res1: {} results",res1.size());
		LOG.debug("res2: {} results",res2.size());
		assertTrue(res1.containsAll(res2));
		assertTrue(res2.containsAll(res1));
	}

	public void testSeq1() throws IOException, NoSolutionException,
			ParseException, UsageException {
		checkSameOutput("abcabcabc", 5, 3);
	}

	public void testSeqEmpty() throws IOException, NoSolutionException,
			ParseException, UsageException {
		checkSameOutput("abcdefg", 7, 2);
	}

	public void testSeqFull() throws IOException, NoSolutionException,
			ParseException, UsageException {
		checkSameOutput("abcdefg", 7, 1);
	}
}
