/* SoccerMaster.java
   This class presents a gui to player,... it can be run as an application or an applet

   Copyright (C) 2001  Yu Zhang

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
/* This file modifed to fix bug #430844
							jdm, June 7 2001
*/
/* Modified to add functionality of the Arena class that can be instantiated
   with either the Field class or the Rink class.  The Rink class is used if
   the command line parameter "-hockey" is used.
						        jdm, June 7 2001
*/

/* Modified to add Java3D capabilities - the -3D option now starts it with
   a FieldJ3D for its Arena.
   In addition to calling FieldJ3D rather than just Field, this meant making 
   the menus 'heavyweight' so that the Java3D part is not rendered over them.
   
                                fcm, August 29 2001
*/                                


package soccer.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.applet.*;
import soccer.common.*;

public class SoccerMaster extends JApplet
{

  static boolean isApplet = true;

  SoccerMaster parent = null;

  // networking
  public static InetAddress address;
  public static String host = "localhost";
  public static int port = 7777;
  public static boolean playingSoccer = true, in3D = false;
  public Transceiver transceiver = null;

  // server and AI processes setup
  public static Runtime runtime = Runtime.getRuntime();
  public static Process serverP = null;
  public static int maxCommands = 21; // max commands can be started
  public static Vector activeCommands = new Vector(maxCommands);

  // server interface threads
  public Cplayer   player   = null; // soccer server data accepting and sending for playing
  public Cviewer   viewer   = null; // soccer server data accepting and sending for viewing
  public Replayer  replayer = null; // log file data translating for viewing

  public AudioClip applause;
  public AudioClip kick;
  public AudioClip referee1;
  public AudioClip referee2;

  public AudioClip preGame;
  public AudioClip firstHalf;
  public AudioClip halfTime;
  public AudioClip secondHalf;

  // log file
  public RandomAccessFile logFile = null;

  // game interface
  public Arena arena; // soccer field/hockey rink, players and ball/puck display

  Dimension d;

  JMenuBar mb;

  JToolBar toolBar;
  JMenu gameMenu;
  JMenu settingMenu;
  JMenu replayMenu;

  public JCheckBoxMenuItem music;
  public JCheckBoxMenuItem sound;

  public JCheckBoxMenuItem number;
  public JCheckBoxMenuItem chat;

  JPanel mainPane;

  JPanel statusPane;

  JPanel leftStatus;
  public JLabel leftName;
  public JLabel leftScore;

  JPanel gameStatus;
  public JLabel period;
  public JLabel mode;
  public JLabel time;

  JPanel rightStatus;
  public JLabel rightName;
  public JLabel rightScore;

  JPanel contentPane;

  Action setupServerAction;
  Action setupAIAction;

  public Action playAction;
  public Action viewAction;
  public Action stopAction;

  Action vOpenAction;
  Action vPlayAction;
  Action vBackAction;
  Action vStopAction;
  Action vPauseAction;
  Action vForwardAction;
  Action vRewindAction;

  Action exitAction;

  JLabel comm;
  public JTextField input;

  PlayDialog playDialog;
  ViewDialog viewDialog;
  ServerDialog serverDialog;
  AIDialog aiDialog;

