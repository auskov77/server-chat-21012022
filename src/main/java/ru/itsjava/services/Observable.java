package ru.itsjava.services;

public interface Observable {
    void addObserver(Observer observer);
    void deleteObserver(Observer observer);
    void notifyObservers(String message);
    void notifyObserverExceptMe(String message, Observer observer);
}
