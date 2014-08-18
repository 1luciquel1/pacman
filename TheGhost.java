import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Written by Ryan D'souza Brown University CS 015 Final Project Represents the ghosts in the Pacman game
 */

public class TheGhost extends PacmanItem {
  
  private static final Color FRIGHTENED_COLOR = Color.GREEN;
  private static final Point[] corners = {new Point(1, 21), new Point(21, 1), new Point(21, 21), new Point(1, 1)};
  
  private static final byte WALL = -1;
  private static final byte UNEXPLORED = Byte.MAX_VALUE;
  private static final byte GHOST = 0;
  private static final byte PACMAN = -2;
  private static final byte CORNER = -3;
  
  private static final byte SIZE = 23;
  
  private final byte[][] theBoard = new byte[SIZE][SIZE];
  private final Point aPoint = new Point();
  private final Queue<Point> prospectivePoints = new LinkedList<Point>();
  
  private final Color startColor;
  
  private Mode gameMode;
  private Direction randomDirection;
  
  private Point cornerPoint;
  private Point pacmanLoc = null;
  private Point cornerLoc = null;
  
  private final byte randomDistFromPacman;
  
  private byte lookFor;
  private long startPenTime;
  private boolean isReleased = false;
  
  /** Constructor */
  public TheGhost(Color theColor, int x, int y, final byte[][] pacmanGrid, final Mode gameMode) {
    super((byte)x, (byte)y, theColor);
    this.startColor = theColor;
    randomDistFromPacman = 0; //(byte) theGenerator.nextInt(4);
    cornerPoint = getCorner(new Point(x, y));
    cornerLoc = cornerPoint;
    startPenTime = System.currentTimeMillis();
    this.updateBoard(pacmanGrid);
  }
  
  /** Updates the ghost's internal board with the ghost's custom values */
  public void updateBoard(final byte[][] pacmanBoard) {
    
    final Point pacmanP = new Point();
    
    for (byte i = 0; i < pacmanBoard.length; i++) {
      for (byte y = 0; y < pacmanBoard[i].length; y++) {
        if (pacmanBoard[i][y] == Pacman.WALL) {
          this.theBoard[i][y] = this.WALL;
        }
        else if(pacmanBoard[i][y] == Pacman.PACMAN) { 
          this.theBoard[i][y] = this.PACMAN;
          pacmanP.setX(i);
          pacmanP.setY(y);
        }
        else {
          this.theBoard[i][y] = this.UNEXPLORED;
        }
      }
    }
    this.theBoard[this.y][this.x] = GHOST;
    this.theBoard[cornerPoint.getY()][cornerPoint.getX()] = CORNER;
    
    reassignPacman(pacmanP);
    
    aPoint.setX(x);
    aPoint.setY(y);
  }
  
  /** Re-assigns pacman to a point a random distance from Pacman's actual location
    * Use is so that all the ghosts don't go for exactly Pacman */
  private void reassignPacman(final Point pacmanPoint){
    for(Direction theDirection : theDirections) { 
      final Point directionDistance = getProspectivePoint(pacmanPoint, theDirection, randomDistFromPacman);
      if(itemAtPoint(directionDistance) != WALL) { 
        this.theBoard[pacmanPoint.getY()][pacmanPoint.getX()] = UNEXPLORED;
        this.theBoard[directionDistance.getY()][directionDistance.getX()] = PACMAN;
        return;
      }
    }
  }
  
  /** Move and change colors according to gameMode */
  public void move(final Point ghostLocation, final Mode gameMode) { 
    this.gameMode = gameMode;
    setColor();
    
    if(gameMode == Mode.CHASE) {
      lookFor = PACMAN;
      startBreadthFirstAlgorithm(ghostLocation);
    }
    
    else if(gameMode == Mode.FRIGHTENED) {
      lookFor = CORNER;
      startBreadthFirstAlgorithm(ghostLocation);
    }
    
    else if(gameMode == Mode.SCATTER) {
      scatterMode(ghostLocation);
    }
  }
  
  /**Scatter mode: Move randomly */
  private void scatterMode(final Point currentLoc) { 
    
    if(randomDirection == null) { 
      randomDirection = super.getRandomDirection();
    }
    
    if(itemAtPoint(randomDirection, currentLoc) == WALL) { 
      randomDirection = super.getRandomDirection();
      scatterMode(currentLoc);
    }
    else {
      super.move(randomDirection);
    }
  }
  
  /**Change Ghost color based upon game mode */
  private void setColor() { 
    if(gameMode == Mode.FRIGHTENED) {
      theColor = FRIGHTENED_COLOR;
    }
    else { 
      theColor = startColor;
    }
  }
  
