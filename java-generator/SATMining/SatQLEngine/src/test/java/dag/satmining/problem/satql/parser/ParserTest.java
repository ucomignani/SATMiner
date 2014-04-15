/* ./SATQLEngine/src/test/java/dag/satmining/problem/satql/parser/ParserTest.java

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
package dag.satmining.problem.satql.parser;

import java.io.StringReader;

import junit.framework.TestCase;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.problem.satql.ast.ASTDictionnary;
import dag.satmining.problem.satql.ast.MiningQuery;
import dag.satmining.problem.satql.ast.sql.SQLTrue;

/**
 *
 * @author ecoquery
 */
public class ParserTest extends TestCase {

    private SATQLParser<DimacsLiteral> mkParser(String data) {
        return new SATQLParser<DimacsLiteral>(new StringReader(data));
    }
    
    private SATQLParser<DimacsLiteral> fromResource(String resource) {
        return new SATQLParser<DimacsLiteral>(getClass().getResourceAsStream(resource));
    }

    public void testAndExpr() throws ParseException {
        ASTDictionnary dict = new ASTDictionnary();
        SATQLParser<DimacsLiteral> p;
        p = mkParser("TRuE");
        assertNotNull(p.AndExpr(dict));
        p = mkParser("TRUE AnD true");
        assertNotNull(p.AndExpr(dict));
        try {
            p = mkParser("true and");
            p.AndExpr(dict);
            fail("Parse error not detected");
        } catch (ParseException e) {
        }
    }

    public void testValues() throws ParseException {
        ASTDictionnary dict = new ASTDictionnary();
        SATQLParser<DimacsLiteral> p;
        p = mkParser("0102");
        assertNotNull(p.MiningVal(dict));
        try {
            p = mkParser("'ab '' c'");
            assertNotNull(p.MiningConstantValue(dict));
            p.MiningVal(dict);
            fail("did not parse full string");
        } catch (ParseException e) {
        }
    }
    
    public void testCmp() throws ParseException {
        ASTDictionnary dict = new ASTDictionnary();
        SATQLParser<DimacsLiteral> p;
        p=mkParser("123 = 'ab'");
        assertNotNull(p.AtomicExpr(dict));
    }
    
    public void testTupleValues() throws ParseException {
        ASTDictionnary dict = new ASTDictionnary();
        dict.getAttributeConstant("a");
        dict.getAttributeVariable("B");
        SATQLParser<DimacsLiteral> p;
        p = mkParser("t.a");
        assertNotNull(p.MiningTupleValue(dict));
        p = mkParser("t2.B");
        assertNotNull(p.MiningTupleValue(dict));
        try {
            p = mkParser("t3.c");
            p.MiningTupleValue(dict);
            fail("c is not known but parsing was ok");
        } catch (ParseException e) {};
    }
    
    public void testParseMiningQuery() throws ParseException {
        MiningQuery<DimacsLiteral> mq;
        SATQLParser<DimacsLiteral> p = fromResource("/funct_deps.satql");
        mq = p.MiningQuery();
        assertNotNull(mq);
        assertEquals(2,mq.getSchemaVariables().size());
        assertEquals(5,mq.getAttributes().size());
        assertEquals(SQLTrue.instance(),mq.getWhere().getExpr());
    }
    
    public void testParseMiningQueryIfThen() throws ParseException {
        MiningQuery<DimacsLiteral> mq;
        SATQLParser<DimacsLiteral> p = fromResource("/funct_deps_ifthen.satql");
        mq = p.MiningQuery();
        assertNotNull(mq);
        assertEquals(2,mq.getSchemaVariables().size());
        assertEquals(5,mq.getAttributes().size());
        assertEquals(SQLTrue.instance(),mq.getWhere().getExpr());
    }
    
}
