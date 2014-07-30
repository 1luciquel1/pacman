import java.awt.Color;
import java.awt.Point;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Arrays;
/** Written by Ryan D'souza
  * Brown University CS 015 Final Project 
  * Represents the ghosts in the Pacman game */

public class TheGhost extends PacmanItem {
  private Color theColor;
  private long startPenTime;
  private final Queue<Point> prospectivePoints = new LinkedList<Point>();
  
  /** Constructor */
  public TheGhost(Color theColor, int x, int y) {
    super(x, y, theColor);
    
    //For the time the ghost is in the pen
    startPenTime = System.currentTimeMillis();
  }
  
  public Queue<Point> getProspectivePoints() {
    return this.prospectivePoints;
  }
  
  public void addPoint(final Point thePoint) {
    prospectivePoints.add(thePoint);
  }
  
  public void clearQ() {
    this.prospectivePoints.removeAll(prospectivePoints);
  }
  
  public Point getFirst() {
    return this.prospectivePoints.remove();
  }
  
  public Point getButKeepFirst() {
    return this.prospectivePoints.peek();
  }
  
  public void addPoints(final Point[] thePoints) {
    this.prospectivePoints.addAll(Arrays.asList(thePoints));
  }
  
  public Point[] getPoints() {
    return prospectivePoints.toArray(new Point[prospectivePoints.size()]);
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