/**
 * 
 */
package org.commcare.suite.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.services.Logger;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.XPathSyntaxException;

/**
 * <p>A Menu definition describes the structure of how
 * actions should be provided to the user in a CommCare
 * application.</p>
 * 
 * @author ctsims
 *
 */
public class Menu implements Externalizable {
    DisplayUnit display;
    Vector<String> commandIds;
    String[] commandExprs;
    String id;
    String root;
    XPathExpression relevantExpr;
    
    /**
     * Serialization only!!!
     */
    public Menu() {
        
    }
    
    public Menu(String id, String root, XPathExpression relevantExpr, DisplayUnit display, Vector<String> commandIds, String[] commandExprs) {
        this.id = id;
        this.root = root;
        this.relevantExpr = relevantExpr;
        this.display = display;
        this.commandIds = commandIds;
        this.commandExprs = commandExprs;
    }
    
    /**
     * @return The ID of what menu an option to navigate to
     * this menu should be displayed in.
     */
    public String getRoot() {
        return root;
    }
    
    /**
     * @return A Text which should be displayed to the user as
     * the action which will display this menu.
     */
    public Text getName() {
        return display.getText();
    }
    
    /**
     * @return The ID of this menu. <p>If this value is "root"
     * many CommCare applications will support displaying this
     * menu's options at the app home screen</p> 
     */
    public String getId() {
        return id;
    }

    /**
     * @return A string representing an XPath expression to determine
     * whether or not to display this menu.
     */
    public XPathExpression getRelevantExpr() {
        return relevantExpr;
    }
    
    /**
     * @return The ID of what command actions should be available
     * when viewing this menu.
     */
    public Vector<String> getCommandIds() {
        //UNSAFE! UNSAFE!
        return commandIds;
    }
    
    public XPathExpression getRelevantCondition(int index) throws XPathSyntaxException {
        //Don't cache this for now at all
        return commandExprs[index] == null ? null : XPathParseTool.parseXPath(commandExprs[index]);
    }
    
    /**
     * @param index the 
     * @return the raw xpath string for a relevant condition (if available). Largely for
     * displaying to the user in the event of a failure
     */
    public String getRelevantConditionRaw(int index) {
        return commandExprs[index];
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
     */
    public void readExternal(DataInputStream in, PrototypeFactory pf)
            throws IOException, DeserializationException {
        id = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        root = ExtUtil.readString(in);
        display = (DisplayUnit)ExtUtil.read(in, DisplayUnit.class);
        commandIds = (Vector<String>)ExtUtil.read(in, new ExtWrapList(String.class),pf);
        commandExprs =  new String[ExtUtil.readInt(in)];
        for(int i = 0 ; i < commandExprs.length; ++i) {
            if(ExtUtil.readBool(in)) {
                commandExprs[i] = ExtUtil.readString(in);
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
     */
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeString(out,ExtUtil.emptyIfNull(id));
        ExtUtil.writeString(out,root);
        ExtUtil.write(out, display);
        ExtUtil.write(out, new ExtWrapList(commandIds));
        ExtUtil.writeNumeric(out, commandExprs.length);
        for(int i = 0 ; i < commandExprs.length ; ++i) {
            if(commandExprs[i] == null) {
                ExtUtil.writeBool(out, false);
            } else{
                ExtUtil.writeBool(out, true);
                ExtUtil.writeString(out, commandExprs[i]);
            }
        }
    }


    public String getImageURI() {
        return display.getImageURI();
    }
    
    public String getAudioURI() {
        return display.getAudioURI();
    }
    // unsafe! assumes that xpath expressions evaluate properly...
    public int indexOfCommand(String cmd){
        return commandIds.indexOf(cmd);
    }

}
