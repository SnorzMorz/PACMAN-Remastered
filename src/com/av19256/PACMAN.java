package com.av19256;

//PACMAN

//imports

// TODO

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Scanner;
import java.util.stream.IntStream;

public class PACMAN extends JPanel implements ActionListener {

    private final Font arial = new Font("Arial", Font.BOLD, 16); //Font for text
    private final int BLOCK_SIZE = 24; //How big one block is
    private final int N_BLOCKS = 15; //Number of blocks W and H
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE; //Number of blocks and block size
    private final int[] validSpeeds = {1, 2, 3, 4, 5}; // Available speeds
    Pacman pacman;
    Ghost ghost;
    Ghost ghost2;
    Ghost ghost3;
    Ghost ghost4;
    Map map;
    EditorMap editorMap;

    Timer timer; // Timer for animation
    private boolean inGame = false; //If in game
    private boolean dying = false; //If PACMAN is alive
    private boolean paused = false; // If paused
    private boolean inEditor = false; // If in Editor
    private boolean showControls = false; // To show controls in editor
    private int berries; // Keeping track of amout of berries
    private Color color = Color.blue;
    private int xMovement, yMovement; // For keyboard control
    private int currentSpeed = 3;
    private final int[] screen; // For drawing game

    public PACMAN() { // Constructor

        pacman = new Pacman();
        ghost = new Ghost(0);
        ghost2 = new Ghost(1);
        ghost3 = new Ghost(2);
        ghost4 = new Ghost(2);
        map = new Map();
        editorMap = new EditorMap();

        map.importMap();
        timer = new Timer(40, this); // Timer for animation
        timer.start();

        screen = new int[N_BLOCKS * N_BLOCKS]; // For screen coordinates and data

        addKeyListener(new TAdapter());
        setFocusable(true);
        startGame();
        playSound("res\\pacman_beginning.wav");
    }


    private void mainMenu(Graphics2D graphics) { // For generating text in the main menu

        int[] scores = getScores();


        graphics.setColor(Color.black); // Background
        graphics.fillRect(SCREEN_SIZE / 4, 80, SCREEN_SIZE / 2 + 10, 200);


        String start = "Press SPACE to start";
        String edit = "Press E for Map Editor";
        String mapPick = "<- and -> to pick map";
        graphics.setFont(arial);
        graphics.setColor(Color.green);
        graphics.drawString(start, (SCREEN_SIZE) / 4 + 10, 100);
        graphics.drawString(edit, (SCREEN_SIZE) / 4 + 8, 120);
        graphics.drawString(mapPick, (SCREEN_SIZE) / 4 + 8, 140);


        String topScore = "Top Scores:";
        graphics.drawString(topScore, (SCREEN_SIZE) / 3 + 15, 160);

        for (int i = 0; i < 5; i++) {
            topScore = (i + 1) + ". " + scores[i];
            graphics.drawString(topScore, (SCREEN_SIZE) / 3 + 15, 180 + (i * 20));
        }

    }

