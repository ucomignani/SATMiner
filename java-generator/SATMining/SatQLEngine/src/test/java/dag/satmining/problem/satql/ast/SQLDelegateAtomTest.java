package dag.satmining.problem.satql.ast;

import java.util.regex.Matcher;

import junit.framework.TestCase;

public class SQLDelegateAtomTest extends TestCase { 
    
    public void testAttIDRegex() {
        Matcher m = SQLDelegateAtom.IDENTIFIER_PATTERN.matcher("$A = truc.$B");
        assertTrue(m.find());
        assertTrue(m.find(2));
        assertFalse(m.start() == 2);
        assertTrue(m.find(10));
        assertTrue(m.start() == 10);
        assertFalse(m.find(11));
        assertTrue(m.find(0) && m.start()==0);
    }
    
    public void testParsingAtom() {
        String q = " t1.$A = t2.$A";
        Matcher mI = SQLDelegateAtom.IDENTIFIER_PATTERN.matcher(q);
        Matcher mO = SQLDelegateAtom.OTHER_PATTERN.matcher(q);
        int start = 0;
        assertTrue(mI.find(start));
        assertFalse(mI.start() == start);
        assertTrue(mO.find(start));
        assertEquals(start,mO.start());
        start = mO.end();
        assertEquals(4,start);
        assertTrue(mI.find(start));
        assertEquals(start,mI.start());
        assertTrue(mO.find(start));
        assertFalse(mO.start() == start);
        start = mI.end();
        assertEquals(6,start);
        assertTrue(mI.find(start));
        assertFalse(mI.start() == start);
        assertTrue(mO.find(start));
        assertEquals(start,mO.start());
        start = mO.end();
        assertEquals(12,start);
        assertTrue(mI.find(start));
        assertEquals(start,mI.start());
        assertFalse(mO.find(start) && mO.start() == start);
    }
        
}
