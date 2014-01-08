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
