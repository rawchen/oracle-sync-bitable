package com.lundong.sync.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author RawChen
 * @date 2023-03-13 13:42
 */
@Data
public class Order {

	/**
	 * 平台单号
	 */
	@JSONField(name = "platform_order")
	private String platformOrder;

	/**
	 * NS单号
	 */
	@JSONField(name = "ns_order")
	private String nsOrder;

	/**
	 * 附属
	 */
	@JSONField(name = "subsidiary")
	private String subsidiary;

	/**
	 * 仓库
	 */
	@JSONField(name = "warehouse")
	private String warehouse;

	/**
	 * 日期
	 */
	@JSONField(name = "date", serialize = false)
	private String date;

	/**
	 * 日期
	 */
	@JSONField(name = "originDate")
	private Long originDate;

	/**
	 * 运输费
	 */
	@JSONField(name = "shippingcost")
	private Double shippingcost;

	/**
	 * 销售代表
	 */
	@JSONField(name = "salesrep")
	private String salesrep;


	/**
	 * 账户
	 */
	@JSONField(name = "account")
	private String account;

	/**
	 * 市场
	 */
	@JSONField(name = "marketplace")
	private String marketplace;

	/**
	 * 交货日期
	 */
	@JSONField(name = "deliveryDate")
	private String deliveryDate;

	/**
	 * 状态
	 */
	@JSONField(name = "status")
	private String status;

	/**
	 * 币别
	 */
	@JSONField(name = "currency")
	private String currency;

	/**
	 * 汇率
	 */
	@JSONField(name = "exchangerate")
	private Double exchangerate;

	/**
	 * 商品名称
	 */
	@JSONField(name = "商品SKU")
	private String sku;

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
	 * 商品价格匹配代码
	 */
	@JSONField(name = "商品名称")
	private String displayname;

	/**
	 * 商品销售代表
	 */
	@JSONField(name = "商品销售代表")
	private String goodsSalesrep;

	/**
	 * 价格合计
	 */
	@JSONField(name = "价格合计")
	private Double priceSum;

	/**
	 * 商品总数量
	 */
	@JSONField(name = "商品总数量")
	private Integer goodsSum;

	@JSONField(serialize = false)
	private List<Items> items;
}
