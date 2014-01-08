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

package boolvar.utility;

import boolvar.model.Variable;
import boolvar.model.Literal;
import java.util.*;

import boolvar.output.*;

public class PropagateCNF 
{
    //The current pseudo Boolean formula
    private CNFformula formula;
    
    //The observable Boolean variables
    private ArrayList<Variable> observable;
    
    //The value assigned to each (observable or not) variable
    private Hashtable<Variable, Boolean> value;
    
    //Set to true when contradiction is detected
    private boolean unsat;
    
    //Set to true each time the formula is modified.
    private boolean modified;
    
    /**
     * Creates a testing environment.
     */
    public PropagateCNF()
    {
        formula = new CNFformula(1);
        observable = new ArrayList<Variable>();
        value = new Hashtable<Variable,Boolean>();
        unsat = false;
    }
    
    /**
     * Adds a new formula, i.e., a list of pseudo Boolean
     * constraints, to the current formula.
     * @param f the formula to add.
     */
    public void addFormula(CNFformula f)
    {
        formula.addFormula(f);
    }
    
    /**
     * Adds a new pseudo Boolean constraint to the current formula.
     * @param t the constraint to add.
     */
    public void addClause(Clause t) throws QuotaException
    {
        formula.addClause(t);
    }
    
    /**
     * Adds a new observable variable.
     * @param v the variable to add.
     */
    public void addVariable(Variable v)
    {
        observable.add(v);
    }
    
    /**
     * Adds several new observable variables.
     * @param tab Array of Variables to add.
     */
    public void addVariables(Variable[] tab)
    {
        observable.addAll(new ArrayList<Variable>(Arrays.asList(tab)));
    }
    
    /**
     * Adds several new observable variables.
     * @param list ArrayList of Variables to add.
     */
    public void addVariables(ArrayList<Variable> list)
    {
        observable.addAll(list);
    }
    
    /**
     * Assigns a value to an observable variable.
     * @param v the variable to be assigned.
     * @param x the value to assign.
     */
    public void assignVariable(Variable v, boolean x)
    {
        if(!observable.contains(v))
            observable.add(v);
        value.put(v, new Boolean(x));
    }
    
    /**
     * Assigns a value to each observable variable, according
     * to its rank.
     * @param tab array containing the values that will be assigned
     * to the variables: 0 for true, 1 for false. Other values
     * make the variable unassigned.
     */
    public void assignVariables(int[] tab)
    {
        if(tab.length >observable.size())
            throw(new RuntimeException("To much variables to assign\n"));
        
        for(int i=0; i<tab.length; i++)
        {
            Variable v = observable.get(i);
            if(tab[i]==0)
                value.put(v, false);
            else if(tab[i]==1)
                value.put(v, true);
            else
                value.remove(v);
        }
    }
    
    /**
     * Tests if an inconsistency was detected.
     * @return true if and only if an inconsistency was detected.
     */
    public boolean isUnsat()
    {
        return unsat;
    }
    
    /**
     * Verifies whether the formula is satisfied by the
     * current assignation.
     * @return true if and only if the formula is empty.
     */
    public boolean isTrue()
    {
        return formula.size()==0;
    }
    
    /**
     * Gives the value assigned to a variable.
     * @param v the variable to test.
     * @return the value assigned to v: 0 for false, 1 for true, 2 if
     * v is not assigned.
     */
    public int getValue(Variable v)
    {
        if(!value.containsKey(v))
            return 2;
        if(value.get(v))
            return 1;
        else
            return 0;
    }
    
    /**
     * Tests whether the values assigned to the observable variables
     * match with the values provided as a mask. The mask is an Array
     * of integers: 0 for false, 1 for true, other values will be ignored.
     * @param tab the mask.
     * @return true if the values assigned to the observable variables
     * match with the mask.
     */
    public boolean testValues(int[] tab)
    {
        if(tab.length >observable.size())
            throw(new RuntimeException("To much variables to assign\n"));
        
        for(int i=0; i<tab.length; i++)
        {
            if((tab[i]==0)||(tab[i]==1))
            {
                if(!value.containsKey(observable.get(i)))
                    return false;
                Variable v = observable.get(i);
                if(tab[i]==0)
                {
                    if(value.get(v)) return false;
                }
                else
                    if(!value.get(v)) return false;
            }            
        }
        return true;
    }
    
    /**
     * Simplifies a constraint by removing the variables
     * that are assigned.
     * @param t the constraint to simplify.
     * @return true if and only if the resulting constraint is false.
     */
    private boolean simplifyCNF(Clause t)
    {
        int n = t.size();
        int i = 0;
        while(i<n)
        {
            Variable var = t.getLiteral(i).getVariable();
            boolean sign = t.getLiteral(i).getSign();
            if(value.containsKey(var))
            {
                if(value.get(var)==sign)
                {
                    t.unfill();
                    modified = true;
                    return false;
                }
                else
                {
                    t.removeLiteral(i);
                    n--;
                    modified = true;
                }           
            }
            else
                i++;    
        }
        return t.size()==0;
    }
    
    /**
     * Checks a constraint for inconsistency.
     * @param t the constraint to test.
     * @return true if the constraint cannot be satisfied, whatever
     * the values of its variables.
     */
    private boolean testInconsistency(Clause t)
    {
        return t.size()==0;
    }
    
