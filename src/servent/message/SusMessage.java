package servent.message;

public class SusMessage extends BasicMessage{
    public SusMessage(int senderPort, int receiverPort) {
        super(MessageType.SUS, senderPort, receiverPort);
    }
}
