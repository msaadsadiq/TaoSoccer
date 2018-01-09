/*
  Copyright (C) 2003 by Krzysztof Langner

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the 
   Free Software Foundation, Inc., 
   59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package soccer.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import soccer.common.RefereeData;
import soccer.server.SoccerServer;
import soccer.server.sim.World;
import soccer.server.sim.WorldListener;



/**
 * This is monitor used to test players.
 * It starts frame and server in the same VM.
 * It doesn't add any player to the teams and have additional
 * functionality to change the process fo the game (eg can change game state).
 * 
 * @author Krzysztof Langner
 */
public class SoccerMonitor extends JFrame implements WorldListener
{

  //----------------------------------------------------------------------------
  /**
   * Main
   */
  public static void main(String[] args) {
    Properties    properties = null;
    SoccerMonitor app = new SoccerMonitor();
    String        configFileName = "./properties";

    try {
      File file = new File(configFileName);
      if(file.exists()){
        
        System.out.println("Load properties from file: " + configFileName);
        properties = new Properties();
				properties.load(new FileInputStream(configFileName));
      }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    app.init(properties);
    app.show();
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Update view after model changed
   */
  public void modelChanged(){
    
    updateStatus();
    repaint();
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @see soccer.server.WorldListener#refereeSignal()
   */
  public void refereeSignal() {
    updateStatus();
  }
  
  //----------------------------------------------------------------------------
  /**
   * Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      exitAction.actionPerformed(null);
    }
  }


  //---------------------------------------------------------------------------
  /**
   * Init frame
   * @param properties Properties which should be used to initialize server
   */
  private void init(Properties properties){

    server = new SoccerServer();
    if(properties != null)
      server.setProperties(properties);
    
    getContentPane().setLayout(new BorderLayout());
    initView();
    setSize(800, 650);
    centerFrame();
    initActions();
    setJMenuBar(createMenu());
    getContentPane().add(createToolbar(), BorderLayout.NORTH);

    startServer();
  }
  
  
  //---------------------------------------------------------------------------
  private void initView(){
    
    World       world = server.getWorld();
    JPanel      mainPanel;
    JPanel      statusPanel;
    JPanel      panel;
    Dimension   d = new Dimension(200, 25);
    Dimension   dgs = new Dimension(300, 25);
  
    mainPanel = new JPanel(new BorderLayout());
    getContentPane().add(mainPanel, BorderLayout.CENTER);

    // Create status Pane
    statusPanel = new JPanel();
    statusPanel.setLayout(new FlowLayout());
    statusPanel.setBackground(Color.gray);
    statusPanel.setBorder(BorderFactory.createRaisedBevelBorder());
    statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    mainPanel.add(statusPanel, BorderLayout.NORTH);

    // left team status
    leftStatus = new JLabel();
    panel = new JPanel();
    panel.setPreferredSize(d);
    panel.setBackground(Color.yellow);
    panel.setBorder(BorderFactory.createLoweredBevelBorder());
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(leftStatus);
    statusPanel.add(panel);

    // general game status
    panel = new JPanel();
    panel.setPreferredSize(dgs);
    panel.setBackground(Color.orange);
    panel.setBorder(BorderFactory.createLoweredBevelBorder());
    panel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(world.getGamePeriod().getView());
    gameStatus = new JLabel();
    panel.add(gameStatus);
    statusPanel.add(panel);

    // right team status
    rightStatus = new JLabel();
    panel = new JPanel();
    panel.setPreferredSize(d);
    panel.setBackground(Color.red);
    panel.setBorder(BorderFactory.createLoweredBevelBorder());
    panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    panel.add(rightStatus);
    statusPanel.add(panel);
  
    
    fieldView = new FieldPanel(server);
    mainPanel.add(fieldView, BorderLayout.CENTER);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Center frame on the screen
   */ 
  private void centerFrame(){
    
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    
    setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Start soccer server. 
   * The server is started in the same virtual machine
   */
  private void startServer(){
    
    server.init(7777);
    server.getWorld().addListener(this);

  }


  //---------------------------------------------------------------------------
  /**
   * Init actions
   */
  private void initActions(){

    exitAction = new ExitAction();
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Create toolbar
   */
  private JToolBar createToolbar(){
    
    JToolBar toolbar = new JToolBar();
    
//    toolbar.add(openAction);
    
    return toolbar;
  }

  
  //---------------------------------------------------------------------------
  /**
   * Create menu
   */
  private JMenuBar createMenu(){
    
    JMenuBar  menuBar = new JMenuBar();
    JMenu     fileMenu;
    JMenu     simMenu;
    
    
    fileMenu = new JMenu("Game");
//    fileMenu.add(openAction);
    fileMenu.addSeparator();
    fileMenu.add(exitAction);
    menuBar.add(fileMenu);
    
    return menuBar;
  }
  

  //---------------------------------------------------------------------------
  /**
   * Update status
   */
  private void updateStatus(){
    
    World   world = server.getWorld();
    String  status = new String();
    
    leftStatus.setText(world.getTeamName(true) + " : " + world.getScore(true));
    rightStatus.setText(world.getTeamName(false) + " : " + world.getScore(false));
    
    
    status += " : ";
    
    switch(world.mode){
      case RefereeData.BEFORE_KICK_OFF:
        status += "Before kick off";
        break;
        
      case RefereeData.KICK_OFF_L:
      status += "Kick off l";
        break;
        
      case RefereeData.KICK_OFF_R:
      status += "Kick off r";
        break;
        
      case RefereeData.THROW_IN_L:
      status += "Throw in l";
        break;

      case RefereeData.THROW_IN_R:
        status += "Throw in r";
        break;
        
      case RefereeData.CORNER_KICK_L:
        status += "corner l";
        break;
        
      case RefereeData.CORNER_KICK_R:
        status += "corner r";
        break;
        
      case RefereeData.GOAL_KICK_L:
        status += "goal kick l";
        break;

      case RefereeData.GOAL_KICK_R:
        status += "goal kick r";
        break;
        
      case RefereeData.OFFSIDE_L:
        status += "offside l";
        break;
        
      case RefereeData.OFFSIDE_R:
        status += "offside r";
        break;
        
      case RefereeData.PLAY_ON:
        status += "play";
        break;
    }
    
    status += " " + world.getTickCount()/600 + ":" + (world.getTickCount()%600)/10;
    gameStatus.setText(status);
  }
  
  
  //---------------------------------------------------------------------------
  // Private members
  /** Server */
  private SoccerServer    server;
  /* Status panels */
  private JLabel          leftStatus;
  private JLabel          rightStatus;
  private JLabel          gameStatus;
  /* Actions */
  private ExitAction      exitAction;
  private FieldPanel      fieldView;
}
