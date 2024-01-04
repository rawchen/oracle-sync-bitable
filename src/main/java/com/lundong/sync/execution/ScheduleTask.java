package com.lundong.sync.execution;

import cn.hutool.core.util.StrUtil;
import com.lundong.sync.config.Constants;
import com.lundong.sync.util.FileUtil;
import com.lundong.sync.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Spring Boot定时任务
 *
 * @author RawChen
 * @date 2023-12-03 17:21
 */
@Slf4j
@Component
@EnableScheduling
public class ScheduleTask {

    /**
     * 启动延迟4秒执行一次 && 每1天执行一次
     * 判断是否为新的季度，是的话初始化一个TABLE出来
     */
    @Scheduled(initialDelay = 4000, fixedRate = 24 * 60 * 60 * 1000)
//	@Scheduled(fixedRate = 30 * 1000)
    private void scheduleTask() {
        // 初始化到内存
        // 如果当前是1月1日 4月1日 7月1日 10月1日 就是新的一个季度
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

//        int month = 1;
//        int day = 1;

        int quarter = (month - 1) / 3 + 1;
        if ((month == 1 || month == 4 || month == 7 || month == 10) && (day == 1)) {
            log.info("每天执行，当前是否为新的季度：是");
            String tableIdOrder = SignUtil.createTable(Constants.APP_TOKEN, year + "年Q" + quarter + "订单商品表");
            if (!StrUtil.isEmpty(tableIdOrder)) {
                FileUtil.stringToTextFile(tableIdOrder);
            }
        } else {
            log.info("每天执行，当前是否为新的季度：否");
        }
    }

    /**
     * 每隔10分钟刷新一个token
     */
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 10 * 60 * 1000)
    private void scheduleRefreshToken() {
        log.info("重新获得一个tenant_access_token");
        String accessToken = SignUtil.getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU);
        if (!StrUtil.isEmpty(accessToken)) {
            Constants.ACCESS_TOKEN = accessToken;
        }
    }
}
