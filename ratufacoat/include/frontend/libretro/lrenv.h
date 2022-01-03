/* -*- Mode: C; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// -------------------------------------------------------------------------*/

/**
 * Environment setup.
 * 
 * @since 2022/01/02
 */

#ifndef SQUIRRELJME_LRENV_H
#define SQUIRRELJME_LRENV_H

/* Anti-C++. */
#ifdef __cplusplus
#ifndef SJME_CXX_IS_EXTERNED
#define SJME_CXX_IS_EXTERNED
#define SJME_CXX_SQUIRRELJME_LRENV_H
extern "C"
{
#endif /* #ifdef SJME_CXX_IS_EXTERNED */
#endif /* #ifdef __cplusplus */

/*--------------------------------------------------------------------------*/

/** ROM Order Key. */
#define SJME_LIBRETRO_CONFIG_ROM_ORDER "squirreljme_rom_order"

/** External/Internal ROM order. */
#define SJME_LIBRETRO_CONFIG_ROM_ORDER_EXT_INT "external+internal"

/** Internal/External ROM order. */
#define SJME_LIBRETRO_CONFIG_ROM_ORDER_INT_EXT "internal+external"

/** External ROM order. */
#define SJME_LIBRETRO_CONFIG_ROM_ORDER_EXT "external"

/** Internal ROM order. */
#define SJME_LIBRETRO_CONFIG_ROM_ORDER_INT "internal"

/*--------------------------------------------------------------------------*/

/* Anti-C++. */
#ifdef __cplusplus
#ifdef SJME_CXX_SQUIRRELJME_LRENV_H
}
#undef SJME_CXX_SQUIRRELJME_LRENV_H
#undef SJME_CXX_IS_EXTERNED
#endif /* #ifdef SJME_CXX_SQUIRRELJME_LRENV_H */
#endif /* #ifdef __cplusplus */

#endif /* SQUIRRELJME_LRENV_H */
