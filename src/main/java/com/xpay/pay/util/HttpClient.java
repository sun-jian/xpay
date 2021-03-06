package com.xpay.pay.util;

import com.xpay.pay.ApplicationConstants;
import com.xpay.pay.exception.GatewayException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpClient {

  protected final static Logger logger = LogManager.getLogger("AccessLog");

  public static String doGet(String url, int timeout) {
    CloseableHttpResponse response = null;
    RequestConfig defaultRequestConfig = RequestConfig.custom()
        .setSocketTimeout(timeout).setConnectTimeout(timeout)
        .setConnectionRequestTimeout(timeout)
        .setStaleConnectionCheckEnabled(true).build();
    CloseableHttpClient client = null;
    String result = null;
    long l = System.currentTimeMillis();
    try {
      HttpGet httpGet = new HttpGet(url);
      httpGet.setConfig(defaultRequestConfig);
      logger.info("Get: " + url);
      client = HttpClients.custom()
          .setDefaultRequestConfig(defaultRequestConfig).build();

      response = client.execute(httpGet);

      if (response != null && response.getEntity() != null) {
        result = IOUtils.toString(response.getEntity().getContent());
      }
      logger.info("Get result: " + result + ", took "
          + (System.currentTimeMillis() - l) + "ms");
      return result;
    } catch (Exception e) {
      logger.error("Get failed took " + (System.currentTimeMillis() - l)
          + "ms", e);
      throw new GatewayException(ApplicationConstants.CODE_ERROR_JSON,
          e.getMessage());
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {

        }
      }
    }
  }

  public static String doPost(String url, String body, int timeout) {
    CloseableHttpResponse response = null;
    RequestConfig defaultRequestConfig = RequestConfig.custom()
        .setSocketTimeout(timeout).setConnectTimeout(timeout)
        .setConnectionRequestTimeout(timeout)
        .setStaleConnectionCheckEnabled(true).build();
    CloseableHttpClient client = null;
    String result = null;
    long l = System.currentTimeMillis();
    try {
      StringEntity entityParams = new StringEntity(body, "utf-8");
      HttpPost httpPost = new HttpPost(url);
      httpPost.setConfig(defaultRequestConfig);
      httpPost.setEntity(entityParams);
      logger.info("Post: " + url + ", content: " + body);

      client = HttpClients.custom()
          .setDefaultRequestConfig(defaultRequestConfig).build();

      response = client.execute(httpPost);

      if (response != null && response.getEntity() != null) {
        result = IOUtils.toString(response.getEntity().getContent());
      }
      logger.info("Post result: " + result + ", took "
          + (System.currentTimeMillis() - l) + "ms");
      return result;
    } catch (Exception e) {
      logger.error("Post failed took " + (System.currentTimeMillis() - l)
          + "ms", e);
      throw new GatewayException(ApplicationConstants.CODE_ERROR_JSON,
          e.getMessage());
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {

        }
      }
    }
  }

  public static String doPost(String url, Map<String, String> para, int timeout) {
    CloseableHttpResponse response = null;
    RequestConfig defaultRequestConfig = RequestConfig.custom()
        .setSocketTimeout(timeout).setConnectTimeout(timeout)
        .setConnectionRequestTimeout(timeout)
        .setStaleConnectionCheckEnabled(true).build();
    CloseableHttpClient client = null;
    String result = null;
    long l = System.currentTimeMillis();
    try {

      HttpPost httpPost = new HttpPost(url);
      httpPost.setConfig(defaultRequestConfig);
      HttpEntity httpEntity = new UrlEncodedFormEntity(para.entrySet().stream().map(
          (Function<Entry<String, String>, NameValuePair>) entry -> new BasicNameValuePair(
              entry.getKey(), entry.getValue())).collect(
          Collectors.toList()));
      httpPost.setEntity(httpEntity);
      logger.info("Post: " + url);

      client = HttpClients.custom()
          .setDefaultRequestConfig(defaultRequestConfig).build();

      response = client.execute(httpPost);

      if (response != null && response.getEntity() != null) {
        result = IOUtils.toString(response.getEntity().getContent(), "UTF-8").replaceAll("\n", "");
      }
      logger.info("Post result: " + result + ", status="+response.getStatusLine().getStatusCode() +"， took " + (System.currentTimeMillis() - l) + "ms");
      return result;
    } catch (Exception e) {
      logger.error("Post failed took " + (System.currentTimeMillis() - l)
          + "ms", e);
      throw new GatewayException(ApplicationConstants.CODE_ERROR_JSON,
          e.getMessage());
    } finally {
      if (client != null) {
        try {
          client.close();
        } catch (Exception e) {

        }
      }
    }
  }
}
