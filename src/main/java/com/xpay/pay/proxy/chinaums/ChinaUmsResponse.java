package com.xpay.pay.proxy.chinaums;

import org.apache.commons.lang3.StringUtils;

public class ChinaUmsResponse {
	public static final String SUCCESS = "SUCCESS";
	private String errCode;
	private String errMsg;
	private String msgType;
	private String mid;
	private String tid;
	private String billNo;
	private String billDate;
	private String billQRCode;
	private String billStatus;
	
	public String getErrCode() {
		return errCode;
	}
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getMid() {
		return mid;
	}
	public void setMid(String mid) {
		this.mid = mid;
	}
	public String getTid() {
		return tid;
	}
	public void setTid(String tid) {
		this.tid = tid;
	}
	public String getBillNo() {
		return billNo;
	}
	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}
	public String getBillDate() {
		return billDate;
	}
	public void setBillDate(String billDate) {
		this.billDate = billDate;
	}
	public String getBillQRCode() {
		return billQRCode;
	}
	public void setBillQRCode(String billQRCode) {
		this.billQRCode = billQRCode;
	}
	public String getBillStatus() {
		return billStatus;
	}
	public void setBillStatus(String billStatus) {
		this.billStatus = billStatus;
	}
	public String getQrCodeId() {
		return StringUtils.isBlank(this.billQRCode)?null:billQRCode.substring(billQRCode.lastIndexOf("id=")+3);
	}
}
