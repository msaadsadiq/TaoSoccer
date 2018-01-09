/* World.java
   This class simulates the rules used in Soccer Game
   
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import soccer.common.RefereeData;
import soccer.common.Vector2d;


/**
 * This class is responsible for simulation of the all
 * objects on the field.
 * 
 * @author Krzysztof Langner
 */
public class World extends Thread 
{
  // Soccer Field data (in meter)
  public static final double LENGTH         = 100;
  public static final double WIDTH          = 65;
  public static final double SIDEWALK       = 5;
  public static final double RADIUS         = 9;
  public static final double GOAL_DEPTH     = 2;
  public static final double GOAL_WIDTH     = 8;
  public static final double GOALAREA_WIDTH = 18;
  public static final double GOALAREA_DEPTH = 6;
  public static final double PENALTY_WIDTH  = 40;
  public static final double PENALTY_DEPTH  = 16;  
  public static final double PENALTY_CENTER = 12;
  public static final double CORNER         = 1;

  // Sides
  public static final int SIDE_LEFT = 1;
  public static final int SIDE_RIGHT = 2;

  // Game status
  public int mode = RefereeData.BEFORE_KICK_OFF;

  //---------------------------------------------------------------------------
  /**
   * Constructor
   */
  public World()
  {
    // initialize the ball
    ball = new Sball();
    gamePeriod = new GamePeriod(200, 5*600);
    kickOffSide = SIDE_LEFT;
    checkOffside = true;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Set world simulation parameters
   */
  public void initFromProperties(Properties p){
    
    int waitTime;
    int halfTime;
    
    leftTeamName = p.getProperty("left_name", leftTeamName);
    rightTeamName = p.getProperty("right_name", rightTeamName);
    
    stepLength = (int)(1000*Double.parseDouble(p.getProperty("heartrate", "0.1")));
    waitTime = (int)(10*Integer.parseInt(p.getProperty("wait_time", "20")));
    halfTime = (int)(10*Integer.parseInt(p.getProperty("half_time", "300")));
    checkOffside = (Boolean.getBoolean(p.getProperty("offside", "true")));
    
    gamePeriod = new GamePeriod(waitTime, halfTime);
            
  }
    
    
  //---------------------------------------------------------------------------
  /**
   * add listener
   */
  public void addListener(WorldListener l){
    listeners.add(l);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Remove listener
   */
  public void removeListener(WorldListener l){
    listeners.remove(l);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * This function returns game state object
   * @return game state
   */
  public ModelItem getGamePeriod(){
    
    return gamePeriod;
  }

  
  //---------------------------------------------------------------------------
  /**
   * @return number of ticks from the begining of the simulation 
   */
  public int getTickCount(){
    
    return ticker;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Get offside status
   * @player check offside status for this player
   * @return
   *  0 no offside
   *  1 this player is in offside position
   *  2 teammate is on offside position
   */
  public int getOffsideStatus(Splayer player){
    
    if(lastKickedPlayer != null && lastKickedPlayer.isTeammate(player)){
      
      if(offsidePlayers.contains(player))
        return 1;
      else if(offsidePlayers.size() > 0)
        return 2;
    }
    
    return 0;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Return team name
   * @param left return left team?
   * @return left team if left true right team if false
   */
  public String getTeamName(boolean left){
    
    if(left)
      return leftTeamName;
    else
      return rightTeamName;
  }

  
  //---------------------------------------------------------------------------
  /**
   * Return team score
   * @param left return left team?
   * @return left team if left true right team if false
   */
  public int getScore(boolean left){
    
    if(left)
      return scoreLeft;
    else
      return scoreRight;
  }

  
  //---------------------------------------------------------------------------
  /**
   * Main simulator function
   */
  public void run() 
  {
    int inertia = 0;

    try 
    {
      while( true ) 
      {
        // get the time before the step
        long timeBefore = System.currentTimeMillis();

        if(inertia == 0) 
          setPlayerAction();
        
        inertia ++;
        if(inertia == Splayer.INERTIA) 
          inertia = 0;
  
        ticker ++;
 
        onSimulationStep();

        // figure out how long it takes to process
        long timeSpent = System.currentTimeMillis() - timeBefore;

        if (timeSpent < stepLength) 
          sleep( stepLength - timeSpent );
      }
    }
    catch (Exception e) 
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Return ball
   */
  public Sball getBall(){  
    
    return ball;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * get the player by team side and Id
   */
  public Splayer getPlayer(char type, int id)
  {
    Enumeration players = null;
    Splayer player = null;

    players = playerList.elements();
    while(players.hasMoreElements())
    {
      player = (Splayer) players.nextElement();
      if(player.id == id && player.side == type) 
        return player;
    }
  
    return null;

  }


  //---------------------------------------------------------------------------
  /**
   * Simulate game
   */
  private void onSimulationStep() throws IOException{
    
    if(gamePeriod.updateGamePeriod(ticker)){
      beforeKickOff();
      refereeSignal();
    }
    else if(gamePeriod.canPlay()){
      if(checkRefereeSignal()){
        updatePlayersPosition();
        refereeSignal();
      }
      else{
        moveObjects();
      }
    }
    else{
      moveObjects();
    }

    notifyModelChanged();
    
  } 
    
    
  //---------------------------------------------------------------------------
  /**
   * Ask referee about game state
   * @return true if there is referee signal
   */
  private boolean checkRefereeSignal()
  {
    if(mode == RefereeData.BEFORE_KICK_OFF)
    {
      // "before_kick_off" lasts for 2 sec
      timer++;
      if(timer > 20) // "before_kick_off" ends
      {
        timer = 0;
        if(kickOffSide == SIDE_LEFT)
        {
          mode = RefereeData.KICK_OFF_L;
          rightOff = true;
          leftOff = false;
        }
        else
        {
          mode = RefereeData.KICK_OFF_R;
          rightOff = false;
          leftOff = true;                
        }
        
        ball.set(0,0);
        return true;
      }
    }
    else if(mode == RefereeData.OFFSIDE_L){

      mode = RefereeData.KICK_OFF_R;
      rightOff = false;
      leftOff = true;
      return true;                
    }
    else if(mode == RefereeData.OFFSIDE_R){

      mode = RefereeData.KICK_OFF_L;
      rightOff = true;
      leftOff = false;
      return true;                
    }
    else if(mode ==RefereeData.PLAY_ON)
    {
      double x = ball.getPosition().getX();
      double y = ball.getPosition().getY();
      
      if(x < - LENGTH / 2)
      {
        if(Math.abs(y) < GOAL_WIDTH / 2) // right team scores
        {
          scoreRight ++; 
          mode = RefereeData.BEFORE_KICK_OFF;
          kickOffSide = SIDE_LEFT;
          timer = 0;
          ball.set(0,0);
          rightOff = true;
          leftOff = true;
        }
        else if(lastKickedPlayer.side == 'l') // right team corner kick
        {
          mode = RefereeData.CORNER_KICK_R;
          if(y > 0) 
            ball.set(-LENGTH/2, WIDTH/2);
          else 
            ball.set(-LENGTH/2, -WIDTH/2);
          rightOff = false;
          leftOff = true;
        }
        else if(lastKickedPlayer.side == 'r') // left team goal kick
        {
          mode = RefereeData.GOAL_KICK_L;
          if(y > 0) 
            ball.set(-LENGTH/2 + GOALAREA_DEPTH, GOALAREA_WIDTH/2);
          else 
            ball.set(-LENGTH/2 + GOALAREA_DEPTH, -GOALAREA_WIDTH/2);
          rightOff = true;
          leftOff = false;
        }
        
        return true;        
      }
      else if(x >  LENGTH / 2)
      {
        if(Math.abs(y) < GOAL_WIDTH / 2) // left team scores
        {
          scoreLeft ++; 
          mode = RefereeData.BEFORE_KICK_OFF;
          kickOffSide = SIDE_RIGHT;
          timer = 0;
          ball.set(0,0);
          rightOff = true;
          leftOff = true;
        }
        else if(lastKickedPlayer.side == 'l') // right team goal kick
        {
          mode = RefereeData.GOAL_KICK_R;
          if(y > 0) 
            ball.set(LENGTH/2 - GOALAREA_DEPTH, GOALAREA_WIDTH/2);
          else 
            ball.set(LENGTH/2 - GOALAREA_DEPTH, -GOALAREA_WIDTH/2);
          rightOff = false;
          leftOff = true;
        }
        else if(lastKickedPlayer.side == 'r') // left team corner kick
        {
          mode = RefereeData.CORNER_KICK_L;
          if(y > 0) 
            ball.set(LENGTH/2, WIDTH/2);
          else 
            ball.set(LENGTH/2, -WIDTH/2);
          rightOff = true;
          leftOff = false;
        }
        
        return true;    
      }      
      else if(Math.abs(y) > WIDTH/2)
      {        
        if(y > 0) 
          ball.set(x, WIDTH/2);
        else 
          ball.set(x, -WIDTH/2);

        if(lastKickedPlayer.side == 'r') // left team throw in
        {
          mode = RefereeData.THROW_IN_L;
          rightOff = true;
          leftOff = false;
        }
        else 
        {
          mode = RefereeData.THROW_IN_R;
          rightOff = false;
          leftOff = true;
        }
        
        return true;
      }
    }
    
    return false;
    
  }


  //---------------------------------------------------------------------------
  public void setPlayerAction()
  {
    // variable used to loop
    Enumeration players = null;
    Splayer player = null;
      
    // for each player set up their actions
    players = playerList.elements();
    while(players.hasMoreElements())
    {
      player = (Splayer) players.nextElement();
      player.setPlayerAction();      
    }
    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return enumeration with left team players
   */
  public Enumeration getPlayers(){
    
    return playerList.elements();
  }


  //---------------------------------------------------------------------------
  /**
   * Add new player to simulation.
   */
  public void addPlayer(Splayer player){

    playerList.add(player);
  }


  //---------------------------------------------------------------------------
  /**
   * Remove player from simulation.
   */
  public void removePlayer(Splayer player){
    playerList.remove(player);
  }
  
  
  //---------------------------------------------------------------------------
  /*
   * Prepare world to play a game
   */
  private void beforeKickOff(){
    
    mode = RefereeData.BEFORE_KICK_OFF;
    ball.set(0,0);
    leftOff = true;
    rightOff = true;
    timer = 0;
  }


  //---------------------------------------------------------------------------
  /**
   * Move objects on the field
   */ 
  private void moveObjects()
  {
    boolean collision = false;  // the flag to indicate if there's collision between players
    // variable used to loop
    Enumeration players = null;
    Splayer player = null; 
    // the player who has the ball
    Splayer ballPlayer = null;
    // a group of players trying to control the ball.
    // they are all close enough to the ball to kick it.
    // who eventually has the ball is randomly decided.
    Vector fighters = new Vector();


    // for each player, decide their position
    players = playerList.elements();
    while(players.hasMoreElements())
    {
      player = (Splayer) players.nextElement();

      // the player's position if nothing else happens
      Vector2d pos = player.nextPosition();
      Vector2d np = updatePosition(player, pos);

      // if the player collides with its teammates
      if(np == null && !isColliding(player, pos)){
        // set player's new position     
        player.getPosition().setXY(pos);
      }
      

      // if the player is close to ball enough to be a potential ball controller
      if(ball.canControl(player))
        fighters.addElement(player);

    }
      
    // Get last ball's controller
    if(!ball.free)
      ballPlayer = getPlayer(ball.controllerType, ball.controllerId); 

    if(ballPlayer != null)
    {
      if(ballPlayer.isKickingBall())
      {
        // the player is kicking the ball
        fighters.removeElement(ballPlayer);

        double kickDir = 2 * (Math.random() - 0.5) * Splayer.KICKRANDOM + ballPlayer.getDirection();
        Vector2d p = ball.nextPosition(ballPlayer.getForce(), kickDir);
        ball.getPosition().setXY(p.getX(), p.getY());
        ballKicked(ballPlayer);
      }
      else
      {
        ball.updatePosition();
      }

      ballPlayer.setHaveBall(false);
    }
    else // the ball is under nobody's control
    {
      ball.updatePosition();
    }

    // check if ball collide
    if(isColliding(null, ball.getPosition())){
      ball.collide();
    }
    
    // find out who has controlled the ball at last
    int size = fighters.size();
    if (size > 0)
    {
      int controller = (int) Math.floor(Math.random()*size);
      player = (Splayer) fighters.elementAt(controller);
      ball.controllerType = player.side;
      ball.controllerId = player.id;
      ball.free = false;
      player.setHaveBall(true);
          
    }   
    else
    {
      ball.free = true;
    }
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Update next player position.
   * Check if player can move to this position
   * @param player player which want to move
   * @param pos position to move
   * @return null if player can move to new position
   *  or corrected position if it can't
   */
  private Vector2d updatePosition(Splayer player, Vector2d pos){

    boolean   keepOff = false;
    Vector2d  newPos = null;
    Vector2d  ballPos = ball.getPosition();
    double    dis = ballPos.distance(pos);
    

    if((player.side == 'l' && leftOff) || (player.side == 'r' && rightOff))
      keepOff = true;
    
    // If we are before kick off then all players should return to its own side
    if(mode == RefereeData.PRE_GAME){
      if(player.side == 'l'){
        if(pos.getX() >= 0)
          newPos = new Vector2d(-20, pos.getY());
      }
      else{
        if(pos.getX() <= 0)
        newPos = new Vector2d(20, pos.getY());
      }
    }
    
    // if the player is not allowed to move close to ball
    if(dis < RADIUS && keepOff) 
    {
      double moveDist = RADIUS-dis;
      newPos = new Vector2d(pos);
      
      if(pos.getX() < ballPos.getX()){
        if(pos.getX()-moveDist > -LENGTH/2)
          newPos.addX(-moveDist);
        else
          newPos.addX(moveDist+RADIUS);
      }
      else{
        if(pos.getX()+moveDist < LENGTH/2)
          newPos.addX(moveDist);
        else
          newPos.addX(-moveDist-RADIUS);
      }
    }
  
    // if the player is moving outside of the soccer field
    if(Math.abs(pos.getX()) > LENGTH/2 + SIDEWALK - 1) 
    {
      newPos = new Vector2d(pos);
      if(pos.getX() > 0) 
        newPos.setX(LENGTH/2 + SIDEWALK - 1);
      else 
        newPos.setX(-(LENGTH/2 + SIDEWALK - 1));
    }

    if(Math.abs(pos.getY()) > WIDTH/2 + SIDEWALK - 1) 
    {
      newPos = new Vector2d(pos);
      if(newPos.getY() > 0) 
        newPos.setY(WIDTH/2 + SIDEWALK - 1);
      else 
        newPos.setY(-(WIDTH/2 + SIDEWALK - 1));
    }
    
    if(mode == RefereeData.BEFORE_KICK_OFF){
    }
    
    return newPos;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Check if this player collidens with another player
   * @param player player to check
   * @return true if player collides with another player
   */
  private boolean isColliding(Splayer player, Vector2d pos){

    // if the player collides with its teammates
    Splayer player2 = null;
    Enumeration players = null;

    players = playerList.elements();
    while(players.hasMoreElements())
    {
      player2 = (Splayer) players.nextElement();
      if(player != player2 && player2.getPosition().distance(pos) < Splayer.COLLIDERANGE)
      {
        return true;
      }
    }

    return false;
    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Called after each simulation step to notify listeners
   * about changes.
   */
  private void notifyModelChanged(){
    
    Enumeration e = listeners.elements();
    
    while(e.hasMoreElements()){
      
      WorldListener l = (WorldListener)e.nextElement();
      l.modelChanged();
    }
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Called after referee signal.
   */
  private void refereeSignal(){
    
    Enumeration e = listeners.elements();
    
    while(e.hasMoreElements()){
      
      WorldListener l = (WorldListener)e.nextElement();
      l.refereeSignal();
    }
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Called after player kicked ball
   * @param player player which kicked the ball
   */
  private void ballKicked(Splayer player){
    
    lastKickedPlayer = player;
    if(mode != RefereeData.PLAY_ON)
    {
      mode = RefereeData.PLAY_ON;
      rightOff = false;
      leftOff = false;
      refereeSignal();
    }
    else if(checkOffside && isOffsidePlayer(player)){
      // Offside
      if(player.side == 'l'){
        mode = RefereeData.OFFSIDE_L;
      }
      else{
        mode = RefereeData.OFFSIDE_R;
      }
      
      ball.set(player.getPosition().getX(), player.getPosition().getY());
      refereeSignal();
    }
    else{
      markOffsidePlayers(player);
    }
    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Mark offside players
   * @param player player who kicked ball
   */
  private void markOffsidePlayers(Splayer player){
    
    Enumeration players = null;
    double      lastX = 0;
    Vector2d    pos;
    
    // Remove old offside position
    offsidePlayers.clear();
    
    if(player.side == 'l'){
     
      // find offside position
      players = playerList.elements();
      while(players.hasMoreElements())
      {
        Splayer p = (Splayer) players.nextElement();
        if(!p.isTeammate(player) && !p.isGoalkeeper()){
          
          pos = p.getPosition();
          if(pos.getX() > lastX)
            lastX = pos.getX(); 
        }
      }
      
      players = playerList.elements();
      while(players.hasMoreElements())
      {
        Splayer p = (Splayer) players.nextElement();
        if(p.isTeammate(player) && p != player){
          
          pos = p.getPosition();
          if(pos.getX() > lastX)
          offsidePlayers.add(p);
        }
      }
      
    }
    else{
      // find offside position
      players = playerList.elements();
      while(players.hasMoreElements())
      {
        Splayer p = (Splayer) players.nextElement();
        if(!p.isTeammate(player) && !p.isGoalkeeper()){
          
          pos = p.getPosition();
          if(pos.getX() < lastX)
            lastX = pos.getX(); 
        }
      }
      
      players = playerList.elements();
      while(players.hasMoreElements())
      {
        Splayer p = (Splayer) players.nextElement();
        if(p.isTeammate(player) && p != player){
          
          pos = p.getPosition();
          if(pos.getX() < lastX)
          offsidePlayers.add(p);
        }
      }
    }
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Check if given player was on the offside when the ball was kicked
   * @param player player to check
   * @return true if offside
   */
  private boolean isOffsidePlayer(Splayer player){
    
    return offsidePlayers.contains(player);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Update players position after game status changed
   */
  private void updatePlayersPosition(){

    Splayer      player;
    Enumeration players = null;

    players = playerList.elements();
    while(players.hasMoreElements())
    {
      player = (Splayer) players.nextElement();
      Vector2d pos = updatePosition(player, player.getPosition());
      if(pos != null)
        player.getPosition().setXY(pos);
    }
  }
  
  
  //---------------------------------------------------------------------------
  // Private members
  /** Simulation step in milisecons */
  private int         stepLength = 100;
  /** Listeners */
  private Vector      listeners = new Vector();  
  /** All players are in this vector */
  private Vector      playerList = new Vector();
  /** Game period */
  private GamePeriod  gamePeriod;
  /**
   * A flag to indicate that left team players must be
   * RADIUS meters away from the ball
   */
  private boolean leftOff = true;   
  /**
   * A flag to indicate that right team players must be
   * RADIUS meters away from the ball
   */
  public boolean rightOff = true;  
  /** before kick off timer */
  private short timer = 0; 
  /** Check if we should applay offside rule */
  private boolean offsideRule = true;
  /** Simulation timer */          
  private int   ticker;
  /** Ball */
  private Sball ball;
  /** Left team name */
  private String leftTeamName = "Ajax";
  /** Right team name */
  private String rightTeamName = "Arsenal";
  /** Left team score */  
  private int scoreLeft;
  /** right team score */
  private int scoreRight;
  /** Player who last kicked the ball */
  private Splayer lastKickedPlayer;
  /** Use offside rule */
  private boolean checkOffside;
  /** Kick off side */
  private int kickOffSide;
  /** Players on the offside position */
  private Vector offsidePlayers = new Vector();
  
}
