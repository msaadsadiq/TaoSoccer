/* SeeData.java

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
 * Provides visual data for server informing player type clients.
 *
 * @author Yu Zhang
 */
public class SeeData implements Data
{

  /**
   * status NO_OFFSIDE indicates no players are at offside position.
   */
  public static final int NO_OFFSIDE     = 0;
  /**
   * status OFFSIDE indicates I'm at offside position.
   */
  public static final int OFFSIDE     = 1;
  /**
   * status T_OFFSIDE indicates my teammates are at offside position.
   */
  public static final int T_OFFSIDE     = 2;
  
  /**
   * the current simulation step.
   */
  public int time;
  /**
   * the player.
   */
  public Player player;
  /**
   * the player's offside position status. 0 means no players in player's team 
   * are at offside position. 1 means the player is at offside position. 
   * 2 means the player's teammates are at offside position.
   */  
  public int status;
  /**
   * the ball.
   */
  public Ball ball;  
  /**
   * a list of positions for all players 
   */
  public Vector playerList = new Vector(); 
  /**
   * a list of positions for left team. 
   */
  public Vector leftTeam; 
  /**
   * a list of positions for right team. 
   */
  public Vector rightTeam; 


  /**
   * Constructs an empty SeeData for reading from an UDP packet.
   */
  public SeeData()
  {
    this.time = 0;
    this.player = new Player();
    this.status = 0;
    this.ball = new Ball();
    this.leftTeam = new Vector();
    this.rightTeam =new Vector();
    
  } 
  
  /** 
   * Constructs a SeeData for writing to an UDP packet.
   *
   * @param time the current simulation step.
   * @param player the player.
   * @param status the player's offside position status.
   * @param ball the ball.
   * @param leftTeam a list of positions for the left team.
   * @param rightTeam a list of positions for the right team.
   */
  public SeeData(int time, Player player, int status, Ball ball, 
                  Vector leftTeam, Vector rightTeam)

  {
    this.time = time;
    this.player = player;
    this.status = status;
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

    // Get player side
    player.side = st.nextToken().charAt(0);

    // Get the " "
    st.nextToken();

    // Get player id
    player.id = Integer.parseInt(st.nextToken());

    // Get the " "
    st.nextToken();    

    // Get player position x
    x = Double.parseDouble(st.nextToken()) / 100;

    // Get the " "
    st.nextToken();

    // Get player position y
    y = Double.parseDouble(st.nextToken()) / 100;

    // Get player position
    player.position.setXY(x,y);

    // Get the " "
    st.nextToken();
    
    // Get player direction
    player.direction = Double.parseDouble(st.nextToken());

    // Get the " "
    st.nextToken();

    // Get the status
    status = Integer.parseInt(st.nextToken());

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
    sb.append(Packet.SEE);
    sb.append(' ');
    sb.append(time);
    sb.append(' ');

    // send the player information
    sb.append(player.side);
    sb.append(' ');
    sb.append(player.id);
    sb.append(' ');
    sb.append((int)(player.position.getX() * 100));
    sb.append(' ');
    sb.append((int)(player.position.getY() * 100));
    sb.append(' ');
    sb.append((int)player.direction);
    sb.append(' ');
    sb.append(status);
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
    
    // send rest of the players
    players = playerList.elements();
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
