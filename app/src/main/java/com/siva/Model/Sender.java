package com.siva.Model;

import android.provider.ContactsContract;

import java.security.PublicKey;

/**
 * Created by MANIKANDAN on 24-12-2017.
 */

public class Sender {

   public String to;
    public Notification notification;

    public Sender() {
    }

    public Sender(String to, Notification notification) {
        this.to = to;
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
