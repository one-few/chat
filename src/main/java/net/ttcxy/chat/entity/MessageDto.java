package net.ttcxy.chat.entity;

/**
 * created by huanglei on 2020/6/15
 */
public class MessageDto {

    public static final String MESSAGE = "message";
    public static final String USER_COUNT = "userCount";


    private String type;

    /**
     * 发送人名
     */
    private String sendName;

    /**
     * 消息时间
     */
    private String createDate;

    /**
     * 内容
     */
    private String text;

    public MessageDto(String type ,String sendName, String createDate, String text) {
        this.type = type;
        this.sendName = sendName;
        this.createDate = createDate;
        this.text = text;
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
