package org.javarosa.xpath.expr;

import org.commcare.util.LogTypes;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.Logger;
import org.javarosa.xpath.analysis.AnalysisInvalidException;
import org.javarosa.xpath.analysis.ContainsUncacheableExpressionAnalyzer;
import org.javarosa.xpath.analysis.ReferencesMainInstanceAnalyzer;
import org.javarosa.xpath.analysis.XPathAnalyzable;

/**
 * Superclass for an XPathExpression that keeps track of all information related to if it can be
 * cached, and contains wrapper functions for all caching operations.
 *
 * @author Aliza Stone
 */
public abstract class InFormCacheableExpr implements XPathAnalyzable {

    private Object justRetrieved;
    protected boolean computedCacheability;
    protected boolean isCacheable;

    boolean isCached(EvaluationContext ec) {
        if (ec.expressionCachingEnabled()) {
            queueUpCachedValue(ec);
            return justRetrieved != null;
        }
        return false;
    }

    private void queueUpCachedValue(EvaluationContext ec) {
        justRetrieved = ec.expressionCacher().getCachedValue(cacheKey(ec));
    }

    /**
     * queueUpCachedValue must always be called first!
     */
    Object getCachedValue() {
        return justRetrieved;
    }

    void cache(Object value, EvaluationContext ec) {
        if (ec.expressionCachingEnabled() && isCacheable(ec)) {
            ec.expressionCacher().cache(cacheKey(ec), value);
        }
    }

    private ExpressionCacheKey cacheKey(EvaluationContext ec) {
        return new ExpressionCacheKey(this, ec.getContextRef());
    }

    protected boolean isCacheable(EvaluationContext ec) {
        if (!computedCacheability) {
            computeCacheability(ec);
        }
        return isCacheable;
    }

    private void computeCacheability(EvaluationContext ec) {
        if (ec.getMainInstance() instanceof FormInstance) {
            try {
                isCacheable =
                        !referencesMainFormInstance(this, (FormInstance)ec.getMainInstance(), ec) &&
                        !containsUncacheableSubExpression(this, ec);
            } catch (AnalysisInvalidException e) {
                // If the analysis didn't complete then we assume it's not cacheable
                isCacheable = false;
            }
            computedCacheability = true;
        } else {
            Logger.log(LogTypes.SOFT_ASSERT,
                    "Caching was enabled in the ec, but the main instance provided " +
                            "to InFormCacheableExpr by the ec was not of type FormInstance: " + ec.getMainInstance());
            isCacheable = false;
        }
    }

    public static boolean referencesMainFormInstance(XPathAnalyzable expr, FormInstance formInstance, EvaluationContext ec) throws AnalysisInvalidException {
        String formInstanceRoot = formInstance.getBase().getChildAt(0).getName();
        return (new ReferencesMainInstanceAnalyzer(formInstanceRoot, ec)).computeResult(expr);
    }

    public static boolean containsUncacheableSubExpression(XPathAnalyzable expr, EvaluationContext ec) throws AnalysisInvalidException {
        return (new ContainsUncacheableExpressionAnalyzer(ec)).computeResult(expr);
    }

}
