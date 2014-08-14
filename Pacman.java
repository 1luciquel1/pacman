import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Written by Ryan D'souza Brown University CS 015 Final Project Main Class of Pacman game
 */

public class Pacman extends JPanel {
  
  private Mode gameMode;
  private long modeStart;
  
  private final byte board[][] = getBoard();
  private final TheGhost[] theGhosts = new TheGhost[4];
  
  private static final String SPACE = "     ";
  
  private static final byte SCALE = 20;
  private static final byte PACMAN_SIZE = 15;
  private static final byte GHOST_SIZE = 20;
  private static final byte DOT_SIZE = 5;
  private static final byte ENERGIZER_SIZE = DOT_SIZE * 2;
  
  private static final int TIME_CHASE = 5; //Seconds 10
  private static final int TIME_SCATTER = 7;
  private static final int TIME_FRIGHTENED = 10; 
  private static final byte GHOST_RELEASE = 5;
  
  public static final byte WALL = 1 << 0;
  public static final byte FREE = 1 << 1;
  public static final byte DOT = 1 << 2;
  public static final byte ENERGIZER = 1 << 3;
  public static final byte PACMAN = 1 << 4;
  public static final byte GHOST = 1 << 5;
  public static final byte OUT = 1 << 6;
  
  private final Queue<TheGhost> ghostPenQ = new LinkedList<TheGhost>();
  
  private TheGhost redGhost, pinkGhost, blueGhost, orangeGhost;
  private ThePacman pacman;
  
  private Graphics2D theG;
  
  private Point ghostReleasePoint;
  private Point ghostSpawnPoint;
  
  private long pacmanScore = 0;
  private byte pacmanLives = 3;
  
  private final JLabel pacmanScoreLabel;
  private final JLabel pacmanLivesLabel;
  private final JLabel ghostModeLabel;
  private final JLabel nextGhostReleaseLabel;
  
  private boolean controlTouch = false;
  private boolean isChaseMode;
  
  private long ghostModeStart;
  private long hitEnergizerAt;
  private long ghostReleasedAt;
  
  /** Constructor, initializes JPanel and board */
  public Pacman() {
    super();
    setSize(new Dimension(400, 400));
    setMinimumSize(new Dimension(400, 400));
    setFocusable(true);
    requestFocusInWindow();
    
    pacmanScoreLabel = new JLabel("Score: " + pacmanScore, JLabel.RIGHT);
    pacmanScoreLabel.setForeground(Color.white);
    add(pacmanScoreLabel);
    
    pacmanLivesLabel = new JLabel(SPACE + "Lives: " + pacmanLives, JLabel.LEFT);
    pacmanLivesLabel.setForeground(Color.WHITE);
    add(pacmanLivesLabel);
    
    ghostModeLabel = new JLabel(SPACE + "Normal", JLabel.LEFT);
    ghostModeLabel.setForeground(Color.WHITE);
    add(ghostModeLabel);
    
    nextGhostReleaseLabel = new JLabel(SPACE + "Ghost Release", JLabel.LEFT);
    nextGhostReleaseLabel.setForeground(Color.WHITE);
    add(nextGhostReleaseLabel);
    
    gameMode = Mode.CHASE;
    modeStart = System.currentTimeMillis();
    
    initializeVariables();
    addKeyListener(new ControlListener());
    start();
  }
  
  /** Start the other threads */
  public void start() { 
    new Thread(new GameLogic()).start();
  }
  
