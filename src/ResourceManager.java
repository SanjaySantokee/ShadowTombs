import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;


public class ResourceManager {

    private ArrayList tiles;
    private int currentMap;
    private GraphicsConfiguration gc;


    private Sprite playerSprite;
    private Sprite musicSprite;
    private Sprite coinSprite;
    private Sprite goalSprite;
    private Sprite grubSprite;
    private Sprite flySprite;
    private Sprite goalSprite2;
    private boolean endgame = false;


    public ResourceManager(GraphicsConfiguration gc) {
        this.gc = gc;
        loadTileImages();
        loadCreatureSprites();
        loadPowerUpSprites();
    }

    public boolean isEndgame() {
        return endgame;
    }

    public Image loadImage(String name) {
        String filename = "assets/images/" + name;
        return new ImageIcon(filename).getImage();
    }


    public Image getMirrorImage(Image image) {
        return getScaledImage(image, -1, 1);
    }


    public Image getFlippedImage(Image image) {
        return getScaledImage(image, 1, -1);
    }


    private Image getScaledImage(Image image, float x, float y) {


        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate(
                (x - 1) * image.getWidth(null) / 2,
                (y - 1) * image.getHeight(null) / 2);


        Image newImage = gc.createCompatibleImage(
                image.getWidth(null),
                image.getHeight(null),
                Transparency.BITMASK);


        Graphics2D g = (Graphics2D) newImage.getGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }


    public TileMap loadNextMap() {
        TileMap map = null;

        while (map == null) {

            currentMap++;

            if (currentMap == 2)
                endgame = true;

            try {
                map = loadMap(
                        "assets/maps/map" + currentMap + ".txt");
            } catch (IOException ex) {

                if (currentMap == 1) {

                    return null;
                }
                currentMap = 0;
                map = null;
            }
        }

        return map;
    }


