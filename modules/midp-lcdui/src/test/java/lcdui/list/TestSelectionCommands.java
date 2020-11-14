// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package lcdui.list;

import cc.squirreljme.jvm.mle.constants.UIWidgetProperty;
import cc.squirreljme.runtime.lcdui.mle.StaticDisplayState;
import cc.squirreljme.runtime.lcdui.mle.UIBackend;
import cc.squirreljme.runtime.lcdui.mle.UIBackendFactory;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.List;
import net.multiphasicapps.tac.UntestableException;

/**
 * Tests that selection commands work properly.
 *
 * @since 2020/11/03
 */
public class TestSelectionCommands
	extends BaseList
{
	/** Number of list items to test. */
	public static final int NUM_ITEMS =
		3;
	
	/**
	 * {@inheritDoc}
	 * @since 2020/11/03
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	@Override
	protected void test(Display __display, List __list, int __type,
		String __typeName)
	{
		// This is only valid on implicit tests
		if (__type != Choice.IMPLICIT)
			throw new UntestableException("Implicit only.");
		
		// Listener used to keep track of state
		SelectionListener listener = new SelectionListener();
		__list.setCommandListener(listener);
		
		// Add items to the list
		for (int i = 0; i < TestSelectionCommands.NUM_ITEMS; i++)
			__list.append(Character.toString((char)('a' + i)), null);
		
		// Set items as selected, which should trigger selection
		UIBackend backend = UIBackendFactory.getInstance();
		for (int i = 0; i < TestSelectionCommands.NUM_ITEMS; i++)
		{
			// Send event and wait for it to be flushed out
			backend.widgetProperty(StaticDisplayState.locate(__list),
				UIWidgetProperty.INT_LIST_ITEM_SELECTED, i, 1);
			backend.flushEvents();
			
			// Make sure it was selected
			synchronized (listener)
			{
				this.secondary("selected-" + i,
					listener.selectedItems.contains(i)); 
				this.secondary("last-" + i,
					listener.lastSelected == i);
			}
		}
	}
}