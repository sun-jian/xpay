package com.xpay.pay.dao.entity;

import com.xpay.pay.proxy.PaymentResponse.TradeStatus;

public class Order {
	private long id;
	private String orderNo;
	private long appId;
	private long storeId;
	private long storeChannelId;
	private String totalFee;
	private String orderTime;
	private String sellerOrderNo;
	private String extOrderNo;
	private int payChannel;
	private String attach;
	private String deviceId;
	private String ip;
	private String notifyUrl;
	private String codeUrl;
	private String prepayId;
	private TradeStatus status;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public long getAppId() {
		return appId;
	}
	public void setAppId(long appId) {
		this.appId = appId;
	}
	public long getStoreId() {
		return storeId;
	}
	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}
	public long getStoreChannelId() {
		return storeChannelId;
	}
	public void setStoreChannelId(long storeChannelId) {
		this.storeChannelId = storeChannelId;
	}
	public String getTotalFee() {
		return totalFee;
	}
	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public String getSellerOrderNo() {
		return sellerOrderNo;
	}
	public void setSellerOrderNo(String sellerOrderNo) {
		this.sellerOrderNo = sellerOrderNo;
	}
	public String getExtOrderNo() {
		return extOrderNo;
	}
	public void setExtOrderNo(String extOrderNo) {
		this.extOrderNo = extOrderNo;
	}
	public int getPayChannel() {
		return payChannel;
	}
	public void setPayChannel(int payChannel) {
		this.payChannel = payChannel;
	}
	public String getAttach() {
		return attach;
	}
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public String getCodeUrl() {
		return codeUrl;
	}
	public void setCodeUrl(String codeUrl) {
		this.codeUrl = codeUrl;
	}
	public String getPrepayId() {
		return prepayId;
	}
	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}
	public TradeStatus getStatus() {
		return status;
	}
	public void setStatus(TradeStatus status) {
		this.status = status;
	}
	
}
