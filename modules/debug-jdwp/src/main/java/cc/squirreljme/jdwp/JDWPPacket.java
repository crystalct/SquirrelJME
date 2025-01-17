// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.jdwp;

import cc.squirreljme.jdwp.views.JDWPViewThread;
import cc.squirreljme.jdwp.views.JDWPViewType;
import cc.squirreljme.runtime.cldc.debug.Debugging;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Deque;

/**
 * Represents a packet for JDWP.
 * 
 * This class is mutable and as such must be thread safe.
 *
 * @since 2021/03/10
 */
public final class JDWPPacket
	implements Closeable
{
	/** Flag used for replies. */
	public static final short FLAG_REPLY =
		0x80;
	
	/** Grow size. */
	private static final byte _GROW_SIZE =
		32;
	
	/** The queue where packets will go when done. */
	private final Reference<Deque<JDWPPacket>> _queue;
	
	/** The ID of this packet. */
	volatile int _id;
	
	/** The flags for this packet. */
	volatile int _flags;
	
	/** The command set (if not a reply). */
	volatile int _commandSet =
		-1;
	
	/* The command (if not a reply). */
	volatile int _command =
		-1;
	
	/** The error code (if a reply). */
	volatile ErrorType _errorCode;
	
	/** The packet data. */
	private volatile byte[] _data;
	
	/** The length of the data. */
	private volatile int _length;
	
	/** The read position. */
	private volatile int _readPos;
	
	/** Is this packet open? */
	private volatile boolean _open;
	
	/**
	 * Initializes the packet with the queue it will go back into whenever
	 * it is done being used.
	 * 
	 * @param __queue The queue for packets.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/03/10
	 */
	JDWPPacket(Deque<JDWPPacket> __queue)
		throws NullPointerException
	{
		if (__queue == null)
			throw new NullPointerException("NARG");
		
		this._queue = new WeakReference<>(__queue);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/03/10
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	@Override
	public void close()
		throws JDWPException
	{
		synchronized (this)
		{
			// Ignore if closed already (prevent double queue add)
			if (!this._open)
				return;
			
			// Set to closed
			this._open = false;
			
			// Return to the queue
			Deque<JDWPPacket> queue = this._queue.get();
			if (queue != null)
				synchronized (queue)
				{
					queue.add(this);
				}
		}
	}
	
	/**
	 * Returns the command Id.
	 * 
	 * @return The command Id.
	 * @since 2021/03/13
	 */
	protected int command()
	{
		synchronized (this)
		{
			// Ensure it is valid
			this.__checkOpen();
			this.__checkType(false);
			
			return this._command;
		}
	}
	
	/**
	 * Returns the command set for the packet.
	 * 
	 * @return The command set.
	 * @since 2021/03/12
	 */
	public JDWPCommandSet commandSet()
	{
		synchronized (this)
		{
			// Ensure it is valid
			this.__checkOpen();
			this.__checkType(false);
			
			// Map the ID
			return JDWPCommandSet.of(this._commandSet);
		}
	}
	
	/**
	 * Returns a copy of the given packet.
	 * 
	 * @param __packet The packet to copy from.
	 * @return {@code this}.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/04/30
	 */
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	protected JDWPPacket copyOf(JDWPPacket __packet)
		throws NullPointerException
	{
		if (__packet == null)
			throw new NullPointerException("NARG");
		
		// Read from other packet to prevent potential deadlock
		int id;
		int flags;
		int commandSet;
		int command;
		ErrorType errorCode;
		byte[] data;
		int length;
		int readPos;
		synchronized (__packet)
		{
			id = __packet._id;
			flags = __packet._flags;
			commandSet = __packet._commandSet;
			command = __packet._command;
			errorCode = __packet._errorCode;
			data = __packet._data;
			length = __packet._length;
			readPos = __packet._readPos;
		}
		
		// Set packet details
		synchronized (this)
		{
			this._id = id;
			this._flags = flags;
			this._commandSet = commandSet;
			this._command = command;
			this._errorCode = errorCode;
			this._length = length;
			this._readPos = readPos;
			
			// Data might not even be valid here
			if (data != null)
			{
				// Can we get away with only copying part of the array?
				byte[] ourData = this._data;
				if (ourData != null && ourData.length >= data.length)
					System.arraycopy(data, 0,
						ourData, 0, data.length);
				
				// Larger size, which will require array re-allocation
				else
					this._data = data.clone();
			}
		}
		
		return this;
	}
	
	/**
	 * Returns the packet ID.
	 * 
	 * @return The packet it.
	 * @since 2021/03/12
	 */
	public int id()
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			return this._id;
		}
	}
	
	/**
	 * Is this a reply packet?
	 * 
	 * @return If this is a reply.
	 * @since 2021/03/11
	 */
	public boolean isReply()
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			return (this._flags & JDWPPacket.FLAG_REPLY) != 0;
		}
	}
	
	/**
	 * Reads the given array from the packet.
	 * 
	 * @param __controller The controller used.
	 * @param __nullable Can this be null?
	 * @return The array value.
	 * @throws JDWPException If this does not refer to a valid array.
	 * @since 2021/04/11
	 */
	public final Object readArray(JDWPController __controller,
		boolean __nullable)
		throws JDWPException
	{
		Object object = this.readObject(__controller, __nullable);
		
		// Is this an invalid array?
		if (__controller.viewObject().arrayLength(object) < 0)
		{
			if (__nullable && object == null)
				return null;
			
			// Fail with invalid thread
			throw ErrorType.INVALID_ARRAY.toss(object,
				System.identityHashCode(object));
		}
		
		return object;
	}
	
	/**
	 * Reads a single boolean from the packet
	 * 
	 * @return The single read value.
	 * @throws JDWPException If the end of the packet was reached.
	 * @since 2021/04/17
	 */
	public boolean readBoolean()
		throws JDWPException
	{
		return this.readByte() != 0;
	}
	
	/**
	 * Reads a single byte from the packet
	 * 
	 * @return The single read value.
	 * @throws JDWPException If the end of the packet was reached.
	 * @since 2021/03/13
	 */
	public final byte readByte()
		throws JDWPException
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			// {@squirreljme.error AG0d End of packet reached. 
			// (The packet size)}
			int readPos = this._readPos;
			if (readPos >= this._length)
				throw new JDWPException("AG0d " + readPos);
			
			// Read in and increment the position
			byte rv = this._data[readPos];
			this._readPos = readPos + 1;
			return rv;
		}
	}
	
	/**
	 * Reads the given frame from the packet.
	 * 
	 * @param __controller The controller used.
	 * @param __nullable Can this be null?
	 * @return The frame value.
	 * @throws JDWPException If this does not refer to a valid frame.
	 * @since 2021/04/11
	 */
	protected Object readFrame(JDWPController __controller, boolean __nullable)
	{
		int id = this.readId();
		Object frame = __controller.state.items.get(id);
		
		// Is this valid?
		if (!__controller.viewFrame().isValid(frame))
		{
			if (__nullable && frame == null)
				return null;
			
			// Fail with invalid thread
			throw ErrorType.INVALID_FRAME_ID.toss(frame, id);
		}
		
		return frame;
	}
	
	/**
	 * Reads an identifier from the packet.
	 * 
	 * @return The single read value.
	 * @throws JDWPException If the end of the packet was reached.
	 * @since 2021/03/13
	 */
	public final int readId()
		throws JDWPException
	{
		return this.readInt();
	}
	
	/**
	 * Reads an integer from the packet
	 * 
	 * @return The single read value.
	 * @throws JDWPException If the end of the packet was reached.
	 * @since 2021/03/13
	 */
	public final int readInt()
		throws JDWPException
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			// Read in each byte
			return ((this.readByte() & 0xFF) << 24) |
				((this.readByte() & 0xFF) << 16) |
				((this.readByte() & 0xFF) << 8) |
				(this.readByte() & 0xFF);
		}
	}
	
	/**
	 * Reads a location from the packet.
	 * 
	 * @param __controller The controller to read from.
	 * @return The given location.
	 * @throws JDWPException If the location is not valid.
	 * @since 2021/04/17
	 */
	public JDWPLocation readLocation(JDWPController __controller)
		throws JDWPException
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			// Ignore the type tag, we do not need to know the
			// difference between interfaces and classes
			this.readByte();
			
			// Make sure the type and method are valid
			JDWPViewType viewType = __controller.viewType();
			Object type = this.readType(__controller, false);
			int methodDx = this.readId();
			if (!viewType.isValidMethod(type, methodDx))
				throw ErrorType.INVALID_METHOD_ID.toss(type, methodDx,
					null);
			
			// Build location
			return new JDWPLocation(type, methodDx, this.readLong(),
				viewType.methodName(type, methodDx),
				viewType.methodSignature(type, methodDx));
		}
	}
	
	/**
	 * Reads aa long from the packet
	 * 
	 * @return The single read value.
	 * @throws JDWPException If the end of the packet was reached.
	 * @since 2021/03/17
	 */
	public long readLong()
		throws JDWPException
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			// Read in each byte
			return ((this.readByte() & 0xFFL) << 56) |
				((this.readByte() & 0xFFL) << 48) |
				((this.readByte() & 0xFFL) << 40) |
				((this.readByte() & 0xFFL) << 32) |
				((this.readByte() & 0xFFL) << 24) |
				((this.readByte() & 0xFF) << 16) |
				((this.readByte() & 0xFF) << 8) |
				(this.readByte() & 0xFF);
		}
	}
	
	/**
	 * Reads the given object from the packet.
	 * 
	 * @param __controller The controller used.
	 * @param __nullable Can this be null?
	 * @return The object value.
	 * @throws JDWPException If this does not refer to a valid object.
	 * @since 2021/04/11
	 */
	public final Object readObject(JDWPController __controller,
		boolean __nullable)
		throws JDWPException
	{
		int id = this.readId();
		Object object = __controller.state.items.get(id);
		
		// Is this not a valid object?
		if (!__controller.viewObject().isValid(object))
		{
			// If this is a valid thread, then bounce through to the thread's
			// instance object
			if (__controller.viewThread().isValid(object))
			{
				Object alt = __controller.viewThread().instance(object);
				if (__controller.viewObject().isValid(alt))
				{
					// Make sure it is registered
					__controller.state.items.put(alt);
					return alt;
				}
			}
			
			// If this a valid class, bounce to the class instance object
			if (__controller.viewType().isValid(object))
			{
				// Double check if it is valid
				Object alt = __controller.viewType().instance(object);
				if (__controller.viewObject().isValid(alt))
				{
					// Make sure it is registered
					__controller.state.items.put(alt);
					return alt;
				}
			}
			
			// Not valid?
			if (__nullable && object == null)
				return null;
			
			// Fail with invalid thread
			throw ErrorType.INVALID_OBJECT.toss(object, id);
		}
		
		return object;
	}
	
	/**
	 * Reads the specified string.
	 * 
	 * @return The read string.
	 * @throws JDWPException If it could not be read.
	 * @since 2021/03/13
	 */
	public final String readString()
		throws JDWPException
	{
		synchronized (this)
		{
			// Ensure this is open
			this.__checkOpen();
			
			// Read length
			int len = this.readInt();
			
			// Read in UTF data
			byte[] utf = new byte[len];
			for (int i = 0; i < len; i++)
				utf[i] = this.readByte();
			
			// Build final string
			try
			{
				return new String(utf, "utf-8");
			}
			catch (UnsupportedEncodingException __e)
			{
				// {@squirreljme.error AG0f UTF-8 not supported?}
				throw new JDWPException("AG0f", __e);
			}
		}
	}
	
	/**
	 * Reads the given thread from the packet.
	 * 
	 * @param __controller The controller used.
	 * @param __nullable Can this be null?
	 * @return The object value.
	 * @throws JDWPException If this does not refer to a valid thread.
	 * @since 2021/04/11
	 */
	public final Object readThread(JDWPController __controller,
		boolean __nullable)
		throws JDWPException
	{
		int id = this.readId();
		Object thread = __controller.state.items.get(id);
		
		// Is this valid?
		JDWPViewThread viewThread = __controller.viewThread();
		if (!viewThread.isValid(thread))
		{
			// Threads may be aliased to objects, and as such if we try to
			// read a thread that is aliased by an object we need to get
			// the original thread back
			// Scan through all threads and see if we can find it again
			if (__controller.viewObject().isValid(thread))
				for (Object check : __controller.__allThreads())
					if (thread == viewThread.instance(check))
					{
						// Make sure it is registered
						__controller.state.items.put(check);
						return check;
					}
			
			if (__nullable && thread == null)
				return null;
			
			// Fail with invalid thread
			throw ErrorType.INVALID_THREAD.toss(thread, id);
		}
		
		return thread;
	}
	
	/**
	 * Reads the given thread group from the packet.
	 * 
	 * @param __controller The controller used.
	 * @param __nullable Can this be null?
	 * @return The thread group.
	 * @throws JDWPException If this does not refer to a valid thread group.
	 * @since 2021/04/14
	 */
	public final Object readThreadGroup(JDWPController __controller,
		boolean __nullable)
		throws JDWPException
	{
		int id = this.readId();
		Object group = __controller.state.items.get(id);
		
		// Is this valid?
		if (!__controller.viewThreadGroup().isValid(group))
		{
			if (__nullable && group == null)
				return null;
			
			// Fail with invalid thread
			throw ErrorType.INVALID_THREAD.toss(group, id);
		}
		
		return group;
	}
	
	/**
	 * Reads the given type from the packet.
	 * 
	 * @param __controller The controller used.
	 * @param __nullable Can this be null?
	 * @return The type value.
	 * @throws JDWPException If this does not refer to a valid type.
	 * @since 2021/04/12
	 */
	public final Object readType(JDWPController __controller,
		boolean __nullable)
		throws JDWPException
	{
		int id = this.readId();
		Object object = __controller.state.items.get(id);
		
		// Is this valid?
		if (!__controller.viewType().isValid(object))
		{
			// We may be trying to read the type of an object, so we need to
			// alias to that
			if (__controller.viewObject().isValid(object))
			{
				Object alt = __controller.viewObject().type(object);
				if (__controller.viewType().isValid(alt))
				{
					// Make it is registered
					__controller.state.items.put(alt);
					return alt;
				}
			}
			
			if (__nullable && object == null)
				return null;
			
			// Fail with invalid thread
			throw ErrorType.INVALID_CLASS.toss(object, id);
		}
		
		return object;
	}
	
	/**
	 * Resets and opens the packet.
	 * 
	 * @param __open Should this be opened?
	 * @since 2021/03/12
	 */
	protected final void resetAndOpen(boolean __open)
	{
		synchronized (this)
		{
			// {@squirreljme.error AG05 Cannot reset an open packet.}
			if (this._open)
				throw new JDWPException("AG05");
			
			this._id = 0;
			this._flags = 0;
			this._commandSet = -1;
			this._command = -1;
			this._errorCode = null;
			this._length = 0;
			this._readPos = 0;
			
			// Mark as open?
			this._open = __open;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2021/03/13
	 */
	@Override
	public final String toString()
	{
		synchronized (this)
		{
			if (!this._open)
				return "JDWPPacket:Closed";
			
			// Find the command set
			JDWPCommandSet commandSet = JDWPCommandSet.of(this._commandSet);
			JDWPCommand command = (commandSet == null ? null :
				commandSet.command(this._command));
			
			int flags = this._flags;
			return String.format("JDWPPacket[id=%08x,flags=%02x,len=%d]:%s",
				this._id, flags, this._length,
				((flags & JDWPPacket.FLAG_REPLY) != 0 ?
					(this._errorCode == ErrorType.NO_ERROR ? "" :
						String.format("[error=%s]", this._errorCode)) :
					String.format("[cmdSet=%s;cmd=%s]",
						(commandSet == null ||
							commandSet == JDWPCommandSet.UNKNOWN ?
							this._commandSet : commandSet),
						(command == null ? this._command : command))));
		}
	}
	
	/**
	 * Writes to the given packet.
	 * 
	 * @param __b The buffer.
	 * @param __o The offset.
	 * @param __l The length.
	 * @throws IndexOutOfBoundsException If the offset and/or length are
	 * negative or exceed the array bounds.
	 * @throws JDWPException If this could not be written.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/03/21
	 */
	public void write(byte[] __b, int __o, int __l)
		throws IndexOutOfBoundsException, JDWPException, NullPointerException
	{
		if (__b == null)
			throw new NullPointerException("NARG");
		if (__o < 0 || __l < 0 || (__o + __l) > __b.length)
			throw new IndexOutOfBoundsException("IOOB");
		
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			for (int i = 0; i < __l; i++)
				this.writeByte(__b[__o + i]);
		}
	}
	
	/**
	 * Writes the boolean to the output.
	 * 
	 * @param __b The boolean to write.
	 * @throws JDWPException If it could not be written.
	 * @since 2021/03/13
	 */
	public void writeBoolean(boolean __b)
		throws JDWPException
	{
		this.writeByte((__b ? 1 : 0));
	}
	
	/**
	 * Writes the given byte.
	 * 
	 * @param __v The value to write.
	 * @since 2021/03/12
	 */
	public void writeByte(int __v)
	{
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Where this will be going
			int length = this._length;
			byte[] data = this._data;
			
			// Too small?
			if (data == null || length + 1 > data.length)
				this._data = (data = (data == null ?
					new byte[JDWPPacket._GROW_SIZE] : Arrays.copyOf(
					data, data.length + JDWPPacket._GROW_SIZE)));
			
			// Write the byte
			data[length] = (byte)__v;
			this._length = length + 1;
		}
	}
	
	/**
	 * Writes an ID to the output.
	 * 
	 * @param __v The value to write.
	 * @throws JDWPException If it could not be written.
	 * @since 2021/04/10
	 */
	public void writeId(int __v)
		throws JDWPException
	{
		this.writeInt(__v);
	}
	
	/**
	 * Writes an integer to the output.
	 * 
	 * @param __v The value to write.
	 * @throws JDWPException If it could not be written.
	 * @since 2021/03/12
	 */
	public void writeInt(int __v)
		throws JDWPException
	{
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Write the data
			this.writeByte(__v >> 24);
			this.writeByte(__v >> 16);
			this.writeByte(__v >> 8);
			this.writeByte(__v);
		}
	}
	
	/**
	 * Writes a location into the packet.
	 * 
	 * @param __controller The controller used. 
	 * @param __location The location used.
	 * @throws JDWPException If the packet could not be written.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/04/25
	 */
	public void writeLocation(JDWPController __controller,
		JDWPLocation __location)
		throws JDWPException, NullPointerException
	{
		this.writeLocation(__controller, __location.type,
			__location.methodDx, __location.codeDx);
	}
	
	/**
	 * Writes a location into the packet.
	 * 
	 * @param __controller The controller used. 
	 * @param __class The class to write.
	 * @param __atMethodIndex The method index.
	 * @param __atCodeIndex The code index.
	 * @throws JDWPException If the packet could not be written.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/04/11
	 */
	public void writeLocation(JDWPController __controller, Object __class,
		int __atMethodIndex, long __atCodeIndex)
		throws JDWPException, NullPointerException
	{
		if (__controller == null)
			throw new NullPointerException("NARG");
		
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Write class located within
			this.writeByte(JDWPUtils.classType(__controller, __class).id);
			this.writeId(System.identityHashCode(__class));
			
			// Write the method ID and the special index (address)
			this.writeId(__atMethodIndex);
			
			// Where is this located? Note that the index is a long here
			// although such a high value should hopefully never be needed
			// in SquirrelJME
			this.writeLong(__atCodeIndex);
		}
	}
	
	/**
	 * Writes a long to the output.
	 * 
	 * @param __v The value to write.
	 * @throws JDWPException If it could not be written.
	 * @since 2021/03/13
	 */
	public void writeLong(long __v)
		throws JDWPException
	{
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Write the data
			this.writeByte((byte)(__v >> 56));
			this.writeByte((byte)(__v >> 48));
			this.writeByte((byte)(__v >> 40));
			this.writeByte((byte)(__v >> 32));
			this.writeByte((byte)(__v >> 24));
			this.writeByte((byte)(__v >> 16));
			this.writeByte((byte)(__v >> 8));
			this.writeByte((byte)(__v));
		}
	}
	
	/**
	 * Writes a short to the output.
	 * 
	 * @param __v The value to write.
	 * @throws JDWPException If it could not be written.
	 * @since 2021/03/19
	 */
	public void writeShort(int __v)
		throws JDWPException
	{
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Write the data
			this.writeByte(__v >> 8);
			this.writeByte(__v);
		}
	}
	
	/**
	 * Writes the string to the output.
	 * 
	 * @param __string The string to write.
	 * @throws JDWPException If it could not be written.
	 * @since 2021/03/13
	 */
	public void writeString(String __string)
		throws JDWPException
	{
		synchronized (this)
		{
			byte[] bytes;
			try
			{
				bytes = __string.getBytes("utf-8");
			}
			
			// {@squirreljme.error AG0e UTF-8 is not supported?}
			catch (UnsupportedEncodingException __e)
			{
				throw new JDWPException("AG0e", __e);
			}
			
			// Write length
			this.writeInt(bytes.length);
			
			// Write all the bytes
			for (byte b : bytes)
				this.writeByte(b);
		}
	}
	
	/**
	 * Writes to the output.
	 * 
	 * @param __out The output.
	 * @throws IOException On write errors.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/03/12
	 */
	protected void writeTo(DataOutputStream __out)
		throws IOException, NullPointerException
	{
		if (__out == null)
			throw new NullPointerException("NARG");
		
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Write shared header (includes header size)
			__out.writeInt(this._length + CommLink._HEADER_SIZE);
			__out.writeInt(this._id);
			__out.writeByte(this._flags);
			
			// Reply packet
			if (this.isReply())
				__out.writeShort(this._errorCode.id);
			
			// Command packet
			else
			{
				__out.writeByte(this._commandSet);
				__out.writeByte(this._command);
			}
			
			// Write output data
			__out.write(this._data, 0, this._length);
		}
	}
	
	/**
	 * Writes a value to the output.
	 * 
	 * @param __val The value to write.
	 * @param __context Context value which may adjust how the value is
	 * written, this may be {@code null}.
	 * @param __untag Untagged value?
	 * @throws JDWPException If it failed to write.
	 * @since 2021/04/11
	 */
	public void writeValue(Object __val, JDWPValueTag __context,
		boolean __untag)
		throws JDWPException
	{
		// We really meant to write a value here
		if (__val instanceof JDWPValue)
		{
			this.writeValue(((JDWPValue)__val).get(), __context, __untag);
			return;
		}
		
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			// Depends on the context
			switch (__context)
			{
					// Void type
				case VOID:
					this.writeByte('V');
					break;
				
					// Boolean value, may be untagged
				case BOOLEAN:
					if (!__untag)
						this.writeByte('Z');
					this.writeBoolean(((__val instanceof Boolean) ?
						(boolean)__val :
						((Number)__val).longValue() != 0));
					break;
					
					// Byte value, may be untagged
				case BYTE:
					if (!__untag)
						this.writeByte('B');
					this.writeByte(((Number)__val).byteValue());
					break;
					
					// Short value, may be untagged
				case SHORT:
					if (!__untag)
						this.writeByte('S');
					this.writeShort(((Number)__val).shortValue());
					break;
					
					// Character value, may be untagged
				case CHARACTER:
					if (!__untag)
						this.writeByte('C');
					this.writeShort(((__val instanceof Character) ?
						(char)__val :
						((Number)__val).shortValue()));
					break;
					
					// Integer value, may be untagged
				case INTEGER:
					if (!__untag)
						this.writeByte('I');
					this.writeInt(((Number)__val).intValue());
					break;
					
					// Long value, may be untagged
				case LONG:
					if (!__untag)
						this.writeByte('J');
					this.writeLong(((Number)__val).longValue());
					break;
					
					// Float value, may be untagged
				case FLOAT:
					if (!__untag)
						this.writeByte('F');
					this.writeInt(Float.floatToRawIntBits(
						((Number)__val).floatValue()));
					break;
					
					// Long value, may be untagged
				case DOUBLE:
					if (!__untag)
						this.writeByte('D');
					this.writeLong(Double.doubleToRawLongBits(
						((Number)__val).doubleValue()));
					break;
				
					// Objects are always tagged
				case ARRAY:
				case OBJECT:
				case CLASS_OBJECT:
				case CLASS_LOADER:
				case THREAD:
				case THREAD_GROUP:
				case STRING:
					// Write the tag
					switch (__context)
					{
						case ARRAY:
							this.writeByte('[');
							break;
						
						case CLASS_OBJECT:
							this.writeByte('c');
							break;
						
						case CLASS_LOADER:
							this.writeByte('l');
							break;
						
						case THREAD:
							this.writeByte('t');
							break;
						
						case THREAD_GROUP:
							this.writeByte('g');
							break;
						
						case STRING:
							this.writeByte('s');
							break;
						
						default:
							this.writeByte('L');
							break;
					}
					
					this.writeId(System.identityHashCode(__val));
					break;
				
				default:
					throw Debugging.oops(__context);
			}
		}
	}
	
	/**
	 * Writes a void type to the output.
	 * 
	 * @throws JDWPException If it failed to write.
	 * @since 2021/03/19
	 */
	public void writeVoid()
		throws JDWPException
	{
		synchronized (this)
		{
			// Must be an open packet
			this.__checkOpen();
			
			this.writeByte('V');
		}
	}
	
	/**
	 * Checks if the packet is open.
	 * 
	 * @throws IllegalStateException If it is not open.
	 * @since 2021/03/12
	 */
	void __checkOpen()
		throws IllegalStateException
	{
		// {@squirreljme.error AG0b Packet not open.}
		if (!this._open)
			throw new IllegalStateException("AG0b");
	}
	
	/**
	 * Checks if this is a reply packet or not.
	 * 
	 * @param __isReply If this should be reply.
	 * @throws IllegalStateException If it is not a reply packet.
	 * @since 2021/03/12
	 */
	void __checkType(boolean __isReply)
		throws IllegalStateException
	{
		// {@squirreljme.error AG0c Packet type mismatched. (Requested reply?)}
		if (__isReply != this.isReply())
			throw new IllegalStateException("AG0c " + __isReply);
	}
	
	/**
	 * Loads the packet data within.
	 * 
	 * @param __header The header.
	 * @param __data The packet data.
	 * @param __dataLen The data length.
	 * @throws NullPointerException On null arguments.
	 * @since 2021/03/10
	 */
	void __load(byte[] __header, byte[] __data, int __dataLen)
		throws NullPointerException
	{
		synchronized (this)
		{
			// {@squirreljme.error AG0a Packet is already open.}
			if (this._open)
				throw new IllegalStateException("AG0a");
			
			// Grow (or allocate) to fit the data
			byte[] data = this._data;
			if (data == null || data.length < __dataLen)
				this._data = (data = new byte[__dataLen]);
			
			// Copy it in quickly
			System.arraycopy(__data, 0,
				data, 0, __dataLen);
			
			// Common header bits
			this._length = __dataLen;
			this._id = ((__header[4] & 0xFF) << 24) |
				((__header[5] & 0xFF) << 16) |
				((__header[6] & 0xFF) << 8) |
				(__header[7] & 0xFF);
			int flags;
			this._flags = (flags = __header[8]);
			
			// Reply type
			if ((flags & JDWPPacket.FLAG_REPLY) != 0)
			{
				// These are not used
				this._commandSet = -1;
				this._command = -1;
				
				// Read just the error code
				this._errorCode = ErrorType.of(((__header[9] & 0xFF) << 8) |
					(__header[10] & 0xFF));
			}
			
			// Non-reply
			else
			{
				// These are not used
				this._errorCode = null;
				
				// Read the command used
				this._commandSet = __header[9] & 0xFF;
				this._command = __header[10] & 0xFF;
			}
			
			// Becomes open now
			this._open = true;
		}
	}
}
