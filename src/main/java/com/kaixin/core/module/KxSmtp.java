package com.kaixin.core.module;

import com.kaixin.core.app.KxApp;
import com.kaixin.core.util.PropsKeys;
import com.kaixin.core.util.PropsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/*
 * 对SMTP协议的封装，实现邮件发送
 */

public class KxSmtp {

	private Session mailSession = null; 
	private String mailFromAddr = "";
	
	private static final Logger log = LoggerFactory.getLogger(KxSmtp.class);
			
			
	public KxSmtp() {

		try {
			Properties props = new Properties();
	        
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.host", PropsUtil.get(PropsKeys.SMTP_HOST));
	        props.put("mail.smtp.port", PropsUtil.get(PropsKeys.SMTP_PORT));
	        props.put("mail.smtp.timeout", "5000");
	
	        /* 新的javamail这样初始化ssl就可以了 */
	        if (PropsUtil.getBoolean(PropsKeys.SMTP_SSL))
	        	props.put("mail.smtp.ssl.enable", "true");
	        
	        mailFromAddr = PropsUtil.get(PropsKeys.SMTP_USERNAME);
			mailSession = Session.getInstance(props,
	          new javax.mail.Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication(PropsUtil.get(PropsKeys.SMTP_USERNAME), PropsUtil.get(PropsKeys.SMTP_PASSWORD));
	            }
	          });
		}catch(Exception e) {
			log.error("init smtp error:", e);
		}
	}
	
	public void sendAsync(final String to, final String cc, final String bcc,final String subject, final String content) {
		KxApp.executor.submit(new Runnable() {
		    public void run() {
		        send(to, cc, bcc, subject, content);
		    }
		});
	}

	public void send(String to, String cc, String bcc, String subject, String content) {
		try {
			sendImpl(to,cc,bcc,subject,content);
		} catch (Exception e) {
			log.error("send mail failed:" , e);
		}
	}


	public void sendImpl(String to, String cc, String bcc, String subject, String content) throws Exception {
		if(mailSession == null)
			return;
		
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(mailFromAddr));
        if (to != null)
        	message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to));
        if (cc != null)
        	message.setRecipients(Message.RecipientType.CC,InternetAddress.parse(cc));
        if (bcc != null)
        	message.setRecipients(Message.RecipientType.BCC,InternetAddress.parse(bcc));


        message.setSubject(subject == null ? "" : subject);
        
        if (content != null)
			message.setContent(content, "text/html; charset=utf-8");

        Transport.send(message);
	}
	
}
