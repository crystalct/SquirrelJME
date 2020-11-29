// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This represents a single section within the output.
 *
 * @since 2019/08/11
 */
public final class ChunkSection
	extends OutputStream
	implements DataOutput
{
	/** The size of the bufer. */
	private static final int _BUFFER_SIZE =
		512;
	
	/** The fixed size of this section. */
	protected final int fixedsize;
	
	/** The alignment of this section. */
	protected final int alignment;
	
	/** Is this a variable size section? */
	protected final boolean isvariable;
	
	/** Data rewrites which are possible. */
	final List<__Rewrite__> _rewrites =
		new LinkedList<>();
	
	/** The tracker for the dirtiness. */
	final __Dirty__ _dirty;
	
	/** The byte buffer data. */
	byte[] _data;
	
	/** The current size of the section. */
	int _size;
	
	/** The write address of this section. */
	int _writeaddress =
		-1;
	
	/** The write size of this section. */
	int _writesize =
		-1;
	
	/**
	 * Initializes the written section.
	 *
	 * @param __size The size to use.
	 * @param __align The alignment to use.
	 * @param __d The dirty flag.
	 * @throws IllegalArgumentException If the size is zero or negative.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	ChunkSection(int __size, int __align, __Dirty__ __d)
		throws IllegalArgumentException, NullPointerException
	{
		if (__d == null)
			throw new NullPointerException("NARG");
		
		// {@squirreljme.error BD3l Zero or negative size. (The size)}
		if (__size != ChunkWriter.VARIABLE_SIZE && __size <= 0)
			throw new IllegalArgumentException("BD3l " + __size);
		
		// Set
		this.fixedsize = __size;
		this.alignment = (Math.max(__align, 1));
		this.isvariable = (__size == ChunkWriter.VARIABLE_SIZE);
		
		// Dirty flag storage
		this._dirty = __d;
		
		// If this is a fixed size section, we never have to expand it
		// so we can allocate all the needed data!
		if (__size != ChunkWriter.VARIABLE_SIZE)
			this._data = new byte[__size];
		else
			this._data = new byte[ChunkSection._BUFFER_SIZE];
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void close()
	{
		// Do nothing
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void flush()
	{
		// Do nothing
	}
	
	/**
	 * Returns the current written size of the section.
	 *
	 * @return The current section size.
	 * @since 2019/08/11
	 */
	public final int size()
	{
		return this._size;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void write(int __b)
		throws IOException
	{
		// {@squirreljme.error BD3m Size of section exceeded. (The size
		// of the section)}
		int size = this._size;
		if (!this.isvariable && size + 1 > this.fixedsize)
			throw new IOException("BD3m " + size);
		
		// Possibly resize the data array, only when variable
		byte[] data = this._data;
		if (this.isvariable && size >= data.length)
		{
			data = Arrays.copyOf(data, size + ChunkSection._BUFFER_SIZE);
			this._data = data;
		}
		
		// Write into the data
		data[size] = (byte)__b;
		
		// Size up
		this._size = size + 1;
		
		// Becomes dirty
		this._dirty._dirty = true;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void write(byte[] __b)
		throws IOException, NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		
		this.write(__b, 0, __b.length);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void write(byte[] __b, int __o, int __l)
		throws IndexOutOfBoundsException, IOException, NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		if (__o < 0 || __l < 0 || (__o + __l) > __b.length)
			throw new IndexOutOfBoundsException("IOOB");
		
		// {@squirreljme.error BD3p Size of section exceeded.}
		int size = this._size;
		if (!this.isvariable && size + __l > this.fixedsize)
			throw new IOException("BD3p");
		
		// Possibly resize the data array (only when variable)
		byte[] data = this._data;
		if (this.isvariable && size + __l >= data.length)
		{
			data = Arrays.copyOf(data,
				size + (Math.max(__l, ChunkSection._BUFFER_SIZE)));
			this._data = data;
		}
		
		// Write into the data
		for (int i = 0; i < __l; i++)
			data[size++] = __b[__o++];
		
		// Size up
		this._size = size;
		
		// Becomes dirty
		this._dirty._dirty = true;
	}
	
	/**
	 * Writes padding which aligns to a given amount from within the
	 * data stream as itself.
	 *
	 * @param __n The number of bytes to align to.
	 * @return The number of alignment bytes written.
	 * @throws IllegalArgumentException If the requested alignment is
	 * negative.
	 * @throws IOException On write errors.
	 * @since 2019/08/11
	 */
	public final int writeAlignment(int __n)
		throws IllegalArgumentException, IOException
	{
		// {@squirreljme.error BD3k Cannot align to a negative amount.
		// (The alignment)}
		if (__n < 1)
			throw new IllegalArgumentException("BD3k " + __n);
		
		// Not point aligning to a byte
		if (__n == 1)
			return 0;
		
		throw new todo.TODO();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeBoolean(boolean __v)
		throws IOException
	{
		throw new todo.TODO();
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeByte(int __v)
		throws IOException
	{
		this.write(__v);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeBytes(String __v)
		throws IOException, NullPointerException
	{
		if (__v == null)
			throw new NullPointerException("NARG");
		
		for (int i = 0, n = __v.length(); i < n; i++)
			this.write(__v.charAt(i));
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeChar(int __v)
		throws IOException
	{
		this.writeShort(__v);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeChars(String __v)
		throws IOException, NullPointerException
	{
		if (__v == null)
			throw new NullPointerException("NARG");
		
		for (int i = 0, n = __v.length(); i < n; i++)
		{
			char c = __v.charAt(i);
			
			this.write(c >> 8);
			this.write(c);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeDouble(double __v)
		throws IOException
	{
		this.writeLong(Double.doubleToLongBits(__v));
	}
	
	/**
	 * Writes the size of the file as an integer.
	 *
	 * @throws IOException On write errors.
	 * @since 2019/08/11
	 */
	public final void writeFileSizeInt()
		throws IOException
	{
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.INTEGER, __RewriteValue__.SIZE, 0, null));
			
		// Place padding
		this.writeInt(0);
	}
	
	/**
	 * Writes the size of the file as a short.
	 *
	 * @throws IOException On write errors.
	 * @since 2019/08/11
	 */
	public final void writeFileSizeShort()
		throws IOException
	{
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.SHORT, __RewriteValue__.SIZE, 0, null));
			
		// Place padding
		this.writeShort(0);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeFloat(float __v)
		throws IOException
	{
		this.writeInt(Float.floatToIntBits(__v));
	}
	
	/**
	 * Writes a future integer.
	 * 
	 * @return Future integer value.
	 * @throws IOException On read/write errors.
	 * @since 2020/11/29
	 */
	public ChunkFutureInteger writeFutureInt()
		throws IOException
	{
		ChunkFutureInteger rv = new ChunkFutureInteger();
		
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.INTEGER, __RewriteValue__.VALUE, rv));
			
		// Place padding
		this.writeInt(0);
		
		return rv;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeInt(int __v)
		throws IOException
	{
		this.write(__v >> 24);
		this.write(__v >> 16);
		this.write(__v >> 8);
		this.write(__v);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeLong(long __v)
		throws IOException
	{
		this.write((int)(__v >> 56));
		this.write((int)(__v >> 48));
		this.write((int)(__v >> 40));
		this.write((int)(__v >> 32));
		this.write((int)(__v >> 24));
		this.write((int)(__v >> 16));
		this.write((int)(__v >> 8));
		this.write((int)(__v));
	}
	
	/**
	 * Writes the specified number of bytes as padding, the padding
	 * value is zero.
	 *
	 * @param __n The number of bytes to pad with.
	 * @throws IOException On write errors.
	 * @since 2019/08/11
	 */
	public final void writePadding(int __n)
		throws IOException
	{
		this.writePadding(__n, 0);
	}
	
	/**
	 * Writes the specified number of bytes as padding.
	 *
	 * @param __n The number of bytes to pad with.
	 * @param __v The padding value to write.
	 * @throws IllegalArgumentException If the padding amount is negative.
	 * @throws IOException On write errors.
	 * @since 2019/08/11
	 */
	public final void writePadding(int __n, int __v)
		throws IllegalArgumentException, IOException
	{
		// {@squirreljme.error BD3j Negative padding. (The padding)}
		if (__n < 0)
			throw new IllegalArgumentException("BD3j " + __n);
		
		// Not writing anything, so ignore
		if (__n == 0)
			return;
		
		// Write the padding
		for (int i = 0; i < __n; i++)
			this.write(__v);
	}
	
	/**
	 * Writes the address of the given section as an integer.
	 *
	 * @param __s The section to write the address of.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/24
	 */
	public final void writeSectionAddressInt(ChunkSection __s)
		throws IOException, NullPointerException
	{
		this.writeSectionAddressInt(__s, 0);
	}
	
	/**
	 * Writes the address of the given section as an integer.
	 *
	 * @param __s The section to write the address of.
	 * @param __o The offset to use.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final void writeSectionAddressInt(ChunkSection __s, int __o)
		throws IOException, NullPointerException
	{
		if (__s == null)
			throw new NullPointerException("NARG");
		
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.INTEGER, __RewriteValue__.ADDRESS, __o, __s));
			
		// Place padding
		this.writeInt(0);
	}
	
	/**
	 * Writes the address of the given section as a short.
	 *
	 * @param __s The section to write the address of.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/24
	 */
	public final void writeSectionAddressShort(ChunkSection __s)
		throws IOException, NullPointerException
	{
		this.writeSectionAddressShort(__s, 0);
	}
	
	/**
	 * Writes the address of the given section as a short.
	 *
	 * @param __s The section to write the address of.
	 * @param __o The offset value.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final void writeSectionAddressShort(ChunkSection __s, int __o)
		throws IOException, NullPointerException
	{
		if (__s == null)
			throw new NullPointerException("NARG");
		
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.SHORT, __RewriteValue__.ADDRESS, __o, __s));
			
		// Place padding
		this.writeShort(0);
	}
	
	/**
	 * Writes the size of the given section as an integer.
	 *
	 * @param __s The section and its size to write.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final void writeSectionSizeInt(ChunkSection __s)
		throws IOException, NullPointerException
	{
		if (__s == null)
			throw new NullPointerException("NARG");
		
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.INTEGER, __RewriteValue__.SIZE, 0, __s));
			
		// Place padding
		this.writeInt(0);
	}
	
	/**
	 * Writes the size of the given section as a short.
	 *
	 * @param __s The section and its size to write.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final void writeSectionSizeShort(ChunkSection __s)
		throws IOException, NullPointerException
	{
		if (__s == null)
			throw new NullPointerException("NARG");
		
		// Record rewrite
		this._rewrites.add(new __Rewrite__(this._size,
			__RewriteType__.SHORT, __RewriteValue__.SIZE, 0, __s));
			
		// Place padding
		this.writeShort(0);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeShort(int __v)
		throws IOException
	{
		this.write(__v >> 8);
		this.write(__v);
	}
	
	/**
	 * Writes the specified short value and checks to make sure it is
	 * within the range of a short.
	 *
	 * @param __v The value to write.
	 * @throws IOException If the short could not be written or it
	 * exceeds the range of a short value.
	 * @since 2019/08/11
	 */
	public final void writeShortChecked(int __v)
		throws IOException
	{
		// {@squirreljme.error BD3o Signed short value out of range.
		// (The value)}
		if (__v < -32768 || __v > 32767)
			throw new IOException("BD3o " + __v);
		
		this.write(__v >> 8);
		this.write(__v);
	}
	
	/**
	 * Writes the specified unsigned short value and checks to make sure it
	 * is within the range of an unsigned short.
	 *
	 * @param __v The value to write.
	 * @throws IOException If the unsigned short could not be written or it
	 * exceeds the range of an unsigned short value.
	 * @since 2019/08/11
	 */
	public final void writeUnsignedShortChecked(int __v)
		throws IOException
	{
		// {@squirreljme.error BD3n Unsigned short value out of range.
		// (The value)}
		if (__v < 0 || __v > 65535)
			throw new IOException("BD3n " + __v);
		
		this.write(__v >> 8);
		this.write(__v);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/08/11
	 */
	@Override
	public final void writeUTF(String __v)
		throws IOException, NullPointerException
	{
		if (__v == null)
			throw new NullPointerException("NARG");
		
		// Write into a buffer
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(
			2 + (__v.length() * 2));
			 DataOutputStream dos = new DataOutputStream(baos))
		{
			// Write data
			dos.writeUTF(__v);
			
			// Dump to this internally
			baos.writeTo(this);
		}
	}
}
