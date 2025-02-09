package data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import proto.SnakesProto;

@Getter
@Builder
public class MessageWithSender {
    private SnakesProto.GameMessage message;
    private String ip;
    private Integer port;
    @Setter
    private boolean onlyView;
}