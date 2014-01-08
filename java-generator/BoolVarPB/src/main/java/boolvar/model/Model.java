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

import java.util.ArrayList;

import boolvar.model.constraints.Constraint;

/**
 * A Model specify a problem with high level (user) constraints
 * @author Olivier Bailleux
 */
public class Model 
{
    ArrayList<Constraint> constraints;

    /**
     * Creates a new empty Model.
     */
    public Model()
    {
        constraints = new ArrayList<Constraint>();
    }
    
    /**
     * Gives the number of constraints in the model.
     * @return The number of constraints in the model.
     */
    public int size()
    {
        return constraints.size();
    }
    
    /**
     * Gets a constraint.
     * @param i The rank of the desired constraint.
     * @return The desired constraint.
     */
    public Constraint getConstraint(int i)
    {
        return constraints.get(i);
    }
    
    /**
     * Adds a new constraint to the model.
     * @param q The constraint to add.
     */
    public void post(Constraint q)
    {
        constraints.add(q);
    }
    
    /**
     * Creates a string representing the model
     * for debugging or display purpose.
     * @return the resulting string.
     */
    @Override
    public String toString()
    {
        String output = "";
        for(int i=0; i<constraints.size(); i++)
            output += (constraints.get(i).toString()+"\n");
        return output;
    }
}
