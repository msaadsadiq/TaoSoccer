/* Sball.java

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

import soccer.common.Ball;
import soccer.common.Vector2d;

public class Sball extends MovableObj
{

  // player will have chance to get the ball when ball-player distance is under control range
  public static double CONTROLRANGE   = 0.5; 
  
  // max random factor for ball movement
  public static double RANDOM     = 0.02; 

  // the player who has the ball
  public char controllerType;
  public int controllerId;
  public boolean free;


  //---------------------------------------------------------------------------
  public Sball()
  {
    super(DECAY, MAX_SPEED);
    controllerType = 'f';
    controllerId = 0;
    free = true;
  }    


  //---------------------------------------------------------------------------
  // set the ball at position(x,y)
  public void set(double x, double y)
  {
    moveTo(x, y);
    controllerType = 'f';
    controllerId = 0;
    free = true;
  }    
  
  
  //---------------------------------------------------------------------------
  /**
   * @return Ball object
   */
  public Ball getBall(){
    return new Ball(getPosition(), controllerType, controllerId);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Update ball position
   */
  public void updatePosition(){
    
    getPosition().setXY(nextPosition(0, 0));
  }


  //---------------------------------------------------------------------------
  /**
   * Get next position
   * @return Vector with new position
   */    
  public Vector2d nextPosition(double force, double dir){

    return move(force * ACCEL_MAX, dir);    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return true if player can be ball controller
   */
  public boolean canControl(Splayer player){
    
    return (getPosition().distance(player.getPosition()) < CONTROLRANGE);
  }


  //---------------------------------------------------------------------------
  // Private members
  private final static double DECAY = 0.94;
  private final static double ACCEL_MAX = 0.025;
  private final static double MAX_SPEED = 3;
  
}
