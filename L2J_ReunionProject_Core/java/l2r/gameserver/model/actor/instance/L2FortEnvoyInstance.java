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
package l2r.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2FortEnvoyInstance extends L2Npc
{
	public L2FortEnvoyInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
		setInstanceType(InstanceType.L2FortEnvoyInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		String filename;
		
		if (!player.isClanLeader() || (getFort().getResidenceId() != player.getClan().getFortId()))
		{
			filename = "data/html/fortress/envoy-noclan.htm";
		}
		else if (getFort().getFortState() == 0)
		{
			filename = "data/html/fortress/envoy.htm";
		}
		else
		{
			filename = "data/html/fortress/envoy-no.htm";
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%castleName%", String.valueOf(CastleManager.getInstance().getCastleById(getFort().getCastleIdFromEnvoy(getNpcId())).getName()));
		player.sendPacket(html);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String par = "";
		if (st.countTokens() >= 1)
		{
			par = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("select"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(par);
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			
			int castleId = 0;
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			if (val == 2)
			{
				castleId = getFort().getCastleIdFromEnvoy(getNpcId());
				if (CastleManager.getInstance().getCastleById(castleId).getOwnerId() < 1)
				{
					html.setHtml("<html><body>Contact is currently not possible, " + CastleManager.getInstance().getCastleById(castleId).getName() + " Castle isn't currently owned by clan.</body></html>");
					player.sendPacket(html);
					return;
				}
			}
			getFort().setFortState(val, castleId);
			html.setFile(player.getHtmlPrefix(), "data/html/fortress/envoy-ok.htm");
			html.replace("%castleName%", String.valueOf(CastleManager.getInstance().getCastleById(getFort().getCastleIdFromEnvoy(getNpcId())).getName()));
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}