/* ViewData.java

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


package soccer.common;

import java.util.*;

/**
 * Provides visual data for server informing viewer type clients.
 *
 * @author Yu Zhang
 */
public class ViewData implements Data
{
  
  /**
   * the current simulation step.
   */
  public int time;

  /**
   * the ball.
   */
  public Ball ball;  
  /**
   * a list of positions for left team. 
   */
  public Vector leftTeam; 
  /**
   * a list of positions for right team. 
   */
  public Vector rightTeam; 


  /**
   * Constructs an empty ViewData for reading from an UDP packet.
   */
  public ViewData()
  {
    this.time = 0;
    this.ball = new Ball();
    this.leftTeam = new Vector();
    this.rightTeam =new Vector();
    
  } 
  
  /** 
   * Constructs a ViewData for writing to an UDP packet.
   *
   * @param time the current simulation step.
   * @param ball the ball.
   * @param leftTeam a list of positions for the left team.
   * @param rightTeam a list of positions for the right team.
   */
  public ViewData(int time, Ball ball, 
                  Vector leftTeam, Vector rightTeam)

  {
    this.time = time;
    this.ball = ball;
    this.leftTeam = leftTeam;
    this.rightTeam = rightTeam;

  } 
  
  // Load its data content from a string
  public void readData(StringTokenizer st)
  {
    double x,y;
    
    // Get the time.
    time = Integer.parseInt(st.nextToken());

    // Get the " "
    st.nextToken();

    // Get ball's position x
    x = Double.parseDouble(st.nextToken()) / 100;

    // Get the " "
    st.nextToken();

    // Get ball's position y
    y = Double.parseDouble(st.nextToken()) / 100;

    // Get ball's position
    ball.position.setXY(x,y);

    // Get the " "
    st.nextToken();

    // Get ball's controller type.
    ball.controllerType = st.nextToken().charAt(0);

    // Get the " "
    st.nextToken();

    // Get ball's controller id.
    ball.controllerId = Integer.parseInt(st.nextToken());

    // Get the " "
    st.nextToken();
    
    // Read each object information
    while(st.nextToken().charAt(0) == Packet.OPEN_TOKEN)
    {
      
      Player obj = new Player();
       
      // Get the side info.
      obj.side = st.nextToken().charAt(0);

      // Get the " "
      st.nextToken();
      
      obj.id = Integer.parseInt(st.nextToken());
	
      // Get the " "
      st.nextToken();   	

      // Get the position and facing
      x = Double.parseDouble(st.nextToken()) / 100;
	
      // Get the " "
      st.nextToken();  	

      y = Double.parseDouble(st.nextToken()) / 100;
      obj.position.setXY(x,y);
	
      // Get the " "
      st.nextToken();  
	
      obj.direction = Double.parseDouble(st.nextToken());	

      // add it to its team
      if(obj.side == 'l') leftTeam.addElement(obj);
      else rightTeam.addElement(obj);

      // Get the " "
      st.nextToken();      
      
      // Get the ")"
      st.nextToken(); 
      
      // Get the " "
      st.nextToken();       
    }
  }

  // Stream its data content to a string.
  public void writeData(StringBuffer sb)
  {
    Player obj;
    Enumeration players;
    
    // send packet name and time
    sb.append(Packet.VIEW);
    sb.append(' ');
    sb.append(time);
    sb.append(' ');

    // send ball information
    sb.append((int)(ball.position.getX() * 100));
    sb.append(' ');
    sb.append((int)(ball.position.getY() * 100));
    sb.append(' ');
    sb.append(ball.controllerType);
    sb.append(' ');
    sb.append(ball.controllerId);
    sb.append(' ');
    
    // send left team information
    players = leftTeam.elements();
    while (players.hasMoreElements())
    {
      obj = (Player) players.nextElement();
      
      sb.append('(');
      sb.append(obj.side);
      sb.append(' ');
      sb.append(obj.id);
      sb.append(' ');
      sb.append((int)(obj.position.getX() * 100));
      sb.append(' ');
      sb.append((int)(obj.position.getY() * 100));
      sb.append(' ');
      sb.append((int)obj.direction);
      sb.append(' ');
      sb.append(")");
      sb.append(" ");
    }
    
    // send right team information
    players = rightTeam.elements();
    while (players.hasMoreElements())
    {
      obj = (Player) players.nextElement();
      
      sb.append('(');
      sb.append(obj.side);
      sb.append(' ');
      sb.append(obj.id);
      sb.append(' ');
      sb.append((int)(obj.position.getX() * 100));
      sb.append(' ');
      sb.append((int)(obj.position.getY() * 100));
      sb.append(' ');
      sb.append((int)obj.direction);
      sb.append(' ');
      sb.append(")");
      sb.append(" ");
    }    

  } 
  
}
