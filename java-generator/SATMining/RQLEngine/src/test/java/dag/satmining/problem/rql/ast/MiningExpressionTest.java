/* ./RQLEngine/src/test/java/dag/satmining/problem/rql/ast/MiningExpressionTest.java

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.problem.rql.parser.ParseException;
import dag.satmining.problem.rql.parser.RQLParser;
import java.io.StringReader;
import junit.framework.TestCase;

/**
 *
 * @author ecoquery
 */
public class MiningExpressionTest extends TestCase {
    
    private ASTDictionnary dict;
    
    private MiningExpression read(String data) throws ParseException {
        RQLParser<DimacsLiteral> p = new RQLParser<DimacsLiteral>(new StringReader(data));
        return p.MiningExpr(dict);
    }
    
    public MiningExpressionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        if (dict == null) {
            dict = new ASTDictionnary();
            dict.getAttributeConstant("a");
            dict.getAttributeVariable("A");
            dict.getAttributeVariable("B");
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testPrintEquals() throws ParseException {
        MiningExpression e = read("F A(X) t.A=t2.A and true");
        String s = e.toString();
        assertEquals(e, read(s));
        assertEquals("( F A(X) (t.A = t2.A AND TRUE) )",s);
    }
    
    public void testPushDown() throws ParseException {
        MiningExpression e1 = read("(NOT t.A=t2.A) OR t.B=t2.B");
        MiningExpression e1p = e1
                .pushDown(Quantifier.Exists, dict.getAttributeVariable("A"), dict.getSchemaVariable("X"));
        MiningExpression e2 = read("NOT (F A(X) t.A=t2.A) OR t.B=t2.B");
        assertEquals(e2,e1p);
        e1p = e1p
                .pushDown(Quantifier.ForAll, dict.getAttributeVariable("B"), dict.getSchemaVariable("Y"));
        e2 = read("NOT (F A(X) t.A=t2.A) OR (F B(Y) t.B=t2.B)");
        assertEquals(e2,e1p);
    }
}
