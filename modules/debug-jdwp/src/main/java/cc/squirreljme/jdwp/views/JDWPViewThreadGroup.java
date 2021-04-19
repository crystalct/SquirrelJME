// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jdwp.views;

/**
 * A view of thread groups.
 *
 * @since 2021/04/10
 */
public interface JDWPViewThreadGroup
	extends JDWPViewValidObject
{
	/**
	 * Finds a type of the given name from this thread group.
	 * 
	 * @param __which Which thread group?
	 * @param __name The name of the type to find.
	 * @return The found type.
	 * @since 2021/04/18
	 */
	Object findType(Object __which, String __name);
	
	/**
	 * Returns the name of the thread group.
	 * 
	 * @param __which Which thread group to get the name of?
	 * @return The name of the group.
	 * @since 2021/04/10
	 */
	String name(Object __which);
	
	/**
	 * Returns the threads which are a part of this group.
	 * 
	 * @param __which The object being referred to as a thread group.
	 * @return The threads that are part of this thread group.
	 * @since 2021/04/10
	 */
	Object[] threads(Object __which);
}
