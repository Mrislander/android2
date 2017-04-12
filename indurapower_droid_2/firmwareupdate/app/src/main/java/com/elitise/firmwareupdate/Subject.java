package com.elitise.firmwareupdate;

/**
 * Created by andy on 4/27/16.
 */
public interface Subject {
    public void registerObserver(Observer obs);
    public void removeObserver(Observer obs);
    public void notifyObservers();
}
