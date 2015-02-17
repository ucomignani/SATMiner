/* ./satmining-backend/src/test/java/dag/satmining/backend/sat4j/SimpleUnitPropagation.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.sat4j.pb.core;

import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.posLit;
import static org.sat4j.core.LiteralsUtils.toDimacs;
import static org.sat4j.core.LiteralsUtils.neg;

import java.util.Iterator;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author ucomignani
 */
public class SimpleUnitPropagation {

	private int idHead;
	private IVec<VecInt> constrs;
	private IVec<VecInt> learnts;

	public SimpleUnitPropagation(){
		this.idHead = 0;
	}


	IVec<VecInt> initStructureClauses(IVec<Constr> in){
		IVec<VecInt> res = new Vec<VecInt>();
		Iterator<Constr> it = in.iterator();

		while(it.hasNext())
		{
			res.push(it.next().toVector());
		}
		return res;
	}

	/**
	 * @param p
	 * 			literal a propager
	 * 
	 * @return liste des literaux propages. Si un conflit apparait, retourne null
	 */

	public VecInt simplePropagation(int p, IVecInt trail, ILits vocIn, IVec<Constr> constrsIn, IVec<Constr> learntsIn){

		// on passe a une structure vectorielle specifique car la structure fournie par le solveur ne permet pas l'acces en ecriture aux litteraux
		// TODO re-tester si une implementation avec la propagation propre au solveur ne serait pas plus efficace et sans risques de fausser les stats
		this.constrs = initStructureClauses(constrsIn);
		this.learnts = initStructureClauses(learntsIn);

		VecInt propagatedLiterals = new VecInt();
		propagatedLiterals.push(p);
		
		IteratorInt literalVec = null;
		int literal;
		VecInt vecRes = null;

		// detection point fixe et insertion des literaux resultats dans le vecteur sans doublons
		while(propagatedLiterals != null && idHead < propagatedLiterals.size())
		{
			vecRes = propagate(propagatedLiterals.get(idHead), this.constrs);
			if(vecRes != null)
			{
				literalVec = vecRes.iterator();
				while(literalVec.hasNext())
				{
					literal = literalVec.next();
					if(!propagatedLiterals.contains(literal))
						propagatedLiterals.push(literal);
				}
			}
			else
			{
				return null;
			}

			vecRes = propagate(propagatedLiterals.get(idHead), this.learnts);
			if(vecRes != null)
			{
				literalVec = vecRes.iterator();
				while(literalVec.hasNext())
				{
					literal = literalVec.next();
					if(!propagatedLiterals.contains(literal))
						propagatedLiterals.push(literal);
				}
			}
			else
			{
				return null;
			}
			
			idHead++;
		}

		return propagatedLiterals;
	}

	private VecInt propagate(int p, IVec<VecInt> vectorClauses){
		VecInt clauseTmp = null;
		VecInt res = new VecInt();
		
		IVec<VecInt> vectorClausesTmp = new Vec<VecInt>(); //on itere sur une copie afin d'eviter de pouvoir faire les remove dans vectorClauses a la volee et sans decalage a gerer
		vectorClauses.copyTo(vectorClausesTmp);
		
		Iterator<VecInt> itClauses = vectorClausesTmp.iterator();
		
		while(itClauses.hasNext())
		{
			clauseTmp = itClauses.next();

			VecInt clauseAvant = new VecInt();
			clauseTmp.copyTo(clauseAvant);;

			clauseTmp = simplifyClause(p, vectorClauses, clauseTmp);
					
			if(isEmptyClause(clauseTmp))
				return null;	
			
			if(isClauseUnit(clauseTmp))
				res.push(clauseTmp.get(0));
		}
		
		return res;
	}

	private VecInt simplifyClause(int p, IVec<VecInt> vectorClauses, VecInt clauseTmp) {
		IteratorInt itInt = clauseTmp.iterator();
		VecInt clauseTmp2 = new VecInt();
		int literalCourant;
		boolean isPropagated = false;

		while(itInt.hasNext() && isPropagated == false)
		{
			literalCourant = itInt.next();

			
			if(isRemovableLiteral(p, literalCourant))
			{
				clauseTmp.copyTo(clauseTmp2);
				clauseTmp2.remove(literalCourant);
				vectorClauses.remove(clauseTmp);
				vectorClauses.push(clauseTmp2);

				clauseTmp = clauseTmp2;
			}
			
			if(isRemovableClause(p, literalCourant))
			{
				vectorClauses.remove(clauseTmp);
				isPropagated = true;
			}
		}

		return clauseTmp;

	}

	private boolean isRemovableClause(int p, int literalCourant) {
		return p == literalCourant;

	}
	
	private boolean isRemovableLiteral(int p, int literalCourant) {
		return neg(p) == literalCourant;

	}
	
	private boolean isClauseUnit(VecInt clauseTmp) {
		return clauseTmp.size() == 1;		
	}

	private boolean isEmptyClause(VecInt clauseTmp) {
		return clauseTmp.size() == 0;
	}
}
