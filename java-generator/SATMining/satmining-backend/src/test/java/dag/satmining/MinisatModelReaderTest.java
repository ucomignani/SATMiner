/* ./satmining-backend/src/test/java/dag/satmining/MinisatModelReaderTest.java

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

package dag.satmining;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import junit.framework.TestCase;
import dag.satmining.backend.Interpretation;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.minisat.MinisatModelReader;

public class MinisatModelReaderTest extends TestCase {

	public void testReader() {
		String input = "=\n|12 \n=  gh\n12 1 1245 56 \n78 80 789\n";
		StringReader sr = new StringReader(input);
		MinisatModelReader mr = new MinisatModelReader(sr);
		assertTrue(mr.getNext());
		Interpretation inter = mr.getCurrentInterpretation();
		assertNotNull(inter);
		assertFalse(inter.getValue(new DimacsLiteral(3)));
		assertTrue(inter.getValue(new DimacsLiteral(1)));
		assertTrue(inter.getValue(new DimacsLiteral(12)));
		assertTrue(inter.getValue(new DimacsLiteral(56)));
		assertTrue(inter.getValue(new DimacsLiteral(1245)));
		assertTrue(mr.getNext());
		inter = mr.getCurrentInterpretation();
		assertNotNull(inter);
		assertFalse(inter.getValue(new DimacsLiteral(1)));
		assertFalse(inter.getValue(new DimacsLiteral(12)));
		assertTrue(inter.getValue(new DimacsLiteral(78)));
		assertTrue(inter.getValue(new DimacsLiteral(80)));
		assertTrue(inter.getValue(new DimacsLiteral(789)));
		assertFalse(mr.getNext());
	}
	
	public void testNbModels() throws IOException {
		InputStream is = getClass().getResourceAsStream("/output1.txt");
		MinisatModelReader mr = new MinisatModelReader(new InputStreamReader(is));
		for(int i = 0 ; i < 40; ++i) {
			assertTrue(mr.getNext());
		}
		assertFalse(mr.getNext());
	}
	
}
