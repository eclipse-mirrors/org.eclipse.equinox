/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.bidi.internal.consumable;

import org.eclipse.equinox.bidi.STextEngine;
import org.eclipse.equinox.bidi.STextEnvironment;
import org.eclipse.equinox.bidi.custom.STextProcessor;
import org.eclipse.equinox.bidi.internal.STextActivator;

/**
 *  <code>STextJava</code> is a processor for structured text
 *  composed of Java statements. Such a structured text may span
 *  multiple lines.
 *  <p>
 *  In applications like an editor where parts of the text might be modified
 *  while other parts are not, the user may want to call
 *  {@link STextEngine#leanToFullText leanToFullText}
 *  separately on each line and save the initial state of each line (this is
 *  the final state of the previous line which can be retrieved using
 *  the value returned in the first element of the <code>state</code> argument).
 *  If both the content
 *  of a line and its initial state have not changed, the user can be sure that
 *  the last <i>full</i> text computed for this line has not changed either.
 *
 *  @see STextEngine#leanToFullText explanation of state in leanToFullText
 *
 *  @author Matitiahu Allouche
 */
public class STextJava extends STextProcessor {
	private static final byte WS = Character.DIRECTIONALITY_WHITESPACE;
	static final String lineSep = STextActivator.getInstance().getProperty("line.separator"); //$NON-NLS-1$

	public STextJava() {
		super("[](){}.+-<>=~!&*/%^|?:,;\t"); //$NON-NLS-1$
	}

	/**
	 *  @return 4 as the number of special cases handled by this processor.
	 */
	public int getSpecialsCount(STextEnvironment environment, String text, byte[] dirProps) {
		return 4;
	}

	/**
	     *  This method looks for occurrences of 4 special strings:
	     *  <ol>
	     *    <li>spaces</li>
	     *    <li>literals starting with quotation mark</li>
	     *    <li>comments starting with slash-asterisk</li>
	     *    <li>comments starting with slash-slash</li>
	     *  </ol>
	     */
	public int indexOfSpecial(STextEnvironment environment, String text, byte[] dirProps, int[] offsets, int caseNumber, int fromIndex) {
		switch (caseNumber) {
			case 1 : /* space */
				return text.indexOf(' ', fromIndex);
			case 2 : /* literal */
				return text.indexOf('"', fromIndex);
			case 3 : /* slash-aster comment */
				return text.indexOf("/*", fromIndex); //$NON-NLS-1$
			case 4 : /* slash-slash comment */
				return text.indexOf("//", fromIndex); //$NON-NLS-1$
		}
		// we should never get here
		return -1;
	}

	/**
	 *  This method processes the 4 special cases as follows.
	     *  <ol>
	     *    <li>skip the run of spaces</li>
	     *    <li>look for a matching quotation mark and skip until after it</li>
	     *    <li>skip until after the closing asterisk-slash</li>
	     *    <li>skip until after a line separator</li>
	     *  </ol>
	 */
	public int processSpecial(STextEnvironment environment, String text, byte[] dirProps, int[] offsets, int[] state, int caseNumber, int separLocation) {
		int location, counter, i;

		STextProcessor.processSeparator(text, dirProps, offsets, separLocation);
		switch (caseNumber) {
			case 1 : /* space */
				separLocation++;
				while (separLocation < text.length() && text.charAt(separLocation) == ' ') {
					STextProcessor.setDirProp(dirProps, separLocation, WS);
					separLocation++;
				}
				return separLocation;
			case 2 : /* literal */
				location = separLocation + 1;
				while (true) {
					location = text.indexOf('"', location);
					if (location < 0)
						return text.length();
					for (counter = 0, i = location - 1; text.charAt(i) == '\\'; i--) {
						counter++;
					}
					location++;
					if ((counter & 1) == 0)
						return location;
				}
			case 3 : /* slash-aster comment */
				if (separLocation < 0) { // continuation line
					location = 0;
				} else
					location = separLocation + 2; // skip the opening slash-aster
				location = text.indexOf("*/", location); //$NON-NLS-1$
				if (location < 0) {
					state[0] = caseNumber;
					return text.length();
				}
				// we need to call processSeparator since text may follow the
				//  end of comment immediately without even a space
				STextProcessor.processSeparator(text, dirProps, offsets, location);
				return location + 2;
			case 4 : /* slash-slash comment */
				location = text.indexOf(lineSep, separLocation + 2);
				if (location < 0)
					return text.length();
				return location + lineSep.length();
		}
		// we should never get here
		return text.length();
	}
}
