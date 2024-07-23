package app;

import servent.message.PingMessage;
import servent.message.SusMessage;
import servent.message.util.MessageUtil;

import java.util.concurrent.atomic.AtomicBoolean;

public class FaultDetection implements Runnable{
    public static AtomicBoolean running=new AtomicBoolean(true);
    public static final Object deleteLock=new Object();
    @Override
    public void run() {
        while(running.get()){
            //pinguje prvom sledecem, ako ne dobije poruku, pitace prvog sledeceg posle sledeceg.
            if(AppConfig.chordState.getSuccessorTable()[0]!=null ){
                PingMessage pingMessage=new PingMessage(AppConfig.myServentInfo.getListenerPort(),AppConfig.chordState.getNextNodePort());
                MessageUtil.sendMessage(pingMessage);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(!running.get())
                    break;
                if(!AppConfig.gotPongFromSuccessor){
                    //pitaj succesor[1]
                    if(AppConfig.chordState.getSuccessorTable()[1]!=null){
                      //posalji neki sus message succesor[1]
                        SusMessage susMessage=new SusMessage(AppConfig.myServentInfo.getListenerPort(),AppConfig.chordState.getSuccessorTable()[1].getListenerPort());
                        MessageUtil.sendMessage(susMessage);
                        AppConfig.timestampedStandardPrint("SENDING SUS MESSAGE TO SUCCESSOR[1] FOR MY SUCCESSOR");
                    }
                    MessageUtil.sendMessage(pingMessage);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if(!running.get())
                        break;
                    //ako i dalje nismo dobili pong iskljucujemo ga
                    if(!AppConfig.gotPongFromSuccessor){
                         //

                        AppConfig.timestampedStandardPrint("Didnt get pong from "+AppConfig.chordState.getSuccessorTable()[0]);
                        AppConfig.chordState.removeNode(AppConfig.chordState.getSuccessorTable()[0]);

                        AppConfig.timestampedStandardPrint("My valueMap is now "+ AppConfig.chordState.getValueMap());

                        }



                    }
                    AppConfig.gotPongFromSuccessor=false;
                }

            }





    }
}
