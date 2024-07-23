package servent.message;

import app.AppConfig;

import java.util.List;

public class ReplyTokenMessage extends BasicMessage {

    private final List<Integer> tokenQueue;
    private final int[] lastRequest;
    private final int requesterId;
    public ReplyTokenMessage(int senderPort, int receiverPort, String messageText, List<Integer> tokenQueue, int[] lastRequest,int requesterId) {
        super(MessageType.TOKEN_REPLY, senderPort, receiverPort, messageText);
        this.tokenQueue=tokenQueue;
        this.lastRequest=lastRequest;
        this.requesterId=requesterId;
    }

    public List<Integer> getTokenQueue() {
        return tokenQueue;
    }

    public int[] getLastRequest() {
        return lastRequest;
    }

    public int getRequesterId() {
        return requesterId;
    }
}
