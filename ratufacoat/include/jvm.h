/* -*- Mode: C; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// -------------------------------------------------------------------------*/

/**
 * JVM Definitions.
 * 
 * @since 2021/02/27
 */

#ifndef SQUIRRELJME_JVM_H
#define SQUIRRELJME_JVM_H

#include "error.h"
#include "native.h"
#include "sjmecon.h"
#include "oldstuff.h"

/* Anti-C++. */
#ifdef __cplusplus
#ifndef SJME_CXX_IS_EXTERNED
#define SJME_CXX_IS_EXTERNED
#define SJME_CXX_SQUIRRELJME_JVM_H
extern "C"
{
#endif /* #ifdef SJME_CXX_IS_EXTERNED */
#endif /* #ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

/**
 * Instance of the JVM.
 *
 * @since 2019/06/03
 */
typedef struct sjme_jvm sjme_jvm;

/**
 * Java virtual machine arguments.
 *
 * @since 2019/06/03
 */
typedef struct sjme_jvmargs
{
	/** The format of the arguments. */
	int format;
	
	/** Arguments that can be used. */
	union
	{
		/** Standard C. */
		struct
		{
			/** Argument count. */
			int argc;
			
			/** Arguments. */
			char** argv;
		} stdc;
	} args;
} sjme_jvmargs;

/**
 * Options used to initialize the virtual machine.
 *
 * @since 2019/06/06
 */
typedef struct sjme_jvmoptions
{
	/** The amount of RAM to allocate, 0 is default. */
	sjme_jint ramsize;
	
	/** Preset ROM pointer, does not need loading? */
	void* presetrom;
	
	/** Preset ROM size. */
	sjme_jint romsize;
	
	/** If non-zero then the ROM needs to be copied (address unsafe). */
	sjme_jbyte copyrom;
	
	/** Command line arguments sent to the VM. */
	sjme_jvmargs args;
} sjme_jvmoptions;

/**
 * Executes code running within the JVM.
 *
 * @param jvm The JVM to execute.
 * @param error JVM execution error.
 * @param cycles The number of cycles to execute for.
 * @return Non-zero if the JVM is resuming, otherwise zero on its exit.
 * @since 2019/06/05
 */
sjme_jint sjme_jvmexec(sjme_jvm* jvm, sjme_error* error, sjme_jint cycles);

/**
 * Destroys the virtual machine instance.
 *
 * @param jvm The JVM to destroy.
 * @param error The error state.
 * @return Non-zero if successful.
 * @since 2019/06/09
 */
sjme_jint sjme_jvmDestroy(sjme_jvm* jvm, sjme_error* error);

/**
 * Creates a new instance of a SquirrelJME JVM.
 *
 * @param args Arguments to the JVM.
 * @param options Options used to initialize the JVM.
 * @param nativefuncs Native functions used in the JVM.
 * @param error Error flag.
 * @return The resulting JVM or {@code NULL} if it could not be created.
 * @since 2019/06/03
 */
sjme_jvm* sjme_jvmNew(sjme_jvmoptions* options, sjme_nativefuncs* nativefuncs,
	sjme_error* error);

/**
 * Returns the virtual memory of the given JVM.
 * 
 * @param jvm The JVM to get the virtual memory of.
 * @return The virtual memory.
 * @deprecated Deprecated, do not use.
 * @since 2021/02/28 
 */
sjme_vmem* sjme_jvmVMem(sjme_jvm* jvm);

/*--------------------------------------------------------------------------*/

/* Anti-C++. */
#ifdef __cplusplus
#ifdef SJME_CXX_SQUIRRELJME_JVM_H
}
#undef SJME_CXX_SQUIRRELJME_JVM_H
#undef SJME_CXX_IS_EXTERNED
#endif /* #ifdef SJME_CXX_SQUIRRELJME_JVM_H */
#endif /* #ifdef __cplusplus */

#endif /* SQUIRRELJME_JVM_H */
