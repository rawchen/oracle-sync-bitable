package com.lundong.sync.util.netsuite;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shuangquan.chen
 * @date 2024-01-18 17:19
 */
@Slf4j
public class FeishuUtil {
    /**
     * 发送消息
     *
     * @param accessToken
     * @param chatId
     * @param userId
     * @param content
     * @return
     */
    public static void sendMsg(String accessToken, String chatId, String userId, String content) {
        if (chatId != null && !"".equals(chatId)) {
            // 发送群消息
            JSONObject bodyObject = new JSONObject();
            bodyObject.put("receive_id", chatId);
            bodyObject.put("msg_type", "text");
            bodyObject.put("content", "{\"text\":\"" + escape(content) + "\"}");
            String resultStr = cn.hutool.http.HttpRequest.post("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=chat_id")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(bodyObject.toJSONString())
                    .execute()
                    .body();
            JSONObject jsonObject = JSON.parseObject(resultStr);
            if (jsonObject != null && jsonObject.getInteger("code") == 0) {
                log.info("群消息发送成功：" + content);
            } else {
                log.error("群消息发送接口调用失败: {}", resultStr);
            }
        }

        if (userId != null && !"".equals(userId)) {
            // 发送用户消息
            JSONObject bodyObject = new JSONObject();
            bodyObject.put("receive_id", userId);
            bodyObject.put("msg_type", "text");
            bodyObject.put("content", "{\"text\":\"" + escape(content) + "\"}");
            String resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=user_id")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(bodyObject.toJSONString())
                    .execute()
                    .body();
            JSONObject jsonObject = JSON.parseObject(resultStr);
            if (jsonObject != null && jsonObject.getInteger("code") == 0) {
                log.info("用户消息发送成功：" + content);
            } else {
                log.error("用户消息接口调用失败: {}", resultStr);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param chatId
     * @param userId
     * @param content
     */
    public static void sendMsg(String chatId, String userId, String content) {
        sendMsg(Constants.ACCESS_TOKEN, chatId, userId, content);
    }

    public static String escape(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '	':
                    escaped.append("    ");
                    break;
                case '\r':
                case '\n':
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
