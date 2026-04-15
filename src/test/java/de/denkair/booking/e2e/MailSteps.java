package de.denkair.booking.e2e;

import com.icegreen.greenmail.util.GreenMail;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Then;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Steps that assert on mails captured by the GreenMail SMTP server started
 * in CucumberSpringConfig. Scenarios that book a flight via HttpSteps or
 * BrowserSteps will cause MailService to send through this in-memory SMTP.
 */
public class MailSteps {

    @Autowired private GreenMail greenMail;

    @Before
    public void purgeInboxBeforeScenario() {
        // Fresh mailbox per scenario — avoids cross-test bleed.
        try {
            greenMail.purgeEmailFromAllMailboxes();
        } catch (Exception ignored) { }
    }

    /**
     * After each scenario, dump every captured MimeMessage to target/e2e-mails/
     * as .eml (openable in any mail client) AND refresh an index.html summary
     * of every .eml in the directory — opens cleanly in any browser.
     */
    @After
    public void dumpMailsToDisk(Scenario scenario) {
        MimeMessage[] mails = greenMail.getReceivedMessages();
        if (mails.length == 0) return;
        File dir = new File("target/e2e-mails");
        dir.mkdirs();
        String safe = scenario.getName().replaceAll("[^a-zA-Z0-9]+", "_");
        for (int i = 0; i < mails.length; i++) {
            File out = new File(dir, safe + "_" + (i + 1) + ".eml");
            try (FileOutputStream fos = new FileOutputStream(out)) {
                mails[i].writeTo(fos);
            } catch (Exception e) {
                System.err.println("[mail-dump] failed to write " + out + ": " + e.getMessage());
            }
        }
        rebuildIndexHtml(dir);
    }

