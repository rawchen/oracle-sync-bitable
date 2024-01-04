package com.lundong.sync.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author RawChen
 * @date 2023-06-25 14:02
 */
@EnableConfigurationProperties
public class Constants {

    // 同步多维表格ID
    public static final String APP_TOKEN = "IAvCbNkcdaRIGmsBxRxxxxxxxx";

    // 同步多维表格表单ID
    public static final String TABLE_ID_ORDER = "";

    // 飞书自建应用 App ID
    public final static String APP_ID_FEISHU = "cli_a50fd5f0xxxxxx";

    // 飞书自建应用 App Secret
    public final static String APP_SECRET_FEISHU = "Y5ppj47vHTbpYoKjrY6xxxxxxxxxx";

    public static String ACCESS_TOKEN = "";

}
