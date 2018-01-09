/* AIDialog.java
   This class opens a dialog to set up the AI players

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

public class AIDialog extends JDialog implements ActionListener
{

  private SoccerMaster soccerMaster;
  private String command = "java -cp soccer.jar soccer.client.ai.AIPlayers -l 10 -r 11";
  
  private JTextField commandField;
  private JComboBox commandBox = new JComboBox();
  private JTextField inputField;
  private JTextArea outputArea;
  
  // ai player runtime control
  private PrintStream ps; // process input stream
  private DataInputStream ds; // process output stream
  private ActiveCommand current =null; // current selected one  
  
  public AIDialog(SoccerMaster soccerMaster)
  {
    super();
    this.soccerMaster = soccerMaster;
    
    setTitle("AI Players");
    setSize(400, 300);
    setResizable(false);
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    
    Container c = getContentPane();
    c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
    
    JLabel l1 = new JLabel("Command Line:");
    l1.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l1);
    
    // command line field
    commandField = new JTextField(40);
    commandField.setActionCommand("Command");
    commandField.setText(command);
    commandField.addActionListener(this);
    commandField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(commandField);
    c.add(Box.createVerticalGlue());

    JLabel l2 = new JLabel("Current Active Command:");
    l2.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l2);    
    
    commandBox = new JComboBox();
    commandBox.setActionCommand("Active");
    commandBox.addActionListener(this);
    commandBox.setEditable(false);
    commandBox.setAlignmentX(LEFT_ALIGNMENT);
    c.add(commandBox);
    c.add(Box.createVerticalGlue());
    
    // set input field
    JLabel l3 = new JLabel("Input:");
    l3.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l3);
    
    // input field
    inputField = new JTextField(40);
    inputField.setActionCommand("Input");    
    inputField.addActionListener(this);
    inputField.setAlignmentX(LEFT_ALIGNMENT);
    c.add(inputField);
    c.add(Box.createVerticalGlue());
    
    // set output field
    JLabel l4 = new JLabel("Output:");
    l4.setAlignmentX(LEFT_ALIGNMENT);
    c.add(l4);    

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
      if(SoccerMaster.activeCommands.size() < SoccerMaster.maxCommands)
      {
        command = commandField.getText();
        try
        {
          Process p = SoccerMaster.runtime.exec(command);
	  PrintStream p_s = new PrintStream(p.getOutputStream());
	  ds = new DataInputStream(p.getInputStream());
	  OutputUpdater ou = new OutputUpdater(ds, 50);
	  ou.start();
	  if(ou.isAlive())
	  {
            ActiveCommand ac = new ActiveCommand(command, p, ou, p_s);
	    commandBox.addItem(ac);
	    SoccerMaster.activeCommands.addElement(ac);
	  }
	
        }
        catch(Exception ex)
        {
          JOptionPane.showMessageDialog(soccerMaster,
                                        ex, "Error", JOptionPane.ERROR_MESSAGE); 
	  ex.printStackTrace();
        }
      }
      else JOptionPane.showMessageDialog(soccerMaster,
                                        "Max number has been reached. No more process.", 
					"Error", JOptionPane.ERROR_MESSAGE);   	      
    }
    else if(e.getActionCommand().equals("Off"))
    {
      ActiveCommand ac = (ActiveCommand)commandBox.getSelectedItem();
      if(ac != null)
      {
        try
        {
          ac.getProcess().destroy();
          ac.getOutputUpdater().setOK(false);
          commandBox.removeItem(ac);
	  			SoccerMaster.activeCommands.removeElement(ac);
        }
        catch(Exception ex){}
      }
    }
    else if(e.getActionCommand().equals("Active"))
    {
      ActiveCommand ac = (ActiveCommand)commandBox.getSelectedItem();
      if(ac != null && !ac.equals(current))
      {
	      
        if(current != null)current.getOutputUpdater().setOutput(null);
        ps = ac.getPrintStream();
	outputArea.setText("");
        ac.getOutputUpdater().setOutput(outputArea);
        current = ac;	
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
