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

package boolvar.output;

import java.util.ArrayList;

/**
 * A peudo-Booelan formula is a list of pseudo-Boolean constraints
 * (i.e., PBconst instances).
 */
public class PBformula 
{
    private ArrayList<PBconst> formula;
    
    /**
     * Creates an empty CNF formula.
     */
    public PBformula()
    {
        formula = new ArrayList<PBconst>();
    }
    
    /**
     * Creates an empty CNF formula.
     * @param capacity Initial capacity.
     */
    public PBformula(int capacity)
    {
        formula = new ArrayList<PBconst>(capacity);
    }
    
    /**
     * Adds a new clause to the current formula.
     * @param q The clause to add.
     */
    public void addPBconst(PBconst q)
    {
        formula.add(q);
    }
    
    /**
     * Gives the size of the current formula.
     * @return the number of clauses in the formula.
     */
    public int size()
    {
        return formula.size();
    }
    
    /**
     * Gives the clause with rank i in the current formula.
     * @param i the rank of the required clause.
     * @return the required clause.
     */
    public PBconst getPBconst(int i)
    {
        return formula.get(i);
    }
    
    /**
     * Removes the clause with rank i from the current formula.
     * @param i the rank of the clause to remove.
     */
    public void removePBconst(int i)
    {
        formula.remove(i);
    }
    
    /**
     * Removes all the clauses belonging to the current formula.
     */
    public void clear()
    {
        formula.clear();
    }
    
    /**
     * Adds a new formula to the current one.
     * @param f the formula to add.
     */
    public void addFormula(PBformula f)
    {
        formula.addAll(f.formula);
    }
    
    /**
     * Gives the number of literal occurrences in the current formula.
     * @return The sum of the sizes of all the constraints in the formula.
     */
    public int fullSize()
    {
        int size = 0;
        for(int i=0; i<formula.size(); i++)
        {
            PBconst q = formula.get(i);
            size += q.size();
        }
        return size;
    }
    
    /**
     * Removes all the constants true or false in the current formula.
     * TO DO...
     */
    /*
    public void removeConstants()
    {
        int n = formula.size();
        int i=0;
        while(i<n)
        {
            PBconst q = formula.get(i);
            if(q.removeConstants())
            {
                formula.remove(i);
                n--;
            }
            else
            {
                if(q.size()==0)
                    throw new RuntimeException("Inconsistency detected");
                i++;
            }
        }
    }
    */
    
    @Override
    public String toString()
    {
        String output = "[";
        for(int i=0; i<formula.size(); i++)
        {
            output += formula.get(i).toString();
        }
        return output+"]";
    }
}
