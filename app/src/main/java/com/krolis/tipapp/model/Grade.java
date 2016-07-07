package com.krolis.tipapp.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Krolis on 2016-05-23.
 */
public class Grade implements Serializable{
    private String subject;
    private Map<String, String> notes;

    public Grade(String subject, Map<String, String> notes) {
        this.subject = subject;
        this.notes = notes;
    }

    public String getSubject() {
        return subject;
    }

    public Map<String, String> getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(subject);
        builder.append(":");
        for(Map.Entry<String, String> entry : notes.entrySet()) {
            builder.append(" ");
            builder.append(entry.getValue());
            builder.append(" ");
            builder.append(entry.getKey());
        }
        if(notes == null || notes.isEmpty()){
            builder.append("brak oceny");
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        int result  = 17;
        result = 31 * result + subject.hashCode();
        result = 31 * result + notes.hashCode();

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Grade){
            Grade g = (Grade) obj;
            if(this.subject.equals(g.subject) && this.notes.equals(g.notes))
                return true;
            else
                return false;
        }
        return super.equals(obj);
    }

}
