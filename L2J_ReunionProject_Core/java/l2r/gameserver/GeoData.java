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
package l2r.gameserver;

import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.interfaces.ILocational;
import l2r.gameserver.util.GeoUtils;
import l2r.gameserver.util.LinePointIterator;
import l2r.gameserver.util.Util;

import com.l2jserver.gameserver.geoengine.Direction;
import com.l2jserver.gameserver.geoengine.NullDriver;
import com.l2jserver.gameserver.geoengine.abstraction.IGeoDriver;

/**
 * @author -Nemesiss-, FBIagent
 */
public class GeoData implements IGeoDriver
{
	private static class SingletonHolder
	{
		protected final static GeoData _instance;
		
		static
		{
			_instance = new GeoData();
		}
	}
	
	private static final Logger _LOGGER = Logger.getLogger(GeoData.class.getName());
	private static final int _ELEVATED_SEE_OVER_DISTANCE = 2;
	private static final int _MAX_SEE_OVER_HEIGHT = 32;
	
	public static GeoData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final IGeoDriver _driver;
	
	protected GeoData()
	{
		if (Config.GEODATA > 0)
		{
			IGeoDriver driver = null;
			try
			{
				Class<?> cls = Class.forName(Config.GEODATA_DRIVER);
				if (!IGeoDriver.class.isAssignableFrom(cls))
				{
					throw new ClassCastException("Geodata driver class needs to implement IGeoDriver!");
				}
				Constructor<?> ctor = cls.getConstructor(Properties.class);
				Properties props = new Properties();
				try (FileInputStream fis = new FileInputStream(Paths.get("config", "GeoDriver.properties").toString()))
				{
					props.load(fis);
				}
				driver = (IGeoDriver) ctor.newInstance(props);
			}
			catch (Exception ex)
			{
				_LOGGER.log(Level.SEVERE, "Failed to load geodata driver!", ex);
				System.exit(1);
			}
			// we do it this way so it's predictable for the compiler
			_driver = driver;
		}
		else
		{
			_driver = new NullDriver(null);
		}
	}
	
	@Override
	public int getGeoX(int worldX)
	{
		return _driver.getGeoX(worldX);
	}
	
	@Override
	public int getGeoY(int worldY)
	{
		return _driver.getGeoY(worldY);
	}
	
	@Override
	public int getWorldX(int geoX)
	{
		return _driver.getWorldX(geoX);
	}
	
	@Override
	public int getWorldY(int geoY)
	{
		return _driver.getWorldY(geoY);
	}
	
	@Override
	public boolean hasGeoPos(int geoX, int geoY)
	{
		return _driver.hasGeoPos(geoX, geoY);
	}
	
