/* /Benchmark/src/main/java/org/Benchmark/Main.java

   Copyright (C) 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package org.Benchmark;

import dag.satmining.backend.sat4j.SAT4JPBBuilder;
import dag.satmining.backend.sat4j.SAT4JPBBuilderPBCPGUIDE;
import dag.satmining.backend.sat4j.SAT4JPBBuilderPBCPGUIDE_T;
import dag.satmining.backend.sat4j.SAT4JPBBuilderPGUIDE;
import dag.satmining.backend.sat4j.SAT4JPBBuilderPRAND;

public class Main {

	public static void main(String[] args) throws Exception {
		
		if(!(args.length == 2 || args.length == 3))
		{
			System.out.println("Erreur, mauvais nombre d'arguments...");
			System.exit(1);
		}

		String premierArg = args[0];
		String secondArg = args[1];
		SAT4JPBBuilder sat4jHandler = null;
		
		switch(premierArg)
		{
		case "BASE":
			sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
			break;
		case "PRAND":
			sat4jHandler = new SAT4JPBBuilderPRAND(SAT4JPBBuilderPRAND.SMALL);
			break;
		case "PGUIDE":
			sat4jHandler = new SAT4JPBBuilderPGUIDE(SAT4JPBBuilderPGUIDE.SMALL);
			break;
		case "PBCPGUIDE":
			sat4jHandler = new SAT4JPBBuilderPBCPGUIDE(SAT4JPBBuilderPBCPGUIDE.SMALL);
			break;
		case "PBCPGUIDE_T":
			sat4jHandler = new SAT4JPBBuilderPBCPGUIDE_T(SAT4JPBBuilderPBCPGUIDE_T.SMALL, Integer.parseInt(secondArg));
			secondArg = args[2];
			break;
		default:
			System.out.println("Algorithme inconnu.");
			System.exit(1);
		}

		Benchmark benchmark = new Benchmark(sat4jHandler);
		System.out.println("Fichier utilise: " + secondArg);
		benchmark.bench(secondArg);
	}
}
