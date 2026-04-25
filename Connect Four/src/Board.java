import enums.DiscColor;
import java.util.Arrays;

public class Board {

    private final int ROWS, COLS;
    private final DiscColor[][] board;
    // Maintains the available row to place disc in each column.
    private final int[] colState;

    public Board(int rows, int cols){
        this.ROWS = rows;
        this.COLS = cols;
        this.board = new DiscColor[ROWS][COLS];
        this.colState = new int[COLS];

        // Fill board with none.
        for(int i=0; i<ROWS; ++i) Arrays.fill(board[i], DiscColor.NONE);
    }

    public int placeDisc(DiscColor color, int col){
        // Check if row is available in specified column,
        if(colState[col] == ROWS) return -1;
        else {
            int availableRow = colState[col];
            board[availableRow][col] = color;
            ++colState[col];
            return availableRow;
        }
    }

    public boolean isConnect4(int row, int col){
        // Check if there is a connect 4.
        // Check downwards first.
        final DiscColor currentColor = board[row][col];
        int count = 0;
        for(int i=row; i>=0; --i){
            if(currentColor == board[i][col]) ++count;
            else break;
        }
        if(count >= 4) return true;

        // Check left & right.
        count = 0;
        for(int j=col; j>=0; --j){
            if(currentColor == board[row][j]) ++count;
            else break;
        }
        for(int j=col+1; j<COLS; ++j){
            if(currentColor == board[row][j]) ++count;
            else break;
        }
        if(count >= 4) return true;

        // Check diagonals.
        //
        final int[][] diagonals1 = new int[][]{
            {1, 1}, {-1, -1}
        };

        final int[][] diagonals2 = new int[][]{
            {1, -1}, {-1, 1}
        };
        return checkDiagonals(row, col, diagonals1) || checkDiagonals(row, col, diagonals2);
    }

    private boolean checkDiagonals(int row, int col, int[][] dirs){
        int count = 1;
        final DiscColor currentColor = board[row][col];

        for(int[] dir: dirs){
            int i = row + dir[0];
            int j = col + dir[1];
            while(i >= 0 && i<ROWS && j>=0 && j<COLS){
                if(currentColor == board[i][j]){
                    count += 1;
                    i += dir[0];
                    j += dir[1];
                }
                else break;
            }
        }

        return (count >= 4);
    }

    public boolean isFull(){
        for(int j=0; j<COLS; ++j){
            if(colState[j] != ROWS) return false;
        }
        return true;
    }

}