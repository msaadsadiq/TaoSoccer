/* Rink.java
   This class shows the ice rink, players, and puck.
   
   Copyright (C) 2001  Jefferson Montgomery

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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import soccer.common.*;

public class Rink extends Arena 
{
  private static Color bg = Color.white;
  private static Color fg = Color.red;
  
// Rink dimensions taken from Canadian Hockey Association Rule book
// and are rounded to the nearest int.  Not all detail is displayed.
//
// http://www.canadianhockey.ca/e/develop/officials/rulesindex.html

  public static int METER  = 12;

  public static int LENGTH                  = (int) (60.96 * METER);
  public static int WIDTH                   = (int) (30.48 * METER);
  public static int MARGIN                  = (int) (4     * METER);
  public static int CORNER_DIAMETER         = (int) (17.06 * METER);
  public static int GOAL_LINE_DISTANCE      = (int) (3.05 * METER);   // From boards (3.05-4.57m acceptable)
  public static int GOAL_LINE_FIX           = (int) (12);
  public static int BLUE_LINE_DISTANCE      = (int) (18.29 * METER);  // From goal line
  public static int GOAL_WIDTH              = (int) (1.83 * METER);
  public static int FACEOFF_DOT_DIAMETER    = (int) (0.6096 * METER);
  public static int FACEOFF_CIRCLE_DIAMETER = (int) (9.14 * METER);
  public static int FACEOFF_LENGTH          = (int) (6.09 * METER);
  public static int FACEOFF_WIDTH           = (int) (6.71 * METER);
  public static int NEUTRAL_FACEOFF_LENGTH  = (int) (1.52 * METER);
  public static int HASH_DISTANCE_1         = (int) (5.64 * METER);
  public static int HASH_DISTANCE_2         = (int) (6.55 * METER);
  public static int HASH_LENGTH             = (int) (0.6096 * METER);
  public static int LINE_RADIUS             = (int) (0.3048 * METER);
  public static int LINE_LENGTH             = (int) (1.22 * METER);
  public static int LINE_SEPARATION         = (int) (0.4572 * METER);
  public static int CROSS_LENGTH            = (int) (0.8636 * METER);

  public static double ballSize = 0.6;
  public static double playerSize = 0.9;

  private SoccerMaster soccerMaster;
  
  // the upper left corner for moving objects
  double x;
  double y;
    
  // the center of the moving object
  Vector2d c = new Vector2d();
  
  // for loop
  Player player = null;
  Enumeration players = null;
	
  public Rink(SoccerMaster soccerMaster) 
  {
    //Initialize drawing colors, border, opacity.
    setBackground(bg);
    setForeground(fg);

    Dimension d = new Dimension(LENGTH + MARGIN + MARGIN, WIDTH + MARGIN + MARGIN);
    setPreferredSize(d);
    setMaximumSize(d);
    setMinimumSize(d);
    setBorder(BorderFactory.createRaisedBevelBorder());
    
    this.soccerMaster = soccerMaster;

  }
    
  public void paintComponent(Graphics g) 
  {

    // clears the background
    super.paintComponent(g);      
	
    // draw the arena
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // draw the lines
    g2.setColor(Color.red);
    g2.setStroke(new BasicStroke(1));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE,
	MARGIN + GOAL_LINE_FIX, 
	MARGIN + GOAL_LINE_DISTANCE,
	MARGIN + WIDTH - GOAL_LINE_FIX));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE,
	MARGIN + GOAL_LINE_FIX, 
	MARGIN + LENGTH - GOAL_LINE_DISTANCE,
	MARGIN + WIDTH - GOAL_LINE_FIX));
    g2.setStroke(new BasicStroke(2));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH / 2,
	MARGIN, 
	MARGIN + LENGTH / 2,
	MARGIN + WIDTH));
    g2.setColor(Color.blue);
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + BLUE_LINE_DISTANCE,
	MARGIN, 
	MARGIN + GOAL_LINE_DISTANCE + BLUE_LINE_DISTANCE,
	MARGIN + WIDTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - BLUE_LINE_DISTANCE,
	MARGIN,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - BLUE_LINE_DISTANCE,
	MARGIN + WIDTH));

    // draw the goals
    g2.setColor(Color.red);
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE,
	MARGIN + WIDTH / 2 - GOAL_WIDTH / 2, 
	MARGIN + GOAL_LINE_DISTANCE,
	MARGIN + WIDTH / 2 + GOAL_WIDTH / 2));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE,
	MARGIN + WIDTH / 2 - GOAL_WIDTH / 2, 
	MARGIN + LENGTH - GOAL_LINE_DISTANCE,
	MARGIN + WIDTH / 2 + GOAL_WIDTH / 2));
    g2.setStroke(new BasicStroke(1));
    g2.draw(new Arc2D.Double(
	MARGIN + GOAL_LINE_DISTANCE - GOAL_WIDTH / 2,
	MARGIN + WIDTH / 2 - GOAL_WIDTH / 2,
	GOAL_WIDTH,
	GOAL_WIDTH,
	270,180,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - GOAL_WIDTH / 2,
	MARGIN + WIDTH / 2 - GOAL_WIDTH / 2,
	GOAL_WIDTH,
	GOAL_WIDTH,
	90,180,Arc2D.OPEN));

    // draw centre faceoff area
    g2.setColor(Color.blue);
    g2.fillArc(
	MARGIN + LENGTH / 2 - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360);
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH / 2 - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_CIRCLE_DIAMETER / 2,
	FACEOFF_CIRCLE_DIAMETER,
	FACEOFF_CIRCLE_DIAMETER,
	0,360,Arc2D.OPEN));

    // draw neutral faceoff areas
    g2.setColor(Color.red);
    g2.draw(new Arc2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + BLUE_LINE_DISTANCE + NEUTRAL_FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + BLUE_LINE_DISTANCE + NEUTRAL_FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - BLUE_LINE_DISTANCE - NEUTRAL_FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - BLUE_LINE_DISTANCE - NEUTRAL_FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360,Arc2D.OPEN));

    // draw endzone faceoff areas
    // left top
    // dot
    g2.fillArc(
	MARGIN + GOAL_LINE_DISTANCE + FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360);
    // circle
    g2.draw(new Arc2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + FACEOFF_LENGTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	FACEOFF_CIRCLE_DIAMETER,
	FACEOFF_CIRCLE_DIAMETER,
	0,360,Arc2D.OPEN));
    // hashmarks
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    // left bottom
    // dot
    g2.fillArc(
	MARGIN + GOAL_LINE_DISTANCE + FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360);
    // circle
    g2.draw(new Arc2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + FACEOFF_LENGTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	FACEOFF_CIRCLE_DIAMETER,
	FACEOFF_CIRCLE_DIAMETER,
	0,360,Arc2D.OPEN));
    // hash marks
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + GOAL_LINE_DISTANCE + HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    // right top
    // dot
    g2.fillArc(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360);
    // circle
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - FACEOFF_LENGTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	FACEOFF_CIRCLE_DIAMETER,
	FACEOFF_CIRCLE_DIAMETER,
	0,360,Arc2D.OPEN));
    // hash marks
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 - FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    // right bottom
    // dot
    g2.fillArc(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - FACEOFF_LENGTH - FACEOFF_DOT_DIAMETER / 2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_DOT_DIAMETER / 2,
	FACEOFF_DOT_DIAMETER,
	FACEOFF_DOT_DIAMETER,
	0,360);
    // circle
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - FACEOFF_LENGTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	FACEOFF_CIRCLE_DIAMETER,
	FACEOFF_CIRCLE_DIAMETER,
	0,360,Arc2D.OPEN));
    // hash marks
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH - FACEOFF_CIRCLE_DIAMETER / 2 - HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_1,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2,
	MARGIN + LENGTH - GOAL_LINE_DISTANCE - HASH_DISTANCE_2,
	MARGIN + WIDTH / 2 + FACEOFF_WIDTH + FACEOFF_CIRCLE_DIAMETER / 2 + HASH_LENGTH));

    // draw the boards
    g2.setColor(Color.black);
    g2.setStroke(new BasicStroke(3));

    g2.draw(new Line2D.Double(
	MARGIN + CORNER_DIAMETER / 2, 
	MARGIN, 
	MARGIN + LENGTH - CORNER_DIAMETER / 2, 
	MARGIN));
    g2.draw(new Line2D.Double(
	MARGIN + CORNER_DIAMETER / 2, 
	MARGIN + WIDTH, 
	MARGIN + LENGTH - CORNER_DIAMETER / 2,
	MARGIN + WIDTH));
    g2.draw(new Line2D.Double(
	MARGIN,
	MARGIN + CORNER_DIAMETER / 2, 
	MARGIN,
	MARGIN + WIDTH - CORNER_DIAMETER / 2));
    g2.draw(new Line2D.Double(
	MARGIN + LENGTH,
	MARGIN + CORNER_DIAMETER / 2, 
	MARGIN + LENGTH,
	MARGIN + WIDTH - CORNER_DIAMETER / 2));

    g2.draw(new Arc2D.Double(
	MARGIN,
	MARGIN,
	CORNER_DIAMETER,
	CORNER_DIAMETER,
	90,90,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN,
	MARGIN + WIDTH - CORNER_DIAMETER,
	CORNER_DIAMETER,
	CORNER_DIAMETER,
	180,90,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - CORNER_DIAMETER,
	MARGIN,
	CORNER_DIAMETER,
	CORNER_DIAMETER,
	0,90,Arc2D.OPEN));
    g2.draw(new Arc2D.Double(
	MARGIN + LENGTH - CORNER_DIAMETER,
	MARGIN + WIDTH - CORNER_DIAMETER,
	CORNER_DIAMETER,
	CORNER_DIAMETER,
	270,90,Arc2D.OPEN));

    // set font size to the player size
    setFont(g2, METER * 2, 6);

    // draws all moving objects
    if(world != null)
    {
		     
      // draw messages from player clients
      if(world.leftM != null && soccerMaster.chat.isSelected())
      {
        g2.setColor(Color.yellow);
        g2.drawString("(" + world.leftM.id + "):" + world.leftM.message,
	             0, 2 * METER);
      }
      if(world.rightM != null && soccerMaster.chat.isSelected())
      {
        g2.setColor(Color.red);
        g2.drawString("(" + world.rightM.id + "):" + world.rightM.message,
	             (LENGTH / 2) * METER, 2 * METER);
      } 
      
      if(world.ball != null)
      {
     
        // draw the puck                 
        c.setXY(world.ball.position);
        soccer2user(c);

        x = c.getX() - ballSize * METER;
        y = c.getY() - ballSize * METER;
      
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bi.createGraphics();
        big.setColor(Color.white);
        big.fillRect(0, 0, 4, 4);
        big.setColor(Color.black);
        big.fillOval(0, 0, 4, 4);
        Rectangle r = new Rectangle(0,0,4,4);
        g2.setPaint(new TexturePaint(bi, r));
        g2.fill (new Ellipse2D.Double(x, y, ballSize * METER * 2, ballSize * METER * 2));
      }

      if(world.leftTeam != null)
      {
        // draw left players
        players = world.leftTeam.elements();
        while(players.hasMoreElements())
        {
      
          player = (Player) players.nextElement();
          c.setXY(player.position);
          soccer2user(c);
          x = c.getX() - playerSize * METER;
          y = c.getY() - playerSize * METER;
        
          g2.setColor(Color.yellow);
          g2.fill(new Ellipse2D.Double(x, y, playerSize * METER * 2, playerSize * METER * 2));
        
          g2.setColor(Color.red);
          g2.draw(new Line2D.Double(c.getX(), c.getY(), 
                  c.getX() + playerSize * METER * Math.cos(Util.Deg2Rad(player.direction)) , 
                  c.getY() - playerSize * METER * Math.sin(Util.Deg2Rad(player.direction))));
          if(soccerMaster.number.isSelected())
            g2.drawString(Integer.toString(player.id), (int)x, (int)y); 
        }
      }

      if(world.rightTeam != null)
      {
        // draw right players
        players = world.rightTeam.elements();
        while(players.hasMoreElements())
        {
          player = (Player) players.nextElement();
          c.setXY(player.position);
          soccer2user(c);
          x = c.getX() - playerSize * METER;
          y = c.getY() - playerSize * METER;
        
          g2.setColor(Color.red);
          g2.fill (new Ellipse2D.Double(x, y, playerSize * METER * 2, playerSize * METER * 2));
        
          g2.setColor(Color.yellow);
          g2.draw(new Line2D.Double(c.getX(), c.getY(), 
                  c.getX() + playerSize * METER * Math.cos(Util.Deg2Rad(player.direction)) , 
                  c.getY() - playerSize * METER * Math.sin(Util.Deg2Rad(player.direction))));
          if(soccerMaster.number.isSelected())
            g2.drawString(Integer.toString(player.id), (int)x, (int)y);		  
        }
      }
    
      // identify myself on the field
      if(world.me != null)
      {
        c.setXY(world.me.position);
        soccer2user(c);
	
        x = c.getX() - playerSize * METER;
        y = c.getY() - playerSize * METER;
		 
        if(world.me.side == 'l') g2.setColor(Color.yellow);
	else g2.setColor(Color.red);
        g2.fill (new Ellipse2D.Double(x, y, playerSize * METER * 2, playerSize * METER * 2));

        if(world.me.side == 'l') g2.setColor(Color.red);
	else g2.setColor(Color.yellow);
        g2.draw(new Line2D.Double(c.getX(), c.getY(), 
                c.getX() + playerSize * METER * Math.cos(Util.Deg2Rad(world.me.direction)) , 
                c.getY() - playerSize * METER * Math.sin(Util.Deg2Rad(world.me.direction))));

        g2.setColor(Color.blue);
	g2.fill (new Arc2D.Double(x, y, playerSize * METER * 2, playerSize * METER * 2,
	         world.me.direction + 90, 180, Arc2D.OPEN));
		
        if(soccerMaster.number.isSelected())
          g2.drawString(Integer.toString(world.me.id), (int)x, (int)y);		
      }
    }
  }

  public Vector2d getGoalPosition() { return new Vector2d(LENGTH/2,0); }
  
  // Coordinate System Conversions
  
  // User space is a device-independent logical coordinate system. 
  // the coordinate space that your program uses. All geometries passed into 
  // Java 2D rendering routines are specified in user-space coordinates.
  // the origin of user space is the upper-left corner of the component's drawing
  // area. The x coordinate increases to the right, and the y coordinate increases downward.
  
  // Soccer space is used in soccer server.
  // the origin of soccer space is the center of the soccer field. The x coordinate increases 
  // to the right, and the y coordinate increases upward.
  
  // convert from Java 2d user space to soccer space
  public void user2soccer(Vector2d p)
  {
    double x = p.getX() / METER;
    double y = p.getY() / METER;

    double origin_x = LENGTH / 2;
    double origin_y = WIDTH / 2;
    
    x = x - origin_x;
    y = - (y - origin_y);

    p.setXY(x, y);
    
    return;
    
  }        
  
  // convert from soccer space to Java 2d user space 
  public void soccer2user(Vector2d p)
  {
    double x = p.getX();
    double y = p.getY();

    double origin_x = (-LENGTH / 2) ;
    double origin_y = WIDTH / 2;
    
    x = (x - origin_x) * METER;
    y = - (y - origin_y) * METER;

    p.setXY(x, y);
    
    return;
  }
}
