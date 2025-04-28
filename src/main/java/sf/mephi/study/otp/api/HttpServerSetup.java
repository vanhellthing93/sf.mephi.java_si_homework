package sf.mephi.study.otp.api;

import sf.mephi.study.otp.api.controller.UserController;
import sf.mephi.study.otp.dao.UserDAO;
import sf.mephi.study.otp.service.UserService;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerSetup {

    public static void main(String[] args) throws IOException {
        // Создаем DAO, сервисы и контроллеры
        UserDAO userDAO = new UserDAO();
        UserService userService = new UserService(userDAO);
        UserController userController = new UserController(userService);

        // Создаем HTTP-сервер
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Настраиваем обработчики для различных эндпоинтов
        server.createContext("/register", userController.registerUserHandler());
        server.createContext("/getUser", userController.getUserHandler());

        // Запускаем сервер
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8080");
    }
}