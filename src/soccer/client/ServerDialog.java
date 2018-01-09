/* ServerDialog.java
   This class opens a dialog to set up the local server

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
import java.io.*;

public class ServerDialog extends JDialog implements ActionListener 
{

  private SoccerMaster soccerMaster;
  private String command = "java -cp soccer.jar soccer.server.SoccerServer";
  private String property = "";
  private String port = "";
  private String others ="";
  
  private JTextField commandField;
  private JTextField portField;
  private JTextField propertyField;
  private JTextField othersField;
  
  private JTextField inputField;
  private JTextArea outputArea;
  
  // server runtime control
  private PrintStream ps; // process input stream
  private DataInputStream ds; // process output stream
  private OutputUpdater serverOU;
  
  public ServerDialog(SoccerMaster soccerMaster)
  {
    super();
    this.soccerMaster = soccerMaster;
    
    setTitle("Server");
    setSize(400, 350);
    setResizable(false);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    
    Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
    
    JLabel l1 = new JLabel("Command:");
    l1.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l1);
    
    commandField = new JTextField(40);
    commandField.setText(command);
    commandField.setActionCommand("Command");
    commandField.addActionListener(this);
    commandField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(commandField);
    c.add(Box.createVerticalGlue());
    
    // set port number
    JLabel l2 = new JLabel("Port number:");
    l2.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l2);
    
    portField = new JTextField(40);
    portField.setText(port);
    portField.setActionCommand("Port");
    portField.addActionListener(this);
    portField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(portField);
    c.add(Box.createVerticalGlue());
    
    // set property file
    JLabel l3 = new JLabel("Property file:");
    l3.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l3);
    
    propertyField = new JTextField(40);
    propertyField.setText(property);
    propertyField.setActionCommand("Property");    
    propertyField.addActionListener(this);
    propertyField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(propertyField);
    c.add(Box.createVerticalGlue());
    
    // set other command parameters
    JLabel l4 = new JLabel("Other parameters:");
    l4.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l4);
    
    othersField = new JTextField(40);
    othersField.setText(others);
    othersField.setActionCommand("Other");    
    othersField.addActionListener(this);
    othersField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(othersField);
    c.add(Box.createVerticalGlue());

    // set input field
    JLabel l5 = new JLabel("Input:");
    l5.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l5);
    
    // input field
    inputField = new JTextField(40);
    inputField.setActionCommand("Input");    
    inputField.addActionListener(this);
    inputField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(inputField);
    c.add(Box.createVerticalGlue());
    
    // set output field
    JLabel l6 = new JLabel("Output:");
    l6.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l6);    

    // output field
    outputArea = new JTextArea(6, 80);
    outputArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(outputArea,
                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);
    c.add(scrollPane);
    c.add(Box.createVerticalGlue());			 

    // option buttons
    JButton on = new JButton("On");
    on.setActionCommand("On");
    on.addActionListener(this);
    JButton off = new JButton("Off");
    off.setActionCommand("Off");
    off.addActionListener(this);    
    JButton close = new JButton("Close");
    close.setActionCommand("Close");
    close.addActionListener(this);

    // option panel
    JPanel option = new JPanel();
    option.setLayout(new FlowLayout());
    option.add(on);
    option.add(off);
    option.add(close);    
    option.setAlignmentX(LEFT_ALIGNMENT);    
    c.add(option);
    c.add(Box.createVerticalGlue());

                 
    
  }
  
  public void actionPerformed(ActionEvent e) 
  {
    //   
    if(e.getActionCommand().equals("On")) 
    {
      if(serverOU != null)
        if(!serverOU.isOK()) SoccerMaster.serverP = null;
      if(SoccerMaster.serverP != null) 
      {
        JOptionPane.showMessageDialog(soccerMaster,
                                      "You need to turn off the current server first.", 
				      "Error", JOptionPane.ERROR_MESSAGE); 
        return;
      }	      
      command = commandField.getText();
      port = portField.getText();
      property = propertyField.getText();
      others = othersField.getText();
      try
      {
        SoccerMaster.serverP = SoccerMaster.runtime.exec(command + " " + port + " " + property + " " + others);
	ps = new PrintStream(SoccerMaster.serverP.getOutputStream());
	ds = new DataInputStream(SoccerMaster.serverP.getInputStream());
	serverOU = new OutputUpdater(outputArea, ds, 50);
	serverOU.start();
	
      }
      catch(Exception ex)
      {
        JOptionPane.showMessageDialog(soccerMaster,
                                      ex, "Error", JOptionPane.ERROR_MESSAGE);    	      
      }	      
    }
    else if(e.getActionCommand().equals("Off"))
    {
      if(SoccerMaster.serverP != null)
      {
        try
        {
          outputArea.setText("");
	  SoccerMaster.serverP.destroy();
	  SoccerMaster.serverP = null;
          serverOU.setOK(false);	  

        }
        catch(Exception ex)
        {
          JOptionPane.showMessageDialog(soccerMaster,
                                      ex, "Error", JOptionPane.ERROR_MESSAGE);    	      
        }
      }	                
    }
    else if(e.getActionCommand().equals("Input"))
    {
      if(ps != null)
      {
        ps.println(inputField.getText());
        ps.flush();
      }
    }
    else if(e.getActionCommand().equals("Close")) this.setVisible(false);
            
  }

  
}
