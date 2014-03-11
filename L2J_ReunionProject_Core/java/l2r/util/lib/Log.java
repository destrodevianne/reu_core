/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.util.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import l2r.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 0.1, 2005-06-06
 * @author Balancer
 */
public class Log
{
	private static final Logger _log = LoggerFactory.getLogger(Log.class);
	
	public static final void add(String text, String cat)
	{
		String date = (new SimpleDateFormat("yy.MM.dd H:mm:ss")).format(new Date());
		String curr = (new SimpleDateFormat("yyyy-MM-dd-")).format(new Date());
		new File("log/game").mkdirs();
		
		final File file = new File("log/game/" + (curr != null ? curr : "") + (cat != null ? cat : "unk") + ".txt");
		try (FileWriter save = new FileWriter(file, true))
		{
			save.write("[" + date + "] " + text + Config.EOL);
		}
		catch (IOException e)
		{
			_log.warn("Error saving logfile: ", e);
		}
	}
}
