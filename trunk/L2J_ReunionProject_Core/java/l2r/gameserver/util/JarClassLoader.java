/*
 * Copyright (C) 2004-2013 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class loader for the dynamic extensions used by DynamicExtension class.
 * @author galun
 */
public class JarClassLoader extends ClassLoader
{
	private static Logger _log = LoggerFactory.getLogger(JarClassLoader.class.getCanonicalName());
	private final HashSet<String> _jars = new HashSet<>();
	
	public void addJarFile(String filename)
	{
		_jars.add(filename);
	}
	
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException
	{
		try
		{
			byte[] b = loadClassData(name);
			return defineClass(name, b, 0, b.length);
		}
		catch (Exception e)
		{
			throw new ClassNotFoundException(name);
		}
	}
	
	private byte[] loadClassData(String name) throws IOException
	{
		byte[] classData = null;
		final String fileName = name.replace('.', '/') + ".class";
		for (String jarFile : _jars)
		{
			
			final File file = new File(jarFile);
			try (ZipFile zipFile = new ZipFile(file);)
			{
				final ZipEntry entry = zipFile.getEntry(fileName);
				if (entry == null)
				{
					continue;
				}
				classData = new byte[(int) entry.getSize()];
				try (DataInputStream zipStream = new DataInputStream(zipFile.getInputStream(entry)))
				{
					zipStream.readFully(classData, 0, (int) entry.getSize());
				}
				break;
			}
			catch (IOException e)
			{
				_log.warn(jarFile + ": " + e.getMessage(), e);
				continue;
			}
		}
		if (classData == null)
		{
			throw new IOException("class not found in " + _jars);
		}
		return classData;
	}
}
