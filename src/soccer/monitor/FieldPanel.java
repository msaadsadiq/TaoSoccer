/*
  Copyright (C) 2003 by Krzysztof Langner

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


package soccer.monitor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import soccer.common.Util;
import soccer.common.Vector2d;
import soccer.server.SoccerServer;
import soccer.server.sim.Splayer;
import soccer.server.sim.World;


/**
 * This class represents view with Field and players
 * 
 * @author Krzysztof Langner
 */
public class FieldPanel extends JComponent implements MouseListener 
{
  
  //---------------------------------------------------------------------------
  /**
   * Constructor 
   */
  public FieldPanel(SoccerServer server) {
    
    this.server = server;
    addMouseListener(this);
    init();
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Paint component
   */
  public void paint(Graphics g) {
    
    int   width = getWidth();
    int   height = getHeight();
    
    
    // Set antialiasing
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Fill backgroud
    g.setColor(BACKCOLOR);
    g.fillRect(0, 0, width, height);
    
    // calculate scale
    scale = Math.min(
      (float)width/(float)(World.LENGTH + 2*World.SIDEWALK),
      (float)height/(float)(World.WIDTH + 2*World.SIDEWALK));
      
    // draw pitch
    drawPitch(g2);
    
    // draw moving objects
    drawObjects(g2);
  }
  
  
  //---------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent event) {
    
    World     world = server.getWorld();
    Vector2d  v = new Vector2d(event.getX(), event.getY());
    
    v = view2soccer(v);
    world.getBall().moveTo(v.getX(), v.getY());
  }


