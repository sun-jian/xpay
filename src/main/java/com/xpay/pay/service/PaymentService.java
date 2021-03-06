package com.xpay.pay.service;

import static com.xpay.pay.ApplicationConstants.CODE_COMMON;
import static com.xpay.pay.ApplicationConstants.STATUS_BAD_REQUEST;
import static com.xpay.pay.ApplicationConstants.STATUS_UNAUTHORIZED;
import static com.xpay.pay.proxy.IPaymentProxy.NO_RESPONSE;

import com.xpay.pay.model.StoreChannel.IpsProps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xpay.pay.ApplicationConstants;
import com.xpay.pay.exception.Assert;
import com.xpay.pay.model.App;
import com.xpay.pay.model.Bill;
import com.xpay.pay.model.Order;
import com.xpay.pay.model.Store;
import com.xpay.pay.model.StoreChannel;
import com.xpay.pay.model.StoreChannel.PaymentGateway;
import com.xpay.pay.model.StoreGoods;
import com.xpay.pay.proxy.IPaymentProxy;
import com.xpay.pay.proxy.IPaymentProxy.PayChannel;
import com.xpay.pay.proxy.PaymentProxyFactory;
import com.xpay.pay.proxy.PaymentRequest;
import com.xpay.pay.proxy.PaymentResponse;
import com.xpay.pay.proxy.PaymentResponse.OrderStatus;
import com.xpay.pay.util.AppConfig;
import com.xpay.pay.util.CommonUtils;
import com.xpay.pay.util.IDGenerator;

@Service
public class PaymentService {
	@Autowired
	private PaymentProxyFactory paymentProxyFactory;
	@Autowired
	private OrderService orderService;
	@Autowired
	private StoreService storeService;

	public Order createOrder(App app, String uid, String orderNo, Store store, PayChannel channel,
			String deviceId, String ip, Float totalFee, String orderTime,
			String sellerOrderNo, String attach, String notifyUrl,String returnUrl,
			String subject, String storeChannelId) {
		StoreChannel storeChannel = null;
		if(StringUtils.isNotBlank(storeChannelId)) {
			storeService.findStoreChannelById(Long.valueOf(storeChannelId));
		} else {
			storeChannel = orderService.findUnusedChannelByStore(store, orderNo);
			storeChannel = storeChannel == null? orderService.findUnusedChannelByAgent(store.getAgentId(), orderNo): storeChannel;
		}
		Assert.notNull(storeChannel, String.format("No avaiable store channel, please try later, sellerOrderNo: %s", StringUtils.trimToEmpty(sellerOrderNo)));

		Order order = new Order();
		order.setApp(app);
		order.setOrderNo(orderNo);
		order.setStore(store);
		order.setStoreId(store.getId());
		order.setStoreChannel(storeChannel);
		order.setPayChannel(channel);
		order.setDeviceId(deviceId);
		order.setIp(ip);
		order.setTotalFee(totalFee);
		order.setOrderTime(orderTime);
		order.setSellerOrderNo(sellerOrderNo);
		order.setAttach(attach);
		order.setNotifyUrl(notifyUrl);
		order.setReturnUrl(returnUrl);
		order.setSubject(subject);
		orderService.insert(order);

		return order;
	}
	
	public Order createGoodsOrder(Store store, StoreGoods goods, String uid, String orderNo) {
		Assert.isTrue(goods!=null && CollectionUtils.isNotEmpty(goods.getExtGoodsList()), "No avaiable goods");
		validateQuota(store);
		
		Order order = new Order();
		order.setCodeUrl(orderService.findAvaiableQrCode(store, goods));
		order.setSubject(goods.getName());
		order.setTotalFee(goods.getAmount());
		if(StringUtils.isNotBlank(orderNo)) {
			order.setOrderNo(orderNo);
		} else {
			order.setOrderNo(IDGenerator.buildQrOrderNo(goods.getStoreId()));
		}
		order.setSellerOrderNo(uid);
		order.setStoreId(store.getId());
		order.setNotifyUrl(store.getNotifyUrl());
		order.setReturnUrl(store.getReturnUrl());
		order.setGoodsId(goods.getId());
		order.setExtStoreCode(goods.getExtStoreId());
		order.setStatus(OrderStatus.NOTPAY);
		order.setOrderTime(IDGenerator.formatNow(IDGenerator.TimePattern14));
		if(order.getCodeUrl().startsWith("https://qr.chinaums.com")) {
			order.setPayChannel(PayChannel.XIAOWEI);
		} else {
			order.setPayChannel(PayChannel.XIAOWEI_H5);
		}
		order.setAppId(store.getAppId());
		orderService.insert(order);
		return order;
	}

