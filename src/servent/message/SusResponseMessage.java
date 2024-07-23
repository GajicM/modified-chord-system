package servent.message;

public class SusResponseMessage extends BasicMessage{
    private boolean notSus;
    public SusResponseMessage(int senderPort, int receiverPort,boolean isNotSus) {
        super(MessageType.SUS_RESPONSE, senderPort, receiverPort);
        notSus=isNotSus;
    }

    public boolean isNotSus() {
        return notSus;
    }


}
