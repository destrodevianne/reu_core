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
package l2r.tools.dbinstaller.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import l2r.tools.dbinstaller.DBOutputInterface;
import l2r.tools.images.ImagesTable;

/**
 * @author mrTJO
 */
public class DBInstallerGUI extends JFrame implements DBOutputInterface
{
	private static final long serialVersionUID = -1005504757826370170L;
	
	private final JProgressBar _progBar;
	private final JTextArea _progArea;
	private final Connection _con;
	
	public DBInstallerGUI(Connection con)
	{
		super("L2J Database Installer");
		setLayout(new BorderLayout());
		setDefaultLookAndFeelDecorated(true);
		setIconImage(ImagesTable.getImage("l2j.png").getImage());
		
		_con = con;
		
		int width = 480;
		int height = 360;
		Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds((resolution.width - width) / 2, (resolution.height - height) / 2, width, height);
		setResizable(false);
		
		_progBar = new JProgressBar();
		_progBar.setIndeterminate(true);
		add(_progBar, BorderLayout.PAGE_START);
		
		_progArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(_progArea);
		
		_progArea.setEditable(false);
		appendToProgressArea("Connected");
		
		add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void setProgressIndeterminate(boolean value)
	{
		_progBar.setIndeterminate(value);
	}
	
	@Override
	public void setProgressMaximum(int maxValue)
	{
		_progBar.setMaximum(maxValue);
	}
	
	@Override
	public void setProgressValue(int value)
	{
		_progBar.setValue(value);
	}
	
	@Override
	public void appendToProgressArea(String text)
	{
		_progArea.append(text + System.getProperty("line.separator"));
		_progArea.setCaretPosition(_progArea.getDocument().getLength());
	}
	
	@Override
	public Connection getConnection()
	{
		return _con;
	}
	
	@Override
	public void setFrameVisible(boolean value)
	{
		setVisible(value);
	}
	
	@Override
	public int requestConfirm(String title, String message, int type)
	{
		return JOptionPane.showConfirmDialog(null, message, title, type);
	}
	
	@Override
	public void showMessage(String title, String message, int type)
	{
		JOptionPane.showMessageDialog(null, message, title, type);
	}
}
