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

import boolvar.model.Variable;
import boolvar.model.Literal;
import java.math.*;
import java.util.*;

import boolvar.output.Clause;
import boolvar.output.PBconst;
import boolvar.utility.PropagateCNF;
import boolvar.utility.PropagatePB;
import boolvar.utility.Quota;
import boolvar.utility.QuotaException;
import boolvar.output.CNFformula;
import boolvar.output.PBformula;

/**
 * A pseudo-Boolean constraint a_1 w_1 + ... + a_n w_n <= B, where
 * a_1, ..., a_n, B are positive integers and w_1, ...,w_n are
 * literals.
 */
public class PBconstraint implements Constraint 
{
    Literal[] lit;        // Literals w_1, ...,w_n
    BigInteger[] coeff;   // Coefficients a_1, ...,a_n
    BigInteger bound;     // Bound B
    
    /**
     * Encoding using binary adders.
     */
    public static final int LINEAR = 1;
    /**
     * Encoding that does not use additional variable.
     */
    public static final int DIRECT = 2;
    /**
     * Encoding based on BDD.
     */
    public static final int BDD = 3;
    
    /**
     * Arc consistent if reasonable size, else LINEAR.
     */
    public static final int AUTO = 6;
    
    /**
     * Polynomial WatchDog encoding.
     */
    public static final int POLYW =4;
    
    /**
     * Global Bargraph Cascading    
     */
    public static final int GLOBC=5;
    
    private static int variant = DIRECT;          // encoding variant.
    
    static private int sizeLIN = 0;
    static private int sizeDIR = 0;
    static private int sizeBDD = 0;
    static private int sizePW = 0;
    static private int sizeGLOB = 0;
    
    static private int countLIN = 0;
    static private int countDIR = 0;
    static private int countBDD = 0;
    static private int countPW = 0;
    static private int countGLOB = 0;
    
    static public String stringStat()
    {
    	String out = "Encoding : LINEAR, DIRECT, BDD, POLYW, GLOBC\n";
    	out += ("Size : "+sizeLIN+", "+sizeDIR+", "+sizeBDD+", "+sizePW+", "+sizeGLOB+"\n");
    	out += ("Count : "+countLIN+", "+countDIR+", "+countBDD+", "+countPW+", "+countGLOB+"\n");
    	return out;
    }
    
    private CNFformula cnfOutput;
    
    //Used for BDD encoding variant
    class BDD
    {
        int rank;
        BigInteger bound;
        @Override
        public boolean equals(Object other)
        {
            if(other instanceof BDD)
            {
                BDD otherBDD = (BDD)other;
                return ((rank==otherBDD.rank)&&bound.equals(otherBDD.bound));
            }
            return false;
        }
        @Override
        public int hashCode()
        {
            return rank+bound.hashCode();
        }
    }
    private Hashtable<BDD,Literal> cache;
    
    //Used for Polynomial Watchdog encoding variant
    class TC //Like Threshold counter
    {
        ArrayList<Literal> lits;
        int threshold;
        @Override
        public boolean equals(Object other)
        {
            if(other instanceof TC)
            {
                TC otherTC = (TC)other;
                return ((threshold==otherTC.threshold)&&lits.equals(otherTC.lits));
            }
            return false;
        }
        @Override
        public int hashCode()
        {
            return threshold+lits.hashCode();
        }
    }
    private Hashtable<TC,Literal> cacheTC;
    
    /**
     * Creates a pseudo-Boolean constraint.
     * @param l literals.
     * @param c coefficients.
     * @param b bound.
     */
    public PBconstraint(Literal[] l, BigInteger[] c, BigInteger b)
    {
        lit = l;
        coeff = c;
        bound = b;
        sortCoeffs();
    }
    
    /**
     * Creates a pseudo-Boolean constraint.
     * @param l literals.
     * @param c coefficients.
     * @param b bound.
     */
    public PBconstraint(ArrayList<Literal> l, ArrayList<BigInteger> c,
                        BigInteger b)
    {
       lit = l.toArray(new Literal[]{});
       coeff = c.toArray(new BigInteger[]{});
       bound = b;
       sortCoeffs();
    }
    
    /**
     * Creates a pseudo-Boolean constraint.
     * @param l literals.
     * @param c coefficients.
     * @param b bound.
     */
    public PBconstraint(Literal[] l, int[] c, int b)
    {
        lit = l;
        coeff = new BigInteger[c.length];
        bound = new BigInteger(""+b);
        for(int i=0; i<c.length; i++)
        {
            coeff[i] =  new BigInteger(""+c[i]);
        }
        sortCoeffs();
    }
    
    /**
     * Creates a pseudo-Boolean constraint.
     * @param l literals.
     * @param c coefficients.
     * @param b bound.
     */
    public PBconstraint(ArrayList<Literal> l, ArrayList<Integer> c, int b)
    {
       lit = l.toArray(new Literal[]{});
       coeff = new BigInteger[c.size()];
       bound = new BigInteger(""+b);
       for(int i=0; i<c.size(); i++)
        {
            coeff[i] =  new BigInteger(""+c.get(i));
        }
       sortCoeffs();
    }
    
    private void sortCoeffs()
    {
        BigInteger tmpCoeff;
        Literal tmpLit;
        for(int i=0; i<coeff.length-1; i++)
            for(int j=i+1; j<coeff.length; j++)
            {
                if(coeff[i].compareTo(coeff[j])>0)
                {
                    tmpCoeff=coeff[i];
                    tmpLit=lit[i];
                    coeff[i]=coeff[j];
                    lit[i]=lit[j];
                    coeff[j]=tmpCoeff;
                    lit[j]=tmpLit;
                }
            }
        verifCoeffs();
    }
    
