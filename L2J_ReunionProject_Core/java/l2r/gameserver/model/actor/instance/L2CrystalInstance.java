package l2r.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastSet;

import l2r.L2DatabaseFactory;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.CtrlIntention;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.ClanTable;
import l2r.gameserver.datatables.NpcTable;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.L2Spawn;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.MyTargetSelected;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.ValidateLocation;
import l2r.util.Rnd;

/**
 * @author Matim
 * @version 2.1 <br>
 * <br>
 *          L2Occupation Crystal Instance<br>
 * <br>
 *          <li>will not move anywhere. <li>will not attack attackers. <li>it has hp bar status. <br>
 *          <br>
 *          Owner of the crystal may manage crystal functions, such as: <li>defence abilitiy <li>warning ability <li>stats upgrades <li>special items/buffs/teleports (TODO)
 */
public class L2CrystalInstance extends L2Npc
{
	private final int DEFENDERS_COUNT = 5;
	private int _ownerClanId = 1;
	private String _locationName = "";
	private boolean _defenceAbilityEnabled = false;
	private boolean _warningAbilityEnabled = false;
	private boolean _statsUpgradeEnabled = false;
	private boolean _canWarn = true;
	private boolean _canSpawnDefenders = true;
	
	private int _warningCount = 0;
	private int _defenceSpawnCount = 0;
	
	public static FastSet<L2Npc> _defenders = new FastSet<>();
	