    /**
     * Assigns any variable of a given constraint when one of its
     * two possible values makes the constraint false. Do not modify
     * the constraint, but only (if applicable) the values assigned to
     * its variables.
     * @param t the input constraint.
     * @return true if and only if a new assignation is inconsistent
     * with an existing one.
     */
    private boolean filteringCNF(Clause t)
    {
        if(testInconsistency(t))
            return true;
        
        if(t.size()==1)
        {
            Variable var = t.getLiteral(0).getVariable();
            boolean sign = t.getLiteral(0).getSign();
            if(value.containsKey(var))
            {
                if(value.get(var)!=sign)
                    return true;
            }
            else
            {
                value.put(var, sign);
                modified = true;
            }
        }
        return false;
    }
    
    /**
     * Achieves all possible propagations, filtering and simplifications 
     * until one of the following facts hold: the formula is satisfied,
     * an inconsistency is detected, or no more propagation can be done.
     */
    public void propagate()
    {
        do
        {   
            modified = false;
            int i=0; int n=formula.size();
            while(i<n)
            {
                Clause t = formula.getClause(i);
                if(simplifyCNF(t))
                {
                    unsat = true;
                    return;
                }
                else if(t.size()==0)
                {
                    formula.removeClause(i);
                    n--;
                }
                else
                    i++;
            }
            
            /*System.out.println("--- simplification ---\n"+toString());*/
            
            if(isTrue()) return;
            
            n = formula.size();
            for(i=0; i<n; i++)
            {
                if(filteringCNF(formula.getClause(i)))
                {
                    unsat = true;
                    return;
                }
            }
            
            /*System.out.println("--- filtrage ---\n"+toString());*/
        }
        while(modified);
    }
    
    /**
     * Calls propagate(), then applies the pure literal rule if
     * the resulting formula is neither unsat, nor true.
     */
    public void propagatePure()
    {
        propagate();
        if(isUnsat()||isTrue()) return;
        
        int nClauses=formula.size();
        int i=0;
        while(i<nClauses)
        {
            Clause q = formula.getClause(i);
            if(isPure(q))
            {
                formula.removeClause(i);
                nClauses--;
            }
            else
                i++;
        }
        
        boolean existAllNeg = false;
        boolean existAllPos = false;
        for(i=0; i<nClauses; i++)
        {
            Clause c = formula.getClause(i);
            if(isAllNeg(c)) existAllNeg = true;
            if(isAllPos(c)) existAllPos = true;
        }
        if((!existAllNeg)||(!existAllPos))
            formula.clear();
    }
    
    boolean isAllNeg(Clause q)
    {
        for(int i=0; i<q.size(); i++)
            if(q.getLiteral(i).getSign())
                return false;
        return true;
    }
    
    boolean isAllPos(Clause q)
    {
        for(int i=0; i<q.size(); i++)
            if(!(q.getLiteral(i).getSign()))
                return false;
        return true;
    }
    
    private boolean isLiteralPure(Literal lit)
    {
        Literal notl =lit.neg();
        for(int j=0; j<formula.size(); j++)
        {
            Clause q1 = formula.getClause(j);
            for(int k=0; k<q1.size(); k++)
            {
                if(q1.getLiteral(k).equals(notl))
                    return false;
            }
        }
        return true;
    }
    
    private boolean isPure(Clause q)
    {
        for(int i=0; i<q.size(); i++)
        {
            Literal l = q.getLiteral(i);
            if(isLiteralPure(l))
                return true;
        }
        return false;
    }

/***************************************************************************/
/*                                   TESTS                                 */
/***************************************************************************/

    /**
     * Gives a string representation of the current formula.
     * @return the resulting string.
     */
    @Override
    public String toString()
    {
        String output = "";
        output += "Formula :\n";
        for(int i=0; i<formula.size(); i++)
            output += (formula.getClause(i).toString() + "\n");
        
        output += "Variables :\n";
        for(int i=0; i<observable.size(); i++)
        {
            Variable var = observable.get(i);
            output += (var.toString() + " ");
            if(!value.containsKey(var))
                output += "[X]\n";
            else if(value.get(var))
                output += "[1]\n";
            else
                output += "[0]\n";
        }
        
        output += "Status : ";
        if(isUnsat())
            output += "UNSAT\n";
        else if(isTrue())
            output += "TRUE\n";
        else
            output += "UNKNOW\n";
        return output;
    }

    public static void test() throws QuotaException
    {
        //Creating the propagation context
        PropagateCNF prop = new PropagateCNF();
        
        //Creating the variables
        Variable[] vars = new Variable[3];
        for(int i=0; i<vars.length; i++)
            vars[i]= new Variable();
        
        //Creating a formula
        CNFformula f = new CNFformula();
        
        Clause t = new Clause(vars[0].getPosLit(), vars[1].getNegLit());
        f.addClause(t);
        
        t = new Clause(vars[0].getNegLit());
        f.addClause(t);
        
        t = new Clause(vars[1].getPosLit(), vars[2].getPosLit());
        f.addClause(t);
        
        //t = new Clause(new Literal(vars[2],false));
        //f.add(t);
        
        prop.addFormula(f);
        
        //Making some variables observable
        prop.addVariables(vars);
        
        //prop.assignVariable(vars[2], false);
        //prop.assignVariables(new int[]{1,2,1});
        
        System.out.println(prop.toString());
        
        prop.propagate();
        
        System.out.println(prop.toString());
        
        //System.out.println(prop.testValues(new int[]{1,0,1}));
    }
}
