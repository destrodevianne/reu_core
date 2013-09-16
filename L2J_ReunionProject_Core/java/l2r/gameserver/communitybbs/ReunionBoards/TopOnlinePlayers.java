package l2r.gameserver.communitybbs.ReunionBoards;

import javolution.text.TextBuilder;
import gr.reunion.aioItem.PlayersTopData;
import gr.reunion.configs.SmartCommunityConfigs;
import gr.reunion.datatables.CustomTable;

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
		for (PlayersTopData playerData : CustomTable.getInstance().getTopOnlineTime())
		{
			if (getCounter() <= SmartCommunityConfigs.TOP_PLAYER_RESULTS)
			{
				String name = playerData.getCharName();
				String cName = playerData.getClanName();
				int onlineTime = playerData.getOnlineTime();
				
				addChar(name, cName, getPlayerRunTime(onlineTime));
				setCounter(getCounter() + 1);
			}
		}
	}
	
	public String loadTopList()
	{
		return _topOnline.toString();
	}
	
	private void addChar(String name, String cname, String onTime)
	{
		_topOnline.append("<tr>");
		_topOnline.append("<td valign=\"top\" align=\"center\">" + getCounter() + "</td");
		_topOnline.append("<td valign=\"top\" align=\"center\">" + name + "</td");
		_topOnline.append("<td valign=\"top\" align=\"center\">" + cname + "</td>");
		_topOnline.append("<td valign=\"top\" align=\"center\">" + onTime + "</td>");
		_topOnline.append("</tr>");
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