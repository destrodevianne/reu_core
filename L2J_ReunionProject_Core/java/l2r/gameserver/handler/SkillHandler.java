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
package l2r.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.scripts.handlers.skillhandlers.BallistaBomb;
import l2r.gameserver.scripts.handlers.skillhandlers.BeastSkills;
import l2r.gameserver.scripts.handlers.skillhandlers.Blow;
import l2r.gameserver.scripts.handlers.skillhandlers.Continuous;
import l2r.gameserver.scripts.handlers.skillhandlers.CpDamPercent;
import l2r.gameserver.scripts.handlers.skillhandlers.Craft;
import l2r.gameserver.scripts.handlers.skillhandlers.DeluxeKey;
import l2r.gameserver.scripts.handlers.skillhandlers.Detection;
import l2r.gameserver.scripts.handlers.skillhandlers.Disablers;
import l2r.gameserver.scripts.handlers.skillhandlers.Dummy;
import l2r.gameserver.scripts.handlers.skillhandlers.Fishing;
import l2r.gameserver.scripts.handlers.skillhandlers.FishingSkill;
import l2r.gameserver.scripts.handlers.skillhandlers.GetPlayer;
import l2r.gameserver.scripts.handlers.skillhandlers.GiveReco;
import l2r.gameserver.scripts.handlers.skillhandlers.GiveVitality;
import l2r.gameserver.scripts.handlers.skillhandlers.InstantJump;
import l2r.gameserver.scripts.handlers.skillhandlers.Manadam;
import l2r.gameserver.scripts.handlers.skillhandlers.Mdam;
import l2r.gameserver.scripts.handlers.skillhandlers.NornilsPower;
import l2r.gameserver.scripts.handlers.skillhandlers.Pdam;
import l2r.gameserver.scripts.handlers.skillhandlers.RefuelAirShip;
import l2r.gameserver.scripts.handlers.skillhandlers.Resurrect;
import l2r.gameserver.scripts.handlers.skillhandlers.ShiftTarget;
import l2r.gameserver.scripts.handlers.skillhandlers.Sow;
import l2r.gameserver.scripts.handlers.skillhandlers.StrSiegeAssault;
import l2r.gameserver.scripts.handlers.skillhandlers.SummonFriend;
import l2r.gameserver.scripts.handlers.skillhandlers.TakeCastle;
import l2r.gameserver.scripts.handlers.skillhandlers.TakeFort;
import l2r.gameserver.scripts.handlers.skillhandlers.TransformDispel;
import l2r.gameserver.scripts.handlers.skillhandlers.Trap;
import l2r.gameserver.scripts.handlers.skillhandlers.Unlock;

/**
 * @author UnAfraid
 */
public class SkillHandler implements IHandler<ISkillHandler, L2SkillType>
{
	private final Map<Integer, ISkillHandler> _datatable;
	
	protected SkillHandler()
	{
		_datatable = new HashMap<>();
		
		registerHandler(new BallistaBomb());
		registerHandler(new BeastSkills());
		registerHandler(new Blow());
		registerHandler(new Continuous());
		registerHandler(new CpDamPercent());
		registerHandler(new Craft());
		registerHandler(new DeluxeKey());
		registerHandler(new Detection());
		registerHandler(new Disablers());
		registerHandler(new Dummy());
		registerHandler(new Fishing());
		registerHandler(new FishingSkill());
		registerHandler(new GetPlayer());
		registerHandler(new GiveReco());
		registerHandler(new GiveVitality());
		registerHandler(new InstantJump());
		registerHandler(new Manadam());
		registerHandler(new Mdam());
		registerHandler(new NornilsPower());
		registerHandler(new Pdam());
		registerHandler(new RefuelAirShip());
		registerHandler(new Resurrect());
		registerHandler(new ShiftTarget());
		registerHandler(new Sow());
		registerHandler(new StrSiegeAssault());
		registerHandler(new SummonFriend());
		registerHandler(new TakeCastle());
		registerHandler(new TakeFort());
		registerHandler(new TransformDispel());
		registerHandler(new Trap());
		registerHandler(new Unlock());
	}
	
	@Override
	public void registerHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.put(t.ordinal(), handler);
		}
	}
	
	@Override
	public synchronized void removeHandler(ISkillHandler handler)
	{
		L2SkillType[] types = handler.getSkillIds();
		for (L2SkillType t : types)
		{
			_datatable.remove(t.ordinal());
		}
	}
	
	@Override
	public ISkillHandler getHandler(L2SkillType skillType)
	{
		return _datatable.get(skillType.ordinal());
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}