  /** Initalizes pacman and ghosts start locations */
  public void initializeVariables() {
    Point ghostStart = null;
    
    for (byte i = 0; i < board.length; i++) {
      for (byte y = 0; y < board[i].length; y++) {
        // Pacman starting location
        if (board[i][y] == PACMAN) {
          pacman = new ThePacman(y, i, Color.YELLOW);
        }
        
        // Ghost starting location
        else if (board[i][y] == GHOST) {
          ghostStart = new Point(y, i);
        }
      }
    }
    
    final byte x = (byte) ghostStart.getX();
    final byte y = (byte) ghostStart.getY();
    
    // Left Inside
    redGhost = new TheGhost(Color.RED, x - 2, y, board, gameMode);
    board[redGhost.getY()][redGhost.getX()] = GHOST;
    theGhosts[0] = redGhost;
    ghostPenQ.add(redGhost);
    
    // Middle inside
    blueGhost = new TheGhost(Color.CYAN, x, y, board, gameMode);
    board[blueGhost.getY()][blueGhost.getX()] = GHOST;
    theGhosts[1] = blueGhost;
    ghostPenQ.add(blueGhost);
    ghostSpawnPoint = new Point(blueGhost.getX(), blueGhost.getY());
    
    // Right inside
    orangeGhost = new TheGhost(Color.ORANGE, x + 2, y, board, gameMode);
    board[orangeGhost.getY()][orangeGhost.getX()] = GHOST;
    theGhosts[2] = orangeGhost;
    ghostPenQ.add(orangeGhost);
    
    // Outside
    pinkGhost = new TheGhost(Color.PINK, x, y - 2, board, gameMode);
    // pinkGhost.setY(y - 2);
    board[pinkGhost.getY()][pinkGhost.getX()] = GHOST;
    theGhosts[3] = pinkGhost;
    ghostReleasePoint = new Point(pinkGhost.getX(), pinkGhost.getY());
    ghostReleasedAt = System.currentTimeMillis();
    pinkGhost.release();
    
    // for(int i = 0; i < theGhosts.length; i++)
    // System.out.println(theGhosts[i]);
    
    isChaseMode = true;
    ghostModeStart = System.currentTimeMillis();
  }
  
  /**
   * Returns a byte representing the item that the parameter's item will hit based on the parameter item's direction
   */
  private byte getItemInNextMove(final PacmanItem movingItem, final PacmanItem.Direction theDirection) {
    try {
      switch (theDirection) {
        case UP:
          return board[movingItem.getY() - 1][movingItem.getX()];
          
        case DOWN:
          return board[movingItem.getY() + 1][movingItem.getX()];
          
        case LEFT:
          return board[movingItem.getY()][movingItem.getX() - 1];
          
        case RIGHT:
          return board[movingItem.getY()][movingItem.getX() + 1];
          
        default:
          return Byte.MAX_VALUE;
      }
    } catch (Exception e) {
      return OUT;
    }
  }
  
  /** Return byte of item in that Point */
  public byte getItemAtPoint(final Point thePoint) {
    return board[(byte) thePoint.getY()][(byte) thePoint.getX()];
  }

  /** Moves the item parameter based on the direction parameter */
  public void moveItem(final ThePacman theItem, final PacmanItem.Direction theDirection) {
    controlTouch = false;
    
    if (theDirection == null) {
      return;
    }
    theItem.setFacingDirection(theDirection);
    final byte itemInNextDirection = getItemInNextMove(pacman, theDirection);
    
    if (itemInNextDirection == OUT) {
      return;
    }
    
    if (itemInNextDirection == GHOST) {
      if (isFrightened()) {
        eatGhost(theDirection);
      }
      else {
        hitGhost();
      }
      return;
    }
    
    if (itemInNextDirection == DOT) { 
      pacmanScore += 10;
    }
    
    if (itemInNextDirection != WALL) {
      board[pacman.getY()][pacman.getX()] = FREE;
      pacman.move(theDirection);
    }
    
    if (itemInNextDirection == ENERGIZER) {
      hitEnergizerAt = System.currentTimeMillis();
      pacmanScore += 100;
      gameMode = Mode.FRIGHTENED;
      modeStart = System.currentTimeMillis();
    }
    
    board[pacman.getY()][pacman.getX()] = PACMAN;
    
    updateLabels();
  }
  
