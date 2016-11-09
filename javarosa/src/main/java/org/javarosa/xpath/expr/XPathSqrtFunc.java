package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathSqrtFunc extends XPathFuncExpr {
    private static final String NAME = "sqrt";
    private static final int EXPECTED_ARG_COUNT = 1;

    public XPathSqrtFunc() {
        name = NAME;
        expectedArgCount = EXPECTED_ARG_COUNT;
    }

    public XPathSqrtFunc(XPathExpression[] args) throws XPathSyntaxException {
        super(NAME, args, EXPECTED_ARG_COUNT, true);
    }

    @Override
    public Object evalBody(DataInstance model, EvaluationContext evalContext) {
        return sqrt(evaluatedArgs[0]);
    }

    /**
     * Returns the square root of the argument, expressed in radians.
     */
    private static Double sqrt(Object o) {
        double value = FunctionUtils.toDouble(o);
        return Math.sqrt(value);
    }
}
