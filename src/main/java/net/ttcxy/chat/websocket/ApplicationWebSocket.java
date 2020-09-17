package net.ttcxy.chat.websocket;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import net.ttcxy.chat.entity.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.xml.ws.soap.Addressing;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jackcooper
 * @create 2017-12-28 13:04
 */
@ServerEndpoint(value = "/websocket")
@Component
public class ApplicationWebSocket{

    //静态变量，用来记录当前在线连接数。设计成线程安全的。
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    //concurrent包的线程安全Set，用来存放每个客户端对应的ApplicationWebSocket对象。
    private static CopyOnWriteArraySet<ApplicationWebSocket> webSocketSet = new CopyOnWriteArraySet<ApplicationWebSocket>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    private static List<String> messageList = new ArrayList<>();

    private static Map<String,String> usernameMap = new HashMap<>();

    @Autowired
    HttpServletRequest httpServletRequest;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {

        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        try {
            final MessageDto messageDto = new MessageDto(MessageDto.USER_COUNT,"系统消息", "2020.06.15",  ""+getOnlineCount());
            final String string = JSON.toJSONString(messageDto);
            sendInfo(string);
            for (String str : messageList){
                sendMessage(str);
            }
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }



    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param text 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String text, Session session) {

        final JSONObject json = JSON.parseObject(text);
        final String type = json.getString("type");
        if ("message".equals(type)){
            final String username = usernameMap.get(session.getId());
            if (StrUtil.isBlank(username)){
                return;
            }
            final String jsonString = JSON.toJSONString(new MessageDto(MessageDto.MESSAGE,username, DateUtil.format(new Date(), "yyyy.MM.dd"), json.getString("message")));
            if (messageList.size()>20){
                synchronized (messageList){
                    messageList.remove(0);
                    messageList.add(jsonString);
                }
            }else{
                messageList.add(jsonString);
            }
            //群发消息
            for (ApplicationWebSocket item : webSocketSet) {
                try {
                    item.sendMessage(jsonString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if ("setting".equals(type)){
            usernameMap.put(session.getId(),json.getString("username"));
        }

    }

    /**
     * 发生错误时调用
     @OnError
     */
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }


    public void sendMessage(String jsonText) throws IOException {
        //getBasicRemote是阻塞式的
        this.session.getBasicRemote().sendText(jsonText);
        //非阻塞式的
        // this.session.getAsyncRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message) throws IOException {
        for (ApplicationWebSocket item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        ApplicationWebSocket.onlineCount.getAndIncrement();
    }

    public static synchronized void subOnlineCount() {
        ApplicationWebSocket.onlineCount.getAndDecrement();
    }
}
