package com.lundong.sync.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author RawChen
 * @date 2023-03-13 13:42
 */
@Data
public class Good {

	/**
	 * NS单号
	 */
	@JSONField(name = "ns_order")
	private String nsOrder;

	/**
	 * 商品名称
	 */
	@JSONField(name = "商品名称")
	private String itemName;

	/**
	 * 商品数量
	 */
	@JSONField(name = "商品数量")
	private Integer itemQuantity;

	/**
	 * 商品价格
	 */
	@JSONField(name = "商品价格")
	private Double itemPrice;

	/**
	 * 商品面料名称
	 */
	@JSONField(name = "商品面料名称")
	private String itemFabricName;

	/**
	 * 商品价格匹配代码
	 */
	@JSONField(name = "商品价格匹配代码")
	private String itemPricematchingCode;

	/**
	 * 父记录
	 */
	@JSONField(name = "父记录")
	private List<String> parentRecordIds;


}
