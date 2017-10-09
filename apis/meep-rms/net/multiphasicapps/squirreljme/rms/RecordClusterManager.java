// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.rms;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;
import net.multiphasicapps.squirreljme.midlet.ActiveMidlet;
import net.multiphasicapps.squirreljme.midlet.MidletSuiteName;
import net.multiphasicapps.squirreljme.midlet.MidletSuiteVendor;

/**
 * This is the base class for a manager which provides access to a record
 * store.
 *
 * The project {@code squirreljme-rms-file} provides an implementation of this
 * class which uses the host filesystem to store records.
 *
 * @since 2017/02/27
 */
public abstract class RecordClusterManager
{
	/** The current owner. */
	private static volatile Reference<RecordStoreOwner> _THIS_OWNER;
	
	/** Current opened record clusters. */
	private final Map<RecordStoreOwner, RecordCluster> _clusters =
		new HashMap<>();
	
	/**
	 * Internally opens the record cluster for the given suite.
	 *
	 * It is guaranteed that only a single cluster is being opened.
	 *
	 * @param __o The cluster to open.
	 * @return The opened cluster.
	 * @throws NullPointerException On null arguments.
	 * @throws RecordStoreException If there is a problem with the record
	 * system.
	 * @since 2017/02/28
	 */
	protected abstract RecordCluster internalOpen(RecordStoreOwner __o)
		throws NullPointerException, RecordStoreException;
	
	/**
	 * Opens the cluster manager for the given suite.
	 *
	 * @param __o The suite to open the cluster for, if it is already open
	 * then the existing cluster will be returned.
	 * @return The opened cluster.
	 * @throws NullPointerException On null arguments.
	 * @throws RecordStoreException If there is a problem with the record
	 * system.
	 * @since 2017/02/27
	 */
	public final RecordCluster open(RecordStoreOwner __o)
		throws NullPointerException, RecordStoreException
	{
		// Check
		if (__o == null)
			throw new NullPointerException("NARG");
		
		// Lock on clusters
		Map<RecordStoreOwner, RecordCluster> clusters = this._clusters;
		synchronized (clusters)
		{
			RecordCluster rv = clusters.get(__o);
			if (rv == null)
				clusters.put(__o, (rv = internalOpen(__o)));
			
			return rv;
		}
	}
	
	/**
	 * Returns the owner for the current MIDlet.
	 *
	 * @return The owner for the current midlet.
	 * @since 2017/02/28
	 */
	public static RecordStoreOwner thisOwner()
	{
		Reference<RecordStoreOwner> ref = _THIS_OWNER;
		RecordStoreOwner rv;
		
		// Cache?
		if (ref == null || null == (rv = ref.get()))
		{
			// Need to build the MIDlet name
			MIDlet mid = ActiveMidlet.get();
			
			// {@squirreljme.error DC02 Could not get the name and/or
			// vendor of the current MIDlet}
			String name = mid.getAppProperty("midlet-name"),
				vend = mid.getAppProperty("midlet-vendor");
			if (name == null || vend == null)
				throw new RuntimeException("DC02");
			
			// Set
			_THIS_OWNER = new WeakReference<>((rv = new RecordStoreOwner(
				new MidletSuiteName(name), new MidletSuiteVendor(vend))));
		}
		
		return rv;
	}
}

