/* Cviewer.java
   This class get sensing info from server, and display it.
   
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

public class Cviewer extends Thread 
{

  public  static int RATE = 100;

  private SoccerMaster soccerMaster;
  private World world; //world view
  private Sensor sensor; // server data processing

  private boolean end = false;
  
  private Packet info;
  
  public Cviewer(SoccerMaster soccerMaster)  
  {
    this.soccerMaster = soccerMaster;
    world = new World();
    soccerMaster.arena.setWorld(world);    
  }
 
  public void run() 
  {
    int i = 0;
    end = false;
    while(!end)
    {

      try
      {
        // sensor collect info from server, build its world
        info = soccerMaster.transceiver.receive();
	viewing(info);
	i++;
	if(i%600 == 0) 
	{
          EmptyData empty = new EmptyData();
          Packet command = new Packet(Packet.EMPTY, empty, SoccerMaster.address, SoccerMaster.port);
          soccerMaster.transceiver.send(command);
	}
	
      }
      catch(IOException e)
      {

      }

    }
  }

  public void end()
  {
    try
    {
    end = true;
    ByeData bye = new ByeData();
    Packet command = new Packet(Packet.BYE, bye, SoccerMaster.address, SoccerMaster.port);
    soccerMaster.transceiver.send(command); 
    }
    catch(IOException e)
    {

    }       
  }

  private void viewing(Packet info)
  {
	  
    int min;
    int sec;

    // process the info
    if(info.packetType == Packet.VIEW)
    {
      world.view = (ViewData)info.data;
      
      world.me = null;
      world.ball = world.view.ball;
      world.leftTeam = world.view.leftTeam;
      world.rightTeam = world.view.rightTeam;
      
      // get the time information
      sec = world.view.time / (1000 / RATE);
      min = sec / 60;
      sec = sec % 60;

      soccerMaster.time.setText(min + ":" + sec);

      // find out if somebody has kicked the ball
      if(world.ball.controllerType != world.preController)
      {
        world.preController = world.ball.controllerType;
        if(world.ball.controllerType == 'f' && soccerMaster.sound.isSelected())
	soccerMaster.kick.play();
      }

      // update the arena
      soccerMaster.arena.repaint();       
    }
    else if(info.packetType == Packet.HEAR)
    {
      world.message = (HearData)info.data;
      if(world.message.side == 'l') world.leftM = world.message;
      else if(world.message.side == 'r') world.rightM = world.message;
    }
    else if(info.packetType == Packet.REFEREE)
    {
      world.referee = (RefereeData)info.data;
      
      // get the time information
      sec = world.referee.time / (1000 / RATE);
      min = sec / 60;
      sec = sec % 60;

      
      soccerMaster.period.setText(RefereeData.periods[world.referee.period] + ":");
      soccerMaster.mode.setText(RefereeData.modes[world.referee.mode] + ":");
      soccerMaster.time.setText(min + ":" + sec);

      soccerMaster.leftName.setText(world.referee.leftName);
      soccerMaster.leftScore.setText(":" + world.referee.goal_l);
      
      soccerMaster.rightName.setText(world.referee.rightName);
      soccerMaster.rightScore.setText(":" + world.referee.goal_r );
      
      if(world.referee.goal_l > world.leftGoal)
      {
        world.leftGoal = world.referee.goal_l;
	if(soccerMaster.sound.isSelected()) soccerMaster.applause.play();
      }
      else if(world.referee.goal_r > world.rightGoal)
      {
        world.rightGoal = world.referee.goal_r;
        if(soccerMaster.sound.isSelected()) soccerMaster.applause.play();
      }
      else if(world.referee.period != world.prePeriod)
      {
        if(soccerMaster.sound.isSelected()) soccerMaster.referee2.play();
	world.prePeriod = world.referee.period;
	if(world.referee.period == RefereeData.PRE_GAME)
	{
          soccerMaster.secondHalf.stop();
	  if(soccerMaster.music.isSelected()) soccerMaster.preGame.loop();
	}
	else if(world.referee.period == RefereeData.FIRST_HALF)
	{
          soccerMaster.preGame.stop();
	  if(soccerMaster.music.isSelected()) soccerMaster.firstHalf.loop();
	}
	else if(world.referee.period == RefereeData.HALF_TIME)
	{
          soccerMaster.firstHalf.stop();
	  if(soccerMaster.music.isSelected()) soccerMaster.halfTime.loop();
	}
	else if(world.referee.period == RefereeData.SECOND_HALF)
	{
          soccerMaster.halfTime.stop();
	  if(soccerMaster.music.isSelected()) soccerMaster.secondHalf.loop();
	}
      }
      else if(world.referee.mode != world.preMode)
      {
        if(soccerMaster.sound.isSelected()) soccerMaster.referee1.play();
        world.preMode = world.referee.mode;
      }
    }       
  
  }

   
}
