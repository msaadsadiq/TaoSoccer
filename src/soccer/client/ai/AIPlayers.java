/* AIPlayers.java
   The AI players start program

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

package soccer.client.ai;

import java.io.*;
import java.util.*;
import java.net.*;
import soccer.common.*;

public class AIPlayers
{

  static InetAddress address;
  static String host = "localhost";
  static int port = 7777;
  
  static int left = 0;
  static int right = 0;
  
  Vector robots = new Vector();

  private InitData       init = null;

  public AIPlayers()
  {
    try
    {
      address = InetAddress.getByName(host);
    }
    catch(Exception e)
    {
      System.out.println("Network error:" + e);
      System.exit(1);
    }
    for(int i=0; i<left; i++) init(true, i);
    for(int i=0; i<right; i++) init(false, i);
  } 
 
  // initialize the robot 
  private void init(boolean left, int role)
  {
    try
    {
      Transceiver transceiver = new Transceiver(false);

      // Send the connect packet to server
      ConnectData connect;
      if(left) connect = new ConnectData(ConnectData.PLAYER, ConnectData.LEFT);
      else connect = new ConnectData(ConnectData.PLAYER, ConnectData.RIGHT);
      Packet packet = new Packet(Packet.CONNECT, connect, address, port);
      transceiver.send(packet);
	
      // wait for the connect message from server
      transceiver.setTimeout(1000);
      int limit = 0;
      packet = null;
      while(limit<60) try
      {
        packet = transceiver.receive();
	break;
      }
      catch(Exception e) {limit++;}
      transceiver.setTimeout(0);
      if(packet == null) 
      {
	System.out.println("waiting for server: Timeout.");
	return;
      }
      
      if(packet.packetType == Packet.INIT)
      {
        init = (InitData) packet.data;
        Robot robot = new Robot(transceiver, init, role);
	robots.addElement(robot);
        robot.start();
  
      }
      else
      {
        System.out.println("Error: Packet type wrong. Can not INIT:-(");
        return;
      }
    }
    catch(Exception e)
    {
      System.out.println("Error during start up: " + e);
      return;
    }
  }
    
  public static void main(String argv[]) throws IOException
  {
    try
    {					
      // First look for parameters
      for( int c = 0 ; c < argv.length ; c += 2 )
      {
        if( argv[c].compareTo("-h") == 0 )
        {
          host = argv[c+1];
        }
        else if( argv[c].compareTo("-p") == 0 )
        {
          port = Integer.parseInt(argv[c+1]);
        }
        else if( argv[c].compareTo("-l") == 0 )
        {
          left = Integer.parseInt(argv[c+1]);
        }
        else if( argv[c].compareTo("-r") == 0 )
        {
          right = Integer.parseInt(argv[c+1]);
        }	
        else
        {
          throw new Exception();
        }
      }
    }
    catch(Exception e)
    {
      System.err.println("");
      System.err.println("USAGE: AIPlayers [-parameter value]");
      System.err.println("");
      System.err.println("Parameters              value              default");
      System.err.println("--------------------------------------------------");
      System.err.println("h                      host_name         localhost");
      System.err.println("p                     port_number             7777");
      System.err.println("l         number of robots joining left side     1");
      System.err.println("r         number of robots joining right side    0");      
      System.err.println("");
      System.err.println("Example:");
      System.err.println("AIPlayers -h lcivs2 -p 7777");
      System.err.println("or");
      System.err.println("AIPlayers -l 4 -r 5");
      return;
    }

    AIPlayers AIs = new AIPlayers();

  }    
}
