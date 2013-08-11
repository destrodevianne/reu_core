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

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SkillTable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.ZoneId;
import l2r.util.Rnd;
import gr.reunion.configs.ChaoticZoneConfigs;

/**
 * @author -=GodFather=-
 */
public class L2ChaoticZone extends L2RespawnZone
{
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
		
		if (character.isPlayer())
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
		
		if (character.isPlayer())
		{
			((L2PcInstance) character).sendMessage("You left the Chaotic Zone.");
			if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_SKILL)
			{
				((L2PcInstance) character).stopSkillEffects(ChaoticZoneConfigs.CHAOTIC_ZONE_SKILL_ID);
			}
			((L2PcInstance) character).setPvpFlag(0);
			((L2PcInstance) character).broadcastUserInfo();
		}
	}
	
	@Override
	public void onDieInside(final L2Character character)
	{
		if (character.isPlayer() && ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_AUTO_REVIVE)
		{
			character.sendMessage("Get ready! You will be revive in " + ChaoticZoneConfigs.CHAOTIC_ZONE_REVIVE_DELAY + " seconds!");
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if (character.isDead())
					{
						int r = Rnd.get(ChaoticZoneConfigs.CHAOTIC_ZONE_AUTO_RES_LOCS_COUNT);
						character.teleToLocation(ChaoticZoneConfigs.xCoords[r], ChaoticZoneConfigs.yCoords[r], ChaoticZoneConfigs.zCoords[r]);
						character.doRevive();
					}
				}
			}, ChaoticZoneConfigs.CHAOTIC_ZONE_REVIVE_DELAY * 1000);
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		if (character.isPlayer())
		{
			SkillTable.getInstance().getInfo(1323, 1).getEffects(character, character);
			character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
			character.setCurrentCp(character.getMaxCp());
			if (ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_SKILL)
			{
				SkillTable.getInstance().getInfo(ChaoticZoneConfigs.CHAOTIC_ZONE_SKILL_ID, 1).getEffects(character, character);
			}
		}
	}
}