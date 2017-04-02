// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.jit;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * This is the part of the JIT which accepts a class file which is parsed and
 * then recompiled to native machine code as it is being processed.
 *
 * @since 2017/04/02
 */
public class JIT
{
	/**
	 * Initializes the JIT processor.
	 *
	 * @param __is The input class file to process.
	 * @param __conf The configuration for the JIT.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/04/02
	 */
	JIT(DataInputStream __is, JITConfig __conf)
		throws NullPointerException
	{
		// Check
		if (__is == null || __conf == null)
			throw new NullPointerException("NARG");
		
		throw new todo.TODO();
	}
}

