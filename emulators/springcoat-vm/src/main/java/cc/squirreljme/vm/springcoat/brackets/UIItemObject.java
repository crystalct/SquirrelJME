// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.springcoat.brackets;

import cc.squirreljme.jvm.mle.brackets.UIItemBracket;
import cc.squirreljme.vm.springcoat.AbstractGhostObject;

/**
 * This wraps a native {@link UIItemBracket}.
 *
 * @since 2020/07/18
 */
public final class UIItemObject
	extends AbstractGhostObject
{
	/** The item to wrap. */
	public final UIItemBracket item;
	
	/**
	 * Initializes the item object.
	 * 
	 * @param __item The item to wrap.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/07/18
	 */
	public UIItemObject(UIItemBracket __item)
		throws NullPointerException
	{
		if (__item == null)
			throw new NullPointerException("NARG");
		
		this.item = __item;
	}
}
