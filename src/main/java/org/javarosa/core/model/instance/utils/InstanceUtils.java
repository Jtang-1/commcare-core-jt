package org.javarosa.core.model.instance.utils;

import static org.javarosa.core.model.instance.utils.TreeUtilities.xmlToTreeElement;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xml.util.InvalidStructureException;

import java.io.IOException;

/**
 * Collection of static instance loading methods
 *
 * @author Phillip Mates
 */
public class InstanceUtils {

    public static FormInstance loadFormInstance(String formFilepath)
            throws InvalidStructureException, IOException {
        TreeElement root = xmlToTreeElement(formFilepath);
        return new FormInstance(root, null);
    }
}
