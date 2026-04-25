
import enums.DiscColor;
import enums.GameState;


public class Game {
    private final int ROWS = 6, COLS = 7;
    private final Player player1, player2;
    private Player activePlayer;
    private final Board board;
    private GameState state;

    public Game(Player player1, Player player2){
        this.player1 = player1;
        this.player2 = player2;

        // Player 1 is always active player.
        this.activePlayer = player1;

        // Assign discs at the beginning of the game.
        player1.assignDiscColor(DiscColor.RED);
        player2.assignDiscColor(DiscColor.YELLOW);

        // Setup board.
        this.board = new Board(ROWS, COLS);

        this.state = GameState.ACTIVE;
    }

    public GameState placeDisc(Player player, int col){
        // Is game active?
        if(this.state != GameState.ACTIVE) return state;

        // Is current player allowed to make move?
        if(!player.getId().equals(activePlayer.getId())){
            System.err.println("Invalid move");
            return GameState.ACTIVE;
        }

        // Place disc on board.
        final DiscColor currPlayerDisc = activePlayer.getPlayerColor();
        int diskPlacedOnRow = this.board.placeDisc(currPlayerDisc, col);
        if(diskPlacedOnRow == -1){
            System.err.println("Invalid move");
            return GameState.ACTIVE;
        }

        // Check if connect 4?
        if(this.board.isConnect4(diskPlacedOnRow, col)){
            state = GameState.WIN;
            return GameState.WIN;
        }

        // Check if game is full.
        if(this.board.isFull()){
            this.state = GameState.DRAW;
            return GameState.DRAW;
        }

        // Change active player
        if(activePlayer.getId().equals(player1.getId())){
            activePlayer = player2;
        } else {
            activePlayer = player1;
        }

        return GameState.ACTIVE;

    }


}