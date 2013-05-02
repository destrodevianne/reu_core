package l2r.gameserver.handler;

import l2r.gameserver.instancemanager.TransformationManager;
import l2r.gameserver.scripts.handlers.transformations.*;

public class TransformHandler
{
	protected TransformHandler()
	{
		TransformationManager.getInstance().registerTransformation(new Akamanah());
		TransformationManager.getInstance().registerTransformation(new Anakim());
		TransformationManager.getInstance().registerTransformation(new AquaElf());
		TransformationManager.getInstance().registerTransformation(new ArcherCaptain());
		TransformationManager.getInstance().registerTransformation(new AurabirdFalcon());
		TransformationManager.getInstance().registerTransformation(new AurabirdOwl());
		TransformationManager.getInstance().registerTransformation(new Benom());
		TransformationManager.getInstance().registerTransformation(new BlockCheckerBlue());
		TransformationManager.getInstance().registerTransformation(new Buffalo());
		TransformationManager.getInstance().registerTransformation(new DarkElfMercenary());
		TransformationManager.getInstance().registerTransformation(new DarkmanePacer());
		TransformationManager.getInstance().registerTransformation(new DemonPrince());
		TransformationManager.getInstance().registerTransformation(new DemonRace());
		TransformationManager.getInstance().registerTransformation(new DivineEnchanter());
		TransformationManager.getInstance().registerTransformation(new DivineHealer());
		TransformationManager.getInstance().registerTransformation(new DivineKnight());
		TransformationManager.getInstance().registerTransformation(new DivineRogue());
		TransformationManager.getInstance().registerTransformation(new DivineSummoner());
		TransformationManager.getInstance().registerTransformation(new DivineWarrior());
		TransformationManager.getInstance().registerTransformation(new DivineWizard());
		TransformationManager.getInstance().registerTransformation(new DollBlader());
		TransformationManager.getInstance().registerTransformation(new DoomWraith());
		TransformationManager.getInstance().registerTransformation(new DragonBomberNormal());
		TransformationManager.getInstance().registerTransformation(new DragonBomberStrong());
		TransformationManager.getInstance().registerTransformation(new DragonBomberWeak());
		TransformationManager.getInstance().registerTransformation(new DragonMasterKarin());
		TransformationManager.getInstance().registerTransformation(new DragonMasterLee());
		TransformationManager.getInstance().registerTransformation(new DwarfGolem());
		TransformationManager.getInstance().registerTransformation(new DwarfMercenary());
		TransformationManager.getInstance().registerTransformation(new ElfMercenary());
		TransformationManager.getInstance().registerTransformation(new FlyingFinalForm());
		TransformationManager.getInstance().registerTransformation(new EpicQuestChild());
		TransformationManager.getInstance().registerTransformation(new EpicQuestFrog());
		TransformationManager.getInstance().registerTransformation(new EpicQuestNative());
		TransformationManager.getInstance().registerTransformation(new FortressCaptain());
		TransformationManager.getInstance().registerTransformation(new GameManager());
		TransformationManager.getInstance().registerTransformation(new GolemGuardianNormal());
		TransformationManager.getInstance().registerTransformation(new GolemGuardianStrong());
		TransformationManager.getInstance().registerTransformation(new GolemGuardianWeak());
		TransformationManager.getInstance().registerTransformation(new Gordon());
		TransformationManager.getInstance().registerTransformation(new GrailApostleNormal());
		TransformationManager.getInstance().registerTransformation(new GrailApostleStrong());
		TransformationManager.getInstance().registerTransformation(new GrailApostleWeak());
		TransformationManager.getInstance().registerTransformation(new GrizzlyBear());
		TransformationManager.getInstance().registerTransformation(new GuardianStrider());
		TransformationManager.getInstance().registerTransformation(new GuardsoftheDawn());
		TransformationManager.getInstance().registerTransformation(new HeavyTow());
		TransformationManager.getInstance().registerTransformation(new Heretic());
		TransformationManager.getInstance().registerTransformation(new HumanMercenary());
		TransformationManager.getInstance().registerTransformation(new InfernoDrakeNormal());
		TransformationManager.getInstance().registerTransformation(new InfernoDrakeStrong());
		TransformationManager.getInstance().registerTransformation(new InfernoDrakeWeak());
		TransformationManager.getInstance().registerTransformation(new InquisitorBishop());
		TransformationManager.getInstance().registerTransformation(new InquisitorElvenElder());
		TransformationManager.getInstance().registerTransformation(new InquisitorShilienElder());
		TransformationManager.getInstance().registerTransformation(new JetBike());
		TransformationManager.getInstance().registerTransformation(new Kadomas());
		TransformationManager.getInstance().registerTransformation(new Kamael());
		TransformationManager.getInstance().registerTransformation(new KamaelGuardCaptain());
		TransformationManager.getInstance().registerTransformation(new KamaelMercenary());
		TransformationManager.getInstance().registerTransformation(new Kiyachi());
		TransformationManager.getInstance().registerTransformation(new KnightofDawn());
		TransformationManager.getInstance().registerTransformation(new LavaGolem());
		TransformationManager.getInstance().registerTransformation(new LilimKnightNormal());
		TransformationManager.getInstance().registerTransformation(new LilimKnightStrong());
		TransformationManager.getInstance().registerTransformation(new LilimKnightWeak());
		TransformationManager.getInstance().registerTransformation(new LureTow());
		TransformationManager.getInstance().registerTransformation(new MagicLeader());
		TransformationManager.getInstance().registerTransformation(new MyoRace());
		TransformationManager.getInstance().registerTransformation(new Native());
		TransformationManager.getInstance().registerTransformation(new OlMahum());
		TransformationManager.getInstance().registerTransformation(new OnyxBeast());
		TransformationManager.getInstance().registerTransformation(new OrcMercenary());
		TransformationManager.getInstance().registerTransformation(new Pig());
		TransformationManager.getInstance().registerTransformation(new Pixy());
		TransformationManager.getInstance().registerTransformation(new PumpkinGhost());
		TransformationManager.getInstance().registerTransformation(new Rabbit());
		TransformationManager.getInstance().registerTransformation(new Ranku());
		TransformationManager.getInstance().registerTransformation(new RoyalGuardCaptain());
		TransformationManager.getInstance().registerTransformation(new SaberToothTiger());
		TransformationManager.getInstance().registerTransformation(new Scarecrow());
		TransformationManager.getInstance().registerTransformation(new ScrollBlue());
		TransformationManager.getInstance().registerTransformation(new ScrollRed());
		TransformationManager.getInstance().registerTransformation(new ShinyPlatform());
		TransformationManager.getInstance().registerTransformation(new SnowKung());
		TransformationManager.getInstance().registerTransformation(new SteamBeatle());
		TransformationManager.getInstance().registerTransformation(new SujinChild());
		TransformationManager.getInstance().registerTransformation(new TawnyManedLion());
		TransformationManager.getInstance().registerTransformation(new Teleporter());
		TransformationManager.getInstance().registerTransformation(new Teleporter2());
		TransformationManager.getInstance().registerTransformation(new Timitran());
		TransformationManager.getInstance().registerTransformation(new TinGolem());
		TransformationManager.getInstance().registerTransformation(new Tow());
		TransformationManager.getInstance().registerTransformation(new TrejuoChild());
		TransformationManager.getInstance().registerTransformation(new Treykan());
		TransformationManager.getInstance().registerTransformation(new Unicorniun());
		TransformationManager.getInstance().registerTransformation(new UnicornNormal());
		TransformationManager.getInstance().registerTransformation(new UnicornStrong());
		TransformationManager.getInstance().registerTransformation(new UnicornWeak());
		TransformationManager.getInstance().registerTransformation(new ValeMaster());
		TransformationManager.getInstance().registerTransformation(new VanguardDarkAvenger());
		TransformationManager.getInstance().registerTransformation(new VanguardPaladin());
		TransformationManager.getInstance().registerTransformation(new VanguardShilienKnight());
		TransformationManager.getInstance().registerTransformation(new VanguardTempleKnight());
		TransformationManager.getInstance().registerTransformation(new WingTow());
		TransformationManager.getInstance().registerTransformation(new WoodHorse());
		TransformationManager.getInstance().registerTransformation(new Yeti());
		TransformationManager.getInstance().registerTransformation(new Yeti2());
		TransformationManager.getInstance().registerTransformation(new Zaken());
		TransformationManager.getInstance().registerTransformation(new Zariche());
		TransformationManager.getInstance().registerTransformation(new Zombie());
	}
	
	public static TransformHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TransformHandler _instance = new TransformHandler();
	}
}