    private static void rebuildIndexHtml(File dir) {
        File[] eml = dir.listFiles((d, n) -> n.endsWith(".eml"));
        if (eml == null) return;
        java.util.Arrays.sort(eml, java.util.Comparator.comparing(File::getName));

        StringBuilder h = new StringBuilder();
        h.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
         .append("<title>DenkAir E2E captured emails</title>")
         .append("<style>")
         .append("body{font:14px/1.45 -apple-system,system-ui,sans-serif;max-width:980px;margin:32px auto;padding:0 20px;color:#1f2937}")
         .append("h1{font-size:22px;margin:0 0 4px}")
         .append(".sub{color:#6b7280;margin:0 0 28px}")
         .append(".mail{border:1px solid #e5e7eb;border-radius:10px;margin:18px 0;overflow:hidden}")
         .append(".hdr{background:#f3f4f6;padding:12px 16px;border-bottom:1px solid #e5e7eb}")
         .append(".hdr .scn{font-weight:600;color:#111827}")
         .append(".hdr .meta{color:#6b7280;font-size:12.5px;margin-top:4px;font-family:ui-monospace,monospace}")
         .append(".body{padding:16px;background:#fff;white-space:pre-wrap;font-family:ui-monospace,Menlo,monospace;font-size:13px}")
         .append(".tag{display:inline-block;background:#dbeafe;color:#1e40af;font-size:11px;padding:2px 7px;border-radius:10px;margin-right:6px;font-family:ui-monospace,monospace}")
         .append("</style></head><body>")
         .append("<h1>DenkAir — Booking Confirmation Emails</h1>")
         .append("<p class=\"sub\">Captured by GreenMail (127.0.0.1:3025) during the last <code>make e2e</code> run · ")
         .append(eml.length).append(" message(s)</p>");

        for (File f : eml) {
            try {
                javax.mail.Session s = javax.mail.Session.getInstance(new java.util.Properties());
                MimeMessage m = new MimeMessage(s, new java.io.FileInputStream(f));
                String from = m.getFrom() != null && m.getFrom().length > 0 ? m.getFrom()[0].toString() : "";
                String to = String.join(", ",
                        java.util.Arrays.stream(m.getAllRecipients() == null ? new javax.mail.Address[0] : m.getAllRecipients())
                                .map(Object::toString).toArray(String[]::new));
                String subject = m.getSubject() == null ? "" : m.getSubject();
                Object content = m.getContent();
                String body = content == null ? "" : content.toString();

                h.append("<div class=\"mail\"><div class=\"hdr\">")
                 .append("<div class=\"scn\">").append(escape(f.getName())).append("</div>")
                 .append("<div class=\"meta\">")
                 .append("<span class=\"tag\">From</span>").append(escape(from))
                 .append(" <span class=\"tag\">To</span>").append(escape(to))
                 .append("<br><span class=\"tag\">Subject</span>").append(escape(subject))
                 .append("</div></div>")
                 .append("<div class=\"body\">").append(escape(body)).append("</div>")
                 .append("</div>");
            } catch (Exception e) {
                h.append("<div class=\"mail\"><div class=\"body\">failed to parse ")
                 .append(escape(f.getName())).append(": ").append(escape(e.getMessage()))
                 .append("</div></div>");
            }
        }
        h.append("</body></html>");

        try (FileOutputStream fos = new FileOutputStream(new File(dir, "index.html"))) {
            fos.write(h.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("[mail-dump] failed to write index.html: " + e.getMessage());
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Then("the inbox for {string} has {int} message(s)")
    public void inboxHasNMessages(String address, int expected) throws Exception {
        waitForIncoming(expected);
        MimeMessage[] mails = greenMail.getReceivedMessagesForDomain(addressDomain(address));
        long count = 0;
        for (MimeMessage m : mails) {
            for (javax.mail.Address to : m.getAllRecipients()) {
                if (to.toString().equalsIgnoreCase(address)) { count++; break; }
            }
        }
        assertEquals(expected, (int) count,
                "expected " + expected + " mail(s) for " + address + ", got " + count);
    }

    @Then("the latest mail to {string} has subject containing {string}")
    public void latestSubjectContains(String address, String needle) throws Exception {
        MimeMessage m = latestFor(address);
        assertNotNull(m, "no mail found for " + address);
        String subject = m.getSubject();
        assertTrue(subject != null && subject.contains(needle),
                "subject='" + subject + "' did not contain '" + needle + "'");
    }

    @Then("the latest mail to {string} body contains {string}")
    public void latestBodyContains(String address, String needle) throws Exception {
        MimeMessage m = latestFor(address);
        assertNotNull(m, "no mail found for " + address);
        String body = m.getContent().toString();
        assertTrue(body.contains(needle),
                "body did not contain '" + needle + "' — actual: "
                        + body.substring(0, Math.min(body.length(), 500)));
    }

    @Then("the latest mail to {string} is from {string}")
    public void latestFromIs(String address, String expectedFrom) throws Exception {
        MimeMessage m = latestFor(address);
        assertNotNull(m, "no mail found for " + address);
        String from = m.getFrom()[0].toString();
        assertTrue(from.equalsIgnoreCase(expectedFrom),
                "from='" + from + "' expected '" + expectedFrom + "'");
    }

    private MimeMessage latestFor(String address) throws Exception {
        waitForIncoming(1);
        MimeMessage latest = null;
        for (MimeMessage m : greenMail.getReceivedMessages()) {
            for (javax.mail.Address to : m.getAllRecipients()) {
                if (to.toString().equalsIgnoreCase(address)) latest = m;
            }
        }
        return latest;
    }

    private void waitForIncoming(int min) {
        // GreenMail 1.6 signature: waitForIncomingEmail(long timeout, int emailCount) : boolean
        greenMail.waitForIncomingEmail(TimeUnit.SECONDS.toMillis(5), min);
    }

    private static String addressDomain(String addr) {
        int at = addr.indexOf('@');
        return at < 0 ? addr : addr.substring(at + 1);
    }
}
