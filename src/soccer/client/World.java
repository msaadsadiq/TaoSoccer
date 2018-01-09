/* World.java
   The player's view of the soccer field.

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

import java.util.*;
import soccer.common.*;

public class World 
{

  // High Level Actions Symble
  public static final int SHOOT           = 1;
  public static final int MOVE            = 2;
  public static final int PASS            = 3;
  public static final int CHASE           = 4;
  
  // Low Level Action Symble
  public static final int DRIVE           = 5;
  public static final int KICK            = 6;

  // Once a player makes up his mind to do an action, the action will
  // be carried out for next INERTIA steps. So this means, the client
  // does not need to send a command every step, he only needs to send
  // a command every INERTIA steps.
  // This will also give a client more time to think before to actually 
  // do something.
  public static int INERTIA   =  3;

  // physical factors of the soccer world

  // Heart rate ( the time each step spends in sec)
  public static double HEARTRATE    = 0.1;
  
  // player will have chance to get the ball when ball-player distance is under control range
  public static double CONTROLRANGE   = 1.5; 
  
  // maximum ball speed in m/s
  public static double BALLMAXSPEED   = 23;

  // friction factor, such as a1 = -FRICTIONFACTOR * v0;
  public static double FRICTIONFACTOR     = 0.065;

  // player's maximum speed (in m/s)
  public static double MAXSPEED   = 7; 
  
  // the time a player needs to reach full speed (in sec)
  // without friction
  public static double TIMETOMAX  = 1;

  // player's maximum dash force 
  public static double MAXDASH  = 100;

  // player's maximum kick force 
  public static double MAXKICK  = 100;

  // max dribble force factor, when player is dribbling, 
  //the max force he can use to dash is MAXDASH * DRIBBLE 
  public static  double DRIBBLEFACTOR  = 0.4;

  // players collides when their distance is smaller than COLLIDERANGE
  public static double COLLIDERANGE = 0.6;

  // K1 is the force factor, MAXSPEED speed divided by TIMETOMAX
  // MAXDASH * K1 * TIMETOMAX * (1 / HEARTRATE) = MAXSPEED * HEARTRATE
  public static double K1 = MAXSPEED * HEARTRATE * 
                            HEARTRATE / TIMETOMAX / MAXDASH;

  // K2 is the friction factor,
  // 0 = MAXDASH * k1 + MAXSPEED * HEARTRATE * K2;
  public static double K2 = MAXDASH / (MAXSPEED * HEARTRATE) * K1; 		
  
  // BK1 is the kick force factor 
  public static double BK1     = BALLMAXSPEED * HEARTRATE / MAXKICK;

  // latest info from server
  public InitData init = null;
  public RefereeData referee = null;
  public SeeData see = null;
  public ViewData view = null;
  public HearData message = null;

  public HearData leftM = null;
  public HearData rightM = null;

  // my status
  public Player me;
  // my drive force and direction
  public double force;
  public double direction;
  // my offside status
  public int status;
  // used to store my position at previous step,
  // for calculating my velocity.
  public Vector2d prePosition = new Vector2d();
  public Vector2d myVelocity = new Vector2d();

  // my action time
  public int actTime = 0;
  
  // ball status
  public Ball ball;

  // used to store ball's position at previous step,
  // for calculating ball's velocity.
  public Vector2d ballPosition =  new Vector2d(); 
  public Vector2d ballVelocity = new Vector2d();  

  // players
  public Vector leftTeam;
  public Vector rightTeam;
  
  // high-level knowledge
  public boolean IHaveBall = false;
  public boolean myBall = false;
  public Vector2d ball2Me = null; // ball's position relative to the player
  public double distance2Ball;
  public double direction2Ball;



  public int prePeriod = -1;
  public int preMode = RefereeData.BEFORE_KICK_OFF;
  public char preController = 'f';
  public int leftGoal = 0;
  public int rightGoal = 0;
  
  
  // high level action 
  public int actionType;
  
  public Vector2d destination = new Vector2d();

  public World(InitData init)
  {
    this.init = init;  
  }

  public World()
  {
 
  }  
    

}
