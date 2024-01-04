package com.lundong.sync;

import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.Order;
import com.lundong.sync.util.SignUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void t01() {
        System.out.println(SignUtil.getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU));
    }

    @Test
    void t02() {
        SignUtil.findBaseList(Constants.ACCESS_TOKEN, "NVkybCNQPaEpDksjHdMccX4YnWd", "tblvznNPbCdy8oNU", Order.class);
    }

    @Test
    void t03() {
        String tableId = SignUtil.createTable("NVkybCNQPaEpDksjHdMccX4YnWd", "数据表名称2");
        System.out.println(tableId);
    }

    @Test
    void t04() {
        List<String> views = SignUtil.views("IAvCbNkcdaRIGmsBxR8cHxV4nBf", "tblMeDPGMqofalFO");
        for (String view : views) {
            System.out.println(view);
        }
    }
}
