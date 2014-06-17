/* ./satmining-constraints/src/main/java/dag/satmining/constraints/mining/Generator.java

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
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
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
	public final void buildModel(ReifiedWeightedPBBuilder<L> sat) throws NoSolutionException {
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
