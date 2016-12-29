package com.pattern.observer;

/**
 * Created by rensong.pu on 2016/12/29.
 */
public interface Subject {
    void add(Observer observer);
    void del(Observer observer);
    void notifyObs();
}
