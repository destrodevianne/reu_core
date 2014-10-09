package gr.reunion.antibotEngine.dynamicHtmls;

import javolution.text.TextBuilder;
import l2r.Config;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.util.Rnd;
import gr.reunion.configsEngine.AntibotConfigs;
import gr.reunion.imageGeneratorEngine.GenerateLogos;
import gr.reunion.imageGeneratorEngine.LogoType;

/**
 * @author -=GodFather=-
 */
public class GenerateHtmls
{
	private static int _extraImgId;
	private static int _refreshImgId;
	
	public GenerateHtmls(L2PcInstance player)
	{
		// Dummy default
	}
	
	public static void captchaHtml(L2PcInstance activeChar, String botType)
	{
		TextBuilder tb = new TextBuilder();
		NpcHtmlMessage antibotReply = new NpcHtmlMessage(0);
		
		generateAntibotImages(activeChar);
		// Random image file name
		int imgId = IdFactory.getInstance().getNextId();
		// Conversion from .png to .dds, and crest packed send
		GenerateLogos.getInstance().captchaLogo(activeChar, imgId);
		
		tb.append("<html><title>Captcha Antibot System</title><body><center>Enter the 5-digits code below and click Confirm.<br>");
		tb.append("<img src=\"Crest.crest_" + Config.SERVER_ID + "_" + imgId + "\" width=256 height=64><br>");
		tb.append("<table width=254><tr>");
		tb.append("<td width=200 align=left><font color=\"888888\">To refresh the image click this button:</font></td><td width=20 align=left");
		
		switch (botType)
		{
			case "FARM":
				tb.append("<button action=\"bypass -h voice .farmcaptcha\" width=16 height=16 back=\"Crest.crest_" + Config.SERVER_ID + "_" + _refreshImgId + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + _refreshImgId + "\">");
				tb.append("</td></tr></table><br1>");
				tb.append("<font color=\"888888\">(There are only english uppercase letters.)</font><br1>");
				if (AntibotConfigs.ENABLE_DOUBLE_PROTECTION)
				{
					tb.append("<table width=254><tr>");
					tb.append("<td width=160 align=left><font color=\"849D68\">Select the correct answer:</font></td>");
					tb.append("<td align=center><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + _extraImgId + "\" width=32 height=16></td>");
					tb.append("<td align=right><combobox width=62 var=type list=Maybe;Yes;No></td>");
					tb.append("</tr></table><br1>");
				}
				tb.append("<font color=\"FF0000\">Tries Left: " + activeChar.getTries() + "</font><br>");
				tb.append("<edit var=\"code\" width=110><br>");
				tb.append("<button value=\"Confirm\" action=\"bypass -h voice .antibot $code $type\" width=80 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"><br>");
				tb.append("<table bgcolor=2E2E2E>");
				tb.append("<tr>");
				tb.append("<td width=270><font color=898989>If you close by mistake this window, you can open it again by typing </font>");
				tb.append("<font color=849D68>\".captcha\"</font><font color=898989> in all chat.<br1>");
				tb.append("You have " + AntibotConfigs.JAIL_TIMER + " second(s) to answer and 3 tries,<br1>");
				tb.append("if time passes or your answer is wrong to all tries, punishment will be jail.</font></td>");
				activeChar.setFarmBotCode(GenerateLogos.getInstance().getFinalString());
				break;
			case "ENCHANT":
				tb.append("<button action=\"bypass -h voice .enchantcaptcha\" width=16 height=16 back=\"Crest.crest_" + Config.SERVER_ID + "_" + _refreshImgId + "\" fore=\"Crest.crest_" + Config.SERVER_ID + "_" + _refreshImgId + "\">");
				tb.append("</td></tr></table><br1>");
				tb.append("<font color=\"888888\">(There are only english uppercase letters.)<br1>");
				if (AntibotConfigs.ENABLE_DOUBLE_PROTECTION)
				{
					tb.append("<table width=254><tr>");
					tb.append("<td width=160 align=left><font color=\"849D68\">Select the correct answer:</font></td>");
					tb.append("<td align=center><img src=\"Crest.crest_" + Config.SERVER_ID + "_" + _extraImgId + "\" width=32 height=16></td>");
					tb.append("<td align=right><combobox width=62 var=type list=Maybe;Yes;No></td>");
					tb.append("</tr></table><br>");
				}
				tb.append("<edit var=\"code\" width=110><br>");
				tb.append("<button value=\"Confirm\" action=\"bypass -h voice .enchantbot $code $type\" width=80 height=26 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.button_df\"><br>");
				tb.append("<table bgcolor=2E2E2E>");
				tb.append("<tr>");
				tb.append("<td width=270><font color=898989>If you close by mistake this window, it will open again in few seconds.</font></td>");
				activeChar.setEnchantBotCode(GenerateLogos.getInstance().getFinalString());
				break;
			default:
				break;
		}
		tb.append("</tr></table>");
		tb.append("</center></body></html>");
		
		antibotReply.setHtml(tb.toString());
		activeChar.sendPacket(antibotReply);
		GenerateLogos.getInstance().getFinalString().replace(0, 5, "");
	}
	
	private static void generateAntibotImages(L2PcInstance activeChar)
	{
		int r = Rnd.get(1, 3);
		
		switch (r)
		{
			case 1:
				activeChar.setBotAnswer("Maybe");
				_extraImgId = Integer.valueOf(LogoType.MAYBE.getText());
				break;
			case 2:
				activeChar.setBotAnswer("Yes");
				_extraImgId = Integer.valueOf(LogoType.YES.getText());
				break;
			case 3:
				activeChar.setBotAnswer("No");
				_extraImgId = Integer.valueOf(LogoType.NO.getText());
				break;
			default:
				break;
		}
		
		_refreshImgId = Integer.valueOf(LogoType.REFRESH.getText());
	}
}