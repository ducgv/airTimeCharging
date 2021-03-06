/**
 * 
 */
package com.itpro.paymentgw;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.itpro.paymentgw.cmd.GetSubInfoCmd;
import com.itpro.paymentgw.cmd.KeepAliveCmd;
import com.itpro.paymentgw.cmd.LoginCmd;
import com.itpro.paymentgw.cmd.PaymentGWCmd;
import com.itpro.paymentgw.cmd.PaymentPostpaidCmd;
import com.itpro.paymentgw.cmd.TopupPrepaidCmd;
import com.itpro.util.ProcessingThread;
import com.itpro.util.Queue;
import com.topup.payment.TopupPaymentApiWS;
import com.topup.payment.TopupPaymentApiWSPortType;
import com.topup.payment.xsd.TopupPaymentApiWSKeepAliveResult;
import com.topup.payment.xsd.TopupPaymentApiWSLoginResult;
import com.topup.payment.xsd.TopupPaymentApiWSPaymentPospaidResult;
import com.topup.payment.xsd.TopupPaymentApiWSPaymentPostpaidHeader;
import com.topup.payment.xsd.TopupPaymentApiWSQeuryProfileHeader;
import com.topup.payment.xsd.TopupPaymentApiWSQeuryProfilefoResult;
import com.topup.payment.xsd.TopupPaymentApiWSTopupPrepaidHeader;
import com.topup.payment.xsd.TopupPaymentApiWSTopupPrepaidResult;

/**
 * @author Giap Van Duc
 *
 */
public class PaymentGWSession extends ProcessingThread {
	public PaymentGWSession(PaymentGWCmd userCmd, Queue queueResp) {
		// TODO Auto-generated constructor stub
		//serviceLocator = new Vasgateway_ServiceLocator();
		this.userCmd = userCmd;
		this.queueResp = queueResp;
	}
	
