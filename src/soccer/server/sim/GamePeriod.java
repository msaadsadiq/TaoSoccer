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

package soccer.server.sim;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * This class is responsible for tracking game state
 * @author Krzysztof Langner
 */
public class GamePeriod implements ModelItem{
  
  //---------------------------------------------------------------------------
  /**
   * Constructor
   * @param prepareTime Time in simulation steps before game starts
   * @param playTime Period time in simulation steps.
   */
  public GamePeriod(int prepareTime, int playTime){
    
    this.prepareTime = prepareTime;
    this.playTime = playTime;
    period = PRE_GAME;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * This function advances the ticker and decides which game period is in
   * and set its game mode according to the period change
   * @return true if game period changed
   */
  public boolean updateGamePeriod(int step)
  {
    int oldPeriod = period; 
  
    if (step < prepareTime) 
      period = PRE_GAME;
    else if (step < prepareTime + playTime) 
      period = FIRST_HALF;
    else if (step < 2*prepareTime + playTime) 
      period = HALF_TIME;
    else if (step < 2*(prepareTime + playTime)) 
      period = SECOND_HALF;
    else  
      period = END_GAME; 

    if(period != oldPeriod){
      updateView();
      return true;
    }
    
    return false;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Check if it is time to play
   * @return true if it is first or second half period.
   */
  public boolean canPlay(){
    
    return (period == FIRST_HALF || period == SECOND_HALF);
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return string view representation of this object
   */
  public String getStringView(){
    
    String view;
    
    switch(period){
      case PRE_GAME:
        view = "0";
        break;
      
      case FIRST_HALF:
      view = "1";
      break;
      
      case HALF_TIME:
      view = "2";
      break;
      
      case SECOND_HALF:
      view = "3";
      break;
      
      default:
      view = "0";
      break;
    }
    
    return view;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * @return component view
   */
  public JComponent getView(){
    
    if(view == null)
      view = new JLabel(getPeriodAsString());
      
    return view;
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Return period as text
   */
  private String getPeriodAsString(){
    
    switch(period){
      case PRE_GAME:
        return "Before game";
      
      case FIRST_HALF:
        return "First half";
      
      case HALF_TIME:
        return "Half time";
      
      case SECOND_HALF:
        return "Second half";
      
      default:
        return "End game";
    }
  }
  
  
  //---------------------------------------------------------------------------
  /**
   * Period changed update view
   */
  private void updateView(){
    
    if(view != null){
      view.setText(getPeriodAsString());
    }
      
  }
  
  
  //---------------------------------------------------------------------------
  // Private members
  /** Current game period */
  private int     period;
  /** Time before game starts in simulation steps */
  private int     prepareTime = 600;
  /** First and second half time in simulation steps */
  private int     playTime = 5*600;
  /** This object view */
  private JLabel  view;
  /** play period PRE_GAME identifier. */
  private static final int PRE_GAME     = 0;
  /** play period FIRST_HALF identifier.  */
  private static final int FIRST_HALF   = 1;
  /** play period HALF_TIME identifier. */
  private static final int HALF_TIME    = 2;
  /** play period SECOND_HALF identifier.   */
  private static final int SECOND_HALF  = 3;
  /** after game identifier.   */
  private static final int END_GAME  = 4;

}
