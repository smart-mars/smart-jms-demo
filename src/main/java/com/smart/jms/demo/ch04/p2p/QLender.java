package com.smart.jms.demo.ch04.p2p;


import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class QLender implements MessageListener {
    private QueueConnection queueConnection = null;
    private QueueSession queueSession = null;
    private Queue requestQueue = null;

    public QLender(String queuecf, String requestQueue) {
        try {
            Context context = new JndiFactory().getJndiContext();
            QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) context.lookup(queuecf);
            queueConnection = queueConnectionFactory.createQueueConnection();

            // 创建 JMS 会话
            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // 查找申请队列
            this.requestQueue = (Queue) context.lookup(requestQueue);

            queueConnection.start();

            // 创建消息监听器
            QueueReceiver queueReceiver = queueSession.createReceiver(this.requestQueue);
            queueReceiver.setMessageListener(this);

            System.out.println("Waiting for loan requests...");
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            boolean accepted = false;

            // 从消息中获取数据
            MapMessage mapMessage = (MapMessage) message;
            double salary = mapMessage.getDouble("salary");
            double loanAmt = mapMessage.getDouble("loanAmount");

            if (loanAmt < 200000) {
                accepted = (salary / loanAmt) > .25;
            } else {
                accepted = (salary / loanAmt) > .33;
            }
            System.out.println(String.format("%% = %f, loan is %s", (salary / loanAmt), (accepted ? "Accepted" : "Declined")));

            // 将结果返回借方
            TextMessage textMessage = queueSession.createTextMessage();
            textMessage.setText(accepted ? "Accepted" : "Declined");
            textMessage.setJMSCorrelationID(message.getJMSMessageID());

            // 创建发送者并发送消息
            QueueSender queueSender = queueSession.createSender((Queue) message.getJMSReplyTo());
            queueSender.send(textMessage);

            System.out.println("\nWaiting for loan requests...");

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void exit() {
        try {
            queueConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String queuecf = null;
        String requestQueue = null;
        if (2 == args.length) {
            queuecf = args[0];
            requestQueue = args[1];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java QLender factory request_queue");
            System.exit(0);
        }

        QLender lender = new QLender(queuecf, requestQueue);

        try {
            // 持续运行，直到按下确认键为止
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("QLender application started");
            System.out.println("Press enter to quit application");
            bufferedReader.readLine();
            lender.exit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
