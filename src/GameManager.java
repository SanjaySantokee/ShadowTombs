import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;
import javax.sound.sampled.AudioFormat;

public class GameManager extends GameCore {

    private boolean win = false;
    private int secondsLeft = 40;
    private boolean loser = false;
    private Scanner scoreScanner;
    private List<Integer> highscores = new ArrayList<>();
    private boolean computedScore = false;


    public static void main(String[] args) {
        new GameManager().run();
    }

    private static final AudioFormat PLAYBACK_FORMAT =
            new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    private TileMap map;

    private ResourceManager resourceManager;
    private SoundClip prizeSound;
    private SoundClip boopSound;
    private SoundClip backgroundSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;

    private Image endGame;


    public void init() {
        super.init();


        initInput();


        resourceManager = new ResourceManager(
                screen.getFullScreenWindow().getGraphicsConfiguration());


        renderer = new TileMapRenderer();
        renderer.setBackground(
                resourceManager.loadImage("backgrounds/mountainsRocksBackground.png"),
                resourceManager.loadImage("backgrounds/skyBackground.png"),
                resourceManager.loadImage("backgrounds/skyBackground.png"));

        endGame = resourceManager.loadImage("backgrounds/skyBackground.png");
        map = resourceManager.loadNextMap();


        backgroundSound = new SoundClip();
        boopSound = new SoundClip();
        prizeSound = new SoundClip();

        backgroundSound.open("assets/sounds/zelda.wav");

        backgroundSound.play(0);
        backgroundSound.loop();

        startCountdown();
        loadScoresFile();
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    //Does time countdown
    private java.util.Timer countdown = new java.util.Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {

            int secondsLeft = getSecondsLeft();

            if (win) {
                stopCountDown();
                return;
            }

            if (secondsLeft - 1 < 0) {
                loser = true;
                stopCountDown();
                return;
            }

            setSecondsLeft(secondsLeft - 1);


//            System.out.println("Seconds Left: " + secondsLeft);
        }
    };

    public void startCountdown() {
        countdown.scheduleAtFixedRate(task, 2000, 1000);
    }

    public void stopCountDown() {
        countdown.cancel();
    }

    public void loadScoresFile(){
        try {
            scoreScanner = new Scanner(new File("assets/scores/highscores.txt"));
        } catch (Exception e){
            System.out.println("Error Loading File : highscores.txt");
        }

        String scoreString;
        int scoreValue = 0;

        while(scoreScanner.hasNext()){
            scoreString = scoreScanner.next();
            scoreValue = Integer.valueOf(scoreString);
            highscores.add(scoreValue);
        }

        scoreScanner.close();

        Collections.sort(highscores, Collections.reverseOrder());
    }

    public List<Integer> getHighscores() {
        return highscores;
    }

    public void setScore(int score){

        if (computedScore)
            return;

        computedScore = true;

        highscores.add(score);
        Collections.sort(highscores, Collections.reverseOrder());

        try{
            FileWriter fw=new FileWriter("assets/scores/highscores.txt");
            fw.write(getHighscores().get(0).toString() + "\n");
            fw.write(getHighscores().get(1).toString() + "\n");
            fw.write(getHighscores().get(2).toString() + "\n");
            fw.close();
        }catch(Exception e){System.out.println(e);}

    }


    public void stop() {
        super.stop();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
                GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
                GameAction.DETECT_INITAL_PRESS_ONLY);

