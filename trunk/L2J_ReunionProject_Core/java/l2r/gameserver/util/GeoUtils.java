package l2r.gameserver.util;

import com.l2jserver.gameserver.geoengine.Direction;

public final class GeoUtils
{
	public interface PointListener
	{
		/**
		 * @param x
		 * @param y
		 * @return true proceed, false abort
		 */
		boolean onPoint(int x, int y);
	}
	
	public static void main(String[] args)
	{
		forEachLinePoint(1, 1, 5, 20, new PointListener()
		{
			
			@Override
			public boolean onPoint(int x, int y)
			{
				System.out.println("x: " + x + ", y: " + y);
				return true;
			}
		});
	}
	
	public static boolean forEachLinePoint(int srcX, int srcY, int dstX, int dstY, PointListener listener)
	{
		int dx = Math.abs(dstX - srcX), sx = srcX < dstX ? 1 : -1;
		int dy = -Math.abs(dstY - srcY), sy = srcY < dstY ? 1 : -1;
		int err = dx + dy, e2;
		
		for (;;)
		{
			if (!listener.onPoint(srcX, srcY))
			{
				return false;
			}
			
			if ((srcX == dstX) && (srcY == dstY))
			{
				break;
			}
			
			e2 = 2 * err;
			if (e2 > dy)
			{
				err += dy;
				srcX += sx;
			}
			
			if (e2 < dx)
			{
				err += dx;
				srcY += sy;
			}
		}
		
		return true;
	}
	
	/**
	 * difference between x values: never abover 1<br>
	 * difference between y values: never above 1
	 * @param lastX
	 * @param lastY
	 * @param x
	 * @param y
	 * @return
	 */
	public static Direction computeDirection(int lastX, int lastY, int x, int y)
	{
		if (x > lastX) // east
		{
			if (y > lastY)
			{
				return Direction.SOUTH_EAST;
			}
			else if (y < lastY)
			{
				return Direction.NORTH_EAST;
			}
			else
			{
				return Direction.EAST;
			}
		}
		else if (x < lastX) // west
		{
			if (y > lastY)
			{
				return Direction.SOUTH_WEST;
			}
			else if (y < lastY)
			{
				return Direction.NORTH_WEST;
			}
			else
			{
				return Direction.WEST;
			}
		}
		else
		// unchanged x
		{
			if (y > lastY)
			{
				return Direction.SOUTH;
			}
			else if (y < lastY)
			{
				return Direction.NORTH;
			}
			else
			{
				return null;// error, should never happen, TODO: Logging
			}
		}
	}
	
	private GeoUtils()
	{
	}
}
