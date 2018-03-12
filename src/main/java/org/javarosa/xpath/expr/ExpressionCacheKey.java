package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.analysis.TreeReferenceAccumulatingAnalyzer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by amstone326 on 2/23/18.
 */

public class ExpressionCacheKey {

    private InFormCacheableExpr originalExpression;
    private Set<TreeReference> contextualizedRefs;

    public ExpressionCacheKey(InFormCacheableExpr expr, EvaluationContext ec) {
        this.originalExpression = expr;
        contextualizedRefs = new TreeReferenceAccumulatingAnalyzer(ec).accumulate(originalExpression);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExpressionCacheKey) {
            ExpressionCacheKey other = (ExpressionCacheKey)o;

            // potential shortcut, so try this first
            if (contextualizedRefs.size() != other.contextualizedRefs.size()) {
                return false;
            }

            return originalExpression.equals(other.originalExpression) &&
                    contextualizedRefs.equals(other.contextualizedRefs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int argsHash = 0;
        for (TreeReference ref : contextualizedRefs) {
            argsHash ^= ref.hashCode();
        }
        return originalExpression.hashCode() ^ argsHash;
    }

}