    private void verifCoeffs()
    {
        for(int i=0; i<coeff.length; i++)
        {
            if(coeff[i].compareTo(BigInteger.ZERO)<=0)
                throw new RuntimeException("Coefficient négatif ou nul");
        }
    }
    
    /**
     * Sets the encoding variant.
     * <ul>
     * <li> LINEAR : Linear encoding.
     * <li> DIRECT : Encoding with no additional variable.
     * <li> BDD : Encoding using a Binary Decision Diagram.
     * <li> BEST : DIRECT or BDD if reasonable size, else LINEAR.
     * </ul>
     * @param v : Encoding variant.
     */
    public static void setVariant(int v)
    {
        variant = v;
    }
    
    /**
     * The currently used encoding variant. {@link #setVariant(int) }
     * @return the curretly used variant
     */
    public static int getVariant() {
        return variant;
    }
    
    //-------------------------------------------------------------------
    //                   CNF encoding
    //-------------------------------------------------------------------
   
    public CNFformula getCNF() throws QuotaException
    {
        //cnfOutput = new CNFformula();
        int localVariant = variant;
        
        if(localVariant==AUTO)
        {
        	int ticket;
        	int sizeDirect, sizeGPW;
        	boolean okDirect=true;

        	ticket = Quota.beginTry();      	
        	Quota.setMaxLit(2000, ticket);
        	try{getDirectCNF();}
        	catch(QuotaException e){okDirect=false;}
        	sizeDirect = Quota.getNbLitUsed(ticket);
        	Quota.abandonTry(ticket);       	
 
   			ticket = Quota.beginTry();      	
   			try{getGloBarCascadingCNF();}
        	catch(QuotaException e){throw new RuntimeException();}
        	sizeGPW = Quota.getNbLitUsed(ticket);
        	Quota.abandonTry(ticket);
        	
        	if(!okDirect) localVariant = GLOBC;
        	else if (sizeDirect<sizeGPW) localVariant = DIRECT;
        	else localVariant = GLOBC;
        }
        
        CNFformula cnf;
        if(localVariant==LINEAR)
        {
        	cnf = getLinearCNF();
        	sizeLIN += cnf.fullSize();
        	countLIN++;
        	/*System.out.println(cnf.fullSize());*/
            return cnf;
        }
        else if(localVariant==DIRECT)
        {
        	cnf = getDirectCNF();
        	sizeDIR += cnf.fullSize();
        	countDIR++;
            return cnf;
        }
        else if(localVariant==BDD)
        {
        	cnf = getBddCNF();
        	sizeBDD += cnf.fullSize();
        	countBDD++;
            return cnf;
        }
        else if(localVariant==POLYW)
        {
        	cnf = getWatchdogCNF();
        	sizePW += cnf.fullSize();
        	countPW++;
        	return cnf;
        }
        else if(localVariant==GLOBC)
        {
        	cnf = getGloBarCascadingCNF();
        	sizeGLOB += cnf.fullSize();
        	countGLOB++;
        	return cnf;
        }
        else
        	throw new RuntimeException("Encoding variant not implemented");
    }
    
    /*
    public CNFformula getCNF_auto() throws QuotaException
    {
        //cnfOutput = new CNFformula();
        int localVariant = variant;
        
        if(localVariant==AUTO)
        {
        	int ticket;
        	boolean ok;

        	ok=true;
        	ticket = Quota.beginTry();      	
        	Quota.setMaxLit(1000, ticket);
        	try{getDirectCNF();}
        	catch(QuotaException e){ok=false;}
        	Quota.abandonTry(ticket);       	
        	if(ok) localVariant = DIRECT;
        	else
        	{
        		localVariant = LINEAR;
        		ticket = Quota.beginTry();
        		try {getLinearCNF();}
        		catch(QuotaException e){}
        		int sizeLIN = Quota.getNbLitUsed(ticket);
        		Quota.abandonTry(ticket);

        		//max size pour encoding which propagates
        		int quota = sizeLIN*5;

        		ok=true;
        		ticket = Quota.beginTry();      	
        		Quota.setMaxLit(quota, ticket);
        		try{getBddCNF();}
        		catch(QuotaException e){ok=false;}
        		if(ok) 
        		{
        			quota = Quota.getNbLitUsed(ticket);
        			localVariant = BDD;
        		}
        		Quota.abandonTry(ticket);

        		ok=true;
        		ticket = Quota.beginTry();      	
        		Quota.setMaxLit(quota, ticket);
        		try{getWatchdogCNF();}
        		catch(QuotaException e){ok=false;}
        		if(ok) 
        		{
        			quota = Quota.getNbLitUsed(ticket);
        			localVariant = POLYW;
        		}
        		Quota.abandonTry(ticket);

        		if(localVariant==LINEAR)
        		{
        			//max size for encoding which detects inconsistencies
        			quota = sizeLIN*3;
        			System.gc();
        			ok=true;
        			ticket = Quota.beginTry();      	
        			Quota.setMaxLit(quota, ticket);
        			try{getGloBarCascadingCNF();}
        			catch(QuotaException e){ok=false;}
        			if(ok) 
        			{
        				quota = Quota.getNbLitUsed(ticket);
        				localVariant = GLOBC;
        			}
        			Quota.abandonTry(ticket);
        		}
        	}
        }
        
        CNFformula cnf;
        if(localVariant==LINEAR)
        {
        	cnf = getLinearCNF();
        	sizeLIN += cnf.fullSize();
        	countLIN++;
            return cnf;
        }
        else if(localVariant==DIRECT)
        {
        	cnf = getDirectCNF();
        	sizeDIR += cnf.fullSize();
        	countDIR++;
            return cnf;
        }
        else if(localVariant==BDD)
        {
        	cnf = getBddCNF();
        	sizeBDD += cnf.fullSize();
        	countBDD++;
            return cnf;
        }
        else if(localVariant==POLYW)
        {
        	cnf = getWatchdogCNF();
        	sizePW += cnf.fullSize();
        	countPW++;
        	return cnf;
        }
        else if(localVariant==GLOBC)
        {
        	cnf = getGloBarCascadingCNF();
        	sizeGLOB += cnf.fullSize();
        	countGLOB++;
        	return cnf;
        }
        else
        	throw new RuntimeException("Encoding variant not implemented");
    }
    */
    
