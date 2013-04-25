/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.zone.type;

import gr.reunion.configs.CustomServerConfigs;
import l2r.gameserver.datatables.SkillTable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.ZoneId;

/**
 * @author -=DoctorNo=-
 */
public class L2ChaoticZone extends L2RespawnZone
{
	// Chaotic Zone Revive Spots
	public static int[] _x =
	{
		-76063,
		-78299,
		-85489,
		-87738,
		-81864
	};
	public static int[] _y =
	{
		-47285,
		-54143,
		-54154,
		-47331,
		-43048
	};
	public static int[] _z =
	{
		-10682,
		-10682,
		-10684,
		-10682,
		-10686
	};
	
	public L2ChaoticZone(int id)
	{
		super(42490);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.ZONE_CHAOTIC, true);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		character.setInsideZone(ZoneId.NO_STORE, true);
		character.setInsideZone(ZoneId.NO_BOOKMARK, true);
		character.setInsideZone(ZoneId.NO_ITEM_DROP, true);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).setPvpFlag(1);
			((L2PcInstance) character).sendMessage("You entered the Chaotic Zone.");
			((L2PcInstance) character).broadcastUserInfo();
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.ZONE_CHAOTIC, false);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		character.setInsideZone(ZoneId.NO_STORE, false);
		character.setInsideZone(ZoneId.NO_BOOKMARK, false);
		character.setInsideZone(ZoneId.NO_ITEM_DROP, false);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendMessage("You left the Chaotic Zone.");
			if (CustomServerConfigs.ENABLE_CHAOTIC_ZONE_SKILL)
			{
				((L2PcInstance) character).stopSkillEffects(CustomServerConfigs.CHAOTIC_ZONE_SKILL_ID);
			}
			((L2PcInstance) character).setPvpFlag(0);
			((L2PcInstance) character).broadcastUserInfo();
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			// Don nothing
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		SkillTable.getInstance().getInfo(1323, 1).getEffects(character, character);
		character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
		character.setCurrentCp(character.getMaxCp());
		if (CustomServerConfigs.ENABLE_CHAOTIC_ZONE_SKILL)
		{
			SkillTable.getInstance().getInfo(CustomServerConfigs.CHAOTIC_ZONE_SKILL_ID, 1).getEffects(character, character);
		}
	}
}