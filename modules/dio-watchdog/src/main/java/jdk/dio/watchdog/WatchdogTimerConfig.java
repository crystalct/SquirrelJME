// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package jdk.dio.watchdog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jdk.dio.DeviceConfig;

public final class WatchdogTimerConfig
	implements DeviceConfig<WatchdogTimer>, DeviceConfig.HardwareAddressing
{
	public WatchdogTimerConfig(String __a, int __b)
	{
		throw new todo.TODO();
	}
	
	public WatchdogTimerConfig(int __a, int __b)
	{
		throw new todo.TODO();
	}
	
	@Override
	public boolean equals(Object __a)
	{
		throw new todo.TODO();
	}
	
	@Override
	public String getControllerName()
	{
		throw new todo.TODO();
	}
	
	@Override
	public int getControllerNumber()
	{
		throw new todo.TODO();
	}
	
	public int getTimerNumber()
	{
		throw new todo.TODO();
	}
	
	@Override
	public int hashCode()
	{
		throw new todo.TODO();
	}
	
	@Override
	public int serialize(OutputStream __a)
		throws IOException
	{
		if (false)
			throw new IOException();
		throw new todo.TODO();
	}
	
	public static WatchdogTimerConfig deserialize(InputStream __a)
		throws IOException
	{
		if (false)
			throw new IOException();
		throw new todo.TODO();
	}
}


