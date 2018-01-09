/* Sensor.java
   This class get sensing info from the server and build the world from it

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
import java.util.*;


public class Sensor
{

  public  static int RATE = 100;
  
  private World world;
  private Executor executor;
  private SoccerMaster soccerMaster;
  
  public Sensor(World world, Executor executor, SoccerMaster soccerMaster)
  {
    this.world = world;
    this.executor = executor;
    this.soccerMaster = soccerMaster;
  }

  
  public void sensing(Packet info) throws IOException
  {
	  
    int min;
    int sec;

    Player player = null;
    Enumeration players = null;
    // process the info
    if(info.packetType == Packet.SEE)
    {
      world.see = (SeeData)info.data;

      world.me = world.see.player;
      world.status = world.see.status;
      world.ball = world.see.ball;
      world.leftTeam = world.see.leftTeam;
      world.rightTeam = world.see.rightTeam;
      
      // get the time information
      sec = world.see.time / (1000 / RATE);
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

      // find out if I have the ball
      if( world.ball.controllerType == world.me.side && 
	  world.ball.controllerId == world.me.id) world.IHaveBall = true;
          else world.IHaveBall = false;
	  
      // find out ball's velocity
      Vector2d.subtract(world.ball.position, world.ballPosition, world.ballVelocity);
      world.ballPosition.setXY(world.ball.position);

      // find out my velocity
      Vector2d.subtract(world.me.position, world.prePosition, world.myVelocity);
      world.prePosition.setXY(world.me.position);

      // ball's relative position 
      world.distance2Ball = world.see.player.position.distance(world.see.ball.position);
      world.direction2Ball = world.see.player.position.direction(world.see.ball.position);
      
      // find out if last step's action has been successful or not
      synchronized(world)
      {
	if(world.actionType == World.KICK || world.actionType == World.SHOOT
           || world.actionType == World.PASS)
	{
	  if(!world.IHaveBall)
	  {
	    world.force = 0;
	    world.actionType = World.DRIVE;
	  }
	}
	else if(world.actionType == World.CHASE)
	{
	  if(world.IHaveBall)
	  {
	    world.force = 0;
	    world.actionType = World.DRIVE;
	  }		  
	}
	else if(world.actionType == World.MOVE)
	{
	  double dist = world.destination.distance(world.me.position);
	  if(dist < 5)
	  {
	    world.force = 0;
	    world.actionType = World.DRIVE;
	  }		  
	}
      }
      // update the arena
      soccerMaster.arena.repaint();
      
      // execute the commands
      if(world.see.time - world.actTime >= World.INERTIA || world.IHaveBall)
      {
        executor.executing();
	world.actTime = world.see.time;
      }
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
