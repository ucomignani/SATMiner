/* SatQLEngine/src/test/java/dag/satmining/problem/satql/ast/SQLDelegateAtomTest.java

   Copyright (C) 2014 Emmanuel Coquery.

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
