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
package l2r.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.Server;
import l2r.gameserver.cache.CrestCache;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.AdminTable;
import l2r.gameserver.datatables.ArmorSetsData;
import l2r.gameserver.datatables.AugmentationData;
import l2r.gameserver.datatables.CharNameTable;
import l2r.gameserver.datatables.CharSummonTable;
import l2r.gameserver.datatables.CharTemplateTable;
import l2r.gameserver.datatables.ClanTable;
import l2r.gameserver.datatables.ClassListData;
import l2r.gameserver.datatables.DoorTable;
import l2r.gameserver.datatables.EnchantGroupsData;
import l2r.gameserver.datatables.EnchantHPBonusData;
import l2r.gameserver.datatables.EnchantItemData;
import l2r.gameserver.datatables.EnchantOptionsData;
import l2r.gameserver.datatables.EventDroplist;
import l2r.gameserver.datatables.ExperienceTable;
import l2r.gameserver.datatables.FishData;
import l2r.gameserver.datatables.FishingMonstersData;
import l2r.gameserver.datatables.FishingRodsData;
import l2r.gameserver.datatables.HennaData;
import l2r.gameserver.datatables.HerbDropTable;
import l2r.gameserver.datatables.HitConditionBonus;
import l2r.gameserver.datatables.InitialEquipmentData;
import l2r.gameserver.datatables.ItemTable;
import l2r.gameserver.datatables.ManorData;
import l2r.gameserver.datatables.MerchantPriceConfigTable;
import l2r.gameserver.datatables.MultiSell;
import l2r.gameserver.datatables.NpcBufferTable;
import l2r.gameserver.datatables.NpcTable;
import l2r.gameserver.datatables.OfflineTradersTable;
import l2r.gameserver.datatables.OptionsData;
import l2r.gameserver.datatables.PetDataTable;
import l2r.gameserver.datatables.RecipeData;
import l2r.gameserver.datatables.SkillTable;
import l2r.gameserver.datatables.SkillTreesData;
import l2r.gameserver.datatables.SpawnTable;
import l2r.gameserver.datatables.StaticObjects;
import l2r.gameserver.datatables.SummonSkillsTable;
import l2r.gameserver.datatables.TeleportLocationTable;
import l2r.gameserver.datatables.UIData;
import l2r.gameserver.geoeditorcon.GeoEditorListener;
import l2r.gameserver.handler.EffectHandler;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.instancemanager.AirShipManager;
import l2r.gameserver.instancemanager.AntiFeedManager;
import l2r.gameserver.instancemanager.AuctionManager;
import l2r.gameserver.instancemanager.BoatManager;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.CastleManorManager;
import l2r.gameserver.instancemanager.ClanHallManager;
import l2r.gameserver.instancemanager.CoupleManager;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.DayNightSpawnManager;
import l2r.gameserver.instancemanager.DimensionalRiftManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.instancemanager.FourSepulchersManager;
import l2r.gameserver.instancemanager.GlobalVariablesManager;
import l2r.gameserver.instancemanager.GraciaSeedsManager;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.instancemanager.HellboundManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.instancemanager.ItemAuctionManager;
import l2r.gameserver.instancemanager.ItemsOnGroundManager;
import l2r.gameserver.instancemanager.MailManager;
import l2r.gameserver.instancemanager.MapRegionManager;
import l2r.gameserver.instancemanager.MercTicketManager;
import l2r.gameserver.instancemanager.PcCafePointsManager;
import l2r.gameserver.instancemanager.PetitionManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.RaidBossPointsManager;
import l2r.gameserver.instancemanager.RaidBossSpawnManager;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.instancemanager.TransformationManager;
import l2r.gameserver.instancemanager.WalkingManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.AutoSpawnHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.PartyMatchRoomList;
import l2r.gameserver.model.PartyMatchWaitingList;
import l2r.gameserver.model.entity.Hero;
import l2r.gameserver.model.olympiad.Olympiad;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.L2GamePacketHandler;
import l2r.gameserver.network.communityserver.CommunityServerThread;
import l2r.gameserver.pathfinding.PathFinding;
import l2r.gameserver.script.faenor.FaenorScriptEngine;
import l2r.gameserver.scripting.L2ScriptEngineManager;
import l2r.gameserver.taskmanager.AutoAnnounceTaskManager;
import l2r.gameserver.taskmanager.KnownListUpdateTaskManager;
import l2r.gameserver.taskmanager.TaskManager;
import l2r.status.Status;
import l2r.util.DeadLockDetector;
import l2r.util.IPv4Filter;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import gr.reunion.configs.CustomConfigController;
import gr.reunion.interf.NexusEvents;
import gr.reunion.main.CustomServerMods;
import gr.reunion.main.ReunionInfo;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		_log.finest(getClass().getSimpleName() + ": used mem:" + getUsedMemoryMB() + "MB");
		
		if (Config.SERVER_VERSION != null)
		{
			_log.info(getClass().getSimpleName() + ": L2J Server Version:    " + Config.SERVER_VERSION);
		}
		if (Config.DATAPACK_VERSION != null)
		{
			_log.info(getClass().getSimpleName() + ": L2J Datapack Version:  " + Config.DATAPACK_VERSION);
		}
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe(getClass().getSimpleName() + ": Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillTable.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		EnchantItemData.getInstance();
		EnchantOptionsData.getInstance();
		OptionsData.getInstance();
		EnchantHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		TradeController.getInstance();
		MultiSell.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		ExperienceTable.getInstance();
		HitConditionBonus.getInstance();
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		AdminTable.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			PathFinding.getInstance();
		}
		
		printSection("NPCs");
		HerbDropTable.getInstance();
		NpcTable.getInstance();
		WalkingManager.getInstance();
		StaticObjects.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		HellboundManager.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		PcCafePointsManager.getInstance();
		ManorData.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();
		TeleportLocationTable.getInstance();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		
		// Custom settings section
		printSection("Custom");
		CustomServerMods.getInstance().checkCustomMods();
		// System.out.println("Loading static images....");
		// CustomServerMods.getInstance().loadStaticImages();
		
		// Antibot systems
		printSection("Antibot Engine");
		CustomServerMods.getInstance().checkAntibotMod();
		
		// Leader board load data
		printSection("Leaderboards");
		CustomServerMods.getInstance().checkLeaderboardsMod();
		
		printSection("Scripts");
		QuestManager.getInstance();
		TransformationManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceConfigTable.getInstance().updateReferences();
		
		try
		{
			_log.info(getClass().getSimpleName() + ": Loading Server Scripts");
			File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
			}
		}
		catch (IOException ioe)
		{
			_log.severe(getClass().getSimpleName() + ": Failed loading scripts.cfg, no script going to be loaded");
		}
		
		QuestManager.getInstance().report();
		TransformationManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		NexusEvents.start();
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the
		// allocation pool
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(getClass().getSimpleName() + ": Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		L2World.prepare();
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");
		
		AutoAnnounceTaskManager.getInstance();
		
		ReunionInfo.load();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		// Custom configs load section
		CustomConfigController.getInstance().reloadCustomConfigs();
		_log.info("Custom Configs Loaded...");
		printSection("Database");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info(GameServer.class.getSimpleName() + ": Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}
