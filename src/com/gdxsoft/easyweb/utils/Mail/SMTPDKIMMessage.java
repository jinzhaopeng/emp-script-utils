/* 
 * Copyright 2008 The Apache Software Foundation or its licensors, as
 * applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * A licence was granted to the ASF by Florian Sager on 30 November 2008
 */

package com.gdxsoft.easyweb.utils.Mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.util.LineOutputStream;

/*
 * Extension of SMTPMessage for the inclusion of a DKIM signature.
 * 
 * @author Florian Sager, http://www.agitos.de, 22.11.2008
 */

public class SMTPDKIMMessage extends SMTPMessage {

	private DKIMSigner signer;
	private String encodedBody;

	public SMTPDKIMMessage(Session session, DKIMSigner signer) {
		super(session);
		this.signer = signer;
	}
	
	public SMTPDKIMMessage(MimeMessage message, DKIMSigner signer) throws MessagingException {
		super(message);
		this.signer = signer;
	}

	public SMTPDKIMMessage(Session session, InputStream is, DKIMSigner signer) throws MessagingException {
		super(session, is);
		this.signer = signer;
	}

	/**
	 * Output the message as an RFC 822 format stream, without
	 * specified headers.  If the <code>saved</code> flag is not set,
	 * the <code>saveChanges</code> method is called.
	 * If the <code>modified</code> flag is not
	 * set and the <code>content</code> array is not null, the
	 * <code>content</code> array is written directly, after
	 * writing the appropriate message headers.
	 *
	 * @exception javax.mail.MessagingException
	 * @exception IOException	if an error occurs writing to the stream
	 *				or if an error is generated by the
	 *				javax.activation layer.
	 * @see javax.activation.DataHandler#writeTo
	 * 
	 * This method enhances the JavaMail method MimeMessage.writeTo(OutputStream os String[] ignoreList);
	 * See the according Sun Licence, this contribution is CDDL. 
	 */
	public void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {

		ByteArrayOutputStream osBody = new ByteArrayOutputStream();

		// Inside saveChanges() it is assured that content encodings are set in all parts of the body
		if (!saved) {
			saveChanges();
		}

		// First, write out the body to the body buffer
		if (modified) {
			// Finally, the content. Encode if required.
			// XXX: May need to account for ESMTP ?
			OutputStream osEncoding = MimeUtility.encode(osBody, this.getEncoding());
			this.getDataHandler().writeTo(osEncoding);
			osEncoding.flush(); // Needed to complete encoding
		} else {
			// Else, the content is untouched, so we can just output it
			// Finally, the content. 
			if (content == null) {
				// call getContentStream to give subclass a chance to
				// provide the data on demand
				InputStream is = getContentStream();
				// now copy the data to the output stream
				byte[] buf = new byte[8192];
				int len;
				while ((len = is.read(buf)) > 0)
					osBody.write(buf, 0, len);
				is.close();
				buf = null;
			} else {
				osBody.write(content);
			}
			osBody.flush();
		}
		encodedBody = osBody.toString();

		// Second, sign the message
		String signatureHeaderLine;
		try {
			signatureHeaderLine = signer.sign(this);
		} catch (Exception e) {
			throw new MessagingException(e.getLocalizedMessage(), e);
		}

		// Third, write out the header to the header buffer
		LineOutputStream los = new LineOutputStream(os);
		
		// set generated signature to the top 
		los.writeln(signatureHeaderLine);

		Enumeration<?> hdrLines = getNonMatchingHeaderLines(ignoreList);
		while (hdrLines.hasMoreElements()) {
			los.writeln((String) hdrLines.nextElement());
		}

		// The CRLF separator between header and content
		los.writeln();

		// Send signed mail to waiting DATA command
		os.write(osBody.toByteArray());
		os.flush();
	}

	public String getEncodedBody() {
		return encodedBody;
	}

	public void setEncodedBody(String encodedBody) {
		this.encodedBody = encodedBody;
	}

	// Don't allow to switch to 8-bit MIME, instead 7-bit ascii should be kept
	// 'cause in forwarding scenarios a change to Content-Transfer-Encoding
	// to 7-bit ascii breaks DKIM signatures
	public void setAllow8bitMIME(boolean allow) {
    	// super.setAllow8bitMIME(false);
    }
}
