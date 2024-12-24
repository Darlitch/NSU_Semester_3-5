package data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import proto.SnakesProto;

@Getter
@Builder
public class ToSendMessageWrapper {
    @Setter
    private long msgSeq;
    private SnakesProto.GameMessage message;
    private String ip;
    private Integer port;
    @Setter
    private Long sentAt;
    @Setter
    private int retryCount = 3;
}