    public TileMap reloadMap() {
        try {
            return loadMap(
                    "assets/maps/map" + currentMap + ".txt");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    private TileMap loadMap(String filename)
            throws IOException {
        ArrayList lines = new ArrayList();
        int width = 0;
        int height = 0;


        BufferedReader reader = new BufferedReader(
                new FileReader(filename));
        while (true) {
            String line = reader.readLine();

            if (line == null) {
                reader.close();
                break;
            }


            if (!line.startsWith("#")) {
                lines.add(line);
                width = Math.max(width, line.length());
            }
        }


        height = lines.size();
        TileMap newMap = new TileMap(width, height);
        for (int y = 0; y < height; y++) {
            String line = (String) lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char ch = line.charAt(x);


                int tile = ch - 'A';
                if (tile >= 0 && tile < tiles.size()) {
                    newMap.setTile(x, y, (Image) tiles.get(tile));
                } else if (ch == 'o') {
                    addSprite(newMap, coinSprite, x, y);
                } else if (ch == '!') {
                    addSprite(newMap, musicSprite, x, y);
                } else if (ch == '*') {
                    addSprite(newMap, goalSprite, x, y);
                } else if (ch == '&') {
                    addSprite(newMap, goalSprite2, x, y);
                } else if (ch == '1') {
                    addSprite(newMap, grubSprite, x, y);
                } else if (ch == '2') {
                    addSprite(newMap, flySprite, x, y);
                }
            }
        }


        Sprite player = (Sprite) playerSprite.clone();
        player.setX(TileMapRenderer.tilesToPixels(3));
        player.setY(0);
        newMap.setPlayer(player);

        return newMap;
    }


    private void addSprite(TileMap map,
                           Sprite hostSprite, int tileX, int tileY) {
        if (hostSprite != null) {

            Sprite sprite = (Sprite) hostSprite.clone();


            sprite.setX(
                    TileMapRenderer.tilesToPixels(tileX) +
                            (TileMapRenderer.tilesToPixels(1) -
                                    sprite.getWidth()) / 2);


            sprite.setY(
                    TileMapRenderer.tilesToPixels(tileY + 1) -
                            sprite.getHeight());


            map.addSprite(sprite);
        }
    }


    public void loadTileImages() {
        tiles = new ArrayList();
        char ch = 'A';
        while (true) {
            String name = "tile_" + ch + ".png";
            File file = new File("assets/images/" + name);
            if (!file.exists()) {
                break;
            }
            tiles.add(loadImage(name));
            ch++;
        }

//        tiles = new ArrayList();
//        char ch = 'A';
////        String[] tileID = {"W", "N", "L", "R", "T"};
////        int i =0;
//        while (true) {
//            String name = "tile_" + ch + ".png";
//            System.out.println(name);
//            File file = new File("images/" + name);
//            if (!file.exists()) {
//                break;
//            }
//            tiles.add(loadImage("/tiles" + name));
//            ch++;
//        }
    }


    public void loadCreatureSprites() {

        Image[][] images = new Image[4][];


        images[0] = new Image[]{
                loadImage("player/walk-with-weapon-1.png"),
                loadImage("player/walk-with-weapon-2.png"),
                loadImage("ghost-1.png"),
                loadImage("ghost-2.png"),
                loadImage("ghost-3.png"),
                loadImage("ghostHalo-1.png"),
                loadImage("ghostHalo-2.png"),
                loadImage("ghostHalo-3.png"),
                loadImage("player/walk-with-weapon-3.png"),
                loadImage("player/walk-with-weapon-4.png"),
                loadImage("player/walk-with-weapon-5.png"),
                loadImage("player/walk-with-weapon-6.png"),
                loadImage("player/walk-with-weapon-7.png"),
                loadImage("player/walk-with-weapon-8.png"),
                loadImage("player/walk-with-weapon-9.png"),
                loadImage("player/walk-with-weapon-10.png"),
                loadImage("player/walk-with-weapon-11.png"),
        };

        images[1] = new Image[images[0].length];
        images[2] = new Image[images[0].length];
        images[3] = new Image[images[0].length];
        for (int i = 0; i < images[0].length; i++) {

            images[1][i] = getMirrorImage(images[0][i]);

            images[2][i] = getFlippedImage(images[0][i]);

            images[3][i] = getFlippedImage(images[1][i]);
        }


        Animation[] playerAnim = new Animation[4];
        Animation[] flyAnim = new Animation[4];
        Animation[] grubAnim = new Animation[4];
        Animation[] playerWalkAnim = new Animation[4];
        for (int i = 0; i < 4; i++) {
            playerAnim[i] = createPlayerAnim(
                    images[i][0], images[i][1]);
            flyAnim[i] = createFlyAnim(
                    images[i][2], images[i][3], images[i][4]);
            grubAnim[i] = createGrubAnim(
                    images[i][5], images[i][6], images[i][7]);
            playerWalkAnim[i] = createPlayerWalkAnim(images[i][0], images[i][1], images[i][7], images[i][8],
                    images[i][9], images[i][10], images[i][11], images[i][12],
                    images[i][13], images[i][14], images[i][15]);
        }


        playerSprite = new Player(playerAnim[1], playerAnim[0],
                playerAnim[2], playerAnim[3], playerWalkAnim[1], playerWalkAnim[0]);
        flySprite = new Fly(flyAnim[1], flyAnim[0],
                flyAnim[2], flyAnim[3], playerWalkAnim[1], playerWalkAnim[0]);
        grubSprite = new Grub(grubAnim[1], grubAnim[0],
                grubAnim[2], grubAnim[3], playerWalkAnim[1], playerWalkAnim[0]);

    }


    private Animation createPlayerAnim(Image player1, Image player2) {

        Animation anim = new Animation();
        anim.addFrame(player1, 250);
        anim.addFrame(player2, 250);
//        anim.addFrame(player1, 50);
        return anim;

    }

    private Animation createPlayerWalkAnim(Image player1, Image player2, Image player3, Image player4, Image player5,
                                           Image player6, Image player7, Image player8, Image player9, Image player10,
                                           Image player11)
    {
        Animation anim = new Animation();
        anim.addFrame(player1, 60);
        anim.addFrame(player2, 60);
//        anim.addFrame(player3, 60);
//        anim.addFrame(player4, 60);
//        anim.addFrame(player5, 60);
//        anim.addFrame(player6, 60);
//        anim.addFrame(player7, 60);
//        anim.addFrame(player8, 60);
//        anim.addFrame(player9, 60);
//        anim.addFrame(player10, 60);
//        anim.addFrame(player11, 60);
        return anim;
    }


    private Animation createFlyAnim(Image img1, Image img2,
                                    Image img3) {
        Animation anim = new Animation();
        anim.addFrame(img1, 50);
        anim.addFrame(img2, 50);
        anim.addFrame(img3, 50);
        anim.addFrame(img2, 50);
        return anim;
    }


    private Animation createGrubAnim(Image img1, Image img2, Image img3) {
        Animation anim = new Animation();
        anim.addFrame(img1, 250);
        anim.addFrame(img2, 250);
        anim.addFrame(img3, 250);
        return anim;
    }


    private void loadPowerUpSprites() {

        Animation anim = new Animation();
        anim.addFrame(loadImage("sleleton-1.png"), 100);
        anim.addFrame(loadImage("sleleton-2.png"), 100);
        anim.addFrame(loadImage("sleleton-3.png"), 100);
        anim.addFrame(loadImage("sleleton-4.png"), 100);
        anim.addFrame(loadImage("sleleton-5.png"), 100);
        anim.addFrame(loadImage("sleleton-6.png"), 100);
        goalSprite = new PowerUp.Goal(anim);

        anim = new Animation();
        anim.addFrame(loadImage("artemis-1.png"), 100);
        anim.addFrame(loadImage("artemis-2.png"), 100);
        anim.addFrame(loadImage("artemis-3.png"), 100);
        anim.addFrame(loadImage("artemis-4.png"), 100);
        goalSprite2 = new PowerUp.Goal(anim);


        anim = new Animation();
        anim.addFrame(loadImage("mist-1.png"), 90);
        anim.addFrame(loadImage("mist-2.png"), 90);
        anim.addFrame(loadImage("mist-3.png"), 90);
        anim.addFrame(loadImage("mist-4.png"), 90);
        anim.addFrame(loadImage("mist-5.png"), 90);
        anim.addFrame(loadImage("mist-6.png"), 90);
        coinSprite = new PowerUp.Star(anim);


        anim = new Animation();
        anim.addFrame(loadImage("mist-1.png"), 90);
        anim.addFrame(loadImage("mist-2.png"), 90);
        anim.addFrame(loadImage("mist-3.png"), 90);
        anim.addFrame(loadImage("mist-4.png"), 90);
        anim.addFrame(loadImage("mist-5.png"), 90);
        anim.addFrame(loadImage("mist-6.png"), 90);
        musicSprite = new PowerUp.Music(anim);
    }

}
