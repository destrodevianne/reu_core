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
package l2r.gameserver.model;

import l2r.gameserver.model.actor.L2Character;

public final class Location
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _instanceId;
	
	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
		_instanceId = -1;
	}
	
	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
		_instanceId = -1;
	}
	
	public Location(int x, int y, int z, int heading, int instanceId)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
		_instanceId = instanceId;
	}
	
	public Location(L2Object obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_instanceId = obj.getInstanceId();
	}
	
	public Location(L2Character obj)
	{
		_x = obj.getX();
		_y = obj.getY();
		_z = obj.getZ();
		_heading = obj.getHeading();
		_instanceId = obj.getInstanceId();
	}
	
	/**
	 * Get the x coordinate.
	 * @return the x coordinate
	 */
	public int getX()
	{
		return _x;
	}
	
	/**
	 * Set the x coordinate.
	 * @param x the x coordinate
	 */
	public void setX(int x)
	{
		_x = x;
	}
	
	/**
	 * Get the y coordinate.
	 * @return the y coordinate
	 */
	public int getY()
	{
		return _y;
	}
	
	/**
	 * Set the y coordinate.
	 * @param y the x coordinate
	 */
	public void setY(int y)
	{
		_y = y;
	}
	
	/**
	 * Get the z coordinate.
	 * @return the z coordinate
	 */
	public int getZ()
	{
		return _z;
	}
	
	/**
	 * Set the z coordinate.
	 * @param z the z coordinate
	 */
	public void setZ(int z)
	{
		_z = z;
	}
	
	/**
	 * Set the x, y, z coordinates.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public void setXYZ(int x, int y, int z)
	{
		setX(x);
		setY(y);
		setZ(z);
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	@Override
	public String toString()
	{
		return "[" + getClass().getSimpleName() + "] X: " + _x + " Y: " + _y + " Z: " + _z + " Heading: " + _heading + " InstanceId: " + _instanceId;
	}
	
	/**
	 * @param instanceId the instance Id to set
	 */
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	// Valanths
	public void setHeading(int heading)
	{
		_heading = heading;
	}
}
