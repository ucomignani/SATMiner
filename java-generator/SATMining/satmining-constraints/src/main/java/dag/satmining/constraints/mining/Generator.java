package dag.satmining.constraints.mining;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.output.PatternConverter;

/**
 * A mining problem generator.
 * @author ecoquery
 *
 */
public abstract class Generator<L extends Literal<L>> {

	/**
	 * The constraints to add to the mining problem.
	 */
	private Collection<Constraint<L>> _constraints;
	
	/**
	 * Constructor initializing the constraint list.
	 */
	public Generator() {
		_constraints = new ArrayList<Constraint<L>>();
	}
	
	/**
	 * Adds a constraint to the problem.
	 * @param constraint the constraint to add.
	 */
	public final void addConstraint(Constraint<L> constraint) {
		_constraints.add(constraint);
	}

	/**
	 * Add all the constraints that where added to the problem.
	 * @param sat
	 * @throws NoSolutionException
	 */
	public final void buildModel(PBBuilder<L> sat) throws NoSolutionException {
		for(Constraint<L> constraint : _constraints) {
			constraint.addClauses(sat);
		}
	}
    
	/**
	 * An object that is able to convert a boolean solution to a concrete pattern.
	 * @return
	 */
    public abstract PatternConverter getPatternConverter();
    
    /**
     * Configure this generator using command line arguments.
     * @param inputData
     * @param opts
     * @throws IOException
     * @throws UsageException
     */
    public abstract void configure(Reader inputData, CommandLine opts) throws IOException, UsageException;

    /**
     * The generator's title.
     * @return
     */
    public abstract String getTitle();

    /**
     * The generator's command line options.
     * @return
     */
    public abstract Options getOptions();
    
}
