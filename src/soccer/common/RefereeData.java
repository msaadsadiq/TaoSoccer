/* RefereeData.java

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
 * Provides referee data for server informing all clients.
 *
 * @author Yu Zhang
 */
public class RefereeData implements Data
{

  /**
   * play period string array for displaying. 
   * periods[PRE_GAME] = "preGame", periods[FIRST_HALF] = "firstHalf",
   * periods[HALF_TIME] = "halfTime", periods[SECOND_HALF] = "secondHalf".
   */
  public static final String[] periods = {"preGame", "firstHalf",
                                          "halfTime", "secondHalf"};
  /**
   * play period PRE_GAME identifier.
   */
  public static final int PRE_GAME     = 0;
  /**
   * play period FIRST_HALF identifier.
   */
  public static final int FIRST_HALF   = 1;
  /**
   * play period HALF_TIME identifier.
   */
  public static final int HALF_TIME    = 2;
  /**
   * play period SECOND_HALF identifier.
   */
  public static final int SECOND_HALF  = 3;

  
  /**
   * play mode string array for displaying. 
   * periods[PRE_GAME] = "preGame", periods[FIRST_HALF] = "firstHalf",
   * periods[HALF_TIME] = "halfTime", periods[SECOND_HALF] = "secondHalf".
   */
  public static final String[] modes = {"beforeKickOff", "kickOffL", "kickOffR",
                                        "throwInL", "throwInR", "cornerKickL",
					"cornerKickR", "goalKickL", "goalKickR",
					"offsideL", "offsideR", "playOn"};

  /**
   * play mode BEFORE_KICK_OFF identifier.
   */
  public static final int BEFORE_KICK_OFF = 0;
  /**
   * play mode KICK_OFF_L identifier.
   */
  public static final int KICK_OFF_L      = 1;
  /**
   * play mode KICK_OFF_R identifier.
   */
  public static final int KICK_OFF_R      = 2;
  /**
   * play mode THROW_IN_L identifier.
   */
  public static final int THROW_IN_L      = 3;
  /**
   * play mode THROW_IN_R identifier.
   */
  public static final int THROW_IN_R      = 4;
  /**
   * play mode CORNER_KICK_L identifier.
   */
  public static final int CORNER_KICK_L   = 5;
  /**
   * play mode CORNER_KICK_R identifier.
   */
  public static final int CORNER_KICK_R   = 6;
  /**
   * play mode GOAL_KICK_L identifier.
   */
  public static final int GOAL_KICK_L     = 7;
  /**
   * play mode GOAL_KICK_R identifier.
   */
  public static final int GOAL_KICK_R     = 8;
  /**
   * play mode OFFSIDE_L identifier.
   */
  public static final int OFFSIDE_L       = 9;
  /**
   * play mode OFFSIDE_R identifier.
   */
  public static final int OFFSIDE_R       = 10;
  /**
   * play mode PLAY_ON identifier.
   */
  public static final int PLAY_ON         = 11;

  /**
   * the current simulation step.
   */
  public int time;
  /**
   * the current game period.
   */
  public int period;
  /**
   * the current game mode.
   */
  public int mode;
  /**
   * left team name.
   */
  public String leftName;
  /**
   * the score of the left team.
   */
  public int goal_l;
  /**
   * right team name.
   */
  public String rightName;
  /**
   * the score of the right team.
   */
  public int goal_r;

  /**
   * Constructs an empty RefereeData for reading from an UDP packet.
   */
  public RefereeData()
  {
    this.time = 0;
    this.period = 0;
    this.mode = 0;
    this.leftName = "";
    this.goal_l = 0;
    this.rightName = "";
    this.goal_r = 0;
  }
  
  /** 
   * Constructs a RefereeData for writeing to an UDP packet.
   *
   * @param time the current simulation step.
   * @param period the current game period.
   * @param mode the current game mode.
   * @param leftName the left team name.
   * @param goal_l the score of the left team.
   * @param rightName the right team name.
   * @param goal_r the score of the right team.
   */
  public RefereeData(int time, int period, int mode, 
                     String leftName, int goal_l, String rightName, int goal_r)
  {
    this.time = time;
    this.period = period;
    this.mode = mode;
    this.leftName = leftName;
    this.goal_l = goal_l;
    this.rightName = rightName;
    this.goal_r = goal_r;
  
  } 
  
  // Load its data content from a string.
  public void readData(StringTokenizer st)
  {
    // Get the time.
    time = Integer.parseInt(st.nextToken()); 
    
    // Get the " "
    st.nextToken();         

    // Get the period.
    period = Integer.parseInt(st.nextToken());
    
    // Get the " "
    st.nextToken();         

    // Get the mode.
    mode = Integer.parseInt(st.nextToken());
    
    // Get the " "
    st.nextToken();         
    
    // Get the left team name.
    leftName = st.nextToken();
    
    // Get the " "
    st.nextToken();         
    
    // Get the left team's score.
    goal_l = Integer.parseInt(st.nextToken());
    
    // Get the " "
    st.nextToken();         

    // Get the right team name.
    rightName = st.nextToken();
    
    // Get the " "
    st.nextToken();         

    // Get the right team's score.
    goal_r = Integer.parseInt(st.nextToken()); 
    
  } 
  
  // Stream its data content to a string.
  public void writeData(StringBuffer sb)
  {
    sb.append(Packet.REFEREE);
    sb.append(' ');
    sb.append(time);
    sb.append(' ');
    sb.append(period);
    sb.append(' ');
    sb.append(mode);
    sb.append(' ');
    sb.append(leftName);
    sb.append(' ');
    sb.append(goal_l);
    sb.append(' ');
    sb.append(rightName);
    sb.append(' ');
    sb.append(goal_r);
  } 
  
}