  /** Eats the Ghost if it is not frightened mode */
  private void eatGhost() { 
    if(gameMode != Mode.FRIGHTENED) { 
      hitGhost();
      return;
    }
    final Point pacmanOnGhostPoint = pacman.getPoint();
    for (TheGhost theGhost : theGhosts) {
      if (theGhost.getPoint().equals(pacmanOnGhostPoint)) {
        System.out.println("EATEN:\t" + theGhost.toString());
        pacmanScore += 200;
        theGhost.returnToStartPosition();
        updateBoard(theGhost.getPoint(), FREE);
        updateBoard(theGhost.getPoint(), GHOST);
        ghostRespawn(theGhost);
      }
    }    
  }
  
  /** If Pacman eats a ghost on frightened mode */
  private void eatGhost(final PacmanItem.Direction theDirection) {
    final Point pacmanOriginalPoint = pacman.getPoint();
    pacman.move(theDirection);
    final Point pacmanOnGhostPoint = pacman.getPoint();
    
    for (byte i = 0; i < theGhosts.length; i++) {
      if (theGhosts[i].getPoint().equals(pacmanOnGhostPoint)) {
        pacmanScore += 200;
        ghostRespawn(theGhosts[i]);
        theGhosts[i].returnToStartPosition();
      }
    }
    
    // Make Pacman's old location free
    updateBoard(pacmanOriginalPoint, FREE);
    
    // Set Pacman's new location
    updateBoard(pacmanOnGhostPoint, PACMAN);
  }
  
  /** Paint method, called by repaint() */
  public void paintComponent(Graphics g) {
    theG = (Graphics2D) g;
    releaseGhosts();
    updateLabels();
    drawSquares();
  }
  
  /** Thread that has most of the game logic
    * Handles updated ghosts' board and BFA
    * and movements */
  private class GameLogic implements Runnable { 
    @Override
    public void run() { 
      while(true)  {
        eatGhost();
        hitGhost();
        if(getItemInNextMove(pacman, pacman.getDesiredDirection()) != WALL) {
          pacman.setFacingDirection(pacman.getDesiredDirection());
        }
        moveItem(pacman, pacman.getFacingDirection());
        
        final int modeTime = (int) ((System.currentTimeMillis() - modeStart)/1000);
        switch(gameMode) { 
          
          case FRIGHTENED:
            if(modeTime > TIME_FRIGHTENED) { 
            gameMode = Mode.CHASE;
            modeStart = System.currentTimeMillis();
          }
            break;
            
          case CHASE:
            if(modeTime > TIME_CHASE) { 
            gameMode = Mode.SCATTER;
            modeStart = System.currentTimeMillis();
          }
            break;
            
          case SCATTER:
            if(modeTime > TIME_SCATTER) {
            gameMode = Mode.CHASE;
            modeStart = System.currentTimeMillis();
          }
            break;
            
          default:
            break;
        }
        
        for(TheGhost theGhost : theGhosts) { 
          if(theGhost.isReleased()) { 
            theGhost.updateBoard(board);
            setValue(theGhost.getPoint(), (byte) (getItemAtPoint(theGhost.getPoint()) & (~GHOST)));
            theGhost.move(theGhost.getPoint(), gameMode);
            updateBoard(theGhost.getPoint(), GHOST);
          }
        }
        eatGhost();
        hitGhost();
        try { 
          Thread.sleep(100);
        }
        catch(Exception e) { 
          e.printStackTrace();
        }
        repaint();
      }
    }
  };
  
  private void setValue(final Point thePoint, final byte theValue) { 
    board[thePoint.getY()][thePoint.getX()] = theValue;
  }
  
  /**
   * If Pacman hits a ghost and it's not on frightened mode Move pacman back to initial position, decrement lives
   */
  public void hitGhost() {
    if(isFrightened()) { 
      return;
    }
    final Point pacmanOnGhostPoint = pacman.getPoint();
    for (TheGhost theGhost : theGhosts) {
      if (theGhost.getPoint().equals(pacmanOnGhostPoint)) {
        updateBoard(pacman.getPoint(), FREE);
        pacman.returnToStartPosition();
        updateBoard(pacman.getPoint(), FREE);
        
        pacmanLives--;
        updateLabels();
        return;
      }
    }   
  }
  
