package com.lundong.sync.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lundong.sync.config.Constants;
import com.lundong.sync.entity.Items;
import com.lundong.sync.entity.Order;
import com.lundong.sync.service.BitableService;
import com.lundong.sync.util.FileUtil;
import com.lundong.sync.util.SignUtil;
import com.lundong.sync.util.StringUtil;
import com.lundong.sync.util.netsuite.NetsuiteUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shuangquan.chen
 * @date 2023-12-11 14:59
 */
@Slf4j
@Service
public class BitableServiceImpl implements BitableService {

    /**
     * 销售订单
     *
     * @return
     */
    @Override
    @Scheduled(cron = "0 0 3 ? * *")
    public void sync() {
        log.info("===开始批量同步订单===");
        try {
            String tableId = FileUtil.textFileToString();
            List<Order> orderList = new ArrayList<>();
            List<Order> goods = new ArrayList<>();

//            LocalDateTime dateTime = LocalDateTime.of(2023, 12, 1, 12, 30);

            // 构建昨天这个时间点，构建时间参数json
            // {"startDate":"2023/12/03","endDate":"2023/12/8"}

//            LocalDateTime now = LocalDateTime.now().minusDays(15);
//            long days = ChronoUnit.DAYS.between(dateTime, LocalDateTime.now());
//            for (long k = days + 1; k >= 1; k--) {
//                if (k <= 2) {
//                    return;
//                }
            String content = NetsuiteUtil.request();
            log.info("content: {}", StringUtil.subLog(content));
            // 插入多维表格
            JSONObject orderObject = JSONObject.parseObject(content);
            if ("Success".equals(orderObject.getString("ask"))) {
                List<Order> orders = JSONObject.parseArray(orderObject.getString("data"), Order.class);
                orders = orders.stream().filter(n -> !"9610过渡-母公司".equals(n.getWarehouse())).collect(Collectors.toList());
//                List<Order> collect = data.stream().limit(30).collect(Collectors.toList());
                for (int i = 0; i < orders.size(); i++) {
                    double priceSum = 0;
                    int goodsSum = 0;
//                        JSONArray itemsJsonArray = orderObject.getJSONArray("data").getJSONObject(i).getJSONArray("items");
                    List<Items> items = orders.get(i).getItems();
                    for (int j = 0; j < items.size(); j++) {
                        Order good = new Order();
//                        BeanUtils.copyProperties(data.get(i), order);
                        good.setNsOrder(orders.get(i).getNsOrder());
                        good.setSku(items.get(j).getName());
                        good.setDisplayname(items.get(j).getDisplayname());
                        good.setGoodsSalesrep(items.get(j).getSalesrep());
                        good.setItemQuantity(items.get(j).getQuantity());
                        good.setItemPrice(items.get(j).getPrice());
                        good.setItemFabricName(items.get(j).getFabricName());
                        good.setItemPricematchingCode(items.get(j).getPricematchingCode());
//                            good.setDisplayname(itemsJsonArray.getJSONObject(j).getString("displayname"));
//                            good.setGoodsSalesrep(itemsJsonArray.getJSONObject(j).getString("salesrep"));
//                            good.setItemQuantity(itemsJsonArray.getJSONObject(j).getInteger("quantity"));
//                            good.setItemPrice(itemsJsonArray.getJSONObject(j).getDouble("price"));
//                            good.setItemFabricName(itemsJsonArray.getJSONObject(j).getString("fabric_name"));
//                            good.setItemPricematchingCode(itemsJsonArray.getJSONObject(j).getString("pricematching_code"));
//                            good.setGoodsSum(itemsJsonArray.getJSONObject(j).getInteger("quantity"));
//                            good.setPriceSum(itemsJsonArray.getJSONObject(j).getDouble("price"));
                        good.setAccount(orders.get(i).getAccount());
                        good.setDate(orders.get(i).getDate());
                        good.setCurrency(orders.get(i).getCurrency());
                        good.setStatus(orders.get(i).getStatus());
                        good.setDeliveryDate(orders.get(i).getDeliveryDate());
                        good.setMarketplace(orders.get(i).getMarketplace());
                        good.setSalesrep(orders.get(i).getSalesrep());
                        good.setWarehouse(orders.get(i).getWarehouse());
                        good.setSubsidiary(orders.get(i).getSubsidiary());
                        good.setPlatformOrder(orders.get(i).getPlatformOrder());
                        goods.add(good);
                        Double price = items.get(j).getPrice();
                        Integer quantity = items.get(j).getQuantity();
                        BigDecimal temp = new BigDecimal(priceSum).add(new BigDecimal(price).multiply(new BigDecimal(quantity)));
                        BigDecimal divide = temp.divide(BigDecimal.ONE, 4, RoundingMode.HALF_UP);
                        priceSum = divide.doubleValue();
                        goodsSum += quantity;
                    }
                    orders.get(i).setPriceSum(priceSum);
                    orders.get(i).setGoodsSum(goodsSum);
                    orderList.add(orders.get(i));
                    orderList.addAll(goods);
                    goods.clear();
                }
                List<List<Order>> partitions = ListUtil.partition(orderList, 500);
                for (List<Order> list : partitions) {
                    String itemJson = "";
                    JSONObject body = new JSONObject();
                    JSONArray jsonArray = new JSONArray();
                    for (Order field : list) {
                        JSONObject itemObject = new JSONObject();
                        itemObject.put("fields", JSONObject.toJSON(field));
                        jsonArray.add(itemObject);
                    }
                    body.put("records", jsonArray);
                    itemJson = body.toJSONString();
                    // 处理字段为中文
                    itemJson = StringUtil.processChineseTitleOrder(itemJson);
                    log.info("json: {}", StringUtil.subLog(itemJson));
                    SignUtil.batchInsertRecord(itemJson, Constants.APP_TOKEN, tableId);
                }
                orderList.clear();

                // 新增视图
                // 每天对新增的订单列表map取出不同的店铺，列出视图接口如果map中的店铺没在列出视图里面的话就新增视图接口并更新视图接口筛选条件。(字段id需调用列出字段接口)
                List<String> collect = orders.stream().map(Order::getAccount).distinct().collect(Collectors.toList()); // 12345
                // 列出视图
                List<String> views = SignUtil.views(Constants.APP_TOKEN, tableId); // 12

                // 新增视图 345
                collect = collect.stream().filter(n -> !views.contains(n)).collect(Collectors.toList());
                for (String s : collect) {
                    SignUtil.addView(Constants.APP_TOKEN, tableId, s);
                }
            }
//                orderList.clear();
//            }
        } catch (Exception e) {
            log.error("异常：", e);
        }
        log.info("===批量同步订单结束===");
    }
}