    //--------------------- BDD encoding --------------------------------
    
    private CNFformula getBddCNF() throws QuotaException
    {
    	cnfOutput = new CNFformula();
        cache = new Hashtable<BDD,Literal>();
        Literal output = makesBDD(coeff.length, bound);
        cnfOutput.addClause(new Clause(output.neg()));
        cnfOutput.removeConstants();
        return cnfOutput;
    }
    
    private Literal makesBDD(int nCoeffs, 
                             BigInteger bound) throws QuotaException
    {
    	/*System.out.println("makesBDD("+nCoeffs+","+bound.toString()+")");*/
        if(nCoeffs==0)
            return Literal.FALSE;
        
        BigInteger sumCoeffs = BigInteger.ZERO;
        for(int i=0; i<nCoeffs; i++)
            sumCoeffs = sumCoeffs.add(coeff[i]);
        if(sumCoeffs.compareTo(bound)<=0)
            return Literal.FALSE;
        
        if(bound.compareTo(BigInteger.ZERO)<0)
            return Literal.TRUE;
        
        if(nCoeffs==1)
            return lit[0];
        
        Literal output = new Variable().getPosLit();
        
        BDD ask = new BDD();
        ask.bound = bound;
        ask.rank=nCoeffs;
        if(cache.containsKey(ask))
            return cache.get(ask);
        
        if(bound.compareTo(BigInteger.ZERO)==0)
        {
            for(int i=0; i<nCoeffs; i++)
                cnfOutput.addClause(new Clause(output,lit[i].neg()));
            Clause q = new Clause(output.neg());
            for(int i=0; i<nCoeffs; i++)
                q.addLiteral(lit[i]);
            cnfOutput.addClause(q);
        }
        else
        {
            Literal s = makesBDD(nCoeffs-1,bound);
            Literal t = makesBDD(nCoeffs-1,bound.subtract(coeff[nCoeffs-1]));
            cnfOutput.addClause(new Clause(s.neg(),output));
            cnfOutput.addClause(new Clause(t.neg(),lit[nCoeffs-1].neg(),
                                output));
            cnfOutput.addClause(new Clause(t,output.neg()));
            cnfOutput.addClause(new Clause(s,lit[nCoeffs-1],output.neg()));
        }
        
        cache.put(ask, output);        
        return output;
    }
    
    //--------------------- DIRECT encoding -----------------------------
    
    private CNFformula getDirectCNF() throws QuotaException
    {
        return directCnfEncoding(lit, coeff, bound, coeff.length);
    }
    
    /*
    private CNFformula directCnfEncoding(Literal[] lit, BigInteger[] coeff,
                                         BigInteger bound) throws QuotaException
    {
        CNFformula output;
        int n = coeff.length;
        
        if(bound.compareTo(BigInteger.ZERO)<0)
        {
            output = new CNFformula(1);
            output.addClause(new Clause());
            return output;
        }
        
        BigInteger sumCoeffs = BigInteger.ZERO;
        for(int i=0; i<n; i++)
            sumCoeffs = sumCoeffs.add(coeff[i]);
        if(sumCoeffs.compareTo(bound)<=0)
        {
            output = new CNFformula(0);
            return output;
        }
        
        if(n==1)
        {
            output = new CNFformula(1);
            output.addClause(new Clause(lit[0].neg()));
            return output;
        }
        
        Literal[] newLit = new Literal[n-1];
        BigInteger[] newCoeff = new BigInteger[n-1];
        for(int i=0; i<n-1; i++)
        {
            newLit[i]=lit[i];
            newCoeff[i]=coeff[i];
        }
        output = directCnfEncoding(newLit, newCoeff, 
                                   bound.subtract(coeff[n-1]));
        for(int i=0; i<output.size(); i++)
            output.getClause(i).addLiteral(lit[n-1].neg());
        output.addFormula(directCnfEncoding(newLit, newCoeff, bound));
        return output;
    }
    */
    
    private CNFformula directCnfEncoding(Literal[] lit, BigInteger[] coeff,
    		                             BigInteger bound, int n) throws QuotaException
    {
    	CNFformula output;

    	if(bound.compareTo(BigInteger.ZERO)<0)
    	{
    		output = new CNFformula(1);
    		output.addClause(new Clause());
    		return output;
    	}

    	BigInteger sumCoeffs = BigInteger.ZERO;
    	for(int i=0; i<n; i++)
    		sumCoeffs = sumCoeffs.add(coeff[i]);
    	if(sumCoeffs.compareTo(bound)<=0)
    	{
    		output = new CNFformula(0);
    		return output;
    	}

    	if(n==1)
    	{
    		output = new CNFformula(1);
    		output.addClause(new Clause(lit[0].neg()));
    		return output;
    	}

    	output = directCnfEncoding(lit, coeff, bound.subtract(coeff[n-1]),n-1);
    	for(int i=0; i<output.size(); i++)
    		output.getClause(i).addLiteral(lit[n-1].neg());
    	output.addFormula(directCnfEncoding(lit, coeff, bound, n-1));
    	return output;
    }
    