  //---------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent arg0) {
  }


  //---------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(MouseEvent arg0) {
  }


  //---------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent arg0) {
  }


  //---------------------------------------------------------------------------
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent arg0) {
  }


  //---------------------------------------------------------------------------
  /**
   * Init view
   */
  private void init(){
    
    // Create popup menu
    popupMenu = new JMenu();
    popupMenu.add(new JMenuItem("drop ball"));
    popupMenu.add(new JMenuItem("drop ball"));
    popupMenu.add(new JMenuItem("drop ball"));
    popupMenu.add(new JMenuItem("drop ball"));
  }

  //---------------------------------------------------------------------------
  /**
   * Draw field lines
   */
  private void drawPitch(Graphics2D g){

    Rectangle rect, area;
    int       centerX;
    int       centerY;
    int       x, y;
    int       radius;
    
    
    rect = scaledRect(World.SIDEWALK, World.SIDEWALK,
      World.LENGTH, World.WIDTH);
    
    radius = (int)(World.RADIUS*scale);
    centerX = (int)((World.SIDEWALK + World.LENGTH/2)*scale);
    centerY = (int)((World.SIDEWALK + World.WIDTH/2)*scale);
    
    g.setColor(Color.WHITE);
    g.drawRect(rect.x, rect.y, rect.width, rect.height);
                
    x = rect.x + rect.width/2;
    g.drawLine(x, rect.y, x, rect.y + rect.height);
    
    //draw Penalty areas
    area = scaledRect(World.SIDEWALK, 
      World.SIDEWALK + World.WIDTH/2 - World.PENALTY_WIDTH/2,
      World.PENALTY_DEPTH, World.PENALTY_WIDTH); 
    g.drawRect(area.x, area.y, area.width, area.height);
    area.x = (int)(scale*(World.SIDEWALK + 
                    World.LENGTH - World.PENALTY_DEPTH));
    g.drawRect(area.x, area.y, area.width, area.height);
    
    // draw goal area
    area = scaledRect(World.SIDEWALK, 
      World.SIDEWALK + World.WIDTH/2 - World.GOALAREA_WIDTH/2,
      World.GOALAREA_DEPTH, World.GOALAREA_WIDTH); 
    g.drawRect(area.x, area.y, area.width, area.height);
    area.x = (int)(scale*(World.SIDEWALK + 
                    World.LENGTH - World.GOALAREA_DEPTH));
    g.drawRect(area.x, area.y, area.width, area.height);
    
    // draw central circle
    g.drawOval(centerX-radius, centerY - radius, 2*radius, 2*radius);
    g.fillOval(centerX-2, centerY-2, 5, 5);
    
    // draw penalty point
    x = (int)(scale*(World.SIDEWALK + World.PENALTY_CENTER));
    g.fillOval(x-2, centerY-2, 5, 5);
    x = (int)(scale*(World.SIDEWALK + World.LENGTH - World.PENALTY_CENTER));
    g.fillOval(x-2, centerY-2, 5, 5);
    
    // draw penalty circle             
    g.draw(new Arc2D.Double(
      (World.PENALTY_CENTER - World.RADIUS + World.SIDEWALK)*scale, 
      centerY-radius, 2*radius, 2*radius, 297, 126, Arc2D.OPEN));            
    g.draw(new Arc2D.Double(
      (World.SIDEWALK+World.LENGTH -World.PENALTY_CENTER - World.RADIUS)*scale, 
      centerY-radius, 2*radius, 2*radius, 117, 126, Arc2D.OPEN));            

    // draw corners
    int corner = (int)(World.CORNER*scale);
    // left top corner
    g.draw(new Arc2D.Double(rect.x-corner, rect.y-corner,
              2*corner, 2*corner, 270, 90, Arc2D.OPEN));        
  
      // left bottom corner
    g.draw(new Arc2D.Double(rect.x-corner, rect.y + rect.height-corner, 
              2*corner, 2*corner, 0, 90, Arc2D.OPEN));        

      // right top corner
    g.draw(new Arc2D.Double(rect.x+rect.width-corner, rect.y-corner,
            2*corner, 2*corner, 180, 90, Arc2D.OPEN));        

      // right bottom corner
    g.draw(new Arc2D.Double(rect.x+rect.width-corner, rect.y+rect.height-corner,
            2*corner, 2*corner, 90, 90, Arc2D.OPEN));        
    
    // draw goal
    area = scaledRect(World.SIDEWALK-World.GOAL_DEPTH, 
      World.SIDEWALK + World.WIDTH/2 - World.GOAL_WIDTH/2,
      World.GOAL_DEPTH, World.GOAL_WIDTH); 
    g.drawRect(area.x, area.y, area.width, area.height);
    area.x = (int)(scale*(World.SIDEWALK + World.LENGTH));
    g.drawRect(area.x, area.y, area.width, area.height);
    
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Draw all moving objects
   */
  private void drawObjects(Graphics2D g){
    
    World         world = server.getWorld();
    Vector2d      c;
    double        s;
    Enumeration   players;
    Splayer        player;
    
    
    if(world.getBall() != null)
    {
     
      // draw the ball                 
      c = soccer2view(world.getBall().getPosition());

      BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
      Graphics2D big = bi.createGraphics();
      big.setColor(Color.white);
      big.fillRect(0, 0, 4, 4);
      big.setColor(Color.black);
      big.fillOval(0, 0, 4, 4);
      Rectangle r = new Rectangle(0,0,4,4);
      g.setPaint(new TexturePaint(bi, r));

      g.fill(new Ellipse2D.Double(c.getX()-BALL_SIZE/2, c.getY()-BALL_SIZE/2, 
        BALL_SIZE, BALL_SIZE)); 
    }

    // draw left players
    players = world.getPlayers();
    while(players.hasMoreElements())
    {
      player = (Splayer) players.nextElement();
      c = soccer2view(player.getPosition());
    
      if(player.side == 'l')
        g.setColor(Color.yellow);
      else
        g.setColor(Color.red);
      g.fill (new Ellipse2D.Double(c.getX()-PLAYER_SIZE/2, c.getY()-PLAYER_SIZE/2, 
        PLAYER_SIZE, PLAYER_SIZE)); 
    
      if(player.side == 'l')
        g.setColor(Color.red);
      else
        g.setColor(Color.yellow);
      g.draw(new Line2D.Double(c.getX(), c.getY(), 
              c.getX() + PLAYER_SIZE/2 * Math.cos(Util.Deg2Rad(player.getDirection())) , 
              c.getY() - PLAYER_SIZE/2 * Math.sin(Util.Deg2Rad(player.getDirection()))));
      g.drawString(Integer.toString(player.id), (int)c.getX()+PLAYER_SIZE/2, (int)c.getY()+PLAYER_SIZE/2); 
    }

  }
  
  
  //---------------------------------------------------------------------------
  /**
   * create rectangle and scale its coordinates
   */
  private Rectangle scaledRect(double x, double y, double width, double height){
    
    return new Rectangle((int)(scale*x), (int)(scale*y), 
      (int)(scale*width), (int)(scale*height));
  }
  

  //---------------------------------------------------------------------------
  /**
   * convert from soccer space to Java 2d user space
   */  
  private Vector2d soccer2view(Vector2d p)
  {
    Vector2d  v = new Vector2d();

    v = new Vector2d( (World.SIDEWALK + World.LENGTH/2 + p.getX())*scale,
                      (World.SIDEWALK + World.WIDTH/2 - p.getY())*scale);
    
    return v;
          
  }


  //---------------------------------------------------------------------------
  /**
   * convert from soccer space to Java 2d user space
   */  
  private Vector2d view2soccer(Vector2d p)
  {
    Vector2d  v = new Vector2d();

    v = new Vector2d( p.getX()/scale - World.SIDEWALK - World.LENGTH/2,
                      -p.getY()/scale + World.SIDEWALK + World.WIDTH/2);
    
    return v;
          
  }


  //---------------------------------------------------------------------------
  // Private members
  /** Background color */
  private final Color   BACKCOLOR = Color.green.darker();
  private final int     BALL_SIZE = 8;
  private final int     PLAYER_SIZE = 12;
  /** server */
  private SoccerServer  server;
  /** Scale factor */
  private float scale;
  /** Popup menu */
  JMenu   popupMenu;


}
