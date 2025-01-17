// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.springcoat;

import cc.squirreljme.emulator.MLECallWouldFail;
import cc.squirreljme.jvm.mle.TerminalShelf;
import cc.squirreljme.vm.springcoat.exceptions.SpringMLECallError;

/**
 * Functions for {@link MLETerminal}.
 *
 * @since 2020/06/18
 */
public enum MLETerminal
	implements MLEFunction
{
	/** {@link TerminalShelf#close(int)}. */
	CLOSE("close:(I)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/06
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			try
			{
				int fd = (int)__args[0];
				
				return __thread.machine.terminalPipes.mleClose(fd);
			}
			catch (MLECallWouldFail e)
			{
				throw new SpringMLECallError(e.getMessage(), e);
			}
		}
	}, 
	
	/** {@link TerminalShelf#flush(int)}. */
	FLUSH("flush:(I)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			try
			{
				int fd = (int)__args[0];
				
				return __thread.machine.terminalPipes.mleFlush(fd);
			}
			catch (MLECallWouldFail e)
			{
				throw new SpringMLECallError(e.getMessage(), e);
			}
		}
	},
	
	/** {@link TerminalShelf#write(int, int)}. */
	WRITE_BYTE("write:(II)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			try
			{
				int fd = (int)__args[0];
				int value = (int)__args[1];
				
				return __thread.machine.terminalPipes.mleWrite(fd, value);
			}
			catch (MLECallWouldFail e)
			{
				throw new SpringMLECallError(e.getMessage(), e);
			}
		}
	},
	
	/** {@link TerminalShelf#write(int, byte[], int, int)}. */
	WRITE_BYTES("write:(I[BII)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			try
			{
				if (!(__args[1] instanceof SpringArrayObjectByte))
					throw new SpringMLECallError("Not a byte array.");
				
				int fd = (int)__args[0];
				SpringArrayObjectByte buf = (SpringArrayObjectByte)__args[1];
				int off = (int)__args[2];
				int len = (int)__args[3];
				
				return __thread.machine.terminalPipes
					.mleWrite(fd, buf.array(), off, len);
			}
			catch (MLECallWouldFail e)
			{
				throw new SpringMLECallError(e.getMessage(), e);
			}
		}
	},
	
	/* End. */
	;
	
	/** The dispatch key. */
	protected final String key;
	
	/**
	 * Initializes the dispatcher info.
	 *
	 * @param __key The key.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/06/18
	 */
	MLETerminal(String __key)
		throws NullPointerException
	{
		if (__key == null)
			throw new NullPointerException("NARG");
		
		this.key = __key;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/06/18
	 */
	@Override
	public String key()
	{
		return this.key;
	}
}
