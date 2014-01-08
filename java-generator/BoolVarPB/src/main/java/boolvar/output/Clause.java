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

import boolvar.model.Literal;
import boolvar.model.Variable;
import boolvar.utility.Quota;
import boolvar.utility.QuotaException;

/**
 * Any instance of this class is a propositional clause
 * where the literals are encoded as signed integers.
 * RAW representation: must not be used for specifying
 * a problem, but only to encode it.
 */
public class Clause 
{
	private ArrayList<Literal> tab;
	
        /**
         * Creates a new empty clause with room for 3 literals.
         */
	public Clause()
	{
		tab = new ArrayList<Literal>(3);
	}
	
        /**
         * Creates a new clauses containing one literal.
         * @param a the literal to put into the clause.
         * @throws QuotaException 
         */
	public Clause (Literal a) throws QuotaException
	{
		tab = new ArrayList<Literal>(1);
		tab.add(a);
		Quota.addLits(1);
    	Quota.verifQuota();
	}
	
        /**
         * Creates a new binary clause.
         * @param a first literal.
         * @param b second literal.
         * @throws QuotaException 
         */
	public Clause (Literal a, Literal b) throws QuotaException
	{
		tab = new ArrayList<Literal>(2);
		tab.add(a); tab.add(b);
		Quota.addLits(2);
    	Quota.verifQuota();
	}
	
        /**
         * Creates a new ternary clause.
         * @param a first literal.
         * @param b second literal.
         * @param c third literal.
         * @throws QuotaException 
         */
	public Clause (Literal a, Literal b, Literal c) throws QuotaException
	{
		tab = new ArrayList<Literal>(3);
		tab.add(a); tab.add(b);	tab.add(c);
		Quota.addLits(3);
    	Quota.verifQuota();
	}
	
        /**
         * Creates a new clause containing 4 literals.
         * @param a a literal.
         * @param b a literal.
         * @param c a literal.
         * @param d a literal.
         * @throws QuotaException 
         */
	public Clause (Literal a, Literal b, Literal c, Literal d) throws QuotaException
	{
		tab = new ArrayList<Literal>(4);
		tab.add(a); tab.add(b);	tab.add(c); tab.add(d);
		Quota.addLits(4);
    	Quota.verifQuota();
	}
	
        /**
         * Creates a new clause containing 5 literals.
         * @param a a literal.
         * @param b a literal.
         * @param c a literal.
         * @param d a literal.
         * @param e a literal.
         * @throws QuotaException 
         */
	public Clause (Literal a, Literal b, Literal c, Literal d, Literal e) throws QuotaException
	{
		tab = new ArrayList<Literal>(5);
		tab.add(a); tab.add(b);	tab.add(c); tab.add(d);	tab.add(e);
		Quota.addLits(5);
    	Quota.verifQuota();
	}
	
        /**
         * Gives the number of literals of the clause.
         * @return the number of literals.
         */
	public int size()
	{
		return tab.size();
	}
	
        /**
         * Gives one of the literals of the clause.
         * @param n the rang of the literal (first index value is 0).
         * @return the requested literal.
         */
	public Literal getLiteral(int n)
	{
		return tab.get(n);
	}
	
        /**
         * Adds a literal to the clause.
         * @param l the literal to add.
         * @throws QuotaException 
         */
	public void addLiteral(Literal l) throws QuotaException
	{
		tab.add(l);
		Quota.addLits(1);
    	Quota.verifQuota();
	}
	
        /**
         * Removes a literal. 
         * @param rank rank of the literal to remove.
         */
        public void removeLiteral(int rank)
        {
            tab.remove(rank);
        }
        
        /**
         * Unfills the current clause.
         */
        public void unfill()
        {
            tab.clear();
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
        
        /**
         * Makes a list of clauses according to an Array of patterns.
         * @param pattern pattern[i] explains of to build the clause i
         * according to the literals. 1: the corresponding literal is
         * used as it is, 0: the literal is negated, 2: the literal is not
         * used.
         * @param lit Literals used to build the clauses.
         * @return the resulting list of clauses.
         * @throws QuotaException 
         */
        public static ArrayList<Clause> makeClauses(int[][] pattern, 
                                                    Literal[] lit) throws QuotaException
        {
            ArrayList<Clause> output = new ArrayList<Clause>();
            
            int nbClauses = pattern.length;
            for(int i=0; i<nbClauses; i++)
            {
                Clause q = makeClause(pattern[i],lit);
                if(q!=null)
                    output.add(q);
            }
            
            return output;
        }
        
        /**
         * Removes all the constants true or false in the clause.
         * @return true if and only if the resulting clause is true.
         */
        public boolean removeConstants()
        {
            int n = size();
            int i=0;
            while(i<n)
            {
                Literal l=tab.get(i);
                if(l.getVariable()==null)
                {
                    if(l.getSign())
                        return true;
                    removeLiteral(i);
                    n--;
                }
                else
                    i++;
            }
            return false;
        }
        
        /**
         * Removes all the constants true or false in a formula
         * represented as a list of clauses.
         * @param formula the formula to simplify.
         */
        public static void removeConstants(ArrayList<Clause> formula)
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
         * Makes a string representation of the clause
         * for visualization or debugging purpose.
         * @return the string representing the clause.
         */
        @Override
	public String toString()
	{
		String res="(";
		for(int i=0; i<tab.size(); i++)
		{
			res += tab.get(i).toString();
			if((i+1)<tab.size())
				res += " or ";
		}
		res +=")";
		return res;
	}
        
        /**
         * Gives the number of literal occurrences in a formula.
         * @param formula List of clauses.
         * @return size of the input formula.
         */
        static public int formulaSize(ArrayList<Clause> formula)
        {
            int size = 0;
            for(int i=0; i<formula.size(); i++)
            {
                Clause q = formula.get(i);
                size += q.size();
            }
            return size;
        }
        
        public static void test() throws QuotaException
        {
            Literal[] lit = new Literal[4];
            lit[0] = new Variable().getPosLit();
            lit[1] = new Variable().getNegLit();
            lit[2] = Literal.TRUE;
            lit[3] = Literal.FALSE;
            
            final int[][] pattern =
            {
                {1,2,0,2},
                {2,0,2,2},
                {0,1,2,2},
                {1,0,2,0}
            };
            
            ArrayList<Clause> res = makeClauses(pattern, lit);
            
            for(int i=0; i<res.size(); i++)
                System.out.println(res.get(i).toString());
        }
        
        /**
         * Creates a string representation of this clause in dimacs format.
         * The code of this method has been moved from 
         * {@link CnfProblem#post(boolvar.output.Clause) CnfProblem.post(Clause)}
         * in order to enable strong backdoor clause writing.
         * @return the dimacs line for this clause.
         */
        public String dimacsLine() {
            String result = "";
            for (int i = 0; i < size(); i++) {
                result += (makeLiteral(getLiteral(i)) + " ");
            }
            result += "0\n";
            return result;
        }

	// Translates a literal into an integer
	private int makeLiteral(Literal l) {
		int sign = 1;
		if (!l.getSign())
			sign = -1;
		int idvar = l.getVariable().getId();
		return idvar * sign;
	}
}
