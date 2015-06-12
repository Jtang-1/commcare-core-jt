package org.javarosa.core.model.instance.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class QuestionDataElementTests extends TestCase {
    private final String stringElementName = "String Data Element";

    StringData stringData;
    IntegerData integerData;

    TreeElement stringElement;
    TreeElement intElement;

    protected void setUp() throws Exception {
        super.setUp();
        stringData = new StringData("Answer Value");
        integerData = new IntegerData(4);

        intElement = new TreeElement("intElement");
        intElement.setValue(integerData);

        stringElement = new TreeElement(stringElementName);
        stringElement.setValue(stringData);

    }

    public QuestionDataElementTests(String name) {
        super(name);
    }

    public QuestionDataElementTests() {
        super();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(new QuestionDataElementTests("testIsLeaf"));
        suite.addTest(new QuestionDataElementTests("testGetName"));
        suite.addTest(new QuestionDataElementTests("testSetName"));
        suite.addTest(new QuestionDataElementTests("testGetValue"));
        suite.addTest(new QuestionDataElementTests("testSetValue"));
        suite.addTest(new QuestionDataElementTests("testAcceptsVisitor"));
        suite.addTest(new QuestionDataElementTests("testSuperclassMethods"));

        return suite;
    }


    public void testIsLeaf() {
        assertTrue("Question Data Element returned negative for being a leaf", stringElement.isLeaf());
    }

    public void testGetName() {
        assertEquals("Question Data Element 'string' did not properly get its name", stringElement.getName(), stringElementName);
    }

    public void testSetName() {
        String newName = new String("New Name");
        stringElement.setName(newName);

        assertEquals("Question Data Element 'string' did not properly set its name", stringElement.getName(), newName);
    }

    public void testGetValue() {
        IAnswerData data = stringElement.getValue();
        assertEquals("Question Data Element did not return the correct value", data, stringData);
    }

    public void testSetValue() {
        stringElement.setValue(integerData);
        assertEquals("Question Data Element did not set value correctly", stringElement.getValue(), integerData);

        try {
            stringElement.setValue(null);
        } catch (Exception e) {
            fail("Question Data Element did not allow for its value to be set as null");
        }

        assertEquals("Question Data Element did not return a null value correctly", stringElement.getValue(), null);

    }


    private class MutableBoolean {
        private boolean bool;

        public MutableBoolean(boolean bool) {
            this.bool = bool;
        }

        void setValue(boolean bool) {
            this.bool = bool;
        }

        boolean getValue() {
            return bool;
        }
    }

    public void testAcceptsVisitor() {
        final MutableBoolean visitorAccepted = new MutableBoolean(false);
        final MutableBoolean dispatchedWrong = new MutableBoolean(false);
        ITreeVisitor sampleVisitor = new ITreeVisitor() {

            public void visit(FormInstance tree) {
                dispatchedWrong.bool = true;
            }

            public void visit(AbstractTreeElement element) {
                visitorAccepted.bool = true;
            }
        };

        stringElement.accept(sampleVisitor);
        assertTrue("The visitor's visit method was not called correctly by the QuestionDataElement", visitorAccepted.getValue());

        assertTrue("The visitor was dispatched incorrectly by the QuestionDataElement", !dispatchedWrong.getValue());
    }

    private void testSuperclassMethods() {
        //stringElement should not have a root at this point.

        //TODO: Implement tests for the 'attribute' system.
    }
}
