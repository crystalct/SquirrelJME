// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package dev.shadowtail.jarfile;

import cc.squirreljme.jvm.Constants;
import dev.shadowtail.classfile.mini.MinimizedClassFile;
import dev.shadowtail.classfile.mini.Minimizer;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import net.multiphasicapps.classfile.ClassFile;
import net.multiphasicapps.classfile.ClassName;
import net.multiphasicapps.classfile.ClassNames;
import net.multiphasicapps.classfile.InvalidClassFormatException;

/**
 * This contains the state of the entire bootstrap, its memory and all of the
 * classes which may be loaded accordingly.
 *
 * @since 2019/09/14
 */
public final class BootstrapState
{
	/** The size of the static field area. */
	public static final int STATIC_FIELD_SIZE =
		8192;
	
	/** The initializer to use. */
	protected final Initializer initializer =
		new Initializer();
	
	/** Class information which has been loaded. */
	private final Map<ClassName, LoadedClassInfo> _classinfos =
		new LinkedHashMap<>();
	
	/** Reference to self. */
	private final Reference<BootstrapState> _selfref =
		new WeakReference<>(this);
	
	/** Static field pointer area. */
	private int _sfieldarea;
	
	/** Static field area next pointer. */
	private int _sfieldnext;
	
	/**
	 * Allocates static field space.
	 *
	 * @return The pointer to the static field area that was allocated.
	 * @throws IllegalArgumentException If the size is zero or negative.
	 * @since 2019/09/14
	 */
	public final int allocateStaticFieldSpace(int __sz)
		throws IllegalArgumentException
	{
		// {@squirreljme.error BC0e Cannot allocate zero or negative static
		// field space.}
		if (__sz <= 0)
			throw new IllegalArgumentException("BC0e");
		
		// Allocate area for static fields, if not done yet
		int sfieldarea = this.staticFieldAreaAddress();
		
		// Determine the space that is needed
		int sfieldnext = this._sfieldnext;
		int snext = sfieldnext + __sz;
		
		// {@squirreljme.error BC03 Ran out of static field space.}
		if (snext >= BootstrapState.STATIC_FIELD_SIZE)
			throw new RuntimeException("BC03");
			
		// Set next pointer area
		this._sfieldnext = snext;
		
		// Pointer is here
		return sfieldnext;
	}
	
	/**
	 * Returns an info pointer for the given class names.
	 *
	 * @param __cls The class names to get info pointers for.
	 * @return The pointer to the class info pointer list.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/09/14
	 */
	public final int classNamesInfoPointer(ClassNames __cls)
		throws NullPointerException
	{
		if (__cls == null)
			throw new NullPointerException("NARG");
		
		// Needed for allocations and writes
		Initializer initializer = this.initializer;
		
		// The IDs are contained within int[] arrays for simplicity
		int n = __cls.size(),
			rv = initializer.allocate(Constants.ARRAY_BASE_SIZE + (4 * n));
		
		// Write object details
		initializer.memWriteInt(Modifier.RAM_OFFSET,
			rv + Constants.OBJECT_CLASS_OFFSET,
			this.findClass("[I").infoPointer());
		initializer.memWriteInt(
			rv + Constants.OBJECT_COUNT_OFFSET,
			999999);
		initializer.memWriteInt(
			rv + Constants.ARRAY_LENGTH_OFFSET,
			n);
		
		// Write ID elements
		for (int i = 0, wp = rv + Constants.ARRAY_BASE_SIZE;
			i < n; i++, wp += 4)
			initializer.memWriteInt(Modifier.RAM_OFFSET,
				wp, this.findClass(__cls.get(i)).infoPointer());
		
		// Use this pointer here
		return rv;
	}
	
	/**
	 * Finds the class which uses the given name.
	 *
	 * @param __cl The class name to find.
	 * @return The loaded class information.
	 * @throws InvalidClassFormatException If the class was not found.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/09/14
	 */
	public final LoadedClassInfo findClass(String __cl)
		throws InvalidClassFormatException, NullPointerException
	{
		if (__cl == null)
			throw new NullPointerException("NARG");
		
		return this.findClass(new ClassName(__cl));
	}
	
	/**
	 * Finds the class which uses the given name.
	 *
	 * @param __cl The class name to find.
	 * @return The loaded class information.
	 * @throws InvalidClassFormatException If the class was not found.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/09/14
	 */
	public final LoadedClassInfo findClass(ClassName __cl)
		throws InvalidClassFormatException, NullPointerException
	{
		if (__cl == null)
			throw new NullPointerException("NARG");
		
		// Locate pre-loaded class
		Map<ClassName, LoadedClassInfo> classinfos = this._classinfos;
		LoadedClassInfo rv = classinfos.get(__cl);
		if (rv == null)
		{
			// Load special primitive and array types magically!
			if (__cl.isPrimitive() || __cl.isArray())
				classinfos.put(__cl, (rv = new LoadedClassInfo(
					Minimizer.minimizeAndDecode(
						ClassFile.special(__cl.field())), 0, this._selfref)));
			
			// {@squirreljme.error BC0c Could not find the specified class.
			// (The class name)}
			else
				throw new InvalidClassFormatException("BC0c " + __cl);
		}
		
		return rv;
	}
	
	/**
	 * Loads the class file information.
	 *
	 * @param __b The class file data.
	 * @param __pos The position of the class file in ROM.
	 * @return The loaded class information
	 * @throws IOException On read errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/09/14
	 */
	public final LoadedClassInfo loadClassFile(byte[] __b, int __pos)
		throws IOException, NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		
		// Decode it
		MinimizedClassFile cf = MinimizedClassFile.decode(__b);
		
		// Store loaded for later boot usage
		LoadedClassInfo rv;
		if (this._classinfos.put(cf.thisName(),
			(rv = new LoadedClassInfo(cf, __pos, this._selfref))) != null)
		{
			// {@squirreljme.error BC0b Class file has already been loaded.
			// (The class name)}
			throw new IllegalStateException("BC0b " + cf.thisName());
		}
		
		return rv;
	}
	
	/**
	 * Returns the address of the static field area.
	 *
	 * @return The static field address area.
	 * @since 2019/09/14
	 */
	public final int staticFieldAreaAddress()
	{
		// Allocate area for static fields, if not done yet
		int sfieldarea = this._sfieldarea;
		if (sfieldarea <= 0)
			this._sfieldarea = (sfieldarea = this.initializer.allocate(
				BootstrapState.STATIC_FIELD_SIZE));
		
		return sfieldarea;
	}
}
