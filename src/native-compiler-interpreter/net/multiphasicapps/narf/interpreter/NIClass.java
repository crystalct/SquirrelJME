// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU Affero General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.narf.interpreter;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import net.multiphasicapps.descriptors.ClassNameSymbol;
import net.multiphasicapps.narf.library.NLClass;

/**
 * This represents a class which is loaded by the interpreter.
 *
 * @since 2016/04/21
 */
public class NIClass
{
	/** The interpreter core. */
	protected final NICore core;
	
	/** The based class (if {@code null} is a virtual class). */
	protected final NLClass base;
	
	/** Is this class fully loaded? */
	protected final boolean loaded;
	
	/** The name of this class. */
	protected final ClassNameSymbol thisname;
	
	/** The super class of this class. */
	protected final NIClass superclass;
	
	/**
	 * Initializes an interpreted class.
	 *
	 * @param __core The core.
	 * @param __base The class to base off.
	 * @param __cns The name of the class.
	 * @param __tm The map to place a partially loaded class within.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/04/21
	 */
	NIClass(NICore __core, NLClass __base,
		ClassNameSymbol __cns,
		Map<ClassNameSymbol, Reference<NIClass>> __tm)
		throws NullPointerException
	{
		// Check
		if (__core == null || __base == null || __tm == null || __cns == null)
			throw new NullPointerException("NARG");
		
		// Set
		core = __core;
		base = __base;
		
		// {@squirreljme.error NI0b The class which was read differs by name
		// with the class that is to be loaded. (The loaded class name; The
		// requested class)}
		thisname = __base.thisName();
		if (!__cns.equals(thisname))
			throw new NIException(core, NIException.Type.CLASS_NAME_MISMATCH,
				String.format("NI0b %s %s", thisname, __cns));
		
		// DEBUG
		System.err.printf("DEBUG -- Init class %s%n", thisname);
		
		// Place into the given map, it would be partially loaded at this time
		__tm.put(__cns, new WeakReference<>(this));
		
		// Obtain the superclass of this class
		ClassNameSymbol sn = __base.superName();
		if (sn == null)
			superclass = null;
		else
		{
			// Load it
			superclass = __core.initClass(sn);
			
			// {@squirreljme.error NI0c The current class eventually extends
			// itself. (The name of this class)}
			for (NIClass rover = superclass; rover != null;
				rover = rover.superclass)
				if (rover == this)
					throw new NIException(core,
						NIException.Type.CLASS_CIRCULARITY,
						String.format("NI0c %s", thisname));
		}
		
		// Class loaded
		loaded = true;
	}
	
	/**
	 * Is this class fully loaded?
	 *
	 * @return {@code true} if it is loaded.
	 * @since 2016/04/22
	 */
	public boolean isLoaded()
	{
		return loaded;
	}
}

