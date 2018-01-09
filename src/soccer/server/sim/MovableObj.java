/* Moving.java

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

import soccer.common.Vector2d;

public class MovableObj 
{

  //---------------------------------------------------------------------------
  /**
   * Constructor
   */
  public MovableObj(double decay, double maxSpeed)
  {
    this.decay = decay;
    this.maxSpeed = maxSpeed;
    position = new Vector2d();
    velocity = new Vector2d();
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return object position
   */
  public Vector2d getPosition(){
    
    return position;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Get next position
   * @return Vector with new position
   */    
  public Vector2d move(double force, double dir){
    
    Vector2d  nextPos;
    Vector2d  u;
    Vector2d  a = new Vector2d(velocity);
    double    norm;

    a = Vector2d.polar(force, dir);
    u = Vector2d.add(velocity, a);
    norm = u.norm();
    if(norm > maxSpeed){
      u.times(maxSpeed/norm);
    }
    
    nextPos = Vector2d.add(getPosition(), u);
    velocity = new Vector2d(u.getX()*decay, u.getY()*decay);
    
    return nextPos;
  }


  //---------------------------------------------------------------------------
  /**
   * set the object at given position
   */
  public void moveTo(double x, double y)
  {
    position = new Vector2d(x, y);
    velocity = new Vector2d();
  }
  
  
  //---------------------------------------------------------------------------
  public void collide(){
    
    velocity.setX(-0.2*velocity.getX());
  }
  
  
  //---------------------------------------------------------------------------
  // Private members
  /** Object position */
  private Vector2d  position;
  private Vector2d  velocity; 
  private double    decay;
  private double    maxSpeed;

}

