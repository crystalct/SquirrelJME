// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.plugin.multivm;

import org.gradle.api.Task;
import org.gradle.api.specs.Spec;

/**
 * This always evaluates to false.
 *
 * @since 2020/10/31
 */
public class AlwaysFalse
	implements Spec<Task>
{
	/**
	 * {@inheritDoc}
	 * @since 2020/10/07
	 */
	@Override
	public boolean isSatisfiedBy(Task __task)
	{
		return false;
	}
}
