package l2r.gameserver.model.actor.instance;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.SkillTable;
import l2r.gameserver.model.actor.FakePc;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.ZoneId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SystemMessage;
import gr.reunion.buffItem.AutoBuff;
import gr.reunion.buffItem.BuffCategories;
import gr.reunion.buffItem.BuffInstance;
import gr.reunion.buffItem.runnable.BuffItemDelay;
import gr.reunion.configs.AioBufferConfigs;
import gr.reunion.datatables.CustomTable;
import gr.reunion.main.Conditions;
import gr.reunion.securitySystem.SecurityActions;
import gr.reunion.securitySystem.SecurityType;

public class L2BufferInstance extends L2Npc
{
	/* Constants for dynamic htmls */
	private static final String _headHtml = "<html><title>Npc Buffer</title><body><center>";
	private static final String _endHtml = "</center></body></html>";
	/* The coin used to buff players */
	private static final int _coinperbuff = AioBufferConfigs.AIO_BUFFCOIN;
	/* Price per buff */
	private static final int _buffprice = AioBufferConfigs.AIO_PRICE_PERBUFF;
	/* buffs amount */
	private static final int _maxDance = AioBufferConfigs.AIO_MAXDANCE_PERPROFILE;
	private static final int _maxBuffs = AioBufferConfigs.AIO_MAXBUFFS_PERPROFILE;
	
	public enum Packet
	{
		DYNAMIC,
		FILE
	}
	
