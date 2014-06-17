/* ./satmining-backend/src/main/java/dag/satmining/backend/boolvarpb/BVPBBuilder.java

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

package dag.satmining.backend.boolvarpb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import boolvar.model.Literal;
import boolvar.model.Model;
import boolvar.model.Variable;
import boolvar.model.constraints.Constraint;
import boolvar.model.constraints.PBconstraint;
import boolvar.output.CNFformula;
import boolvar.output.Clause;
import boolvar.output.CnfProblem;
import boolvar.output.PBformula;
import boolvar.utility.QuotaException;
import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.constraints.WeightedPBBuilder;
import dag.satmining.constraints.impl.AbstractClauseBuilder;
import dag.satmining.constraints.impl.OneTrue;
import dag.satmining.constraints.impl.PBReifier;
import dag.satmining.output.PatternConverter;
import dag.satmining.output.SolutionWriter;

public class BVPBBuilder extends AbstractClauseBuilder<BVLiteral> implements
		PBBuilder<BVLiteral>, WeightedPBBuilder<BVLiteral>, ReifiedWeightedPBBuilder<BVLiteral>, Constraint,
		SolutionWriter {

	private BitSet _strongBackdoor;
	private CNFformula _cnf;
	private PBReifier<BVLiteral> _pbReifier;
	private OneTrue<BVLiteral> _exactlyOneTrue;
	private List<Variable> _strongVars;
	private List<Variable> _vars;
	private File _dimacsFile;

	public BVPBBuilder() {
		_strongBackdoor = new BitSet();
		_cnf = new CNFformula();
		_pbReifier = new PBReifier<BVLiteral>(this);
		_exactlyOneTrue = new OneTrue<BVLiteral>(this);
		_strongVars = new ArrayList<Variable>();
		_vars = new ArrayList<Variable>();
		_vars.add(null);
	}

	@Override
	public BVLiteral newLiteral() {
		return newLiteral(true, false);
	}

	@Override
	public BVLiteral newStrongLiteral() {
		return newLiteral(true, true);
	}

	@Override
	public BVLiteral newLiteral(boolean positive, boolean strong) {
		Variable v = new Variable();
		_vars.add(v);
		BVLiteral l = new BVLiteral(v.getPosLit());
		if (strong) {
			addToStrongBackdoor(l);
		}
		return positive ? l : l.getOpposite();
	}

	@Override
	public void addClause(BVLiteral[] lits) throws NoSolutionException {
		try {
			Clause c = new Clause();
			for (BVLiteral lit : lits) {
				c.addLiteral(lit.getLitImpl());
			}
			_cnf.addClause(c);
		} catch (QuotaException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addToStrongBackdoor(BVLiteral l) {
		if (!_strongBackdoor.get(l.getVariableId())) {
			_strongBackdoor.set(l.getVariableId());
			_strongVars.add(l.getLitImpl().getVariable());
		}
	}

	@Override
	public void endProblem() throws NoSolutionException {
		try {
			// Add strong backdoor clause to the cnf...
			StrongClause sc = new StrongClause();
			for (Variable v : getStrongBackdoor()) {
				sc.addLiteral(v.getPosLit());
			}
			_cnf.addClause(sc);
			if (_dimacsFile != null) {
				writeDimacsToFile(_dimacsFile);
			}
		} catch (QuotaException e) {
			throw new NoSolutionException(e);
		} catch (IOException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addPBInequality(BVLiteral[] lits, Ineq ineq, int value)
			throws NoSolutionException {
		int[] coefs = new int[lits.length];
		for (int i = 0; i < lits.length; i++) {
			coefs[i] = 1;
		}
		addWPBInequality(lits, coefs, ineq, value);
	}

	@Override
	public void addWPBInequality(BVLiteral[] lits, int[] coefs, Ineq ineq,
			int value) throws NoSolutionException {
		try {
			if (ineq == Ineq.EQ || ineq == Ineq.LEQ) {
				Literal[] nativeLits = new Literal[lits.length];
				for (int i = 0; i < lits.length; i++) {
					nativeLits[i] = lits[i].getLitImpl();
				}
				_cnf.addFormula(new PBconstraint(nativeLits, coefs, value)
						.getCNF());
			}
			if (ineq == Ineq.EQ || ineq == Ineq.GEQ) {
				Literal[] nativeLits = new Literal[lits.length];
				int sum = 0;
				for (int i = 0; i < lits.length; i++) {
					nativeLits[i] = lits[i].getLitImpl().neg();
					sum += coefs[i];
				}
				_cnf.addFormula(new PBconstraint(nativeLits, coefs, sum
						- value).getCNF());
			}
		} catch (QuotaException e) {
			throw new NoSolutionException(e);
		}
	}

	@Override
	public void addPBInequality(Collection<BVLiteral> lits, Ineq ineq, int value)
			throws NoSolutionException {
		addPBInequality(lits.toArray(new BVLiteral[lits.size()]), ineq, value);
	}

	@Override
	public void addReifiedPBInequality(BVLiteral[] lits, Ineq ineq, int value,
			BVLiteral equivalentTo) throws NoSolutionException {
		_pbReifier.addReifiedIneqality(lits, ineq, value, equivalentTo);
	}

	@Override
	public void addReifiedPBInequality(Collection<BVLiteral> lits, Ineq ineq,
			int value, BVLiteral equivalentTo) throws NoSolutionException {
		addReifiedPBInequality(lits.toArray(new BVLiteral[lits.size()]), ineq,
				value, equivalentTo);
	}

	@Override
	public void addExactlyOneTrue(Collection<BVLiteral> lits)
			throws NoSolutionException {
		addExactlyOneTrue(lits.toArray(new BVLiteral[lits.size()]));
	}

	@Override
	public void addExactlyOneTrue(BVLiteral[] lits) throws NoSolutionException {
		_exactlyOneTrue.exactlyOneTrue(lits);
	}

	/**
	 * The underlying cnf that is generated.
	 * 
	 * @return
	 */
	@Override
	public CNFformula getCNF() {
		return _cnf;
	}

	/**
	 * The collection of variables in the strong backdoor.
	 * 
	 * @return
	 */
	public Collection<Variable> getStrongBackdoor() {
		return _strongVars;
	}

	@Override
	public PBformula getPB() {
		throw new UnsupportedOperationException("getPB");
	}

	public void writeDimacsToFile(File file) throws FileNotFoundException {
		Model model = new Model();
		model.post(this);
		PrintWriter pw = new PrintWriter(file);
		CnfProblem problem = new CnfProblem(pw);
		problem.read(model);
		pw.close();
		problem.writeCNFLine(file.getAbsolutePath());
	}

	public void writeDimacsToFile(String filename) throws FileNotFoundException {
		writeDimacsToFile(new File(filename));
	}

	public String getDimacs() {
		Model model = new Model();
		model.post(this);
		CnfProblem pb = new CnfProblem();
		pb.read(model);
		return pb.getOutput();
	}

	@Override
	public BVLiteral[] lArray(int size) {
		return new BVLiteral[size];
	}

	@Override
	public BVLiteral[][] lMatrix(int size, int size2) {
		return new BVLiteral[size][size2];
	}

	@Override
	protected BVLiteral[] toArray(Collection<BVLiteral> c) {
		return c.toArray(new BVLiteral[c.size()]);
	}

	@Override
	public BVLiteral fromDimacs(int dimacs) {
		return new BVLiteral(_vars.get(Math.abs(dimacs)).getLit(dimacs > 0));
	}

	@Override
	public SolutionWriter getCNFWriter() {
		return this;
	}

	@Override
	public void setOutput(String outputFile) throws IOException {
		if ("-".equals(outputFile)) {
			throw new IllegalArgumentException(
					"BoolvarPB does not support output to stdout");
		}
		this._dimacsFile = new File(outputFile);
	}

	@Override
	public void writeSolution(PatternConverter converter) throws IOException {
		writeDimacsToFile(_dimacsFile);
	}

	boolean isStrong(int variableId) {
		return _strongBackdoor.get(variableId);
	}

	public void freeInternal() {
		_strongBackdoor = null;
		_strongVars = null;
		_vars = null;
	}

	@Override
	public void addReifiedWPBInequality(BVLiteral[] lits, int[] coefs,
			Ineq ineq, int value, BVLiteral equivTo) throws NoSolutionException {
		// TODO Auto-generated method stub
		
	}
}
