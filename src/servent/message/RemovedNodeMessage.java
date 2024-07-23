package servent.message;

import app.ServentInfo;

public class RemovedNodeMessage extends BasicMessage{
    ServentInfo removedServentInfo;
    public RemovedNodeMessage(int senderPort, int receiverPort, ServentInfo removedNode) {
        super(MessageType.REMOVED_NODE, senderPort, receiverPort);
        this.removedServentInfo=removedNode;
    }

    public ServentInfo getRemovedServentInfo() {
        return removedServentInfo;
    }

    @Override
    public String toString() {
        return super.toString() + "removed= " +removedServentInfo;
    }
}
