package io;

import proto.SnakesProto.GameMessage;
import data.MessageWithSender;
import data.ToSendMessageWrapper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UnicastManager {
    private static final int ACK_CHECK_MS = 2000;
    private static final int BUF_SIZE = 65000;

    private final DatagramSocket socket;
    private long msgSeq = 0;

    private final BlockingQueue<ToSendMessageWrapper> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<MessageWithSender> receiveQueue = new LinkedBlockingQueue<>();
    private final List<ToSendMessageWrapper> sentList = new ArrayList<>();

    private final Thread sendWorkerThread;
    private final Thread receiveWorkerThread;
    private final Thread ackCheckWorkerThread;

    private volatile boolean stopped = false;

    public UnicastManager(DatagramSocket socket) {
        this.socket = socket;

        sendWorkerThread = new Thread(this::sendWorker);
        sendWorkerThread.start();
        receiveWorkerThread = new Thread(this::receiveWorker);
        receiveWorkerThread.start();
        ackCheckWorkerThread = new Thread(this::ackCheckWorker);
        ackCheckWorkerThread.start();
    }

    void stop() {
        stopped = true;
        sendWorkerThread.interrupt();
        receiveWorkerThread.interrupt();
        ackCheckWorkerThread.interrupt();
        sendQueue.clear();
        receiveQueue.clear();
    }

    public void sendPacket(String ip, int port, GameMessage msg) {
        sendQueue.add(ToSendMessageWrapper.builder().ip(ip).port(port).message(msg).build());
    }

    public MessageWithSender receivePacket() throws InterruptedException {
        return receiveQueue.take();
    }

    private void receiveWorker() {
        byte[] receiveBuffer = new byte[BUF_SIZE];
        while (!stopped) {
            var receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            try {
                socket.receive(receivePacket);
                byte[] bytes = new byte[receivePacket.getLength()];
                System.arraycopy(receiveBuffer, 0, bytes, 0, receivePacket.getLength());
                var gameMessage = GameMessage.parseFrom(bytes);
                receiveQueue.add(MessageWithSender.builder().message(gameMessage).port(receivePacket.getPort()).ip(receivePacket.getAddress().getHostAddress()).build());
                receivePacket.setLength(receiveBuffer.length);

                if (gameMessage.hasAck()) {
                    synchronized (sentList) {
                        var elem = sentList.stream()
                                .filter(wrapper -> wrapper.getMsgSeq() == gameMessage.getMsgSeq())
                                .findAny();
                        elem.ifPresent(sentList::remove);
                    }
                } else {
                    var ackData = GameMessage.newBuilder()
                            .setAck(GameMessage.AckMsg.getDefaultInstance())
                            .setMsgSeq(gameMessage.getMsgSeq())
                            .build()
                            .toByteArray();
                    var ackPacket = new DatagramPacket(ackData, ackData.length, receivePacket.getAddress(), receivePacket.getPort());
                    socket.send(ackPacket);
                }
            } catch (IOException e) {}
        }
    }

    private void ackCheckWorker() {
        while (true) {
            try {
                Thread.sleep(ACK_CHECK_MS);
            } catch (InterruptedException e) {
                break;
            }
            var currentTime = System.currentTimeMillis();
            synchronized (sentList) {
                sentList.stream()
                        .filter(wrapper -> currentTime - wrapper.getSentAt() > ACK_CHECK_MS)
                        .forEach(wrapper -> {
                            if (wrapper.getRetryCount() > 0) {
                                wrapper.setRetryCount(wrapper.getRetryCount() - 1);
                                sendQueue.add(wrapper);
                            }
                        });
                sentList.removeIf(wrapper -> currentTime - wrapper.getSentAt() > ACK_CHECK_MS);
            }
        }
    }

    private void sendWorker() {
        while (true) {
            ToSendMessageWrapper wrapper;
            try {
                wrapper = sendQueue.take();
            } catch (InterruptedException e) {
                break;
            }

            msgSeq++;

            var sendData = GameMessage.newBuilder(wrapper.getMessage()).setMsgSeq(msgSeq).build().toByteArray();
            wrapper.setMsgSeq(msgSeq);
            wrapper.setSentAt(System.currentTimeMillis());
            try {
                var packet = new DatagramPacket(
                        sendData,
                        sendData.length,
                        InetAddress.getByName(wrapper.getIp()),
                        wrapper.getPort()
                );

                synchronized (sentList) {
                    sentList.add(wrapper);
                }

                socket.send(packet);

            } catch (IOException e) {}
        }
    }
}