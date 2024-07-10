/*[INCLUDE-IF Sidecar18-SE]*/
/*
 * Copyright IBM Corp. and others 2004
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] https://openjdk.org/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
 */
package com.ibm.dtfj.image.j9;

import java.io.File;
import java.io.FileNotFoundException;

public interface IFileLocationResolver
{
	/**
	 * Used to lookup files at runtime.  The fullPath is used as a hint for how to find the file and what
	 * it is called but implementers are free to use whatever RI are necessary to find and return
	 * the adequate file
	 *
	 * @param fullPath The full path to the file
	 * @return An abstract reference to the file, ready to be treated as any other file, by the caller (NEVER NULL)
	 * @throws FileNotFoundException If we couldn't find the file anywhere
	 */
	File findFileWithFullPath(String fullPath) throws FileNotFoundException;
}
