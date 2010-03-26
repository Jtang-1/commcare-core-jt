/**
 * 
 */
package org.commcare.util;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.RawRoot;
import org.javarosa.core.reference.Reference;

/**
 * @author ctsims
 *
 */
public class JavaHttpRoot implements RawRoot {

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.RawRoot#derive(java.lang.String)
	 */
	public Reference derive(String URI) throws InvalidReferenceException {
		return new JavaHttpReference(URI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.RawRoot#derive(java.lang.String, java.lang.String)
	 */
	public Reference derive(String URI, String context) throws InvalidReferenceException {
		context = context.substring(0, context.lastIndexOf('/'));
		return new JavaHttpReference(context + URI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.RawRoot#derives(java.lang.String)
	 */
	public boolean derives(String URI) {
		URI = URI.toLowerCase();
		if(URI.startsWith("http://") || URI.startsWith("https://")) {
			return true;
		}
		return false;
	}

}