	public Bill unifiedOrder(Order order) {
		PaymentRequest request = this.toPaymentRequest(order);
		IPaymentProxy paymentProxy = paymentProxyFactory.getPaymentProxy(order.getStoreChannel().getPaymentGateway());
		PaymentResponse response = paymentProxy.unifiedOrder(request);

		Bill bill = response.getBill();
		Assert.isTrue(!StringUtils.isBlank(bill.getCodeUrl()) || !StringUtils.isBlank(bill.getTokenId()),
				ApplicationConstants.STATUS_BAD_GATEWAY, NO_RESPONSE,
				response.getMsg());
		bill.setOrder(order);
		return bill;
	}

	public boolean updateBill(Order order, Bill bill) {
		if(bill == null) {
			order.setStatus(OrderStatus.PAYERROR);
		} else {
			order.setExtOrderNo(bill.getGatewayOrderNo());
			order.setCodeUrl(bill.getCodeUrl());
			order.setPrepayId(bill.getPrepayId());
			order.setTokenId(bill.getTokenId());
			order.setPayInfo(bill.getPayInfo());
			order.setStatus(bill.getOrderStatus());
		}
		return orderService.update(order);
	}

	public boolean updateTradeAmount(Order order) {
		if(order == null) {
			return true;
		}
		Store store = order.getStore();
		if(order.getGoods() == null || order.getGoods().getStoreId()==order.getStoreId()) {
			float newNonBail = store.getNonBail() + order.getTotalFee();
			store.setNonBail(newNonBail);
			return storeService.updateById(store);
		} else if(order.getGoods() != null && order.getGoods().getStoreId()!=order.getStoreId()) {
			float newBail = store.getBail() + order.getTotalFee();
			store.setBail(newBail);
			return storeService.updateById(store);
		}
		return true;
	}

	public void validateQuota(Store store) {
		Assert.notNull(store, "No store found");
		Assert.isTrue(-1 ==store.getQuota() || store.getNonBail()<store.getQuota(), "No enough quota remained");
		Assert.isTrue(-1 == store.getDailyLimit() || store.getNonBail() < store.getDailyLimit(), "Exceed transaction limit");
	}

	public void validateStoreLink(Store store, String returnUrl) {
		Assert.notNull(store, "No store found");
		Assert.notEmpty(returnUrl, STATUS_BAD_REQUEST, CODE_COMMON, "ReturnUrl cannot be null");
		Assert.isTrue(store.isValidStoreLink(returnUrl), STATUS_UNAUTHORIZED, CODE_COMMON, "Unauthorized returnUrl");
	}

	
	public Bill query(Long appId, String orderNo, String storeCode, boolean isCsr) {
		Order order = orderService.findActiveByOrderNo(orderNo);
		Assert.isTrue(storeCode.equals(order.getStore().getCode()), "No such order found for the store");
		Assert.isTrue(appId == order.getAppId(), "No such order found under the app");
		Assert.isTrue(order.isSettle() || CommonUtils.isWithinHours(order.getOrderTime(), IDGenerator.TimePattern14, 24), "Order expired");
		if(isCsr || (order.isRemoteQueralbe() && CommonUtils.isWithinHours(order.getOrderTime(), IDGenerator.TimePattern14, 24))) {
			try {
				PaymentRequest paymentRequest = toQueryRequest(order);
				IPaymentProxy paymentProxy = paymentProxyFactory.getPaymentProxy(order.getStoreChannel().getPaymentGateway());
				PaymentResponse response = paymentProxy.query(paymentRequest);
				Bill bill = response.getBill();
				bill.setOrder(order);
				if(bill!=null && !bill.getOrderStatus().equals(order.getStatus())) {
					order.setStatus(bill.getOrderStatus());
					if(StringUtils.isNotBlank(bill.getTargetOrderNo())) {
						order.setTargetOrderNo(bill.getTargetOrderNo());
					}
					orderService.update(order);
				}
				return bill;
			} catch(Exception e) {

			}
		}
		return toBill(order);
	}

