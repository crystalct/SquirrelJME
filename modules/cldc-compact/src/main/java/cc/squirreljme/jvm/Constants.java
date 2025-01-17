// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jvm;

/**
 * Virtual machine constants.
 *
 * @since 2019/05/26
 */
public interface Constants
{
	/** The offset for the object's class type. */
	byte OBJECT_CLASS_OFFSET =
		0;
	
	/** The offset for the object's reference count. */
	byte OBJECT_COUNT_OFFSET =
		4;
	
	/** Object monitor owner offset. */
	byte OBJECT_MONITOR_OFFSET =
		8;
	
	/** Object monitor count offset. */
	byte OBJECT_MONITOR_COUNT_OFFSET =
		12;
	
	/** Base size for object types. */
	byte OBJECT_BASE_SIZE =
		16;
	
	/** The offset for array length. */
	byte ARRAY_LENGTH_OFFSET =
		16;
	
	/** The base size for arrays. */
	byte ARRAY_BASE_SIZE =
		20;
	
	/** Constant pool cell size. */
	byte POOL_CELL_SIZE =
		4;
	
	/** Bad magic number. */
	int BAD_MAGIC =
		0xE7E5E7E4;
	
	/** Class info flag: Is array type? */
	short CIF_IS_ARRAY =
		0x0001;
	
	/** Class info flag: Is array of objects? */
	short CIF_IS_ARRAY_OF_OBJECTS =
		0x0002;
	
	/** Is this a primitive type? */
	short CIF_IS_PRIMITIVE =
		0x0004;
	
	/** Offset for the configuration key. */
	byte CONFIG_KEY_OFFSET =
		0;
	
	/** Offset for the configuration size. */
	byte CONFIG_SIZE_OFFSET =
		2;
	
	/** Size of the header for configuration items. */
	byte CONFIG_HEADER_SIZE =
		4;
	
	/** The thread ID for out-of-bound IPC events. */
	int OOB_IPC_THREAD =
		0xFFFFFFFF;
}

