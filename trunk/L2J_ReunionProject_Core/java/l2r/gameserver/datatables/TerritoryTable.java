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
package l2r.gameserver.datatables;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.L2Territory;
import l2r.util.lib.SqlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Balancer, Mr
 */
public class TerritoryTable
{
	private static final Logger _log = LoggerFactory.getLogger(TerritoryTable.class);
	
	private static final Map<Integer, L2Territory> _territory = new HashMap<>();
	
	/**
	 * Instantiates a new territory.
	 */
	protected TerritoryTable()
	{
		load();
	}
	
	/**
	 * Gets the random point.
	 * @param terr the territory Id?
	 * @return the random point
	 */
	public int[] getRandomPoint(int terr)
	{
		return _territory.get(terr).getRandomPoint();
	}
	
	/**
	 * Gets the proc max.
	 * @param terr the territory Id?
	 * @return the proc max
	 */
	public int getProcMax(int terr)
	{
		return _territory.get(terr).getProcMax();
	}
	
	/**
	 * Load the data from database.
	 */
	public void load()
	{
		_territory.clear();
		Integer[][] point = SqlUtils.get2DIntArray(new String[]
		{
			"loc_id",
			"loc_x",
			"loc_y",
			"loc_zmin",
			"loc_zmax",
			"proc"
		}, "locations", "loc_id > 0");
		for (Integer[] row : point)
		{
			Integer terr = row[0];
			if (terr == null)
			{
				_log.warn(getClass().getSimpleName() + ": Null territory!");
				continue;
			}
			
			if (_territory.get(terr) == null)
			{
				L2Territory t = new L2Territory(terr);
				_territory.put(terr, t);
			}
			_territory.get(terr).add(row[1], row[2], row[3], row[4], row[5]);
		}
	}
	
	/**
	 * Gets the single instance of Territory.
	 * @return single instance of Territory
	 */
	public static TerritoryTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TerritoryTable _instance = new TerritoryTable();
	}
}
