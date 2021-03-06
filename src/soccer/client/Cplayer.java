/* Cplayer.java
   This class get sensing info, plan its move and execute it.
   
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

import soccer.common.*;
import java.io.*;

public class Cplayer extends Thread 
{
  private SoccerMaster soccerMaster; 
  private World world; //world view
  private Sensor sensor; // server data processing
  private Controller controller; // user input processing
  private Executor executor; // send command to server
  
  private Packet info;

  private boolean end = false;
  
  public Cplayer(InitData init, SoccerMaster soccerMaster)
  {
    this.soccerMaster = soccerMaster;
    world = new World(init);
    soccerMaster.arena.setWorld(world);
    executor = new Executor(world, soccerMaster);
    sensor = new Sensor(world, executor, soccerMaster);
    controller = new Controller(world, soccerMaster);
    soccerMaster.arena.addMouseListener(controller);
    soccerMaster.arena.addMouseMotionListener(controller);
    soccerMaster.arena.addKeyListener(controller);
    soccerMaster.input.addActionListener(controller);
        
  }

  //---------------------------------------------------------------------------
  /*
   * Run player
   */ 
  public void run() 
  {
    end = false;
    while(!end)
    {
      try
      {
        // sensor collect info from server, build its world
        info = soccerMaster.transceiver.receive();
	      sensor.sensing(info);
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }

    }
  }

  public void end()
  {
    try
    {
    end = true;
    ByeData bye = new ByeData();
    Packet command = new Packet(Packet.BYE, bye,  SoccerMaster.address,  SoccerMaster.port);
    soccerMaster.transceiver.send(command); 
    }
    catch(IOException e)
    {

    }    
  }

   
}
