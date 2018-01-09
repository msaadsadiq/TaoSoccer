/* Splayer.java

   Copyright (C) 2001  Yu Zhang
   Copyright (C) 2003  Krzysztof Langner

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


package soccer.server.sim;

import soccer.common.Player;
import soccer.common.Vector2d;

public class Splayer extends MovableObj
{

  // Once a player makes up his mind to do an action, the action will
  // be carried out for next INERTIA steps. So this means, the client
  // does not need to send a command every step, he only needs to send
  // a command every INERTIA steps.
  // This will also give a client more time to think before to actually 
  // do something.
  public static int INERTIA   =  1;
  
  // player's maximum dash force 
  public static double MAXDASH  = 100;
  
  // player's minimum dash force 
  public static double MINDASH  = -30;

  // player's maximum kick force 
  public static double MAXKICK  = 100;
  
  // player's minimum kick force 
  public static double MINKICK  = 0;  
  
  // max random factor for player movement
  public static  double RANDOM     = 0.01; 

  // max dribble force factor, when player is dribbling, 
  //the max force he can use to dash is MAXDASH * DRIBBLE 
  public static  double DRIBBLEFACTOR  = 0.4;

  // kick direction random factor. When you decide to kick the ball to X direction,
  // the actual ball moving direction will be X +/- 10 degrees. So, the closer to the
  // goal, the better chance to score.
  public static  double KICKRANDOM = 10; 
  
  // players collides when their distance is smaller than COLLIDERANGE
  public static double COLLIDERANGE = 0.6; 
     
  // player identifier
  public char side;
  public int id;
  
  // the message from player client
  public String message = null;
  // Once the player has sent the message, he has to wait at least NOWORD sec to   
  // communicate again
  public static double NOWORD = 30;
  public int noWord = 0;


  //---------------------------------------------------------------------------
  /**
   * Constructor
   */
  public Splayer(char side, int id)
  {

    super(DECAY, MAX_SPEED);
    this.side = side;
    this.id = id;

    if(side == 'l') 
    {
      moveTo(-World.LENGTH/2 - 3, 5*id - World.WIDTH/2 - World.SIDEWALK);
      direction = 0;
    }
    else 
    {
      moveTo(World.LENGTH/2 + 3, World.WIDTH/2 + World.SIDEWALK - 5*id);
      direction = 180;
    }

  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return true if this player is goal keeper
   */
  public boolean isGoalkeeper(){
    
    return (id == 1); 
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return true if this is teammate
   */
  public boolean isTeammate(Splayer player){
    
    return (player.side == side);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Set have boal flag
   */
  public void setHaveBall(boolean have){
    haveBall = have;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Get player direction
   */
  public double getDirection(){
    
    return direction;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Get player force
   */
  public double getForce(){
    
    return force;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return true if player wants to kick a ball
   */
  public boolean isKickingBall(){
    return kickBall;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Get player which describes this Splayer
   */
  public Player getPlayer(){
    return new Player(side, id, getPosition(), direction);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Kick ball command
   */
  public void kickBall(double force, double dir){

    cForce = force;
    cDirection = dir;    
    cKickBall = true;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Dash command
   */
  public void dash(double force, double dir){
    
    cForce = force;
    cDirection = dir;
    cKickBall = false;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Set player action
   */
  public void setPlayerAction(){

    direction = cDirection;
    force = cForce;
    kickBall = cKickBall;
    
    cForce = 0;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Get next position
   * @return Vector with new position
   */    
  public Vector2d nextPosition(){
    
    // find out if player can have ball again after he kick it out
    if(noBall > 0) 
      noBall --;
  
    // find out if player can communicate again after he shouts
    if(noWord > 0) 
      noWord --;

    // calculate position    
    return move(force*ACCEL_FACTOR, direction);
    
  }

  
  //---------------------------------------------------------------------------
  // Private members
  /* dash/kick direction and force received from the client */
  private double    cDirection = 0;
  private double    cForce = 0;
  private boolean   cKickBall = false;
  
  // dash/kick direction and force actually used in simulation
  private double    direction = 0;
  private double    force = 0;
  private boolean   kickBall = false; // if the player try to kick the ball
  /** true if the player has the ball under his control */
  private boolean   haveBall = false;
  /** Player can not have ball */ 
  private int       noBall = 0;
  /**
   * Defines how fast player will slow.
   */
  private final static double DECAY = 0.4;
  private final static double ACCEL_FACTOR = 0.004;
  private final static double MAX_SPEED = 3;

}
