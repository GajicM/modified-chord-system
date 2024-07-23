package servent.message;

public class AskViewMessage extends BasicMessage{
    private String addressPort;
    private final int originalSender;
    public AskViewMessage(int senderPort, int receiverPort, String addressPort, int originalSender) {
        super(MessageType.ASK_VIEW, senderPort, receiverPort);
        this.addressPort=addressPort;
        this.originalSender = originalSender;
    }

    public String getAddressPort() {
        return addressPort;
    }

    public int getOriginalSender() {
        return originalSender;
    }
}
