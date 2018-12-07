package com.smart.jms.demo.ch05.pubsub;

import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 发布利率
 */
@SuppressWarnings("WeakerAccess")
public class TLender {
    private TopicConnection topicConnection = null;
    private TopicSession topicSession = null;
    private Topic topic = null;

    public TLender(String topicCf, String topicName) {
        try {
            Context context = new JndiFactory().getJndiContext();
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) context.lookup(topicCf);
            topicConnection = topicConnectionFactory.createTopicConnection();

            topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            topic = (Topic) context.lookup(topicName);

            topicConnection.start();
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    private void publishRate(double newRate) {
        try {
            BytesMessage bytesMessage = topicSession.createBytesMessage();
            bytesMessage.writeDouble(newRate);

            TopicPublisher publisher = topicSession.createPublisher(topic);
            publisher.publish(bytesMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void exit() {
        try {
            topicConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        String topicCf = null;
        String topicName = null;
        if (2 == args.length) {
            topicCf = args[0];
            topicName = args[1];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java TLender factory topic");
            System.exit(0);
        }

        TLender lender = new TLender(topicCf, topicName);

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("TLender Application Started");
            System.out.println("Press enter to exit application");
            System.out.println("Enter: Rate");
            System.out.println("\ne.g. 6.8");

            while (true) {
                System.out.println(">");
                String rate = bufferedReader.readLine();
                if (null == rate || 0 >= rate.trim().length()) {
                    lender.exit();
                    return;
                }

                double newRate = Double.valueOf(rate);
                lender.publishRate(newRate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
