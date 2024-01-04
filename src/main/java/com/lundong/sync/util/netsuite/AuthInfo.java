package com.lundong.sync.util.netsuite;

public class AuthInfo {

    public AuthInfo(String realm, String url, String token_id, String token_secret, String consumer_key, String consumer_secret) {
        setRealm(realm);
        setUrl(url);
        setToken_id(token_id);
        setToken_secret(token_secret);
        setConsumer_key(consumer_key);
        setConsumer_secret(consumer_secret);
    }

    //Restlet URL
    private String url;
    //Token Id
    private String token_id;
    //Token Secret
    private String token_secret;
    //Consumer Key
    private String consumer_key;
    //Consumer Secret
    private String consumer_secret;
    //Account id
    private String realm;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public String getToken_secret() {
        return token_secret;
    }

    public void setToken_secret(String token_secret) {
        this.token_secret = token_secret;
    }

    public String getConsumer_key() {
        return consumer_key;
    }

    public void setConsumer_key(String consumer_key) {
        this.consumer_key = consumer_key;
    }

    public String getConsumer_secret() {
        return consumer_secret;
    }

    public void setConsumer_secret(String consumer_secret) {
        this.consumer_secret = consumer_secret;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

}
