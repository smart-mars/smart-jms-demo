package com.smart.jms.demo.ch07.transaction;


import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;

public class JMSSenderTransacted {
    private QueueConnection connection;
    private QueueSession session;
    private QueueSender sender;

    public JMSSenderTransacted() {
        try {
            Context context = new JndiFactory().getJndiContext();
            QueueConnectionFactory factory = (QueueConnectionFactory) context.lookup("QueueCF");
            connection = factory.createQueueConnection();

            session = connection.createQueueSession(true, Session.SESSION_TRANSACTED);

            Queue queue = (Queue) context.lookup("queueTransacted");
            sender = session.createSender(queue);

            connection.start();
        } catch (JMSException | NamingException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage() {
        try {
            // 在一个事务中发送多条消息...
            System.out.println(String.format("Session Transacted: %s", session.getTransacted()));
            System.out.println(String.format("消息确认模式：%s", session.getAcknowledgeMode()));
            sendMessage("第一条消息");
            sendMessage("第二条消息");
            sendMessage("第三条消息");
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                System.out.println("Exception caught, rolling back session");
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendMessage(String text) throws Exception{
        TextMessage message = session.createTextMessage();
        message.setText(text);
        sender.send(message);
    }

    public static void main(String[] args) {
        JMSSenderTransacted app = new JMSSenderTransacted();
        app.sendMessage();
        System.exit(0);
    }
}
