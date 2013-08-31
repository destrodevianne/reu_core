package l2r.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import l2r.gameserver.enums.TaskType;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.taskmanager.Task;
import l2r.gameserver.taskmanager.TaskManager;
import gr.reunion.datatables.AdventTable;

public class TaskAdvent extends Task
{
	private static final Logger _log = Logger.getLogger(TaskAdvent.class.getName());
	private static final String NAME = "huntingbonus";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(TaskManager.ExecutedTask task)
	{
		AdventTable.getInstance().execRecTask();
		L2PcInstance[] onlinePlayers = L2World.getInstance().getAllPlayersArray();
		for (L2PcInstance player : onlinePlayers)
		{
			if ((player != null) && (player.isOnline()))
			{
				player.pauseAdventTask();
				player.startAdventTask();
			}
		}
		_log.config("Hunting Bonus System reseted");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskType.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}