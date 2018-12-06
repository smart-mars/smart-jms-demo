package com.smart.jms.demo.ch04.p2p;

import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class QBorrower {
    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private Queue responseQueue;
    private Queue requestQueue;

    public QBorrower(String queuecf, String requestQueue, String responseQueue) {
        try {
            Context context = new JndiFactory().getJndiContext();
            QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) context.lookup(queuecf);
            queueConnection = queueConnectionFactory.createQueueConnection();

            queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // 查找请求和应答队列
            this.requestQueue = (Queue) context.lookup(requestQueue);
            this.responseQueue = (Queue) context.lookup(responseQueue);

            queueConnection.start();
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private void sendLoanRequest(double salary, double loanAmt) {
        try {
            // 创建 JMS 消息
            MapMessage msg = queueSession.createMapMessage();
            msg.setDouble("salary", salary);
            msg.setDouble("loanAmount", loanAmt);
            msg.setJMSReplyTo(responseQueue);

            // 创建发送者并发送消息
            QueueSender queueSender = queueSession.createSender(requestQueue);
            queueSender.send(msg);

            // 等待查看贷款申请被接收或拒绝
            String filter = String.format("JMSCorrelationID = '%s'", msg.getJMSMessageID());
            QueueReceiver queueReceiver = queueSession.createReceiver(responseQueue, filter);
            TextMessage textMessage = (TextMessage) queueReceiver.receive(30000);
            if (null == textMessage) {
                System.out.println("QLender not responding");
            } else {
                System.out.println(String.format("Loan request was %s", textMessage.getText()));
            }
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
        System.exit(0);
    }

    public static void main(String[] args) {
        String queuecf = null;
        String requestQueue = null;
        String responseQueue = null;
        if (3 == args.length) {
            queuecf = args[0];
            requestQueue = args[1];
            responseQueue = args[2];
        } else {
            System.out.println("Invalid arguments. Should be: ");
            System.out.println("java QBorrower factory requestQueue responseQueue");
            System.exit(0);
        }

        QBorrower qBorrower = new QBorrower(queuecf, requestQueue, responseQueue);

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("QBorrower Application Started");
            System.out.println("Press enter to quit application");
            System.out.println("Enter: salary, loan_amount");
            System.out.println("\ne.g. 50000, 120000");

            while (true) {
                System.out.println("> ");
                String loanRequest = bufferedReader.readLine();
                if (null == loanRequest || loanRequest.trim().length() <= 0) {
                    qBorrower.exit();
                    return;
                }

                // 解析交易说明
                StringTokenizer stringTokenizer = new StringTokenizer(loanRequest, ",");
                double salary = Double.valueOf(stringTokenizer.nextToken().trim());
                double loanAmt = Double.valueOf(stringTokenizer.nextToken().trim());
                qBorrower.sendLoanRequest(salary, loanAmt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
