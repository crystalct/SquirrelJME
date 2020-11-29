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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.util.LinkedList;
import java.util.List;

/**
 * This is an output stream which writes to section tables, essentially a
 * number of various data chunks in the stream. All sections are ordered in
 * the order that they are created.
 *
 * This class is not thread safe.
 *
 * @since 2019/08/11
 */
public final class ChunkWriter
{
	/** This represents a variable sized section. */
	public static final int VARIABLE_SIZE =
		Integer.MIN_VALUE;
	
	/** The sections which are available in the output. */
	private final List<ChunkSection> _sections =
		new LinkedList<>();
	
	/** Are the section informations dirty? */
	private final __Dirty__ _dirty =
		new __Dirty__();
	
	/** The current file size. */
	private int _filesize;
	
	/**
	 * Adds a section which is of a variable size.
	 *
	 * @return The resulting section.
	 * @since 2019/08/11
	 */
	public final ChunkSection addSection()
	{
		return this.addSection(ChunkWriter.VARIABLE_SIZE, 1);
	}
	
	/**
	 * Adds a section which consists of the given byte array.
	 *
	 * @param __bytes The byte array to initialize as.
	 * @return The resulting section.
	 * @throws IOException If the section could not be written.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final ChunkSection addSection(byte[] __bytes)
		throws IOException, NullPointerException
	{
		return this.addSection(__bytes, 1);
	}
	
	/**
	 * Adds a section which consists of the given byte array using the given
	 * alignment.
	 *
	 * @param __bytes The byte array to initialize as.
	 * @param __align The alignment to use.
	 * @return The resulting section.
	 * @throws IllegalArgumentException If the size is zero or negative and
	 * is not the variable size, or the alignment is below one.
	 * @throws IOException If the section could not be written.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final ChunkSection addSection(byte[] __bytes,
		int __align)
		throws IllegalArgumentException, IOException, NullPointerException
	{
		if (__bytes == null)
			throw new NullPointerException("NARG");
		
		// Add new section
		ChunkSection rv = this.addSection(__bytes.length, __align);
		
		// Write all the bytes into it
		rv.write(__bytes);
		
		// Use this section
		return rv;
	}
	
	/**
	 * Adds a section which is either of a variable size of a fixed size.
	 *
	 * @param __size The size of the section to use, if {@link #VARIABLE_SIZE}
	 * then the section will have a variable size.
	 * @return The section which was created for writing.
	 * @throws IllegalArgumentException If the size is zero or negative and
	 * is not the variable size, or the alignment is below one.
	 * @since 2019/08/11
	 */
	public final ChunkSection addSection(int __size)
		throws IllegalArgumentException
	{
		return this.addSection(__size, 1);
	}
	
	/**
	 * Adds a section which is either of a variable size of a fixed size and
	 * one which has an alignment.
	 *
	 * @param __size The size of the section to use, if {@link #VARIABLE_SIZE}
	 * then the section will have a variable size.
	 * @param __align The alignment of the section, if the value is lower than
	 * {@code 1} it will be set to {@code 1}.
	 * @return The section which was created for writing.
	 * @throws IllegalArgumentException If the size is zero or negative and
	 * is not the variable size, or the alignment is below one.
	 * @since 2019/08/11
	 */
	public final ChunkSection addSection(int __size,
		int __align)
		throws IllegalArgumentException
	{
		// {@squirreljme.error BD3h Zero or negative size section. (The size)}
		if (__size != ChunkWriter.VARIABLE_SIZE && __size <= 0)
			throw new IllegalArgumentException("BD3h " + __size);
		
		// {@squirreljme.error BD3q Zero or negative alignment.
		// (The alignment)}
		if (__align < 1)
			throw new IllegalArgumentException("BD3q " + __align);
		
		// Create section
		ChunkSection rv = new ChunkSection(__size, __align, this._dirty);
		
		// Add to our section list
		this._sections.add(rv);
		
		// Becomes dirty because new section was added
		this._dirty._dirty = true;
		
		// And return this section
		return rv;
	}
	
	/**
	 * Returns the current size of the file.
	 *
	 * @return The file size.
	 * @since 2019/08/25
	 */
	public final int fileSize()
	{
		this.__undirty();
		return this._filesize;
	}
	
	/**
	 * Returns the address of the given section.
	 *
	 * @param __s The section.
	 * @return The address of the section.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/25
	 */
	public final int sectionAddress(ChunkSection __s)
		throws NullPointerException
	{
		if (__s == null)
			throw new NullPointerException("NARG");
		
		this.__undirty();
		return __s._writeaddress;
	}
	
