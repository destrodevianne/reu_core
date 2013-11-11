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
package l2r.gameserver.model.items.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mkizub <BR>
 *         Description of Weapon Type
 */
public enum L2WeaponType implements L2ItemType
{
	SWORD("Sword"),
	BLUNT("Blunt"),
	DAGGER("Dagger"),
	BOW("Bow"),
	POLE("Pole"),
	NONE("None"),
	DUAL("Dual Sword"),
	ETC("Etc"),
	FIST("Fist"),
	DUALFIST("Dual Fist"),
	FISHINGROD("Rod"),
	RAPIER("Rapier"),
	ANCIENTSWORD("Ancient"),
	CROSSBOW("Crossbow"),
	FLAG("Flag"),
	OWNTHING("Ownthing"),
	DUALDAGGER("Dual Dagger"),
	
	// L2J CUSTOM, BACKWARD COMPATIBILITY
	BIGBLUNT("Big Blunt"),
	BIGSWORD("Big Sword");
	
	private static final Logger _log = LoggerFactory.getLogger(L2WeaponType.class);
	private final int _mask;
	private final String _name;
	
	/**
	 * Constructor of the L2WeaponType.
	 * @param name : String designating the name of the WeaponType
	 */
	private L2WeaponType(String name)
	{
		_mask = 1 << ordinal();
		_name = name;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public int mask()
	{
		return _mask;
	}
	
	/**
	 * Returns the name of the WeaponType
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _name;
	}
	
	public static L2WeaponType findByName(String name)
	{
		if (name.equalsIgnoreCase("DUAL"))
		{
			name = "Dual Sword";
		}
		else if (name.equalsIgnoreCase("DUALFIST"))
		{
			name = "Dual Fist";
		}
		for (L2WeaponType type : values())
		{
			if (type.toString().equalsIgnoreCase(name))
			{
				return type;
			}
		}
		_log.warn(L2WeaponType.class.getSimpleName() + ": Requested unexistent enum member: " + name, new IllegalStateException());
		return FIST;
	}
}
