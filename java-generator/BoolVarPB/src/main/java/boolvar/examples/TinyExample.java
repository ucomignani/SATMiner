/*
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

package boolvar.examples;

import boolvar.model.*;
import boolvar.model.constraints.*;
import boolvar.output.*;

/**
 * A minimalistic example of translating a pseudo Boolean constraint
 * into CNF.
 * @author O. Bailleux
 */
public class TinyExample
{
    public static void example()
    {
        //You must first create an input model
        Model m = new Model();

        //Now, you can specify a pseudoBoolean constraint
        //for example 5 x_1 + 3 neg(x_2) + x_3 <= 8

        //You need three variables x_1, x_2 and x_3
        Variable[] x = new Variable[3];

        //Dont forget to create each variable
        for(int i=0; i<3; i++) x[i] =  new Variable();

        //You must then specify the three literals of the constraint
        Literal[] lits = new Literal[3];
        lits[0] = x[0].getPosLit();
        lits[1] = x[1].getNegLit();
        lits[2] = x[2].getPosLit();

        //The coefficients must be pushed into an array
        int[] coeffs = {5,3,1};

        //You are ready to build the constraint
        Constraint q = new PBconstraint(lits,coeffs,8);

        //You can specify the encoding variant that must be used
        //For example, GLOBC stands for the GPW thechnic presented
        //in the SAT 2009 Paper "New Encodings of Pseudo-Boolean
        //Constraints into CNF " from O. Bailleux, Y. Boufkhad and
        //O. Roussel.
        PBconstraint.setVariant(PBconstraint.GLOBC);

        //You must post the constraint to the model
        m.post(q);
        //(You can post as many constraint as you want into the model)

        //You can print the model for verification purpose
        System.out.println(m.toString());

        //It is time to create an output problem instance
        //Because we aim to translate the constraint into CNF,
        //we choose a CnfProblem.
        OutputProblem out = new CnfProblem();

        //Now you can link the model to the output problem.
        out.read(m);

        //The SAT instance in DIMACS format can be obtained
        //in the following way:
        System.out.print(out.getOutput());
    }

    /*
    public static void main(String[] args)
    {
        example();
    }
    */
}