	public L2CrystalInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		loadData(this.getNpcId());
	}
	
	/**
	 * Load Data about crystal from database. Clan owner Id, location name, enabled abilities etc.
	 * @param crystalId
	 */
	private void loadData(int crystalId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("Select * from crystal_list where mapId =" + this.getNpcId());
			
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				_ownerClanId = rs.getInt("clanId");
				_locationName = rs.getString("locationName");
				_defenceAbilityEnabled = rs.getBoolean("defenceEnabled");
				_warningAbilityEnabled = rs.getBoolean("warningEnabled");
				_statsUpgradeEnabled = rs.getBoolean("statsEnabled");
				_warningCount = rs.getInt("warningCount");
				_defenceSpawnCount = rs.getInt("defenceCount");
			}
		}
		catch (Exception e)
		{
			_log.info("Crystal Manager [Load Data]: " + e);
		}
	}
	
	/**
	 * Save Data about crystal into database.
	 * @param crystalId
	 */
	public void saveData(int crystalId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement statement = con.createStatement();
			statement.executeUpdate("UPDATE crystal_list SET clanId ='" + _ownerClanId + "', defenceEnabled ='" + _defenceAbilityEnabled + "', warningEnabled ='" + _warningAbilityEnabled + "', statsEnabled ='" + _statsUpgradeEnabled + "', warningCount ='" + _warningCount + "', defenceCount ='" + _defenceSpawnCount + "' WHERE mapId ='" + crystalId + "'");
			statement.close();
		}
		catch (SQLException e)
		{
			_log.info("Crystal Manager [Save Data]: " + e);
		}
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if ((player == null) || (player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != this.getObjectId()))
		{
			return;
		}
		if (command.startsWith("manageCrystal"))
		{
			if (player.isClanLeader())
			{
				try
				{
					showManageCrystalWindow(player);
				}
				catch (Exception e)
				{
					System.out.println("MANAGE CRYSTAL ERROR: " + e);
				}
			}
		}
		else if (command.startsWith("disableDefence"))
		{
			setDefenceAbilityEnabled(false);
			saveDataAndShowManageWindow(player);
		}
		else if (command.startsWith("enableDefence"))
		{
			setDefenceAbilityEnabled(true);
			saveDataAndShowManageWindow(player);
		}
		else if (command.startsWith("disableWarning"))
		{
			setWarningAbilityEnabled(false);
			saveDataAndShowManageWindow(player);
		}
		else if (command.startsWith("enableWarning"))
		{
			setWarningAbilityEnabled(true);
			saveDataAndShowManageWindow(player);
		}
		else if (command.startsWith("disableStats"))
		{
			setStatsEnabled(false);
			saveDataAndShowManageWindow(player);
		}
		else if (command.startsWith("enableStats"))
		{
			setStatsEnabled(true);
			saveDataAndShowManageWindow(player);
		}
		else if (command.startsWith("defenceInfo"))
		{
			showCrystalWindow(player, "defence.htm");
		}
		else if (command.startsWith("warningInfo"))
		{
			showCrystalWindow(player, "warning.htm");
		}
		else if (command.startsWith("statsInfo"))
		{
			showCrystalWindow(player, "stats.htm");
		}
		else if (command.startsWith("mainWindow"))
		{
			showChatWindow(player, 0);
		}
		else if (command.startsWith("ownerInfo"))
		{
			showOwnerInfoWindow(player);
		}
		else if (command.startsWith("crystalGuide"))
		{
			showCrystalWindow(player, "guide.htm");
		}
		else if (command.startsWith("abilitiesWindow"))
		{
			showAbilitiesShopWindow(player);
		}
		else if (command.startsWith("buyWarning"))
		{
			try
			{
				String val = command.substring(10);
				StringTokenizer st = new StringTokenizer(val);
				
				String id = st.nextToken();
				int idval = Integer.parseInt(id);
				buyAbility(player, idval, 1);
				showAbilitiesShopWindow(player);
				saveData(this.getNpcId());
			}
			catch (StringIndexOutOfBoundsException e)
			{
				player.sendMessage("Error!");
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Specify a valid number.");
			}
		}
		else if (command.startsWith("buyDefence"))
		{
			try
			{
				String val = command.substring(10);
				StringTokenizer st = new StringTokenizer(val);
				
				String id = st.nextToken();
				int idval = Integer.parseInt(id);
				buyAbility(player, idval, 2);
				showAbilitiesShopWindow(player);
				saveData(this.getNpcId());
			}
			catch (StringIndexOutOfBoundsException e)
			{
				player.sendMessage("Error!");
			}
			catch (NumberFormatException nfe)
			{
				player.sendMessage("Specify a valid number.");
			}
		}
		else if (command.startsWith("locStatus"))
		{
			showLocationsStatusWindow(player);
		}
		else if (command.startsWith("crystalFunctions"))
		{
			showCrystalFunctionsWindow(player);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Crystal</title><body><center><br>");
		tb.append("Conqerable <font color=LEVEL>" + getLocationName() + "</font> Location:<br>");
		tb.append("Welcome <font color=LEVEL>" + player.getName() + "</font>, Im Occupation Crystal!<br>");
		
		if (player.getClanId() == getOwnerClanId())
		{
			if (player.isClanLeader())
			{
				tb.append("How may I help you my owner?<br>");
				tb.append("<button value=\"Manage Functions\" action=\"bypass -h npc_%objectId%_manageCrystal\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
			}
			else
			{
				tb.append("Glad to see you!<br>");
				tb.append("Only clan leader may manage crystal!<br>");
			}
			
			tb.append("<button value=\"Crystal Functions\" action=\"bypass -h npc_%objectId%_crystalFunctions\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			tb.append("Go away, I will not help you!<br>");
			tb.append("You are not my owner!<br>");
		}
		
		tb.append("<button value=\"Owner Info\" action=\"bypass -h npc_%objectId%_ownerInfo\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Locations Status\" action=\"bypass -h npc_%objectId%_locStatus\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Crystal Guide\" action=\"bypass -h npc_%objectId%_crystalGuide\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<br><br><br>By Matim");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	/**
	 * Show info about crystal functions. Are they enabled or disabled. Also possibility to use functions is there. Such as Teleports and additional buffs.
	 * @param player
	 */
	public void showCrystalFunctionsWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Crystal</title><body><center><br>");
		tb.append("Crystal Functions:<br>");
		tb.append("<img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
		tb.append("<td width=270 align=\"center\">Defence Ability: <font color=\"LEVEL\">" + isEnabled(defenceAbilityEnabled()) + "</font></td></tr></table><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
		tb.append("<td width=270 align=\"center\">Warning Ability: <font color=\"LEVEL\">" + isEnabled(warningAbilityEnabled()) + "</font></td></tr></table><br>");
		
		tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left><br>");
		tb.append("<button value=\"Main Window\" action=\"bypass -h npc_%objectId%_mainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	/**
	 * Show basic info about clan which is owner of the location. Info like leader name, clan level, member count etc. <br>
	 * <br>
	 * @param player
	 */
	public void showOwnerInfoWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Crystal</title><body><center><br>");
		tb.append("Info About Owner:<br>");
		tb.append("<img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br><br>");
		
		if (_ownerClanId == 0)
		{
			tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
			tb.append("<td width=270 align=\"center\">Location Owned by: <font color=\"LEVEL\">Nobody</font>!</td></tr></table>");
		}
		else
		{
			if (ClanTable.getInstance().getClan(_ownerClanId) != null)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getOwnerClanId());
				String ownerClanName = clan.getName();
				
				tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
				tb.append("<td width=270 align=\"center\">Location Owned by: <font color=\"LEVEL\">" + ownerClanName + "</font> clan!</td></tr></table><br>");
				
				tb.append("<table width=270 bgcolor=\"144499\"><tr>");
				tb.append("<td width=270 align=\"center\">Clan Leader: <font color=\"LEVEL\">" + clan.getLeaderName() + "</font></td></tr></table><br>");
				
				tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
				tb.append("<td width=270 align=\"center\">Members Count: <font color=\"LEVEL\">" + clan.getMembersCount() + "</font></td></tr></table><br>");
				
				tb.append("<table width=270 bgcolor=\"144499\"><tr>");
				tb.append("<td width=270 align=\"center\">Clan Level: <font color=\"LEVEL\">" + clan.getLevel() + "</font></td></tr></table><br>");
				
				tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
				tb.append("<td width=270 align=\"center\">Online Members: <font color=\"LEVEL\">" + clan.getOnlineMembersCount() + "</font></td></tr></table><br>");
			}
			else
			{
				tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr>");
				tb.append("<td width=270 align=\"center\">Location Owned by: <font color=\"LEVEL\">Nobody</font>!</td></tr></table>");
			}
		}
		
		tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_2\" width=300 height=32 align=left><br>");
		tb.append("<button value=\"Main Window\" action=\"bypass -h npc_%objectId%_mainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	/**
	 * Main window, where clan leader which is owner of the crystal, may manage crystal functions, till now there are 3 available: <li>Stats Upgrade - additional HP/Defence etc. <li>Defence Ability - spawn defenders while crystal is beeing attacked. <li>Warning Ability - warn each clan member while
	 * crystal is attacket. <br>
	 * <br>
	 * @param player
	 */
	public void showManageCrystalWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Crystal Manage</title><body><center><br>");
		tb.append("<font color=LEVEL>" + player.getName() + "</font>, you are my Leader!<br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr><td>Defence Ability</td>");
		tb.append("<td><button value=\"Info\" action=\"bypass -h npc_%objectId%_defenceInfo\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		if (defenceAbilityEnabled())
		{
			tb.append("<td><button value=\"Disable\" action=\"bypass -h npc_%objectId%_disableDefence\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		else
		{
			tb.append("<td><button value=\"Enable\" action=\"bypass -h npc_%objectId%_enableDefence\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		tb.append("<table width=270 border=0><tr><td>Warning Ability</td>");
		tb.append("<td><button value=\"Info\" action=\"bypass -h npc_%objectId%_warningInfo\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		if (warningAbilityEnabled())
		{
			tb.append("<td><button value=\"Disable\" action=\"bypass -h npc_%objectId%_disableWarning\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		else
		{
			tb.append("<td><button value=\"Enable\" action=\"bypass -h npc_%objectId%_enableWarning\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr><td>Stats Upgrades</td>");
		tb.append("<td><button value=\"Info\" action=\"bypass -h npc_%objectId%_statsInfo\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		if (statsUpgradeEnabled())
		{
			tb.append("<td><button value=\"Disable\" action=\"bypass -h npc_%objectId%_disableStats\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		else
		{
			tb.append("<td><button value=\"Enable\" action=\"bypass -h npc_%objectId%_enableStats\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		tb.append("<br><br><button value=\"Buy Abilities\" action=\"bypass -h npc_%objectId%_abilitiesWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\">");
		tb.append("<button value=\"Buy Functions\" action=\"bypass -h npc_%objectId%_functionsWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></body></html>");
		tb.append("<button value=\"Main Window\" action=\"bypass -h npc_%objectId%_mainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	/**
	 * Basic window where owner of the crystal (Clan Leader) may buy additional ammount of the abilities, each time when crystal will use his ability, for example will warn clan members or spawn his defenders (defence ability) defence ability or warning ability count will be decrested, each ability
	 * cos 100 Clan Reputation Points. To prevent spamming with warning etc, there is delay - one minute.
	 * @param player
	 */
	public void showAbilitiesShopWindow(L2PcInstance player)
	{
		L2Clan clan = player.getClan();
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Crystal</title><body><center><br>");
		tb.append("<font color=\"LEVEL\">Abilities Shop:</font><br>");
		tb.append("Each time your clan will be warned or crystal will use Defence Ability (will spawn defenders) - your bought abilities count will be decrested. Here you can buy additional ammount of the abilities!<br>");
		tb.append("You need <font color=\"LEVEL\">100</font> Clan Repotation Points to buy one.<br>");
		tb.append("Your clan CRP ammount: <font color=\"LEVEL\">" + clan.getReputationScore() + "</font><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr><td>Warning Count:</td><td><edit var=\"count\" width=50></td>");
		tb.append("<td><button value=\"Buy\" action=\"bypass -h npc_%objectId%_buyWarning $count\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"444444\"><tr><td>Defence Count:</td><td><edit var=\"count2\" width=50></td>");
		tb.append("<td><button value=\"Buy\" action=\"bypass -h npc_%objectId%_buyDefence $count2\" width=65 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>");
		
		tb.append("<br><br>Held Warnings Left: <font color=\"LEVEL\">" + _warningCount + "</font><br>");
		tb.append("Held Defenders Spawn Left: <font color=\"LEVEL\">" + _defenceSpawnCount + "</font><br>");
		tb.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_manageCrystal\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	/**
	 * Easier way to show Crystal Htm Window.
	 * @param player
	 * @param htm
	 */
	public void showCrystalWindow(L2PcInstance player, String htm)
	{
		String html = null;
		html = HtmCache.getInstance().getHtm(null, "data/html/mods/CrystalMod/" + htm);
		
		NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
		msg.setHtml(html);
		msg.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(msg);
	}
	
	/**
	 * Show list with all conquerable locations status. With info about owner (only clan names)
	 * @param player
	 */
	public void showLocationsStatusWindow(L2PcInstance player)
	{
		TextBuilder tb = new TextBuilder();
		tb.append("<html><title>Crystal</title><body><center><br>");
		tb.append("<font color=\"LEVEL\">Conquerable Locations Status: </font><br>");
		tb.append("<font color=\"LEVEL\">Not implemented yet, sorry! </font><br>");
		tb.append("<button value=\"Back\" action=\"bypass -h npc_%objectId%_mainWindow\" width=160 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		
		L2PcInstance player = null;
		int _clanId = 0;
		
		if (killer instanceof L2PcInstance)
		{
			player = (L2PcInstance) killer;
		}
		else if (killer instanceof L2Summon)
		{
			player = ((L2Summon) killer).getOwner();
		}
		
		if (player != null)
		{
			if (player.getClan() != null)
			{
				if (_ownerClanId != 0)
				{
					L2Clan oldOwner = ClanTable.getInstance().getClan(_ownerClanId);
					sendMessageToClan(oldOwner, "Your clan lost your " + getLocationName() + " Conquerable Location");
				}
				
				_clanId = player.getClanId();
				_ownerClanId = _clanId;
				
				L2Clan newOwner = ClanTable.getInstance().getClan(_clanId);
				
				cleanCrystalFunctions();
				saveData(this.getNpcId());
				sendMessageToClan(newOwner, "Your clan Conquered " + getLocationName() + " Location!");
			}
			else
			{
				player.sendPacket(new ExShowScreenMessage("You need clan to conquer a location!", 5000));
			}
		}
		return true;
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if (!this.canTarget(player))
		{
			return;
		}
		
		player.setLastFolkNPC(this);
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			getAI();
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
			player.sendPacket(new ValidateLocation(this));
		}
		else if (interact)
		{
			if (!isAutoAttackable(player))
			{
				showChatWindow(player, 0);
			}
			else if (!player.isAlikeDead())
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			
			player.sendPacket(new ValidateLocation(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		L2PcInstance player = null;
		boolean canAttack = false;
		if (attacker instanceof L2PcInstance)
		{
			player = (L2PcInstance) attacker;
		}
		else if (attacker instanceof L2Summon)
		{
			player = ((L2Summon) attacker).getOwner();
		}
		
		if (player != null)
		{
			if (player.getClanId() != _ownerClanId)
			{
				canAttack = true;
			}
			
			if (canAttack)
			{
				super.reduceCurrentHp(damage, attacker, skill);
				warnClanOwnerMembers();
				spawnDefenders(attacker);
			}
			else if (player.getClan() == null)
			{
				attacker.sendPacket(new ExShowScreenMessage("You need clan to conquer a location!", 5000));
			}
			else
			{
				attacker.sendPacket(new ExShowScreenMessage("You cant kill your own Crystal!", 5000));
			}
		}
	}
	
	/**
	 * Send Message to each online clan member. <br>
	 * @param clan
	 * @param msg
	 */
	private static void sendMessageToClan(L2Clan clan, String msg)
	{
		if (clan != null)
		{
			for (L2ClanMember member : clan.getMembers())
			{
				if (member.isOnline())
				{
					L2PcInstance player = member.getPlayerInstance();
					player.sendPacket(new ExShowScreenMessage(msg, 5000));
				}
			}
		}
	}
	
	/**
	 * Save data into Database and show Managment window again. Just to reduce same code all the time.
	 * @param player
	 */
	private void saveDataAndShowManageWindow(L2PcInstance player)
	{
		saveData(this.getNpcId());
		showManageCrystalWindow(player);
	}
	
	/**
	 * A way to sell owner Crystal Abilities such as Warning and Defence ones. If abilitiId is 1, it means that it is Warning ability, if 2 its Defence. Owner need Clan Reputation Points to purchase abilities. Each cost 100 Clan Reputation Points.
	 * @param player
	 * @param count
	 * @param abilitiId
	 */
	private void buyAbility(L2PcInstance player, int count, int abilitiId)
	{
		int crp = player.getClan().getReputationScore();
		int price = count * 100;
		
		if (crp >= price)
		{
			if (abilitiId == 1)
			{
				int current = _warningCount;
				_warningCount = current + count;
				player.getClan().setReputationScore(player.getClan().getReputationScore() - price, true);
				player.sendPacket(new CreatureSay(0, Say2.PARTY, "Crystal", "Successfully purchased " + count + " warnings!"));
			}
			else if (abilitiId == 2)
			{
				int current = _defenceSpawnCount;
				_defenceSpawnCount = current + count;
				player.getClan().setReputationScore(player.getClan().getReputationScore() - price, true);
				player.sendPacket(new CreatureSay(0, Say2.PARTY, "Crystal", "Successfully purchased " + count + " Defence Abilities!"));
			}
		}
		else
		{
			player.sendPacket(new CreatureSay(0, Say2.PARTY, "Crystal", "You don't have enough Clan Reputation Points!"));
		}
	}
	
	/**
	 * Warn each online clan member, if their crystal is under attack. To prevent spam after each hit from the attacker, there is delay. Default delay between next warning: 60 second. Also Crystal Warning Ability should be enabled.
	 */
	private void warnClanOwnerMembers()
	{
		if (warningAbilityEnabled())
		{
			if (canWarn())
			{
				if (_warningCount > 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(getOwnerClanId());
					if (clan != null)
					{
						sendMessageToClan(clan, "Your " + getLocationName() + " location is under attack!");
						_warningCount--;
						setCanWarn(false);
						ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleWarningTask(), 60000);
					}
				}
			}
		}
	}
	
	/**
	 * Spawn 5 new Defenders (NPC's) Provoke them to attack character which attacked crystal. After 60 seconds, defenders should disappear.
	 * @param attacker
	 */
	private void spawnDefenders(L2Character attacker)
	{
		if (defenceAbilityEnabled())
		{
			if (canSpawnDefenders())
			{
				if (_defenceSpawnCount > 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(getOwnerClanId());
					if (clan != null)
					{
						L2Npc defender1 = null;
						L2Npc defender2 = null;
						
						for (int i = 1; i <= DEFENDERS_COUNT; i++)
						{
							defender1 = addSpawn(50050, this.getX() - Rnd.get(100), this.getY() - Rnd.get(100), this.getZ());
							defender1.getKnownList().addKnownObject(attacker);
							_defenders.add(defender1);
							defender2 = addSpawn(50051, this.getX() - Rnd.get(100), this.getY() - Rnd.get(100), this.getZ());
							defender2.getKnownList().addKnownObject(attacker);
							_defenders.add(defender2);
						}
						
						for (L2Npc npc : _defenders)
						{
							((L2Attackable) npc).addDamageHate(attacker, 9000, 9000);
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker, null);
							npc.setRunning();
						}
						
						setCanSpawnDefenders(false);
						_defenceSpawnCount--;
						ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleDefenceTast(), 60000);
					}
				}
			}
		}
	}
	
	private static L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(0);
				spawn.setHeading(1);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z);
				spawn.stopRespawn();
				result = spawn.spawnOne(true);
				
				return result;
			}
		}
		catch (Exception e1)
		{
			
		}
		return null;
	}
	
	/**
	 * Prepare crystal functions for new owner. At begining each functions should be disabled. <li>Clean Crystal Functions <li>Save Crystal Data
	 */
	private void cleanCrystalFunctions()
	{
		setCanWarn(true);
		setCanSpawnDefenders(true);
		setDefenceAbilityEnabled(false);
		setWarningAbilityEnabled(false);
		setStatsEnabled(false);
		_warningCount = 0;
		_defenceSpawnCount = 0;
	}
	
	/**
	 * Delay for warning ability, to prevent spam.
	 */
	private class ScheduleWarningTask implements Runnable
	{
		public ScheduleWarningTask()
		{
			// Nothing
		}
		
		@Override
		public void run()
		{
			setCanWarn(true);
		}
	}
	
	/**
	 * Delay for defence spawn, to prevent spam.
	 */
	private class ScheduleDefenceTast implements Runnable
	{
		public ScheduleDefenceTast()
		{
			// Nothing
		}
		
		@Override
		public void run()
		{
			setCanSpawnDefenders(true);
			
			for (L2Npc npc : _defenders)
			{
				npc.deleteMe();
			}
			
			_defenders.clear();
		}
	}
	
	/**
	 * @param b
	 * @return Enabled/Disabled
	 */
	private String isEnabled(boolean b)
	{
		if (b)
		{
			return "Enabled";
		}
		return "Disabled";
	}
	
	public void setOwnerClanId(int clanId)
	{
		_ownerClanId = clanId;
	}
	
	public int getOwnerClanId()
	{
		return _ownerClanId;
	}
	
	public void setLocationName(String name)
	{
		_locationName = name;
	}
	
	public String getLocationName()
	{
		return _locationName;
	}
	
	public boolean defenceAbilityEnabled()
	{
		return _defenceAbilityEnabled;
	}
	
	public void setDefenceAbilityEnabled(boolean b)
	{
		_defenceAbilityEnabled = b;
	}
	
	public boolean warningAbilityEnabled()
	{
		return _warningAbilityEnabled;
	}
	
	public void setWarningAbilityEnabled(boolean b)
	{
		_warningAbilityEnabled = b;
	}
	
	public boolean statsUpgradeEnabled()
	{
		return _statsUpgradeEnabled;
	}
	
	public void setStatsEnabled(boolean b)
	{
		_statsUpgradeEnabled = b;
	}
	
	public boolean canWarn()
	{
		return _canWarn;
	}
	
	public void setCanWarn(boolean b)
	{
		_canWarn = b;
	}
	
	public boolean canSpawnDefenders()
	{
		return _canSpawnDefenders;
	}
	
	public void setCanSpawnDefenders(boolean b)
	{
		_canSpawnDefenders = b;
	}
}