    //--------------------- LINEAR encoding -----------------------------
    private CNFformula getLinearCNF() throws QuotaException
    {
    	cnfOutput = new CNFformula();
        ArrayList<ArrayList<Literal>> buckets = createBuckets();
        int nBuckets = buckets.size();
        
        ArrayList<Literal> leftValue = new ArrayList<Literal>(nBuckets);
        
        for(int i=0; i<nBuckets; i++)
        {
            ArrayList<Literal> output = makeTotalizer(buckets.get(i));
            if(output.size()==0)
                leftValue.add(Literal.FALSE);
            else
                leftValue.add(output.get(0));
            
            for(int j=1; j<output.size(); j++)
            {
                int rank = i+j;
                if(rank>=nBuckets)
                {
                    buckets.add(new ArrayList<Literal>());
                    nBuckets++;
                }
                buckets.get(rank).add(output.get(j));
            }
        }
     
        makeComparator(leftValue, bound);
        
        return cnfOutput;
    }
    
    
    private ArrayList<ArrayList<Literal>> createBuckets()
    {   
        int maxWeight =0;
        BigInteger zero = new BigInteger("0");
        for(int i=0; i<coeff.length; i++)
        {
            int w = 0;
            BigInteger c = coeff[i];
            while(c.divide(new BigInteger("2")).compareTo(zero)>0)
            {
                w++;
                c = c.divide(new BigInteger("2"));
            }
            if(w>maxWeight)
                maxWeight = w;
        }
                
        ArrayList<ArrayList<Literal>> buckets = 
                new ArrayList<ArrayList<Literal>>(maxWeight+1);
        for(int i=0; i<=maxWeight; i++)
            buckets.add(new ArrayList<Literal>());
        
        for(int i=0; i<coeff.length; i++)
        {       
            BigInteger c = coeff[i];
            int w = 0;
            while(c.compareTo(BigInteger.ZERO)>0)
            {
                if(c.mod(new BigInteger("2")).compareTo(zero)>0)
                    buckets.get(w).add(lit[i]);
                w++;
                c = c.divide(new BigInteger("2"));   
            }
        }
        
        return buckets;
    }
    
    private ArrayList<Literal> makeTotalizer(ArrayList<Literal> input) throws QuotaException
    {
        ArrayList<Literal> output; 
        int nInput = input.size();
        if(nInput==0)
        {
            output = new ArrayList<Literal>();
            output.add(Literal.FALSE);
            return output;
        }
        else if(nInput==1)
        {
            output = new ArrayList<Literal>();
            output.add(input.get(0));
            return output;
        }
        else
        {
            ArrayList<Literal> input1 = new ArrayList<Literal>();
            ArrayList<Literal> input2 = new ArrayList<Literal>();
            for(int i=0; i<nInput; i++)
            {
                if(i<nInput/2)
                    input1.add(input.get(i));
                else
                    input2.add(input.get(i));
            }
            ArrayList<Literal> output1 = makeTotalizer(input1);
            ArrayList<Literal> output2 = makeTotalizer(input2);
            output = makeBinaryAdder(output1, output2);
        }
        return output;
    }
    
    private ArrayList<Literal> makeBinaryAdder(ArrayList<Literal> input1,
                                       ArrayList<Literal> input2) throws QuotaException
    {
        int length;
        if(input1.size()<input2.size())
        {
            length = input2.size();
            for(int i=0; i<length-input1.size(); i++)
                input1.add(Literal.FALSE);
        }
        else
        {
            length = input1.size();
            for(int i=0; i<length-input2.size(); i++)
                input2.add(Literal.FALSE);
        }
        
        ArrayList<Literal> output = new ArrayList<Literal>(length+1);
        Literal carryOut = Literal.FALSE;
        for(int i=0; i<length; i++)
        {
            output.add(new Variable().getPosLit());
            Literal carryIn = carryOut;
            carryOut = new Variable().getPosLit();
            if(i==length-1)
               output.add(carryOut);
            
            makeClausesAdder(input1.get(i),input2.get(i),carryIn,
                             output.get(i),carryOut);
        }
        return output;
    }
    
    private void makeClausesAdder(Literal in1, Literal in2, Literal carryIn,
                                  Literal out, Literal carryOut) throws QuotaException
    {
        final int[][] clausesPattern =
        {
            {0,0,0,1,2},
            {1,1,1,0,2},
            {0,1,1,1,2},
            {1,0,1,1,2},
            {1,1,0,1,2},
            {1,0,0,0,2},
            {0,1,0,0,2},
            {0,0,1,0,2},
            
            {0,0,2,2,1},
            {0,2,0,2,1},
            {2,0,0,2,1},
            {1,1,2,2,0},
            {1,2,1,2,0},
            {2,1,1,2,0},
            
            {1,2,2,0,0},
            {2,1,2,0,0},
            {2,2,1,0,0},
            {0,2,2,1,1},
            {2,0,2,1,1},
            {2,2,0,1,1}
        };
        
        Literal[] literals = {in1, in2, carryIn, out, carryOut};
        
        cnfOutput.addClauses(clausesPattern,literals);
    }
    
    private void makeComparator(ArrayList<Literal> input, BigInteger bound) throws QuotaException
    {
        ArrayList<Boolean> boundBool = new ArrayList<Boolean>();
        final BigInteger DEUX = new BigInteger("2");
        while(bound.compareTo(BigInteger.ZERO)>0)
        {
            if(bound.mod(DEUX).compareTo(BigInteger.ZERO)>0)
                boundBool.add(true);
            else
                boundBool.add(false);
            bound = bound.divide(DEUX);
        }
        
        while(input.size()<boundBool.size())
            input.add(Literal.FALSE);
        while(boundBool.size()<input.size())
            boundBool.add(false);
        
        cnfOutput.addFormula(makeComparator(input,boundBool));
    }
    
