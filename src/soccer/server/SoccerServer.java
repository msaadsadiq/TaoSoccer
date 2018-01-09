/* SoccerServer.java

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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import soccer.server.sim.World;
import soccer.server.sim.Splayer;
import soccer.server.sim.Sball;



/*
 * SoccerServer
 *
 * The SoccerServer runs three main threads.  The first
 * is the Console Thread.  It gives you a simple
 * interface that responds to text commands, and quit.
 * The second is the Host Thread.  It creates a
 * SocketServer and waits...  The last is the HeartOfWorld Thread
 * that operates the main logic functions of the game.
 */

public class SoccerServer
{

  //---------------------------------------------------------------------------
  /**
   * constructor
   */
  public SoccerServer()
  {
    // Initialize the application.
    soccerWorld = new World();

    host = new Host(7777);
    heartOfWorld = new SocketProxy(host, soccerWorld);
  } 
  
  
  //---------------------------------------------------------------------------
  /**
   * Initialize the application, then drop into a console loop
   * until it's time to quit.
   */
  public static void main(String args[]) 
  {
    Properties properties = null;
    boolean offside = true;
    boolean log = false;
    String  configFileName = "./properties";
    int     port = 7777;
    
    try
    {         
      // First look for parameters
      for( int c = 0 ; c < args.length ; c += 2 )
      {
        if(args[c].compareTo("-pf") == 0)
        {
          configFileName = args[c+1];
        }
        else if(args[c].compareTo("-po") == 0 )
        {
          port = Integer.parseInt(args[c+1]);  
        }
        else if(args[c].compareTo("-no") == 0 )
        {
          offside = false;  
        }
        else if(args[c].compareTo("-lo") == 0 )
        {
          log = true; 
        } 
        else
        {
          throw new Exception();
        }
      }
      
      File file = new File(configFileName);
      if(file.exists()){
        System.out.println("Load properties from file: " + configFileName);
        properties = new Properties();
        properties.load(new FileInputStream(configFileName));
      } 

    }
    catch(Exception e)
    {
      System.out.print("\n");
      System.out.print("\nSServer:USAGE: java SoccerServer [-parameter value]");
      System.out.print("\nSServer:");
      System.out.print("\nSServer:Parameters    value           default");
      System.out.print("\nSServer:-------------------------------------");
      System.out.print("\nSServer:pf   property_file_name  ./properties");
      System.out.print("\nSServer:po        port number            7777");
      System.out.print("\nSServer:no              /                 /   (means no offside)");
      System.out.print("\nSServer:");
      System.out.print("\nSServer:Example:");
      System.out.print("\nSServer:java SoccerServer -no -pf properties");
      return;
    }
        
    SoccerServer server = new SoccerServer();
    
    if(properties != null)
      server.setProperties(properties);

    server.init(port);

  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Initialize server
   */
  public void init(int port){
    
    System.out.println("SoccerServer ver " + VERSION);

    // Heart of World begins to pump 'blood'
    soccerWorld.start();

    // Host begins to take care of visitors
    host.start();

  }
   
   
  //---------------------------------------------------------------------------
  /**
   * @return world object
   */
  public World getWorld(){
    
    return soccerWorld;  
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * set properties
   */
  public void setProperties(Properties properties)
  {
    
    soccerWorld.initFromProperties(properties);
    
    Splayer.MAXDASH = Double.parseDouble(properties.getProperty("maxdash", 
                                                String.valueOf(Splayer.MAXDASH)));

    Splayer.MINDASH = Double.parseDouble(properties.getProperty("mindash", 
                                                String.valueOf(Splayer.MINDASH)));

    Splayer.MAXKICK = Double.parseDouble(properties.getProperty("maxkick", 
                                                String.valueOf(Splayer.MAXKICK)));

    Splayer.MINKICK = Double.parseDouble(properties.getProperty("minkick", 
                                                String.valueOf(Splayer.MINKICK)));

    Splayer.RANDOM = Double.parseDouble(properties.getProperty("random_p", 
                                                String.valueOf(Splayer.RANDOM)));

    Splayer.KICKRANDOM = Double.parseDouble(properties.getProperty("kick_random", 
                                                String.valueOf(Splayer.KICKRANDOM)));
            
    Splayer.DRIBBLEFACTOR = Double.parseDouble(properties.getProperty("dribble_factor", 
                                                String.valueOf(Splayer.DRIBBLEFACTOR)));
            
    Splayer.NOWORD = Double.parseDouble(properties.getProperty("noword", 
                                                String.valueOf(Splayer.NOWORD)));

    Splayer.COLLIDERANGE = Double.parseDouble(properties.getProperty("collide_range", 
                                                String.valueOf(Splayer.COLLIDERANGE)));             
    Splayer.INERTIA = Integer.parseInt(properties.getProperty("inertia", 
                                                String.valueOf(Splayer.INERTIA)));
            
    Sball.CONTROLRANGE = Double.parseDouble(properties.getProperty("control_range", 
                                                String.valueOf(Sball.CONTROLRANGE)));

    Sball.RANDOM = Double.parseDouble(properties.getProperty("random_b", 
                                                String.valueOf(Sball.RANDOM)));           


  }
  
  
  //---------------------------------------------------------------------------
  // Private members
  private static final String VERSION = "1.3";
  /** The soccerWorld simulates the soccer game environment. */
  private World soccerWorld = null;
  /** 
   * The HeartOfWorld operates the main logic of the game, 
   * and sends out sensing packets. 
   */
  private SocketProxy heartOfWorld = null;
  /** The Host receives incoming packets. */
  private Host host = null;
  
}
