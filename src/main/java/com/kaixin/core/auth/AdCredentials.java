package com.kaixin.core.auth;


/**
 * Convenience class to help extract userPrincipalName and sAMAccountName from passed credentials
 */
public class AdCredentials extends BasicCredentials {

    private final String sAMAccountName;

    public AdCredentials(String username, String password){
        super(username, password);
        if(username.contains("@")){
            this.sAMAccountName = username.substring(0, username.indexOf('@'));
        }else if(username.contains("\\")){
            this.sAMAccountName = username.substring(username.indexOf('\\')+1);
        }else{
            this.sAMAccountName = username;
        }
    }


    public String getsAMAccountName() {
        return sAMAccountName;
    }

    public String getUserPrincipalName(String defaultDomain) {
        return getUsername().contains("@") ? getUsername() : String.format("%s@%s", sAMAccountName, defaultDomain);
    }

    public static AdCredentials fromBasicCredentials(BasicCredentials credentials) {
        return (credentials instanceof AdCredentials) ? (AdCredentials) credentials : new AdCredentials(credentials.getUsername(), credentials.getPassword());
    }
}
