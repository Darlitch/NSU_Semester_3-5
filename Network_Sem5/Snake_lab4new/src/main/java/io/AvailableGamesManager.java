package io;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.EvictingQueue;
import proto.SnakesProto;
import data.MessageWithSender;
import data.PlayerSignature;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class AvailableGamesManager {
    private final Queue<MessageWithSender> games = EvictingQueue.create(5);
    private final Cache<PlayerSignature, MessageWithSender> allGames = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(3000, TimeUnit.MILLISECONDS).build();
    private final Thread gamesListenerWorkerThread;
    private final MulticastManager multicastManager;

    public AvailableGamesManager(MulticastManager multicastManager) {
        this.multicastManager = multicastManager;

        gamesListenerWorkerThread = new Thread(this::gamesListenerWorker);
        gamesListenerWorkerThread.start();
    }

    private void gamesListenerWorker() {
        while (true) {
            MessageWithSender msg;
            try {
                msg = multicastManager.receivePacket();
            } catch (InterruptedException e) {
                break;
            }
            if (msg.getMessage().hasAnnouncement()) {
                allGames.put(new PlayerSignature(msg.getIp(), msg.getPort()), msg);
                games.add(msg);
            }
        }
    }

    public void announce(SnakesProto.GameMessage.AnnouncementMsg announcementMsg) {
        multicastManager.sendPacket(
                SnakesProto.GameMessage
                        .newBuilder()
                        .setAnnouncement(
                                announcementMsg
                        )
                        .setMsgSeq(0)
                        .build()
        );
    }

    public Collection<MessageWithSender> getAllGames() {
        return allGames.asMap().values();
    }

    void stop() {
        gamesListenerWorkerThread.interrupt();
    }
}