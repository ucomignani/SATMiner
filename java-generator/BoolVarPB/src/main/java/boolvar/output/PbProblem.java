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

import boolvar.model.*;
import java.math.BigInteger;

import boolvar.model.Variable;

/**
 * A PbProblem is a problem that is bound to be translated into
 * raw pseudo Boolean constraints.
 */
public class PbProblem implements OutputProblem
{
    private StringBuffer output;
    
    private int nTerms;
    
/**
 * Creates a pseudo Boolean problem instance.
 */
    public PbProblem()
    {
        output = new StringBuffer("");
        nTerms = 0;
    }
    
    //Adds a new pseudo Boolean constraint to the problem
    private void post(PBconst t)
    {
        String s = "";
        for(int i=0; i<t.size(); i++)
        {
            if(t.getCoeff(i).compareTo(BigInteger.ZERO)>0) s += "+";
            s += (t.getCoeff(i).toString() + " x");
            s += (t.getVariable(i).getId() + " ");
        }
        if(t.getOperator()==2)
            s += ">";
        s += "= ";
        s += (t.getBound().toString() + ";\n");
        output.append(s);
        nTerms++;
    }
    
    //Adds several new pseudo Boolean contraints to the problem
    private void post(PBformula f)
    {
        for(int i=0; i<f.size(); i++)
            post(f.getPBconst(i));
    }
    
    
 /**
 * Adds the constraints of a model to the problem.
 * @param m the model to read.
 */
    public void read(Model m)
    {
        for(int i=0; i<m.size(); i++)
            post(m.getConstraint(i).getPB());
    }
    
/**
 * Produces the pseudo Boolean problem as a string, in opb format.
 * @return the string representing the problem.
 */    
    public String getOutput()
    {
        String start = "* #variable= " + Variable.getNbUsed() + 
                       " #constraint= "+nTerms+" *\n";
        return start + output.toString();
    }
}