	/**
	 * Method to send the html to char
	 * @param player
	 * @param html
	 * @param packet
	 */
	private void sendPacket(L2PcInstance player, String html, Packet packet)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		if (packet.equals(Packet.FILE))
		{
			msg.setFile(player.getHtmlPrefix(), "/data/html/NpcBuffer/" + html);
			msg.replace("%objectId%", String.valueOf(getObjectId()));
		}
		if (packet.equals(Packet.DYNAMIC))
		{
			msg.setHtml(html);
			msg.replace("%objectId%", String.valueOf(getObjectId()));
		}
		player.sendPacket(msg);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getHtmlPrefix(), "data/html/NpcBuffer/" + getTemplate().getNpcId() + ".htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		
	}
	
	public L2BufferInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2BufferInstance);
		FakePc fpc = getFakePc();
		if (fpc != null)
		{
			setTitle(fpc.title);
		}
	}
	
	// Manages all bypasses for normal players
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		final String[] subCommand = command.split("_");
		
		// No null pointers
		if (player == null)
		{
			return;
		}
		
		if (!Conditions.checkPlayerBasicConditions(player))
		{
			return;
		}
		
		// Page navigation, html command how to starts
		if (command.startsWith("Chat"))
		{
			if (subCommand[1].isEmpty() || (subCommand[1] == null))
			{
				return;
			}
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getHtmlPrefix(), "data/html/NpcBuffer/" + subCommand[1]);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
		
		// Send buffs from profile to player or party or pet
		else if (command.startsWith("bufffor"))
		{
			FastList<Integer> buffIds = player.getProfileBuffs(subCommand[1]);
			int priceCount = buffIds.size();
			
			if (AioBufferConfigs.AIO_BUFFER_ENABLE_DELAY && BuffItemDelay._delayers.contains(player))
			{
				if (AioBufferConfigs.AIO_BUFFER_DELAY_SENDMESSAGE)
				{
					player.sendMessage("In order to use buffer functions again, you will have to wait " + AioBufferConfigs.AIO_BUFFER_DELAY + "!");
				}
				return;
			}
			
			// Avoiding null pointers
			if ((player.getInventory().getItemByItemId(_coinperbuff) == null) || (player.getInventory().getItemByItemId(_coinperbuff).getCount() < _buffprice))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
			}
			
			if (command.startsWith("buffforpet"))
			{
				L2Summon summon = player.getSummon();
				if (summon == null)
				{
					player.sendMessage("Summon your pet first.");
					return;
				}
				player.destroyItemByItemId("Scheme system", _coinperbuff, priceCount * _buffprice, player, true);
				for (int id : buffIds)
				{
					BuffInstance buff = CustomTable.getInstance().getBuff(id);
					
					if (!player.isInsideRadius(summon, 300, false, false))
					{
						continue;
					}
					else if (buff == null)
					{
						continue;
					}
					else if ((player.getInventory().getItemByItemId(AioBufferConfigs.BUFF_ITEM_ID) != null) || (player.isPremium()))
					{
						SkillTable.getInstance().getInfo(buff.getId(), buff.getCustomLevel()).getEffects(player, summon);
					}
					else
					{
						SkillTable.getInstance().getInfo(buff.getId(), buff.getLevel()).getEffects(player, summon);
					}
				}
				player.getSummon().setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.getSummon().setCurrentCp(player.getMaxCp());
				if (AioBufferConfigs.AIO_BUFFER_ENABLE_DELAY)
				{
					ThreadPoolManager.getInstance().executeTask(new BuffItemDelay(player));
				}
			}
			else if (command.startsWith("buffforparty"))
			{
				if (player.getParty() == null)
				{
					player.sendMessage("Your are not in a party.");
					return;
				}
				player.destroyItemByItemId("Scheme system", _coinperbuff, priceCount * _buffprice, player, true);
				for (L2PcInstance member : player.getParty().getMembers())
				{
					if (!player.isInsideRadius(member, 300, false, false))
					{
						continue;
					}
					for (int id : buffIds)
					{
						BuffInstance buff = CustomTable.getInstance().getBuff(id);
						
						if (buff == null)
						{
							continue;
						}
						
						L2Skill skill = (player.getInventory().getItemByItemId(AioBufferConfigs.BUFF_ITEM_ID) != null) || (player.isPremium()) ? SkillTable.getInstance().getInfo(buff.getId(), buff.getCustomLevel()) : SkillTable.getInstance().getInfo(buff.getId(), buff.getLevel());
						
						if (skill == null)
						{
							continue;
						}
						
						skill.getEffects(member, member);
					}
					member.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
					member.setCurrentCp(player.getMaxCp());
				}
				if (AioBufferConfigs.AIO_BUFFER_ENABLE_DELAY)
				{
					ThreadPoolManager.getInstance().executeTask(new BuffItemDelay(player));
				}
			}
			// Personal buffs
			else if (command.startsWith("buffforme"))
			{
				player.destroyItemByItemId("Scheme system", _coinperbuff, priceCount * _buffprice, player, true);
				for (int id : buffIds)
				{
					BuffInstance buff = CustomTable.getInstance().getBuff(id);
					
					if (buff == null)
					{
						continue;
					}
					
					L2Skill skill = (player.getInventory().getItemByItemId(AioBufferConfigs.BUFF_ITEM_ID) != null) || (player.isPremium()) ? SkillTable.getInstance().getInfo(buff.getId(), buff.getCustomLevel()) : SkillTable.getInstance().getInfo(buff.getId(), buff.getLevel());
					
					if (skill == null)
					{
						continue;
					}
					
					skill.getEffects(player, player);
				}
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
				if (AioBufferConfigs.AIO_BUFFER_ENABLE_DELAY)
				{
					ThreadPoolManager.getInstance().executeTask(new BuffItemDelay(player));
				}
			}
		}
		
		// Buffer
		else if (command.startsWith("removebuff"))
		{
			player.stopAllEffects();
			sendPacket(player, "555-2.htm", Packet.FILE);
		}
		else if (command.startsWith("healme"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			sendPacket(player, "555-2.htm", Packet.FILE);
		}
		else if (command.startsWith("autobuff"))
		{
			if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneId.PEACE))
			{
				player.sendMessage("Cannot use this feature here with flag.");
				return;
			}
			
			AutoBuff.autoBuff(player);
			sendPacket(player, "555-2.htm", Packet.FILE);
		}
		else if (command.startsWith("buff"))
		{
			if ((player.getInventory().getItemByItemId(_coinperbuff) == null) || (player.getInventory().getItemByItemId(_coinperbuff).getCount() < _buffprice))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_REQUIRED_ITEMS));
				return;
			}
			
			player.destroyItemByItemId("Buffer", _coinperbuff, _buffprice, player, true);
			int buffId = Integer.parseInt(subCommand[1]);
			
			BuffInstance buff = CustomTable.getInstance().getBuff(buffId);
			
			if (buff == null)
			{
				SecurityActions.startSecurity(player, SecurityType.NPC_BUFFER);
				return;
			}
			
			int buffLevel = buff.getLevel();
			
			if ((player.getInventory().getItemByItemId(AioBufferConfigs.BUFF_ITEM_ID) != null) || (player.isPremium()))
			{
				buffLevel = buff.getCustomLevel();
			}
			
			L2Skill skill = SkillTable.getInstance().getInfo(buffId, buffLevel);
			
			if (skill != null)
			{
				skill.getEffects(player, player);
			}
			else
			{
				SecurityActions.startSecurity(player, SecurityType.NPC_BUFFER);
			}
			
			switch (subCommand[0])
			{
				case "buffdance":
					sendPacket(player, "555-3.htm", Packet.FILE);
					break;
				case "buffsong":
					sendPacket(player, "555-4.htm", Packet.FILE);
					break;
				case "buffprop":
					sendPacket(player, "555-5.htm", Packet.FILE);
					break;
				case "buffover":
					sendPacket(player, "555-6.htm", Packet.FILE);
					break;
				case "buffdwarf":
					sendPacket(player, "555-7.htm", Packet.FILE);
					break;
				case "buffwar":
					sendPacket(player, "555-8.htm", Packet.FILE);
					break;
				case "buffmisc":
					sendPacket(player, "555-9.htm", Packet.FILE);
					break;
				case "buffelder":
					sendPacket(player, "555-10.htm", Packet.FILE);
					break;
				default:
					break;
			}
		}
		
		// Scheme adds reached
		else if (command.startsWith("saveProfile"))
		{
			try
			{
				if (!player.createProfile(subCommand[1]))
				{
					return;
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Please specify a valid profile name.");
				sendPacket(player, "555-11.htm", Packet.FILE);
				return;
			}
			
			sendPacket(player, "555-13.htm", Packet.FILE);
		}
		else if (command.startsWith("showAvaliable"))
		{
			switch (subCommand[0])
			{
				case "showAvaliableDwarf":
					showBuffsToAdd(player, subCommand[1], BuffCategories.DWARF, "addDwarf");
					break;
				case "showAvaliableMisc":
					showBuffsToAdd(player, subCommand[1], BuffCategories.MISC, "addMisc");
					break;
				case "showAvaliableElder":
					showBuffsToAdd(player, subCommand[1], BuffCategories.ELDER, "addElder");
					break;
				case "showAvaliableChant":
					showBuffsToAdd(player, subCommand[1], BuffCategories.CHANT, "addChant");
					break;
				case "showAvaliableOver":
					showBuffsToAdd(player, subCommand[1], BuffCategories.OVERLORD, "addOver");
					break;
				case "showAvaliableProp":
					showBuffsToAdd(player, subCommand[1], BuffCategories.PROPHET, "addProp");
					break;
				case "showAvaliableDance":
					showBuffsToAdd(player, subCommand[1], BuffCategories.DANCE, "addDance");
					break;
				case "showAvaliableSong":
					showBuffsToAdd(player, subCommand[1], BuffCategories.SONG, "addSong");
					break;
				default:
					break;
			}
		}
		else if (command.startsWith("add"))
		{
			BuffCategories category = BuffCategories.PROPHET;
			switch (subCommand[0])
			{
				case "addChant":
					category = BuffCategories.CHANT;
					break;
				case "addOver":
					category = BuffCategories.OVERLORD;
					break;
				case "addElder":
					category = BuffCategories.ELDER;
					break;
				case "addDwarf":
					category = BuffCategories.DWARF;
					break;
				case "addMisc":
					category = BuffCategories.MISC;
					break;
				case "addProp":
					category = BuffCategories.PROPHET;
					break;
				case "addDance":
					category = BuffCategories.DANCE;
					break;
				case "addSong":
					category = BuffCategories.SONG;
					break;
				default:
					break;
			}
			
			if ((category == BuffCategories.DANCE) || (category == BuffCategories.SONG))
			{
				if (!checkDanceAmount(player, subCommand[1], category))
				{
					return;
				}
			}
			else
			{
				if (!checkBuffsAmount(player, subCommand[1], category))
				{
					return;
				}
			}
			
			ThreadPoolManager.getInstance().executeTask(new buffSaver(player, category, subCommand[1], Integer.parseInt(subCommand[2]), this));
		}
		
		// Scheme removals
		else if (command.startsWith("deleteProfile"))
		{
			player.delProfile(subCommand[1]);
		}
		else if (command.startsWith("showBuffsToDelete"))
		{
			showBuffsToDelete(player, subCommand[1], "removeBuffs");
		}
		else if (command.startsWith("removeBuffs"))
		{
			ThreadPoolManager.getInstance().executeTask(new buffDeleter(player, subCommand[1], Integer.parseInt(subCommand[2]), this));
		}
		else if (command.startsWith("showProfiles"))
		{
			showSchemeToEdit(player, subCommand[1]);
		}
	}
	
	public void callBuffToAdd(BuffCategories category, L2PcInstance player, String profile)
	{
		String bypass = "addProp";
		
		switch (category)
		{
			case DANCE:
				bypass = "addDance";
				break;
			case SONG:
				bypass = "addSong";
				break;
			case MISC:
				bypass = "addMisc";
				break;
			case ELDER:
				bypass = "addElder";
				break;
			case OVERLORD:
				bypass = "addOver";
				break;
			case PROPHET:
				bypass = "addProp";
				break;
			case DWARF:
				bypass = "addDwarf";
				break;
			case CHANT:
				bypass = "addChant";
				break;
			case NONE:
				bypass = "removeBuffs";
				break;
			default:
				break;
		}
		showBuffsToAdd(player, profile, category, bypass);
	}
	
	// SCHEME SYSTEM
	private void showSchemeToEdit(L2PcInstance player, String action)
	{
		FastList<String> profileNames = player.getProfiles();
		
		TextBuilder tb = new TextBuilder();
		tb.append(_headHtml);
		tb.append("Choose the profile<br></center><font color=00FFFF>Scheme Profiles:</font><center><img src=\"L2UI.SquareGray\" width=280 height=1><table bgcolor=131210>");
		for (String profile : profileNames)
		{
			tb.append("<tr>");
			tb.append("<td align=center><button value=\"" + profile + "\" action=\"bypass -h npc_%objectId%_" + action + "_" + profile + "\" width=135 height=28 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></td>");
			tb.append("</tr>");
		}
		tb.append("</table><img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append(_endHtml);
		
		sendPacket(player, tb.toString(), Packet.DYNAMIC);
	}
	
	/**
	 * Shows the available buffs to add to a profile
	 * @param player
	 * @param profile
	 * @param category
	 * @param bypass
	 */
	public void showBuffsToAdd(L2PcInstance player, String profile, BuffCategories category, String bypass)
	{
		FastList<Integer> ownedBuffs = player.getProfileBuffs(profile);
		int i = 0;
		
		TextBuilder tb = new TextBuilder();
		tb.append(_headHtml);
		tb.append("Choose the buffs to be added<br></center>");
		switch (category)
		{
			case CHANT:
				tb.append("<font color=00FFFF>Scheme Buffer: Chant</font>");
				break;
			case DANCE:
				tb.append("<font color=00FFFF>Scheme Buffer: Dance</font>");
				break;
			case SONG:
				tb.append("<font color=00FFFF>Scheme Buffer: Song</font>");
				break;
			case OVERLORD:
				tb.append("<font color=00FFFF>Scheme Buffer: Overlord</font>");
				break;
			case PROPHET:
				tb.append("<font color=00FFFF>Scheme Buffer: Prophet</font>");
				break;
			case ELDER:
				tb.append("<font color=00FFFF>Scheme Buffer: Elder</font>");
				break;
			case DWARF:
				tb.append("<font color=00FFFF>Scheme Buffer: Dwarf</font>");
				break;
			case MISC:
				tb.append("<font color=00FFFF>Scheme Buffer: Misc</font>");
				break;
			default:
				break;
		}
		tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1>");
		for (BuffInstance buffInst : CustomTable.getInstance().getBuffs().values())
		{
			// Just a check to know if this buff
			// Is in the category we wish to be
			if (buffInst.getCategory() != category)
			{
				continue;
			}
			
			int id = buffInst.getId();
			Integer level = buffInst.getLevel();
			String description = buffInst.getDescription();
			String name = buffInst.getName();
			
			// Check if the buff id exists in the owned buffs fastlist
			// Remember the l2pc has only the buffId contained
			if ((ownedBuffs != null) && ownedBuffs.contains(id))
			{
				continue;
			}
			
			if ((i % 2) == 0)
			{
				tb.append("<table bgcolor=131210>");
			}
			else
			{
				tb.append("<table>");
			}
			tb.append("<tr>");
			if ((id == 4699) || (id == 4700))
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill1331\" fore=\"icon.skill1331\"></td>");
			}
			if ((id == 4702) || (id == 4703))
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill1332\" fore=\"icon.skill1332\"></td>");
			}
			if (id < 1000)
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill0" + id + "\" fore=\"icon.skill0" + id + "\"></td>");
			}
			if ((id > 1000) && (id != 4699) && (id != 4700) && (id != 4702) && (id != 4703))
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill" + id + "\" fore=\"icon.skill" + id + "\"></td>");
			}
			tb.append("<td><table>");
			tb.append("<tr><td width=220>" + name + "<font color=a1a1a1> Lv</font> <font color=ae9977>" + level + "</font></td></tr>");
			tb.append("<tr><td width=220><font color=b0bccc>" + description + "</font></td></tr></table></td></tr>");
			tb.append("</table>");
			i++;
		}
		tb.append("<br><br><img src=L2UI.SquareWhite width=280 height=1><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_555-13.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append(_endHtml);
		
		sendPacket(player, tb.toString(), Packet.DYNAMIC);
	}
	
	// Shows the available buffs to add to a profile;
	public void showBuffsToDelete(L2PcInstance player, String profile, String bypass)
	{
		FastList<Integer> ownedBuffs = player.getProfileBuffs(profile);
		int i = 0;
		
		if (ownedBuffs == null)
		{
			player.sendMessage("There are no buffs in that profile.");
			return;
		}
		
		TextBuilder tb = new TextBuilder();
		tb.append(_headHtml);
		tb.append("Choose the buffs to be deleted<br></center>");
		tb.append("<font color=00FFFF>Scheme Buffer: Remove</font>");
		tb.append("<center><img src=\"L2UI.SquareGray\" width=280 height=1><table bgcolor=131210>");
		for (BuffInstance buffInst : CustomTable.getInstance().getBuffs().values())
		{
			int id = buffInst.getId();
			Integer level = buffInst.getLevel();
			String description = buffInst.getDescription();
			String name = buffInst.getName();
			
			// Check if the buff id exists in the owned buffs fastlist
			// Remember the l2pc has only the buffId contained
			if (!ownedBuffs.contains(id))
			{
				continue;
			}
			
			if ((i % 2) == 0)
			{
				tb.append("<table bgcolor=131210>");
			}
			else
			{
				tb.append("<table>");
			}
			tb.append("<tr>");
			if ((id == 4699) || (id == 4700))
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill1331\" fore=\"icon.skill1331\"></td>");
			}
			if ((id == 4702) || (id == 4703))
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill1332\" fore=\"icon.skill1332\"></td>");
			}
			if (id < 1000)
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill0" + id + "\" fore=\"icon.skill0" + id + "\"></td>");
			}
			if ((id > 1000) && (id != 4699) && (id != 4700) && (id != 4702) && (id != 4703))
			{
				tb.append("<td width=40><button action=\"bypass -h npc_%objectId%_" + bypass + "_" + profile + "_" + id + "\" width=32 height=32 back=\"icon.skill" + id + "\" fore=\"icon.skill" + id + "\"></td>");
			}
			tb.append("<td><table>");
			tb.append("<tr><td width=220>" + name + "<font color=a1a1a1> Lv</font> <font color=ae9977>" + level + "</font></td></tr>");
			tb.append("<tr><td width=220><font color=b0bccc>" + description + "</font></td></tr></table></td></tr>");
			tb.append("</table>");
			i++;
		}
		tb.append("<br><br><img src=L2UI.SquareWhite width=280 height=1><button value=\"Back\" action=\"bypass -h npc_%objectId%_Chat_555.htm\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append(_endHtml);
		
		sendPacket(player, tb.toString(), Packet.DYNAMIC);
	}
	
	private boolean checkDanceAmount(L2PcInstance player, String profile, BuffCategories category)
	{
		if (player.getDanceSongCount(profile) == _maxDance)
		{
			player.sendMessage("You cannot add more than " + _maxDance + " dances-songs.");
			callBuffToAdd(category, player, profile);
			return false;
		}
		return true;
	}
	
	private boolean checkBuffsAmount(L2PcInstance player, String profile, BuffCategories category)
	{
		if (player.getOtherBuffCount(profile) == _maxBuffs)
		{
			player.sendMessage("You cannot add more than " + _maxBuffs + " buffs.");
			callBuffToAdd(category, player, profile);
			return false;
		}
		return true;
	}
}