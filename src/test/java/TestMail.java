package test.java;

import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import com.gdxsoft.easyweb.utils.Mail.Attachment;
import com.gdxsoft.easyweb.utils.Mail.MailDecode;
import org.junit.Test;

public class TestMail extends TestBase {
	/**
	 * PraseMimeMessage类测试
	 */
	public static void main(String args[]) throws Exception {
		TestMail t = new TestMail();
		t.testMail();
	}

	@Test
	public void testMail() throws Exception {
		String host = null; //
		String username = null;
		String password = null;

		super.printCaption("读取pop3邮件");
		this.readPop3Mails(host, username, password);
	}

	private void readPop3Mails(String host, String username, String password) throws Exception {

		if (host == null || username == null || password == null) {
			super.captionLength("skip test");
			return;
		}

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("pop3");
		store.connect(host, username, password);
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message message[] = folder.getMessages();
		System.out.println("Messages's　length:　" + message.length);
		MailDecode pmm = null;
		for (int i = 0; i < message.length; i++) {
			pmm = new MailDecode((MimeMessage) message[i], "D:\\image");
			System.out.println("Message　" + i + "　subject:　" + pmm.getSubject());
			System.out.println("Message　" + i + "　sentdate:　" + pmm.getSentDate());
			System.out.println("Message　" + i + "　replysign:　" + pmm.getReplySign());
			System.out.println("Message　新的" + i + "　hasRead:　" + pmm.isNew());
			System.out.println("Message　附件" + i + "　　containAttachment:　" + pmm.isContainAttach((Part) message[i]));
			System.out.println("Message　" + i + "　form:　" + pmm.getFrom());
			System.out.println("Message　" + i + "　to:　" + pmm.getMailAddress("to"));
			System.out.println("Message　" + i + "　cc:　" + pmm.getMailAddress("cc"));
			System.out.println("Message　" + i + "　bcc:　" + pmm.getMailAddress("bcc"));
			System.out.println("Message" + i + "　sentdate:　" + pmm.getSentDate());
			System.out.println("Message　" + i + "　Message-ID:　" + pmm.getMessageId());
			System.out.println("Message　正文" + i + "　bodycontent:　\r\n" + pmm.getBodyText());

			pmm.saveAttachments();

			List<Attachment> atts = pmm.getAtts();
			for (int k = 0; k < atts.size(); k++) {
				Attachment att = atts.get(k);
				System.out.println(att.toString());
			}
		}
	}

}
