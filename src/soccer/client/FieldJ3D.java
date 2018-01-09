/* FieldJ3D.java
   This class shows the field and players and ball using Java3D.
   A modified version of Yu Zhang's Field.java by Fergus C. Murray, August 29 2001. 
   
   Copyright (C) 2001  Yu Zhang
   Modified 254

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

/* Modified to make Field class a subclass of Arena class.  Also, world and
   methods setFont, setWorld, getWorld, and isFocusTraversable were moved to 
   Arena.
						          jdm, June 7 2001
*/

/* Replaced pitch-drawing and sprite-drawing with routines to update Java3D world.
   Also set panel not to be opaque, in order to allow Java3D to be used.

                                  fcm, August 29 2001.
*/


package soccer.client;

import javax.swing.*;
import javax.media.j3d.*;
import javax.media.j3d.GeometryArray;
import javax.vecmath.*;
import javax.vecmath.TexCoord2f;
import java.awt.*;
import java.util.*;
import java.net.*;
import soccer.common.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;

public class FieldJ3D extends Arena 
{
  private static Color bg = Color.green.darker();
  private static Color fg = Color.red;
  
  public static int LENGTH           = 100;
  public static int WIDTH            = 65;
  public static int SIDEWALK         = 5;
  public static int RADIUS           = 9;
  public static int GOAL_DEPTH       = 2;
  public static int GOAL_WIDTH      = 8;
  public static int GOALAREA_WIDTH   = 18;
  public static int GOALAREA_DEPTH   = 6;
  public static int PENALTY_WIDTH    = 40;
  public static int PENALTY_DEPTH    = 16;  
  public static int PENALTY_CENTER   = 12;
  public static int CORNER           = 1;  	 
  
  public static int METER = 7;
  
  public static double ballSize = 0.6;
  public static double playerSize = 0.9;

  private SoccerMaster soccerMaster;
  
  // the upper left corner for moving objects
  double x;
  double y;
    
  // the center of the moving object
  soccer.common.Vector2d c = new soccer.common.Vector2d();
  
  // for loop
  Player player = null;
  Enumeration players = null;

  Transform3D myTransform3D, thisTransform3D;
  Vector3d thisVector3d;
  AxisAngle4d thisAxisAngle4d;
  Transform3D viewRotation, viewTranslation;
  TransformGroup viewRotationGroup, viewTranslationGroup, ballTransformGroup, thisTransformGroup;
  SimpleUniverse myUniverse;
  Vector leftTeamTransform=new Vector();
  Vector leftTeamGeometry=new Vector();
  Vector rightTeamTransform=new Vector();
  Vector rightTeamGeometry=new Vector();
//  Slave myBehaviour;
  TOSModel curPlayer;
  boolean stepping=true;
  double oldlx[]=new double[22];
  double oldly[]=new double[22];
  double oldrx[]=new double[22];
  double oldry[]=new double[22];

  // There's no guarantee that a loaded model will be a sensible size or height for the pitch, 
  // so use these parameters to compensate.
  double scale1 = 1;
  double scale2 = 1;
  double height1 = 0;
  double height2 = 0;
  float speed;
  
  public FieldJ3D(SoccerMaster soccerMaster) 
  {
    //Initialize drawing colors, border, opacity.
    setBackground(bg);
    setForeground(fg);

    Dimension d = new Dimension((LENGTH + SIDEWALK * 2) * METER,
	                        (WIDTH  + SIDEWALK * 2) * METER);
//    JPanel jpanel = new JPanel();
    setPreferredSize(d);
    setMaximumSize(d);
    setMinimumSize(d);
    setBorder(BorderFactory.createRaisedBevelBorder());
    //add(jpanel);
    this.setLayout( new BorderLayout() );
    this.setOpaque( false ); // Otherwise the Java3D panel is hidden by green.
    
    this.soccerMaster = soccerMaster;
    
    //  Java3D initialisation begins.
    System.out.println("Java3D initialisation begins.");
    Canvas3D myCanvas3D = new
	 Canvas3D(com.sun.j3d.utils.universe.SimpleUniverse.getPreferredConfiguration());    
	this.add(myCanvas3D);
	thisTransform3D = new Transform3D();
	thisVector3d = new Vector3d();
	thisAxisAngle4d = new AxisAngle4d();
	myUniverse = new SimpleUniverse(myCanvas3D,2);
	ViewingPlatform myView = myUniverse.getViewingPlatform();
	myUniverse.getViewer().getView().setBackClipDistance(150.0);
	MultiTransformGroup mtg = myView.getMultiTransformGroup();
	viewRotationGroup = mtg.getTransformGroup(0);
	viewTranslationGroup = mtg.getTransformGroup(mtg.getNumTransforms()-1);
	viewRotation = new Transform3D();
	viewRotation.setRotation(new AxisAngle4f(1.0f,0f,0f,0f));
	viewTranslation = new Transform3D();
	viewTranslation.setTranslation(new Vector3d(0.0f,0.0f,150.0f));
	viewTranslationGroup.setTransform(viewTranslation);
	viewRotationGroup.setTransform(viewRotation);
	BranchGroup scene = constructContentBranch();
	myUniverse.addBranchGraph(scene);

  }
  
