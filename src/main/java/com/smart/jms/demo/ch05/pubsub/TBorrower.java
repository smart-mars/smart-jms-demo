package com.smart.jms.demo.ch05.pubsub;

import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 订阅利率
 */
@SuppressWarnings("WeakerAccess")
public class TBorrower implements MessageListener {
    private TopicConnection topicConnection = null;
    private double currentRate;

    public TBorrower(String topicCf, String topicName, String rate) {
        try {
            currentRate = Double.valueOf(rate);

            Context context = new JndiFactory().getJndiContext();
            TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) context.lookup(topicCf);
            topicConnection = topicConnectionFactory.createTopicConnection();

            TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = (Topic) context.lookup(topicName);

            TopicSubscriber subscriber = topicSession.createSubscriber(topic);
            subscriber.setMessageListener(this);

            topicConnection.start();

            System.out.println("Waiting for loan requests...");
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            BytesMessage bytesMessage = (BytesMessage) message;
            double newRate = bytesMessage.readDouble();

            if ((currentRate - newRate) >= 1.0) {
                System.out.println(String.format("New rate = %f - Consider refinancing loan", newRate));
            } else {
                System.out.println(String.format("New rate = %f - Keep existing loan", newRate));
            }

            System.out.println("\nWaiting for rate updates...");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void exit(){
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
        String rate = null;
        if (3 == args.length) {
            topicCf = args[0];
            topicName = args[1];
            rate = args[2];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java TBorrower factory topic rate");
            System.exit(0);
        }

        TBorrower borrower = new TBorrower(topicCf, topicName, rate);

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("TBorrower Application Started");
            System.out.println("Press enter to quit application");
            bufferedReader.readLine();
            borrower.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
