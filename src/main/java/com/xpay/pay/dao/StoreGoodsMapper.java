package com.xpay.pay.dao;

import java.util.List;

import com.xpay.pay.model.StoreGoods;

public interface StoreGoodsMapper  extends BaseMapper<StoreGoods> {
	public StoreGoods findByCode(String code);

	public List<StoreGoods> findByStoreId(long storeId);
}
