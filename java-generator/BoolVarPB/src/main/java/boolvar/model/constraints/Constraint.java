/**
Copyright (c) 2008-2009 Olivier Bailleux

This file is a part of the BoolVar/PB project.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package boolvar.model.constraints;

import boolvar.output.CNFformula;
import boolvar.output.PBformula;
import boolvar.utility.QuotaException;

/**
 * A Constraint can be translated either into a CNF formula
 * or into a set of pseudo Boolean constraints.
 */
public interface Constraint 
{
    /**
     * Produces a CNF formula resulting from the translation
     * of the Constraint.
     * @return the resulting formula.
     * @throws QuotaException 
     */
    public CNFformula getCNF() throws QuotaException;
	
    /**
     * Produces a set of pseudo Boolean constraints resulting
     * from the translation of the Constraint.
     * @return the resulting set of pseudo Boolean constraints.
     */
    public PBformula getPB();
	
    /**
     * Produces a string representation of the Constraint
     * for display or debugging purpose.
     * @return the resulting string.
     */
    public String toString();    
}