  /** Draws the entire board, including ghosts and pacman */
  public void drawSquares() {
    for (byte i = 0; i < board.length; i++) {
      for (byte y = 0; y < board[i].length; y++) {
        switch (board[i][y]) {
          case WALL:
            theG.setColor(Color.BLUE);
            theG.fillRect(y * SCALE, i * SCALE, SCALE, SCALE);
            break;
            
          case FREE:
            drawBlackSquare(i, y);
            break;
            
          case DOT:
            drawBlackSquare(i, y);
            theG.setColor(Color.WHITE);
            theG.fillOval(y * SCALE + 5, i * SCALE + 7, DOT_SIZE, DOT_SIZE);
            break;
            
          case ENERGIZER:
            drawBlackSquare(i, y);
            theG.setColor(Color.WHITE);
            theG.fillOval(y * SCALE + 5, i * SCALE + 7, ENERGIZER_SIZE, ENERGIZER_SIZE);
            break;
            
          case PACMAN:
            drawBlackSquare(i, y);
            theG.setColor(Color.YELLOW);
            theG.fillOval(y * SCALE, i * SCALE, PACMAN_SIZE, PACMAN_SIZE);
            break;
            
          case GHOST:
            break;
            
          default:
            drawBlackSquare(i, y);
            break;
        }
      }
    }
    for (byte i = 0; i < theGhosts.length; i++) {
      drawGhost(theGhosts[i]);
    }
  }
  
  /**
   * Listens to keyboard events, sets the facing direction based on those events Then moves the item in regards to the
   * facing direction
   */
  private class ControlListener implements KeyListener {
    public void keyPressed(KeyEvent e) {
      
      controlTouch = true;
      
      // Direction item will move in
      PacmanItem.Direction movingDirection;
      
      // Current location becomes nothing for Pacman
      board[pacman.getY()][pacman.getX()] = FREE;
      
      switch (e.getKeyCode()) {
        // LEFT
        case KeyEvent.VK_LEFT:
          movingDirection = PacmanItem.Direction.LEFT;
          break;
          
          // RIGHT
        case KeyEvent.VK_RIGHT:
          movingDirection = PacmanItem.Direction.RIGHT;
          break;
          
          // UP
        case KeyEvent.VK_UP:
          movingDirection = PacmanItem.Direction.UP;
          break;
          
          // DOWN
        case KeyEvent.VK_DOWN:
          movingDirection = PacmanItem.Direction.DOWN;
          break;
          
        default:
          movingDirection = null;
          break;
      }
      pacman.setDesiredDirection(movingDirection);
      //moveItem(pacman, movingDirection);
    }
    
    public void keyReleased(KeyEvent e) {
    }
    
    public void keyTyped(KeyEvent e) {
    }
  }
  
  /** Main method, creates frame and adds game to it */
  public static void main(String[] ryan) {
    JFrame theFrame = new JFrame("Pacman");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    theFrame.setSize(500, 500);
    
    theFrame.add(new Pacman());
    theFrame.setVisible(true);
  }
  
  /**
   * If it is time, removes next ghost from pen and places ghost at initial ghostReleasePoint
   */
  private void releaseGhosts() {
    if (ghostPenQ.size() != 0) {
      if ((System.currentTimeMillis() - ghostReleasedAt) / 1000 == GHOST_RELEASE)
        ghostLeavePen(ghostPenQ.remove());
    }
  }
  
  /**
   * Removes ghost from its position on the board, updates ghosts coordinates to that of initial point, updates board
   * to that value
   */
  private void ghostLeavePen(final TheGhost theGhost) {
    board[theGhost.getY()][theGhost.getX()] = FREE;
    theGhost.setX((byte) ghostReleasePoint.getX());
    theGhost.setY((byte) ghostReleasePoint.getY());
    board[theGhost.getY()][theGhost.getX()] = (byte) (GHOST | board[theGhost.getY()][theGhost.getX()]);
    ghostReleasedAt = System.currentTimeMillis();
    theGhost.release();
  }
  
