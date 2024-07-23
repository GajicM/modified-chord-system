package servent.message;

public class TellDeleteMessage extends BasicMessage{
    private final String retVal;
    private final int originalAsker;
    public TellDeleteMessage(int senderPort, int receiverPort, String retVal, int originalAsker) {
        super(MessageType.TELL_DELETE, senderPort, receiverPort);
        this.retVal = retVal;

        this.originalAsker = originalAsker;
    }

    public String getRetVal() {
        return retVal;
    }

    public int getOriginalAsker() {
        return originalAsker;
    }
}
