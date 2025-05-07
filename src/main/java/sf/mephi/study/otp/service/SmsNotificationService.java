package sf.mephi.study.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.SubmitSM;
import sf.mephi.study.otp.config.AppConfig;
import org.smpp.Connection;
import org.smpp.Session;

public class SmsNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsNotificationService() {
        this.host = AppConfig.getSmppHost();
        this.port = AppConfig.getSmppPort();
        this.systemId = AppConfig.getSmppSystemId();
        this.password = AppConfig.getSmppPassword();
        this.systemType = AppConfig.getSmppSystemType();
        this.sourceAddress = AppConfig.getSmppSourceAddr();
        logger.debug("SmsNotificationService initialized with host: {}, port: {}", host, port);
    }

    public void sendCode(String destination, String code) {
        Connection connection = null;
        Session session = null;

        try {
            // 1. Установка соединения
            connection = new TCPIPConnection(host, port);
            session = new Session(connection);
            // 2. Подготовка Bind Request
            BindTransmitter bindRequest = new BindTransmitter();
            bindRequest.setSystemId(systemId);
            bindRequest.setPassword(password);
            bindRequest.setSystemType(systemType);
            bindRequest.setInterfaceVersion((byte) 0x34); // SMPP v3.4
            bindRequest.setAddressRange(sourceAddress);
            // 3. Выполнение привязки
            BindResponse bindResponse = session.bind(bindRequest);
            if (bindResponse.getCommandStatus() != 0) {
                throw new Exception("Bind failed: " + bindResponse.getCommandStatus());
            }
            // 4. Отправка сообщения
            SubmitSM submitSM = new SubmitSM();
            submitSM.setSourceAddr(sourceAddress);
            submitSM.setDestAddr(destination);
            submitSM.setShortMessage("Your code: " + code);

            session.submit(submitSM);
            logSuccess(destination);
        } catch (Exception e) {
            handleError("Failed to send SMS to " + destination, e);
        } finally {
            try {
                if (session != null) {
                    session.unbind();
                }
            } catch (Exception e) {
                logger.error("Failed to unbind session", e);
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.error("Failed to close connection", e);
            }
        }
    }

    private void logSuccess(String destination) {
        logger.info("SMS sent successfully to {}", destination);
    }

    private void handleError(String message, Exception e) {
        logger.error(message, e);
    }
}
