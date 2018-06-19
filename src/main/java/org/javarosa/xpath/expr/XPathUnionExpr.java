package org.javarosa.xpath.expr;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathUnsupportedException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class XPathUnionExpr extends XPathBinaryOpExpr {
    @SuppressWarnings("unused")
    public XPathUnionExpr() {
    } //for deserialization

    public XPathUnionExpr(XPathExpression a, XPathExpression b) {
        super(-1, a, b);
    }

    @Override
    protected Object evalRaw(DataInstance model, EvaluationContext evalContext) {
        throw new XPathUnsupportedException("nodeset union operation");
    }

    @Override
    public String toString() {
        return super.toString("union");
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        readExpressions(in, pf);
        computedCacheability = ExtUtil.readBool(in);
        exprIsCacheable = ExtUtil.readBool(in);
        computedContextTypes = ExtUtil.readBool(in);
        contextRefIsRelevant = ExtUtil.readBool(in);
        originalContextRefIsRelevant = ExtUtil.readBool(in);
        op = -1;
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        writeExpressions(out);
        ExtUtil.writeBool(out, computedCacheability);
        ExtUtil.writeBool(out, exprIsCacheable);
        ExtUtil.writeBool(out, computedContextTypes);
        ExtUtil.writeBool(out, contextRefIsRelevant);
        ExtUtil.writeBool(out, originalContextRefIsRelevant);

    }

    @Override
    public String toPrettyString() {
        return "unsupported union operation";
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) ||
                ((o instanceof XPathUnionExpr) && binOpEquals((XPathBinaryOpExpr)o));
    }
}
