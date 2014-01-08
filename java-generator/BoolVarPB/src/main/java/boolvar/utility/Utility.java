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
import java.util.ArrayList;

/**
 * This class provides utility methods for managing arrays and
 * collections of literals and variables.
 */
public abstract class Utility 
{
    /**
     * Creates a compact Literal array, where all the Literals
     * have consecutive indices, starting from 0.
     * @param L input Array of Literals (can be sparse).
     * @return resulting Array of Literals.
     */
    static public Literal[] makeLiterals(Literal[] L)
    {
            return compact(L);
    }

    /**
     * Creates a compact Array of Literals from a possibly
     * sparse Array of Variables.
     * @param V input Array of Variables (can be sparse).
     * @return resulting Array of Literals.
     */
    static public Literal[] makeLiterals(Variable[] V)
    {
            V = compact(V);
            Literal[] L = new Literal[V.length];
            for(int i=0; i<V.length; i++)
                    L[i] = V[i].getPosLit();
            return L;
    }

    /**
     * Creates a compact Array of Literals either from an
     * ArrayList of Literals or from an ArrayList of Variables.
     * @param T input ArrayList.
     * @return resulting Array of Literals.
     */
    static public Literal[] makeLiterals(ArrayList<?> T)
    {
            if(T.get(0).getClass()==Variable.class)
            {
                    return makeLiterals(T.toArray(new Variable[]{}));
            }
            if(T.get(0).getClass()==Literal.class)
            {
                    return T.toArray(new Literal[]{});
            }
            throw new RuntimeException("Format d'entree incorrect");
    }

    /**
     * Creates a compact Array of Variable from a possibly spase one.
     * @param V input Array of Variables.
     * @return resulting Array of Variables.
     */
    static public Variable[] makeVariables(Variable[] V)
    {
            return compact(V);
    }

    /**
     * Creates a compact Array of Variables from an
     * ArrayList of Variables.
     * @param V input ArrayList of Variables.
     * @return resulting Array of Variables.
     */
    static public Variable[] makeVariables(ArrayList<Variable> V)
    {
            return V.toArray(new Variable[]{});
    }

    /**
     * Remove null values from an array of variables.
     * @param in	Array of variables to compact.
     * @return		Compacted array of variables.
     */
    static private Variable[] compact(Variable[] in)
    {
            int count=0;
            for(int i=0; i<in.length; i++)
                    if(in[i] != null)
                            count++;
            if(count!=0)
            {
                    Variable[] out = new Variable[count];
                    count=0;
                    for(int i=0; i<in.length; i++)
                            if(in[i] != null)
                                    out[count++] = in[i];
                    return out;
            }
            else
                    return in;
    }

    /**
     * Remove null values from an array of literals.
     * @param in	Array of literals to compact.
     * @return		Compacted array of literals.
     */
    static private Literal[] compact(Literal[] in)
    {
            int count=0;
            for(int i=0; i<in.length; i++)
                    if(in[i] != null)
                            count++;
            if(count!=0)
            {
                    Literal[] out = new Literal[count];
                    count=0;
                    for(int i=0; i<in.length; i++)
                            if(in[i] != null)
                                    out[count++] = in[i];
                    return out;
            }
            else
                    return in;
    }
}