    private CNFformula makeComparator(ArrayList<Literal> input,
                                             ArrayList<Boolean> bound) throws QuotaException
    {
        int size = input.size();
        if(size==0)
        {
            return new CNFformula(0);
        }
        Literal l = input.get(size-1);
        Literal notL = l.neg();
        boolean b = bound.get(size-1);
        input.remove(size-1);
        bound.remove(size-1);
        CNFformula output = makeComparator(input,bound);
        if(b)
        {
            for(int i=0; i<output.size(); i++)
                output.getClause(i).addLiteral(notL);
        }
        else
            output.addClause(new Clause(notL));
        output.removeConstants();
        return output;
    }
    
    //--------------- Polynomial WatchDog Encoding ----------------------
    
    public CNFformula getWatchdogCNF() throws QuotaException
    {
    	cnfOutput = new CNFformula();
        int nCoeffs = coeff.length;

        if(bound.compareTo(BigInteger.ZERO)<0)
        {
        	cnfOutput.addClause(new Clause(Literal.FALSE));
            cnfOutput.removeConstants();
    		return cnfOutput;
        }     
        
        BigInteger sumCoeffs = BigInteger.ZERO;
        for(int i=0; i<nCoeffs; i++)
            sumCoeffs = sumCoeffs.add(coeff[i]);
        if(sumCoeffs.compareTo(bound)<=0)
        {
    		return cnfOutput;
        }
        
        if(nCoeffs==1)
        {
        	cnfOutput.addClause(new Clause(lit[0].neg()));
            cnfOutput.removeConstants();
    		return cnfOutput;
        }

        cacheTC = new Hashtable<TC,Literal>();
        for(int i=0; i<nCoeffs; i++)
        {
        	makeWatchDog(i);
        }
        cnfOutput.removeConstants();
        return cnfOutput;
    }
    
    private void makeWatchDog(int rank) throws QuotaException
    {
    	int n=lit.length;
    	
    	BigInteger maxW; //highest coefficient
    	int p; //All bits in coefficients have weight at most 2^p
    	BigInteger b; //bound for setting watched literal to 0
    	BigInteger t; //tare
    	int k; // b + t = k*(2^p)
    	
    	maxW = BigInteger.ZERO;
    	for(int i=0; i<n; i++)
    		if(i!=rank)
    			if(coeff[i].compareTo(maxW)>0)
    				maxW = coeff[i];
    	
    	p=0; 
    	BigInteger two_power_p = BigInteger.ONE;
    	BigInteger two = BigInteger.valueOf(2);
    	while(maxW.compareTo(BigInteger.ONE)>0)
    	{
    		p++;
    		maxW = maxW.divide(two);
    		two_power_p =two_power_p.multiply(two);
    	}
    	
    	b = (bound.subtract(coeff[rank])).add(BigInteger.ONE);
    	
    	t = BigInteger.ZERO;
    	k=0;
    	while(t.compareTo(b)<0)
    	{
    		k++;
    		t = t.add(two_power_p);
    	}
    	
    	t = t.subtract(b);
    	
    	/*----------------
    	System.out.println("**********************************************************");
    	System.out.println("WatchDog du literal du rang "+rank);
    	System.out.print("Coefficients: ");
    	for(int i=0; i<n; i++)
    		if(i!=rank)
    			System.out.print(coeff[i].toString()+"*"+lit[i].toString()+" ");
    	System.out.println("\np = "+p);
    	System.out.println("b = "+b.toString());
    	System.out.println("t = "+t.toString());
    	System.out.println("k = "+k);
    	//----------------*/
    	
    	ArrayList<ArrayList<Literal>> buckets = new ArrayList<ArrayList<Literal>>(p+1);
    	for(int i=0; i<=p; i++)
    		buckets.add(new ArrayList<Literal>());
    	for(int i=0; i<n; i++)
    		if(i!=rank)
    		{
    			BigInteger co = coeff[i];
    			int j=0;
    			while(co.compareTo(BigInteger.ZERO)>0)
    			{
    				if(co.mod(two).compareTo(BigInteger.ZERO)!=0)
    					buckets.get(j).add(lit[i]);
    				j++;
    				co = co.divide(two);
    			}
    		}
   	
    	BigInteger tare = t;
    	int j=0;
    	while(tare.compareTo(BigInteger.ZERO)>0)
    	{
    		if(tare.mod(two).compareTo(BigInteger.ZERO)!=0)
    			buckets.get(j).add(Literal.TRUE);
    		j++;
    		tare = tare.divide(two);
    	}
    	
    	/*------------------
    	for(int i=0; i< buckets.size(); i++)
    	{
    		System.out.println("bucket "+i+" : "+buckets.get(i).toString());
    	}
    	//------------------*/
    	
    	ArrayList<Literal> lastBar = new ArrayList<Literal>(0);
    	for(int i=0; i<=p; i++)
    	{
    		ArrayList<Literal> varBar = new ArrayList<Literal>();
    		ArrayList<Literal> inputVars = buckets.get(i);
    		for(j=1; j<=inputVars.size(); j++)
    		{
    			Literal out = makeTC(inputVars,j);
    			varBar.add(out);
    			/*System.out.println("TC("+inputVars.toString()+","+j+") -> "+out.toString());*/
    		}
    		int nn = varBar.size()+lastBar.size();
    		if(i!=p)
    		{
    			ArrayList<Literal> newBar = new ArrayList<Literal>();
    			for(j=2; j<=nn; j+=2)
    			{
    				Literal out = make2BT(varBar,lastBar,j);
    				newBar.add(out);
    				/*System.out.println("2BT("+varBar.toString()+","+lastBar.toString()+","+j+") -> "+out.toString());*/
    			}
    			lastBar = newBar;
    		}
    		else
    		{
    			Literal out = make2BT(varBar,lastBar,k);
    			/*System.out.println("2BT("+varBar.toString()+","+lastBar.toString()+","+j+") -> "+out.toString());*/
    			cnfOutput.addClause(new Clause(out.neg(),lit[rank].neg()));
    		}
    	}
    }
    
