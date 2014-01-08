/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.parser;

import java.io.StringReader;

import junit.framework.TestCase;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.problem.rql.ast.ASTDictionnary;
import dag.satmining.problem.rql.ast.MiningQuery;
import dag.satmining.problem.rql.ast.sql.SQLTrue;

/**
 *
 * @author ecoquery
 */
public class ParserTest extends TestCase {

    private RQLParser<DimacsLiteral> mkParser(String data) {
        return new RQLParser<DimacsLiteral>(new StringReader(data));
    }
    
    private RQLParser<DimacsLiteral> fromResource(String resource) {
        return new RQLParser<DimacsLiteral>(getClass().getResourceAsStream(resource));
    }

    public void testAndExpr() throws ParseException {
        ASTDictionnary dict = new ASTDictionnary();
        RQLParser<DimacsLiteral> p;
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
        RQLParser<DimacsLiteral> p;
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
        RQLParser<DimacsLiteral> p;
        p=mkParser("123 = 'ab'");
        assertNotNull(p.AtomicExpr(dict));
    }
    
    public void testTupleValues() throws ParseException {
        ASTDictionnary dict = new ASTDictionnary();
        dict.getAttributeConstant("a");
        dict.getAttributeVariable("B");
        RQLParser<DimacsLiteral> p;
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
        RQLParser<DimacsLiteral> p = fromResource("/funct_deps.rql");
        mq = p.MiningQuery();
        assertNotNull(mq);
        assertEquals(2,mq.getSchemaVariables().size());
        assertEquals(5,mq.getAttributes().size());
        assertEquals(SQLTrue.instance(),mq.getWhere().getExpr());
    }
    
}
