import java.lang.reflect.Constructor;


public abstract class Creature extends Sprite {


    private static final int DIE_TIME = 1000;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_DYING = 1;
    public static final int STATE_DEAD = 2;

    private Animation left;
    private Animation right;
    private Animation deadLeft;
    private Animation deadRight;
    private Animation walkLeft;
    private Animation walkRight;
    private int state;
    private long stateTime;

    private boolean isPlayer = false;
    private boolean isWalkingLeft = true;
    private boolean isWalkingRight = false;

    public Creature(Animation left, Animation right,
                    Animation deadLeft, Animation deadRight, Animation walkLeft, Animation walkRight, boolean isPlayer) {
        super(right);
        this.left = left;
        this.right = right;
        this.deadLeft = deadLeft;
        this.deadRight = deadRight;
        this.walkLeft = walkLeft;
        this.walkRight = walkRight;
        this.isPlayer = isPlayer;
        state = STATE_NORMAL;
    }


    public Object clone() {

        Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance((Animation) left.clone(),
                    (Animation) right.clone(),
                    (Animation) deadLeft.clone(),
                    (Animation) deadRight.clone(),
                    (Animation) walkLeft.clone(),
                    (Animation) walkRight.clone());
        } catch (Exception ex) {

            ex.printStackTrace();
            return null;
        }
    }

    public void setWalkingLeft(boolean walkingLeft) {
        isWalkingLeft = walkingLeft;
    }

    public void setWalkingRight(boolean walkingRight) {
        isWalkingRight = walkingRight;
    }



    public float getMaxSpeed() {
        return 0;
    }


    public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(-getMaxSpeed());
        }
    }


    public int getState() {
        return state;
    }


    public void setState(int state) {
        if (this.state != state) {
            this.state = state;
            stateTime = 0;
            if (state == STATE_DYING) {
                setVelocityX(0);
                setVelocityY(0);
            }
        }
    }


    public boolean isAlive() {
        return (state == STATE_NORMAL);
    }


    public boolean isFlying() {
        return false;
    }


    public void collideHorizontal() {
        setVelocityX(-getVelocityX());
    }


    public void collideVertical() {
        setVelocityY(0);
    }


    public void update(long elapsedTime) {

        Animation newAnim = anim;
//        if (isPlayer){
//            if (isWalkingLeft)
//                newAnim = walkLeft;
//            if (isWalkingRight)
//                newAnim = walkRight;
//        }
        if (getVelocityX() < 0) {
            newAnim = left;
        } else if (getVelocityX() > 0) {
            newAnim = right;
        }
        if (state == STATE_DYING && newAnim == left) {
            newAnim = deadLeft;
        } else if (state == STATE_DYING && newAnim == right) {
            newAnim = deadRight;
        }


        if (anim != newAnim) {
            anim = newAnim;
            anim.start();
        } else {
            anim.update(elapsedTime);
        }


        stateTime += elapsedTime;
        if (state == STATE_DYING && stateTime >= DIE_TIME) {
            setState(STATE_DEAD);
        }
    }

}
