/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.commons.service.aws;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import io.kodokojo.commons.service.EmailSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SesEmailSenderTest {

    public static void main(String[] args) {

        Region region = Region.getRegion(Regions.fromName("eu-west-1"));
        SesEmailSender emailSender = new SesEmailSender("antoine@kodokojo.io", region);

        Set<EmailSender.Attachment> attchments = new HashSet<>();

        EmailSender.PlainTextAttachment<String> textAttachment = new EmailSender.PlainTextAttachment<>("Coucou", "test.txt");
        attchments.add(textAttachment);
        emailSender.send(Collections.singletonList("jpthiery@kodokojo.io"), null, null, "[TEST] SesEmailSender with attachment", getContent(textAttachment.getId()), attchments);


    }

    private static String getContent(String id) {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\"> <!-- utf-8 works for most cases -->\n" +
                "  <meta name=\"viewport\" content=\"width=device-width\"> <!-- Forcing initial-scale shouldn't be necessary -->\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"> <!-- Use the latest (edge) version of IE rendering engine -->\n" +
                "  <title></title> <!-- The title tag shows in email notifications, like Android 4.4. -->\n" +
                "\n" +
                "  <!-- Web Font / @font-face : BEGIN -->\n" +
                "  <!-- NOTE: If web fonts are not required, lines 9 - 26 can be safely removed. -->\n" +
                "\n" +
                "  <!-- Desktop Outlook chokes on web font references and defaults to Times New Roman, so we force a safe fallback font. -->\n" +
                "  <!--[if mso]>\n" +
                "  <style>\n" +
                "    * {\n" +
                "      font-family: sans-serif !important;\n" +
                "    }\n" +
                "  </style>\n" +
                "  <![endif]-->\n" +
                "\n" +
                "  <!-- All other clients get the webfont reference; some will render the font and others will silently fail to the fallbacks. More on that here: http://stylecampaign.com/blog/2015/02/webfont-support-in-email/ -->\n" +
                "  <!--[if !mso]><!-->\n" +
                "  <!-- insert web font reference, eg: <link href='https://fonts.googleapis.com/css?family=Roboto:400,700' rel='stylesheet' type='text/css'> -->\n" +
                "  <!--<![endif]-->\n" +
                "\n" +
                "  <!-- Web Font / @font-face : END -->\n" +
                "\n" +
                "  <!-- CSS Reset -->\n" +
                "  <style type=\"text/css\">\n" +
                "\n" +
                "    /* What it does: Remove spaces around the email design added by some email clients. */\n" +
                "    /* Beware: It can remove the padding / margin and add a background color to the compose a reply window. */\n" +
                "    html,\n" +
                "    body {\n" +
                "      margin: 0 !important;\n" +
                "      padding: 0 !important;\n" +
                "      height: 100% !important;\n" +
                "      width: 100% !important;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: Stops email clients resizing small text. */\n" +
                "    * {\n" +
                "      -ms-text-size-adjust: 100%;\n" +
                "      -webkit-text-size-adjust: 100%;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: Centers email on Android 4.4 */\n" +
                "    div[style*=\"margin: 16px 0\"] {\n" +
                "      margin:0 !important;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: Stops Outlook from adding extra spacing to tables. */\n" +
                "    table,\n" +
                "    td {\n" +
                "      mso-table-lspace: 0pt !important;\n" +
                "      mso-table-rspace: 0pt !important;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: Fixes webkit padding issue. Fix for Yahoo mail table alignment bug. Applies table-layout to the first 2 tables then removes for anything nested deeper. */\n" +
                "    table {\n" +
                "      border-spacing: 0 !important;\n" +
                "      border-collapse: collapse !important;\n" +
                "      table-layout: fixed !important;\n" +
                "      Margin: 0 auto !important;\n" +
                "    }\n" +
                "    table table table {\n" +
                "      table-layout: auto;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: Uses a better rendering method when resizing images in IE. */\n" +
                "    img {\n" +
                "      -ms-interpolation-mode:bicubic;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: Overrides styles added when Yahoo's auto-senses a link. */\n" +
                "    .yshortcuts a {\n" +
                "      border-bottom: none !important;\n" +
                "    }\n" +
                "\n" +
                "    /* What it does: A work-around for iOS meddling in triggered links. */\n" +
                "    .mobile-link--footer a,\n" +
                "    a[x-apple-data-detectors] {\n" +
                "      color:inherit !important;\n" +
                "      text-decoration: underline !important;\n" +
                "    }\n" +
                "  </style>\n" +
                "\n" +
                "  <!-- Progressive Enhancements -->\n" +
                "  <style>\n" +
                "\n" +
                "    /* What it does: Hover styles for buttons */\n" +
                "    .button-td,\n" +
                "    .button-a {\n" +
                "      transition: all 100ms ease-in;\n" +
                "    }\n" +
                "    .button-td:hover,\n" +
                "    .button-a:hover {\n" +
                "      background: #555555 !important;\n" +
                "      border-color: #555555 !important;\n" +
                "    }\n" +
                "\n" +
                "    /* Media Queries */\n" +
                "    @media screen and (max-width: 480px) {\n" +
                "\n" +
                "      /* What it does: Forces elements to resize to the full width of their container. Useful for resizing images beyond their max-width. */\n" +
                "      .fluid,\n" +
                "      .fluid-centered {\n" +
                "        width: 100% !important;\n" +
                "        max-width: 100% !important;\n" +
                "        height: auto !important;\n" +
                "        margin-left: auto !important;\n" +
                "        margin-right: auto !important;\n" +
                "      }\n" +
                "      /* And center justify these ones. */\n" +
                "      .fluid-centered {\n" +
                "        margin-left: auto !important;\n" +
                "        margin-right: auto !important;\n" +
                "      }\n" +
                "\n" +
                "      /* What it does: Forces table cells into full-width rows. */\n" +
                "      .stack-column,\n" +
                "      .stack-column-center {\n" +
                "        display: block !important;\n" +
                "        width: 100% !important;\n" +
                "        max-width: 100% !important;\n" +
                "        direction: ltr !important;\n" +
                "      }\n" +
                "      /* And center justify these ones. */\n" +
                "      .stack-column-center {\n" +
                "        text-align: center !important;\n" +
                "      }\n" +
                "\n" +
                "      /* What it does: Generic utility class for centering. Useful for images, buttons, and nested tables. */\n" +
                "      .center-on-narrow {\n" +
                "        text-align: center !important;\n" +
                "        display: block !important;\n" +
                "        margin-left: auto !important;\n" +
                "        margin-right: auto !important;\n" +
                "        float: none !important;\n" +
                "      }\n" +
                "      table.center-on-narrow {\n" +
                "        display: inline-block !important;\n" +
                "      }\n" +
                "      a.link,\n" +
                "      a.link:hover,\n" +
                "      a.link:visited {\n" +
                "        font-family: sans-serif;\n" +
                "        font-size: 15px;\n" +
                "        mso-height-rule: exactly;\n" +
                "        line-height: 20px;\n" +
                "        color: #4A4A52;\n" +
                "        text-decoration: underline;\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "  </style>\n" +
                "\n" +
                "</head>\n" +
                "<body width=\"100%\" bgcolor=\"#FFF\" style=\"Margin: 0;\">\n" +
                "<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" height=\"100%\" width=\"100%\" bgcolor=\"#4A4A52\" style=\"border-collapse:collapse;\"><tr><td valign=\"top\">\n" +
                "  <tr>\n" +
                "    <td>\n" +
                "      <center style=\"width: 100%;\">\n" +
                "    \n" +
                "        <!-- Visually Hidden Preheader Text : BEGIN -->\n" +
                "        <div style=\"display:none;font-size:1px;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;mso-hide:all;font-family: sans-serif;\">\n" +
                "          Kodo Kojo\n" +
                "        </div>\n" +
                "        <!-- Visually Hidden Preheader Text : END -->\n" +
                "    \n" +
                "        <div style=\"max-width: 680px;\">\n" +
                "          <!--[if (gte mso 9)|(IE)]>\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"680\" align=\"center\">\n" +
                "            <tr>\n" +
                "              <td>\n" +
                "          <![endif]-->\n" +
                "    \n" +
                "    \n" +
                "    \n" +
                "          <!-- Email Body : BEGIN -->\n" +
                "          <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" bgcolor=\"#FFF\" width=\"100%\" style=\"max-width: 680px;\">\n" +
                "    \n" +
                "            <!-- Hero Image, Flush : BEGIN -->\n" +
                "            <tr>\n" +
                "              <td>\n" +
                "                <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" bgcolor=\"#3A3A3D\" width=\"100%\" height=\"70px\" style=\"max-width: 680px;\">\n" +
                "                  <tr>\n" +
                "                    <td style=\"height: 110px; background-color: #3A3A3D;\">\n" +
                "                      <img src=\"http://blog.xebia.fr/wp-content/uploads/2016/10/logo-white-kodokojo-baseline-simplified.png\" alt=\"Kodo Kojo logo\" style=\"border:none; margin-left: 20px;\" width=\"180\" />\n" +
                "                      <div style=\"width: 400px; text-align: right; color: #fff; font-family:myriad pro, Arial, Helvetica, sans-serif; font-size: 18px; float:right; padding-right: 20px; padding-top: 20px\">\n" +
                "                        %userName% account details\n" +
                "                      </div>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                  <tr>\n" +
                "                    <td style=\"height: 10px; background-color: #60DADF\"></td>\n" +
                "                  </tr>\n" +
                "                </table>\n" +
                "    \n" +
                "              </td>\n" +
                "            </tr>\n" +
                "    \n" +
                "            <!-- CONTENT -->\n" +

                "            <tr>\n" +
                "              <td>\n" +
                "                <table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">\n" +
                "                  <tr>\n" +
                "                    <td style=\"padding: 15px 15px 0 15px; text-align: left; font-family: sans-serif; font-size: 15px; mso-height-rule: exactly; line-height: 20px; color: #4A4A52;\">\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\"><b>Welcome to Kodo Kojo, we are very excited to have you on board!</b></p>\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\">\n" +
                "                        Tanks to subscribe to our SaaS service, we hope you will enjoy it!\n" +
                "                        Feel free to report any suggestion and problem to <a href=\"mailto:support@kodokojo.io\" target=\"_blank\" class=\"link\" style=\"font-family: sans-serif; font-size: 15px; mso-height-rule: exactly; line-height: 20px; color: #4A4A52; text-decoration: underline;\">support@kodokojo.io</a>\n" +
                "                        You can also <a href=\"https://gitter.im/kodokojo/kodokojo\" target=\"_blank\" class=\"link\" style=\"font-family: sans-serif; font-size: 15px; mso-height-rule: exactly; line-height: 20px; color: #4A4A52; text-decoration: underline;\">chat with us directly on our gitter</a>.\n" +
                "                        For any further informations, please visite <a href=\"https://kodokojo.io\" target=\"_blank\" class=\"link\" style=\"font-family: sans-serif; font-size: 15px; mso-height-rule: exactly; line-height: 20px; color: #4A4A52; text-decoration: underline;\">kodokojo.io</a>\n" +
                "                      </p>\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\">\n" +
                "                        Here are all your informations binded to your account <span style=\"color: #4A4A52; font-weight: bold\">%userName%</span>.<br/>\n" +
                "                        You can change details by editting your user informations on <a href=\"https://my.kodokojo.io/members\" target=\"_blank\" class=\"link\" style=\"font-family: sans-serif; font-size: 15px; mso-height-rule: exactly; line-height: 20px; color: #4A4A52; text-decoration: underline;\">the members page</a>.\n" +
                "                      </p>\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\">\n" +
                "                        <b>Your user name:</b><br/>\n" +
                "                        %userName%\n" +
                "                      </p>\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\">\n" +
                "                        <b>Your password:</b><br/>\n" +
                "                        %k4rouivetpo139ap4a006ah143%\n" +
                "                      </p>\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\">\n" +
                "                        <b>Your generated SSH private key:</b><br/>\n" +
                "                        %-----BEGIN RSA PRIVATE KEY-----<br/>\n" +
                "                        MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCIiTddCskXUNi7<br/>RCcgnZGGcfqskOmLeL2pQc7W9wXrsbBDJz74J78LKWT+HvMVj+A8fKEVzh3Ftc6I<br/>lQFyQClXXnuhFY+nyoy9HDKxAw1SyesqE21Sb++ih7mOFU7UMGv2UCfKxVvOPFxR<br/>2Okjyar6CES9jG6u6quorwaBu9mUty0vej61PAQL65Txd8BTaGlKCTNEXLSFmvnp<br/>\n" +
                "                        pBIRvNBCRxgzJnRDly/afedmw+5KGXTxXZMSexGa9oe9fszCzvg0y8o17HUs/sW4<br/>\n" +
                "                        OcZUeKHHplVTR+urZx7rSL6KjJ/hAH9TXlAx6JcIwrdHrVxz2SRTpLPWZQm5qzlb<br/>5OOqvFr1AgMBAAECggEAVCkorDxyueGGxt/6slsOEe5+ExL8MpGJbyR2aAE6cA9G<br/>\n" +
                "                        VA6D/Rka5LluXEodksPt97rm3HcB2RX5Ki2XB6LPGODPmfqdY1MyL3uLL5tvAgIe<br/>\n" +
                "                        5/+zmmYPM4Mv+Inf3mG+msTL0myW62g/i+AFzZ6IuriQDrramw1iJIdGnn95XfYe<br/>nT7Yk4I6JmaAu+DpbEGIZV2WoNXJnXK2EXFXzxkaMmdKjbsE0SBQCLUDaVZmn9bx<br/>\n" +
                "                        BF51LL3a2aYTvFpb0ER6dWWSWIc9FLVzz10dEDDWvJoU6Z+SwS6/UUv/EJpp+DFy<br/>\n" +
                "                        ZDAajzYqZy6KficP8RoCEnPGHQAPgAo+siETK6qj3QKBgQD78/FQ0awQubWKOEH4<br/>Oyz5sSUgtz17ZYDw3t3sX70o3tvEQxKZdWwCdqlI88eKYcCn3DcKAmJmhtJlVjRX<br/>KYkQQ43csDXfOHXen5MvU9svWVvZOIW7XAraZRsBeWLvhsCIdN+usE+YNAJ3s6SH<br/>4UJehmIjPnZTKkIM8vcMBn63pwKBgQCKuqq9JEPwZu+FkNnvoXtAtar0yH98vAYq<br/>\n" +
                "                        TFkG7QXjQHRQbUns/e47e8p0Fvjq8yRHtCBfTnJqvCAlWpWY7JKMd5AfJ2T3YdcR<br/>TxSjJhFkIAxB60piI+QbTq0jLEn5CQfosGH7gPjVerA672L83OXtpyZPd+9x72AO<br/>\n" +
                "                        WcJuaMSsAwKBgQCji9TP/lpvvOyfnScNZ/Qo3JlaJDfvmpLZSAHMRtU163vCaTtw<br/>i60h6D640S1soUl6bNL5V1Tico+uIgf1sEt9WCyE3YkKrc6tRO44oXk8wgeB+FOu<br/>q++LlmeyTEYxb0oZCayM63uvM8uKQf0CCvGXBCo98HTERUD8wNYmYyzsLwKBgQCC<br/>\n" +
                "                        pJlnd0B3oyhLOwbRcNvWK36r+Ch+ub6AlNd9+zYBNVCT8OeeQ9/WqpQUURHmiESR<br/>\n" +
                "                        NeMKfaCoPTN0meKpWZgEqg/SFtIxWTUkurkvjwjvpnKEnWS2GSCWSrgnmGytFkEZ<br/>\n" +
                "                        cwlCxMkQmPJe/dLVV907uZ4NVl/qhseJnCPKv+T+KwKBgQDLVeG47p69g5nLBR2K<br/>\n" +
                "                        yO8CUXInsucTtjEx3d1GeHoooC/UjOLo8+aZ2DdGgRSbfjnPZQ7dNG8uZRDAiHjv<br/>W9ul87GGI4lGqbpzlVsSk8h5STeKWXcIbrnEOPBt6iRBSFT/7VdQIBQstJ/3qm05<br/>\n" +
                "                        n57rpPmkEe4QCTx6MtLxzjRLPg\\u003d\\u003d<br/>\n" +
                "                        -----END RSA PRIVATE KEY-----%<br/>\n" +
                "                      </p>\n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word;\">\n" +
                "                        <b>Your generated SSH public key:</b><br/>\n" +
                "                        %ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCDlnFiOX5toTCA5qnGQ5gePwn+gif6Pef/Cn038H6+gG0dsQtG8qMKrqbLTA5kKbr9gZFABAPGf6WWRSYuVGHfsSXkUfPIS/vv2zLxZhe6kmpUEIElt5m4XjZe1aTwQn1ZLkq1w5CG94+o2FZZQcHSaS2eAISXs498l6n4jpuT3TDFIK7LkmT18FX9M7KDljHFeATNNTVXu7yIKAG240inB5wIh3n7gtkbSN2eR+5LdrFh7Gezq2RmFv4f2rk3QXWR2w7zHT7XRmVb6RArY3+GefoUfSaBSyELTOUWOHbOlhP8JA/blLPqPtyXl2ZN2udDNhkWeYtg8Yq0lyrUXLrt antouam@gmail.com%\n" +
                "                      </p>\n" +
                "    \n" +
                "    \n" +
                "                      <p style=\"word-wrap: break-word; color: #4A4A52;\">\n" +
                "                        You can now continue building great things on your brand new software factory!<br/>\n" +
                "                        <b>--</b><br/>\n" +
                "                        <b>The Kodo Kojo Team</b>\n" +
                "    \n" +
                "                      </p>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </table>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "          </table>\n" +
                "          <!-- FOOTER -->\n" +
                "    \n" +
                "          <table width=\"100%\" style=\"max-width: 680px; margin: 0px \" cellpadding=\"0px\" cellspacing=\"0px\">\n" +
                "            <tbody>\n" +
                "            <tr>\n" +
                "              <td colspan=\"2\" bgcolor=\"#FFF\">\n" +
                "                <div class=\"kodo-kojo-logo\" style=\"text-align:center; padding-bottom: 20px\">\n" +
                "                  <a href=\"https://kodokojo.io/\" target=\"_blank\"><img src=\"http://blog.xebia.fr/wp-content/uploads/2016/10/logo-black-kodokojo-baseline-simplified.png\" alt=\"Kodo Kojo logo\" style=\"border:none;\" width=\"180\" /></a>\n" +
                "                </div>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "              <td colspan=\"2\" bgcolor=\"#60DADF\" style=\"height: 10px; background-color: #60DADF\"></td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "              <td colspan=\"2\" bgcolor=\"#3A3A3D\" align=\"center\" style=\"color: #ffffff; height:60px; background-color: #3A3A3D;\">\n" +
                "                <a style=\"color:#dadae5; text-align:center; font-family:myriad pro, Arial, Helvetica, sans-serif; font-size:16px; text-decoration:none; \" target=\"_blank\" href=\"https://kodokojo.io\"> &gt;| kodokojo.io |&lt; </a>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "            </tbody>\n" +
                "          </table>\n" +
                "          </div>\n" +
                "        </center>\n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </table>\n" +
                "</body>\n" +
                "</html>\n";

        return html;
    }

}