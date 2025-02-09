package io;

import java.util.Comparator;
import proto.SnakesProto;

public class PlayersRateComp implements Comparator<SnakesProto.GamePlayer>{
    @Override
    public int compare(SnakesProto.GamePlayer o1, SnakesProto.GamePlayer o2) {
        return o2.getScore() - o1.getScore();
    }
}