    private int[] getScores() { // Getting scores from text file

        Scanner scanner = null;

        try {
            scanner = new Scanner(new File("scores.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int[] scores = new int[1000]; // Who will have more than 1000 scores?
        int i = 0;
        while (scanner.hasNextInt()) {
            scores[i++] = scanner.nextInt();
        }

        scores = IntStream.of(scores).boxed().sorted(Comparator.reverseOrder()).mapToInt(j -> j).toArray(); // A way to reverse order


        return scores;

    }


    private void playSound(String filepath) { // For playing sounds, open the sound file as a Java input stream

        InputStream in;
        try {
            in = new FileInputStream(filepath);

            AudioStream audioStream = new AudioStream(in);

            AudioPlayer.player.start(audioStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void startGame() { // When starting the game

        ghost.initGhost();
        ghost2.initGhost();
        ghost3.initGhost();
        ghost4.initGhost();

        map.score = 0;
        berries = 0;
        pacman.setLives(3);
        map.level = 1;
        color = Color.blue;
        map.initMap();
        currentSpeed = 3;
    }


    private void continueLevel() { // When starting any level


        ghost.initGhost();
        ghost2.initGhost();
        ghost3.initGhost();
        ghost4.initGhost();


        pacman.restartPacman();

        xMovement = 0; // For cursor control
        yMovement = 0;
        dying = false;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g); // must do


        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black); // Background
        g2d.fillRect(0, 0, 500, 500);


        if (inEditor) {
            editorMap.drawMap(g2d);
            editorMap.mapEditor(g2d);
            if (showControls) {
                editorMap.showControls(g2d);
            }
        } else if (inGame) {

            if (!paused) {
                if (dying) {
                    pacman.death();
                } else {
                    if (pacman.bigDot) {
                        pacman.checkBigDot();
                    }
                    map.drawMap(g2d);
                    pacman.movePacman();
                    pacman.drawPacman(g2d);
                    ghost.moveGhost(g2d);
                    ghost2.moveGhost(g2d);
                    ghost3.moveGhost(g2d);
                    ghost4.moveGhost(g2d);
                    map.checkMap();
                    map.drawScore(g2d);
                    map.drawBerries(g2d);

                }


            } else {
                map.drawMap(g2d);
                pacman.drawPacman(g2d);
                map.drawScore(g2d);
                map.checkMap();

            }


        } else {
            map.drawMap(g2d);
            mainMenu(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    class TAdapter extends KeyAdapter { // Keyboard control

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) { // if in game
                switch (key) {
                    case KeyEvent.VK_P:
                        paused = !paused;
                        break;
                    case KeyEvent.VK_UP:
                        xMovement = 0;
                        yMovement = -1;
                        break;
                    case KeyEvent.VK_DOWN:
                        xMovement = 0;
                        yMovement = 1;
                        break;
                    case KeyEvent.VK_LEFT:
                        xMovement = -1;
                        yMovement = 0;
                        break;
                    case KeyEvent.VK_RIGHT:
                        xMovement = 1;
                        yMovement = 0;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        inGame = false;
                        break;
                    default:
                        break;


                }
            } else if (inEditor) { //If in editor
                switch (key) {
                    case KeyEvent.VK_UP:
                        xMovement = 0;
                        yMovement = -1;
                        break;
                    case KeyEvent.VK_DOWN:
                        xMovement = 0;
                        yMovement = 1;
                        break;
                    case KeyEvent.VK_LEFT:
                        xMovement = -1;
                        yMovement = 0;
                        break;
                    case KeyEvent.VK_RIGHT:
                        xMovement = 1;
                        yMovement = 0;
                        break;
                    case KeyEvent.VK_SPACE:
                        editorMap.addBlock();
                        break;
                    case KeyEvent.VK_B:
                        editorMap.addBerry();
                        break;
                    case KeyEvent.VK_E:
                        inEditor = false;
                    case KeyEvent.VK_C:
                        showControls = !showControls;
                        break;
                    case KeyEvent.VK_S:
                        editorMap.saveMap();
                        inEditor = false;
                    case KeyEvent.VK_D:
                        editorMap.addBigDot();
                    default:
                        break;
                }
            } else { // In main menu

                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    startGame();
                }
                if (key == KeyEvent.VK_E) {
                    inEditor = true;
                }
                if (key == KeyEvent.VK_RIGHT) {
                    map.pickedMap++;
                    map.importMap();
                    map.initMap();
                }
                if (key == KeyEvent.VK_LEFT) {
                    if (map.pickedMap > 0) {
                        map.pickedMap--;
                    }
                    map.importMap();
                    map.initMap();

                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) { // To check if in standstill

            int key = e.getKeyCode();

            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_LEFT
                    || key == KeyEvent.VK_UP || key == KeyEvent.VK_RIGHT) {
                xMovement = 0;
                yMovement = 0;
            }
        }
    }

    public class Pacman { // Pacman class

        private final Image up, down, left, right, stand;
        private int pacmanX, pacmanY, pacmand_x, pacmand_y;
        private int lives;
        private Calendar bigDotEnd;
        private boolean bigDot;

        Pacman() { // Constructor
            lives = 3;
            bigDot = false;

            down = new ImageIcon("res\\down.gif").getImage();
            up = new ImageIcon("res\\up.gif").getImage();
            left = new ImageIcon("res\\left.gif").getImage();
            right = new ImageIcon("res\\right.gif").getImage();
            stand = new ImageIcon("res\\pacman.png").getImage();
        }

        public int getLives() {
            return lives;
        }

        public void setLives(int n) {
            lives = n;
        }

        public int getPacmanX() {
            return pacmanX;
        }

        public int getPacmanY() {
            return pacmanY;
        }


        public void restartPacman() {
            pacmanX = 8 * BLOCK_SIZE; // Start location and variables for Pacman
            pacmanY = 10 * BLOCK_SIZE;
            pacmand_x = 0; // These also are the zero coordinates for Pacman
            pacmand_y = 0;
        }

        private void drawPacman(Graphics2D g2d) { // Drawing correct pacman image

            if (pacmand_x == -1) { // Depending on movement draw correct gif
                g2d.drawImage(left, pacmanX, pacmanY, null);
            } else if (pacmand_x == 1) {
                g2d.drawImage(right, pacmanX, pacmanY, null);
            } else if (pacmand_y == -1) {
                g2d.drawImage(up, pacmanX, pacmanY, null);
            } else if (pacmand_y == 1) {
                g2d.drawImage(down, pacmanX, pacmanY, null);
            } else if (pacmand_x == 0 && pacmand_y == 0) {
                g2d.drawImage(stand, pacmanX, pacmanY, null);
            }
        }

        public void movePacman() { // for moving pacaman on the screen

            int pos; // position
            int block; // block

            if (pacmanX % BLOCK_SIZE == 0 && pacmanY % BLOCK_SIZE == 0) { // determine position of Pacman on the map
                pos = pacmanX / BLOCK_SIZE + N_BLOCKS * (pacmanY / BLOCK_SIZE);
                block = screen[pos];

                if ((block & 16) != 0) { // Check if on dot
                    screen[pos] = block & 15; // if on dot put the bitwise AND result without the dot
                    playSound("res\\pacman_eat.wav");
                    map.score++;
                } else if ((block & 32) != 0) { // CHeck if on berry
                    screen[pos] = block & 31;
                    map.score += 10;
                    berries++;
                    playSound("res\\pacman_eatfruit.wav");
                } else if ((block & 64) != 0) {
                    screen[pos] = block & 63;
                    bigDot = true;
                    bigDotEnd = Calendar.getInstance(); // Gets a calendar using the default time zone and locale
                    bigDotEnd.add(Calendar.SECOND, 6);
                    playSound("res\\pacman_eat.wav");
                }

                if (xMovement != 0 || yMovement != 0) {
                    if (!((xMovement == -1 && yMovement == 0 && (block & 1) != 0) // When not at a border
                            || (xMovement == 1 && yMovement == 0 && (block & 4) != 0)
                            || (xMovement == 0 && yMovement == -1 && (block & 2) != 0)
                            || (xMovement == 0 && yMovement == 1 && (block & 8) != 0))) {
                        pacmand_x = xMovement;
                        pacmand_y = yMovement;
                    }
                }

                if ((pacmand_x == -1 && pacmand_y == 0 && (block & 1) != 0) // when at a border
                        || (pacmand_x == 1 && pacmand_y == 0 && (block & 4) != 0)
                        || (pacmand_x == 0 && pacmand_y == -1 && (block & 2) != 0)
                        || (pacmand_x == 0 && pacmand_y == 1 && (block & 8) != 0)) {
                    pacmand_x = 0;
                    pacmand_y = 0;
                }


            }
            //Speed of PACMAN
            int PACMANSPEED = 6;
            pacmanX = pacmanX + PACMANSPEED * pacmand_x;
            pacmanY = pacmanY + PACMANSPEED * pacmand_y;
        }

        private void checkBigDot() { // Check if its end of big dot
            if (Calendar.getInstance().after(bigDotEnd)) {
                bigDot = false;
            }
        }

        private void death() { // Oops died

            lives--;

            if (lives == 0) {
                inGame = false;
                map.saveScore();
                playSound("res\\pacman_death.wav");

            }

            continueLevel();
        }
    }

    public class Ghost { // Ghost class
        public Image ghost, ghostBlue;
        Calendar ghostRevive;
        private int ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed; //Also for ghost position
        private int[] dx, dy; // Ghost movement


        Ghost(int picture) {

            ghostBlue = new ImageIcon("res\\ghost4.gif").getImage();
            initGhost();

            if (picture == 0) {
                ghost = new ImageIcon("res\\ghost.gif").getImage();
            } else if (picture == 1) {
                ghost = new ImageIcon("res\\ghost2.gif").getImage();
            } else if (picture == 2) {
                ghost = new ImageIcon("res\\ghost3.gif").getImage();
            } else if (picture == 3) {
                ghost = new ImageIcon("res\\ghost4.gif").getImage();
            }


        }

        private void drawGhost(Graphics2D g2d, int x, int y) { // For loading the correct image of ghost

            if (pacman.bigDot) {
                g2d.drawImage(ghostBlue, x, y, null);
            } else {
                g2d.drawImage(ghost, x, y, null);
            }
        }

        private void initGhost() {
            int random;


            ghost_y = 4 * BLOCK_SIZE; // start location for ghosts
            ghost_x = 4 * BLOCK_SIZE;
            ghost_dy = 0;
            ghost_dx = 0;
            dx = new int[4];
            dy = new int[4];
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) { // Random speeds for ghosts
                random = currentSpeed;
            }

            ghostSpeed = validSpeeds[random]; // Speed must be form valid speeds
        }

        private void moveGhost(Graphics2D g2d) { // For moving ghosts randomly

            int pos;
            int count;


            if (ghost_x % BLOCK_SIZE == 0 && ghost_y % BLOCK_SIZE == 0) {
                pos = ghost_x / BLOCK_SIZE + N_BLOCKS * (ghost_y / BLOCK_SIZE);
                if (pos < 0 || pos > N_BLOCKS * N_BLOCKS) {
                    initGhost();
                    pos = ghost_x / BLOCK_SIZE + N_BLOCKS * (ghost_y / BLOCK_SIZE);
                }

                count = 0;


                if ((screen[pos] & 1) == 0 && ghost_dx != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screen[pos] & 2) == 0 && ghost_dy != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screen[pos] & 4) == 0 && ghost_dx != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screen[pos] & 8) == 0 && ghost_dy != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screen[pos] & 15) == 15) {
                        ghost_dx = 0;
                        ghost_dy = 0;
                    } else {
                        ghost_dx = -ghost_dx;
                        ghost_dy = -ghost_dy;
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_dx = dx[count];
                    ghost_dy = dy[count];
                }

            }


            if (ghostRevive == null || Calendar.getInstance().after(ghostRevive)) {

                ghost_x = ghost_x + (ghost_dx * ghostSpeed);
                ghost_y = ghost_y + (ghost_dy * ghostSpeed);
                drawGhost(g2d, ghost_x + 1, ghost_y + 1);

                if (pacman.getPacmanX() > (ghost_x - 12) && pacman.getPacmanX() < (ghost_x + 12)
                        && pacman.getPacmanY() > (ghost_y - 12) && pacman.getPacmanY() < (ghost_y + 12)
                        && inGame) {
                    if (pacman.bigDot) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.SECOND, 2);
                        ghostRevive = cal;
                        initGhost();
                        playSound("res\\pacman_eatghost.wav");
                    } else {
                        dying = true;
                    }
                }
            }

        }


    }

    public class Map { // Map class
        int maxSpeed;
        int pickedMap;
        Image berry, heart;
        private int level; //Current level
        private int maxScore; // Maximum score in a map
        private int[] map;
        private int score; //Counters for score and lives

        Map() {
            maxSpeed = 5;
            level = 1;
            score = 0;
            map = new int[N_BLOCKS * N_BLOCKS];
            pickedMap = 0;
            importMap();
            maxScore = getMaxScore();

            berry = new ImageIcon("res\\berry.png").getImage();
            heart = new ImageIcon("res\\heart.png").getImage();
        }


        private void checkMap() { // To check if the game is over

            boolean finished = true;

            if (score < maxScore * level + (30 * (level - 1))) {
                finished = false;
            }

            if (finished) {

                score += 30;
                level++;

                if (level % 2 == 0) {
                    color = Color.red;
                }
                if (level % 3 == 0) {
                    color = Color.green;
                }


                if (currentSpeed < maxSpeed) {
                    currentSpeed++;
                }

                initMap();
            }
        }

        private void saveScore() { // For writing the final score to text file

            try {

                FileWriter myWriter = new FileWriter("scores.txt", true);
                myWriter.write(score + "\n");
                myWriter.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

        private void initMap() { // For copying the map from one array to another (Useful when starting new level)

            int i;
            for (i = 0; i < map.length; i++) {
                screen[i] = map[i];
            }
            maxScore = getMaxScore();

            continueLevel();
        }

        private void drawScore(Graphics2D g) { // For drawing score at the bottom of the screen during play

            g.setFont(arial);
            g.setColor(Color.green);
            String s = "Score: " + score;
            g.drawString(s, SCREEN_SIZE / 2 + 95, SCREEN_SIZE + 15);

            for (int i = 0; i < pacman.getLives(); i++) {
                g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, null);
            }

        }

        public void drawMap(Graphics2D g2d) { // Drawing map to screen

            short i = 0;
            int x, y;

            for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
                for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(3));

                    if ((map[i] == 0)) {
                        g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                    }

                    if ((screen[i] & 1) != 0) {
                        g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                    }

                    if ((screen[i] & 2) != 0) {
                        g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                    }

                    if ((screen[i] & 4) != 0) {
                        g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                                y + BLOCK_SIZE - 1);
                    }

                    if ((screen[i] & 8) != 0) {
                        g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                                y + BLOCK_SIZE - 1);
                    }

                    if ((screen[i] & 16) != 0) {
                        g2d.setColor(Color.white);
                        g2d.fillOval(x + 10, y + 10, 6, 6);
                    }
                    if ((screen[i] & 32) != 0) {
                        g2d.drawImage(berry, x + 1, y, null);
                    }
                    if ((screen[i] & 64) != 0) {
                        g2d.setColor(Color.white);
                        g2d.fillOval(x + 7, y + 7, 11, 11);
                    }

                    i++;
                }
            }
        }

        private void drawBerries(Graphics2D g2d) { // Draw berries on right side

            for (int i = 0; i < berries; i++) {
                g2d.drawImage(berry, SCREEN_SIZE + 5, 20 + i * 10, null);
            }

        }

        private void importMap() { // Importing map from txt file
            try (BufferedReader br = new BufferedReader(new FileReader("maps.txt"))) {
                String line;
                int current = 0;
                String[] importMapString = new String[N_BLOCKS * N_BLOCKS];
                int[] importMap = new int[N_BLOCKS * N_BLOCKS];

                while ((line = br.readLine()) != null) {
                    if (current == pickedMap) {
                        importMapString = line.split(" ");
                        break;
                    }
                    current++;
                }
                if (current == pickedMap) {
                    for (int i = 0; i < importMapString.length; i++) {
                        importMap[i] = Integer.parseInt(importMapString[i]);
                    }
                    map = importMap;
                    initMap();
                } else {
                    pickedMap--;
                }


            } catch (Exception e) {

            }

        }

        private int getMaxScore() { // For calculating Max Score in a map
            int maxScore = 0;
            for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
                if ((map[i] & 16) != 0) {
                    maxScore++;
                } else if ((map[i] & 32) != 0) {
                    maxScore += 10;
                }

            }
            return maxScore;
        }

    }

    public class EditorMap extends Map { // Class for map in editor mode
        int[] map;
        private int markedBlock, editX, editY; // For knowing which block is marked in the editor and x and y coordinates;

        EditorMap() {

            markedBlock = 0;
            editX = 0;
            editY = 0;

            // Each position in the game,
            // 0 = block, 1 = left border, 2 = top border, 4 = right border, 8 = bottom border, 16 = white dot, 32 = berry, 64 = big dot . ADD NUMBERS TOGETHER
            // We use these numbers because we will be using the bitwise operator &
            // For example if num = 22, 22 & 16 in binary is 10100 and 10000,
            // the bitwise and is 16 so there is a dot, right border is 100, so the result is 100, so there is also right border and so on...
            // Very useful for map editor
            // https://blog.kylekukshtel.com/p2p-networking-2
            map = new int[]{ // Used in the editor as a map

                    19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 64, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
                    25, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
            };
        }

        private void addDot() { // To change from block/berry to dot in Editor
            if (map[markedBlock] >= 64) { // If its currently big dot
                map[markedBlock] = 16 + map[markedBlock] - 64;
            } else if (map[markedBlock] >= 32) { // If its currently berry
                map[markedBlock] = 16 + map[markedBlock] - 32;
            } else if (map[markedBlock] == 0) { // If its a block
                map[markedBlock] = 16;

                if (markedBlock > N_BLOCKS - 1) { // if not on top edge
                    if (map[markedBlock - N_BLOCKS] == 0) {
                        map[markedBlock] += 2;
                    } else {
                        map[markedBlock - 15] -= 8;
                    }
                } else { // on top edge
                    map[markedBlock] += 2;
                }
                if (markedBlock < N_BLOCKS * N_BLOCKS - N_BLOCKS) { // if not on bottom edge
                    if (map[markedBlock + N_BLOCKS] == 0) {
                        map[markedBlock] += 8;
                    } else {
                        map[markedBlock + N_BLOCKS] -= 2;
                    }
                } else { // bottom edge
                    map[markedBlock] += 8;
                }
                if ((markedBlock) % N_BLOCKS != 0) { // if not on left edge
                    if (map[markedBlock - 1] == 0) {
                        map[markedBlock] += 1;
                    } else {
                        map[markedBlock - 1] -= 4;
                    }
                } else { // left edge
                    map[markedBlock] += 1;
                }
                if ((markedBlock + 1) % N_BLOCKS != 0) { // if not on right edge
                    if (map[markedBlock + 1] == 0) {
                        map[markedBlock] += 4;
                    } else {
                        map[markedBlock + 1] -= 1;
                    }
                } else { // right edge
                    map[markedBlock] += 4;
                }
            }
        }


        private void saveMap() { // Saving map to txt file

            try {
                FileWriter myWriter = new FileWriter("maps.txt", true);
                for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
                    myWriter.write(map[i] + " ");
                }
                myWriter.write("\n");
                myWriter.close();
            } catch (IOException e) {

                e.printStackTrace();
            }

        }

        @Override
        public void drawMap(Graphics2D g2d) {
            short i = 0;
            int x, y;

            for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
                for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(3));

                    if ((map[i] == 0)) {
                        g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                    }

                    if ((map[i] & 1) != 0) {
                        g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                    }

                    if ((map[i] & 2) != 0) {
                        g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                    }

                    if ((map[i] & 4) != 0) {
                        g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                                y + BLOCK_SIZE - 1);
                    }

                    if ((map[i] & 8) != 0) {
                        g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                                y + BLOCK_SIZE - 1);
                    }

                    if ((map[i] & 16) != 0) {
                        g2d.setColor(Color.white);
                        g2d.fillOval(x + 10, y + 10, 6, 6);
                    }
                    if ((map[i] & 32) != 0) {
                        g2d.drawImage(berry, x + 1, y, null);
                    }
                    if ((map[i] & 64) != 0) {
                        g2d.setColor(Color.white);
                        g2d.fillOval(x + 7, y + 7, 11, 11);
                    }

                    i++;
                }
            }
        }

        private void showControls(Graphics2D g2d) { // Showing controls in map editor
            g2d.setColor(Color.black); // Background
            g2d.fillRect(SCREEN_SIZE / 7, 80, SCREEN_SIZE / 2 + 100, 160);
            String s1 = "Press SPACE to change between ";
            String s12 = "BLOCK and DOT";
            String s2 = "Press B to add BERRY";
            String s3 = "PRESS E to EXIT without saving";
            String s4 = "PRESS S to EXIT with saving";
            String s5 = "Press D to add BIG DOT";
            g2d.setColor(Color.green);
            g2d.setFont(arial);
            g2d.drawString(s1, (SCREEN_SIZE) / 6, 100);
            g2d.drawString(s12, (SCREEN_SIZE) / 6, 118);
            g2d.drawString(s2, (SCREEN_SIZE) / 6, 140);
            g2d.drawString(s3, (SCREEN_SIZE) / 6, 162);
            g2d.drawString(s4, (SCREEN_SIZE) / 6, 184);
            g2d.drawString(s5, (SCREEN_SIZE) / 6, 206);
        }

        private void addBigDot() { // Adding big dot in map editor
            if (map[markedBlock] >= 64) {
                addDot();
            } else {
                addDot();
                map[markedBlock] = 64 + map[markedBlock] - 16;
            }

        }

        private void addBerry() { // To change from dot/block to berry in editor
            if (map[markedBlock] >= 32) {
                addDot();
            } else {
                addDot();
                map[markedBlock] = 32 + map[markedBlock] - 16;
            }
        }

        private void changeEditorCoordinates() { // When moving the red square in map editor
            if (xMovement == 1) {
                if (editX < BLOCK_SIZE * (N_BLOCKS - 1)) {
                    editX += BLOCK_SIZE;
                    markedBlock++;
                    xMovement = 0;
                }
            } else if (xMovement == -1) {
                if (editX > 0) {
                    editX -= BLOCK_SIZE;
                    markedBlock--;
                    xMovement = 0;
                }

            } else if (yMovement == 1) {
                if (editY < BLOCK_SIZE * (N_BLOCKS - 1)) {
                    editY += BLOCK_SIZE;
                    markedBlock += 15;
                    yMovement = 0;
                }
            } else if (yMovement == -1) {
                if (editY > 0) {
                    editY -= BLOCK_SIZE;
                    markedBlock -= 15;
                    yMovement = 0;
                }
            }
        }

        private void mapEditor(Graphics2D g2d) { // When opening the map editor
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(Color.red);
            g2d.drawRect(editX + 1, editY + 1, BLOCK_SIZE, BLOCK_SIZE);

            g2d.setColor(Color.green);
            g2d.setFont(arial);
            g2d.drawString("Press C for CONTROLS", 150, SCREEN_SIZE + 20);

            changeEditorCoordinates();

        }

        private void addBlock() { // To change from dot/berry to Block in editor
            if (map[markedBlock] != 0) {
                map[markedBlock] = 0;

                if (markedBlock > N_BLOCKS - 1 && map[markedBlock - N_BLOCKS] != 0) {
                    map[markedBlock - 15] += 8;
                }
                if (markedBlock < N_BLOCKS * N_BLOCKS - N_BLOCKS && map[markedBlock + N_BLOCKS] != 0) {
                    map[markedBlock + N_BLOCKS] += 2;
                }
                if ((markedBlock) % N_BLOCKS != 0 && map[markedBlock - 1] != 0) {
                    map[markedBlock - 1] += 4;

                }
                if ((markedBlock + 1) % N_BLOCKS != 0 && map[markedBlock + 1] != 0) {
                    map[markedBlock + 1] += 1;
                }
            } else {
                addDot();
            }

        }
    }


}
