// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jdwp.trips;

import cc.squirreljme.jdwp.JDWPClassStatus;

/**
 * Trips on the state of a class.
 *
 * @since 2021/04/17
 */
public interface JDWPTripClassStatus
	extends JDWPTrip
{
	/**
	 * Signals that the status of a class has been changed.
	 * 
	 * @param __thread The source thread.
	 * @param __which Which class is this being used on?
	 * @param __status The status of the class.
	 * @since 2021/04/17
	 */
	void classStatus(Object __thread, Object __which,
		JDWPClassStatus __status);
}
