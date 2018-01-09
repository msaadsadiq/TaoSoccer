/* ConnectData.java

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
 * Provides connection data for client connecting to server.
 *
 * @author Yu Zhang
 */
public class ConnectData implements Data
{

  /**
   * Connection client Type identifier 'p'.
   */
  public static final char PLAYER   = 'p';
  /**
   * Connection client Type identifier 'v'.
   */
  public static final char VIEWER   = 'v';

  /**
   * Side type 'a'.
   */
  public static final char ANYSIDE   = 'a';
  /**
   * Side type 'l'.
   */
  public static final char LEFT     = 'l';
  /**
   * Side type 'r'.
   */
  public static final char RIGHT    = 'r';

  /**
   * Describes the type of client.
   */
  public char clientType;

  /**
   * Describes the side the client is going to join.
   */
  public char sideType;


  /**
   * Constructs an empty ConnectData for reading from an UDP packet.
   */
  public ConnectData()
  {
    clientType = ' ';
    sideType   = ' ';
  } 
  
  /**
   * Constructs a ConnectData for writeing to an UDP packet.
   *
   * @param ct client type.
   * @param st side type.
   */
  public ConnectData(char ct, char st)
  {
    clientType = ct;
    sideType = st;
  } 
  
  // Load its data content from a string.
  public void readData(StringTokenizer st)
  {
    // Get the connection type.
    clientType = st.nextToken().charAt(0);

    // Get the " "
    st.nextToken();    
    
    // Get the side type.
    sideType = st.nextToken().charAt(0);   

  } 
  
  // Stream its data content to a string.
  public void writeData(StringBuffer sb)
  {
    sb.append(Packet.CONNECT);
    sb.append(' ');
    sb.append(clientType);
    sb.append(' ');
    sb.append(sideType);
  } 
  
  
  public String toString(){
    return "connect " + clientType + " side: " + sideType;
  }
  
}
