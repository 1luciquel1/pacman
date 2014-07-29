import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

/** Written by Ryan D'souza
  * Brown University CS 015 Final Project 
  * Main Class of Pacman game*/

public class Pacman extends JPanel {
  
  private static final int board[][] = getBoard();
  private static final TheGhost[] theGhosts = new TheGhost[4];
  
  private static final String SPACE = "     ";
  
  private static final int SCALE = 20;
  private static final int PACMAN_SIZE = 15;
  private static final int GHOST_SIZE = 20;
  private static final int DOT_SIZE = 5;
  private static final int ENERGIZER_SIZE = DOT_SIZE * 2;
  
  private static final int CALCULATION_NORMAL = Integer.MAX_VALUE/10;
  private static final int CALCULATION_ENERGIZER = Integer.MAX_VALUE/30; //80;
  
  private static final int FRIGHTENED = 800; //SHOULD BE 7 SECONDS
  private static final int CHASE = 20; //20 Seconds
  private static final int SCATTER = 7; //7 Seconds
  
  private static final int WALL = 0;
  private static final int FREE = 1;
  private static final int DOT = 2;
  private static final int ENERGIZER = 3;
  private static final int PACMAN = 4;
  private static final int GHOST = 5;
  private static final int OUT = 6;
  
  private static final Queue<TheGhost> ghostPenQ = new LinkedList<TheGhost>();
  
  private static TheGhost redGhost, pinkGhost, blueGhost, orangeGhost;
  private static ThePacman pacman;
  
  private static Graphics2D theG;
  
  private static final int GHOST_RELEASE = 5; //Release ghost every 5 seconds
  private static Point ghostReleasePoint;
  private static Point ghostSpawnPoint;
  
  private static int pacmanScore = 0;
  private static int pacmanLives = 3;
  
  private static JLabel pacmanScoreLabel;
  private static JLabel pacmanLivesLabel;
  private static JLabel ghostModeLabel;
  private static JLabel nextGhostReleaseLabel;
  
  private boolean controlTouch = false;
  private static boolean isChaseMode; 
  
  private static long ghostModeStart;
  private static long hitEnergizerAt;
  private static long ghostReleasedAt;
  
  /** Constructor, initializes JPanel and board */
  public Pacman() {
    super();
    setSize(new Dimension(400, 400));
    setMinimumSize(new Dimension(400, 400));
    addKeyListener(new ControlListener());
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
    
    initializeVariables();
  }
  
  /** Initalizes pacman and ghosts start locations */
  public static void initializeVariables() {
    Point ghostStart = null;
    
    for(int i = 0; i < board.length; i++) {
      for(int y = 0; y < board[i].length; y++) {
        //Pacman starting location
        if(board[i][y] == PACMAN)
          pacman = new ThePacman(y, i, Color.YELLOW);
        
        //Ghost starting location
        else if(board[i][y] == GHOST)
          ghostStart = new Point(y, i);
      }
    }
    
    int x = (int) ghostStart.getX();
    int y = (int) ghostStart.getY();
    
    //Left Inside
    redGhost = new TheGhost(Color.RED, x, y);
    redGhost.setX(x - 2);
    board[redGhost.getY()][redGhost.getX()] = GHOST;
    theGhosts[0] = redGhost;
    ghostPenQ.add(redGhost);
    
    //Middle inside
    blueGhost = new TheGhost(Color.CYAN, x, y);
    board[blueGhost.getY()][blueGhost.getX()] = GHOST;
    theGhosts[1] = blueGhost;
    ghostPenQ.add(blueGhost);
    ghostSpawnPoint = new Point(blueGhost.getX(), blueGhost.getY());
    
    //Right inside
    orangeGhost = new TheGhost(Color.ORANGE, x, y);
    orangeGhost.setX(x + 2);
    board[orangeGhost.getY()][orangeGhost.getX()] = GHOST;
    theGhosts[2] = orangeGhost;
    ghostPenQ.add(orangeGhost);
    
    //Outside
    pinkGhost = new TheGhost(Color.PINK, x, y);
    pinkGhost.setY(y - 2);
    board[pinkGhost.getY()][pinkGhost.getX()] = GHOST;
    theGhosts[3] = pinkGhost;
    ghostReleasePoint = new Point(pinkGhost.getX(), pinkGhost.getY());
    ghostReleasedAt = System.currentTimeMillis();
    
    //for(int i = 0; i < theGhosts.length; i++)
    //System.out.println(theGhosts[i]);
    
    isChaseMode = true;
    ghostModeStart = System.currentTimeMillis();
  }
  