	//private Vasgateway_ServiceLocator serviceLocator;
	private PaymentGWCmd userCmd;
	private Queue queueResp;
	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#OnHeartBeat()
	 */
	@Override
	protected void OnHeartBeat() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#initialize()
	 */
	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		setLogPrefix("[PaymentGWSession] ");
	}

	/* (non-Javadoc)
	 * @see com.itpro.util.ProcessingThread#process()
	 */
	@Override
	protected void process() {
		// TODO Auto-generated method stub
		try{
			if (userCmd instanceof LoginCmd) {
				LoginCmd loginCmd = (LoginCmd)userCmd;
				OnLoginCmd(loginCmd);
			}
			else if (userCmd instanceof KeepAliveCmd) {
				KeepAliveCmd keepAliveCmd = (KeepAliveCmd)userCmd;
				OnKeepAliveCmd(keepAliveCmd);
			}
			else if (userCmd instanceof GetSubInfoCmd) {
				GetSubInfoCmd getSubInfoCmd = (GetSubInfoCmd)userCmd;
				OnGetSubInfoCmd(getSubInfoCmd);
			}
			else if (userCmd instanceof TopupPrepaidCmd) {
				TopupPrepaidCmd topupPrepaidCmd = (TopupPrepaidCmd)userCmd;
				OnTopupPrepaidCmd(topupPrepaidCmd);
			}
			else if (userCmd instanceof PaymentPostpaidCmd) {
				PaymentPostpaidCmd paymentPostpaidCmd = (PaymentPostpaidCmd)userCmd;
				OnPaymentPostpaidCmd(paymentPostpaidCmd);
			}
		}
		catch(Exception e){
			userCmd.result = PaymentGWResultCode.R_ERROR;
			userCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			userCmd.resultString=e.getMessage();
			logInfo(userCmd.getRespString());
			queueResp.enqueue(userCmd);
		}
		stop();
}

	private void OnPaymentPostpaidCmd(PaymentPostpaidCmd paymentPostpaidCmd) {
		// TODO Auto-generated method stub
		logInfo(paymentPostpaidCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = new TopupPaymentApiWS();
		TopupPaymentApiWSPortType service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
		//TopupPaymentApiWSPaymentPospaidResult result = service.paymentPostpaid(paymentPostpaidCmd.rechargeMsisdn, ""+paymentPostpaidCmd.amount, ""+paymentPostpaidCmd.transactionId, (new SimpleDateFormat("yyyyMMdd")).format(paymentPostpaidCmd.reqDate), paymentPostpaidCmd.token);
		TopupPaymentApiWSPaymentPospaidResult result = service.paymentPostpaid(paymentPostpaidCmd.rechargeMsisdn, ""+paymentPostpaidCmd.amount, ""+paymentPostpaidCmd.transactionId, (new SimpleDateFormat("yyyyMMdd")).format(paymentPostpaidCmd.reqDate), ""+paymentPostpaidCmd.balanceBonus, ""+paymentPostpaidCmd.dataBonus, paymentPostpaidCmd.originalNumber, paymentPostpaidCmd.token);
		paymentPostpaidCmd.result = PaymentGWResultCode.R_SUCCESS;
		paymentPostpaidCmd.advanceBalance = result.getMsisdnAdvanceBalance().isNil()?0:Integer.parseInt(result.getMsisdnAdvanceBalance().getValue());
		paymentPostpaidCmd.debitBalance = result.getMsisdnDebitBalance().isNil()?0:Integer.parseInt(result.getMsisdnDebitBalance().getValue());
		TopupPaymentApiWSPaymentPostpaidHeader header = result.getPaymentPostpaidHeader().isNil()?null:result.getPaymentPostpaidHeader().getValue();
		if(header!=null){
			paymentPostpaidCmd.resultCode=Integer.parseInt(header.getResultcode().getValue());
			paymentPostpaidCmd.resultString=header.getResultDes().getValue();
		}
		else{
			paymentPostpaidCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			paymentPostpaidCmd.resultString=PaymentGWResultCode.resultDesc.get(PaymentGWResultCode.RC_CALL_SOAP_ERROR);
		}
		
		logInfo(paymentPostpaidCmd.getRespString());
		queueResp.enqueue(paymentPostpaidCmd);
	}

	private void OnTopupPrepaidCmd(TopupPrepaidCmd topupPrepaidCmd) {
		// TODO Auto-generated method stub
		logInfo(topupPrepaidCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = new TopupPaymentApiWS();
		TopupPaymentApiWSPortType service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
		//TopupPaymentApiWSTopupPrepaidResult result = service.topupPrepaid(topupPrepaidCmd.msisdn, ""+topupPrepaidCmd.amount, ""+topupPrepaidCmd.transactionId, (new SimpleDateFormat("yyyyMMdd")).format(topupPrepaidCmd.reqDate), topupPrepaidCmd.token);
		TopupPaymentApiWSTopupPrepaidResult result = service.topupPrepaid(topupPrepaidCmd.msisdn, ""+topupPrepaidCmd.amount, ""+topupPrepaidCmd.transactionId, (new SimpleDateFormat("yyyyMMdd")).format(topupPrepaidCmd.reqDate), ""+topupPrepaidCmd.balanceBonus, ""+topupPrepaidCmd.dataBonus, topupPrepaidCmd.originalNumber, topupPrepaidCmd.token);
		topupPrepaidCmd.result = PaymentGWResultCode.R_SUCCESS;
		topupPrepaidCmd.currentBalance = result.getTargetCurrentBalance().isNil()?0:Integer.parseInt(result.getTargetCurrentBalance().getValue());
		try {
			topupPrepaidCmd.newActiveDate = result.getTargetNewActivedate().isNil()?null:(new SimpleDateFormat("yyyyMMdd").parse(result.getTargetNewActivedate().getValue()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logError("OnTopupPrepaidCmd: Error when parse newActiveDate field of msisdn "+topupPrepaidCmd.msisdn);
			topupPrepaidCmd.newActiveDate = null;
		}
		TopupPaymentApiWSTopupPrepaidHeader header = result.getTopupMasterSimPrepaidHeader().isNil()?null:result.getTopupMasterSimPrepaidHeader().getValue();
		if(header!=null){
			topupPrepaidCmd.resultCode=Integer.parseInt(header.getResultcode().getValue());
			topupPrepaidCmd.resultString=header.getResultDes().getValue();
		}
		else{
			topupPrepaidCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			topupPrepaidCmd.resultString=PaymentGWResultCode.resultDesc.get(PaymentGWResultCode.RC_CALL_SOAP_ERROR);
		}
		
		logInfo(topupPrepaidCmd.getRespString());
		queueResp.enqueue(topupPrepaidCmd);
	}

	private void OnGetSubInfoCmd(GetSubInfoCmd getSubInfoCmd) {
		// TODO Auto-generated method stub
		logInfo(getSubInfoCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = new TopupPaymentApiWS();
		TopupPaymentApiWSPortType service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
		TopupPaymentApiWSQeuryProfilefoResult result = service.qeuryProfileSubcriber(getSubInfoCmd.msisdn, ""+getSubInfoCmd.transactionId, (new SimpleDateFormat("yyyyMMdd")).format(getSubInfoCmd.reqDate), getSubInfoCmd.token);
		getSubInfoCmd.result = PaymentGWResultCode.R_SUCCESS;
		try {
		    getSubInfoCmd.balance=result.getPpsBalance().isNil()?-1:Integer.parseInt(result.getPpsBalance().getValue());
        } catch (Exception e) {
            logError("OnGetSubInfoCmd: Error when parse balance field of msisdn "+getSubInfoCmd.msisdn);
            getSubInfoCmd.balance = -1;
        }
		try {
		    getSubInfoCmd.subType = result.getPayType().isNil()?-1:Integer.parseInt(result.getPayType().getValue());
        } catch (Exception e) {
            logError("OnGetSubInfoCmd: Error when parse subType field of msisdn "+getSubInfoCmd.msisdn);
            getSubInfoCmd.subType = -1;
        }
		getSubInfoCmd.subId=result.getSubID().isNil()?"-1": result.getSubID().getValue();
	
		try {
			getSubInfoCmd.activeDate = result.getActiveDate().isNil()?null:(new SimpleDateFormat("yyyyMMdd").parse(result.getActiveDate().getValue()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logError("OnGetSubInfoCmd: Error when parse activeDate field of msisdn "+getSubInfoCmd.msisdn);
			getSubInfoCmd.activeDate = null;
		}
		TopupPaymentApiWSQeuryProfileHeader header = result.getQeuryBasicInfoHeader().isNil()?null:result.getQeuryBasicInfoHeader().getValue();
		if(header!=null){
			getSubInfoCmd.resultCode=Integer.parseInt(header.getResultcode().getValue());
			getSubInfoCmd.resultString=header.getResultDes().getValue();
		}
		else{
			getSubInfoCmd.resultCode=-1;
			getSubInfoCmd.resultString="Call API function error";
		}
		
		logInfo(getSubInfoCmd.getRespString());
		queueResp.enqueue(getSubInfoCmd);
	}

	private void OnLoginCmd(LoginCmd loginCmd) {
		// TODO Auto-generated method stub
		/*
		logInfo(loginCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = new TopupPaymentApiWS();
		TopupPaymentApiWSPortType service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
		TopupPaymentApiWSLoginResult result = service.loginWS(loginCmd.spID, loginCmd.spPassword, ""+loginCmd.transactionId);
		loginCmd.result = PaymentGWResultCode.R_SUCCESS;
		loginCmd.resultCode=Integer.parseInt(result.getResultcode().getValue());
		loginCmd.resultString=result.getResultDescrib().getValue();
		loginCmd.token=result.getToken().getValue();		
		logInfo(loginCmd.getRespString());
		queueResp.enqueue(loginCmd);
		*/
		logInfo(loginCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = null;
		TopupPaymentApiWSPortType service = null;
		TopupPaymentApiWSLoginResult result = null;
		try {
			topupPaymentApiWS = new TopupPaymentApiWS();
			service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
			result = service.loginWS(loginCmd.spID, loginCmd.spPassword, ""+loginCmd.transactionId);
		} catch (Exception e) {
			// TODO: handle exception
			loginCmd.result = PaymentGWResultCode.R_ERROR;
			loginCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			loginCmd.resultString=e.getMessage();
			logInfo(loginCmd.getRespString());
			queueResp.enqueue(loginCmd);
			return;
		}

		loginCmd.result = PaymentGWResultCode.R_SUCCESS;
		try{
			loginCmd.resultCode=Integer.parseInt(result.getResultcode().getValue());
		}
		catch(Exception e){
			loginCmd.result = PaymentGWResultCode.R_ERROR;
			loginCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			loginCmd.resultString=PaymentGWResultCode.resultDesc.get(PaymentGWResultCode.RC_CALL_SOAP_ERROR);
			logError(loginCmd.getRespString());
			queueResp.enqueue(loginCmd);
			return;
		}
		loginCmd.resultString=result.getResultDescrib().getValue();
		loginCmd.token=result.getToken().getValue();
		logInfo(loginCmd.getRespString());
		queueResp.enqueue(loginCmd);
	}
	
	private void OnKeepAliveCmd(KeepAliveCmd keepAliveCmd) {
		// TODO Auto-generated method stub
		/*
		logInfo(keepAliveCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = new TopupPaymentApiWS();
		TopupPaymentApiWSPortType service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
		TopupPaymentApiWSKeepAliveResult result = service.keepalive(keepAliveCmd.token);
		keepAliveCmd.result = PaymentGWResultCode.R_SUCCESS;
		keepAliveCmd.resultCode=Integer.parseInt(result.getResultcode().getValue());
		keepAliveCmd.resultString=result.getResultDes().getValue();
		logInfo(keepAliveCmd.getRespString());
		queueResp.enqueue(keepAliveCmd);
		*/
		logInfo(keepAliveCmd.getReqString());
		TopupPaymentApiWS topupPaymentApiWS = null;
		TopupPaymentApiWSPortType service = null;
		TopupPaymentApiWSKeepAliveResult result = null;
		try {
			topupPaymentApiWS = new TopupPaymentApiWS();
			service = topupPaymentApiWS.getTopupPaymentApiWSHttpSoap11Endpoint();
			result = service.keepalive(keepAliveCmd.token);
		} catch (Exception e) {
			// TODO: handle exception
			keepAliveCmd.result = PaymentGWResultCode.R_ERROR;
			keepAliveCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			keepAliveCmd.resultString=e.getMessage();
			logError(keepAliveCmd.getRespString());
			queueResp.enqueue(keepAliveCmd);
			return;
		}

		keepAliveCmd.result = PaymentGWResultCode.R_SUCCESS;
		try{
			keepAliveCmd.resultCode=Integer.parseInt(result.getResultcode().getValue());
			keepAliveCmd.resultString=result.getResultDes().isNil()?"NULL value":result.getResultDes().getValue();
		}
		catch(Exception e){
			keepAliveCmd.result = PaymentGWResultCode.R_ERROR;
			keepAliveCmd.resultCode=PaymentGWResultCode.RC_CALL_SOAP_ERROR;
			keepAliveCmd.resultString=PaymentGWResultCode.resultDesc.get(PaymentGWResultCode.RC_CALL_SOAP_ERROR);
			logError(keepAliveCmd.getRespString());
			queueResp.enqueue(keepAliveCmd);
			return;
		}
		
		logInfo(keepAliveCmd.getRespString());
		queueResp.enqueue(keepAliveCmd);
	}
}