        inputManager = new InputManager(
                screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_A);
        inputManager.mapToKey(moveRight, KeyEvent.VK_D);
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }

        Player player = (Player) map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
            if (moveLeft.isPressed()) {
                velocityX -= player.getMaxSpeed();
//                player.setWalkingLeft(true);
//                player.setWalkingRight(false);
            }
            if (moveRight.isPressed()) {
                velocityX += player.getMaxSpeed();
//                player.setWalkingLeft(false);
//                player.setWalkingRight(true);
            }
            if (jump.isPressed()) {
                player.jump(false);
            }
            player.setVelocityX(velocityX);
        }

    }

    public TileMap getMap() {
        return map;
    }


    public Point getTileCollision(Sprite sprite,
                                  float newX, float newY) {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);


        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
                toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
                toY + sprite.getHeight() - 1);


        for (int x = fromTileX; x <= toTileX; x++) {
            for (int y = fromTileY; y <= toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                        map.getTile(x, y) != null) {

                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }


        return null;
    }


    public boolean isCollision(Sprite s1, Sprite s2) {

        if (s1 == s2) {
            return false;
        }


        if (s1 instanceof Creature && !((Creature) s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature) s2).isAlive()) {
            return false;
        }


        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());


        return (s1x < s2x + s2.getWidth() &&
                s2x < s1x + s1.getWidth() &&
                s1y < s2y + s2.getHeight() &&
                s2y < s1y + s1.getHeight());
    }


    public Sprite getSpriteCollision(Sprite sprite) {


        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite) i.next();
            if (isCollision(sprite, otherSprite)) {

                return otherSprite;
            }
        }


        return null;
    }

    public void paintLeaderBoard(Graphics2D g2){
        Font f = new Font("Consolas", Font.BOLD, 50);
        g2.setFont(f);

        g2.setColor(new Color(255, 255, 255));

        g2.drawString("Leaderboard", 400, 270);

        f = new Font("Consolas", Font.BOLD, 30);
        g2.setFont(f);

        g2.drawString("1. 00:00:" + highscores.get(0).toString(), 460, 300);
        g2.drawString("2. 00:00:" + highscores.get(1).toString(), 460, 330);
        g2.drawString("3. 00:00:" + highscores.get(2).toString(), 460, 360);

        f = new Font("Calibri", Font.BOLD, 40);
        g2.setFont(f);
        g2.drawString("PRESS [ESC] TO QUIT", 400, 430);
    }

    public void draw(Graphics2D g) {

        Font f = new Font("Calibri", Font.BOLD, 30);
        g.setFont(f);

        if (win) {
            setScore(secondsLeft);
            g.drawImage(endGame, 0, 0, screen.getWidth(), screen.getHeight(), null);
            g.drawString("You Win !", screen.getWidth()/2, screen.getHeight()/2);
            paintLeaderBoard(g);
        }
        else if(loser){
            g.drawImage(endGame, 0, 0, screen.getWidth(), screen.getHeight(), null);
            g.drawString("You Lose !", screen.getWidth()/2, screen.getHeight()/2);
            paintLeaderBoard(g);
        }
        else{
            renderer.draw(g, map, screen.getWidth(), screen.getHeight());
            g.drawString("Time Left:  00:00:" + secondsLeft, 20, 20);
            g.drawString("Regroup with Artemis to escape the ShadowTombs", 300, 20);
            if (!resourceManager.isEndgame())
                g.drawString("Current Objective: Get to the sacred skeleton to find the path to Artemis", 300, 45);
            else
                g.drawString("Current Objective: Find Artemis and get away from here as fast as you can", 300, 45);
        }
    }



    public void update(long elapsedTime) {

        Creature player = (Creature) map.getPlayer();


        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            return;
        }

        checkInput(elapsedTime);

        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite) i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature) sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                } else {
                    updateCreature(creature, elapsedTime);
                }
            }

            sprite.update(elapsedTime);
        }
    }


    private void updateCreature(Creature creature,
                                long elapsedTime) {


        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                    GRAVITY * elapsedTime);
        }


        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
                getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        } else {

            if (dx > 0) {
                creature.setX(
                        TileMapRenderer.tilesToPixels(tile.x) -
                                creature.getWidth());
            } else if (dx < 0) {
                creature.setX(
                        TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player) creature, false);
        }


        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        } else {

            if (dy > 0) {
                creature.setY(
                        TileMapRenderer.tilesToPixels(tile.y) -
                                creature.getHeight());
            } else if (dy < 0) {
                creature.setY(
                        TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player) creature, canKill);
        }

    }


    public void checkPlayerCollision(Player player,
                                     boolean canKill) {
        if (!player.isAlive()) {
            return;
        }


        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp) collisionSprite);
        } else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature) collisionSprite;
            if (canKill) {

                boopSound.open("assets/sounds/powerUp.wav");
                boopSound.play(0);
                secondsLeft += 2;
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            } else {

                player.setState(Creature.STATE_DYING);
            }
        }
    }


    public void win(){
        Graphics2D g = screen.getGraphics();
        g.drawImage(endGame, 0,0, screen.getWidth(), screen.getHeight(), null);

    }

    public void acquirePowerUp(PowerUp powerUp) {
        secondsLeft += 1;
        map.removeSprite(powerUp);
        prizeSound.open("assets/sounds/powerUp.wav");

        if (powerUp instanceof PowerUp.Star) {

            prizeSound.play(0);
        } else if (powerUp instanceof PowerUp.Music) {

            prizeSound.play(0);
        } else if (powerUp instanceof PowerUp.Goal) {

            prizeSound.play(0);

            if (resourceManager.isEndgame()){
                this.win = true;
            }
            else
                map = resourceManager.loadNextMap();

        }
    }

}
