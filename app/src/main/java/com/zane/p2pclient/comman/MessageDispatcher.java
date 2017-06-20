package com.zane.p2pclient.comman;

import android.util.Log;

import com.zane.p2pclient.comman.parse.AbstractParseMan;

import java.io.IOException;

/**
 * 分发Message到责任链中去
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class MessageDispatcher extends Thread{
    private AbstractParseMan headParser;
    private MessageQueue messageQueue;

    public MessageDispatcher(AbstractParseMan headParser) {
        super("MessageDispatcher");
        this.headParser = headParser;
        this.messageQueue = MessageQueue.getInstance();
    }

    public void finish() {
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            Message message = null;
            try {
                message = messageQueue.take();
                int tryTime = message.getTryTime();
                if (tryTime < 5) {
                    message.setTryTime(++tryTime);
                    if ("send".equals(message.getType())) {
                        headParser.send(message);
                    } else if ("receive".equals(message.getType())) {
                        headParser.receive(message);
                    } else {
                        Log.i("MessageDispatcher", "Message without type!");
                    }
                } else {
                    Log.i("MessageDispatcher", "Failed to send message in 5 times: " + message.toString());
                }
            } catch (InterruptedException e) {
                interrupt();
            } catch (IOException e) {
                messageQueue.put(message);
            }
        }
    }
}