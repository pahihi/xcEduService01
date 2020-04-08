package com.xuecheng.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author whj
 * @createTime 2020-02-23 18:12
 * @description 消费者
 **/
public class Consumer07_topic {

    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String EXCHANGE_ROUTING_INFORM = "exchange_topic_inform";

    public static void main(String[] args) {
        //通过连接工厂连接mq
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机
        connectionFactory.setVirtualHost("/");
        Connection connection = null;

        try {
            //建立连接
            connection = connectionFactory.newConnection();
            //建立会话通道
            Channel channel = connection.createChannel();

            /**
             * 声明交换机
             * 1、交换机名称
             * 2、交换机类型，fanout、topic、direct、headers
             */
            channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.TOPIC);
            //声明队列
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);

            /**
             * 交换机和队列绑定
             * 1、队列名称
             * 2、交换机名称
             * 3、路由key
             */
            channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_ROUTING_INFORM, "inform.#.email.#");
            //实现消费方法
            DefaultConsumer consumer = new DefaultConsumer(channel) {
                /**
                 * 但消息接收到后，此方法将被调用
                 * @param consumerTag 消费者标签用来表示消费者，在监听队列时设置channel.basicConsume
                 * @param envelope 信封，通过envelope
                 * @param properties 消息属性
                 * @param body 消息内容
                 * @throws IOException
                 */
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties
                        ,byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id,在同道中标识消息的id,可用于确认消息已接收；
                    long deliveryTag = envelope.getDeliveryTag();
                    String s = new String(body, StandardCharsets.UTF_8);
                    System.out.println("receive message:" + s);
                }
            };
            /**
             * 监听队列
             * 1、队列名称
             * 2、是否自动回复，设置为true为表示消息接收到自动向mq回复接收到了，mq接收到回复会删除消息，设置为false则需要手动回复
             * 3、消费消息的方法，消费者接收到消息后调用此方法
             */
            channel.basicConsume(QUEUE_INFORM_EMAIL, true, consumer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
