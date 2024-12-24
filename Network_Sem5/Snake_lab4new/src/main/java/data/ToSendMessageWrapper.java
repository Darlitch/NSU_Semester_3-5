package data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import proto.SnakesProto;

@Builder
public class ToSendMessageWrapper {
    @Getter
    @Setter
    private long msgSeq;
    @Getter
    private SnakesProto.GameMessage message;
    @Getter
    private String ip;
    @Getter
    private Integer port;
    @Getter
    @Setter
    private Long sentAt;
    @Getter
    @Setter
    private int retryCount = 3;
}