package org.chenxinwen.micontacts.bean;

/**
 * Created by chenxinwen on 16/8/9.11:02.
 * Email:191205292@qq.com
 */

public class Contacts implements Comparable<Contacts> {

    private String sortKey;
    private String name="";
    private int id;
    private String url;
    private String pinyin;
    private String number="";
    private String email ="";
    private String tag ="";
    private int blackList = 0;
    private char firstChar;
    public Contacts()
    {
        sortKey = " ";
        name=" ";
        id = 0;
        url = "noImg";
        pinyin = "";
        number = " ";
        email =" ";
        tag =" ";
        blackList = 0;
        firstChar = '#';
    }
    public String getEmail(){return email;}
    public String getTag(){return tag;}
    public int getBlackList(){return blackList;}

    public  void setEmail(String email){this.email = email;}
    public  void setTag(String tag){this.tag = tag;}
    public  void setBlackList(int blackList){this.blackList = blackList;}


    public String getNumber()
    {
        return number;
    }
    public String getPinyin() {
        return pinyin;
    }
    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }
    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
        String first = pinyin.substring(0, 1);
        if (first.matches("[A-Za-z]")) {
            firstChar = first.toUpperCase().charAt(0);
        } else {
            firstChar = '#';
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public char getFirstChar() {
        return firstChar;
    }

    @Override
    public int compareTo(Contacts another) {

        //return this.pinyin.compareTo(another.getPinyin());
        return this.getSortKey().compareTo(another.getSortKey());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Contacts) {
            return this.id == ((Contacts) o).getId();
        } else {
            return super.equals(o);
        }
    }
}

