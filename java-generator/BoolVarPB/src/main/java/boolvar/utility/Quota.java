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

import java.util.*;

import boolvar.model.Variable;


public class Quota 
{
	static Stack<Integer> backupNbVar = new Stack<Integer>();
	static Stack<Integer> backupNbLit = new Stack<Integer>();
	static Stack<Integer> varQuota = new Stack<Integer>();
	static Stack<Integer> litQuota = new Stack<Integer>();
	static int currentTicket = 939;
    static int countLit = 0;
	
    
    /**
     * Initializes a new trying session. The variables and literal quotas
     * are set to 0 (i.e., no quota).
     * @return the ticket number of the current session.
     */
    static public int beginTry()
    {
    	varQuota.push(0);
    	litQuota.push(0);
    	backupNbVar.push(Variable.getNbUsed());
    	backupNbLit.push(countLit);
    	return ++currentTicket;
    }
    
    /**
     * Sets the maximum allowed number of new variables for the current 
     * trying session. Must be called after the trying session is initialized.
     * @param maxVar variable quota for the specified trying session.
     * @param ticket ticket number for the current session.
     */
    static public void setMaxVar(int maxVar, int ticket)
    {
    	if(ticket!=currentTicket)
    		throw new RuntimeException("Bad ticket");
    	varQuota.pop();
    	varQuota.push(maxVar);
    }
    
    /**
     * Sets the maximum allowed number of new literal occurrences for 
     * the current trying session. Must be called after the trying session is 
     * initialized.
     * @param maxLit literal quota for the specified trying session.
     * @param ticket ticket number for the current session.
     */
    static public void setMaxLit(int maxLit, int ticket)
    {
    	if(ticket!=currentTicket)
    		throw new RuntimeException("Bad ticket");
    	litQuota.pop();
    	litQuota.push(maxLit);
    }
    
    /**
     * Gets the number of variables consumed during the current session.
     * @param ticket ticket number of the current session.
     * @return the number of consumed variables.
     */
    static public int getNbVarUsed(int ticket)
    {
    	if(ticket!=currentTicket)
    		throw new RuntimeException("Bad ticket");
    	return Variable.getNbUsed()-backupNbVar.peek();
    }
    
    /**
     * Gets the number of literal occurrences consumed during the current 
     * session.
     * @param ticket ticket number of the current session.
     * @return the number of consumed literals.
     */
    static public int getNbLitUsed(int ticket)
    {
    	if(ticket!=currentTicket)
    		throw new RuntimeException("Bad ticket");
    	return countLit-backupNbLit.peek();
    }
    
    /**
     * Adds occurrences to the literal counter.
     * @param nbOcc the number of literal occurrences to add.
     */
    static public void addLits(int nbOcc)
    {
    	countLit += nbOcc;
    	/*System.out.println("-->" + countLit);*/
    }
    
    /**
     * Gets the amount of literal occurrences generated so far.
     * @return the number of literal occurrences.
     */
    static public int getCumulatedSize()
    {
    	return countLit;
    }
    
    /**
     * Checks whether the current quotas are ok. The quotas related to
     * the current session hide any other ones.
     * @throws QuotaException
     */
    static public void verifQuota() throws QuotaException
    {
    	if(varQuota.empty()&&litQuota.empty())
    		return;
    	
    	int varQ = varQuota.peek();
    	int litQ = litQuota.peek();
    	
    	//---------------------------
    	//System.out.println("--> quota: "+litQ + "taille: "+ (countLit-backupNbLit.peek()));
    	//---------------------------
    	
    	if(varQ!=0)
    	{
    		if(varQ<(Variable.getNbUsed()-backupNbVar.peek()))
    			throw new QuotaException("Variable quota overflow");
    	}
    	if(litQ!=0)
    	{
    		if(litQ<countLit-backupNbLit.peek())
    			throw new QuotaException("Size quota overflow");
    	}
    }
    
    /**
     * Closes a trying session, given that the clauses and variables produced
     * will be abandoned.
     * @param ticket the ticket of the current session.
     */
    static public void abandonTry(int ticket)
    {
    	if(ticket!=currentTicket)
    		throw new RuntimeException("Bad ticket");
    	currentTicket--;
    	Variable.setUsed(backupNbVar.pop());
    	countLit = backupNbLit.pop();
    	varQuota.pop();
    	litQuota.pop();
    }
    
    /**
     * Closes a trying session, given that the clauses and variables produced
     * will be conserved.
     * @param ticket the ticket of the current session.
     */
    static public void commitTry(int ticket)
    {
    	if(ticket!=currentTicket)
    		throw new RuntimeException("Bad ticket");
    	currentTicket--;
    	backupNbVar.pop();
    	backupNbLit.pop();
    	varQuota.pop();
    	litQuota.pop();
    }
}
