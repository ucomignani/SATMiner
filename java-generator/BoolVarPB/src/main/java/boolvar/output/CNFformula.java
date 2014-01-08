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

import java.util.*;
import boolvar.model.Literal;
//import boolvar.utility.Quota;
import boolvar.utility.QuotaException;

/**
 * A CNF formula is a list of clauses
 */
public class CNFformula 
{
    private ArrayList<Clause> formula;
    
    /**
     * Creates an empty CNF formula.
     */
    public CNFformula()
    {
        formula = new ArrayList<Clause>();
    }
    
    /**
     * Creates an empty CNF formula.
     * @param capacity Initial capacity.
     */
    public CNFformula(int capacity)
    {
        formula = new ArrayList<Clause>(capacity);
    }
    
    /**
     * Adds a new clause to the current formula.
     * @param q The clause to add.
     * @throws QuotaException 
     */
    public void addClause(Clause q) throws QuotaException
    {
    	//Quota.addLits(q.size());
    	//Quota.verifQuota();
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
    public Clause getClause(int i)
    {
        return formula.get(i);
    }
    
    /**
     * Removes the clause with rank i from the current formula.
     * @param i the rank of the clause to remove.
     */
    public void removeClause(int i)
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
    public void addFormula(CNFformula f)
    {
        formula.addAll(f.formula);
    }
    
    /**
     * Gives the number of literal occurrences in the current formula.
     * @return The sum of the sizes of all the clauses in the formula.
     */
    public int fullSize()
    {
        int size = 0;
        for(int i=0; i<formula.size(); i++)
        {
            Clause q = formula.get(i);
            size += q.size();
        }
        return size;
    }
    
    /**
     * Removes all the constants true or false in the current formula.
     */
    public void removeConstants()
    {
        int n = formula.size();
        int i=0;
        while(i<n)
        {
            Clause q = formula.get(i);
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
    
     /**
     * Adds a list of clauses according to an Array of patterns.
     * @param pattern pattern[i] explains of to build the clause i
     * according to the literals. 1: the corresponding literal is
     * used as it is, 0: the literal is negated, 2: the literal is not
     * used.
     * @param lit Literals used to build the clauses.
     */
    public void addClauses(int[][] pattern, Literal[] lit) throws QuotaException
    {
        int nbClauses = pattern.length;
        for(int i=0; i<nbClauses; i++)
        {
            Clause q = makeClause(pattern[i],lit);
            if(q!=null)
            	addClause(q);
                //formula.add(q);
        }
    }
    
    private static Clause makeClause(int[] pattern, Literal[] lit) throws QuotaException
    {
        if(pattern.length!=lit.length)
            throw new RuntimeException("Incorrect pattern lentgth");
        for(int i=0; i<pattern.length; i++)
            if(lit[i].getVariable()==null)
            {
                if((pattern[i]==1)&&(lit[i].getSign()))
                    return null;
                if((pattern[i]==0)&&(!lit[i].getSign()))
                    return null;
            }
        Clause output = new Clause();
        for(int i=0; i<pattern.length; i++)
        {
            if((pattern[i]!=2)&&(lit[i].getVariable()!=null))
            {
                if(pattern[i]==1)
                    output.addLiteral(lit[i]);
                else
                    output.addLiteral(lit[i].neg());
            }
        }
        if(output.size()==0)
            throw new RuntimeException("Empty clause = inconsistency");
        return output;        
    }
    
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
