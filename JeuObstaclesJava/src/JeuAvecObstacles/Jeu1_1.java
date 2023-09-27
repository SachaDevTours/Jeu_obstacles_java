/**
 * 
 */
package JeuAvecObstacles;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;



public class Jeu1_1 extends JPanel implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int playerX = 50;
    private int playerY = 300;
    private int playerVelocityY = 0;
    private boolean isJumping = false;
    private boolean canDoubleJump = true;
    private long lastDoubleJumpTime = 0;
    private static final long DOUBLE_JUMP_COOLDOWN = 3000; // Cooldown de 3 secondes
    private boolean isGameOver = false;
    private JButton replayButton;
    private int score = 0;
    private Timer scoreTimer;
    private Timer rotationTimer;
    private Timer timer;
    private List<Obstacle> obstacles = new ArrayList<>();
    private List<Triangle> triangles = new ArrayList<>();
    private Random random = new Random();
    private double rotationAngle = 0.0;
    private boolean collisionDisabled = false;
    private long collisionDisableStartTime = 0;
    private static final long COLLISION_DISABLE_DURATION = 10000;
    private boolean triangleCooldown = false;
    private long triangleCooldownStartTime = 0;
    private long lastCollisionWithTriangleTime = 0;
    private int respawnTriangleDuration = 30000;
    

    // Ajout de la couleur du joueur en fonction des triangles
    private Color playerColor = Color.BLUE; // Couleur par défaut
    private Color triangleColor = Color.YELLOW; // Couleur des triangles.

    // Ajoutez une variable pour suivre le niveau actuel
    private int currentLevel = 1;

    public Jeu1_1() {
        timer = new Timer(10, this);
        timer.start();

        scoreTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    score++;
                }
            }
        });
        scoreTimer.start();

        rotationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotationAngle += Math.toRadians(10);
                if (rotationAngle >= Math.toRadians(360)) {
                    rotationAngle = 0.0;
                }
            }
        });
        rotationTimer.start();

        setPreferredSize(new Dimension(800, 400));
        setFocusable(true);
        requestFocus();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isGameOver && (!isJumping || canDoubleJump)) {
                    jump();
                }
            }
        });

        lastDoubleJumpTime = System.currentTimeMillis() - DOUBLE_JUMP_COOLDOWN;

        generateObstacles();
        generateTriangles();

        // Initialisez canDoubleJump à true au niveau 2
        canDoubleJump = (currentLevel == 2);
    }

    private void generateObstacles() {
        obstacles.add(new Obstacle(800, 325, getRandomWidth(), 25));
        obstacles.add(new Obstacle(1000, 200, getRandomWidth(), 25));
        obstacles.add(new Obstacle(1200, 325, getRandomWidth(), 25));
        obstacles.add(new Obstacle(1400, 200, getRandomWidth(), 25));
        obstacles.add(new Obstacle(1600, 325, getRandomWidth(), 25));
    }

    private void generateTriangles() {
        // Supprimer les anciens triangles
        triangles.clear();

        // Ajouter un nouveau triangle jaune
        triangles.add(new Triangle(900, 325, 30, -2, Color.YELLOW));
    }

    private int getRandomWidth() {
        int maxWidth = 50;
        return random.nextInt(maxWidth - 25 + 1) + 25;
    }
    

    private void jump() {
        long currentTime = System.currentTimeMillis();

        if (!isJumping) {
            // Jouer le son lorsque le joueur saute
            playJumpSound();

            // Premier saut
            isJumping = true;
            playerVelocityY = -15;
        } else if (currentLevel == 2 || (canDoubleJump && currentTime - lastDoubleJumpTime >= DOUBLE_JUMP_COOLDOWN)) {
            // Double saut après le cooldown (ou sans cooldown au niveau 2)
            playerVelocityY = -15;
            lastDoubleJumpTime = currentTime;
            canDoubleJump = false;
        }
    }
    
    private void playJumpSound() {
        try {
            File soundFile = new File("jump.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private boolean isCooldownOver() {
//        long currentTime = System.currentTimeMillis();
//        return currentTime - lastDoubleJumpTime >= DOUBLE_JUMP_COOLDOWN;
//    }

    private void addReplayButton() {
        if (replayButton != null) {
            remove(replayButton);
        }

        replayButton = new JButton("Rejouer");
        replayButton.setBounds(350, 200, 100, 50);
        replayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                isGameOver = false;
                obstacles.clear();
                playerX = 50;
                playerY = 300;
                playerVelocityY = 0;
                isJumping = false;
                canDoubleJump = true;
                lastDoubleJumpTime = 0;
                score = 0;

                resetGame(); // Réinitialisez le jeu au niveau 1

                // Redémarrer les timers
                timer.start();
                scoreTimer.start();
                rotationTimer.start();

                remove(replayButton);
                repaint();
            }
        });
        add(replayButton);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Perdu", 350, 150);

            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Score: " + score, 50, 50);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(0, 350, 800, 50);

            g.setColor(Color.RED);
            for (Obstacle obstacle : obstacles) {
                g.fillRect(obstacle.getX(), obstacle.getY(), obstacle.getWidth(), obstacle.getHeight());
            }

            // Utilisez la couleur initial des triangles
            g.setColor(triangleColor);
            for (Triangle triangle : triangles) {
                int[] xPoints = triangle.getXPoints();
                int[] yPoints = triangle.getYPoints();
                g.fillPolygon(xPoints, yPoints, 3);
            }

            Ellipse2D.Double playerCircle = new Ellipse2D.Double(playerX, playerY, 50, 50);

            g2d.setColor(playerColor); // Utilisez la couleur du joueur
            g2d.rotate(rotationAngle, playerX + 25, playerY + 25);
            g2d.fill(playerCircle);

            g2d.rotate(-rotationAngle, playerX + 25, playerY + 25);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Score: " + score, 50, 30);

            long remainingCooldown = getRemainingDoubleJumpCooldown();
            if (remainingCooldown < 0) {
                String cooldownText = "Cooldown : 0s";
                g.drawString(cooldownText, 50, 60);
            } else {
                String cooldownText = "Cooldown: " + (remainingCooldown / 1000) + "s";
                g.drawString(cooldownText, 50, 60);
            }
        }

        // Afficher le niveau actuel en bas à gauche
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.getFontMetrics().stringWidth("Level: " + currentLevel);
        g.drawString("Level: " + currentLevel, 50, getHeight() - 310);
    }

    private long getRemainingDoubleJumpCooldown() {
        long currentTime = System.currentTimeMillis();
        return lastDoubleJumpTime + DOUBLE_JUMP_COOLDOWN - currentTime;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver) {
            if (playerY < 300) {
                playerVelocityY += 1;
            }

            playerY += playerVelocityY;

            if (playerY >= 300) {
                playerY = 300;
                isJumping = false;
                canDoubleJump = true; // Réinitialiser canDoubleJump lorsque le joueur touche le sol
            }

            for (Obstacle obstacle : obstacles) {
                if (!collisionDisabled && playerX + 50 > obstacle.getX() && playerX < obstacle.getX() + obstacle.getWidth()
                        && playerY + 50 > obstacle.getY() && playerY < obstacle.getY() + obstacle.getHeight()) {
                    gameOver();
                    break;
                }
            }

            Iterator<Triangle> triangleIterator = triangles.iterator();
            while (triangleIterator.hasNext()) {
                Triangle triangle = triangleIterator.next();
                if (!collisionDisabled && triangle.intersects(playerX, playerY, 50, 50)) {
                    collisionWithTriangle();
                    triangleIterator.remove(); // Supprimer le triangle en collision
                }
                triangle.move();
            }

            if (collisionDisabled && System.currentTimeMillis() - collisionDisableStartTime >= COLLISION_DISABLE_DURATION) {
                collisionDisabled = false;
                // Réinitialiser la couleur du joueur en bleu
                playerColor = Color.BLUE;
            }

            // Ajouter un nouveau triangle après X secondes
            if (triangleCooldown && System.currentTimeMillis() - lastCollisionWithTriangleTime >= respawnTriangleDuration) {
                generateTriangles();
                triangleCooldown = false;
            }

            // Gérez le passage au niveau 2 lorsque le score atteint 50
            if (currentLevel == 1 && score >= 50) {
                currentLevel = 2;
                obstacles.clear(); // Effacez les obstacles du niveau 1
                // Ajoutez les obstacles du niveau 2
                obstacles.add(new Obstacle(800, 200, getRandomWidth(), 25));
                obstacles.add(new Obstacle(900, 325, getRandomWidth(), 25));
                obstacles.add(new Obstacle(1000, 200, getRandomWidth(), 25));
                obstacles.add(new Obstacle(1100, 325, getRandomWidth(), 25));
                obstacles.add(new Obstacle(1200, 200, getRandomWidth(), 25));
                // Réinitialisez canDoubleJump à true au niveau 2
                canDoubleJump = true;
            }

            // Gérez le mouvement des obstacles en fonction du niveau
            if (currentLevel == 1) {
                for (Obstacle obstacle : obstacles) {
                    obstacle.move();
                    if (obstacle.getX() + obstacle.getWidth() < 0) {
                        obstacle.setX(800);
                    }
                }
            } else if (currentLevel == 2) {
                moveObstaclesLevel2();
            }

            

            // Ajouter un nouveau triangle si la liste est vide
            if (triangles.isEmpty() && !triangleCooldown) {
                generateTriangles();
            }

            repaint();
            
        }
    }

    // Ajoutez une méthode pour gérer le mouvement des obstacles du niveau 2
    private void moveObstaclesLevel2() {
        for (Obstacle obstacle : obstacles) {
            // Déplacez les obstacles de haut en bas
            obstacle.setY(obstacle.getY() + obstacle.getDirection());
            if (obstacle.getY() <= 0) {
                obstacle.setDirection(1); // Changez la direction vers le bas
            } else if (obstacle.getY() + obstacle.getHeight() >= 400) {
                obstacle.setDirection(-1); // Changez la direction vers le haut
            }
            obstacle.move();
            if (obstacle.getX() + obstacle.getWidth() < 0) {
                obstacle.setX(800);
            }
        }
    }

    private void playGameOverSound() {
        try {
            File soundFile = new File("game_over.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    
    private void gameOver() {
        isGameOver = true;
        timer.stop();
        scoreTimer.stop();
        rotationTimer.stop();
        playGameOverSound(); // Jouer le son du jeu terminé
        addReplayButton();
        repaint();
    }
    
    
    private void playStarSound() {
        try {
            File soundFile = new File("star.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    private void collisionWithTriangle() {
        if (!collisionDisabled) {
            collisionDisabled = true;
            collisionDisableStartTime = System.currentTimeMillis();

            // Enregistrez le temps de la collision
            lastCollisionWithTriangleTime = System.currentTimeMillis();

            // Activer la temporisation des triangles
            triangleCooldown = true;
            setTriangleCooldownStartTime(System.currentTimeMillis());

            // Changer la couleur du joueur en jaune
            playerColor = Color.YELLOW;

            // Jouer le son lorsque le joueur devient jaune
            playStarSound();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Jeu1_1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new Jeu1_1());
            frame.pack();
            frame.setVisible(true);
            frame.setIconImage(new ImageIcon("icon.png").getImage());
        });
    }

    private class Obstacle {
        private int x;
        private int y;
        private int width;
        private int height;
        private int direction = 1; // Direction par défaut (vers le bas)

        public Obstacle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getDirection() {
            return direction;
        }

        public void setDirection(int direction) {
            this.direction = direction;
        }

        public void move() {
            x -= 5;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    private class Triangle {
        private int x;
        private int y;
        private int size;
        private int dx;

        public Triangle(int x, int y, int size, int dx, Color yellow) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.dx = dx;
        }

        public int[] getXPoints() {
            return new int[]{x, x + size / 2, x + size};
        }

        public int[] getYPoints() {
            return new int[]{y + size, y, y + size};
        }

        public boolean intersects(int px, int py, int pw, int ph) {
            Polygon playerPolygon = new Polygon(
                    new int[]{px, px + pw / 2, px + pw, px},
                    new int[]{py, py + ph, py, py},
                    4
            );

            Polygon trianglePolygon = new Polygon(getXPoints(), getYPoints(), 3);

            return playerPolygon.intersects(trianglePolygon.getBounds2D());
        }

        public void move() {
            x += dx;
            if (x + size < 0) {
                x = 800;
            }
        }
    }

    // Réinitialisez le jeu au niveau 1
    private void resetGame() {
        currentLevel = 1;
        generateObstacles();
        generateTriangles();
        canDoubleJump = true;
    }

	public long getTriangleCooldownStartTime() {
		return triangleCooldownStartTime;
	}

	public void setTriangleCooldownStartTime(long triangleCooldownStartTime) {
		this.triangleCooldownStartTime = triangleCooldownStartTime;
	}
}

