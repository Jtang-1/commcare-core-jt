package org.commcare.cases.instance;

import org.commcare.cases.model.StorageBackedModel;
import org.commcare.core.interfaces.UserSandbox;
import org.commcare.modern.util.Pair;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.InstanceBase;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.xpath.expr.XPathPathExpr;

import java.util.Hashtable;

/**
 * @author Phillip Mates (pmates@dimagi.com)
 */
public class FlatFixtureInstanceTreeElement extends StorageInstanceTreeElement<StorageBackedModel, FixtureChildElement> {

    private FlatFixtureInstanceTreeElement(AbstractTreeElement instanceRoot,
                                          IStorageUtilityIndexed<StorageBackedModel> storage,
                                          String modelName, String childName) {
        super(instanceRoot, storage, modelName, childName);
    }

    public static FlatFixtureInstanceTreeElement get(UserSandbox sandbox,
                                                     InstanceBase instanceBase) {
        Pair<String, String> modelAndChild = sandbox.getFlatFixturePathBases(instanceBase.getInstanceName());
        IStorageUtilityIndexed<StorageBackedModel> storage =
                sandbox.getFlatFixtureStorage(instanceBase.getInstanceName(), null);
        return new FlatFixtureInstanceTreeElement(instanceBase, storage, modelAndChild.first, modelAndChild.second);
    }

    @Override
    protected FixtureChildElement buildElement(StorageInstanceTreeElement<StorageBackedModel, FixtureChildElement> storageInstance,
                                               int recordId, String id, int mult) {
        return new FixtureChildElement(storageInstance, mult, recordId);
    }

    @Override
    protected FixtureChildElement getChildTemplate() {
        return FixtureChildElement.buildFixtureChildTemplate(this);
    }

    @Override
    protected Hashtable<XPathPathExpr, String> getStorageIndexMap() {
        return null;
    }
}
