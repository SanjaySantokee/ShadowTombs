import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import javax.sound.sampled.AudioFormat;

public class GameManager extends GameCore {

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


        map = resourceManager.loadNextMap();


        backgroundSound = new SoundClip();
        boopSound = new SoundClip();
        prizeSound = new SoundClip();

        backgroundSound.open(Constants.BACKGROUND_SOUND);

        backgroundSound.play(0);
        backgroundSound.loop();
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


    public void draw(Graphics2D g) {
        renderer.draw(g, map,
                screen.getWidth(), screen.getHeight());
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

                boopSound.open("sounds/boop2.wav");
                boopSound.play(0);
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
            } else {

                player.setState(Creature.STATE_DYING);
            }
        }
    }


    public void acquirePowerUp(PowerUp powerUp) {

        map.removeSprite(powerUp);
        prizeSound.open("sounds/prize.wav");

        if (powerUp instanceof PowerUp.Star) {

            prizeSound.play(0);
        } else if (powerUp instanceof PowerUp.Music) {

            prizeSound.play(0);
        } else if (powerUp instanceof PowerUp.Goal) {

            prizeSound.play(0);
            map = resourceManager.loadNextMap();
        }
    }

}
