package org.javarosa.xpath.analysis;

import org.javarosa.core.model.instance.TreeReference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.annotations.Nullable;

/**
 * Created by amstone326 on 8/11/17.
 */

public abstract class XPathAccumulatingAnalyzer<T> extends XPathAnalyzer {

    protected List<T> accumulatedList;

    public XPathAccumulatingAnalyzer() {
        super();
    }

    public XPathAccumulatingAnalyzer(TreeReference contextRef) {
        super(contextRef);
    }

    @Nullable
    public List<T> accumulateAsList(XPathAnalyzable rootExpression) {
        try {
            rootExpression.applyAndPropagateAnalyzer(this);
            return aggregateResults(new ArrayList<T>());
        } catch (AnalysisInvalidException e) {
            return null;
        }
    }

    @Nullable
    public Set<T> accumulateAsSet(XPathAnalyzable rootExpression) {
        try {
            rootExpression.applyAndPropagateAnalyzer(this);
            Set<T> set = new HashSet<>();
            for (T item : aggregateResults(new ArrayList<T>())) {
                set.add(item);
            }
            return set;
        } catch (AnalysisInvalidException e) {
            return null;
        }
    }

    private List<T> aggregateResults(List<T> aggregated) {
        aggregated.addAll(this.accumulatedList);
        for (XPathAnalyzer subAnalyzer : this.subAnalyzers) {
            ((XPathAccumulatingAnalyzer)subAnalyzer).aggregateResults(aggregated);
        }
        return aggregated;
    }

    // For all AccumulatingAnalyzers, it should be sufficient to handle a current() reference by
    // applying the analyzer separately to the expression itself and to the context ref
    @Override
    public void doAnalysisForTreeRefWithCurrent(TreeReference expressionWithContextTypeCurrent)
            throws AnalysisInvalidException {

        if (getOriginalContextRef() == null) {
            throw new AnalysisInvalidException();
        }

        doNormalTreeRefAnalysis(expressionWithContextTypeCurrent);
        getOriginalContextRef().applyAndPropagateAnalyzer(this);
    }

    // For all AccumulatingAnalyzers, it should be sufficient to handle a relative reference by
    // applying the analyzer separately to the expression itself and to the context ref
    @Override
    public void doAnalysisForRelativeTreeRef(TreeReference expressionWithContextTypeRelative)
            throws AnalysisInvalidException {

        if (getContextRef() == null) {
            throw new AnalysisInvalidException();
        }

        doNormalTreeRefAnalysis(expressionWithContextTypeRelative);

        if (!this.isSubAnalyzer) {
            // Relative refs only introduce something new to analyze if they are in the top-level
            // expression
            getContextRef().applyAndPropagateAnalyzer(this);
        }
    }

}
