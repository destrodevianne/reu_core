package l2r.gameserver.model.actor.instance;

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
import gr.reunion.configs.AioBufferConfigs;
import gr.reunion.datatables.CustomTable;
import gr.reunion.javaBuffer.AutoBuff;
import gr.reunion.javaBuffer.BuffCategories;
import gr.reunion.javaBuffer.BuffInstance;
import gr.reunion.javaBuffer.PlayerMethods;
import gr.reunion.javaBuffer.buffItem.runnable.BuffItemDelay;
import gr.reunion.javaBuffer.buffNpc.dynamicHtmls.GenerateHtmls;
import gr.reunion.javaBuffer.buffNpc.dynamicHtmls.GenerateHtmls.Packet;
import gr.reunion.javaBuffer.buffNpc.runnable.BuffNpcDeleter;
import gr.reunion.javaBuffer.buffNpc.runnable.BuffNpcSaver;
import gr.reunion.main.Conditions;
import gr.reunion.securitySystem.SecurityActions;
import gr.reunion.securitySystem.SecurityType;

public class L2BufferInstance extends L2Npc
{
	/* The coin used to buff players */
	private static final int _coinperbuff = AioBufferConfigs.AIO_BUFFCOIN;
	/* Price per buff */
	private static final int _buffprice = AioBufferConfigs.AIO_PRICE_PERBUFF;
	/* buffs amount */
	private static final int _maxDance = AioBufferConfigs.AIO_MAXDANCE_PERPROFILE;
	private static final int _maxBuffs = AioBufferConfigs.AIO_MAXBUFFS_PERPROFILE;
	
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
			FastList<Integer> buffIds = PlayerMethods.getProfileBuffs(subCommand[1], player);
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
			GenerateHtmls.sendPacket(player, "555-2.htm", Packet.FILE, getObjectId());
		}
		else if (command.startsWith("healme"))
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			GenerateHtmls.sendPacket(player, "555-2.htm", Packet.FILE, getObjectId());
		}
		else if (command.startsWith("autobuff"))
		{
			if ((player.getPvpFlag() != 0) && !player.isInsideZone(ZoneId.PEACE))
			{
				player.sendMessage("Cannot use this feature here with flag.");
				return;
			}
			
			AutoBuff.autoBuff(player);
			GenerateHtmls.sendPacket(player, "555-2.htm", Packet.FILE, getObjectId());
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
					GenerateHtmls.sendPacket(player, "555-3.htm", Packet.FILE, getObjectId());
					break;
				case "buffsong":
					GenerateHtmls.sendPacket(player, "555-4.htm", Packet.FILE, getObjectId());
					break;
				case "buffprop":
					GenerateHtmls.sendPacket(player, "555-5.htm", Packet.FILE, getObjectId());
					break;
				case "buffover":
					GenerateHtmls.sendPacket(player, "555-6.htm", Packet.FILE, getObjectId());
					break;
				case "buffdwarf":
					GenerateHtmls.sendPacket(player, "555-7.htm", Packet.FILE, getObjectId());
					break;
				case "buffwar":
					GenerateHtmls.sendPacket(player, "555-8.htm", Packet.FILE, getObjectId());
					break;
				case "buffmisc":
					GenerateHtmls.sendPacket(player, "555-9.htm", Packet.FILE, getObjectId());
					break;
				case "buffelder":
					GenerateHtmls.sendPacket(player, "555-10.htm", Packet.FILE, getObjectId());
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
				if (!PlayerMethods.createProfile(subCommand[1], player))
				{
					return;
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Please specify a valid profile name.");
				GenerateHtmls.sendPacket(player, "555-11.htm", Packet.FILE, getObjectId());
				return;
			}
			
			GenerateHtmls.sendPacket(player, "555-13.htm", Packet.FILE, getObjectId());
		}
		else if (command.startsWith("showAvaliable"))
		{
			switch (subCommand[0])
			{
				case "showAvaliableDwarf":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.DWARF, "addDwarf", getObjectId());
					break;
				case "showAvaliableMisc":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.MISC, "addMisc", getObjectId());
					break;
				case "showAvaliableElder":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.ELDER, "addElder", getObjectId());
					break;
				case "showAvaliableChant":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.CHANT, "addChant", getObjectId());
					break;
				case "showAvaliableOver":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.OVERLORD, "addOver", getObjectId());
					break;
				case "showAvaliableProp":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.PROPHET, "addProp", getObjectId());
					break;
				case "showAvaliableDance":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.DANCE, "addDance", getObjectId());
					break;
				case "showAvaliableSong":
					GenerateHtmls.showBuffsToAdd(player, subCommand[1], BuffCategories.SONG, "addSong", getObjectId());
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
			
			ThreadPoolManager.getInstance().executeTask(new BuffNpcSaver(player, category, subCommand[1], Integer.parseInt(subCommand[2]), this));
		}
		
		// Scheme removals
		else if (command.startsWith("deleteProfile"))
		{
			PlayerMethods.delProfile(subCommand[1], player);
		}
		else if (command.startsWith("showBuffsToDelete"))
		{
			GenerateHtmls.showBuffsToDelete(player, subCommand[1], "removeBuffs", getObjectId());
		}
		else if (command.startsWith("removeBuffs"))
		{
			ThreadPoolManager.getInstance().executeTask(new BuffNpcDeleter(player, subCommand[1], Integer.parseInt(subCommand[2]), this));
		}
		else if (command.startsWith("showProfiles"))
		{
			GenerateHtmls.showSchemeToEdit(player, subCommand[1], getObjectId());
		}
	}
	
	private boolean checkDanceAmount(L2PcInstance player, String profile, BuffCategories category)
	{
		if (PlayerMethods.getDanceSongCount(profile, player) == _maxDance)
		{
			player.sendMessage("You cannot add more than " + _maxDance + " dances-songs.");
			GenerateHtmls.callBuffToAdd(category, player, profile, getObjectId());
			return false;
		}
		return true;
	}
	
	private boolean checkBuffsAmount(L2PcInstance player, String profile, BuffCategories category)
	{
		if (PlayerMethods.getOtherBuffCount(profile, player) == _maxBuffs)
		{
			player.sendMessage("You cannot add more than " + _maxBuffs + " buffs.");
			GenerateHtmls.callBuffToAdd(category, player, profile, getObjectId());
			return false;
		}
		return true;
	}
}