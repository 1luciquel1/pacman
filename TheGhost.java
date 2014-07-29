import java.awt.Color;

/** Written by Ryan D'souza
  * Brown University CS 015 Final Project 
  * Represents the ghosts in the Pacman game */

public class TheGhost extends PacmanItem {
  private Color theColor;
  private long startPenTime;
  
  /** Constructor */
  public TheGhost(Color theColor, int x, int y) {
    super(x, y, theColor);
    
    //For the time the ghost is in the pen
    startPenTime = System.currentTimeMillis();
  }
  
  /** @return timeTheGhost was in the pen */
  public long getPenTime() { 
    return this.startPenTime; 
  }
  public void setPenTime(long time) { 
    this.startPenTime = time; 
  }
  
  public String toString() {
    return "GHOST:\t" + name + "\tX: " + x + "\tY: " + y;
  }
}