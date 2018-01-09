/* Robot.java
  
   Copyright (C) 2001  Fergus Crawshay Murray
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


package soccer.client;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;

    public class Robot extends TransformGroup implements TOSModel {
       public Robot (){
           this (new Color3f (0.75f,0.75f,0.9f));
       }
       
       public Robot(Color3f colour) {
            this.colour=colour;
            this.appearance=createAppearance();
            
            // Build a humanoid robot out of boxes.
            
            Transform3D lt= new Transform3D();
            lt.setTranslation(new Vector3f(0.0f,0.0f,1.6f));
            TransformGroup legs=new TransformGroup(lt);
            double lltm[]={1.0,0.0,0.0,-0.5,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,-0.4,  0.0,0.0,0.0,1.0};
            llt= new Transform3D(lltm);
            leftLeg=new TransformGroup(llt);
            leftLeg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            leftLeg.addChild(new Box(0.15f,0.3f,-0.8f,appearance));
            legs.addChild(leftLeg);

            double lstm[]={1.0,0.0,0.0,0.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,-1.2,  0.0,0.0,0.0,1.0};
            lst= new Transform3D(lstm);
            leftShin=new TransformGroup(lst);
            leftShin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            leftShin.addChild(new Box(0.4f,0.2f,0.8f,appearance));
            leftLeg.addChild(leftShin);

            double rltm[]={1.0,0.0,0.0,0.5,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,-0.4,  0.0,0.0,0.0,1.0};
            rlt= new Transform3D(rltm);
            rightLeg=new TransformGroup(rlt);
            rightLeg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            rightLeg.addChild(new Box(0.15f,0.3f,0.8f,appearance));
            legs.addChild(rightLeg);
            addChild(legs);
            
            double rstm[]={1.0,0.0,0.0,0.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,-1.2,  0.0,0.0,0.0,1.0};
            rst= new Transform3D(rstm);
            rightShin=new TransformGroup(rst);
            rightShin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            rightShin.addChild(new Box(0.4f,0.2f,0.8f,appearance));
            rightLeg.addChild(rightShin);

            double latm[]={1.0,0.0,0.0,-1.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,3.0,  0.0,0.0,0.0,1.0};
            lat= new Transform3D(latm);
            leftArm=new TransformGroup(lat);
            leftArm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            leftArm.addChild(new Box(0.3f,0.1f,0.6f,appearance));
            addChild(leftArm);

            double lftm[]={1.0,0.0,0.0,0.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,-0.8,  0.0,0.0,0.0,1.0};
            lft= new Transform3D(lftm);
            leftForearm=new TransformGroup(lft);
            leftForearm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            leftForearm.addChild(new Box(0.3f,0.2f,0.5f,appearance));
            leftArm.addChild(leftForearm);

            double ratm[]={1.0,0.0,0.0,1.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,3.0,  0.0,0.0,0.0,1.0};
            rat= new Transform3D(ratm);
            rightArm=new TransformGroup(rat);
            rightArm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            rightArm.addChild(new Box(0.3f,0.1f,0.6f,appearance));
            addChild(rightArm);

            double rftm[]={1.0,0.0,0.0,0.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,-0.8,  0.0,0.0,0.0,1.0};
            rft= new Transform3D(rftm);
            rightForearm=new TransformGroup(rft);
            rightForearm.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            rightForearm.addChild(new Box(0.3f,0.2f,0.5f,appearance));
            rightArm.addChild(rightForearm);

            double ttm[]={1.0,0.0,0.0,0.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,2.7,  0.0,0.0,0.0,1.0};
            tt= new Transform3D(ttm);
            torso=new TransformGroup(tt);
            torso.addChild(new Box(0.7f,0.5f,1.0f,appearance));
            addChild(torso);

            double htm[]={1.0,0.0,0.0,0.0,  0.0,1.0,0.0,0.0,  0.0,0.0,1.0,4.0,  0.0,0.0,0.0,1.0};
            ht= new Transform3D(htm);
            head=new TransformGroup(ht);
            head.addChild(new Box(0.4f,0.4f,0.4f,appearance));
            addChild(head);
            
            Transform3D rt=new Transform3D();
            rt.setRotation (new AxisAngle4f (0f,0f,1f,(float)(Math.PI*0.5)));
            this.setTransform(rt);
        } 

        Color3f colour;
        Appearance appearance;
        float phase=0;
        int N=16;
        double a;
        int v;
        TransformGroup leftLeg, leftShin, rightLeg, rightShin, leftArm, leftForearm, rightArm, rightForearm, torso, head;
        Transform3D llt, lst, rlt, rst, lat, lft, rat, rft, tt, ht;
        
        public void step(float stepSize){
          
            phase+=stepSize;
            //System.out.println(phase);
            if (phase>Math.PI*2) phase-=Math.PI*2;
            AxisAngle4f angt=new AxisAngle4f();

            // Crude trigonometric approximation of walking motion
            
            angt.set(1.0f,0f,0f,-0.25f+0.5f*(float)Math.sin(phase));
            llt.setRotation(angt);
            leftLeg.setTransform(llt);

            angt.set(1.0f,0f,0f,-0.25f+0.5f*(float)Math.sin(-phase));
            rlt.setRotation(angt);
            rightLeg.setTransform(rlt);

            // Shins and forearms are a little bit behind thighs and upper arms...
            angt.set(1.0f,0f,0f,0.25f+0.25f*(float)Math.sin(-phase+0.4));
            rst.setRotation(angt);
            rightShin.setTransform(rst);
 
            angt.set(1.0f,0.3f,0f,-0.25f+0.25f*(float)Math.sin(-phase+0.4));
            rat.setRotation(angt);
            rightArm.setTransform(rat);
            rft.setRotation(angt);
            rightForearm.setTransform(rft);
            
            angt.set(1.0f,0f,0f,0.25f+0.25f*(float)Math.sin(phase-0.4));
            lst.setRotation(angt);
            leftShin.setTransform(lst);
            
            angt.set(1.0f,0.3f,0f,-0.25f+0.25f*(float)Math.sin(phase-0.4));           
            lat.setRotation(angt);
            leftArm.setTransform(lat);
            lft.setRotation(angt);
            leftForearm.setTransform(lft);

        }

        Appearance createAppearance(){

//Material(Color3f ambientColor, Color3f emissiveColor, Color3f diffuseColor, Color3f specularColor, float shininess) 
            Appearance appearance = new Appearance();
            appearance.setMaterial (new Material (new Color3f(0.1f,0.0f,0.1f), new Color3f(0.2f,0.0f,0.0f), colour, colour, 120f));
            PolygonAttributes polyAttrib = new PolygonAttributes();
            polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
            appearance.setPolygonAttributes(polyAttrib);

            return appearance;
        }

    } 
