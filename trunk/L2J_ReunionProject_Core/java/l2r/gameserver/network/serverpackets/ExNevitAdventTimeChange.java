/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.network.serverpackets;

/**
 * @author godfather
 */
public class ExNevitAdventTimeChange extends L2GameServerPacket
{
	private final int _active;
	private final int _time;
	
	// Add NevitAdvent by pmq Start
	public ExNevitAdventTimeChange(int time, boolean active)
	{
		_time = time > 14400 ? 14400 : time;
		_active = active ? 1 : 0;
	}
	
	@Override
	protected void writeImpl()
	{
		writeH(0xE1);
		writeC(_active);
		writeD(_time);
	}
}