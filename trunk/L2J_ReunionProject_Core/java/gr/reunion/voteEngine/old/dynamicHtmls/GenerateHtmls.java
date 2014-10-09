package gr.reunion.voteEngine.old.dynamicHtmls;

import javolution.text.TextBuilder;
import l2r.Config;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import gr.reunion.configsEngine.IndividualVoteSystemConfigs;
import gr.reunion.imageGeneratorEngine.LogoType;
import gr.reunion.voteEngine.old.VoteHandler;

/**
 * @author -=GodFather=-
 */
public class GenerateHtmls
{
	private GenerateHtmls()
	{
		// Dummy default
	}
	
	private static GenerateHtmls _instance = null;
	private final String serverName = "L][Reunion Team";
	
	public static GenerateHtmls getInstance()
	{
		if (_instance == null)
		{
			_instance = new GenerateHtmls();
		}
		
		return _instance;
	}
	
	public void VoteRewardHtml(L2PcInstance activeChar)
	{
		if (activeChar.isHopZoneDone() && activeChar.isTopZoneDone())
		{
			TextBuilder tb = new TextBuilder();
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			
			tb.append("<html><head><title>Vote Reward Panel</title></head><body>");
			tb.append("<center>");
			tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
			tb.append("<tr>");
			tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><img src=\"L2ui_ct1.Icon_DF_MenuWnd_SystemMenu\" width=\"38\" height=\"38\"></td>");
			tb.append("<td valign=\"top\"><font color=\"FF6600\">Vote Panel</font>");
			tb.append("<br1><font color=\"00FF00\">" + activeChar.getName() + "</font>, get your reward here.</td>");
			tb.append("</tr>");
			tb.append("</table>");
			tb.append("<td valign=\"top\"><font color=\"FF6600\">Choose your reward " + activeChar.getName() + ".</font>");
			tb.append("<button value=\"Item: " + ItemData.getInstance().getTemplate(IndividualVoteSystemConfigs.VOTE_REWARD_ID1).getName() + "   Amount:" + IndividualVoteSystemConfigs.VOTE_REWARD_AMOUNT1 + "\" action=\"bypass -h Vote_votereward1\" width=204 height=20>");
			tb.append("<button value=\"Item: " + ItemData.getInstance().getTemplate(IndividualVoteSystemConfigs.VOTE_REWARD_ID2).getName() + "   Amount:" + IndividualVoteSystemConfigs.VOTE_REWARD_AMOUNT2 + "\" action=\"bypass -h Vote_votereward2\" width=204 height=20>");
			tb.append("<button value=\"Item: " + ItemData.getInstance().getTemplate(IndividualVoteSystemConfigs.VOTE_REWARD_ID3).getName() + "   Amount:" + IndividualVoteSystemConfigs.VOTE_REWARD_AMOUNT3 + "\" action=\"bypass -h Vote_votereward3\" width=204 height=20>");
			tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32 align=center>");
			tb.append("<font color=\"FF6600\">Voting on both sites Done.</font><br>");
			tb.append("<font color=\"FF6600\">Hopzone Status: </font><font color=\"00FF00\">Done</font><br>");
			tb.append("<font color=\"FF6600\">Topzone Status: </font><font color=\"00FF00\">Done</font><br>");
			tb.append("<font color=\"3293F3\">" + serverName + "</font><br>");
			tb.append("<img src=\"L2UI.SquareWhite\" width=250 height=1 align=center>");
			tb.append("</center>");
			tb.append("</body></html>");
			
			html.setHtml(tb.toString());
			activeChar.sendPacket(html);
			
			activeChar.sendMessage("Choose your vote reward! Thanks for voting for us!");
			activeChar.sendMessage("In case you close reward window by accident type again .votepanel!");
		}
		else
		{
			activeChar.sendMessage("Something went wrong, report this error to GMs.");
		}
	}
	
	public void VotePanelHtml(L2PcInstance activeChar)
	{
		if (!activeChar.isHopZoneDone() || !activeChar.isTopZoneDone())
		{
			TextBuilder tb = new TextBuilder();
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			
			tb.append("<html><head><title>Vote System Panel</title></head><body>");
			tb.append("<center><br>");
			tb.append("<table width=\"280\" bgcolor=\"000000\">");
			tb.append("<tr>");
			tb.append("<td align=\"center\"><font color=\"00FF99\">Who's voting now: </font>" + VoteHandler.getWhoIsVoting());
			if (IndividualVoteSystemConfigs.ENABLE_TRIES)
			{
				tb.append(" <font color=\"00ffff\">Tries left: </font>" + activeChar.getVoteTries() + "<br1>");
			}
			else
			{
				tb.append(" <font color=\"00ffff\">Tries left: </font><font color=\"00FF00\">Unlimited</font><br1>");
			}
			tb.append("<font color=\"FF6600\">You can vote in Hopzone at " + VoteHandler.hopCd(activeChar) + "</font><br1>");
			tb.append("<font color=\"FF6600\">You can vote in Topzone at " + VoteHandler.topCd(activeChar) + "</font></td>");
			tb.append("</tr>");
			tb.append("</table>");
			tb.append("<img src=\"L2UI.SquareWhite\" width=280 height=1 align=center><br><br>");
			tb.append("<img src=\"L2UI.SquareWhite\" width=264 height=1 align=center>");
			tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
			tb.append("<tr>");
			tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h Vote_votehopzone\" width=256 height=64 back=\"Crest.crest_" + Config.SERVER_ID + "_" + Integer.valueOf(LogoType.HOPZONE.getText()) + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + Integer.valueOf(LogoType.HOPZONE.getText()) + "\"></td>");
			tb.append("</tr>");
			tb.append("</table>");
			tb.append("<table width=\"250\" cellpadding=\"5\" bgcolor=\"000000\">");
			tb.append("<tr>");
			tb.append("<td width=\"45\" valign=\"top\" align=\"center\"><button action=\"bypass -h Vote_votetopzone\" width=256 height=64 back=\"Crest.crest_" + Config.SERVER_ID + "_" + Integer.valueOf(LogoType.TOPZONE.getText()) + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + Integer.valueOf(LogoType.TOPZONE.getText()) + "\"></td>");
			tb.append("</tr>");
			tb.append("</table>");
			tb.append("<img src=\"L2UI.SquareWhite\" width=264 height=1 align=center><br>");
			tb.append("<table width=\"280\" bgcolor=\"000000\">");
			tb.append("<tr>");
			tb.append("<td align=\"center\"><font color=\"FF6600\">You must vote on both banners for reward!</font></td>");
			tb.append("</tr><tr>");
			if (activeChar.isHopZoneDone())
			{
				tb.append("<td align=\"center\"><font color=\"FF6600\">Hopzone Status: </font><font color=\"00FF00\">Done...</font><br1>");
			}
			else
			{
				tb.append("<td align=\"center\"><font color=\"FF6600\">Hopzone Status: </font><font color=\"FF0000\">Pending...</font><br1>");
			}
			
			if (activeChar.isTopZoneDone())
			{
				tb.append("<font color=\"FF6600\">Topzone Status: </font><font color=\"00FF00\">Done...</font></td>");
			}
			else
			{
				tb.append("<font color=\"FF6600\">Topzone Status: </font><font color=\"FF0000\">Pending...</font></td>");
			}
			tb.append("</tr>");
			tb.append("</table>");
			tb.append("<br><font color=\"3293F3\">" + serverName + "</font><br>");
			tb.append("<img src=\"L2UI.SquareWhite\" width=250 height=1 align=center>");
			tb.append("</center>");
			tb.append("</body></html>");
			
			html.setHtml(tb.toString());
			activeChar.sendPacket(html);
		}
		// Just in case they close reward window by accident
		else
		{
			VoteRewardHtml(activeChar);
		}
	}
}