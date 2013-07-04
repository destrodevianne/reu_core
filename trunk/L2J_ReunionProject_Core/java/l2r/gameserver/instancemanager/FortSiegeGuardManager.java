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
package l2r.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.NpcTable;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.instance.L2FortBallistaInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.entity.Fort;

public class FortSiegeGuardManager
{
	private static final Logger _log = Logger.getLogger(FortSiegeGuardManager.class.getName());
	
	private final Fort _fort;
	protected FastMap<Integer, FastList<L2Spawn>> _siegeGuards = new FastMap<>();
	protected FastList<L2Spawn> _siegeGuardsSpawns;
	
	public FortSiegeGuardManager(Fort fort)
	{
		_fort = fort;
	}
	
	/**
	 * Spawn guards.
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			FastList<L2Spawn> monsterList = getSiegeGuardSpawn().get(getFort().getFortId());
			if (monsterList != null)
			{
				for (L2Spawn spawnDat : monsterList)
				{
					spawnDat.doSpawn();
					if (spawnDat.getLastSpawn() instanceof L2FortBallistaInstance)
					{
						spawnDat.stopRespawn();
					}
					else
					{
						spawnDat.startRespawn();
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error spawning siege guards for fort " + getFort().getName() + ":" + e.getMessage(), e);
		}
	}
	
	/**
	 * Unspawn guards.
	 */
	public void unspawnSiegeGuard()
	{
		try
		{
			FastList<L2Spawn> monsterList = getSiegeGuardSpawn().get(getFort().getFortId());
			
			if (monsterList != null)
			{
				for (L2Spawn spawnDat : monsterList)
				{
					spawnDat.stopRespawn();
					if (spawnDat.getLastSpawn() != null)
					{
						spawnDat.getLastSpawn().doDie(spawnDat.getLastSpawn());
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error unspawning siege guards for fort " + getFort().getName() + ":" + e.getMessage(), e);
		}
	}
	
	/**
	 * Load guards.
	 */
	void loadSiegeGuard()
	{
		_siegeGuards.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM fort_siege_guards Where fortId = ? "))
		{
			ps.setInt(1, getFort().getFortId());
			try (ResultSet rs = ps.executeQuery())
			{
				L2Spawn spawn1;
				L2NpcTemplate template1;
				_siegeGuardsSpawns = new FastList<>();
				while (rs.next())
				{
					int fortId = rs.getInt("fortId");
					template1 = NpcTable.getInstance().getTemplate(rs.getInt("npcId"));
					if (template1 != null)
					{
						spawn1 = new L2Spawn(template1);
						spawn1.setAmount(1);
						spawn1.setLocx(rs.getInt("x"));
						spawn1.setLocy(rs.getInt("y"));
						spawn1.setLocz(rs.getInt("z"));
						spawn1.setHeading(rs.getInt("heading"));
						spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
						spawn1.setLocation(0);
						
						_siegeGuardsSpawns.add(spawn1);
					}
					else
					{
						_log.warning("Missing npc data in npc table for id: " + rs.getInt("npcId"));
					}
					_siegeGuards.put(fortId, _siegeGuardsSpawns);
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Error loading siege guard for fort " + getFort().getName() + ": " + e.getMessage(), e);
		}
	}
	
	public final Fort getFort()
	{
		return _fort;
	}
	
	public final FastMap<Integer, FastList<L2Spawn>> getSiegeGuardSpawn()
	{
		return _siegeGuards;
	}
}
