package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.xpath.XPathArityException;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XpathCoalesceFunc extends XPathFuncExpr {
    public static final String NAME = "coalesce";
    // at least 1 argument
    private static final int EXPECTED_ARG_COUNT = -1;

    public XpathCoalesceFunc() {
        name = NAME;
        expectedArgCount = EXPECTED_ARG_COUNT;
    }

    public XpathCoalesceFunc(XPathExpression[] args) throws XPathSyntaxException {
        super(NAME, args, EXPECTED_ARG_COUNT, true);
    }

    @Override
    protected void validateArgCount() throws XPathSyntaxException {
        if (args.length < 1) {
            throw new XPathArityException(name, "1 or more arguments", args.length);
        }
    }

    @Override
    public Object evalBody(DataInstance model, EvaluationContext evalContext) {
        // Not sure if unpacking here is quiiite right, but it seems right
        for (int i = 0; i < args.length - 1; i++) {
            Object evaluatedArg = FunctionUtils.unpack(args[i].eval(model, evalContext));
            if (!isNull(evaluatedArg)) {
                return evaluatedArg;
            }
        }
        return args[args.length - 1].eval(model, evalContext);
    }

    private static boolean isNull(Object o) {
        if (o == null) {
            return true; //true 'null' values aren't allowed in the xpath engine, but whatever
        } else if (o instanceof String && ((String)o).length() == 0) {
            return true;
        } else {
            return o instanceof Double && ((Double)o).isNaN();
        }
    }

    @Override
    public String getDocumentation() {
        return getDocHeader()
                + "Behavior:  Useful for choosing which of two values to return.  Will return the non-null/empty value.  If both are not null, will return the first argument.\n"
                + "Return: One of the values\n"
                + "Arguments:  The two values to be coalesced\n"
                + "Syntax: coalesce(value_1, value_2).\n"
                + "Example:  This is useful if you want to use a default value when referring to a question which may or may not have been answered.  Ex. coalesce(/data/my_question, \"my default value\").";
    }
}
