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
  
  private static final int SIZE = 23;
  private final int[][] theBoard = new int[SIZE][SIZE];
  
  private static final int WALL = -1;
  private static final int UNEXPLORED = Integer.MAX_VALUE;
  private static final int GHOST = 0;
  
  public void updateGrid(final int[][] pacmanBoard) { 
    for(int i = 0; i < pacmanBoard.length; i++) {
      for(int y = 0; y < pacmanBoard[i].length; y++) { 
        if(pacmanBoard[i][y] == Pacman.WALL) {
          this.theBoard[i][y] = this.WALL;
        }
        else {
          this.theBoard[i][y] = this.UNEXPLORED;
        }
      }
    }
    this.theBoard[this.y][this.x] = GHOST;
  }
  
  private int counter = 0;
  
  /** Checks all 4 directions around the point
    * If that item does not have my number and it's not a wall
    * Add it to the queue and set its number to mine + 1 */
  public void availableInDirections(final Point current) {
    
    //Go through all the directions
    for(Direction theDirection : theDirections) { 
      
      final Point newPoint = getNewPoint(current, theDirection);
      
      //If the value in that direction does not have the value I currently have
      //and is not a wall
      if(itemAtPoint(newPoint) != itemAtPoint(current) && itemAtPoint(newPoint) != WALL) {
        //Add it to the queue
        prospectivePoints.add(newPoint);
        
        //Increment its value from mine
        setValue(newPoint, itemAtPoint(current) + 1);
      }
    }
  }
  
  /** Updates the board at the given Point given the next Direction and the number */
  public void updateBoard(final Point thePoint, final Direction theDirection, final int num) { 
    updateBoard(super.getNewPoint(thePoint, theDirection), num);
  }
  
  /** Updates the board at the given Point with the given number */
  public void updateBoard(final Point thePoint, final int num) { 
    theBoard[(int) thePoint.getY()][(int) thePoint.getX()] = num;
  }
  
  /** Returns the item at a Point given the Point and its Direction */
  public int itemAtPoint(final Direction theDirection, final Point thePoint) { 
    return itemAtPoint(super.getNewPoint(thePoint, theDirection));
  }
  
  /** Returns the item at a Point */
  public int itemAtPoint(final Point thePoint) { 
    return theBoard[(int)thePoint.getY()][(int) thePoint.getX()];
  }
    /** Set item at Point to a certain value */
  public void setValue(final Point thePoint, final int theNum) { 
    theBoard[(int)thePoint.getY()][(int) thePoint.getX()] = theNum;
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
  
  /** Constructor */
  public TheGhost(Color theColor, int x, int y, final int[][] pacmanGrid) {
    super(x, y, theColor);
    
    //For the time the ghost is in the pen
    startPenTime = System.currentTimeMillis();
    
    this.updateGrid(pacmanGrid);
    
    availableInDirections(getPoint());
    
    System.out.println("GHOST\t" + x + "\t" + y);
    Point[] p = getPoints();
    
    for(Point p1 : p) { 
      System.out.println("AVAILABLE\t" + p1.getX() + "\t" + p1.getY());
    }
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
  public void printBoard() {
    for(int i = 0; i < theBoard.length; i++) {
      for(int y = 0; y < theBoard[i].length; y++) {
        System.out.print(theBoard[i][y] + "\t");
      }
      System.out.println("");
    }
  }
}