package com.smart.jms.demo.ch02.chat;

import com.smart.jndi.JndiFactory;

import javax.jms.*;
import javax.naming.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class Chat implements MessageListener {

    private TopicSession pubSession;
    private TopicPublisher publisher;
    private TopicConnection connection;
    private String username;

    private static Properties props;

    public Chat(String topicFactory, String topicName, String username) throws Exception {

        // 使用 jndi.properties 文件获取一个 JNDI 连接
        JndiFactory jndiFactory = new JndiFactory();
        Context context = jndiFactory.getJndiContext();

        TopicConnectionFactory connectionFactory =
                (TopicConnectionFactory) context.lookup(topicFactory);
        TopicConnection connection = connectionFactory.createTopicConnection();

        // 创建两个 pubSession 对象
        TopicSession pubSession = connection.createTopicSession(false,
                Session.AUTO_ACKNOWLEDGE);
        TopicSession subSession = connection.createTopicSession(false,
                Session.AUTO_ACKNOWLEDGE);

        Topic chatTopic = (Topic) context.lookup(topicName);

        // 创建一个 jms 发布者和订阅者
        // createSubscriber 中附加的参数是一个消息选择器（null）和 noLocal 标志的一个真值
        // 它表明这个发布者生产的消息不应该被自己消费
        TopicPublisher publisher = pubSession.createPublisher(chatTopic);
        TopicSubscriber subscriber = subSession.createSubscriber(chatTopic, null, true);

        // 设置一个 jms 消息监听器
        subscriber.setMessageListener(this);

        this.connection = connection;
        this.pubSession = pubSession;
        this.publisher = publisher;
        this.username = username;

        // 启动 jms 连接，允许传送消息
        connection.start();
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

    protected void writeMessage(String text) throws JMSException {
        TextMessage textMessage = pubSession.createTextMessage();
        textMessage.setText(String.format("%s说：%s", this.username, text));
        publisher.publish(textMessage);
    }

    public void close() throws JMSException {
        connection.close();
    }

    public static void main(String[] args) {
        try {
            if (3 != args.length) {
                System.out.println("Factory, Topic, or username missing");
                return;
            }

            Chat chat = new Chat(args[0], args[1], args[2]);

            BufferedReader commandLine = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String msg = commandLine.readLine();
                if ("exit".equalsIgnoreCase(msg)) {
                    chat.close();
                    System.exit(0);
                } else {
                    chat.writeMessage(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
