import java.awt.Color;
import java.awt.Point;

/** Written by Ryan D'souza
  * Brown University CS 015 Final Project 
  * Represents a Pacman object
  * ie. ghosts or Pacman */

public abstract class PacmanItem {
  protected int x, y;
  protected Direction facingDirection;
  protected int startX, startY;
  protected Color theColor;
  protected String name = "";
  
  /** Constructor */
  public PacmanItem(final int x, final int y, final Color theColor){
    this.x = x;
    this.y = y;
    this.theColor = theColor;
    this.name = getName();
    
    this.startX = x;
    this.startY = y;
    
    facingDirection = Direction.UP;
  }
  
  /** Updates the direction and either the X or Y coordinate of the object
    * depending on the direction it is moving in 
    @param direction to move in */
    public void move(Direction theD){
      switch(theD) {
        case UP:
          this.y--;
          facingDirection = Direction.UP;
          break;
          
        case DOWN:
          this.y++;
          facingDirection = Direction.DOWN;
          break;
          
        case LEFT:
          this.x--;
          facingDirection = Direction.LEFT;
          break;
          
        case RIGHT:
          this.x++;
          facingDirection = Direction.RIGHT;
          break;
          
        default:
          break;
      }
    }
    
    /** @return ProspectivePoint if the item were to move in that direction */
    public Point getProspectivePoint(final Direction theDirection) {
      switch(theDirection) {
        case UP:
          return new Point(x, y - 1);
        case DOWN:
          return new Point(x, y + 1);
        case LEFT:
          return new Point(x - 1, y);
        case RIGHT:
          return new Point(x + 1, y);
        default:
          return null;
      }
    }
    
    /** Returns a new Point from the given point and the direction */
    public static Point getNewPoint(final Point theOriginal, final Direction theDirection) { 
      switch(theDirection) { 
                case UP:
          return new Point((int) theOriginal.getX(), (int) theOriginal.getY() - 1);
        case DOWN:
          return new Point((int) theOriginal.getX(), (int) theOriginal.getY() + 1);
        case LEFT:
          return new Point((int) theOriginal.getX() - 1, (int) theOriginal.getY());
        case RIGHT:
          return new Point((int) theOriginal.getX() + 1, (int) theOriginal.getY());
        default:
          return null;
      }
    }
    
    /** @param new point */
    public void setPoint(final Point thePoint) {
      this.x = (int) thePoint.getX();
      this.y = (int) thePoint.getY();
    }
    
    /** @return colorOfItem */  
    public Color getColor() { return this.theColor; }
    
    /** @param colorOfitem */
    public void setColor(Color tC) { 
      this.theColor = tC; 
    }
    
    /** @return startingXPosition */
    public int getStartX() {
      return this.startX; 
    }
    
    /** @return startingYPosition */
    public int getStartY() { 
      return this.startY;
    }
    
    /** Returns the item to initial position by
      * setting X and Y coordinates to the ones first given in the constructor */
    public void returnToStartPosition() {
      this.x = this.startX;
      this.y = this.startY;
      this.facingDirection = Direction.UP;
    }
    
    /** @return direction the item is facing */
    public Direction getFacingDirection() {
      return facingDirection;
    }
    
    /** @param directionToFace */
    public void setFacingDirection(Direction facing) {
      this.facingDirection = facing;
    }
    
    /** Four possible directions to move in */
    public enum Direction {
      UP, DOWN, LEFT, RIGHT; 
    }
    
    /** @return item x coordinate */
    public int getX() {
      return this.x;
    }
    
    /** @return item y coordinate */
    public int getY() {
      return this.y;
    }
    
    /** @param item's new x coordinate */
    public void setX(int x) {
      this.x = x;
    }
    
    /** @param item's new y coordinate */
    public void setY(int y) {
      this.y = y;
    }
    
    /** @return Pointform of object's location */
    public Point getPoint() {
      return new Point(x, y);
    }
    
    /** Sets the name of the item based on the color */
    private String getName() {
      if(theColor == Color.YELLOW)
        return "Yellow";
      else if(theColor == Color.CYAN)
        return "Blue (Cyan)";
      else if(theColor == Color.PINK)
        return "Pink";
      else if(theColor == Color.ORANGE)
        return "Orange";
      else if(theColor == Color.RED)
        return "Red";
      return "Error";
    }
}