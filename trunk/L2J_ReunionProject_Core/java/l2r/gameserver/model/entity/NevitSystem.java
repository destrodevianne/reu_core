package l2r.gameserver.model.entity;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.annotations.RegisterEvent;
import l2r.gameserver.model.events.impl.character.player.OnPlayerLogin;
import l2r.gameserver.model.events.impl.character.player.OnPlayerLogout;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.model.interfaces.IUniqueId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExNevitAdventEffect;
import l2r.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import l2r.gameserver.network.serverpackets.ExNevitAdventTimeChange;

public class NevitSystem implements IUniqueId
{
	// Timers
	private static final int MAX_POINTS = 7200;
	private static final int BONUS_EFFECT_TIME = 180;
	
	// Nevit Hour
	private static final int ADVENT_TIME = 14400;
	public final L2PcInstance _player;
	
	private volatile ScheduledFuture<?> _adventTask;
	private volatile ScheduledFuture<?> _nevitEffectTask;
	
	public NevitSystem(L2PcInstance player)
	{
		_player = player;
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGIN, (OnPlayerLogin event) -> onPlayerLogin(event), this));
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> OnPlayerLogout(event), this));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	private void onPlayerLogin(OnPlayerLogin event)
	{
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6);
		cal.set(Calendar.MINUTE, 30);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		// Reset Nevit's Blessing
		if ((_player.getLastAccess() < (cal.getTimeInMillis() / 1000L)) && (System.currentTimeMillis() > cal.getTimeInMillis()))
		{
			_player.getVariables().set("hunting_time", 0);
		}
		
		// Send Packets
		_player.sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
		_player.sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
		
		startNevitEffect(_player.getVariables().getInt("nevit_b", 0));
		
		// Set percent
		int percent = calcPercent(_player.getVariables().getInt("hunting_points", 0));
		
		if ((percent >= 45) && (percent < 50))
		{
			_player.sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
		}
		else if ((percent >= 50) && (percent < 75))
		{
			_player.sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
		}
		else if (percent >= 75)
		{
			_player.sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	private void OnPlayerLogout(OnPlayerLogout event)
	{
		stopNevitEffectTask(true);
		stopAdventTask(false);
	}
	
	public void addPoints(int val)
	{
		if (getEffectTime() > 0)
		{
			setAdventPoints(0);
		}
		else
		{
			setAdventPoints(getAdventPoints() + val);
		}
		
		if (getAdventPoints() > MAX_POINTS)
		{
			setAdventPoints(0);
			startNevitEffect(BONUS_EFFECT_TIME);
		}
		
		int percent = calcPercent(getAdventPoints());
		switch (percent)
		{
			case 45:
			{
				getPlayer().sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
				break;
			}
			case 50:
			{
				getPlayer().sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
				break;
			}
			case 75:
			{
				getPlayer().sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
				break;
			}
		}
		
		getPlayer().sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
	}
	
	public void startAdventTask()
	{
		if (_adventTask == null)
		{
			synchronized (this)
			{
				if ((_adventTask == null) && (getAdventTime() < ADVENT_TIME))
				{
					_adventTask = ThreadPoolManager.getInstance().scheduleGeneral(new AdventTask(), 30000);
					getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
				}
			}
		}
	}
	
	public class AdventTask implements Runnable
	{
		@Override
		public void run()
		{
			setAdventTime(getAdventTime() + 30);
			if (getAdventTime() >= ADVENT_TIME)
			{
				setAdventTime(ADVENT_TIME);
				stopAdventTask(true);
			}
			else
			{
				addPoints(72);
				if ((getAdventTime() % 60) == 0)
				{
					getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
				}
			}
			stopAdventTask(false);
		}
	}
	
	public synchronized void stopAdventTask(boolean sendPacket)
	{
		if (_adventTask != null)
		{
			_adventTask.cancel(true);
			_adventTask = null;
		}
		if (sendPacket)
		{
			getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
		}
	}
	
	public synchronized void startNevitEffect(int time)
	{
		if (getEffectTime() > 0)
		{
			stopNevitEffectTask(false);
			time += getEffectTime();
		}
		if ((getAdventTime() < ADVENT_TIME) && (time > 0))
		{
			getPlayer().getVariables().set("nevit_b", time);
			getPlayer().sendPacket(new ExNevitAdventEffect(time));
			getPlayer().sendPacket(SystemMessageId.THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE);
			getPlayer().startSpecialEffect(AbnormalEffect.NAVIT_ADVENT.getMask());
			_nevitEffectTask = ThreadPoolManager.getInstance().scheduleGeneral(new NevitEffectEnd(), time * 1000L);
		}
	}
	
	public class NevitEffectEnd implements Runnable
	{
		@Override
		public void run()
		{
			getPlayer().getVariables().remove("nevit_b");
			getPlayer().sendPacket(new ExNevitAdventEffect(0));
			getPlayer().sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
			getPlayer().sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_HAS_ENDED);
			getPlayer().stopSpecialEffect(AbnormalEffect.NAVIT_ADVENT.getMask());
			stopNevitEffectTask(false);
		}
	}
	
	public synchronized void stopNevitEffectTask(boolean saveTime)
	{
		if (_nevitEffectTask != null)
		{
			if (saveTime)
			{
				int time = getEffectTime();
				if (time > 0)
				{
					getPlayer().getVariables().set("nevit_b", time);
				}
				else
				{
					getPlayer().getVariables().remove("nevit_b");
				}
			}
			_nevitEffectTask.cancel(true);
			_nevitEffectTask = null;
		}
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	@Override
	public int getObjectId()
	{
		return _player.getObjectId();
	}
	
	private int getEffectTime()
	{
		if (_nevitEffectTask == null)
		{
			return 0;
		}
		return (int) Math.max(0, _nevitEffectTask.getDelay(TimeUnit.SECONDS));
	}
	
	public boolean isAdventBlessingActive()
	{
		return ((_nevitEffectTask != null) && (_nevitEffectTask.getDelay(TimeUnit.MILLISECONDS) > 0));
	}
	
	public static int calcPercent(int points)
	{
		return (int) ((100.0D / MAX_POINTS) * points);
	}
	
	public void setAdventPoints(int points)
	{
		getPlayer().getVariables().set("hunting_points", points);
	}
	
	public void setAdventTime(int time)
	{
		getPlayer().getVariables().set("hunting_time", time);
	}
	
	public int getAdventPoints()
	{
		return getPlayer().getVariables().getInt("hunting_points", 0);
	}
	
	public int getAdventTime()
	{
		return getPlayer().getVariables().getInt("hunting_time", 0);
	}
}