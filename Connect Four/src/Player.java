import enums.DiscColor;

public class Player {

    private final String id;
    private DiscColor discColor;

    public Player(String id){
        this.id = id;
    }

    public void assignDiscColor(DiscColor color){
        this.discColor = color;
    }

    public DiscColor getPlayerColor() {
        return this.discColor;
    }

    public String getId(){
        return this.id;
    }
    
}