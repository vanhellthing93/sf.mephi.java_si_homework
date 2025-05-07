package sf.mephi.study.otp.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.mephi.study.otp.config.AppConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);
    private final String telegramApiUrl;
    private final String chatId;

    public TelegramNotificationService() {
        String botToken = AppConfig.getTelegramBotToken();
        this.chatId = AppConfig.getTelegramChatId();
        this.telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        logger.debug("TelegramNotificationService initialized with chatId: {}", chatId);
    }

    public void sendCode(String destination, String code) {
        String message = String.format("%s, your confirmation code is: %s", destination, code);
        String url = String.format("%s?chat_id=%s&text=%s",
                telegramApiUrl,
                chatId,
                urlEncode(message));

        logger.debug("Sending Telegram message to {}: {}", destination, message);
        sendTelegramRequest(url);
    }

    private void sendTelegramRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    logger.error("Telegram API error. Status code: {}", statusCode);
                } else {
                    logger.info("Telegram message sent successfully");
                }
            }
        } catch (IOException e) {
            logger.error("Error sending Telegram message", e);
        }
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            logger.error("Error encoding URL", e);
            return "";
        }
    }
}