    /**
     * This method encodes a threshold counter, that is a formula that
     * propagates the output literal to one if and only if there are
     * at least t literal fixed to 1 in the input list of literals.
     * @param l input list of literals.
     * @param t threshold.
     * @return the output literal.
     * @throws QuotaException
     */
    private Literal makeTC(ArrayList<Literal> l, int t) throws QuotaException
    {
    	if(l.size()==0)
    		return Literal.FALSE;
    	
    	if(l.size()<t)
    		return Literal.FALSE;
    	
    	Literal[] tabLit = l.toArray(new Literal[]{});
    	
    	if(l.size()==1)
    	{
    		return tabLit[0];
    	}
    	
    	if(t==0)
    		return Literal.TRUE;
    	
    	TC ask = new TC();
        ask.threshold = t;
        ask.lits = l;
        if(cacheTC.containsKey(ask))
            return cacheTC.get(ask);
        
        Literal y = new Variable().getPosLit();
        int n = l.size();
                
        if(t==1)
        {
        	for(int i=0; i<n; i++)
        	{
        		cnfOutput.addClause(new Clause(l.get(i).neg(),y));
        	}
        }
        else if(t==n)
        {
        	Clause q = new Clause();
        	for(int i=0; i<n; i++)
        	{
        		q.addLiteral(l.get(i).neg());
        	}
        	q.addLiteral(y);
        	cnfOutput.addClause(q);
        }
        else
        {
        	int n_a = n/2;
        	ArrayList<Literal> l_a = new ArrayList<Literal>(n_a);
        	for(int i=0; i<n_a; i++) l_a.add(l.get(i));
        	int n_b = n-(n/2);
        	ArrayList<Literal> l_b = new ArrayList<Literal>(n_b);
        	for(int i=0; i<n_b; i++) l_b.add(l.get(i+n_a));

        	for(int i=1; i<=n_a; i++)
        	{
        		int j =  t-i;
        		if((j>=1)&&(j<=n_b))
        		{
        			Literal y_a = makeTC(l_a,i);
        			Literal y_b = makeTC(l_b,j);
        			cnfOutput.addClause(new Clause(y_a.neg(),y_b.neg(),y));
        		}
        	}

        	if(n_a>=t)
        	{
        		Literal y_c = makeTC(l_a,t);
        		cnfOutput.addClause(new Clause(y_c.neg(),y));
        	}

        	if(n_b>=t)
        	{
        		Literal y_c = makeTC(l_b,t);
        		cnfOutput.addClause(new Clause(y_c.neg(),y));
        	}
        }
        
        cacheTC.put(ask, y);
        return y;
    }
    
    /**
     * Given 2 bargraphs b1 and b2, this method encodes a formula that propagates 1
     * if and only if the sum of the values represented by b1 and b2 is at least t.
     * @param b1 the first bargraph.
     * @param b2 the second bargraph.
     * @param t the threshold.
     * @return the output literal.
     * @throws QuotaException
     */
    private Literal make2BT(ArrayList<Literal> b1, ArrayList<Literal> b2, int t) throws QuotaException
    {
    	int n1 = b1.size();
    	int n2 = b2.size();
    	
    	if(t>n1+n2)
    		return Literal.FALSE;
    	
    	if(t==0)
    		return Literal.TRUE;
    	
    	if((n1==0)&&(n2>=t))
    		return b2.get(t-1);
    	
    	if((n2==0)&&(n1>=t))
    		return b1.get(t-1);
    	
    	Literal y = new Variable().getPosLit();
    	
    	if(n1>=t) cnfOutput.addClause(new Clause(b1.get(t-1).neg(),y));
    	if(n2>=t) cnfOutput.addClause(new Clause(b2.get(t-1).neg(),y));
    	
    	for(int i=1; i<=n1; i++)
    	{
    		int j = t-i;
    		if((j>=1)&&(j<=n2))
    			cnfOutput.addClause(new Clause(b1.get(i-1).neg(),b2.get(j-1).neg(),y));
    	}
    	
    	return y;
    }
    
    public static void tesTmakeTC() throws QuotaException
    {
    	Literal l1 = new Variable().getPosLit();
    	Literal l2 = new Variable().getPosLit();
    	Literal l3 = new Variable().getPosLit();
    	//Literal l4 = new Literal(new Variable(),true);
    	//Literal l5 = new Literal(new Variable(),true);
    	//Literal l6 = new Literal(new Variable(),true);
    	
    	PBconstraint pb = new PBconstraint(new Literal[]{}, new int[]{}, 0);
    	pb.cacheTC = new Hashtable<TC,Literal>();
    	pb.cnfOutput = new CNFformula();
    	
    	ArrayList<Literal> w1 = new ArrayList<Literal>();
    	w1.add(l1);w1.add(l2);w1.add(l3);w1.add(Literal.TRUE);
    	
    	//ArrayList<Literal> w2 = new ArrayList<Literal>();
    	//w2.add(l1);w2.add(l2);w2.add(l5);w2.add(l6);
    	
    	Literal y1 = pb.makeTC(w1,2);
    	System.out.println("--> "+y1.toString());
    	System.out.println(pb.cnfOutput.toString());
    	
    	//Literal y2 = pb.makeTC(w2,2);
    	//System.out.println("--> "+y2.toString());
    	//System.out.println(pb.cnfOutput.toString());
    }
    
