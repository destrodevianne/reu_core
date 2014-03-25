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
package l2r.gameserver.model.items.type;

/**
 * Description of Armor Type
 */

public enum ArmorType implements ItemType
{
	NONE("None"),
	LIGHT("Light"),
	HEAVY("Heavy"),
	MAGIC("Magic"),
	SIGIL("Sigil"),
	
	// L2J CUSTOM
	SHIELD("Shield");
	
	final int _mask;
	final String _name;
	
	/**
	 * Constructor of the ArmorType.
	 * @param name : String designating the name of the ArmorType
	 */
	private ArmorType(String name)
	{
		_mask = 1 << (ordinal() + WeaponType.values().length);
		_name = name;
	}
	
	/**
	 * @return the ID of the ArmorType after applying a mask.
	 */
	@Override
	public int mask()
	{
		return _mask;
	}
	
	/**
	 * @return the name of the ArmorType
	 */
	@Override
	public String getName()
	{
		return _name;
	}
}
