package com.smart.jms.demo.ch07.transaction;


import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JMSReceiver implements MessageListener {
    private QueueConnection connection;
    private QueueSession session;
    private QueueReceiver receiver;

    public JMSReceiver() {
        try {
            Context context = new JndiFactory().getJndiContext();

            QueueConnectionFactory factory = (QueueConnectionFactory) context.lookup("QueueCF");
            connection = factory.createQueueConnection();

            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            connection.start();

            Queue queue = (Queue) context.lookup("queueTransacted");
            receiver = session.createReceiver(queue);
            receiver.setMessageListener(this);


        } catch (NamingException | JMSException e) {
            e.printStackTrace();
        }
    }

    private void close(){
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            System.out.println(textMessage.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JMSReceiver receiver = new JMSReceiver();

        try {
            // 持续运行，直到按下确认键为止
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("JMSReceiver application started");
            System.out.println("Press enter to quit application");
            bufferedReader.readLine();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
