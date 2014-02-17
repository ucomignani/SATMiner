/* ./satmining-core/src/test/java/dag/satmining/constraints/formula/DirectTest.java

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

package dag.satmining.constraints.formula;

import static dag.satmining.constraints.formula.Formula.*;

import org.sat4j.specs.TimeoutException;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.constraints.SAT4JMCB;

public class DirectTest extends SAT4JMCB {

	private DimacsLiteral _a;
	private DimacsLiteral _b;
	private DimacsLiteral _c;
	private DimacsLiteral _d;
	private DimacsLiteral _e;
	private DimacsLiteral _f;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_a = newLiteral();
		addClause(_a);
		_b = newLiteral();
		addClause(_b);
		_c = newLiteral();
		addClause(_c);
		_d = newLiteral();
		addClause(_d);
		_e = newLiteral();
		addClause(_e);
		_f = newLiteral();
		addClause(_f);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		_a = null;
		_b = null;
		_c = null;
		_d = null;
		_e = null;
		_f = null;
	}

	protected void checkPass(Formula f) {
		try {
			f.direct(this);
			assertTrue(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
			fail(e.getMessage());
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}

	}

	protected void checkFail(Formula f) {
		try {
			f.direct(this);
			assertFalse(getSolver().isSatisfiable());
		} catch (NoSolutionException e) {
		} catch (TimeoutException e) {
			fail(e.getMessage());
		}
	}

	public void testAnd1() throws TimeoutException {
		checkPass(and(_a, _b));
	}

	public void testAnd2() throws TimeoutException {
		checkFail(and(_a.getOpposite(), _b));
	}

	public void testAnd3() {
		checkFail(and(_a.getOpposite(), _b.getOpposite()));
	}

	public void testOr1() {
		checkPass(or(_a, _b));
	}

	public void testOr2() {
		checkPass(or(_a.getOpposite(), _b));
	}

	public void testOr3() {
		checkFail(or(_a.getOpposite(), _b.getOpposite()));
	}

	public void testImpl1() {
		checkPass(impl(atom(_a), atom(_b)));
	}

	public void testImpl2() {
		checkPass(impl(atom(_a.getOpposite()), atom(_b.getOpposite())));
	}

	public void testImpl3() {
		checkFail(impl(atom(_a), atom(_b.getOpposite())));
	}

	public void testEquiv1() {
		checkPass(equiv(atom(_a), _b));
	}

	public void testEquiv2() {
		checkFail(equiv(_a.getOpposite(), atom(_b)));
	}

	public void testEquiv3() {
		checkFail(equiv(atom(_a), _b.getOpposite()));
	}

	public void testEquiv4() {
		checkPass(equiv(atom(_a.getOpposite()), _b.getOpposite()));
	}
	
	public void testNeg1() {
		checkPass(not(_a.getOpposite()));
	}
	
	public void testNeg2() {
		checkFail(not(_a));
	}

}
