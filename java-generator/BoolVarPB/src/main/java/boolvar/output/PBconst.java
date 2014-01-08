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
import java.math.BigInteger;

import boolvar.model.Variable;

/**
 * Any instance of this class is a pseudo Boolean constraint.
 * RAW representation: must not be used for specifying
 * a problem, but only to encode it.
 */
public class PBconst
{
    public static final int EQ =1;
    public static final int GEQ = 2;
    
    private ArrayList<Variable> Var;        // Variables
    private ArrayList<BigInteger> Coeff;    // Coefficients
    private int operator;                   // 1 for =, 2 for >=
    private BigInteger bound;               // Bound
    
    /**
     * Creates a new empty pseudo Boolean constraint.
     * Default value for the bound is 0.
     * Default operator is =.
     */
    public PBconst()
    {
        Var = new ArrayList<Variable>();
        Coeff = new ArrayList<BigInteger>();
        operator = EQ;
        bound = BigInteger.ZERO;
    }
    
    /**
     * Creates a new pseudo Boolean constraint by cloning an
     * existing one.
     */
    public PBconst(PBconst t)
    {
        Var = new ArrayList<Variable>();
        Coeff = new ArrayList<BigInteger>();
        operator = t.getOperator();
        bound = t.getBound();
        for(int i=0; i<t.size(); i++)
        {
            Var.add(t.getVariable(i));
            Coeff.add(t.getCoeff(i));
        }
    }
    
    /**
     * Adds a monomial to the pseudo Boolean constraint.
     * @param v the variable.
     * @param c the coefficient.
     */
    public void addMonome(Variable v, int c)
    {
        Var.add(v);
        Coeff.add(new BigInteger(""+c));
    }
    
    /**
     * Adds a monomial to the pseudo Boolean constraint.
     * @param v the variable.
     * @param c the coefficient.
     */
    public void addMonome(Variable v, BigInteger c)
    {
        Var.add(v);
        Coeff.add(c);
    }
    
    /**
     * Removes a monomial from the pseudo Boolean constraint.
     * @param rank index of the monomial to remove.
     */
    public void removeMonome(int rank)
    {
        Var.remove(rank);
        Coeff.remove(rank);
    }
    
    /**
     * Sets the operator of the constraint.
     * @param op 1 for =, 2 for >=.
     */
    public void setOperator(int op)
    {
        operator = op;
    }
    
    /**
     * Sets the bound of the constraint.
     * @param b bound.
     */
    public void setBound(int b)
    {
        bound = new BigInteger(""+b);
    }
    
    /**
     * Sets the bound of the constraint.
     * @param b bound.
     */
    public void setBound(BigInteger b)
    {
        bound = b;
    }
    
    /**
     * Gets the operator of the constraint.
     * @return 1 if =, 2 if >=.
     */
    public int getOperator()
    {
        return operator;
    }
    
    /**
     * Gets the bound of the constraint.
     * @return bound.
     */
    public BigInteger getBound()
    {
        return bound;
    }
    
    /**
     * Gets the number of monomials in the constraint.
     * @return the number of monomials.
     */
    public int size()
    {
        return Var.size();
    }
    
    /**
     * Gets the variable related to the monomial of rank i
     * @param i rank (start from 0).
     * @return the requested variable.
     */
    public Variable getVariable(int i)
    {
        return Var.get(i);
    }
    
    /**
     * Gets the coefficient related to the monomial of rank i
     * @param i rank (start from 0).
     * @return the requested coefficient.
     */
    public BigInteger getCoeff(int i)
    {
        return Coeff.get(i);
    }
    
    /**
     * Creates a String representing a raw pseudo Boolean constraint
     * for debugging and displaying purpose.
     * @return the resulting string.
     */
    @Override
    public String toString()
    {
        String output = "(";
        if(size()==0)
            output += "Empty";
        else
        {
            output += (getCoeff(0).toString() + getVariable(0).toString());
            for(int i=1; i< size(); i++)
                output += (" + " + getCoeff(i).toString() 
                                 + getVariable(i).toString());
            
            if(getOperator()==1)
                output += " = ";
            else
                output += " >= ";
            
            output += getBound().toString();
        }
        return output+")";
    }
}
