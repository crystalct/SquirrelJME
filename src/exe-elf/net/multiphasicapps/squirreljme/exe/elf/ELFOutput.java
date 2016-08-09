// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) 2013-2016 Steven Gawroriski <steven@multiphasicapps.net>
//     Copyright (C) 2013-2016 Multi-Phasic Applications <multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// For more information see license.mkd.
// ---------------------------------------------------------------------------

package net.multiphasicapps.squirreljme.exe.elf;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import net.multiphasicapps.io.data.DataEndianess;
import net.multiphasicapps.io.data.ExtendedDataOutputStream;
import net.multiphasicapps.squirreljme.jit.base.JITCPUEndian;
import net.multiphasicapps.squirreljme.exe.ExecutableOutput;

/**
 * This is used to output ELF binaries.
 *
 * @since 2016/08/07
 */
public class ELFOutput
	implements ExecutableOutput
{
	/** System properties which are available. */
	protected final Map<String, String> properties =
		new HashMap<>();
	
	/** The target bits. */
	protected final int bits;
	
	/** The target endianess. */
	protected final JITCPUEndian endianess;
	
	/** The operating system ABI. */
	protected final int osabi;
	
	/** The CPU type. */
	protected final int cputype;
	
	/** The size of the ELF header. */
	protected final int headersize;
	
	/** The program header size. */
	protected final int pheadersize;
	
	/**
	 * Initializes the ELF output with the specified parameters.
	 *
	 * @param __bits The number of bits the CPU uses.
	 * @param __end The endianess of the CPU.
	 * @param __osabi The operating system ABI.
	 * @param __cputype The CPU type.
	 * @throws IllegalArgumentException If the CPU bit level is not 32 or 64;
	 * or the endianess is not known.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/08/08
	 */
	public ELFOutput(int __bits, JITCPUEndian __end, int __osabi,
		int __cputype)
		throws IllegalArgumentException, NullPointerException
	{
		// Check
		if (__end == null)
			throw new NullPointerException("NARG");
		
		// {@squirreljme.error AX01 Unsupported output CPU bits. (The bit
		// size of the CPU)}
		switch (__bits)
		{
				// 32-bit
			case 32:
				this.headersize = 52;
				this.pheadersize = 32;
				break;
				
				// 64-bit
			case 64:
				this.headersize = 64;
				this.pheadersize = 56;
				break;
				
				// Unknown
			default:
				throw new IllegalArgumentException(String.format("AX01 %d",
					__bits));
		}
		
		// {@squirreljme.error AX02 Unsupported endianess. (The endianess)}
		if (__end != JITCPUEndian.BIG && __end != JITCPUEndian.LITTLE)
			throw new IllegalArgumentException(String.format("AX02 %s",
				__end));
		
		// Set
		this.bits = __bits;
		this.endianess = __end;
		this.osabi = __osabi;
		this.cputype = __cputype;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/08/07
	 */
	@Override
	public void addSystemProperty(String __k, String __v)
		throws NullPointerException
	{
		// Check
		if (__k == null || __v == null)
			throw new NullPointerException("NARG");
		
		// Lock
		Map<String, String> properties = this.properties;
		synchronized (properties)
		{
			properties.put(__k, __v);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2016/08/07
	 */
	@Override
	public void linkBinary(OutputStream __os, String[] __names,
		InputStream[] __blobs)
		throws IOException, NullPointerException
	{
		// Check
		if (__os == null || __names == null || __blobs == null)
			throw new NullPointerException("NARG");
		
		// Load blobs into byte arrays
		int n = __names.length;
		byte[][] preblobs = __preloadBlobs(__blobs);
		
		throw new Error("TODO");
		/*
		// The base address for the actual code data
		int headersize = this.headersize;
		int pheadersize = this.pheadersize;
		int programsize = 0;
		int codebase = headersize + pheadersize;
		
		// It is not possible to write the program header with a fixed size
		// since it would either waste space or be too short. As such, load
		// all blob data into arrays;
		int baseoff = ((codebase + 7) & (~7)), origbaseoff = baseoff;
		int[] blobsoff = new int[n];
		byte[] buf = new byte[512];
		int blobprogramsize = 0;
		for (int i = 0; i < n; i++)
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
			{
				// Align to 8
				baseoff = ((baseoff + 7) & (~7));
				blobsoff[i] = baseoff;
				
				// Copy all the data
				for (InputStream is = __blobs[i];;)
				{
					int rc = is.read(buf);
					
					// EOF?
					if (rc < 0)
						break;
					
					// Copy
					baos.write(buf, 0, rc);
				}
				
				// Store
				byte data[] = baos.toByteArray();
				preblobs[i] = data;
				
				// Add offset
				baseoff += data.length;
				
				// Determine if this contains the entry point
				System.err.println("TODO -- Determine blob entry point.");
			}
		
		// Determine size
		blobprogramsize = baseoff - origbaseoff;
		
		// Determine size of the namespace entries
		System.err.println("TODO -- Determine namespace table size.");
		
		// Extra bootstrap header?
		programsize += blobprogramsize;
		
		// Write the output binary
		try (ExtendedDataOutputStream dos = new ExtendedDataOutputStream(__os))
		{
			// Correct endian
			JITCPUEndian endianess = this.endianess;
			int endid;
			switch (endianess)
			{
					// Big endian
				case BIG:
					dos.setEndianess(DataEndianess.BIG);
					endid = 2;
					break;
					
					// Little endian
				case LITTLE:
					dos.setEndianess(DataEndianess.LITTLE);
					endid = 1;
					break;
				
					// Unknown
				default:
					throw new RuntimeException("OOPS");
			}
			
			// Write header
			dos.writeByte(0x7F);
			dos.writeByte('E');
			dos.writeByte('L');
			dos.writeByte('F');
			
			// Which class?
			int bits = this.bits;
			switch (bits)
			{
					// 32-bit
				case 32:
					dos.writeByte(1);
					break;
				
					// 64-bit:
				case 64:
					dos.writeByte(2);
					break;
				
					// Unknown
				default:
					throw new RuntimeException("OOPS");
			}
			
			// Which endianess
			dos.writeByte(endid);
			
			// Always version 1
			dos.writeByte(1);
			
			// The OS ABI
			dos.writeByte(this.osabi);
			
			// Ignore padding
			dos.writeInt(0);
			dos.writeInt(0);
			
			// ELFs are always static executable
			dos.writeShort(2);
			
			// The instruction set
			dos.writeShort(this.cputype);
			
			// Always version  1
			dos.writeInt(1);
			
			// Entry point (always zero);
			// ph offset (after this header);
			// sh offset (not used, no sections are used)
			switch (bits)
			{
					// 32-bit
				case 32:
					dos.writeInt(0);
					dos.writeInt(headersize);
					dos.writeInt(0);
					break;
				
					// 64-bit
				case 64:
					dos.writeLong(0);
					dos.writeLong(headersize);
					dos.writeLong(0);
					break;
				
					// Unknown
				default:
					throw new RuntimeException("OOPS");
			}
			
			// Ignore flags
			dos.writeInt(0);
			
			// Header is the standard size
			dos.writeShort(headersize);
			
			// Program header details
			dos.writeShort(pheadersize);	// size
			dos.writeShort(1);	// count
			
			// There are no sections
			dos.writeShort(0);
			dos.writeShort(0);
			
			// There is no string index
			dos.writeShort(0);
			
			// Write program header (only one)
			dos.writeInt(1);	// Load this section
			switch (bits)
			{
					// 32-bit
				case 32:
					System.err.println("TODO -- Section offset+size");
					dos.writeInt(codebase);	// Offset in file
					dos.writeInt(0);	// Virtual load address
					dos.writeInt(0);	// Physical address?
					dos.writeInt(programsize);	// Program size (in file)
					dos.writeInt(programsize);	// Program size (in memory)
					dos.writeInt(5);	// RX
					
					// Padding
					dos.writeInt(0);
					break;
				
					// 64-bit
				case 64:
					System.err.println("TODO -- Section offset+size");
					dos.writeInt(5);	// RX
					dos.writeLong(codebase);	// Offset in file
					dos.writeLong(0);	// Virtual load address
					dos.writeLong(0);	// Physical address?
					dos.writeLong(programsize);	// Program size in file
					dos.writeLong(programsize);	// Program size in memory
					
					// Padding
					dos.writeLong(0);
					break;
				
					// Unknown
				default:
					throw new RuntimeException("OOPS");
			}
			
			// Write blob entry table
			System.err.println("TODO -- Write blob table");
			
			// Write blob information
			for (int i = 0; i < n; i++)
			{
				System.err.println("TODO -- Write blob data");
			}
		}
		*/
	}
	
	/**
	 * Preloads blobs into byte arrays since there are unknowns when it comes
	 * to size.
	 *
	 * @param __blobs The blobs to preload.
	 * @return The preloaded blobs.
	 * @throws IOException On read errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2016/08/09
	 */
	private byte[][] __preloadBlobs(InputStream[] __blobs)
		throws IOException, NullPointerException
	{
		// Check
		if (__blobs == null)
			throw new NullPointerException("NARG");
		
		// Preload all blobs
		int n = __blobs.length;
		byte[][] rv = new byte[n][];
		byte[] buf = new byte[512];
		for (int i = 0; i < n; i++)
			try (InputStream is = __blobs[i];
				ByteArrayOutputStream os = new ByteArrayOutputStream())
			{
				// Copy data
				for (;;)
				{
					int rc = is.read(buf);
					
					// EOF?
					if (rc < 0)
						break;
					
					// Write
					os.write(buf, 0, rc);
				}
				
				// Extract the byte array
				rv[i] = os.toByteArray();
			}
		
		// Return it
		return rv;
	}
}

