/* Replayer.java
   This class get display info from a log file, and display it.
   
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
import java.util.*;

public class Replayer extends Thread 
{

  public  static int RATE = 100;

  public  static final int PLAY = 1;
  public  static final int FORWARD = 2;
  public  static final int PAUSE = 3;
  public  static final int BACK = 4;
  public  static final int REWIND = 5;  
  
  public  static final int SKIP = 10;
  
  private SoccerMaster soccerMaster;
  private World world; //world view
  private Sensor sensor; // server data processing

  private int status = Replayer.PAUSE;
  private Stack positions = new Stack();
  private boolean end = false;

  private Packet info = new Packet();
  private String str = null;
  
  public Replayer(SoccerMaster soccerMaster)  
  {
    this.soccerMaster = soccerMaster;
    world = new World();
    soccerMaster.arena.setWorld(world);    
  }
 
  public void run() 
  {
    end = false;
    while(!end)
    {

      try
      {
        // get the time before the step
        long timeBefore = System.currentTimeMillis();

        // get the display info from log file
	switch(status)
	{
	  case Replayer.PLAY:
	    positions.push(new Long(soccerMaster.logFile.getFilePointer()));
            str = soccerMaster.logFile.readLine();
	    if(str == null) status = Replayer.REWIND;
            break;
          
	  case Replayer.FORWARD:
	    for(int i=0; i<SKIP; i++)
	    {
	      positions.push(new Long(soccerMaster.logFile.getFilePointer()));
              str = soccerMaster.logFile.readLine();
	      if(str == null) status = Replayer.REWIND;
	    }
            break;	  

	  case Replayer.PAUSE:
	    break;

	  case Replayer.BACK:
	    if(!positions.empty())
	    {
	      soccerMaster.logFile.seek(((Long)positions.pop()).longValue());
              str = soccerMaster.logFile.readLine();
	    }
	    break;
	    
	  case Replayer.REWIND:
	    for(int i=0; i<SKIP - 1; i++)
	    {
	      if(!positions.empty())
	      {
	        soccerMaster.logFile.seek(((Long)positions.pop()).longValue());
	      }
	    }

	    if(!positions.empty())
	    {
	      soccerMaster.logFile.seek(((Long)positions.pop()).longValue());
              str = soccerMaster.logFile.readLine();
	    }	    
            break;	    

	}

	if(str != null)
	{
          info.readPacket(str);
        }
	
	viewing(info);

        // get the time after the step
        long timeAfter = System.currentTimeMillis();

	// figure out how long it takes to process
        long timeSpent = timeAfter - timeBefore;

        if (timeSpent < RATE) sleep( (int)(RATE - timeSpent) );


      }
      catch(Exception e)
      {

      }

      

    }
  }

  public void setStatus(int s)
  {
    status = s;       
  }

  public int getStatus()
  {
    return status;       
  }

  public void end()
  {
    end = true;
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
	  if(status == PLAY) soccerMaster.kick.play();
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
	if(soccerMaster.sound.isSelected()) 
	  if(status == PLAY) soccerMaster.applause.play();
      }
      else if(world.referee.goal_r > world.rightGoal)
      {
        world.rightGoal = world.referee.goal_r;
        if(soccerMaster.sound.isSelected()) 
	  if(status == PLAY) soccerMaster.applause.play();
      }
      else if(world.referee.period != world.prePeriod)
      {
        if(soccerMaster.sound.isSelected()) 
	  if(status == PLAY) soccerMaster.referee2.play();
	world.prePeriod = world.referee.period;
	if(world.referee.period == RefereeData.PRE_GAME)
	{
          soccerMaster.secondHalf.stop();
	  if(soccerMaster.music.isSelected()) 
	    if(status == PLAY) soccerMaster.preGame.loop();
	}
	else if(world.referee.period == RefereeData.FIRST_HALF)
	{
          soccerMaster.preGame.stop();
	  if(soccerMaster.music.isSelected()) 
	    if(status == PLAY) soccerMaster.firstHalf.loop();
	}
	else if(world.referee.period == RefereeData.HALF_TIME)
	{
          soccerMaster.firstHalf.stop();
	  if(soccerMaster.music.isSelected()) 
	    if(status == PLAY) soccerMaster.halfTime.loop();
	}
	else if(world.referee.period == RefereeData.SECOND_HALF)
	{
          soccerMaster.halfTime.stop();
	  if(soccerMaster.music.isSelected()) 
	    if(status == PLAY) soccerMaster.secondHalf.loop();
	}
      }
      else if(world.referee.mode != world.preMode)
      {
        if(soccerMaster.sound.isSelected()) 
	  if(status == PLAY) soccerMaster.referee1.play();
        world.preMode = world.referee.mode;
      }
    }       
  
  }

   
}
