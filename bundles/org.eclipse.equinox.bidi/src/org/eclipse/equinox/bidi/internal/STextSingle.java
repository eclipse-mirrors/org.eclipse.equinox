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
package org.eclipse.equinox.bidi.internal;

import org.eclipse.equinox.bidi.STextEnvironment;
import org.eclipse.equinox.bidi.custom.STextProcessor;

/**
 *  A base processor for structured text composed of two parts separated by a separator.
 *  The first occurrence of the separator delimits the end of the first part
 *  and the start of the second part. Further occurrences of the separator,
 *  if any, are treated like regular characters of the second text part.
 *  The processor makes sure that the text be presented in the form
 *  (assuming that the equal sign is the separator):
 *  <pre>
 *  part1=part2
 *  </pre>
 *  The string returned by {@link STextProcessor#getSeparators getSeparators}
 *  for this processor should contain exactly one character.
 *  Additional characters will be ignored.
 *
 *  @author Matitiahu Allouche
 */
public class STextSingle extends STextProcessor {

	public STextSingle(String separator) {
		super(separator);
	}

	/**
	 *  This method locates occurrences of the separator.
	 *
	 *  @see #getSeparators getSeparators
	 */
	public int indexOfSpecial(STextEnvironment environment, String text, byte[] dirProps, int[] offsets, int caseNumber, int fromIndex) {
		return text.indexOf(this.getSeparators(environment, text, dirProps).charAt(0), fromIndex);
	}

	/**
	 *  This method inserts a mark before the separator if needed and
	 *  skips to the end of the source string.
	 *
	 *  @return the length of <code>text</code>.
	 */
	public int processSpecial(STextEnvironment environment, String text, byte[] dirProps, int[] offsets, int[] state, int caseNumber, int separLocation) {
		STextProcessor.processSeparator(text, dirProps, offsets, separLocation);
		return text.length();
	}

	/**
	 *  This method returns 1 as number of special cases handled by this processor.
	 *
	 *  @return 1.
	 */
	public int getSpecialsCount(STextEnvironment environment, String text, byte[] dirProps) {
		return 1;
	}

}
