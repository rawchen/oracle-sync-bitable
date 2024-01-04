package com.lundong.sync.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2024-01-03 10:36
 */
@Data
public class Items {

    @JSONField(name = "name")
    private String name;

    @JSONField(name = "quantity")
    private Integer quantity;

    @JSONField(name = "price")
    private Double price;

    @JSONField(name = "fabric_name")
    private String fabricName;

    @JSONField(name = "pricematching_code")
    private String pricematchingCode;

    @JSONField(name = "salesrep")
    private String salesrep;

    @JSONField(name = "displayname")
    private String displayname;

}
