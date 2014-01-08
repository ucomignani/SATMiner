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

package boolvar.model;

/**
 * Represents a Boolean variable
 * @author Olivier Bailleux
 */
public class Variable
{
    // Number of used identifiers
    private static int used=0;
    
    // Current identifier
    // (related to the last produced Variable).
    private int id;
    
    Literal posLit = null;
    Literal negLit = null;
    
    
    /**
     * Creates and records a positive literal with the current variable
     */
    void setPosLit()
    {
    	if(posLit==null) posLit = new Literal(this,true);
    }
    
    /**
     * Creates and records a negative literal with the current variable
     */
    void setNegLit()
    {
    	if(negLit==null) negLit = new Literal(this,false);
    }
    
    /**
     * @return the positive form of the current variable as a literal
     */
    public Literal getPosLit()
    {
    	if(posLit==null) posLit = new Literal(this,true);
    	return posLit;
    }
    
    /**
     * @return the negative form of the current variable as a literal
     */
    public Literal getNegLit()
    {
    	if(negLit==null) negLit = new Literal(this,false);
    	return negLit;
    }
    
    public Literal getLit(boolean sign)
    {
    	if(sign) 
    		return getPosLit();
    	else 
    		return getNegLit();
    }

    /**
     * Create a new variable.
     */
    public Variable()
    {
        id = ++used;
    };

    /**
     * Gets the identifier of the current variable.
     * @return The identifier of the current variable.
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Sets the number of used variables.
     * @param newUsed the new number of used variables.
     */
    static public void setUsed(int newUsed)
    {
    	used = newUsed;
    }

    /**
     * Gets the number of variables currently used
     * @return The number of variables currently used.
     */
    static public int getNbUsed()
    {
        return used;
    }
    
    /**
     * Produces a String representing the Variable,
     * for debugging and display purpose.
     * @return resulting String.
     */
    @Override
    public String toString()
    {
            return "V_"+id;
    }
 
}

