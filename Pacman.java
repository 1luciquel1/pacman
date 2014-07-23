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
  
  private static final int SCALE = 20;
  private static final int PACMAN_SIZE = 15;
  private static final int GHOST_SIZE = 20;
  private static final int DOT_SIZE = 5;
  private static final int ENERGIZER_SIZE = DOT_SIZE * 2;
  
  private static final int CALCULATION_NORMAL = Integer.MAX_VALUE/10;
  private static final int CALCULATION_ENERGIZER = Integer.MAX_VALUE/30; //80;
  private static final int FRIGHTENED_MODE = 800; //5 seconds
  
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
  
  private static long hitEnergizerAt;
  
  private static final int GHOST_RELEASE = 5; //Release ghost every 5 seconds
  private static long ghostReleasedAt;
  private static Point ghostReleasePoint;
  private static Point ghostSpawnPoint;
  
  private static int pacmanScore = 0;
  private static int pacmanLives = 3;
  
  private static JLabel pacmanScoreLabel;
  private static JLabel pacmanLivesLabel;
  private static JLabel isFrightenedLabel;
  private static JLabel nextGhostReleaseLabel;
  
  private boolean controlTouch = false;
  
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
    
    pacmanLivesLabel = new JLabel("     Lives: " + pacmanLives, JLabel.LEFT);
    pacmanLivesLabel.setForeground(Color.WHITE);
    add(pacmanLivesLabel);
    
    isFrightenedLabel = new JLabel("     Normal", JLabel.LEFT);
    isFrightenedLabel.setForeground(Color.WHITE);
    add(isFrightenedLabel);
    
    nextGhostReleaseLabel = new JLabel("     Ghost Release", JLabel.LEFT);
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
    
    for(int i = 0; i < theGhosts.length; i++)
      System.out.println(theGhosts[i]);
    
    ghostPenQ.add(blueGhost);
    ghostPenQ.add(orangeGhost);
    ghostPenQ.add(redGhost);
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
  
  /** Moves the item parameter based on the direction parameter */
  public synchronized void moveItem(final PacmanItem theItem, 
                                    final PacmanItem.Direction theDirection) {
    controlTouch = false;
    
    if(theDirection == null)
      return;
    
    theItem.setFacingDirection(theDirection);
    
    final int itemInNextDirection = getItemInNextMove(pacman, theDirection);
    
    if(itemInNextDirection == OUT)
      return; 
    
    if(itemInNextDirection == GHOST) {
      if(isFrightened())
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
    
    System.out.println("Current Pacman location: " + pacman.getY() + "\t" + pacman.getX());
    System.out.println("Pink Ghost: " + pinkGhost.getY() + "\t" + pinkGhost.getX());
    
    board[pinkGhost.getY()][pinkGhost.getX()] = PACMAN;
    
    moveItem(pacman, theDirection);
    
    pinkGhost.setPoint(ghostSpawnPoint);
    ghostReleasedAt = System.currentTimeMillis();
    ghostPenQ.add(pinkGhost);
    board[pinkGhost.getY()][pinkGhost.getX()] = GHOST;
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
    board[pacman.getY()][pacman.getX()] = FREE;
    pacman.returnToStartPosition();
    pacmanLives--;
    updateLabels();
    updatePacmanBoard();
    System.out.println("GHOST");
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
  
  /** Returns true if Pacman/Ghosts are frightened */
  private boolean isFrightened() {
    return ((System.currentTimeMillis() - hitEnergizerAt)/ 1000) < FRIGHTENED_MODE;
  }
  
  /** Main method, creates frame and adds game to it */
  public static void main(String[] ryan) {
    JFrame theFrame = new JFrame("Pacman");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    theFrame.setSize(500, 500);
    
    theFrame.add(new Pacman());
    theFrame.setVisible(true);
  }
  
  /** Sets pacman's location in the board to PACMAN */
  private void updatePacmanBoard() {
    board[pacman.getY()][pacman.getX()] = PACMAN;
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
  
  /** Updates the score and num lives labels */
  private synchronized void updateLabels() {
    pacmanScoreLabel.setText("Score: " + pacmanScore);
    pacmanLivesLabel.setText("     Lives: " + pacmanLives + "     ");
    
    if(ghostPenQ.size() != 0) {
      int timeToRelease = (int)GHOST_RELEASE - (int)((System.currentTimeMillis() - ghostReleasedAt)/1000);
      nextGhostReleaseLabel.setText("     Ghost Release: " + timeToRelease);
    }
    else
      nextGhostReleaseLabel.setText("     Ghost Release: N/A");
    
    if(isFrightened())
      isFrightenedLabel.setText("     Frightened Mode");
    else
      isFrightenedLabel.setText("     Normal Mode");
  }
}