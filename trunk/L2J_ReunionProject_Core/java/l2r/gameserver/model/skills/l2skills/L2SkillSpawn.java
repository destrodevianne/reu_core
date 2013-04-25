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
package l2r.gameserver.model.skills.l2skills;

import java.util.logging.Level;
import java.util.logging.Logger;

import l2r.gameserver.datatables.NpcTable;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.skills.L2Skill;
import l2r.util.Rnd;

/**
 * @author Zoey76
 */
public class L2SkillSpawn extends L2Skill
{
	private static final Logger _log = Logger.getLogger(L2SkillSpawn.class.getName());
	
	private final int _despawnDelay;
	private final boolean _summonSpawn;
	private final boolean _randomOffset;
	
	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_despawnDelay = set.getInteger("despawnDelay", 0);
		_summonSpawn = set.getBool("isSummonSpawn", false);
		_randomOffset = set.getBool("randomOffset", true);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		if (getNpcId() == 0)
		{
			_log.warning("NPC ID not defined for skill ID:" + getId());
			return;
		}
		
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(getNpcId());
		if (template == null)
		{
			_log.warning("Spawn of the nonexisting NPC ID:" + getNpcId() + ", skill ID:" + getId());
			return;
		}
		
		L2Spawn spawn;
		try
		{
			spawn = new L2Spawn(template);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception in L2SkillSpawn: " + e.getMessage(), e);
			return;
		}
		
		int x = caster.getX();
		int y = caster.getY();
		if (_randomOffset)
		{
			x += (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
			y += (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20));
		}
		
		spawn.setLocx(x);
		spawn.setLocy(y);
		spawn.setLocz(caster.getZ());
		spawn.setHeading(caster.getHeading());
		spawn.stopRespawn();
		
		final L2Npc npc = spawn.doSpawn(_summonSpawn);
		npc.setName(template.getName());
		npc.setTitle(caster.getName());
		npc.setSummoner(caster);
		if (_despawnDelay > 0)
		{
			npc.scheduleDespawn(_despawnDelay);
		}
		npc.setIsRunning(false); // Broadcast info
	}
}
