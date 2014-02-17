/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/Sugar.java

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

package dag.satmining.problem.rql.ast;

import java.util.Collection;

public class Sugar {

	private static int counter = 0;

	private static String freshVar() {
		return "$$" + (counter++);
	}

	public static MiningExpression singleton(ASTDictionnary dict,
			String schemaVar) {
		String v1 = freshVar();
		String v2 = freshVar();
		String v3 = freshVar();
		dict.getAttributeVariable(v1);
		dict.getAttributeVariable(v2);
		dict.getAttributeVariable(v3);
		return dict.and(
				dict.exists(v1, schemaVar, dict.tt()),
				dict.forall(v2, schemaVar,
						dict.forall(v3, schemaVar, dict.attCmp(v2, v3))));
	}

	public static MiningExpression emptyIntersection(ASTDictionnary dict,
			String... sv) {
		MiningExpression e = dict.tt();
		for (int i = 0; i < sv.length - 1; i++) {
			for (int j = i + 1; j < sv.length; j++) {
				String sv1 = sv[i];
				String sv2 = sv[j];
				String av1 = freshVar();
				String av2 = freshVar();
				dict.getAttributeVariable(av1);
				dict.getAttributeVariable(av2);
				MiningExpression e2 = dict.forall(av1, sv1,
						dict.forall(av2, sv2, dict.neg(dict.attCmp(av1, av2))));
				e = dict.and(e, e2);
			}
		}
		return e;
	}

	public static MiningExpression emptyIntersection(ASTDictionnary dict,
			Collection<String> sv) {
		return emptyIntersection(dict, sv.toArray(new String[sv.size()]));
	}

}