	@Override
	public int getNearestZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNearestZ(geoX, geoY, worldZ);
	}
	
	@Override
	public int getNextLowerZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNextLowerZ(geoX, geoY, worldZ);
	}
	
	@Override
	public int getNextHigherZ(int geoX, int geoY, int worldZ)
	{
		return _driver.getNextHigherZ(geoX, geoY, worldZ);
	}
	
	@Override
	public boolean canEnterNeighbors(int geoX, int geoY, int worldZ, Direction first, Direction... more)
	{
		return _driver.canEnterNeighbors(geoX, geoY, worldZ, first, more);
	}
	
	@Override
	public boolean canEnterAllNeighbors(int geoX, int geoY, int worldZ)
	{
		return _driver.canEnterAllNeighbors(geoX, geoY, worldZ);
	}
	
	// ///////////////////
	// L2J METHODS
	/**
	 * Gets the height.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return the height
	 */
	public int getHeight(int x, int y, int z)
	{
		return getNearestZ(getGeoX(x), getGeoY(y), z);
	}
	
	/**
	 * Gets the spawn height.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param zmin the minimum z coordinate
	 * @param zmax the the maximum z coordinate
	 * @return the spawn height
	 */
	public int getSpawnHeight(int x, int y, int zmin, int zmax)
	{
		// + 30, defend against defective geodata and invalid spawn z :(
		return getNextLowerZ(getGeoX(x), getGeoY(y), zmax + 30);
	}
	
	/**
	 * Can see target. Doors as target always return true. Checks doors between.
	 * @param cha the character
	 * @param target the target
	 * @return {@code true} if the character can see the target (LOS), {@code false} otherwise
	 */
	public boolean canSeeTarget(L2Object cha, L2Object target)
	{
		if (target.isDoor())
		{
			// can always see doors :o
			return true;
		}
		
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), cha.getInstanceId(), target.getX(), target.getY(), target.getZ(), target.getInstanceId());
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param cha the character
	 * @param worldPosition the world position
	 * @return {@code true} if the character can see the target at the given world position, {@code false} otherwise
	 */
	public boolean canSeeTarget(L2Object cha, ILocational worldPosition)
	{
		return canSeeTarget(cha.getX(), cha.getY(), cha.getZ(), cha.getInstanceId(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param instanceId
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @param tInstanceId the target's instanceId
	 * @return
	 */
	public boolean canSeeTarget(int x, int y, int z, int instanceId, int tx, int ty, int tz, int tInstanceId)
	{
		if ((instanceId != tInstanceId))
		{
			return false;
		}
		return canSeeTarget(x, y, z, instanceId, tx, ty, tz);
	}
	
	/**
	 * Can see target. Checks doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param instanceId
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int x, int y, int z, int instanceId, int tx, int ty, int tz)
	{
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz, instanceId, true))
		{
			return false;
		}
		return canSeeTarget(x, y, z, tx, ty, tz);
	}
	
	/**
	 * Can see target. Does not check doors between.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @return {@code true} if there is line of sight between the given coordinate sets, {@code false} otherwise
	 */
	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		
		z = getNearestZ(geoX, geoY, z);
		tz = getNearestZ(tGeoX, tGeoY, tz);
		
		if ((geoX == tGeoX) && (geoY == tGeoY))
		{
			if (hasGeoPos(tGeoX, tGeoY))
			{
				return z == tz;
			}
			
			return true;
		}
		
		double fullDist = Util.calculateDistance(geoX, geoY, 0, tGeoX, tGeoY, 0, false, false);
		
		if (tz > z)
		{
			int tmp = tx;
			tx = x;
			x = tmp;
			
			tmp = ty;
			ty = y;
			y = tmp;
			
			tmp = tz;
			tz = z;
			z = tmp;
			
			tmp = tGeoX;
			tGeoX = geoX;
			geoX = tmp;
			
			tmp = tGeoY;
			tGeoY = geoY;
			geoY = tmp;
		}
		
		int fullZDiff = tz - z;
		
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available, skip it, we can always see our own position
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevMoveNearestZ = z;
		int ptIndex = 0;
		
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			
			// check only when it's not the last point & the current position has geodata
			if (/* ((curX != tGeoX) || (curY != tGeoY)) && */hasGeoPos(curX, curY))
			{
				double percentageDist = Util.calculateDistance(geoX, geoY, 0, curX, curY, 0, false, false) / fullDist;
				int beeCurZ = (int) (z + (fullZDiff * percentageDist));
				int beeCurNearestZ = getNearestZ(curX, curY, beeCurZ);
				int moveCurNearestZ;
				Direction prevDir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
				if (canEnterNeighbors(prevX, prevY, prevMoveNearestZ, prevDir))
				{
					switch (prevDir)
					{
						case NORTH_EAST:
							if (canEnterNeighbors(prevX, prevY - 1, prevMoveNearestZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevMoveNearestZ, Direction.NORTH))
							{
								moveCurNearestZ = getNearestZ(curX, curY, prevMoveNearestZ);
							}
							else
							{
								moveCurNearestZ = getNextHigherZ(curX, curY, prevMoveNearestZ);
							}
							break;
						case NORTH_WEST:
							if (canEnterNeighbors(prevX, prevY - 1, prevMoveNearestZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevMoveNearestZ, Direction.NORTH))
							{
								moveCurNearestZ = getNearestZ(curX, curY, prevMoveNearestZ);
							}
							else
							{
								moveCurNearestZ = getNextHigherZ(curX, curY, prevMoveNearestZ);
							}
							break;
						case SOUTH_EAST:
							if (canEnterNeighbors(prevX, prevY + 1, prevMoveNearestZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevMoveNearestZ, Direction.SOUTH))
							{
								moveCurNearestZ = getNearestZ(curX, curY, prevMoveNearestZ);
							}
							else
							{
								moveCurNearestZ = getNextHigherZ(curX, curY, prevMoveNearestZ);
							}
							break;
						case SOUTH_WEST:
							if (canEnterNeighbors(prevX, prevY + 1, prevMoveNearestZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevMoveNearestZ, Direction.SOUTH))
							{
								moveCurNearestZ = getNearestZ(curX, curY, prevMoveNearestZ);
							}
							else
							{
								moveCurNearestZ = getNextHigherZ(curX, curY, prevMoveNearestZ);
							}
							break;
						default:
							moveCurNearestZ = getNearestZ(curX, curY, prevMoveNearestZ);
							break;
					}
				}
				else
				{
					moveCurNearestZ = getNextHigherZ(curX, curY, prevMoveNearestZ);
				}
				
				int maxHeight;
				if ((ptIndex < _ELEVATED_SEE_OVER_DISTANCE) && (fullDist >= _ELEVATED_SEE_OVER_DISTANCE))
				{
					maxHeight = z + _MAX_SEE_OVER_HEIGHT;
					++ptIndex;
				}
				else
				{
					maxHeight = beeCurZ + _MAX_SEE_OVER_HEIGHT;
				}
				
				boolean canSeeThrough = false;
				if ((beeCurNearestZ <= maxHeight) && (moveCurNearestZ <= beeCurNearestZ))
				{
					Direction dir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
					
					// check diagonal step
					switch (dir)
					{
						case NORTH_EAST:
							if (canEnterNeighbors(prevX, prevY - 1, prevMoveNearestZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevMoveNearestZ, Direction.NORTH))
							{
								canSeeThrough = (getNearestZ(prevX, prevY - 1, beeCurZ) <= maxHeight) || (getNearestZ(prevX + 1, prevY, beeCurZ) <= maxHeight);
							}
							else
							{
								canSeeThrough = (getNextHigherZ(prevX, prevY - 1, beeCurZ) <= maxHeight) || (getNextHigherZ(prevX + 1, prevY, beeCurZ) <= maxHeight);
							}
							break;
						case NORTH_WEST:
							if (canEnterNeighbors(prevX, prevY - 1, prevMoveNearestZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevMoveNearestZ, Direction.NORTH))
							{
								canSeeThrough = (getNearestZ(prevX, prevY - 1, beeCurZ) <= maxHeight) || (getNearestZ(prevX - 1, prevY, beeCurZ) <= maxHeight);
							}
							else
							{
								canSeeThrough = (getNextHigherZ(prevX, prevY - 1, beeCurZ) <= maxHeight) || (getNextHigherZ(prevX - 1, prevY, beeCurZ) <= maxHeight);
							}
							break;
						case SOUTH_EAST:
							if (canEnterNeighbors(prevX, prevY + 1, prevMoveNearestZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevMoveNearestZ, Direction.SOUTH))
							{
								canSeeThrough = (getNearestZ(prevX, prevY + 1, beeCurZ) <= maxHeight) || (getNearestZ(prevX + 1, prevY, beeCurZ) <= maxHeight);
							}
							else
							{
								canSeeThrough = (getNextHigherZ(prevX, prevY + 1, beeCurZ) <= maxHeight) || (getNextHigherZ(prevX + 1, prevY, beeCurZ) <= maxHeight);
							}
							break;
						case SOUTH_WEST:
							if (canEnterNeighbors(prevX, prevY + 1, prevMoveNearestZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevMoveNearestZ, Direction.SOUTH))
							{
								canSeeThrough = (getNearestZ(prevX, prevY + 1, beeCurZ) <= maxHeight) || (getNearestZ(prevX - 1, prevY, beeCurZ) <= maxHeight);
							}
							else
							{
								canSeeThrough = (getNextHigherZ(prevX, prevY + 1, beeCurZ) <= maxHeight) || (getNextHigherZ(prevX - 1, prevY, beeCurZ) <= maxHeight);
							}
							break;
						default:
							canSeeThrough = true;
							break;
					}
				}
				
				if (!canSeeThrough)
				{
					return false;
				}
				
				prevMoveNearestZ = moveCurNearestZ;
			}
			
			prevX = curX;
			prevY = curY;
		}
		
		return true;
	}
	
	/**
	 * Move check.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param tx the target's x coordinate
	 * @param ty the target's y coordinate
	 * @param tz the target's z coordinate
	 * @param instanceId the instance id
	 * @return the last Location (x,y,z) where player can walk - just before wall
	 */
	public Location moveCheck(int x, int y, int z, int tx, int ty, int tz, int instanceId)
	{
		int geoX = getGeoX(x);
		int geoY = getGeoY(y);
		z = getNearestZ(geoX, geoY, z);
		int tGeoX = getGeoX(tx);
		int tGeoY = getGeoY(ty);
		tz = getNearestZ(tGeoX, tGeoY, tz);
		
		if (DoorData.getInstance().checkIfDoorsBetween(x, y, z, tx, ty, tz, instanceId, false))
		{
			return new Location(x, y, getHeight(x, y, z));
		}
		
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevZ = z;
		
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			int curZ = getNearestZ(curX, curY, prevZ);
			
			if (hasGeoPos(prevX, prevY))
			{
				Direction dir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
				boolean canEnter = false;
				if (canEnterNeighbors(prevX, prevY, prevZ, dir))
				{
					// check diagonal movement
					switch (dir)
					{
						case NORTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.NORTH);
							break;
						case NORTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.NORTH);
							break;
						case SOUTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.SOUTH);
							break;
						case SOUTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.SOUTH);
							break;
						default:
							canEnter = true;
							break;
					}
				}
				
				if (!canEnter)
				{
					// can't move, return previous location
					return new Location(getWorldX(prevX), getWorldY(prevY), prevZ);
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != tz))
		{
			// different floors, return start location
			return new Location(x, y, z);
		}
		
		return new Location(tx, ty, tz);
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param fromX the X coordinate to start checking from
	 * @param fromY the Y coordinate to start checking from
	 * @param fromZ the Z coordinate to start checking from
	 * @param toX the X coordinate to end checking at
	 * @param toY the Y coordinate to end checking at
	 * @param toZ the Z coordinate to end checking at
	 * @param instanceId the instance ID
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, int instanceId)
	{
		int geoX = getGeoX(fromX);
		int geoY = getGeoY(fromY);
		fromZ = getNearestZ(geoX, geoY, fromZ);
		int tGeoX = getGeoX(toX);
		int tGeoY = getGeoY(toY);
		toZ = getNearestZ(tGeoX, tGeoY, toZ);
		
		if (DoorData.getInstance().checkIfDoorsBetween(fromX, fromY, fromZ, toX, toY, toZ, instanceId, false))
		{
			return false;
		}
		
		LinePointIterator pointIter = new LinePointIterator(geoX, geoY, tGeoX, tGeoY);
		// first point is guaranteed to be available
		pointIter.next();
		int prevX = pointIter.x();
		int prevY = pointIter.y();
		int prevZ = fromZ;
		
		while (pointIter.next())
		{
			int curX = pointIter.x();
			int curY = pointIter.y();
			int curZ = getNearestZ(curX, curY, prevZ);
			
			if (hasGeoPos(prevX, prevY))
			{
				Direction dir = GeoUtils.computeDirection(prevX, prevY, curX, curY);
				boolean canEnter = false;
				if (canEnterNeighbors(prevX, prevY, prevZ, dir))
				{
					// check diagonal movement
					switch (dir)
					{
						case NORTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.NORTH);
							break;
						case NORTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY - 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.NORTH);
							break;
						case SOUTH_EAST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.EAST) && canEnterNeighbors(prevX + 1, prevY, prevZ, Direction.SOUTH);
							break;
						case SOUTH_WEST:
							canEnter = canEnterNeighbors(prevX, prevY + 1, prevZ, Direction.WEST) && canEnterNeighbors(prevX - 1, prevY, prevZ, Direction.SOUTH);
							break;
						default:
							canEnter = true;
							break;
					}
				}
				
				if (!canEnter)
				{
					return false;
				}
			}
			
			prevX = curX;
			prevY = curY;
			prevZ = curZ;
		}
		
		if (hasGeoPos(prevX, prevY) && (prevZ != toZ))
		{
			// different floors
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param from the {@code ILocational} to start checking from
	 * @param toX the X coordinate to end checking at
	 * @param toY the Y coordinate to end checking at
	 * @param toZ the Z coordinate to end checking at
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(ILocational from, int toX, int toY, int toZ)
	{
		return canMove(from.getX(), from.getY(), from.getZ(), toX, toY, toZ, from.getInstanceId());
	}
	
	/**
	 * Checks if its possible to move from one location to another.
	 * @param from the {@code ILocational} to start checking from
	 * @param to the {@code ILocational} to end checking at
	 * @return {@code true} if the character at start coordinates can move to end coordinates, {@code false} otherwise
	 */
	public boolean canMove(ILocational from, ILocational to)
	{
		return canMove(from, to.getX(), to.getY(), to.getZ());
	}
	
	/**
	 * Checks the specified position for available geodata.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return {@code true} if there is geodata for the given coordinates, {@code false} otherwise
	 */
	public boolean hasGeo(int x, int y)
	{
		return hasGeoPos(getGeoX(x), getGeoY(y));
	}
}
