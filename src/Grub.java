
public class Grub extends Creature {

    public Grub(Animation left, Animation right,
                Animation deadLeft, Animation deadRight, Animation walkLeft, Animation walkRight) {
        super(left, right, deadLeft, deadRight, walkLeft, walkRight, false);
    }


    public float getMaxSpeed() {
        return 0.05f;
    }

}
