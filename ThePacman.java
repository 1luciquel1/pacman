import java.awt.Color;

/** Written by Ryan D'souza
  * Brown University CS 015 Final Project 
  * Represents the Pacman */

public class ThePacman extends PacmanItem {
  public ThePacman(byte x, byte y, Color theColor) {
    super(x, y, theColor);
  }
  
  @Override
  public String toString() { 
    return "Pacman. X: " + this.x + "\tY: " + this.y;
  }
}