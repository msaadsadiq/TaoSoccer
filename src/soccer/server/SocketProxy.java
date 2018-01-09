/* 
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
/*
 *  2003/09/- Krzysztof Langner
 *  Refactored the whole class 
 */

package soccer.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import soccer.common.Ball;
import soccer.common.ConnectData;
import soccer.common.DriveData;
import soccer.common.HearData;
import soccer.common.InitData;
import soccer.common.KickData;
import soccer.common.Packet;
import soccer.common.Player;
import soccer.common.RefereeData;
import soccer.common.SeeData;
import soccer.common.TalkData;
import soccer.common.ViewData;
import soccer.server.sim.Splayer;
import soccer.server.sim.World;
import soccer.server.sim.WorldListener;



/**
 * This clas is responsible for talking with external clients.
 * 
 * @author Krzysztof Langner
 */
public class SocketProxy implements HostListener, WorldListener
{
  //---------------------------------------------------------------------------
  /**
   * Constructor
   */
  public SocketProxy(Host host, World soccerWorld) 
  {
    this.host = host;
    this.soccerWorld = soccerWorld;
    
    host.addListener(this);
    soccerWorld.addListener(this);

    // Set up available ID numbers 
    leftAvailable = new Stack();
    rightAvailable = new Stack();

    for(int i=TEAM_FULL;i>=1;i--)
    {
      Integer num = new Integer(i);
      leftAvailable.push(num);
      num = new Integer(i);
      rightAvailable.push(num); 
    }

  }  

  //---------------------------------------------------------------------------
  /**
   * @see soccer.server.HostListener#onPacketReceived(soccer.common.Packet)
   */
  public void onPacketReceived(Packet packet) {
    
    if(packet.packetType == Packet.CONNECT) 
      connectClient(packet);
    else 
      setAction(packet);
  }


