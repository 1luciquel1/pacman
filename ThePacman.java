import java.awt.Color;

/** Written by Ryan D'souza
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