	/**
	 * Returns the size of the given section.
	 *
	 * @param __s The section.
	 * @return The size of the section.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/25
	 */
	public final int sectionSize(ChunkSection __s)
		throws NullPointerException
	{
		if (__s == null)
			throw new NullPointerException("NARG");
		
		this.__undirty();
		return __s._writesize;
	}
	
	/**
	 * Returns a byte array representing the table file.
	 *
	 * @return The resulting byte array.
	 * @since 2019/08/11
	 */
	public final byte[] toByteArray()
	{
		// Setup output byte array which has a base size for the size of the
		// file
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(
			this.fileSize()))
		{
			// Record everything in
			this.writeTo(baos);
			
			// Return the resulting byte array
			return baos.toByteArray();
		}
		
		// {@squirreljme.error BD3i Could not create the byte array.}
		catch (IOException e)
		{
			throw new RuntimeException("BD3i", e);
		}
	}
	
	/**
	 * Writes the table file to the given output stream.
	 *
	 * @param __os The stream to write to.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/08/11
	 */
	public final void writeTo(OutputStream __os)
		throws IOException, NullPointerException
	{
		if (__os == null)
			throw new NullPointerException("NARG");
		
		// The current write pointer
		int writeptr = 0;
		
		// Un-dirty and get the file size
		this.__undirty();
		int filesize = this._filesize;
		
		// Write each individual section to the output stream
		List<ChunkSection> sections = this._sections;
		for (int i = 0, n = sections.size(); i < n; i++)
		{
			ChunkSection section = sections.get(i);
			
			// Get properties of the section
			byte[] data = section._data;
			int cursize = section._size,
				writeaddress = section._writeaddress,
				writesize = section._writesize,
				writeendaddress = writeaddress + writesize,
				actualwrite = (Math.min(cursize, writesize));
			
			// Write padding until we reach our needed address
			while (writeptr < writeaddress)
			{
				__os.write(0);
				writeptr++;
			}
			
			// Rewrites must be processed to figure out how to refer to
			// other section aliases!
			for (__Rewrite__ rewrite : section._rewrites)
			{
				// The target section if any
				Reference<ChunkSection> refsection =
					rewrite._section;
				
				// Determine the value that is to be written
				int value = 0;
				switch (rewrite._value)
				{
						// Address of section
					case ADDRESS:
						if (refsection != null)
							value = refsection.get()._writeaddress;
						break;
						
						// Size of section or file.
					case SIZE:
						if (refsection == null)
							value = filesize;
						else
							value = refsection.get()._writesize;
						break;
						
						// Value
					case VALUE:
						value = rewrite._future.get();
						break;
				}
				
				// Add offset of value
				value += rewrite._offset;
				
				// Perform the actual rewrite
				int paddr = rewrite._address;
				switch (rewrite._type)
				{
					case SHORT:
						data[paddr] = (byte)(value >>> 8);
						data[paddr + 1] = (byte)(value);
						break;
					
					case INTEGER:
						data[paddr] = (byte)(value >>> 24);
						data[paddr + 1] = (byte)(value >>> 16);
						data[paddr + 2] = (byte)(value >>> 8);
						data[paddr + 3] = (byte)(value);
						break;
				}
			}
			
			// Write the section data as an entire chunk, note that this
			// could be a fixed size section with a short buffer!
			__os.write(data, 0, actualwrite);
			writeptr += actualwrite;
			
			// Write padding until we reach our ending address
			while (writeptr < writeendaddress)
			{
				__os.write(0);
				writeptr++;
			}
		}
	}
	
	/**
	 * Undirties and calculations all the section layout and information.
	 *
	 * @since 2019/08/25
	 */
	private final void __undirty()
	{
		// There is no need to calculate if this is not dirty at all
		__Dirty__ dirty = this._dirty;
		if (!dirty._dirty)
			return;
		
		// Our current file size
		int filesize = 0;
		
		// We must go through all of the sections, perform their required
		// alignment while additionally calculating their addresses within
		// the file for section references.
		List<ChunkSection> sections = this._sections;
		for (int i = 0, n = sections.size(); i < n; i++)
		{
			ChunkSection section = sections.get(i);
			
			// Perform alignment of this section
			if ((filesize % section.alignment) != 0)
				filesize += section.alignment - (filesize % section.alignment);
			
			// Section is addressed here
			section._writeaddress = filesize;
			
			// Move the current file size up by the section's size
			int writesize = (section.isvariable ?
				section._size : section.fixedsize);
			filesize += writesize;
			section._writesize = writesize;
		}
		
		// Store file size
		this._filesize = filesize;
		
		// Clear dirty flag
		dirty._dirty = false;
	}
}
