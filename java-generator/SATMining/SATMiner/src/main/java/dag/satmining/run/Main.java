/* ./SATMiner/src/main/java/dag/satmining/run/Main.java

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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.ExternalSolverModelReader;
import dag.satmining.backend.ModelReader;
import dag.satmining.backend.boolvarpb.BVLiteral;
import dag.satmining.backend.boolvarpb.BVPBBuilder;
import dag.satmining.backend.boolvarpb.BVPBOption;
import dag.satmining.backend.boolvarpb.BVPBWrapper;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.dimacs.FileDimacsBackend;
import dag.satmining.backend.minisat.MinisatModelReader;
import dag.satmining.backend.pb.gen.CardNetworksPBFactory;
import dag.satmining.backend.pb.gen.NaivePBFactory;
import dag.satmining.backend.sat4j.SAT4JPBBuilder;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.Limitable;
import dag.satmining.output.Limiter;
import dag.satmining.output.ModelSolutionWriter;
import dag.satmining.output.SolutionWriter;
import dag.satmining.problem.fim.FIMiningGenerator;
import dag.satmining.problem.satql.SatQL;
import dag.satmining.problem.seq.GSProblem;
import dag.satmining.problem.seq.SequenceMiningGenerator1;
import dag.satmining.utils.Timer;

public class Main<L extends Literal<L>> implements Runnable {
    
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	// SAT/PB Backend
	private static final String MINISAT_OPT = "minisat";
	private static final String SAT4J_OPT = "sat4j";
	private static final String CNF_OPT = "cnf";
	private static final String MAX_MODELS_SWITCH = "-max-models";

	// PB Coding
	private static final String BVPB_CODING_OPT = "bvpbCoding";
	private static final String CARDNETWORKS_OPT = "cardnet";
	private static final String NAIVE_OPT = "naive";

	// Problem
	private static final String SATQL_OPT = "satql";
	private static final String GS_OPT = "gs";
	private static final String FIM_OPT = "fim";
	private static final String FSM_OPT = "fsm";

	// Input / output
	private static final String GENSEQ_OPT = "genseq";
	private static final String INPUT_OPT = "i";
	private static final String OUTPUT_OPT = "o";

	// Misc
	private static final String DEBUG_OPT = "debug";
	private static final String LIMIT_OPT = "limit";

	// Internal variables
	private PBBuilder<L> _builder;
	private Generator<L> _generator;
	private CommandLine _cmd;
	private Class<L> _litClazz;
	private Reader _input;
	private String _outputFile = "-";
	private ModelReader _modelReader;
	private SolutionWriter _solutionWriter;
	private PrintStream _err = System.err;
	private int _exitCode = 0;
	boolean _debug = false;
	private long _limit = -1;

	/**
	 * Builds a PB-SAT miner using the provided {@link PBBuilder} as core
	 * backend.
	 * 
	 * @param internalBuilder
	 */
	public Main(Class<L> litClazz, PBBuilder<L> internalBuilder,
			ModelReader modelReader) {
		this._builder = internalBuilder;
		this._litClazz = litClazz;
		this._modelReader = modelReader;
	}

	private Options chooseProblem(String[] args) throws UsageException,
			IOException {
		Options opts = buildOptions();
		List<String> argsL = Arrays.asList(args);
		if (argsL.contains("-" + FIM_OPT)) {
			_generator = new FIMiningGenerator<L>();
		} else if (argsL.contains("-" + GS_OPT)) {
			int idx = argsL.indexOf("-" + GS_OPT);
			if (idx + 1 >= argsL.size()) {
				throw new UsageException("-" + GS_OPT + " requires an alphabet");
			} else {
				String alphaS = argsL.get(idx + 1);
				try {
					GSProblem.Predefined alpha = GSProblem.Predefined
							.valueOf(alphaS);
					_generator = GSProblem.newProblem(_litClazz, alpha);
				} catch (IllegalArgumentException e) {
					throw new UsageException(
							"alphabet should be one of "
									+ Arrays.deepToString(GSProblem.Predefined
											.values()), e);
				}
			}
		} else if (argsL.contains("-" + SATQL_OPT)) {
			_generator = new SatQL<L>(_litClazz);
		} else { // FSM_OPT by default
			_generator = new SequenceMiningGenerator1<L>();
		}
		for (Object o : _generator.getOptions().getOptions()) {
			opts.addOption((Option) o);
		}
		return opts;
	}

	private void handleInputOutput() throws IOException {
		if (_cmd.hasOption(GENSEQ_OPT)) {
			if (_input != null) {
				_input.close();
			}
			String[] values = _cmd.getOptionValues(GENSEQ_OPT);
			int size = Integer.parseInt(values[0]);
			int nbChars = Integer.parseInt(values[1]);
			_input = new DataInputGenerator(size, nbChars, true);
		}
		if (_cmd.hasOption(OUTPUT_OPT)) {
			_outputFile = _cmd.getOptionValue(OUTPUT_OPT);
		}
		if (_cmd.hasOption(INPUT_OPT)) {
			if (_input != null) {
				_input.close();
			}
			String inFile = _cmd.getOptionValue(INPUT_OPT);
			if ("-".equals(inFile)) {
				_input = new InputStreamReader(System.in);
			} else {
				_input = new FileReader(inFile);
			}
		}
	}

	private void selectCoding() {
		if (_cmd.hasOption(CARDNETWORKS_OPT)) {
			_builder = new CardNetworksPBFactory<L>(_builder);
		} else if (_cmd.hasOption(NAIVE_OPT)) {
			_builder = new NaivePBFactory<L>(_builder);
		} // do nothing for native
	}

	private void selectOutputMode() {
		if (_modelReader == null) {
			_solutionWriter = _builder.getCNFWriter();
		} else {
			_solutionWriter = new ModelSolutionWriter(_modelReader);
		}
	}

	private void handleDebug() {
		if (_cmd.hasOption(DEBUG_OPT)) {
			_debug = true;
		}
	}

	public void parseArgs(String[] args) throws UsageException, IOException {
		Options opts = chooseProblem(args);
		try {
			_cmd = new BasicParser().parse(opts, args);
			LOG.info("Parsed cmd line");
		} catch (ParseException ex) {
			LOG.warn("Error while parsing command line: {}",
					ex.getLocalizedMessage());
			throw new UsageException(ex);
		}
		handleDebug();
		handleInputOutput();
		selectCoding();
		selectOutputMode();
		setLimit();
	}

	private void setLimit() throws UsageException {
		if (_cmd.hasOption(LIMIT_OPT)) {
			try {
				_limit = Long.parseLong(_cmd.getOptionValue(LIMIT_OPT));
			} catch (NumberFormatException e) {
				throw new UsageException("Number expected form the limit switch, but found "+_cmd.getOptionValue(LIMIT_OPT));
			}
		}
	}

	@SuppressWarnings("static-access")
	public static Options buildOptions() {
		Options opts = new Options();
		opts.addOption(OptionBuilder.withArgName("size nbchars").hasArgs(2)
				.withDescription("generate a sequence for basic tests")
				.create(GENSEQ_OPT));
		opts.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("output results to file").create(OUTPUT_OPT));
		opts.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("reads data from file (- for stdin)")
				.create(INPUT_OPT));
		opts.addOption(OptionBuilder.withDescription(
				"uses SAT4J as mining backend").create(SAT4J_OPT));
		opts.addOption(OptionBuilder
				.withArgName("minisat_exe")
				.hasArg()
				.withDescription(
						"use minisat as backend, minisat_exe being minisat's executable")
				.create(MINISAT_OPT));
		opts.addOption(OptionBuilder.withDescription("only generate CNF")
				.create(CNF_OPT));
		opts.addOption(OptionBuilder.withDescription(
				"Replace backend encoding with naive encoding").create(
				NAIVE_OPT));
		opts.addOption(OptionBuilder.withDescription(
				"Replace backend encoding with cardinality networks encoding")
				.create(CARDNETWORKS_OPT));
		opts.addOption(OptionBuilder.withDescription(
				"print exception stack in case of an error").create(DEBUG_OPT));
		opts.addOption(OptionBuilder
				.withArgName("coding")
				.hasArg()
				.withDescription(
						"use BoolvarPB with sum encoding: "
								+ Arrays.deepToString(BVPBOption.values())
								+ " (defaults to " + BVPBOption.getVariant()
								+ ")").create(BVPB_CODING_OPT));
		opts.addOption(OptionBuilder.withDescription("mine frequent itemsets")
				.create(FIM_OPT));
		opts.addOption(OptionBuilder.withDescription(
				"mine frequent sequences (default)").create(FSM_OPT));
		opts.addOption(OptionBuilder
				.withArgName("alphabet")
				.hasArg()
				.withDescription(
						"mine frequent sequences over alphabet <alphabet>. <alphabet> can be "
								+ Arrays.deepToString(GSProblem.Predefined
										.values())).create(GS_OPT));
		opts.addOption(OptionBuilder.withDescription("execute SATQL query")
				.create(SATQL_OPT));
		opts.addOption(OptionBuilder.withArgName("n").hasArg().withDescription("limit the results to <n> models").create(LIMIT_OPT));
		return opts;
	}

	@Override
	public void run() {
		try {
			LOG.info("Setup problem ...");
			_solutionWriter.setOutput(_outputFile);
			_generator.configure(_input, _cmd);
			LOG.info("Converting model ...");
			_generator.buildModel(_builder);
			if (_limit == -1 && _generator instanceof Limiter && ((Limiter)_generator).getLimit() != -1) {
					_limit = ((Limiter)_generator).getLimit();
			}
			if (_limit != -1) {
				if(_solutionWriter instanceof Limitable) {
					((Limitable)_solutionWriter).setLimit(_limit);
				} else {
					throw new IllegalStateException("Limit required but not supported by backend"); 
				}
			}
			_builder.endProblem();
			LOG.info("Computing solution ...");
			_solutionWriter.writeSolution(_generator.getPatternConverter());
			LOG.info("finished");
		} catch (NoSolutionException e) {
			LOG.warn("No solution found", e);
			_exitCode = 2;
			if (_debug) {
				e.printStackTrace(System.err);
			}
		} catch (IOException e) {
			LOG.error("IO error", e);
			_exitCode = 3;
			if (_debug) {
				e.printStackTrace(System.err);
			}
		} catch (UsageException e) {
			LOG.error(e.getLocalizedMessage());
			usage();
			_exitCode = 1;
			if (_debug) {
				e.printStackTrace(System.err);
			}
		}
	}

	private static void usage(Generator<?> generator, OutputStream err) {
		PrintWriter pw = new PrintWriter(err);
		HelpFormatter help = new HelpFormatter();
		help.printHelp(pw, 80, "java -jar SATMiner-xxxxx.jar ", "",
				buildOptions(), 1, 0, "");
		pw.flush();
		if (generator != null) {
			help = new HelpFormatter();
			help.printHelp(pw, 80, "Options for " + generator.getTitle(), "",
					generator.getOptions(), 1, 0, "");
			pw.flush();
		}
	}
	
	private void usage() {
		usage(_generator,_err);
	}

	public int getExitCode() {
		return _exitCode;
	}

	private static String getOptVal(List<String> args, String opt) {
		int pos = args.indexOf("-" + opt);
		if (pos == -1) {
			return null;
		} else if (args.size() <= pos + 1) {
			usage(null,System.err);
			System.exit(1);
			return null; // for the compiler
		} else {
			return args.get(pos + 1);
		}
	}

	private static <L extends Literal<L>> PBBuilder<L> defaultEncoding(
			ClauseBuilder<L> builder) {
		return new CardNetworksPBFactory<L>(builder);
	}

	public static Main<?> buildMain(String[] args) throws IOException {
		Main<?> runner;
		List<String> argsL = Arrays.asList(args);
		if (argsL.contains("-" + BVPB_CODING_OPT)) {
			try {
				String codingOpt = getOptVal(argsL, BVPB_CODING_OPT);
				BVPBOption.setVariant(BVPBOption.valueOf(codingOpt));
			} catch (IllegalArgumentException e) {
				System.err.println(e.getLocalizedMessage());
				usage(null,System.err);
				System.exit(1);
			}
			if (argsL.contains("-" + MINISAT_OPT)) {
				String solverCmd = getOptVal(argsL, MINISAT_OPT);
				File cnffile = File.createTempFile("satminer_model_", ".cnf");
				LOG.info("Writing dimacs to {}", cnffile);
				BVPBBuilder builder = new BVPBBuilder();
				builder.setOutput(cnffile.getAbsolutePath());
				ExternalSolverModelReader reader = new ExternalSolverModelReader(
						new MinisatModelReader(), cnffile, solverCmd, "#in",
						"-o", "#out");
				reader.setLimitSwitch(MAX_MODELS_SWITCH);
				runner = new Main<BVLiteral>(BVLiteral.class, builder, reader);
			} else if (argsL.contains("-" + SAT4J_OPT)) {
				SAT4JPBBuilder sat4jbuilder = new SAT4JPBBuilder(
						SAT4JPBBuilder.LARGE);
				BVPBWrapper<DimacsLiteral> wrapper = new BVPBWrapper<DimacsLiteral>(
						sat4jbuilder);
				runner = new Main<BVLiteral>(BVLiteral.class, wrapper,
						sat4jbuilder);
			} else { // defaults to writing the CNF
				BVPBBuilder builder = new BVPBBuilder();
				runner = new Main<BVLiteral>(BVLiteral.class, builder, null);
			}
		} else if (argsL.contains("-" + MINISAT_OPT)) {
			String solverCmd = getOptVal(argsL, MINISAT_OPT);
			File cnffile = File.createTempFile("satminer_model_", ".cnf");
			LOG.info("Writing dimacs to {}", cnffile);
			FileDimacsBackend backend = new FileDimacsBackend(cnffile);
			ExternalSolverModelReader reader = new ExternalSolverModelReader(
					new MinisatModelReader(), cnffile, solverCmd, "#in", "-o",
					"#out");
			reader.setLimitSwitch(MAX_MODELS_SWITCH);
			runner = new Main<DimacsLiteral>(DimacsLiteral.class,
					defaultEncoding(backend), reader);
		} else if (argsL.contains("-" + SAT4J_OPT)) {
			SAT4JPBBuilder sat4jbuilder = new SAT4JPBBuilder(
					SAT4JPBBuilder.LARGE);
			runner = new Main<DimacsLiteral>(DimacsLiteral.class, sat4jbuilder,
					sat4jbuilder);
		} else { // default: just output CNF
			FileDimacsBackend fdb = new FileDimacsBackend();
			runner = new Main<DimacsLiteral>(DimacsLiteral.class,
					defaultEncoding(fdb), null);
		}
		return runner;
	}

	public static void main(String[] args) throws IOException {
		Main<?> runner = null;
		try {
		    Timer timer = Timer.start("total");
			runner = buildMain(args);
			runner.parseArgs(args);
			runner.run();
			timer.stopAndPrint();
			System.exit(runner.getExitCode());
		} catch (UsageException e) {
			usage(runner == null ? null : runner._generator,System.err);
			System.exit(1);
		}
	}
}