  private void createActionComponents()
  {
    JButton button = null;
    JMenuItem menuItem = null;
    URL imgURL;

    // setup local server action
    imgURL = getClass().getResource("/imag/field.gif");
    setupServerAction = new AbstractAction("Set Up Local Server...", new ImageIcon(imgURL))
			{
			  public void actionPerformed(ActionEvent e)
			  {
                            if(serverDialog != null) serverDialog.setVisible(true);
			  }
		        };

    setupServerAction.setEnabled(false);

    button = toolBar.add(setupServerAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Set Up Local Server");
    menuItem = gameMenu.add(setupServerAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // setup local AI players action
    imgURL = getClass().getResource("/imag/robot.gif");
    setupAIAction = new AbstractAction("Set Up Local AI players...", new ImageIcon(imgURL))
		    {
		      public void actionPerformed(ActionEvent e)
		      {
			if(aiDialog != null) aiDialog.setVisible(true);
		      }
		    };

    setupAIAction.setEnabled(false);

    button = toolBar.add(setupAIAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Set Up Local AI players");
    menuItem = gameMenu.add(setupAIAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // action separator
    toolBar.addSeparator();
    gameMenu.addSeparator();

    // play game action
    imgURL = getClass().getResource("/imag/soccer.gif");
    playAction = new AbstractAction("Play Game...", new ImageIcon(imgURL))
                 {
                  public void actionPerformed(ActionEvent e)
		  {
                    if(playDialog != null) playDialog.setVisible(true);
		  }
		};

    playAction.setEnabled(true);

    button = toolBar.add(playAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Play Game");
    menuItem = gameMenu.add(playAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // view game action
    imgURL = getClass().getResource("/imag/view.gif");
    viewAction = new AbstractAction("View Game...", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    if(viewDialog != null) viewDialog.setVisible(true);
		  }
		};

    viewAction.setEnabled(true);

    button = toolBar.add(viewAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("View Game");
    menuItem = gameMenu.add(viewAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // stop playing/viewing game action
    imgURL = getClass().getResource("/imag/stop.gif");
    stopAction = new AbstractAction("Stop Playing/Viewing Game...", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    if(player != null)
		    {
		      player.end();
		      player = null;
		    }
		    if(viewer != null)
		    {
		      viewer.end();
		      viewer = null;
		    }
                    if(transceiver != null)
		    {
                      transceiver.disconnect();
                      transceiver = null;
		    }

		    preGame.stop();
                    firstHalf.stop();
                    secondHalf.stop();
                    halfTime.stop();

		    playAction.setEnabled(true);
		    viewAction.setEnabled(true);
		    vOpenAction.setEnabled(true);

                    if(arena != null)
		    {
		      arena.setWorld(null);
		      arena.repaint();
		    }
		  }

	        };

    stopAction.setEnabled(true);

    button = toolBar.add(stopAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Stop Playing/Viewing Game");
    menuItem = gameMenu.add(stopAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // action separator
    toolBar.addSeparator();
    gameMenu.addSeparator();

    // open the log file action
    imgURL = getClass().getResource("/imag/vopen.gif");
    vOpenAction = new AbstractAction("Open the log file...", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    JFileChooser chooser = new JFileChooser();
		    int option = chooser.showOpenDialog(parent);
		    if(option == JFileChooser.APPROVE_OPTION)
		      if(chooser.getSelectedFile()!=null)
		        try
		        {
		          logFile = new RandomAccessFile(chooser.getSelectedFile(),"r");
			  replayer = new Replayer(parent);
			  replayer.start();
                          playAction.setEnabled(false);
                          viewAction.setEnabled(false);
			  vOpenAction.setEnabled(false);
			  vStopAction.setEnabled(true);
		        }
			catch(Exception ee)
			{
                          JOptionPane.showMessageDialog(parent,
                                   "can not open the log file", "Error", JOptionPane.ERROR_MESSAGE);
			}
		  }

	        };

    vOpenAction.setEnabled(false);

    button = toolBar.add(vOpenAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Open the log file");
    menuItem = replayMenu.add(vOpenAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // rewind back action
    imgURL = getClass().getResource("/imag/vrewind.gif");
    vRewindAction = new AbstractAction("Rewind", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    replayer.setStatus(Replayer.REWIND);
		  }

	        };

    vRewindAction.setEnabled(true);

    button = toolBar.add(vRewindAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Rewind");
    menuItem = replayMenu.add(vRewindAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // play back action
    imgURL = getClass().getResource("/imag/vback.gif");
    vBackAction = new AbstractAction("Play back", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    replayer.setStatus(Replayer.BACK);
		  }

	        };

    vBackAction.setEnabled(true);

    button = toolBar.add(vBackAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Play back");
    menuItem = replayMenu.add(vBackAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // pause action
    imgURL = getClass().getResource("/imag/vpause.gif");
    vPauseAction = new AbstractAction("Pause", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    replayer.setStatus(Replayer.PAUSE);
		  }

	        };

    vPauseAction.setEnabled(true);

    button = toolBar.add(vPauseAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Pause");
    menuItem = replayMenu.add(vPauseAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // play action
    imgURL = getClass().getResource("/imag/vplay.gif");
    vPlayAction = new AbstractAction("Play", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    replayer.setStatus(Replayer.PLAY);
		  }

	        };

    vPlayAction.setEnabled(true);

    button = toolBar.add(vPlayAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Play");
    menuItem = replayMenu.add(vPlayAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // forward action
    imgURL = getClass().getResource("/imag/vforward.gif");
    vForwardAction = new AbstractAction("Fast forward", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    replayer.setStatus(Replayer.FORWARD);
		  }

	        };

    vForwardAction.setEnabled(true);

    button = toolBar.add(vForwardAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Fast forward");
    menuItem = replayMenu.add(vForwardAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // stop action
    imgURL = getClass().getResource("/imag/vstop.gif");
    vStopAction = new AbstractAction("Stop replaying", new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
                    replayer.end();
		    replayer = null;
		    try
		    {
                      logFile.close();
		    }
		    catch(Exception ee){}
		    playAction.setEnabled(true);
		    viewAction.setEnabled(true);
		    vOpenAction.setEnabled(true);

		  }

	        };

    vStopAction.setEnabled(true);

    button = toolBar.add(vStopAction);
    button.setText(""); //an icon-only button
    button.setToolTipText("Stop replaying");
    menuItem = replayMenu.add(vStopAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

    // action separator
    toolBar.addSeparator();

    imgURL = getClass().getResource("/imag/exit.gif");
    exitAction = new AbstractAction("Exit",  new ImageIcon(imgURL))
		{
		  public void actionPerformed(ActionEvent e)
		  {
		    if(player != null)
		    {
		      player.end();
		      player = null;
		    }
		    if(viewer != null)
		    {
		      viewer.end();
		      viewer = null;
		    }
                    if(transceiver != null)
		    {
                      transceiver.disconnect();
                      transceiver = null;
		    }


                    if(serverP != null)
		    {
                      serverP.destroy();
		      serverP = null;
		      for(int i=0; i<activeCommands.size(); i++)
		      {
		        ActiveCommand ac = (ActiveCommand)activeCommands.elementAt(i);
		        ac.getProcess().destroy();
		      }
		    }


		    System.exit(0);
		  }
		};

    exitAction.setEnabled(false);

    //button = toolBar.add(exitAction);
    //button.setText(""); //an icon-only button
    //button.setToolTipText("Exit");
    menuItem = gameMenu.add(exitAction);
    menuItem.setIcon(null); //arbitrarily chose not to use icon in menu

  }


  public void init()
  {
    URL soundURL;
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    if(isApplet)
    {
      String temp;
      temp = getParameter("host");
      if( temp == null)
		host = getDocumentBase().getHost();
      else
		host = temp;
        
      temp = getParameter("in3D");
      if( temp != null) {
		if (temp.compareTo("true") == 0 | temp.compareTo("TRUE") == 0 | temp.compareTo("True") == 0  ) in3D=true;
        else System.out.println("in3D paramater not true.");
      }
      else System.out.println("in3D paramater not found.");
        
      temp = getParameter("port");
      try
      {
		port = Integer.valueOf(temp).intValue();
      }
      catch(Exception e)
      {
		port = 7777;
      }

    }

    parent = this;

    // load audio files
    soundURL = getClass().getResource("/sound/applause.wav");
    applause = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/sound/referee1.wav");
    referee1 = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/sound/referee2.wav");
    referee2 = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/sound/kick.wav");
    kick = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/midi/pregame.mid");
    preGame = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/midi/firsthalf.mid");
    firstHalf = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/midi/halftime.mid");
    halfTime = Applet.newAudioClip(soundURL);

    soundURL = getClass().getResource("/midi/secondhalf.mid");
    secondHalf = Applet.newAudioClip(soundURL);


    // dialog set up
    playDialog = new PlayDialog(this);
    viewDialog = new ViewDialog(this);
    serverDialog = new ServerDialog(this);
    aiDialog = new AIDialog(this);
    playDialog.setVisible(false);
    viewDialog.setVisible(false);
    serverDialog.setVisible(false);
    aiDialog.setVisible(false);

    // Set up the menu bar.
    mb = new JMenuBar();

    // Create the toolbar and menu.
    toolBar = new JToolBar();
    toolBar.setFloatable(false);

    gameMenu = new JMenu("Game");

    replayMenu = new JMenu("Replay");

    createActionComponents();

    toolBar.addSeparator();
    comm = new JLabel("Chat:");
    toolBar.add(comm);
    input = new JTextField(30);
    toolBar.add(input);

    mb.add(gameMenu);

    mb.add(replayMenu);

    settingMenu = new JMenu("Settings");

    music = new JCheckBoxMenuItem("music", true);
    settingMenu.add(music);
    sound = new JCheckBoxMenuItem("sound", true);
    settingMenu.add(sound);

    settingMenu.addSeparator();

    number = new JCheckBoxMenuItem("playerID", true);
    settingMenu.add(number);
    chat = new JCheckBoxMenuItem("chat", true);
    settingMenu.add(chat);

    mb.add(settingMenu);

    setJMenuBar(mb);

    // Create mainPane
    mainPane = new JPanel();
    mainPane.setLayout(new BorderLayout());
    mainPane.setBackground(Color.gray);

    // Create arena Pane
    if( playingSoccer && !in3D )
    {
      arena = new Field(this);
    }
    else if( playingSoccer && in3D )
    {
      arena = new FieldJ3D(this);
    }
    else
    {
      arena = new Rink(this);
    }
    arena.requestFocus();

    // Create status Pane
    statusPane = new JPanel();
    statusPane.setLayout(new FlowLayout());
    d = new Dimension(500, 38);
    statusPane.setPreferredSize(d);
    statusPane.setMaximumSize(d);
    statusPane.setMinimumSize(d);
    statusPane.setBackground(Color.gray);
    statusPane.setBorder(BorderFactory.createRaisedBevelBorder());
    statusPane.setAlignmentX(Component.CENTER_ALIGNMENT);

    // left team status
    leftStatus = new JPanel();
    leftStatus.setLayout(new FlowLayout());
    d = new Dimension(200, 25);
    leftStatus.setPreferredSize(d);
    leftStatus.setMaximumSize(d);
    leftStatus.setMinimumSize(d);
    leftStatus.setBackground(Color.yellow);
    leftStatus.setBorder(BorderFactory.createLoweredBevelBorder());
    leftStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
    leftName = new JLabel("Alliance:");
    leftName.setBackground(Color.yellow);
    leftName.setForeground(Color.black);
    leftName.setAlignmentX(Component.CENTER_ALIGNMENT);
    leftScore = new JLabel("0");
    leftScore.setBackground(Color.yellow);
    leftScore.setForeground(Color.black);
    leftScore.setAlignmentX(Component.CENTER_ALIGNMENT);
    leftStatus.add(leftName);
    leftStatus.add(leftScore);

    // general game status
    gameStatus = new JPanel();
    gameStatus.setLayout(new FlowLayout());
    if(playingSoccer)
      d = new Dimension((Field.LENGTH + Field.SIDEWALK * 2) * Field.METER - 420, 25);
    else
      d = new Dimension((Rink.LENGTH + Rink.MARGIN * 2) - 420, 25);
    gameStatus.setPreferredSize(d);
    gameStatus.setMaximumSize(d);
    gameStatus.setMinimumSize(d);
    gameStatus.setBackground(Color.orange);
    gameStatus.setBorder(BorderFactory.createLoweredBevelBorder());
    gameStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
    period = new JLabel("Before Match:");
    period.setBackground(Color.gray);
    period.setForeground(Color.black);
    period.setAlignmentX(Component.CENTER_ALIGNMENT);
    mode = new JLabel("Before Kick Off:");
    mode.setBackground(Color.gray);
    mode.setForeground(Color.black);
    mode.setAlignmentX(Component.CENTER_ALIGNMENT);
    time = new JLabel("00:00");
    time.setBackground(Color.gray);
    time.setForeground(Color.black);
    time.setAlignmentX(Component.CENTER_ALIGNMENT);
    gameStatus.add(period);
    gameStatus.add(mode);
    gameStatus.add(time);

    // right team status
    rightStatus = new JPanel();
    rightStatus.setLayout(new FlowLayout());
    d = new Dimension(200, 25);
    rightStatus.setPreferredSize(d);
    rightStatus.setMaximumSize(d);
    rightStatus.setMinimumSize(d);
    rightStatus.setBackground(Color.red);
    rightStatus.setBorder(BorderFactory.createLoweredBevelBorder());
    rightStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
    rightName = new JLabel("Empire:");
    rightName.setBackground(Color.red);
    rightName.setForeground(Color.black);
    rightName.setAlignmentX(Component.CENTER_ALIGNMENT);
    rightScore = new JLabel("0");
    rightScore.setBackground(Color.red);
    rightScore.setForeground(Color.black);
    rightScore.setAlignmentX(Component.CENTER_ALIGNMENT);
    rightStatus.add(rightName);
    rightStatus.add(rightScore);

    statusPane.add(leftStatus);
    statusPane.add(gameStatus);
    statusPane.add(rightStatus);

    mainPane.add(statusPane, BorderLayout.NORTH);
    mainPane.add(arena, BorderLayout.CENTER);

    contentPane = (JPanel) getContentPane();

    contentPane.setLayout(new BorderLayout());
    contentPane.add(toolBar, BorderLayout.NORTH);
    contentPane.add(mainPane, BorderLayout.CENTER);
  }

  public static void main(String s[])
  {
    isApplet = false;

    try
    {
      // First look for parameters
      for( int c = 0 ; c < s.length ; c += 2 )
      {
        if( s[c].compareTo("-host") == 0 )
	{
          host = s[c+1];
	}
	else if( s[c].compareTo("-port") == 0 )
	{
          port = Integer.parseInt(s[c+1]);
	}
	else if( s[c].compareTo("-hockey") == 0 )
	{
          playingSoccer = false;
	}
	else if( s[c].compareTo("-3D") == 0 | s[c].compareTo("-3d") == 0  )
	{
          in3D = true;
	}   
        else
	{
	  throw new Exception();
        }
      }
    }
    catch(Exception e)
    {
      System.err.println("");
      System.err.println("USAGE: java SoccerMaster [-parameter value]");
      System.err.println("");
      System.err.println("Parameters  value        default");
      System.err.println("------------------------------------");
      System.err.println("host        host_name    localhost");
      System.err.println("port        port_number  6000");
      System.err.println("hockey      n/a          n/a");
      System.err.println("");
      System.err.println("Example:");
      System.err.println("java SoccerMaster -host localhost -port 6000");
      return;
    }

    JFrame f;
    if( playingSoccer )
      f = new JFrame("SoccerMaster");
    else
      f = new JFrame("HockeyMaster");

    f.addWindowListener(new WindowAdapter()
                        {
			  public void windowClosing(WindowEvent e)
			  {
                            if(serverP != null)
                            {
                              serverP.destroy();
			      serverP = null;
			      for(int i=0; i<activeCommands.size(); i++)
			      {
				ActiveCommand ac = (ActiveCommand)activeCommands.elementAt(i);
				ac.getProcess().destroy();
			      }
		            }
		            System.exit(0);
			  }
			});
    SoccerMaster sm = new SoccerMaster();

    // set windows icon
    ImageIcon img = new ImageIcon(SoccerMaster.class.getClass().getResource("/imag/icon.gif"));
    f.setIconImage(img.getImage());

    sm.init();
    sm.setEnabled();
    f.getContentPane().add("Center", sm);

    f.pack();
    f.setResizable(false);
    f.show();
  }

  public void setEnabled()
  {
    exitAction.setEnabled(true);
    setupServerAction.setEnabled(true);
    setupAIAction.setEnabled(true);

    vOpenAction.setEnabled(true);

  }

}
