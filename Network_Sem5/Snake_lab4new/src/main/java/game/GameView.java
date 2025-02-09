package game;

import lombok.AllArgsConstructor;
import lombok.Data;
import proto.SnakesProto;

public interface GameView {
    @AllArgsConstructor
    @Data
    class Control {
        private Integer playerId;
        private SnakesProto.Direction direction;
    }

    void setState(SnakesProto.GameState state);
}