    public static void tesTmake2BT() throws QuotaException
    {
    	Literal l1 = new Variable().getPosLit();
    	Literal l2 = new Variable().getPosLit();
    	Literal l3 = new Variable().getPosLit();
    	Literal l4 = new Variable().getPosLit();
    	Literal l5 = new Variable().getPosLit();
    	Literal l6 = new Variable().getPosLit();
    	
    	PBconstraint pb = new PBconstraint(new Literal[]{}, new int[]{}, 0);
    	pb.cacheTC = new Hashtable<TC,Literal>();
    	pb.cnfOutput = new CNFformula();
    	
    	ArrayList<Literal> w1 = new ArrayList<Literal>();
    	w1.add(l1);w1.add(l2);w1.add(l3);
    	
    	ArrayList<Literal> w2 = new ArrayList<Literal>();
    	w2.add(l4);w2.add(l5);w2.add(l6);
    	
    	Literal y1 = pb.make2BT(w1,w2,7);
    	System.out.println("--> "+y1.toString());
    	System.out.println(pb.cnfOutput.toString());
    }
    
    //--------------- Global Bargraph Cascading ----------------------
    
    public CNFformula getGloBarCascadingCNF() throws QuotaException
    {
    	cnfOutput = new CNFformula();
        int nCoeffs = coeff.length;
        if(bound.compareTo(BigInteger.ZERO)<0)
        {
        	cnfOutput.addClause(new Clause(Literal.FALSE));
            cnfOutput.removeConstants();
    		return cnfOutput;
        }     
        
        BigInteger sumCoeffs = BigInteger.ZERO;
        for(int i=0; i<nCoeffs; i++)
            sumCoeffs = sumCoeffs.add(coeff[i]);
        if(sumCoeffs.compareTo(bound)<=0)
        {
    		return cnfOutput;
        }
        
        if(nCoeffs==1)
        {
        	cnfOutput.addClause(new Clause(lit[0].neg()));
            cnfOutput.removeConstants();
    		return cnfOutput;
        }

        cacheTC = new Hashtable<TC,Literal>();
        makeCascadingBargraphs();

        cnfOutput.removeConstants();
        return cnfOutput;
    }
    
    private void makeCascadingBargraphs() throws QuotaException
    {
    	int n=lit.length;
    	
    	BigInteger maxW; //highest coefficient
    	int p; //All bits in coefficients have weight at most 2^p
    	BigInteger t; //tare
    	BigInteger b; //watching bound
    	int k; // b + t = k*(2^p)
    	
    	maxW = BigInteger.ZERO;
    	for(int i=0; i<n; i++)
    		if(coeff[i].compareTo(maxW)>0)
    			maxW = coeff[i];
    	
    	p=0; 
    	BigInteger two_power_p = BigInteger.ONE;
    	BigInteger two = BigInteger.valueOf(2);
    	while(maxW.compareTo(BigInteger.ONE)>0)
    	{
    		p++;
    		maxW = maxW.divide(two);
    		two_power_p =two_power_p.multiply(two);
    	}
    	
    	b = bound.add(BigInteger.ONE);
    	
    	t = BigInteger.ZERO;
    	k=0;
    	while(t.compareTo(b)<0)
    	{
    		k++;
    		t = t.add(two_power_p);
    	}
    	
    	t = t.subtract(b);
    	
    	/*----------------
    	System.out.println("**********************************************************");
    	System.out.print("Coefficients: ");
    	for(int i=0; i<n; i++)
   			System.out.print(coeff[i].toString()+"*"+lit[i].toString()+" ");
    	System.out.println("\np = "+p);
    	System.out.println("b = "+b.toString());
    	System.out.println("t = "+t.toString());
    	System.out.println("k = "+k);
    	//----------------*/
    	
    	ArrayList<ArrayList<Literal>> buckets = new ArrayList<ArrayList<Literal>>(p+1);
    	for(int i=0; i<=p; i++)
    		buckets.add(new ArrayList<Literal>());
    	for(int i=0; i<n; i++)
    	{
    		BigInteger co = coeff[i];
    		int j=0;
    		while(co.compareTo(BigInteger.ZERO)>0)
    		{
    			if(co.mod(two).compareTo(BigInteger.ZERO)!=0)
    				buckets.get(j).add(lit[i]);
    			j++;
    			co = co.divide(two);
    		}
    	}
   	
    	BigInteger tare = t;
    	int j=0;
    	while(tare.compareTo(BigInteger.ZERO)>0)
    	{
    		if(tare.mod(two).compareTo(BigInteger.ZERO)!=0)
    			buckets.get(j).add(Literal.TRUE);
    		j++;
    		tare = tare.divide(two);
    	}
    	
    	/*------------------
    	for(int i=0; i< buckets.size(); i++)
    	{
    		System.out.println("bucket "+i+" : "+buckets.get(i).toString());
    	}
    	//------------------*/
    	
    	ArrayList<Literal> lastBar = new ArrayList<Literal>(0);
    	for(int i=0; i<=p; i++)
    	{
    		ArrayList<Literal> varBar = new ArrayList<Literal>();
    		ArrayList<Literal> inputVars = buckets.get(i);
    		
    		for(j=1; j<=inputVars.size(); j++)
    		{
    			Literal out = makeTC(inputVars,j);
    			varBar.add(out);
    			/*System.out.println("TC("+inputVars.toString()+","+j+") -> "+out.toString());/**/
    		}
    		int nn = varBar.size()+lastBar.size();
    		
    		if(i!=p)
    		{
    			ArrayList<Literal> newBar = new ArrayList<Literal>();
    			for(j=2; j<=nn; j+=2)
    			{
    				Literal out = make2BT(varBar,lastBar,j);
    				newBar.add(out);
    				/*System.out.println("2BT("+varBar.toString()+","+lastBar.toString()+","+j+") -> "+out.toString());/**/
    			}
    			lastBar = newBar;
    		}
    		else
    		{
    			Literal out = make2BT(varBar,lastBar,k);
    			/*System.out.println("2BT("+varBar.toString()+","+lastBar.toString()+","+j+") -> "+out.toString());/**/
    			cnfOutput.addClause(new Clause(out.neg()));
    		}
    	}
    }
    
