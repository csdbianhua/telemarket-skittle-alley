package telemarketer.skittlealley.model.game.noonesurvived;

public abstract class NosItem {

    protected final long id;
    /**
     * 最大血量
     */
    protected int maxHealth;
    /**
     * 当前血量
     */
    protected int health;
    /**
     * 名称
     */
    protected String name;
    /**
     * 速度
     */
    protected int speed;
    /**
     * 移动方向
     */
    protected double movingDirection;

    /**
     * 面对方向
     */
    protected double facingDirection;

    /**
     * position x
     */
    protected int x;
    /**
     * position y
     */
    protected int y;

    protected NosItem(long id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getId() {
        return id;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(double facingDirection) {
        this.facingDirection = facingDirection;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public double getMovingDirection() {
        return movingDirection;
    }

    public void setMovingDirection(double movingDirection) {
        this.movingDirection = movingDirection;
    }
}
