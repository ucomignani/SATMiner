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
