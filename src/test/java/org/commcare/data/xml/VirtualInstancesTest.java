package org.commcare.data.xml;

import com.google.common.collect.ImmutableMap;

import org.javarosa.core.model.instance.ExternalDataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class VirtualInstancesTest {
    @Test
    public void testBuildSearchInputRoot()
            throws UnfullfilledRequirementsException, XmlPullParserException,
            InvalidStructureException, IOException {
        String ref = "results";
        String instanceId = VirtualInstances.makeSearchInputInstanceID(ref);
        ExternalDataInstance instance = VirtualInstances.buildSearchInputInstance(ref, ImmutableMap.of(
                "key0", "val0",
                "key1", "val1",
                "key2", "val2"
        ));
        String expectedXml = String.join(
                "",
                "<input id=\"search-input:results\">",
                "<field name=\"key0\">val0</field>",
                "<field name=\"key1\">val1</field>",
                "<field name=\"key2\">val2</field>",
                "</input>"
        );
        TreeElement expected = ExternalDataInstance.parseExternalTree(
                new ByteArrayInputStream(expectedXml.getBytes(StandardCharsets.UTF_8)),
                instanceId
        );
        assertEquals(expected, instance.getRoot());
    }
}