  /**
   * Checks all 4 directions around the point If that item does not have my number and it's not a wall 
   * Add it to the queue and set its number to mine + 1
   */
  private void availableInDirections(final Point current) {
    for (Direction theDirection : theDirections) {
      final Point newPoint = getNewPoint(current, theDirection);
      
      if(newPoint.getX() >= theBoard.length || newPoint.getY() >= theBoard[0].length) { 
        //Skip
      }
      else if(newPoint.getX() < 0 || newPoint.getY() < 0) { 
        //Skip
      }
      
      else if(itemAtPoint(newPoint) == lookFor) {
        setValue(newPoint, itemAtPoint(current) + 1);
        
        if(gameMode == Mode.CHASE) {
          pacmanLoc = newPoint;
        }
        else if(gameMode == Mode.FRIGHTENED) { 
          cornerLoc = newPoint;
        }
        prospectivePoints.clear();
        return;
      }
      
      // If the value in that direction does not have the value I currently have
      // and is not a wall
      else if (itemAtPoint(newPoint) == UNEXPLORED) {
        // Add it to the queue
        prospectivePoints.add(newPoint);
        
        // Increment its value from mine
        setValue(newPoint, itemAtPoint(current) + 1);
      }
    }
  }
  
  /** Starts the BFA to find a point */
  private void startBreadthFirstAlgorithm(final Point startPoint) {
    if(startPoint.equals(cornerPoint)) {
      cornerPoint = getCorner(startPoint);
      return;
    }
    
    prospectivePoints.clear();
    prospectivePoints.add(startPoint);
    
    while (!prospectivePoints.isEmpty()) {
      availableInDirections(prospectivePoints.remove());
    }
    
    if(pacmanLoc != null) { 
      super.move(pacmanToGhost());
    }
  }
  
  /** Returns the direction to a get to the correct item. Item is based upon game mode */
  private Direction pacmanToGhost() { 
    byte itemAtNow = 0;
    if(gameMode == Mode.CHASE) {
      itemAtNow = itemAtPoint(pacmanLoc);
    }
    else if(gameMode == Mode.FRIGHTENED) { 
      itemAtNow = itemAtPoint(cornerLoc);
    }
    
    Point workBackwards = null;
    if(gameMode == Mode.CHASE) {
      workBackwards = pacmanLoc;
    }
    else if(gameMode == Mode.FRIGHTENED) { 
      workBackwards = cornerLoc;
    }
    
    Direction moveDirection = null;
    
    while(itemAtNow != 0) { 
      for(Direction theDirection : theDirections) { 
        final Point newPoint = getNewPoint(workBackwards, theDirection);
        final byte itemAtNewPoint = itemAtPoint(newPoint);
        if(itemAtNewPoint == (itemAtNow - 1) && itemAtNewPoint != this.WALL) {
          moveDirection = theDirection; 
          workBackwards = newPoint;
          itemAtNow = itemAtPoint(workBackwards);
        }
      }
    }
    return getOppositeDirection(moveDirection);
  }
  
  /** Updates the board at the given Point with the given number */
  private void updateBoard(final Point thePoint, final byte num) {
    theBoard[thePoint.getY()][thePoint.getX()] = num;
  }
  
  /** Returns the item at a Point given the Point and its Direction */
  private byte itemAtPoint(final Direction theDirection, final Point thePoint) {
    return itemAtPoint(super.getNewPoint(thePoint, theDirection));
  }
  
  /** Returns the item at a Point */
  private byte itemAtPoint(final Point thePoint) {
    if(thePoint.getX() >= theBoard.length || thePoint.getY() >= theBoard[0].length
         ||  thePoint.getX() < 0 || thePoint.getY() < 0) { 
      if(gameMode == Mode.CHASE) {
        return PACMAN;
      }
      else if(gameMode == Mode.FRIGHTENED) { 
        return CORNER;
      }
      else { 
        return PACMAN;
      }
    }
    return theBoard[(byte) thePoint.getY()][(byte) thePoint.getX()];
  }
  
  /** Set item at Point to a certain value */
  public void setValue(final Point thePoint, final int theNum) {
    theBoard[(byte) thePoint.getY()][(byte) thePoint.getX()] = (byte) theNum;
  }
  
  /** Returns true if the ghost has been released from the pen */
  public boolean isReleased() { 
    return isReleased;
  }
  
  /** Release the ghost from the pen */
  public void release() { 
    isReleased = true;
  }
  
  /** Put the ghost in the pen */
  public void setInPen() { 
    isReleased = false;
  }
  
  /** @return timeTheGhost was in the pen */
  public long getPenTime() {
    return this.startPenTime;
  }
  
  /** Returns the Point for a random corner */
  private Point getCorner(final Point currentPosition) { 
    final Point newPoint = corners[theGenerator.nextInt(corners.length)];
    if(currentPosition.equals(newPoint)) {
      return getCorner(currentPosition);
    }
    return newPoint;
  }
  
  /** Print the board */
  public void printBoard() {
    final DecimalFormat df = new DecimalFormat("00");
    
    for (byte i = 0; i < theBoard.length; i++) {
      for (byte y = 0; y < theBoard[i].length; y++) {
        if (theBoard[i][y] == WALL) {
          System.out.print("XX\t");
        } 
        else if (theBoard[i][y] == UNEXPLORED) {
          System.out.print("--\t");
        } 
        else {
          System.out.print(df.format(theBoard[i][y]) + "\t");
        }
      }
      System.out.println();
    }
    System.out.println();
  }
  
  @Override
  public String toString() {
    return "GHOST:\t" + name + "\tX: " + x + "\tY: " + y;
  }
}