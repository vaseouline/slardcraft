package slard.craft.town;

public class PlayerState implements java.io.Serializable {
    public boolean isButtered;
    public boolean inTown;
    public transient boolean hasDmgTask;

    PlayerState(boolean isButtered, boolean inTown) {
        this.isButtered = isButtered;
        this.inTown = inTown;
        hasDmgTask = false;
    }

}