  private BranchGroup constructContentBranch() {

    //Font myFont = new Font("TimesRoman",Font.BOLD,1);
	//Font3D myFont3D = new Font3D(myFont,new FontExtrusion());
	//Text3D myText3D = new Text3D(myFont3D, "O");
    
    System.out.println("Constructing Content Branch");

	// Create teams first
	Appearance ltAppearance = new Appearance();
	Appearance theAppearance = new Appearance();
	Material ltMaterial=new Material();
	ltMaterial.setDiffuseColor(1f,1f,0f);
	ltAppearance.setMaterial(ltMaterial);
	Appearance rtAppearance = new Appearance();
	Material rtMaterial=new Material();
	rtMaterial.setDiffuseColor(1f,0f,0f);
	rtAppearance.setMaterial(rtMaterial);
	BranchGroup scene = new BranchGroup();
	Vector theTeam;
    TOSModel player;
    
    MouseRotateXZ myMouseRotateXZ = new MouseRotateXZ();
    myMouseRotateXZ.setTransformGroup(viewRotationGroup);
    myMouseRotateXZ.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0),300.0));
    scene.addChild(myMouseRotateXZ);

    MouseZoomOnRightClick myMouseZoomOnRightClick = new MouseZoomOnRightClick();
    myMouseZoomOnRightClick.setTransformGroup(viewTranslationGroup);
    myMouseZoomOnRightClick.setSchedulingBounds(new BoundingSphere(new Point3d(0,0,0),300.0));
    scene.addChild(myMouseZoomOnRightClick);


	for (int i=0; i<22; i++){
		myTransform3D = new Transform3D();
		if (i<11) {
			theAppearance=ltAppearance;
			theTeam=leftTeamTransform;
//            player = new LoadedModel("d:\\jdk1.3.1\\lascasmn.3ds");
            player = new Robot(new Color3f (1f,1f,0f));
            leftTeamGeometry.add(player);
            myTransform3D.setScale(scale1);
		    myTransform3D.setTranslation(new Vector3d(-1.5,-15.5+3.0*i,height1));
		}
		else {
			theAppearance=rtAppearance;
			theTeam=rightTeamTransform;
            //player = new LoadedModel("Simplist.3ds");
            player = new Robot(new Color3f (1f,0f,0f));
            rightTeamGeometry.add(player);            
            myTransform3D.setScale(scale2);
       		myTransform3D.setTranslation(new Vector3d(1.5,-48.5+3.0*i,height2));
		}

		thisTransformGroup = new TransformGroup(myTransform3D);
		thisTransformGroup.addChild((Node)player);
		thisTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		scene.addChild(thisTransformGroup);
        theTeam.addElement(thisTransformGroup);
	}

	// Now the ball
	Appearance ballAppearance = new Appearance();
	Material ballMaterial=new Material();
	ballMaterial.setDiffuseColor(1f,1f,1f);
	ballAppearance.setMaterial(ballMaterial);
	//myText3D = new Text3D(myFont3D, "�");
	com.sun.j3d.utils.geometry.Sphere mySphere = new com.sun.j3d.utils.geometry.Sphere(0.5f, ballAppearance);
	myTransform3D = new Transform3D();
    myTransform3D.setScale(1);
    myTransform3D.setTranslation(new Vector3d(0.0,0.0,0.5));
	ballTransformGroup = new TransformGroup(myTransform3D);
	ballTransformGroup.addChild(mySphere);

	// Now the pitch
	Appearance pitchAppearance = new Appearance();
	//Material pitchMaterial=new Material();
	QuadArray plane = new QuadArray(4, GeometryArray.COORDINATES
									| GeometryArray.TEXTURE_COORDINATE_2);

    float halfLength=SIDEWALK + LENGTH / 2;
    float halfWidth=SIDEWALK + WIDTH / 2;                                  
	Point3f p = new 
    Point3f(-halfLength, halfWidth,	 0.0f);
	plane.setCoordinate(0, p);
	p.set(-halfLength, -halfWidth,   0.0f);
	plane.setCoordinate(1, p);
	p.set(halfLength, -halfWidth,  0.0f);
	plane.setCoordinate(2, p);
	p.set(halfLength,  halfWidth,  0.0f);
	plane.setCoordinate(3, p);

	TexCoord2f q = new TexCoord2f( 0.0f,	1.0f);
	plane.setTextureCoordinate(0, 0, q);
	q.set(0.0f, 0.0f);
	plane.setTextureCoordinate(0, 1, q);
	q.set(0.75f, 0.0f);
	plane.setTextureCoordinate(0, 2, q);
	q.set(0.75f, 1.0f);
	plane.setTextureCoordinate(0, 3, q);
	URL imgURL = getClass().getResource("/imag/pitchf.gif");
	TextureLoader loader = new TextureLoader(imgURL, null);
	ImageComponent2D image = loader.getImage();

	if(image == null) {
		System.out.println("load failed for texture: pitchf.gif");
    }
    // can't use parameterless constuctor
    Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
    texture.setImage(0, image);
    //texture.setEnable(false);
    
    pitchAppearance.setTexture(texture);
    
    //pitchMaterial.setDiffuseColor(0.0f,0.5f,0.0f);
    //pitchAppearance.setMaterial(pitchMaterial);
    //Box pitch = new Box(60f, 36f, 0.1f, pitchAppearance);
    Shape3D pitch = new Shape3D(plane, pitchAppearance);
    myTransform3D = new Transform3D();
    myTransform3D.setTranslation(new Vector3f(0f,0f,-1f));
    TransformGroup pitchTransformGroup = new TransformGroup(myTransform3D);
    pitchTransformGroup.addChild(pitch);
    scene.addChild(pitchTransformGroup);
    
    //FrameBehaviour frameRateGetter = new FrameBehaviour ();
    //frameRateGetter.setSchedulingBounds(new BoundingSphere());
    //scene.addChild(frameRateGetter);
    ballTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    scene.addChild(ballTransformGroup);
    
    DirectionalLight lightD1 = new DirectionalLight();
    lightD1.setDirection (0.1f, 0.5f, -0.5f);
    lightD1.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), 256));
    scene.addChild(lightD1);

    DirectionalLight lightD2 = new DirectionalLight();
    lightD2.setDirection (-0.1f, -0.5f, -0.5f);
    lightD2.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), 256));
    scene.addChild(lightD2);
    
    scene.compile();
    System.out.println("Scene constructed.");
	return(scene);
  }
  
  public void paintComponent(Graphics g) 
  {    
    
    // draws all moving objects
    if(world != null)
    {
		     
      /*
      // draw messages from player clients
      if(world.leftM != null && soccerMaster.chat.isSelected())
      {
        g2.setColor(Color.yellow);
        g2.drawString("(" + world.leftM.id + "):" + world.leftM.message,
	             SIDEWALK * METER, 2 * METER);
      }
      if(world.rightM != null && soccerMaster.chat.isSelected())
      {
        g2.setColor(Color.red);
        g2.drawString("(" + world.rightM.id + "):" + world.rightM.message,
	             (SIDEWALK + LENGTH / 2) * METER, 2 * METER);
      } */
      int index=0;      
      if(world.ball != null)
      {
     
        // draw the ball                 
        c.setXY(world.ball.position);
        //soccer2user(c);

        x = c.getX();// - ballSize * METER;
        y = c.getY();// - ballSize * METER;
        thisVector3d.set(x,y,-0.5f);
	    thisTransform3D.setTranslation(thisVector3d);
        thisTransform3D.setScale(1);
	    ballTransformGroup.setTransform(thisTransform3D);

      }

      if(world.leftTeam != null)
      {
        // draw left players
        players = world.leftTeam.elements();
        index=0;
        while(players.hasMoreElements())
        {          
          player = (Player) players.nextElement();
          c.setXY(player.position);
          //soccer2user(c);
          x = c.getX() - playerSize * METER;
          y = c.getY() - playerSize * METER;
                    
          curPlayer=(TOSModel)leftTeamGeometry.elementAt(index);
          
          if (stepping) {
              speed=(float)Math.sqrt((x-oldlx[index])*(x-oldlx[index])+(y-oldly[index])*(y-oldly[index]));
              curPlayer.step(speed);
          }

        thisVector3d.set(x,y,height1);
        thisTransform3D.setTranslation(thisVector3d);
        thisTransform3D.setScale(scale1);
        thisAxisAngle4d.set(0,0,1,(player.direction)*1.75);
        thisTransform3D.setRotation(thisAxisAngle4d);

        thisTransformGroup=(TransformGroup)leftTeamTransform.elementAt(index);
        thisTransformGroup.setTransform(thisTransform3D);
        oldlx[index]=x;
        oldly[index]=y;
        index++;
                     
        }
      }

      index=0;
      
      if(world.rightTeam != null)
      {
        // draw right players
        players = world.rightTeam.elements();
        while(players.hasMoreElements())
        {
          player = (Player) players.nextElement();
          c.setXY(player.position);
          //soccer2user(c);
          x = c.getX();// - playerSize * METER;
          y = c.getY();// - playerSize * METER;

          curPlayer=(TOSModel)rightTeamGeometry.elementAt(index);
          
          if (stepping) {
              speed=(float)Math.sqrt((x-oldrx[index])*(x-oldrx[index])+(y-oldry[index])*(y-oldry[index]));
              curPlayer.step(speed);
          }

        thisVector3d.set(x,y,height2);
        thisTransform3D.setTranslation(thisVector3d);
        thisAxisAngle4d.set(0,0,1,(player.direction)*1.75);
        thisTransform3D.setScale(scale2);
        thisTransform3D.setRotation(thisAxisAngle4d);

        thisTransformGroup=(TransformGroup)rightTeamTransform.elementAt(index);
        thisTransformGroup.setTransform(thisTransform3D);
        oldrx[index]=x;
        oldry[index]=y;
        index++;
        
        }
      }
    
      // identify myself on the field
      if(world.me != null)
      {
        c.setXY(world.me.position);
        soccer2user(c);
	
        x = c.getX() - playerSize * METER;
        y = c.getY() - playerSize * METER;
		if(world.me.side == 'l') {
            curPlayer=(TOSModel)leftTeamGeometry.elementAt(10);
            if (stepping) {
                speed=(float)Math.sqrt((x-oldlx[10])*(x-oldlx[10])+(y-oldly[10])*(y-oldly[10]));
                curPlayer.step(speed);
            }
            thisTransformGroup=(TransformGroup)leftTeamTransform.elementAt(10);
            oldlx[index]=x;
            oldly[index]=y;
        }
        else {
            curPlayer=(TOSModel)rightTeamGeometry.elementAt(10);
            if (stepping) {
                speed=(float)Math.sqrt((x-oldrx[10])*(x-oldrx[10])+(y-oldry[10])*(y-oldry[10]));
                curPlayer.step(speed);
            }
            thisTransformGroup=(TransformGroup)rightTeamTransform.elementAt(10);
            oldrx[10]=x;
            oldry[10]=y;            
        }


        thisVector3d.set(x,y,3.0);
        thisTransform3D.setTranslation(thisVector3d);
        thisAxisAngle4d.set(0,0,1,(world.me.direction)*1.75);
        thisTransform3D.setRotation(thisAxisAngle4d);

        thisTransformGroup.setTransform(thisTransform3D);
        index++;
            
      }
    }
       
  }

  public soccer.common.Vector2d getGoalPosition() { return new soccer.common.Vector2d(LENGTH/2,0); }
  
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
  public void user2soccer(soccer.common.Vector2d p)
  {
    double x = p.getX() / METER;
    double y = p.getY() / METER;

    double origin_x = SIDEWALK + LENGTH / 2;
    double origin_y = SIDEWALK + WIDTH / 2;
    
    x = x - origin_x;
    y = - (y - origin_y);

    p.setXY(x, y);
    
    return;
    
  }        
  
  // convert from soccer space to Java 2d user space 
  public void soccer2user(soccer.common.Vector2d p)
  {
    double x = p.getX();
    double y = p.getY();

    double origin_x = (- SIDEWALK - LENGTH / 2) ;
    double origin_y = SIDEWALK + WIDTH / 2;
    
    x = (x - origin_x) * METER;
    y = - (y - origin_y) * METER;

    p.setXY(x, y);
    
    return;
          
  }
}
