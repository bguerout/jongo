package org.jongo.model;

import java.util.Date;

import org.jongo.marshall.jackson.oid.Id;

/**
 * A class that is shaped like results from map reduce operations.
 * 
 * @author Christian Trimble
 */
public class MapReduceData {

    public static class Key {
        protected String group;
        protected Date date;

        public Key() {
        }

        public Key(String group, Date date) {
            this.group = group;
            this.date = date;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    public static class Value {
        protected int count;

        public Value() {
        }

        public Value(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    @Id
    protected Key id;
    protected Value value;

    public MapReduceData() {
    }

    public MapReduceData(String group, Date date, int count) {
        this.id = new Key(group, date);
        this.value = new Value(count);
    }

    public Key getId() {
        return id;
    }

    public void setId(Key id) {
        this.id = id;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
