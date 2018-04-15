// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.javac.lexical;

/**
 * This represents a class or interface declaration.
 *
 * @since 2018/04/10
 */
public abstract class ClassOrInterfaceDeclaration
{
	/**
	 * {@inheritDoc}
	 * @since 2018/04/15
	 */
	@Override
	public abstract boolean equals(Object __o);
	
	/**
	 * {@inheritDoc}
	 * @since 2018/04/15
	 */
	@Override
	public abstract int hashCode();
	
	/**
	 * {@inheritDoc}
	 * @since 2018/04/15
	 */
	@Override
	public abstract String toString();
}