	public Bill refund(Long appId, String orderNo, String storeCode, boolean isCsr) {
		Order order = orderService.findActiveByOrderNo(orderNo);
		Assert.isTrue(storeCode.equals(order.getStore().getCode()), "No such order found for the store");
		Assert.isTrue(appId == order.getAppId(), "No such order found under the app");
		Assert.isTrue(!order.isRechargeOrder(), "Recharge order can't be refunded");

		if(isCsr || (order.isRefundable()  && CommonUtils.isWithinHours(order.getOrderTime(), IDGenerator.TimePattern14, 24))) {
			PaymentRequest paymentRequest = toQueryRequest(order);
			paymentRequest.setTotalFee(order.getTotalFee());
			IPaymentProxy paymentProxy = paymentProxyFactory.getPaymentProxy(order.getStoreChannel().getPaymentGateway());
			PaymentResponse response = paymentProxy.refund(paymentRequest);

			Bill bill = response.getBill();
			if(bill !=null && (OrderStatus.REFUND.equals(bill.getOrderStatus()) || OrderStatus.REFUNDING.equals(bill.getOrderStatus())) || OrderStatus.REVOKED.equals(bill.getOrderStatus())) {
				bill.setOrder(order);
				order.setRefundOrderNo(bill.getRefundOrderNo());
				order.setRefundExtOrderNo(bill.getGatewayRefundOrderNo());
				order.setRefundTime(bill.getRefundTime());
				order.setStatus(bill.getOrderStatus());
				orderService.update(order);
			}
			return bill;
		} else {
			return toBill(order);
		}
	}

	private static final String LOCAL_ID = AppConfig.XPayConfig.getProperty("inRequest.address", CommonUtils.getLocalIP());
	private static final String DEFAULT_NOTIFY_URL = AppConfig.XPayConfig.getProperty("notify.endpoint");
	public PaymentRequest toPaymentRequest(Order order) {
		PaymentRequest request = new PaymentRequest();
		request.setExtStoreId(order.getStoreChannel().getExtStoreId());
		request.setChannelProps(order.getStoreChannel().getChannelProps());
		String deviceId = order.getDeviceId();
		deviceId = StringUtils.isBlank(deviceId)?order.getIp():deviceId;
		request.setDeviceId(deviceId);
		request.setPayChannel(order.getPayChannel());
		request.setTotalFee(order.getTotalFee());
		request.setAttach(order.getAttach());
		request.setOrderNo(order.getOrderNo());
//		request.setUserOpenId(order.getDeviceId());
		request.setNotifyUrl(DEFAULT_NOTIFY_URL+order.getStoreChannel().getPaymentGateway().toString().toLowerCase());

		PaymentGateway gateway = order.getStoreChannel().getPaymentGateway();
		if(isDirectReturnChannel(gateway) ) {
			request.setReturnUrl(order.getReturnUrl());
		}
		if(PaymentGateway.JUZHEN.equals(gateway) || PaymentGateway.KEKEPAY.equals(gateway) || PaymentGateway.QFTXMP.equals(gateway)) {
			request.setServerIp(LOCAL_ID);
		} else if(PaymentGateway.MIAOFU.equals(gateway)) {
			String notifyUrl = request.getNotifyUrl() + "/"+request.getOrderNo();
			request.setNotifyUrl(notifyUrl);
		} else if(PaymentGateway.IPSSCAN.equals(gateway)
				|| PaymentGateway.IPSQUICK.equals(gateway)
				|| PaymentGateway.IPSWX.equals(gateway)){
			request.setOrderTime(order.getOrderTime());
			if(request.getChannelProps() != null) {
				IpsProps props = (IpsProps) request.getChannelProps();
				if(props.isUseH5Ext()!=null && props.isUseH5Ext()) {
					request.setExtH5(true);
				}else{
					request.setExtH5(false);
				}
			}
		}
//		else if(PaymentGateway.RUBIPAY.equals(order.getStoreChannel().getPaymentGateway())) {
//			request.setServerIp(LOCAL_ID);
//			request.setNotifyUrl(DEFAULT_NOTIFY_URL+order.getStoreChannel().getPaymentGateway().toString().toLowerCase());
//		}
//		else if(PaymentGateway.SWIFTPASS.equals(order.getStoreChannel().getPaymentGateway())) {
//			request.setServerIp(LOCAL_ID);
//			request.setNotifyUrl(DEFAULT_NOTIFY_URL+order.getStoreChannel().getPaymentGateway().toString().toLowerCase());
//		}
//

		if (StringUtils.isNotBlank(order.getSubject())) {
			request.setSubject(order.getSubject());
		} else {
			request.setSubject(DEFAULT_SUBJECT);
		}
		request.setSubject(this.customizeCsrTel(request.getSubject(), order));
		return request;
	}

