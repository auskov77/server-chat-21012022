package ru.itsjava.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itsjava.dao.UserDao;
import ru.itsjava.domain.User;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@RequiredArgsConstructor
public class ClientRunnable implements Runnable, Observer {
    private final Socket socket;
    private final ServerService serverService;
    private User user;
    private final UserDao userDao;
//    public int countCheck = 0;

    @SneakyThrows
    @Override
    public void run() {
        System.out.println("Client connected");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        String messageFromClient;

        if (authentication(bufferedReader)) { // проверка, устанавливающая подлинность лица, получающего доступ, путем сопоставления сообщенного им идентификатора и предъявленного подтверждающего фактора
            serverService.addObserver(this);
            notifyObserverCheck();
        }
    }

    @SneakyThrows
    private boolean authentication(BufferedReader bufferedReader) {
        String messageFromClient;
        while ((messageFromClient = bufferedReader.readLine()) != null) {
            // авторизация существующего пользователя в БД
            //!autho!login:password
            if (messageFromClient.startsWith("!autho!")) {
                String login = messageFromClient.substring(7).split(":")[0];
                String password = messageFromClient.substring(7).split(":")[1];
                user = userDao.findByNameAndPassword(login, password);
                if (user != null) {
                    notifyMe("autho");
                    return true;
                } else {
                    notifyMe("not autho");
                    reauthorization(bufferedReader); // повторная авторизация
                }
            }
            // регистрация нового пользователя в БД
            //!reg!login:password
            else if (messageFromClient.startsWith("!reg!")) {
                String login = messageFromClient.substring(5).split(":")[0];
                String password = messageFromClient.substring(5).split(":")[1];
                user = userDao.createNewUser(login, password);
                notifyMe("reg");
                return true;
            }
        }
            return false;
    }


    @SneakyThrows
    @Override
    public void notifyMe(String message) {
        PrintWriter clientWriter = new PrintWriter(socket.getOutputStream());
        clientWriter.println(message);
        clientWriter.flush();
    }

    @SneakyThrows
    private void notifyObserverCheck() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String messageFromClient;
        while ((messageFromClient = bufferedReader.readLine()) != null) {
            System.out.println(user.getName() + ":" + messageFromClient);
//                serverService.notifyObservers(user.getName() + ":" + messageFromClient);
            serverService.notifyObserverExceptMe(user.getName() + ":" + messageFromClient, this);
        }
    }
    
    @SneakyThrows
    private boolean reauthorization(BufferedReader bufferedReader){
        String reauthorizationMessage;
        while ((reauthorizationMessage = bufferedReader.readLine()) != null) {
            //!autho!login:password
            if (reauthorizationMessage.startsWith("!autho!")) {
                String login = reauthorizationMessage.substring(7).split(":")[0];
                String password = reauthorizationMessage.substring(7).split(":")[1];
                user = userDao.createNewUser(login, password);
                notifyMe("autho");
                return true;
            }
        }
        return false;        
    }
}
