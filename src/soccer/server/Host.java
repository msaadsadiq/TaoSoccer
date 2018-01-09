/* Host.java

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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Enumeration;
import java.util.Vector;

import soccer.common.Packet;

/*
  * The Host thread waits for clients' data packet and put them
  * in players and viewers.
  */

public class Host extends Thread 
{
  //---------------------------------------------------------------------------
  public Host(int port) 
  {
    this.port = port;
  } 
  
  
  //---------------------------------------------------------------------------
  /**
   * Send this packet
   * @packet packet to send
   */
  public void send(Packet p) throws IOException{
    
    byte[] buffer = p.writePacket().getBytes();
    DatagramPacket packet = 
      new DatagramPacket(buffer, buffer.length, p.address, p.port);
      
    socket.send(packet);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * add listener
   */
  public void addListener(HostListener l){
    listeners.add(l);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Remove listener
   */
  public void removeListener(HostListener l){
    listeners.remove(l);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Listen to socket and broadcast received messages
   */  
  public void run() 
  {
    try
    {
      socket = new DatagramSocket(port);
      byte[] buffer = new byte[BUFFER_SIZE];

      while(true) 
      {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
    
        String message = new String(buffer);
        Packet p = new Packet();
        p.readPacket(message);
        p.address = packet.getAddress();
        p.port = packet.getPort();

        packetReceived(p);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }


  //---------------------------------------------------------------------------
  /**
   * This function broadcasts received packet
   * @param p Packet which was received
   */
  private void packetReceived(Packet p){
    
    Enumeration e = listeners.elements();
    
    while(e.hasMoreElements()){
      
      HostListener l = (HostListener)e.nextElement();
      l.onPacketReceived(p);
    }
  }
  
  
  //---------------------------------------------------------------------------
  // Private members
  private static final int BUFFER_SIZE = 1024;
  /** On this port server will listen to clients */
  private int         port;
  /** List of listeners */
  private Vector      listeners = new Vector();
  /** Host socket */  
  private DatagramSocket    socket;
}