	private PaymentRequest toQueryRequest(Order order) {
		PaymentRequest request = new PaymentRequest();
		PaymentGateway gateway = order.getStoreChannel().getPaymentGateway();
		request.setExtStoreId(order.getStoreChannel().getExtStoreId());
		request.setChannelProps(order.getStoreChannel().getChannelProps());
		request.setPayChannel(order.getPayChannel());
		request.setOrderNo(order.getOrderNo());
		if(isChinaUmsChannel(gateway) || PaymentGateway.JUZHEN.equals(gateway) || PaymentGateway.KEFU.equals(gateway)) {
			request.setOrderTime(order.getOrderTime());
			request.setGatewayOrderNo(order.getExtOrderNo());
		} else if(PaymentGateway.MIAOFU.equals(gateway)) {
			request.setGatewayOrderNo(order.getExtOrderNo());
		} else if(PaymentGateway.IPSQUICK.equals(gateway)
				||PaymentGateway.IPSSCAN.equals(gateway)
				||PaymentGateway.IPSWX.equals(gateway)) {
			if(OrderStatus.REFUNDING.equals(order.getStatus()) || OrderStatus.REFUND.equals(order.getStatus()) || OrderStatus.REFUNDERROR.equals(order.getStatus())){
        request.setRefundTime(order.getRefundTime());
        request.setRefundOrderNo(order.getRefundOrderNo());
        request.setRefundGatewayOrderNo(order.getRefundExtOrderNo());
			}
			request.setOrderTime(order.getOrderTime());
      request.setTotalFee(order.getTotalFee());
		}


		return request;
	}

	private Bill toBill(Order order) {
		Bill bill = new Bill();
		bill.setCodeUrl(order.getCodeUrl());
		bill.setPrepayId(order.getPrepayId());
		bill.setTokenId(order.getTokenId());;
		bill.setPayInfo(order.getPayInfo());
		bill.setOrderNo(order.getOrderNo());
		bill.setGatewayOrderNo(order.getExtOrderNo());
		bill.setOrderStatus(order.getStatus());
		bill.setOrder(order);
		return bill;
	}

	private static final String DEFAULT_SUBJECT = "游戏";
	private static final String DEFAULT_SUBJECT_CHINAUMS = "投诉热线:95534";
	private String customizeCsrTel(String subject, Order order) {
		String storeTel = order.getStore().getCsrTel();
		if(StringUtils.isNotBlank(storeTel)) {
			return subject + storeTel;
		} else if(isChinaUmsChannel(order.getStoreChannel().getPaymentGateway())) {
			return subject+"("+DEFAULT_SUBJECT_CHINAUMS+")";
		}
		return subject;
	}

	private boolean isChinaUmsChannel(PaymentGateway gateway) {
		return PaymentGateway.CHINAUMS.equals(gateway) ||
				PaymentGateway.CHINAUMSV2.equals(gateway) ||
				PaymentGateway.CHINAUMSH5.equals(gateway) ||
				PaymentGateway.CHINAUMSV3.equals(gateway) ||
				PaymentGateway.CHINAUMSWAP.equals(gateway);
	}

	private boolean isDirectReturnChannel(PaymentGateway gateway) {
		return PaymentGateway.CHINAUMS.equals(gateway) ||
				PaymentGateway.CHINAUMSV2.equals(gateway) ||
				PaymentGateway.CHINAUMSH5.equals(gateway) ||
				PaymentGateway.CHINAUMSWAP.equals(gateway) ||
				PaymentGateway.CHINAUMSV3.equals(gateway) ||
				PaymentGateway.UPAY.equals(gateway) ||
				PaymentGateway.IPSQUICK.equals(gateway) ||
				PaymentGateway.IPSWX.equals(gateway) ||
				PaymentGateway.KEKEPAY.equals(gateway) ||
				PaymentGateway.TXF.equals(gateway);
	}

}