  /** Moves Ghost back to pen */
  public void ghostRespawn(final TheGhost theEaten) {
    if(ghostPenQ.size() == 0) { 
      theEaten.setPoint(ghostSpawnPoint);
    }
    else { 
      theEaten.returnToStartPosition();
    }
    ghostPenQ.add(theEaten);
    updateBoard(theEaten.getPoint(), GHOST);
    ghostReleasedAt = System.currentTimeMillis();
    theEaten.setInPen();
  }
  
  /** Update board location with that Pacman type */
  public void updateBoard(final Point thePoint, final byte theItem) {
    if(thePoint.getY() >= board[0].length-1 || thePoint.getX() >= board.length-1) {
      System.out.println("UpdateBoard\t" + thePoint);
      return;
    }
    board[thePoint.getY()][thePoint.getX()] = (byte) (theItem | board[thePoint.getY()][thePoint.getX()]);
  }
  
  /** @return true if chase mode */
  public boolean isChaseMode() {
    return gameMode == Mode.CHASE;
  }
  
  /** Returns true if frightened */
  private boolean isFrightened() { 
    return gameMode == Mode.FRIGHTENED;
  }
  
  /** Draws the ghost in the parameter */
  private void drawGhost(TheGhost theGhost) {
    theG.setColor(theGhost.getColor());
    theG.fillRect(theGhost.getX() * SCALE, theGhost.getY() * SCALE, GHOST_SIZE, GHOST_SIZE);
  }
  
  /** Draws a black square at X and Y */
  private void drawBlackSquare(int x, int y) {
    theG.setColor(Color.BLACK);
    theG.fillRect(y * SCALE, x * SCALE, SCALE, SCALE);
  }
  
  /** Returns the board as a 2D array */
  public static byte[][] getBoard() {
    final int[][] theMap = cs015.fnl.PacmanSupport.SupportMap.getMap();
    final byte[][] theMapByte = new byte[theMap.length][theMap[0].length];
    
    for(int i = 0; i < theMap.length; i++) { 
      for(int y = 0; y < theMap[i].length; y++) { 
        theMapByte[(byte)i][(byte)y] = (byte) (1 << theMap[i][y]);
      }
    }
    return theMapByte;
  }
  
  /** Prints the board as a 2D array */
  public void printBoard() {
    for (byte y = 0; y < board.length; y++) {
      for (byte i = 0; i < board[y].length; i++) {
        System.out.print(board[y][i] + " ");
      }
      System.out.println();
    }
  }
  
  /** Updates the score, num lives, ghost pen release countdown, and ghost mode labels */
  private void updateLabels() {
    pacmanScoreLabel.setText("Score: " + pacmanScore);
    pacmanLivesLabel.setText(SPACE + "Lives: " + pacmanLives + "     ");
    
    if (ghostPenQ.size() >= 0) {
      int timeToRelease = (int) GHOST_RELEASE - (int) ((System.currentTimeMillis() - ghostReleasedAt) / 1000);
      nextGhostReleaseLabel.setText(SPACE + "Ghost Release: " + timeToRelease);
    } 
    else if (ghostPenQ.size() < 0 || (GHOST_RELEASE - ((System.currentTimeMillis() - ghostReleasedAt) / 1000)) < 0) {
      nextGhostReleaseLabel.setText(SPACE + "Ghost Release: N/A");
    }
    
    final int currentTime = (int) ((System.currentTimeMillis() - modeStart) / 1000);
    if (gameMode == Mode.FRIGHTENED) {
      ghostModeLabel.setText(SPACE + "Frightened Mode: " + (TIME_FRIGHTENED - currentTime));
    }
    else if (gameMode == Mode.CHASE) {
      ghostModeLabel.setText(SPACE + "Chase Mode: " + (TIME_CHASE - currentTime));
    }
    else if(gameMode == Mode.SCATTER) {
      ghostModeLabel.setText(SPACE + "Scatter Mode: " + (TIME_SCATTER - currentTime));
    }
  }
}