  /** Returns an int representing the item that the parameter's item will hit
    * based on the parameter item's direction */
  private static int getItemInNextMove(final PacmanItem movingItem, 
                                       final PacmanItem.Direction theDirection) {
    try {
      switch(theDirection) {
        case UP:
          return board[movingItem.getY()-1][movingItem.getX()];
          
        case DOWN:
          return board[movingItem.getY()+1][movingItem.getX()];
          
        case LEFT:
          return board[movingItem.getY()][movingItem.getX()-1];
          
        case RIGHT:
          return board[movingItem.getY()][movingItem.getX()+1];
          
        default:
          return Integer.MAX_VALUE;
      }
    }
    catch(Exception e) {
      return OUT;
    }
  }
  
  /** Return int of item in that Point */
  public static int getItemAtPoint(final Point thePoint) { 
    return board[(int) thePoint.getY()][ (int) thePoint.getX()];
  }
  
  /** Returns an array of points in 1 step in any direction that the ghost can move */
  public static Point[] getValidNeighbors(final TheGhost theGhost) {
    ArrayList<Point> thePoints = new ArrayList<Point>();
    
    if(getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.UP)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.UP));
    if(getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.DOWN)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.DOWN));
    if(getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.LEFT)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.LEFT));
    if(getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.RIGHT)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.RIGHT));
    
    return thePoints.toArray(new Point[thePoints.size()]);
  }
  
  /** Returns an array of points that the Point can move (anything but a wall) */
  public static Point[] getValidNeighbors(final Point thePoint) {
    return getValidNeighbors(new TheGhost(null, (int) thePoint.getX(), (int) thePoint.getY()));
  }
  
  /** Moves the item parameter based on the direction parameter */
  public void moveItem(final PacmanItem theItem, final PacmanItem.Direction theDirection) {
    controlTouch = false;
    
    if(theDirection == null)
      return;
    
    theItem.setFacingDirection(theDirection);
    
    final int itemInNextDirection = getItemInNextMove(pacman, theDirection);
    
    if(itemInNextDirection == OUT)
      return; 
    
    if(itemInNextDirection == GHOST) {
      if(ghostMode())
        eatGhost(theDirection);
      else
        hitGhost();
      return;
    }
    
    if(itemInNextDirection == DOT)
      pacmanScore += 10;
    
    if(itemInNextDirection != WALL) {
      board[pacman.getY()][pacman.getX()] = FREE;
      pacman.move(theDirection);
    }
    
    if(itemInNextDirection == ENERGIZER) {
      hitEnergizerAt = System.currentTimeMillis();
      pacmanScore += 100;
    }
    
    board[pacman.getY()][pacman.getX()] = PACMAN;
    
    updateLabels();
    repaint();
  }
  
  /** If Pacman eats a ghost on frightened mode */
  private void eatGhost(final PacmanItem.Direction theDirection) {   
    pacmanScore += 200;
    
    final Point pacmanOriginalPoint = pacman.getPoint();
    pacman.move(theDirection);
    final Point pacmanOnGhostPoint = pacman.getPoint();
    
    for(int i = 0; i < theGhosts.length; i++) {
      if(theGhosts[i].getPoint().equals(pacmanOnGhostPoint)) {
        ghostRespawn(theGhosts[i]);
      }
    }
    
    //Make Pacman's old location free
    updateBoard(pacmanOriginalPoint, FREE);
    
    //Set Pacman's new location
    updateBoard(pacmanOnGhostPoint, PACMAN);
  }
  
  /** Paint method, called by repaint() */
  public void paintComponent(Graphics g) {
    theG = (Graphics2D) g;
    releaseGhosts();
    drawSquares();
    
    moveItem(pacman, pacman.getFacingDirection());
    try { 
      int temp = 0;
      
      if(((System.currentTimeMillis() - hitEnergizerAt)/1000) <= 5) {
        for(int i = 0; i < CALCULATION_ENERGIZER && !controlTouch; i++)
          temp += i;
      }
      else {
        for(int i = 0; i < CALCULATION_NORMAL && !controlTouch; i++)
          temp += i;
      }
      repaint(); 
    } 
    catch(Exception e) { e.printStackTrace(); }
  }
  
  /** If Pacman hits a ghost and it's not on frightened mode
    * Move pacman back to initial position, decrement lives */
  public void hitGhost() {
    updateBoard(pacman.getPoint(), FREE);
    pacman.returnToStartPosition();
    updateBoard(pacman.getPoint(), PACMAN);
    pacmanLives--;
    updateLabels();
    repaint();
  }
  
  /** Draws the entire board, including ghosts and pacman */
  public static void drawSquares() {
    for(int i = 0; i < board.length; i++) {
      for(int y = 0; y < board[i].length; y++) {
        switch(board[i][y]) {
          case WALL :
            theG.setColor(Color.BLUE);
            theG.fillRect(y * SCALE, i * SCALE, SCALE, SCALE);
            break;
            
          case FREE :
            drawBlackSquare(i, y);
            break;
            
          case DOT :
            drawBlackSquare(i, y);
            theG.setColor(Color.WHITE);
            theG.fillOval(y * SCALE + 5, i * SCALE + 7, DOT_SIZE, DOT_SIZE);
            break;    
            
          case ENERGIZER :
            drawBlackSquare(i, y);
            theG.setColor(Color.WHITE);
            theG.fillOval(y * SCALE + 5, i * SCALE + 7, ENERGIZER_SIZE, ENERGIZER_SIZE);
            break;
            
          case PACMAN :
            drawBlackSquare(i, y);
            theG.setColor(Color.YELLOW);
            theG.fillOval(y * SCALE, i * SCALE, PACMAN_SIZE, PACMAN_SIZE);
            break;
            
          case GHOST :
            break;
            
          default:
            drawBlackSquare(i, y);
            break;
        }
      }
    }
    for(int i = 0; i < theGhosts.length; i++) {
      drawGhost(theGhosts[i]);
    }
  }
  
  /**Listens to keyboard events, sets the facing direction based on those events
    * Then moves the item in regards to the facing direction */
  private class ControlListener implements KeyListener {
    public void keyPressed(KeyEvent e) { 
      
      controlTouch = true;
      
      //Direction item will move in
      PacmanItem.Direction movingDirection;
      
      //Current location becomes nothing for Pacman
      board[pacman.getY()][pacman.getX()] = FREE;
      
      switch(e.getKeyCode()) {
        //LEFT
        case KeyEvent.VK_LEFT:
          movingDirection = PacmanItem.Direction.LEFT;
          break;
          
          //RIGHT
        case KeyEvent.VK_RIGHT:
          movingDirection = PacmanItem.Direction.RIGHT;
          break;
          
          //UP
        case KeyEvent.VK_UP:
          movingDirection = PacmanItem.Direction.UP;
          break;
          
          //DOWN
        case KeyEvent.VK_DOWN:
          movingDirection = PacmanItem.Direction.DOWN;
          break;
          
        default: 
          movingDirection = null;
          break;
      }
      
      moveItem(pacman, movingDirection);
    }
    public void keyReleased(KeyEvent e) { }
    public void keyTyped(KeyEvent e) { }
  }
  
  /** Main method, creates frame and adds game to it */
  public static void main(String[] ryan) {
    JFrame theFrame = new JFrame("Pacman");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    theFrame.setSize(500, 500);
    
    theFrame.add(new Pacman());
    theFrame.setVisible(true);
  }
  
  /** If it is time, removes next ghost from pen and places ghost at
    * initial ghostReleasePoint */
  private static void releaseGhosts() {
    if(ghostPenQ.size() != 0)
      if((System.currentTimeMillis() - ghostReleasedAt)/1000 == GHOST_RELEASE)
      ghostLeavePen(ghostPenQ.remove());
  }
  
  /** Removes ghost from its position on the board, updates 
    * ghosts coordinates to that of initial point, 
    * updates board to that value */
  private static void ghostLeavePen(final TheGhost theGhost) {
    board[theGhost.getY()][theGhost.getX()] = FREE;
    theGhost.setX((int) ghostReleasePoint.getX());
    theGhost.setY((int) ghostReleasePoint.getY());
    board[theGhost.getY()][theGhost.getX()] = GHOST;
    ghostReleasedAt = System.currentTimeMillis();
  }
  
  /** Moves Ghost back to pen */
  public void ghostRespawn(final TheGhost theEaten) {
    theEaten.setPoint(ghostSpawnPoint);
    ghostPenQ.add(theEaten);
    updateBoard(theEaten.getPoint(), GHOST);
    ghostReleasedAt = System.currentTimeMillis();
  }
  
  /** Update board location with that Pacman type */
  public void updateBoard(final Point thePoint, final int theItem) {
    board[(int)thePoint.getY()][(int)thePoint.getX()] = theItem;
  }
  
  /** @return true if chase mode */
  public boolean isChaseMode() {
    
    //If it's chaseMode right now
    if(isChaseMode) {
      //if it's still chase mode
      isChaseMode = (((System.currentTimeMillis() - ghostModeStart)/1000) <= CHASE);
      
      //If ChaseMode is over now, start other mode
      if(!isChaseMode)
        ghostModeStart = System.currentTimeMillis();
      
      return isChaseMode;
    }
    
    //If it's not chaseMode right now
    else if(!isChaseMode) { 
      //If it's still not chase mode
      isChaseMode = (((System.currentTimeMillis() - ghostModeStart)/1000) >= SCATTER);
      
      if(isChaseMode)
        ghostModeStart = System.currentTimeMillis();
      
      return isChaseMode;
    }
    return isChaseMode; 
  }
  
  /** Returns true if Pacman/Ghosts are frightened */
  private boolean ghostMode() {
    return ((System.currentTimeMillis() - hitEnergizerAt)/ 1000) < FRIGHTENED;
  }
  
  /** Draws the ghost in the parameter */
  private static void drawGhost(TheGhost theGhost) {
    theG.setColor(theGhost.getColor());
    theG.fillRect(theGhost.getX() * SCALE, theGhost.getY() * SCALE , GHOST_SIZE, GHOST_SIZE);
  }
  
  /** Draws a black square at X and Y */
  private static void drawBlackSquare(int x, int y) {
    theG.setColor(Color.BLACK);
    theG.fillRect(y * SCALE, x * SCALE, SCALE, SCALE);
  }
  
  /** Returns the board as a 2D array */
  public static int[][] getBoard() {      
    return  cs015.fnl.PacmanSupport.SupportMap.getMap();
  }
  
  /** Prints the board as a 2D array */
  public static void printBoard() {
    for(int y = 0; y < board.length; y++) {
      for(int i = 0; i < board[y].length; i++)
        System.out.print(board[y][i] + " ");
      System.out.println();
    }
  }
  
  /** Updates the score, num lives, ghost pen release countdown, and ghost mode labels */
  private void updateLabels() {
    pacmanScoreLabel.setText("Score: " + pacmanScore);
    pacmanLivesLabel.setText(SPACE + "Lives: " + pacmanLives + "     ");
    
    if(ghostPenQ.size() != 0) {
      int timeToRelease = (int)GHOST_RELEASE - (int)((System.currentTimeMillis() - ghostReleasedAt)/1000);
      nextGhostReleaseLabel.setText(SPACE + "Ghost Release: " + timeToRelease);
    }
    else if(ghostPenQ.size() <= 0)
      nextGhostReleaseLabel.setText(SPACE + "Ghost Release: N/A");
    
    int timeLeft = (int) ((System.currentTimeMillis() - ghostModeStart)/1000);
    if(ghostMode())
      ghostModeLabel.setText(SPACE + "Frightened Mode: " + (FRIGHTENED - timeLeft));
    else if(isChaseMode())
      ghostModeLabel.setText(SPACE + "Chase Mode: " + (CHASE - timeLeft));
    else
      ghostModeLabel.setText(SPACE + "Scatter Mode: " + (SCATTER - timeLeft));
  }
}