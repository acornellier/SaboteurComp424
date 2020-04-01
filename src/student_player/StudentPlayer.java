package student_player;

import java.util.*;

import Saboteur.cardClasses.*;
import boardgame.Move;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.SaboteurPlayer;

import static Saboteur.SaboteurBoardState.BOARD_SIZE;
import static Saboteur.SaboteurBoardState.hiddenPos;

public class StudentPlayer extends SaboteurPlayer {
    private static final double NEVER = 0;
    private static final double DESTROY = 5;
    private static final double TILE = 10;
    private static final double DROP_HIDDEN = 25;
    private static final double SEND_MALUS = 50;
    private static final double CLEAR_MALUS = 75;
    private static final double MAP = 100;

    private static final ArrayList<String> DROP_RANKING_HIDDEN = new ArrayList<>(Arrays.asList(
        "Map",
        "Tile:8",
        "Tile:6",
        "Tile:0",
        "Bonus",
        "Tile:9",
        "Tile:10",
        "Tile:7",
        "Tile:5",
        "Malus",
        "Tile:1",
        "Tile:2",
        "Tile:3",
        "Tile:4",
        "Tile:11",
        "Tile:12",
        "Tile:13",
        "Tile:14",
        "Tile:15",
        "Destroy"
    ));

    private Coord nugget = null;

    public StudentPlayer() {
        super("alex");
    }

    public Move chooseMove(SaboteurBoardState state) {
        try {
            check_nugget(state);
            return best_move(state);
        } catch (Exception e) {
            e.printStackTrace();
            return state.getRandomMove();
        }
    }

    private void check_nugget(SaboteurBoardState state) throws Exception {
        if (nugget != null)
            return;

        ArrayList<Coord> goal_tiles = new ArrayList<>();

        SaboteurTile[][] board = state.getBoardForDisplay();
        for (int[] hidden_pair : hiddenPos) {
            Coord hidden_coord = new Coord(hidden_pair[0], hidden_pair[1]);
            SaboteurTile tile = board[hidden_coord.y][hidden_coord.x];
            if (tile == null)
                throw new Exception("null hidden tile");

            if (tile.getIdx().equals("nugget")) {
                nugget = hidden_coord;
            } else if (tile.getIdx().equals("goalTile")) {
                goal_tiles.add(hidden_coord);
            }
        }

        if (goal_tiles.size() == 1) {
            nugget = goal_tiles.get(0);
        }
    }

    private SaboteurMove best_move(SaboteurBoardState state) throws Exception {
        HashMap<SaboteurMove, Double> move_scores = new HashMap<>();
        for (SaboteurMove move : state.getAllLegalMoves()) {
            move_scores.put(move, move_value(state, move));
        }

        SaboteurMove best_move = null;
        double best_score = -1;
        for (Map.Entry<SaboteurMove, Double> move_score : move_scores.entrySet()) {
            if (move_score.getValue() > best_score) {
                best_score = move_score.getValue();
                best_move = move_score.getKey();
            }
        }
        System.out.println("move " + state.getTurnNumber() + ": " + best_move.toTransportable());
        return best_move;
    }

    private double move_value(SaboteurBoardState state, SaboteurMove move) throws Exception {
        SaboteurCard card = move.getCardPlayed();

        if (card instanceof SaboteurTile) {
            return tile_value(state, move);
        } else if (card instanceof SaboteurBonus) {
            return state.getNbMalus(state.getTurnPlayer()) > 0 ? CLEAR_MALUS : NEVER;
        } else if (card instanceof SaboteurMalus) {
            return SEND_MALUS;
        } else if (card instanceof SaboteurMap) {
            return nugget == null ? MAP : NEVER;
        } else if (card instanceof SaboteurDestroy) {
            return DESTROY;
        } else if (card instanceof SaboteurDrop) {
            return drop_value(state, move);
        } else {
            throw new Exception("unknown card type");
        }
    }

        private double drop_value(SaboteurBoardState state, SaboteurMove move) {
            if (nugget != null)
                return NEVER;

            ArrayList<SaboteurCard> hand = state.getCurrentPlayerCards();
            SaboteurCard dropping = hand.get(move.getPosPlayed()[0]);
            int card_idx = DROP_RANKING_HIDDEN.indexOf(dropping.getName());
            double drop_score = card_idx / (double) DROP_RANKING_HIDDEN.size();
            return DROP_HIDDEN + drop_score;
        }

        private double tile_value(SaboteurBoardState state, SaboteurMove move) {
        if (nugget == null)
            return NEVER;

        Coord pos = new Coord(move.getPosPlayed()[0], move.getPosPlayed()[1]);
        double distance = Math.abs(nugget.x - pos.x) + Math.abs(nugget.y - pos.y);
        return TILE + (1 - distance / (2 * BOARD_SIZE));
    }
}