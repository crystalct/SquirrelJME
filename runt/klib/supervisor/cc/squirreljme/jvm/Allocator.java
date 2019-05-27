// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jvm;

/**
 * This contains the static memory allocator.
 *
 * @since 2019/05/26
 */
public final class Allocator
{
	/** Bit to indicate memory partition is free. */
	public static final int MEMPART_FREE_BIT =
		0x80000000;
	
	/** Memory chunk size offset. */
	public static final int CHUNK_SIZE_OFFSET =
		0;
	
	/** Next chunk address. */
	public static final int CHUNK_NEXT_OFFSET =
		4;
	
	/** The length of chunks. */
	public static final int CHUNK_LENGTH =
		8;
	
	/** The base RAM address. */
	private static volatile int _rambase;
	
	/**
	 * Not used.
	 *
	 * @since 2019/05/26
	 */
	private Allocator()
	{
	}
	
	/**
	 * Allocates the given number of bytes.
	 *
	 * @param __sz The number of bytes to allocate.
	 * @return The address of the allocated data or {@code 0} if there is
	 * not enough memory remaining.
	 * @since 2019/05/26
	 */
	public static final int allocate(int __sz)
	{
		// The number of desired bytes
		int want = CHUNK_LENGTH + (__sz <= 4 ? 4 : ((__sz + 3) & (~3)));
		
		// Go through the memory chunks to locate a free chunk
		int seeker = Allocator._rambase;
		while (seeker != 0)
		{
			// Read chunk properties
			int csz = Assembly.memReadInt(seeker, CHUNK_SIZE_OFFSET),
				cnx = Assembly.memReadInt(seeker, CHUNK_NEXT_OFFSET);
			
			// Is this chunk free?
			boolean isfree = ((csz & MEMPART_FREE_BIT) != 0);
			csz &= (~MEMPART_FREE_BIT);
			
			// Chunk is free and fits the amount of memory we want
			if (isfree && want <= csz)
			{
				// The return pointer is past the chunk info
				int rv = seeker + CHUNK_LENGTH;
				
				// There is more space left in this chunk than what we need
				// so it will be split
				if (want < csz)
				{
					// The position of the new chunk
					int nextseek = seeker + want;
					
					// The size of the next chunk is whatever is left of
					// what was claimed
					Assembly.memWriteInt(nextseek, CHUNK_SIZE_OFFSET,
						(csz - want) | MEMPART_FREE_BIT);
					
					// The next chunk is the next the chunk we claimed
					Assembly.memWriteInt(nextseek, CHUNK_NEXT_OFFSET,
						cnx);
					
					// The size of our current chunk is the wanted size
					Assembly.memWriteInt(seeker, CHUNK_SIZE_OFFSET,
						want);
					
					// The next chunk of the current chunk is the new chunk
					Assembly.memWriteInt(seeker, CHUNK_NEXT_OFFSET,
						nextseek);
				}
				
				// Clear out memory since Java expects the data to be
				// initialized to zero always
				for (int i = CHUNK_LENGTH; i < want; i += 4)
					Assembly.memWriteInt(seeker, i, 0);
				
				// Use this chunk
				return rv;
			}
			
			// Go to the next chunk
			seeker = cnx;
		}
		
		// Could not find a free chunk
		return 0;
	}
	
	/**
	 * Initializes the RAM links.
	 *
	 * @param __rambase The base of RAM.
	 * @param __ramsize The amount of RAM available.
	 * @since 2019/05/26
	 */
	static final void __initRamLinks(int __rambase, int __ramsize)
	{
		// Loops through all blocks
		for (int seeker = __rambase;;)
		{
			// Read current and next offset
			int csz = Assembly.memReadInt(seeker, CHUNK_SIZE_OFFSET),
				cnx = Assembly.memReadInt(seeker, CHUNK_NEXT_OFFSET);
			
			// Reached the terminator, whatever is left is the remaining
			// amount of free memory
			if (csz == 0 || cnx == 0)
			{
				// The size of this block is whatever remains in memory
				Assembly.memWriteInt(seeker, CHUNK_SIZE_OFFSET,
					(__ramsize - seeker) | MEMPART_FREE_BIT);
				
				// And ensure the next block is always zero!
				Assembly.memWriteInt(seeker, CHUNK_NEXT_OFFSET,
					0);
				
				// Stop
				break;
			}
			
			// Go to the next block in memory
			seeker = cnx;
		}
		
		// Set memory parameters
		Allocator._rambase = __rambase;
	}
}