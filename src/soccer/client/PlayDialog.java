/* PlayDialog.java
   This class opens a play dialog to get required information for play
   
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


package soccer.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import soccer.common.*;

public class PlayDialog extends JDialog implements ActionListener 
{

  private SoccerMaster soccerMaster;
  private char side = 'l';
  
  private JTextField hostName;
  private JTextField portNum;
  
  public PlayDialog(SoccerMaster soccerMaster)
  {
    super();
    this.soccerMaster = soccerMaster;
    
    setTitle("Play");
    setSize(250, 200);
    setResizable(false);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    
    Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
    
    JLabel l1 = new JLabel("Side to join:");
    l1.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l1);
    
    // Create the radio buttons.
    JRadioButton leftButton = new JRadioButton("Left", true);    
    leftButton.setActionCommand("Left");
    JRadioButton anyButton = new JRadioButton("None", false);    
    anyButton.setActionCommand("Anyside");    
    JRadioButton rightButton = new JRadioButton("Right", false);    
    rightButton.setActionCommand("Right");
    
    // Group the radio buttons.
    ButtonGroup group = new ButtonGroup();
    group.add(anyButton);
    group.add(leftButton);
    group.add(rightButton);
    anyButton.setSelected(true);
    
    // Register a listener for the radio buttons.
    leftButton.addActionListener(this);
    anyButton.addActionListener(this);
    rightButton.addActionListener(this);
    
    // Put the radio buttons in a panel
    JPanel sideOption = new JPanel();
    sideOption.setLayout(new FlowLayout());
    sideOption.add(leftButton);
    sideOption.add(anyButton);
    sideOption.add(rightButton);
    sideOption.setAlignmentX(LEFT_ALIGNMENT);
    c.add(sideOption);
    c.add(Box.createVerticalGlue());
    
    // get server name
    JLabel l2 = new JLabel("Server name:");
    l2.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l2);
    
    hostName = new JTextField(20);
    hostName.setText(SoccerMaster.host);
    hostName.setActionCommand("Host");
    hostName.addActionListener(this);
    hostName.setAlignmentX(LEFT_ALIGNMENT);
    c.add(hostName);
    c.add(Box.createVerticalGlue());
    
    // get server port
    JLabel l3 = new JLabel("Server port:");
    l3.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l3);
    
    portNum = new JTextField(20);
    portNum.setText(String.valueOf(SoccerMaster.port));
    portNum.setActionCommand("Port");    
    portNum.addActionListener(this);
    portNum.setAlignmentX(LEFT_ALIGNMENT);
    c.add(portNum);
    c.add(Box.createVerticalGlue());

    // option buttons
    JButton OK = new JButton("OK");
    OK.setActionCommand("OK");
    OK.addActionListener(this);
    JButton Cancel = new JButton("Cancel");
    Cancel.setActionCommand("Cancel");
    Cancel.addActionListener(this);

    // option panel
    JPanel option = new JPanel();
    option.setLayout(new FlowLayout());
    option.add(OK);
    option.add(Cancel);
    option.setAlignmentX(LEFT_ALIGNMENT);    
    c.add(option);
    c.add(Box.createVerticalGlue());             
    
  }
  
  public void actionPerformed(ActionEvent e) 
  {
    // set the side form radio buttons      
    if(e.getActionCommand().equals("Left")) side = 'l';
    else if(e.getActionCommand().equals("Right")) side = 'r';
    else if(e.getActionCommand().equals("Anyside")) side = 'a';
    // get the server name and port
    else if(e.getActionCommand().equals("Host")) SoccerMaster.host = new String(hostName.getText());
    else if(e.getActionCommand().equals("Port")) SoccerMaster.port = Integer.parseInt(portNum.getText());
    // if OK, then connect to server
    else if(e.getActionCommand().equals("OK"))
    {
      // get server information
      SoccerMaster.host = new String(hostName.getText());
      SoccerMaster.port = Integer.parseInt(portNum.getText());
      
      setVisible(false);
      
      // link to the server and initialize the player
      init();
      
    }
    else if(e.getActionCommand().equals("Cancel")) setVisible(false);
            
  }

  // initialize the player 
  private void init()
  {
    try
    {
      Transceiver transceiver = new Transceiver(false);
      soccerMaster.transceiver = transceiver;
      SoccerMaster.address = InetAddress.getByName(SoccerMaster.host);
      // Send the connect packet to server
      ConnectData connect = new ConnectData(ConnectData.PLAYER, side);
      Packet packet = new Packet(Packet.CONNECT, connect, 
                          SoccerMaster.address, SoccerMaster.port);
      transceiver.send(packet);
      
      // wait for the connect message from server
      transceiver.setTimeout(1000);
      int limit = 0;
      packet = null;
      while(limit<60)
      { 
        try
        {
          packet = transceiver.receive();
          break;
        }
        catch(Exception e) {
          limit++;
        }
      }
      
      transceiver.setTimeout(0);
      if(packet == null) 
      {
        JOptionPane.showMessageDialog(soccerMaster,
          "Waiting time expired. Can not INIT.", "Error", JOptionPane.ERROR_MESSAGE); 
        return;
      }
	
      if(packet.packetType == Packet.INIT)
      {
        System.gc();
        InitData init = (InitData) packet.data;
        soccerMaster.player = new Cplayer(init, soccerMaster);
        soccerMaster.player.start();
	      soccerMaster.playAction.setEnabled(false);
	      soccerMaster.viewAction.setEnabled(false);
	      soccerMaster.vOpenAction.setEnabled(false);
	      soccerMaster.stopAction.setEnabled(true);
      }
      else
      {
        System.out.println("Can't connect to server. Wrong packet: " + packet);
        JOptionPane.showMessageDialog(soccerMaster,
                                   "Packet type wrong. Can not INIT.", "Error", JOptionPane.ERROR_MESSAGE); 
        return;
      }
    }
    catch(Exception e)
    {
      JOptionPane.showMessageDialog(soccerMaster,
                                   e, "Error", JOptionPane.ERROR_MESSAGE);     
      return;
    }  
 
  }       
  
}
