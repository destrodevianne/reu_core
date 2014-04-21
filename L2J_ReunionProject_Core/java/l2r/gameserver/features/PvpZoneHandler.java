package l2r.gameserver.features;

import java.util.Map.Entry;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.util.Rnd;
import gr.reunion.configsEngine.FlagZoneConfigs;

public class PvpZoneHandler
{
	public static void validateRewardConditions(L2PcInstance killer)
	{
		if (killer.isInParty())
		{
			for (L2PcInstance cha : killer.getParty().getMembers())
			{
				if (cha.getClassId() == ClassId.cardinal)
				{
					if (300 > Rnd.get(1000))
					{
						if (cha.isInsideRadius(killer.getLocation(), 500, false, false))
						{
							addReward(killer);
						}
					}
				}
			}
		}
	}
	
	private static void addReward(L2PcInstance killer)
	{
		for (Entry<Integer, Long> rewards : FlagZoneConfigs.FLAG_ZONE_REWARDS.entrySet())
		{
			if (rewards != null)
			{
				killer.getInventory().addItem("FlagZone", rewards.getKey(), rewards.getValue(), killer, true);
			}
		}
	}
}