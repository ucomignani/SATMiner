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
 * A Literal is a signed Boolean variable.
 */
public class Literal 
{
    Variable var;
    Boolean sign;
    
    public final static Literal TRUE = new Literal(null,true);
    public final static Literal FALSE = new Literal(null,false);

    /**
     * Creates a literal.
     * @param v variable.
     * @param s sign.
     */
    public Literal(Variable v, Boolean s)
    {
            var=v;
            sign=s;
    }

    /**
     * Gets the variable of the literal.
     * @return the variable.
     */
    public Variable getVariable()
    {
            return var;
    }

    /**
     * Gets the sign of the literal.
     * @return the sign.
     */
    public Boolean getSign()
    {
            return sign;
    }

    /**
     * Creates the opposite of the literal
     * (i.e., same variable, opposite sign).
     * @return the new literal.
     */
    public Literal neg()
    {
        //System.out.println("--> "+var+" "+sign);
    	if(var==null)
    	{
    		if(sign)
    			return FALSE;
    		else
    			return TRUE;
    	}
    	return var.getLit(!sign);
    }

    /**
     * Creates a string representing the literal
     * for debugging or display purpose.
     * @return the resulting string.
     */
    @Override
    public String toString()
    {
    		if(var==null)
    		{
    			if(sign)
    				return "T";
    			else
    				return "F";
    		}
            if(sign==false)
                    return "-"+var.toString();
            else
                    return var.toString();
    }
    
    @Override 
    public boolean equals(Object other)
    {
        if(other instanceof Literal)
        {
            Literal otherLit = (Literal)other;
            if(sign!=otherLit.sign)
                return false;   
            return (otherLit.var==var);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.var != null ? this.var.hashCode() : 0);
        hash = 37 * hash + (this.sign != null ? this.sign.hashCode() : 0);
        return hash;
    }
}
