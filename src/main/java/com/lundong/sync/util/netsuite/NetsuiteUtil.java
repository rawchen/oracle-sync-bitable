package com.lundong.sync.util.netsuite;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author shuangquan.chen
 * @date 2024-01-04 19:25
 */
@Slf4j
public class NetsuiteUtil {
    public static String request() {
        String param = "";
        DateTimeFormatter of = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime start = now.minusDays(1);
        String startTime = start.format(of);
//                String endTime = now.format(of);
        param = "{\"startDate\":\"" + startTime + "\",\"endDate\":\"" + startTime + "\"}";
        // 查询数据，组织为实体列表
        String url = "https://xxxxx.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=442&deploy=1";
        String realm = "xxxx";
        String method = "POST";
        String access_token = "b0fe9bd0ac1151a3770d13d2bbf760a48c2221de772347b404xxxxxxxxxxxxxx";
        String token_secret = "8eed197b07c76869e3c51ec629bc06004743750e1b3a262fad1xxxxxxxxxxxxx";
        String consumer_key = "98415986bc8051205491cd2bbb2284e9ab5dc324b59d9eb08adxxxxxxxxxxxxx";
        String consumer_secret = "86330101a0a7959b7d0d53e1da75c3e7a1c975108ac1e8a82xxxxxxxxxxxx";
        AuthInfo authInfo = new AuthInfo(realm, url, access_token, token_secret, consumer_key, consumer_secret);
        String result = "";
        JSONObject orderObject = null;
        String exceptionMessage = "";
        for (int i = 0; i < 3; i++) {
            try {
                result = HttpRequest.requestURL(authInfo, param, method, null);
                orderObject = JSONObject.parseObject(result);
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
                log.error("请求ORACLE数据接口异常：", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ecp) {
                    log.error("sleep异常", ecp);
                }
            }
            if (!StrUtil.isEmpty(result) && orderObject != null) {
                break;
            }
        }
        if (!StrUtil.isEmpty(result) && orderObject != null) {
            if ("Success".equals(orderObject.getString("ask"))) {
                return result;
            } else {
                FeishuUtil.sendMsg(Constants.CHAT_ID, null, "请求ORACLE数据接口异常：" + result);
            }
        } else {
            FeishuUtil.sendMsg(Constants.CHAT_ID, null, "请求ORACLE数据接口异常：" + exceptionMessage);
        }
        return "";
    }
}
