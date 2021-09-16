// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package javax.microedition.m3g;


public abstract class IndexBuffer
	extends Object3D
{
	IndexBuffer()
	{
		throw new todo.TODO();
	}
	
	public abstract int getIndexCount();
	
	public abstract void getIndices(int[] __a);
}


