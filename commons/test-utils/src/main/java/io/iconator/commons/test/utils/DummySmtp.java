package io.iconator.commons.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DummySmtp {

    private static Wiser wiser;

    private static final Logger LOG = LoggerFactory.getLogger(DummySmtp.class);

    public static void start() throws Exception {
        if(wiser == null) {
            wiser = new Wiser();
            wiser.setPort(2525); // Default is 25
            wiser.start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            for (WiserMessage message : wiser.getMessages()) {
                                String envelopeSender = message.getEnvelopeSender();
                                String envelopeReceiver = message.getEnvelopeReceiver();

                                MimeMessage mess = message.getMimeMessage();
                                System.out.println("START EMAIL******************************************************");
                                System.out.println("Sender:"+mess.getSender());
                                if(mess.getFrom() != null) {
                                    for (Address from : mess.getFrom()) {
                                        System.out.println("From:" + from);
                                    }
                                }
                                System.out.println("Content:");
                                //System.out.println(mess.getContent());
                                //System.out.println(mess.toString());
                                Path tmp = Files.createTempFile("",".eml");
                                FileOutputStream fos = new FileOutputStream(tmp.toFile());
                                mess.writeTo(fos);
                                fos.close();
                                System.out.println("Mail written to: file:"+tmp.toString());
                                System.out.println("STOP EMAIL ******************************************************");
                                // now do something fun!
                            }
                            wiser.getMessages().clear();
                            Thread.sleep(2000);
                        } catch (Throwable t) {
                            LOG.error("error in local smtp", t);
                        }
                    }
                }
            }).start();
        }
    }
}