  //---------------------------------------------------------------------------
  /**
   * @see soccer.server.ServerListener#modelChanged()
   */
  public void modelChanged() {

    try{
      sensing();
      broadcasting();
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }


  //---------------------------------------------------------------------------
  /**
   * @see soccer.server.WorldListener#refereeSignal()
   */
  public void refereeSignal() {
    
    try{
      // fill the referee data      
      int period = Integer.parseInt(soccerWorld.getGamePeriod().getStringView());
      RefereeData referee = new RefereeData(soccerWorld.getTickCount(), period, soccerWorld.mode,                           
        soccerWorld.getTeamName(true), soccerWorld.getScore(true), 
        soccerWorld.getTeamName(false), soccerWorld.getScore(false));
      Packet refereePacket = new Packet(Packet.REFEREE, referee);
  
      sending(refereePacket);
    }
    catch(IOException e){
      e.printStackTrace();
    }
  }

  //---------------------------------------------------------------------------
  // send packet to clients
  public void sending(Packet p) throws IOException
  {
    // send the packet to clients
    Enumeration   e;
    PlayerProxy   proxy;
    ViewerProxy   viewer;

    e = proxyPlayers.elements();
    while(e.hasMoreElements())
    {
      proxy = (PlayerProxy) e.nextElement();
      p.address = proxy.address;
      p.port = proxy.port;
      host.send(p);
    }
    

    e = viewers.elements();
    while(e.hasMoreElements())
    {
      viewer = (ViewerProxy) e.nextElement();
      p.address = viewer.address;
      p.port = viewer.port;
      host.send(p);
    } 
    
  }


  //---------------------------------------------------------------------------
  /**
   * send audio information to clients
   */
  public void broadcasting() throws IOException
  {
    Enumeration gamers = null;
    Splayer player = null;
    
    // fill the hear data and send it out
    gamers = soccerWorld.getPlayers();
    while(gamers.hasMoreElements())
    {
      player = (Splayer) gamers.nextElement();
      if(player.message != null && player.noWord == 0)
      {
        HearData hear = new HearData(soccerWorld.getTickCount(), player.side, player.id, player.message);
        Packet hearPacket = new Packet(Packet.HEAR, hear);
        // player can not speak in NOWORD sec
        player.noWord = (int)(Splayer.NOWORD / 0.1);
        player.message = null;
	      sending(hearPacket);
        return;
      }
    }
    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * send visual information to clients
   */
  public void sensing() throws IOException
  {
    // loop parameters
    Enumeration   e;
    PlayerProxy   proxy;
    ViewerProxy   viewer = null;

    
    // create an empty see packet
    Packet seePacket = new Packet();

    // create an empty view packet
    Packet viewPacket = new Packet();    
    
    // create an empty see data     
    SeeData see = new SeeData();
    see.time = soccerWorld.getTickCount();
    see.status = 0;

    // create an empty view data     
    ViewData view = new ViewData();
    view.time = soccerWorld.getTickCount();
    
    // set up ball data
    Ball b = soccerWorld.getBall().getBall();
    if(soccerWorld.getBall().free) 
    {
      b.controllerType = 'f';
      b.controllerId = 0;
    }
    
    // This section must be synchonized with 
    // adding and removing players
    synchronized(proxyPlayers){
     
      // create a player vector
      Vector all = new Vector();
      e = proxyPlayers.elements();
      while(e.hasMoreElements())
      {
        proxy = (PlayerProxy)e.nextElement();
        Player pp = proxy.player.getPlayer();
        all.add(pp);
      }
  
      e = proxyPlayers.elements();
      int i = 0;
      while(e.hasMoreElements())
      {
        proxy = (PlayerProxy) e.nextElement();
        
        see.player = (Player) all.remove(i);
        see.status = soccerWorld.getOffsideStatus(proxy.player);
        
        see.ball = b;
        
        see.playerList = all;
  
        
        seePacket.packetType = Packet.SEE;
        seePacket.data = see;
        seePacket.address = proxy.address;
        seePacket.port = proxy.port;
        host.send(seePacket);
  
        all.add(i, see.player);
        i++;
      }
    }
  }

 
  //---------------------------------------------------------------------------    
  private ViewerProxy getViewer(InetAddress address, int port)
  {
    Enumeration observers = viewers.elements();
    ViewerProxy viewer = null; 
    while(observers.hasMoreElements())
    {
      viewer = (ViewerProxy) observers.nextElement();
      if(viewer.port == port && viewer.address.equals(address)) 
        return viewer;
    }
    return null;            
  }
 
 
  //---------------------------------------------------------------------------
  /**
   * This function is called to connect player or viewer to the server.
   * @param packet Packet with connection request
   */
  private void connectClient(Packet packet)
  {
    ConnectData cd = (ConnectData)packet.data;
    InetAddress address = packet.address;
    int port = packet.port;
  
    try{

      if(cd.clientType == ConnectData.VIEWER)
      {
        // if this viewer already connected
        if(getViewer(address, port) != null) 
          return;
        
        ViewerProxy viewer = new ViewerProxy(address, port);
        viewers.addElement(viewer);
        // reply the client, send the player team and number back.
        InitData init = new InitData(InitData.VIEWER, 1);
        Packet initPacket = new Packet(Packet.INIT, init, address, port);
        host.send(initPacket);
      }
      else if(cd.clientType == ConnectData.PLAYER)
      {
        // if this player already connected
        if(getPlayer(address, port) != null) 
          return;
           
        int playerId;
        if(cd.sideType == ConnectData.LEFT)
        {
          playerId = getLeftPlayerId();

          if(playerId >0)
          {
            createPlayer(address, port, 'l', playerId);
          }
          else{
            notAvailable(address, port); 
          }
        }
        else if(cd.sideType == ConnectData.RIGHT)
        {
          playerId = getRightPlayerId();
          if(playerId >0)
          {
            createPlayer(address, port, 'r', playerId);
          }
          else 
            notAvailable(address, port); // no ID available
        }
        else if(cd.sideType == ConnectData.ANYSIDE)
        {
          if(rightAvailable.size() >= leftAvailable.size())
          {
            if(rightAvailable.size() > 0)
              createPlayer(address, port, 'r', getRightPlayerId());
            else 
              notAvailable(address, port); // no ID available
          }
          else 
          {
            createPlayer(address, port, 'l', getLeftPlayerId());
          }
        }
      }
    }
    catch(IOException e){
      // Client was not connected
      e.printStackTrace();
    }
  } 
  
  
  //---------------------------------------------------------------------------
  /**
   * Create new player
   */
  private void createPlayer(InetAddress address, int port, char side, int id)
    throws IOException
  {
    Splayer       player = new Splayer(side, id);
    PlayerProxy   proxy = new PlayerProxy(address, port, player);
    
    synchronized(proxyPlayers){
      soccerWorld.addPlayer(player);
      proxyPlayers.add(proxy);
    }
    
    // reply the client, send the player team and number back.
    InitData init;
    
    if(side == 'l')
      init = new InitData(InitData.LEFT, id);
    else
    init = new InitData(InitData.RIGHT, id);
    Packet initPacket = new Packet(Packet.INIT, init, address, port);
    host.send(initPacket);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Remove player from simulation
   */
  private void removePlayer(PlayerProxy proxy){
    
    synchronized(proxyPlayers){
      proxyPlayers.remove(proxy);
      soccerWorld.removePlayer(proxy.player);
    }
  }
    

  //---------------------------------------------------------------------------
  // reply client there's no spot available
  private void notAvailable(InetAddress address, int port) throws IOException
  {
    InitData init = new InitData(InitData.FULL, 0);
    Packet initPacket = new Packet(Packet.INIT, init, address, port);
    host.send(initPacket);
  }
  

  //---------------------------------------------------------------------------  
  /**
   * Execute action form client
   * @param packet
   */
  private void setAction(Packet packet)
  {
    InetAddress address = packet.address;
    int         port = packet.port;
    
    
    if(packet.packetType == Packet.BYE)
    {
      PlayerProxy proxy = getPlayer(address, port);
      if(proxy != null)
      {
        if(proxy.player.side == 'l')
        {
          removePlayer(proxy);
          returnLeftPlayerId(proxy.player.id);
        }
        else
        {
          removePlayer(proxy);
          returnRightPlayerId(proxy.player.id);
        }
      }
      else
      {
        ViewerProxy viewer = getViewer(address, port);
        if(viewer != null)
          viewers.remove(viewer);
        else 
          return;
      }
    }
    else
    {
      PlayerProxy proxy = getPlayer(address, port);
      if(proxy == null) 
        return;
      
      if(packet.packetType == Packet.DRIVE)
      {
        DriveData drive = (DriveData)packet.data;

        drive.normalize();
        proxy.player.dash(drive.force, drive.dir);
  
      }
      else if(packet.packetType == Packet.KICK)
      {
        KickData kickData = (KickData)packet.data;

        kickData.normalize();
        proxy.player.kickBall(kickData.force, kickData.dir);
  
      }
      else if(packet.packetType == Packet.TALK)
      {
        TalkData talk = (TalkData)packet.data;
        synchronized(proxy) 
        {
          if(talk.message.length() > 30)
          {
            proxy.player.message = talk.message.substring(0, 29);
          }
          else 
            proxy.player.message = talk.message;
        }
      }
      
    }
  }      


  //---------------------------------------------------------------------------
  // get a left player Id for the new connected player   
  private int getLeftPlayerId()
  {
    int Id;
    try
    {
      Id = ((Integer)leftAvailable.pop()).intValue();
    }
    catch(EmptyStackException e)
    {
      Id = 0;
    }

    return Id;
  } 
    
    
  //---------------------------------------------------------------------------    
  // return a left player Id   
  private void returnLeftPlayerId(int id)
  {
    Integer num = new Integer(id);
    leftAvailable.push(num);
  }
  
  
  //---------------------------------------------------------------------------  
  // get a right player Id for the new connected player  
  public int getRightPlayerId()
  {
    int Id;
    
    try
    {
      Id = ((Integer)rightAvailable.pop()).intValue();
    }
    catch(EmptyStackException e)
    {
      Id = 0;
    }

    return Id;
   }


  //---------------------------------------------------------------------------
  // return a right player Id   
  private void returnRightPlayerId(int id)
  {
    Integer num = new Integer(id);
    rightAvailable.push(num);
  }   


  //---------------------------------------------------------------------------
  /**
   * Find player from the given address.
   * Check all proxies and return player connected with it.
   * @param address player address
   * @param port    player port
   * @return found player on null
   */
  public PlayerProxy getPlayer(InetAddress address, int port)
  {
    Enumeration e;
    PlayerProxy proxy = null;

    e = proxyPlayers.elements();
      
    while(e.hasMoreElements())
    {
      proxy = (PlayerProxy)e.nextElement();
        
      if(proxy.port == port && proxy.address.equals(address)) 
        return proxy;
    }
  
    return null;
  }
    
  //---------------------------------------------------------------------------
  // Private members
  /** the maximum number of clients for each team */
  public static final int TEAM_FULL     = 11; 
  /** World object */
  private World             soccerWorld = null;
  /** Host object */
  private Host              host;
  /** ticker */
  private int               ticker = 0;
  /** Viewers */   
  private Vector viewers = new Vector();
  /** Proxy players */
  private Vector  proxyPlayers = new Vector();
  // Available Id numbers for future players
  private Stack leftAvailable = new Stack();
  private Stack rightAvailable = new Stack();


}
