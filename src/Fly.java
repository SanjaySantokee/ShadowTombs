public class Fly extends Creature {

    public Fly(Animation left, Animation right,
               Animation deadLeft, Animation deadRight, Animation walkLeft, Animation walkRight) {
        super(left, right, deadLeft, deadRight, walkLeft, walkRight, false);
    }


    public float getMaxSpeed() {
        return 0.2f;
    }


    public boolean isFlying() {
        return isAlive();
    }

}
