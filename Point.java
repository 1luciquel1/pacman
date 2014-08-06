public class Point { 
  private byte x;
  private byte y;
  
  public Point(final byte x, final byte y) { 
    this.x = x;
    this.y = y;
  }
  
  public Point() { 
    this.x = (byte) 0;
    this.y = (byte) 0;
  }
  
  public Point(final int x, final int y) { 
    this.x = (byte) x;
    this.y = (byte) y;
  }
  
  public byte getX() { 
    return x;
  }
  
  public byte getY() {
    return y;
  }
  
  public void setX(final byte newX) { 
    this.x = newX;
  }
  
  public void setY(final byte newY) { 
    this.y = newY;
  }
}