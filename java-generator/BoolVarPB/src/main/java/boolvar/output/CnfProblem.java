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

import java.io.PrintWriter;
import java.io.RandomAccessFile;

import boolvar.model.*;
import boolvar.utility.Quota;
import boolvar.utility.QuotaException;

/**
 * A CnfProblem is a problem that is bound to be translated into raw clauses
 * (i.e., a CNF formula).
 */
public class CnfProblem implements OutputProblem {
	// The CNF formula
	private Appendable cnf;

	// Number of clauses in the formula
	private int nClauses;

	public int getnClauses() {
		return nClauses;
	}

	// Number of literals in the formula
	private int nLits;

	public int getnLits() {
		return nLits;
	}

	/**
	 * Creates an empty CNF problem instance. The dimacs instance is stored in
	 * memory.
	 */
	public CnfProblem() {
		cnf = new StringBuffer("");
		nClauses = 0;
		nLits = 0;
	}

	/**
	 * constructor for writing directly into the destination file
	 * 
	 * @param outputFile
	 *            destination file
	 */
	public CnfProblem(PrintWriter outputFile) {
		cnf = outputFile;

		try {
			// reserve space for the metadata
			cnf.append("p cnf                      \n");
		} catch (Exception e) {
			throw new RuntimeException("failed to generate 'p cnf' line");
		}

		nClauses = 0;
		nLits = 0;
	}

	// Adds a new clause to the formula
	private void post(Clause q) {
        // Changed for abstracting dimacs output (needed to handle strong backdoor clauses)
        String result = q.dimacsLine();
        try {
			cnf.append(result);
		} catch (Exception e) {
			throw new RuntimeException("I/O problem");
		}

		nClauses++;
		nLits += q.size();
	}

	// Adds a new formula to the problem
	private void post(CNFformula f) {
		for (int i = 0; i < f.size(); i++)
			post(f.getClause(i));
	}

	/**
	 * Adds the constraints of a model to the problem.
	 * 
	 * @param m
	 *            the model to read.
	 */
	public void read(Model m) {
		for (int i = 0; i < m.size(); i++)
			try {
				post(m.getConstraint(i).getCNF());
			} catch (QuotaException e) {
				e.printStackTrace();
				throw new RuntimeException("Resource quota exceeded");
			}
	}

	public void read(Model m, int maxVar, int maxSize) throws QuotaException {
		int ticket = Quota.beginTry();
		Quota.setMaxVar(maxVar, ticket);
		Quota.setMaxLit(maxSize, ticket);
		try {
			for (int i = 0; i < m.size(); i++)
				post(m.getConstraint(i).getCNF());
		} catch (QuotaException e) {
			Quota.abandonTry(ticket);
			throw new QuotaException("Resource quota exceeded");
		}
		Quota.commitTry(ticket);
	}

	/**
	 * Gives the CNF representation of the problem in DIMACS format
	 * 
	 * @return the CNF formula as a string
	 */
	public String getOutput() {
		return "p cnf " + Variable.getNbUsed() + " " + nClauses + "\n"
				+ cnf.toString();
	}

	/**
	 * When we write driectly to the file, write the 'p cnf' header into the
	 * dimacs file (once we have output all the clauses). Enough space must have
	 * been reserved in the file.
	 * 
	 * @param fileName
	 *            name of the dimacs file
	 */
	public void writeCNFLine(String fileName) {
		try {
			RandomAccessFile f = new RandomAccessFile(fileName, "rw");

			f.writeBytes("p cnf " + Variable.getNbUsed() + " " + nClauses);

			f.close();
		} catch (Exception e) {
			throw new RuntimeException("failed to update 'p cnf' line");
		}
	}
}
