// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package dev.shadowtail.classfile.summercoat.register;

/**
 * A register that implements the given type.
 *
 * @param <T> The type used.
 * @since 2020/11/24
 */
public final class TypedRegister<T>
	extends Register
{
	/** The type used. */
	protected final Class<T> type;
	
	/**
	 * Initializes the basic register.
	 *
	 * @param __class The class type used.
	 * @param __register The register to get.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/11/24
	 */
	public TypedRegister(Class<T> __class, int __register)
		throws NullPointerException
	{
		super(__register);
		
		if (__class == null)
			throw new NullPointerException("NARG");
		
		this.type = __class;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/11/24
	 */
	@Override
	public boolean equals(Object __o)
	{
		return super.equals(__o) &&
			this.type == ((TypedRegister<?>)__o).type;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/11/24
	 */
	@Override
	public int hashCode()
	{
		return this.type.hashCode() ^ super.hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/11/24
	 */
	@Override
	public String toString()
	{
		String className = this.type.getName();
		int ld = className.lastIndexOf('.');
		
		return (ld < 0 ? className : className.substring(ld + 1)) +
			"#" + this.register;
	}
}
