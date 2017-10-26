// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
//     Copyright (C) Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.lcdui.gfx;

/**
 * This is the base class for classes which operate on linear arrays of pixels.
 *
 * @since 2017/02/12
 */
public abstract class PixelArrayGraphics
	extends BasicGraphics
{
	/** Image width. */
	protected final int width;
	
	/** Image height. */
	protected final int height;
	
	/** The image pitch (the actual scanline length in pixels). */
	protected final int pitch;
	
	/** The offset into the data buffer. */
	protected final int offset;
	
	/** The total number of elements used in the data array. */
	protected final int elementcount;
	
	/**
	 * Initializes the graphics drawer which draw into the given integer
	 * array.
	 *
	 * @param __data The buffer to draw into.
	 * @param __width The width of the image.
	 * @param __height The height of the image.
	 * @param __alpha If {@code true} then an alpha channel is used.
	 * @param __pitch The image pitch.
	 * @param __offset The data buffer offset.
	 * @throws ArrayIndexOutOfBoundsException If the image dimensions exceeds
	 * the array bounds.
	 * @throws IllegalArgumentException If the width or height is negative.
	 * @throws NullPointerException On null arguments.
	 * @since 2017/10/26
	 */
	public PixelArrayGraphics(Object __data, int __width, int __height,
		boolean __alpha, int __pitch, int __offset)
		throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			NullPointerException
	{
		super(__alpha);
		
		// Check
		if (__data == null)
			throw new NullPointerException("NARG");
		
		// Determine the array properties
		int datalen;
		if (__data instanceof byte[])
			datalen = ((byte[])__data).length;
		else if (__data instanceof short[])
			datalen = ((short[])__data).length;
		else if (__data instanceof int[])
			datalen = ((int[])__data).length;
		else if (__data instanceof long[])
			datalen = ((long[])__data).length;
		else if (__data instanceof float[])
			datalen = ((float[])__data).length;
		else if (__data instanceof double[])
			datalen = ((double[])__data).length;
		
		// {@squirreljme.error EB25 The specified class is not an array or is
		// not a supported array for image data. (The class)} 
		else
			throw new IllegalArgumentException(String.format("EB25 %s",
				__data.getClass()));
		
		// {@squirreljme.error EB24 The specified parameters exceed the bounds
		// of the array. (The pitch; The height; The offset; The array length)}
		int elementcount = (__pitch * __height);
		if (__offset < 0 || (__offset + elementcount) > datalen)
			throw new ArrayIndexOutOfBoundsException(
				String.format("EB24 %d %d %d %d", __pitch, __height, __offset,
				datalen));
		
		// {@squirreljme.error EB0p Invalid width and/or height specified.}
		if (__width <= 0 || __height <= 0)
			throw new IllegalArgumentException("EB0p");
		
		// {@squirreljme.error EB23 The pitch is less than the width.}
		if (__pitch < __width)
			throw new IllegalArgumentException("EB23");
		
		// Set
		this.width = __width;
		this.height = __height;
		this.pitch = __pitch;
		this.offset = __offset;
		this.elementcount = elementcount;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2017/02/11
	 */
	@Override
	protected int primitiveImageHeight()
	{
		return this.height;
	}

	/**
	 * {@inheritDoc}
	 * @since 2017/02/11
	 */
	@Override
	protected int primitiveImageWidth()
	{
		return this.width;
	}
}

