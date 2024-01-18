package com.lundong.sync.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.ApprovalInstance;
import com.lundong.sync.entity.BitableParam;
import com.lundong.sync.entity.feishu.FeishuUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author RawChen
 * @date 2023-06-25 14:33
 */
@Slf4j
public class SignUtil {

    /**
     * 飞书自建应用获取tenant_access_token
     */
    public static String getAccessToken(String appId, String appSecret) {

        JSONObject object = new JSONObject();
        object.put("app_id", appId);
        object.put("app_secret", appSecret);
        String resultStr = "";
        JSONObject resultObject = null;
        for (int i = 0; i < 3; i++) {
            try {
                HttpResponse execute = HttpRequest.post("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
                        .form(object)
                        .execute();
                resultStr = execute.body();
                execute.close();
                if (StringUtils.isNotEmpty(resultStr)) {
                    resultObject = JSON.parseObject(resultStr);
                    if (resultObject.getInteger("code") != 0) {
                        log.error("获取tenant_access_token失败，重试 {} 次, body: {}", i + 1, resultStr);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ecp) {
                            log.error("sleep异常", ecp);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取tenant_access_token异常，重试 {} 次, message: {}, body: {}", i + 1, e.getMessage(), resultStr);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ecp) {
                    log.error("sleep异常", ecp);
                }
            }
            if (resultObject != null && resultObject.getInteger("code") == 0) {
                break;
            }
        }
        // 重试完检测
        if (resultObject == null || resultObject.getInteger("code") != 0) {
            log.error("重试3次获取tenant_access_token后都失败");
            return "";
        } else {
            String tenantAccessToken = resultObject.getString("tenant_access_token");
            if (tenantAccessToken != null) {
                return tenantAccessToken;
            }
        }
        log.error("access_token获取不成功: {}", resultStr);
        return "";
    }

    /**
     * 获取飞书用户姓名
     *
     * @param accessToken
     * @return
     */
    public static String getFeishuUserName(String accessToken, String userId) {

        Map<String, Object> param = new HashMap<>();
        param.put("user_id_type", "user_id");
        param.put("department_id_type", "department_id");
        FeishuUser feishuUser = new FeishuUser();
        try {
            String resultStr = HttpRequest.get("https://open.feishu.cn/open-apis/contact/v3/users/" + userId)
                    .header("Authorization", "Bearer " + accessToken)
                    .form(param)
                    .execute()
                    .body();
            log.info("获取飞书用户姓名接口: {}", resultStr);
            JSONObject jsonObject = JSONObject.parseObject(resultStr);
            if (jsonObject.getInteger("code") == 0) {
                JSONObject user = jsonObject.getJSONObject("data").getJSONObject("user");
                if (user != null) {
                    feishuUser.setUserId(user.getString("user_id"));
                    feishuUser.setName(user.getString("name"));
                }
                return feishuUser.getName();
            } else {
                log.error("获取飞书用户姓名接口失败: {}", resultStr);
                return "";
            }
        } catch (Exception e) {
            log.error("获取飞书用户姓名接口异常: ", e);
            return "";
        }
    }

    /**
     * 获取飞书用户
     *
     * @return
     */
    public static String getFeishuUserName(String userId) {
        return getFeishuUserName(Constants.ACCESS_TOKEN, userId);
    }

    /**
     * 获取单个审批实例详情
     *
     * @param accessToken
     * @param instanceId
     * @return
     */
    public static ApprovalInstance approvalInstanceDetail(String accessToken, String instanceId) {
        try {
            JSONObject object = new JSONObject();
            object.put("user_id_type", "user_id");
            String resultStr = HttpRequest.get("https://open.feishu.cn/open-apis/approval/v4/instances/" + instanceId)
                    .header("Authorization", "Bearer " + accessToken)
                    .form(object)
                    .execute().body();
            log.info("获取单个审批实例详情接口: {}", resultStr);
            if (StringUtils.isNotEmpty(resultStr)) {
                JSONObject resultObject = JSON.parseObject(resultStr);
                if (resultObject.getInteger("code") == 0) {
                    return resultObject.getJSONObject("data").toJavaObject(ApprovalInstance.class);
                } else {
                    log.error("获取单个审批实例详情接口出错: {}", resultStr);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("获取单个审批实例详情异常: ", e);
            return null;
        }
        return null;
    }

    /**
     * 获取单个审批实例详情
     *
     * @param instanceId
     * @return
     */
    public static ApprovalInstance approvalInstanceDetail(String instanceId) {
        return approvalInstanceDetail(Constants.ACCESS_TOKEN, instanceId);
    }

    /**
     * 列出记录
     *
     * @param accessToken
     * @param appToken
     * @param tableId
     * @return
     */
    public static <T> List<T> findBaseList(String accessToken, String appToken, String tableId, Class<T> tClass) {
        List<T> results = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();
        param.put("page_size", 500);
        boolean hasMore = true;

        while (hasMore) {
            JSONObject jsonObject = null;
            String resultStr = "";
            for (int i = 0; i < 3; i++) {
                try {
                    HttpResponse response = HttpRequest.get("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/records")
                            .header("Authorization", "Bearer " + accessToken)
                            .form(param)
                            .execute();
                    resultStr = response.body();
                    response.close();
                    System.out.println(resultStr);
                    log.info("列出记录接口: {}", resultStr.length() > 100 ? resultStr.substring(0, 100) + "..." : resultStr);
                    //                Thread.sleep(2000L);
                    jsonObject = JSON.parseObject(resultStr);
                } catch (Exception e) {
                    log.error("接口请求失败，重试 {} 次, message: {}, body: {}", i + 1, e.getMessage(), resultStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ecp) {
                        log.error("sleep异常", ecp);
                    }
                }
                if (jsonObject != null && jsonObject.getInteger("code") != 0) {
                    // access_token过期
                    if (jsonObject.getInteger("code") == 99991663) {
                        Constants.ACCESS_TOKEN = SignUtil.getAccessToken(Constants.APP_ID_FEISHU, Constants.APP_SECRET_FEISHU);
                        accessToken = Constants.ACCESS_TOKEN;
                    }
                    log.error("接口请求失败，重试 {} 次, body: {}", i + 1, resultStr);
                } else if (jsonObject != null && jsonObject.getInteger("code") == 0) {
                    break;
                }
            }
            if (jsonObject == null || jsonObject.getInteger("code") != 0) {
                log.error("列出记录接口调用失败");
                return Collections.emptyList();
            }
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray items = data.getJSONArray("items");
            for (int i = 0; i < items.size(); i++) {
                JSONObject records = items.getJSONObject(i).getJSONObject("fields");
                T testEntity;
                testEntity = JSONObject.toJavaObject(records, tClass);
                StringUtil.bracketReplace(testEntity);
                StringUtil.clearSpecialSymbols(testEntity);
                results.add(testEntity);
            }
            if ((boolean) data.get("has_more")) {
                param.put("page_token", data.getString("page_token"));
            } else {
                hasMore = false;
            }
        }
        return results;
    }

    /**
     * 获取多维表列表
     *
     * @param appToken
     * @param tableId
     * @return
     */
    public static <T> List<T> findBaseList(String appToken, String tableId, Class<T> tClass) {
        return findBaseList(Constants.ACCESS_TOKEN, appToken, tableId, tClass);
    }

    /**
     * 列出记录
     *
     * @param accessToken
     * @param bitableParam
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T> T findBaseRecord(String accessToken, BitableParam bitableParam, Class<T> tClass) {
        T result;
        try {
            String resultStr = HttpRequest.get("https://open.feishu.cn/open-apis/bitable/v1/apps/" +
                            bitableParam.getAppToken() +
                            "/tables/" + bitableParam.getTableId() + "/records/" + bitableParam.getRecordId())
                    .header("Authorization", "Bearer " + accessToken)
                    .execute()
                    .body();
            log.info("检索记录接口: {}", resultStr);
//            log.info("检索记录接口: {}", resultStr.length() > 100 ? resultStr.substring(0, 100) + "..." : resultStr);
            JSONObject jsonObject = JSON.parseObject(resultStr);
            if (jsonObject.getInteger("code") != 0) {
                log.error("检索记录接口调用失败");
                return null;
            }
            JSONObject fields = jsonObject.getJSONObject("data").getJSONObject("record").getJSONObject("fields");
            result = JSONObject.toJavaObject(fields, tClass);
            StringUtil.clearSpecialSymbols(result);
        } catch (Exception e) {
            log.info("检索记录接口调用异常", e);
            return null;
        }
        return result;
    }

    public static <T> T findBaseRecord(BitableParam bitableParam, Class<T> tClass) {
        return findBaseRecord(Constants.ACCESS_TOKEN, bitableParam, tClass);
    }

    /**
     * 多维表格新增多条记录
     *
     * @param json
     * @return
     */
    public static List<String> batchInsertRecord(String json, String appToken, String tableId) {
        return batchInsertRecord(Constants.ACCESS_TOKEN, json, appToken, tableId);
    }

    /**
     * 多维表格新增多条记录
     *
     * @param accessToken
     * @param json
     * @return
     */
    private static List<String> batchInsertRecord(String accessToken, String json, String appToken, String tableId) {
        List<String> recordIds = new ArrayList<>();
        String resultStr = "";
        JSONObject resultObject = null;
        for (int i = 0; i < 3; i++) {
            try {
                resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/records/batch_create")
                        .header("Authorization", "Bearer " + accessToken)
                        .body(json)
                        .execute()
                        .body();
                log.info("resultStr: {}", StringUtil.subLog(resultStr));
                if (StringUtils.isNotEmpty(resultStr)) {
                    resultObject = (JSONObject) JSON.parse(resultStr);
                    if (resultObject.getInteger("code") != 0) {
                        log.error("新增多条记录失败，重试 {} 次, body: {}", i + 1, resultStr);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ecp) {
                            log.error("sleep异常", ecp);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("新增多条记录接口调用异常，重试 {} 次, message: {}, body: {}", i + 1, e.getMessage(), resultStr);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ecp) {
                    log.error("sleep异常", ecp);
                }
            }
            if (resultObject != null && resultObject.getInteger("code") == 0) {
                break;
            }
        }
        if (resultObject == null || resultObject.getInteger("code") != 0) {
            log.error("重试3次新增多条记录接口调用后都失败");
            return Collections.emptyList();
        } else {
            JSONObject data = (JSONObject) resultObject.get("data");
            JSONArray records = (JSONArray) data.get("records");
            for (int j = 0; j < records.size(); j++) {
                JSONObject jsonObject = records.getJSONObject(j);
                String recordId = jsonObject.getString("record_id");
                recordIds.add(recordId);
            }
            return recordIds;
        }
    }

    public static String insertRecord(String str, String appToken, String tableId) {
        return insertRecord(Constants.ACCESS_TOKEN, str, appToken, tableId);
    }

    public static String insertRecord(String accessToken, String str, String appToken, String tableId) {
        String resultStr = "";
        JSONObject resultObject = null;
        for (int i = 0; i < 3; i++) {
            try {
                resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/records")
                        .header("Authorization", "Bearer " + accessToken)
                        .body(str)
                        .execute()
                        .body();
                log.info("resultStr: {}", StringUtil.subLog(resultStr));
                if (StringUtils.isNotEmpty(resultStr)) {
                    resultObject = (JSONObject) JSON.parse(resultStr);
                    if (resultObject.getInteger("code") != 0) {
                        log.error("新增记录失败，重试 {} 次, body: {}", i + 1, resultStr);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ecp) {
                            log.error("sleep异常", ecp);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("新增新增记录接口调用异常，重试 {} 次, message: {}, body: {}", i + 1, e.getMessage(), resultStr);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ecp) {
                    log.error("sleep异常", ecp);
                }
            }
            if (resultObject != null && resultObject.getInteger("code") == 0) {
                break;
            }
        }
        if (resultObject == null || resultObject.getInteger("code") != 0) {
            log.error("重试3次新增新增记录接口调用后都失败");
            return "";
        } else {
            // todo 是否记录今天插入成功，防止一天多次执行
            JSONObject data = (JSONObject) resultObject.get("data");
            JSONObject record = (JSONObject) data.get("record");
            return record.getString("record_id");
        }
    }

    public static String createTable(String appToken, String tableName) {
        return createTable(Constants.ACCESS_TOKEN, appToken, tableName);
    }

    public static String createTable(String accessToken, String appToken, String tableName) {
        String json = "{\n" +
                "    \"table\":{\n" +
                "        \"name\":\"" + tableName + "\",\n" +
                "        \"default_view_name\":\"默认的表格视图\",\n" +
                "        \"fields\":[\n" +
                "            {\n" +
                "              \"field_name\": \"NS单号\",\n" +
                "              \"is_primary\": true,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"平台单号\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"附属\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 3,\n" +
                "              \"ui_type\": \"SingleSelect\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"仓库\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 3,\n" +
                "              \"ui_type\": \"SingleSelect\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"日期\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 5,\n" +
                "              \"ui_type\": \"DateTime\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"运输费\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"formatter\": \"0.0000\"\n" +
                "              },\n" +
                "              \"type\": 2,\n" +
                "              \"ui_type\": \"Number\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"销售代表\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"账户\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 3,\n" +
                "              \"ui_type\": \"SingleSelect\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"市场\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"options\": [\n" +
                "                  {\n" +
                "                    \"color\": 0,\n" +
                "                    \"name\": \"Amazon\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"color\": 1,\n" +
                "                    \"name\": \"Rakuten\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"color\": 2,\n" +
                "                    \"name\": \"Walmart\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                    \"color\": 3,\n" +
                "                    \"name\": \"SHOPIFY\"\n" +
                "                  }\n" +
                "                ]\n" +
                "              },\n" +
                "              \"type\": 3,\n" +
                "              \"ui_type\": \"SingleSelect\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"交货日期\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"状态\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"币别\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 3,\n" +
                "              \"ui_type\": \"SingleSelect\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"汇率\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"formatter\": \"0.0000\"\n" +
                "              },\n" +
                "              \"type\": 2,\n" +
                "              \"ui_type\": \"Number\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品总数量\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"formatter\": \"0\"\n" +
                "              },\n" +
                "              \"type\": 2,\n" +
                "              \"ui_type\": \"Number\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"价格合计\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"formatter\": \"0.0000\"\n" +
                "              },\n" +
                "              \"type\": 2,\n" +
                "              \"ui_type\": \"Number\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品SKU\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "           {\n" +
                "              \"field_name\": \"商品名称\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品销售代表\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 3,\n" +
                "              \"ui_type\": \"SingleSelect\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品数量\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"formatter\": \"0\"\n" +
                "              },\n" +
                "              \"type\": 2,\n" +
                "              \"ui_type\": \"Number\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品价格\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": {\n" +
                "                \"formatter\": \"0.0000\"\n" +
                "              },\n" +
                "              \"type\": 2,\n" +
                "              \"ui_type\": \"Number\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品面料名称\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            },\n" +
                "            {\n" +
                "              \"field_name\": \"商品价格匹配代码\",\n" +
                "              \"is_primary\": false,\n" +
                "              \"property\": null,\n" +
                "              \"type\": 1,\n" +
                "              \"ui_type\": \"Text\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        String tableId = "";
        try {
            String resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(json)
                    .execute()
                    .body();
            log.info("新增一个数据表: {}", StringUtil.subLog(resultStr));
            if (StringUtils.isNotEmpty(resultStr)) {
                JSONObject resultObject = (JSONObject) JSON.parse(resultStr);
                if (resultObject.getInteger("code") != 0) {
                    log.info("新增一个数据表失败：{}", resultStr);
                    return "";
                } else {
                    JSONObject data = (JSONObject) resultObject.get("data");
                    tableId = data.getString("table_id");
                    return tableId;
                }
            }
        } catch (Exception e) {
            log.info("新增一个数据表异常", e);
            return "";
        }

        // return tableId;
//        if (StrUtil.isEmpty(tableId)) {
//            log.info("新增一个数据表失败，tableId为空");
//            return "";
//        }
//
//        String jsonField = "{\n" +
//                "    \"field_name\":\"父记录\",\n" +
//                "    \"ui_type\":\"SingleLink\",\n" +
//                "    \"type\":18,\n" +
//                "    \"property\": {\n" +
//                "        \"multiple\": false,\n" +
//                "        \"table_id\": \"" + tableId + "\"\n" +
//                "    }" +
//                "}";
//
//        try {
//            String resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/fields")
//                    .header("Authorization", "Bearer " + accessToken)
//                    .body(jsonField)
//                    .execute()
//                    .body();
//            log.info("resultStr: {}", StringUtil.subLog(resultStr));
//            if (StringUtils.isNotEmpty(resultStr)) {
//                JSONObject resultObject = (JSONObject) JSON.parse(resultStr);
//                if (resultObject.getInteger("code") != 0) {
//                    log.error("新增字段失败：{}", resultStr);
//                    tableId = "";
//                } else {
//                    return tableId;
//                }
//            }
//        } catch (Exception e) {
//            log.info("新增字段异常", e);
//            tableId = "";
//        }

        return "";
    }

    public static List<String> views(String appToken, String tableId) {
        List<String> results = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();
        param.put("page_size", 100);
        boolean hasMore = true;

        while (hasMore) {
            JSONObject jsonObject = null;
            String resultStr = "";
            for (int i = 0; i < 3; i++) {
                try {
                    HttpResponse response = HttpRequest.get("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/views")
                            .header("Authorization", "Bearer " + Constants.ACCESS_TOKEN)
                            .form(param)
                            .execute();
                    resultStr = response.body();
                    response.close();
                    System.out.println(resultStr);
                    log.info("列出记录接口: {}", resultStr);
                    //                Thread.sleep(2000L);
                    jsonObject = JSON.parseObject(resultStr);
                } catch (Exception e) {
                    log.error("接口请求失败，重试 {} 次, message: {}, body: {}", i + 1, e.getMessage(), resultStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ecp) {
                        log.error("sleep异常", ecp);
                    }
                }
                if (jsonObject != null && jsonObject.getInteger("code") != 0) {
                    log.error("接口请求失败，重试 {} 次, body: {}", i + 1, resultStr);
                } else if (jsonObject != null && jsonObject.getInteger("code") == 0) {
                    break;
                }
            }
            if (jsonObject == null || jsonObject.getInteger("code") != 0) {
                log.error("列出记录接口调用失败");
                return Collections.emptyList();
            }
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray items = data.getJSONArray("items");
            for (int i = 0; i < items.size(); i++) {
                String viewName = items.getJSONObject(i).getString("view_name");
                results.add(viewName);
            }
            if ((boolean) data.get("has_more")) {
                param.put("page_token", data.getString("page_token"));
            } else {
                hasMore = false;
            }
        }
        return results;
    }

    public static void addView(String appToken, String tableId, String s) {
        try {
            JSONObject object = new JSONObject();
            object.put("view_name", s);
            String resultStr = HttpRequest.post("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/views")
                    .header("Authorization", "Bearer " + Constants.ACCESS_TOKEN)
                    .form(object)
                    .execute().body();
            log.info("新增视图接口: {}", resultStr);
            if (StringUtils.isNotEmpty(resultStr)) {
                JSONObject resultObject = JSON.parseObject(resultStr);
                if (resultObject.getInteger("code") == 0) {
                    String viewId = resultObject.getJSONObject("data").getJSONObject("view").getString("view_id");
                    String viewName = resultObject.getJSONObject("data").getJSONObject("view").getString("view_name");
                    log.info("新增视图成功, viewId: {}, viewName: {}", viewId, viewName);
                    // 更新视图过滤条件
                    List<String> stringList = fields(appToken, tableId, s);
                    if (ArrayUtil.isEmpty(stringList) || stringList.size() < 2) {
                        log.info("添加视图时检测到字段中无名称为账户的字段");
                        return;
                    }
                    String form = "{\n" +
                            "    \"property\": {\n" +
                            "        \"filter_info\": {\n" +
                            "            \"conditions\": [\n" +
                            "                {\n" +
                            "                    \"field_id\": \"" + stringList.get(0) + "\",\n" +
                            "                    \"operator\": \"is\",\n" +
                            "                    \"value\": \"[\\\"" + stringList.get(1) + "\\\"]\"\n" +
                            "                }\n" +
                            "            ],\n" +
                            "            \"conjunction\": \"and\"\n" +
                            "        }\n" +
                            "    }\n" +
                            "}";
                    String updateResultStr = HttpRequest.patch("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/views/" + viewId)
                            .header("Authorization", "Bearer " + Constants.ACCESS_TOKEN)
                            .body(form)
                            .execute().body();
                    log.info("更新视图接口: {}", updateResultStr);
                    if (StringUtils.isNotEmpty(updateResultStr)) {
                        JSONObject updateResultObject = JSON.parseObject(updateResultStr);
                        if (updateResultObject.getInteger("code") == 0) {
                            log.info("更新视图成功, viewId: {}, viewName: {}", viewId, viewName);
                        } else {
                            log.error("更新视图接口出错, viewId: {}, viewName: {}, {}", viewId, viewName, updateResultStr);
                        }
                    }

                } else {
                    log.error("新增视图接口出错: {}", resultStr);
                }
            }
        } catch (Exception e) {
            log.error("新增视图接口异常: ", e);
        }
    }

    public static List<String> fields(String appToken, String tableId, String s) {
        List<String> r = new ArrayList<>();
        Map<String, Object> param = new HashMap<>();
        param.put("page_size", 100);
        boolean hasMore = true;
        while (hasMore) {
            JSONObject jsonObject = null;
            String resultStr = "";
            for (int i = 0; i < 3; i++) {
                try {
                    HttpResponse response = HttpRequest.get("https://open.feishu.cn/open-apis/bitable/v1/apps/" + appToken + "/tables/" + tableId + "/fields")
                            .header("Authorization", "Bearer " + Constants.ACCESS_TOKEN)
                            .form(param)
                            .execute();
                    resultStr = response.body();
                    response.close();
                    System.out.println(resultStr);
                    log.info("列出字段接口: {}", resultStr);
                    //                Thread.sleep(2000L);
                    jsonObject = JSON.parseObject(resultStr);
                } catch (Exception e) {
                    log.error("接口请求失败，重试 {} 次, message: {}, body: {}", i + 1, e.getMessage(), resultStr);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ecp) {
                        log.error("sleep异常", ecp);
                    }
                }
                if (jsonObject != null && jsonObject.getInteger("code") != 0) {
                    log.error("接口请求失败，重试 {} 次, body: {}", i + 1, resultStr);
                } else if (jsonObject != null && jsonObject.getInteger("code") == 0) {
                    break;
                }
            }
            if (jsonObject == null || jsonObject.getInteger("code") != 0) {
                log.error("列出字段接口调用失败");
                return Collections.emptyList();
            }
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray items = data.getJSONArray("items");
            for (int i = 0; i < items.size(); i++) {
                String fieldName = items.getJSONObject(i).getString("field_name");
                if ("账户".equals(fieldName)) {
                    r.add(items.getJSONObject(i).getString("field_id"));
                    JSONArray jsonArray = items.getJSONObject(i).getJSONObject("property").getJSONArray("options");
                    for (int j = 0; j < jsonArray.size(); j++) {
                        if (s.equals(jsonArray.getJSONObject(j).getString("name"))) {
                            r.add(jsonArray.getJSONObject(j).getString("id"));
                            return r;
                        }
                    }
                }
            }
            if ((boolean) data.get("has_more")) {
                param.put("page_token", data.getString("page_token"));
            } else {
                hasMore = false;
            }
        }
        return Collections.emptyList();
    }
}
