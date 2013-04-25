package l2r.gameserver.model.actor.instance;

import gr.reunion.buffItem.BuffCategories;

/**
 * The class used to add user selected buffs in one profile
 * @author DoctorNo
 */
class buffSaver implements Runnable
{
	private final L2PcInstance _player;
	private final L2BufferInstance _npc;
	private final BuffCategories _category;
	private final String _profile;
	private final int _buffId;
	
	public buffSaver(L2PcInstance player, BuffCategories category, String profile, int buffId, L2BufferInstance npcId)
	{
		_player = player;
		_category = category;
		_profile = profile;
		_buffId = buffId;
		_npc = npcId;
	}
	
	@Override
	public void run()
	{
		_player.addBuffToProfile(_profile, _buffId);
		_npc.callBuffToAdd(_category, _player, _profile);
	}
}

/**
 * The class used to delete user selected buffs from one profile
 * @author DoctorNo
 */
class buffDeleter implements Runnable
{
	private final L2PcInstance _player;
	private final L2BufferInstance _npc;
	private final String _profile;
	private final int _buffId;
	
	public buffDeleter(L2PcInstance player, String profile, int buffId, L2BufferInstance npcId)
	{
		_player = player;
		_profile = profile;
		_buffId = buffId;
		_npc = npcId;
	}
	
	@Override
	public void run()
	{
		_player.delBuffFromProfile(_profile, _buffId);
		_npc.showBuffsToDelete(_player, _profile, "removeBuffs");
	}
}