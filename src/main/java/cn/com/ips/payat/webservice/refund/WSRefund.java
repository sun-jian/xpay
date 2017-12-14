
package cn.com.ips.payat.webservice.refund;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "WSRefund", targetNamespace = "http://payat.ips.com.cn/WebService/Refund", wsdlLocation = "https://newpay.ips.com.cn/psfp-entry/services/refund?wsdl")
public class WSRefund
    extends Service
{

    private final static URL WSREFUND_WSDL_LOCATION;
    private final static WebServiceException WSREFUND_EXCEPTION;
    private final static QName WSREFUND_QNAME = new QName("http://payat.ips.com.cn/WebService/Refund", "WSRefund");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("https://newpay.ips.com.cn/psfp-entry/services/refund?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        WSREFUND_WSDL_LOCATION = url;
        WSREFUND_EXCEPTION = e;
    }

    public WSRefund() {
        super(__getWsdlLocation(), WSREFUND_QNAME);
    }

    public WSRefund(WebServiceFeature... features) {
        super(__getWsdlLocation(), WSREFUND_QNAME, features);
    }

    public WSRefund(URL wsdlLocation) {
        super(wsdlLocation, WSREFUND_QNAME);
    }

    public WSRefund(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, WSREFUND_QNAME, features);
    }

    public WSRefund(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WSRefund(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns RefundService
     */
    @WebEndpoint(name = "WSRefundSoap")
    public RefundService getWSRefundSoap() {
        return super.getPort(new QName("http://payat.ips.com.cn/WebService/Refund", "WSRefundSoap"), RefundService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RefundService
     */
    @WebEndpoint(name = "WSRefundSoap")
    public RefundService getWSRefundSoap(WebServiceFeature... features) {
        return super.getPort(new QName("http://payat.ips.com.cn/WebService/Refund", "WSRefundSoap"), RefundService.class, features);
    }

    private static URL __getWsdlLocation() {
        if (WSREFUND_EXCEPTION!= null) {
            throw WSREFUND_EXCEPTION;
        }
        return WSREFUND_WSDL_LOCATION;
    }

}
