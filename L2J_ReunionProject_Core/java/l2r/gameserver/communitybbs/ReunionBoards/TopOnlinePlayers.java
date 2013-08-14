package l2r.gameserver.communitybbs.ReunionBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.text.TextBuilder;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.ClanTable;
import l2r.gameserver.model.L2Clan;
import gr.reunion.configs.SmartCommunityConfigs;

public class TopOnlinePlayers
{
	private final TextBuilder _topOnline = new TextBuilder();
	private int _counter = 0;
	
	public TopOnlinePlayers(String file)
	{
		loadDB(file);
	}
	
	private void loadDB(String file)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT char_name,clanid,onlinetime FROM characters where accesslevel = 0 order by pvpkills DESC LIMIT " + SmartCommunityConfigs.TOP_PLAYER_RESULTS + ";");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String clanName = "No Clan";
				int clanid = rset.getInt("clanid");
				String name = rset.getString("char_name");
				int onTime = rset.getInt("onlinetime");
				if (clanid != 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(clanid);
					
					// Just in case checking for null pointer
					if (clan != null)
					{
						clanName = clan.getName();
					}
				}
				setCounter(getCounter() + 1);
				
				addChar(name, clanName, getPlayerRunTime(onTime));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public String loadTopList()
	{
		return _topOnline.toString();
	}
	
	private void addChar(String name, String cname, String onTime)
	{
		_topOnline.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=111111 width=750>");
		_topOnline.append("<tr>");
		_topOnline.append("<td FIXWIDTH=40>" + getCounter() + "</td");
		_topOnline.append("<td fixwidth=160>" + name + "</td");
		_topOnline.append("<td fixwidth=160>" + cname + "</td>");
		_topOnline.append("<td fixwidth=80>" + onTime + "</td>");
		_topOnline.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"735\" height=\"1\">");
	}
	
	public String getPlayerRunTime(int secs)
	{
		String timeResult = "";
		if (secs >= 86400)
		{
			timeResult = Integer.toString(secs / 86400) + " Days " + Integer.toString((secs % 86400) / 3600) + " hours";
		}
		else
		{
			timeResult = Integer.toString(secs / 3600) + " Hours " + Integer.toString((secs % 3600) / 60) + " mins";
		}
		return timeResult;
	}
	
	public int getCounter()
	{
		return _counter;
	}
	
	public void setCounter(int counter)
	{
		_counter = counter;
	}
}