    //-------------------------------------------------------------------
    //                   Pseudo-Boolean encoding
    //-------------------------------------------------------------------
    
    public PBformula getPB()
    {
        PBconst output = new PBconst();
        output.setOperator(PBconst.GEQ);
        BigInteger cumul = BigInteger.ZERO;
        for(int i=0; i<coeff.length; i++)
        {
            if(lit[i].getSign())
            {
                output.addMonome(lit[i].getVariable(), coeff[i].negate());
            }
            else
            {
                output.addMonome(lit[i].getVariable(), coeff[i]);
                cumul = cumul.add(coeff[i]);
            }
        }
        output.setBound(cumul.subtract(bound));
        PBformula formula = new PBformula();
        formula.addPBconst(output);
        return formula;
    }
    
    @Override
    public String toString()
    {
        String output = coeff[0].toString()+"."+lit[0].toString();
        for(int i=1; i<coeff.length; i++)
        {
            output += (" + "+coeff[i].toString()+"."+lit[i].toString());
        }
        output += ("<= "+bound.toString());
        return output;
    }
    
    
    //--------------------------- Automated tests -----------------------
    
    private static String testOneConstraint
                          (int[] signs, int[] coeffs, int bound, 
                           int[] values, boolean expected) throws QuotaException
    {
        Literal[] lit = new Literal[signs.length];
        for(int i=0; i<lit.length; i++)
            if(signs[i]==0)
                lit[i] = new Variable().getNegLit();
            else
                lit[i] = new Variable().getPosLit();
        
        Variable[] var = new Variable[lit.length];
        for(int i=0; i<lit.length; i++)
            var[i] = lit[i].getVariable();       
        
        PBconstraint q = new PBconstraint(lit,coeffs,bound);

        for(int encoding=1; encoding<=5; encoding++)
        {
            setVariant(encoding);
            PropagateCNF prop = new PropagateCNF();
            prop.addFormula(q.getCNF());
            prop.addVariables(var);
            prop.assignVariables(values);
            prop.propagate();

            if((expected&&prop.isUnsat())||((!expected)&&prop.isTrue()))
            {      
                String output = "CNF encoding (V"+encoding+") :\n";
                output += (q.toString()+"\n");
                for(int i=0; i<var.length; i++)
                    output += (var[i].toString()+"="+values[i]+" ");
                return output+"\n";
            }
        }
        
        PropagatePB prop1 = new PropagatePB();
        prop1.addFormula(q.getPB());
        prop1.addVariables(var);
        prop1.assignVariables(values);
        prop1.propagate();
        
        if((expected&&prop1.isUnsat())||((!expected)&&prop1.isTrue()))
        {      
            String output = "PB encoding :\n";
            output += (q.toString()+"\n");
            for(int i=0; i<var.length; i++)
                output += (var[i].toString()+"="+values[i]+" ");
            return output+"\n";
        }
        
        return null;
    }
    
    private static String testManyConstraints() throws QuotaException
    {
        String result;
        
        result = testOneConstraint
                 (new int[]{1},new int[]{100},100,new int[]{0},true);
        if(result!=null) return result;
        
        result = testOneConstraint
                 (new int[]{1},new int[]{100},100,new int[]{1},true);
        if(result!=null) return result;
        
        result = testOneConstraint
                 (new int[]{1},new int[]{100},99,new int[]{0},true);
        if(result!=null) return result;
        
        result = testOneConstraint
                 (new int[]{1},new int[]{100},99,new int[]{1},false);
        if(result!=null) return result;
        
        // Random test benches generation
        
        int numberOfTests = 10;
        int numberOfCoeffs = 10;
        int coeffMin = 10;
        int coeffMax = 100;
        
        Random rand = new Random();
        for(int nTest=0; nTest<numberOfTests; nTest++)
        {
            int[] coeff = new int[numberOfCoeffs];
            for(int i=0; i<numberOfCoeffs; i++)
                coeff[i] = rand.nextInt(coeffMax-coeffMin+1)+coeffMin;

            int[] sign = new int[numberOfCoeffs];
            int[] value = new int[numberOfCoeffs];
            for(int i=0; i<numberOfCoeffs; i++)
            {
                sign[i] = rand.nextInt(2);
                value[i] = rand.nextInt(2);
            }

            int w = 0;
            for(int i=0; i<numberOfCoeffs; i++)
            {
                if(sign[i]==value[i])
                    w += coeff[i];
            }
            /*System.out.println("--> signs ["+sign[0]+" "+sign[1]+" "+sign[2]+"] "+
                    "coeffs ["+coeff[0]+" "+coeff[1]+" "+coeff[2]+"] "+"bound "+w+
                    " values ["+value[0]+" "+value[1]+" "+value[2]+"]"); */    
            result = testOneConstraint(sign, coeff, w, value, true);
            if(result!=null) return result;

            result = testOneConstraint(sign, coeff, w+1, value, true);
            if(result!=null) return result;

            if(w>0)
            {
                result = testOneConstraint(sign, coeff, w-1, value, false);
                if(result!=null) return result;

                result = testOneConstraint(sign, coeff, (2*w)/3, value, false);
                if(result!=null) return result;
            }

            result = testOneConstraint(sign, coeff, (3*w)/2, value, true);
            if(result!=null) return result;
        }
        return null;
    }
    
    /**
     * Test method that print a diagnosis.
     */
    public static void test() throws QuotaException
    {        
        String result = testManyConstraints();
        String notif ="Testing PBconstraint --> ";
        if(result==null)
            notif += "OK\n";
        else
            notif += ("BAD :\n"+result);
        System.out.print(notif);
    }
}
