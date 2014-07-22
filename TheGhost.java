import java.awt.Color;

public class TheGhost extends PacmanItem
{
  private Color theColor;
  private long startPenTime;
  
  public TheGhost(Color theColor, int x, int y)
  {
    super(x, y);
    this.theColor = theColor;
    startPenTime = System.currentTimeMillis();
  }
  
  public long getPenTime() { return this.startPenTime; }
  public void setPenTime(long time) { this.startPenTime = time; }
  
  public Color getColor() { return this.theColor; }
  public void setColor(Color tC) { this.theColor = tC; }
  
  public String toString() { return "GHOST:\t" + theColor.toString() + "\tX: " + x + "\tY: " + y; }
}