package com.kaixin.core.config;

import com.google.common.collect.Sets;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

/*
 * LDAP配置封装，用于认证
 */
public class LdapConfiguration {

    public static final int DEFAULT_CONN_TIMEOUT = 1000;
    public static final int DEFAULT_READ_TIMEOUT = 1000;


    @NotNull
    @Valid
    private String domain;
    private String domainController;

    @NotNull
    @Valid
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    @NotNull
    @Valid
    private int connectionTimeout = DEFAULT_CONN_TIMEOUT;


    @NotNull
    @Valid
    private String usernameFilterTemplate = "(&((&(objectCategory=Person)(objectClass=User)))(sAMAccountName=%s))";

    private String[] attributeNames = new String[]{"sAMAccountName", "mail", "memberOf"};
    private boolean sslEnabled = true;

    @NotNull
    @Valid
    private Set<String> requiredGroups = Sets.newHashSet();

    public String getDomainController() {
        return domainController!=null ? domainController : domain ;
    }

    public void setDomainController(String domainController) {
        this.domainController = domainController;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUsernameFilterTemplate() {
        return usernameFilterTemplate;
    }

    public void setUsernameFilterTemplate(String usernameFilterTemplate) {
        this.usernameFilterTemplate = usernameFilterTemplate;
    }

    public String getDomainBase() {
        char[] namePair = domain.toUpperCase().toCharArray();
        String dn = "DC=";
        for (int i = 0; i < namePair.length; i++) {
            if (namePair[i] == '.') {
                dn += ",DC=" + namePair[++i];
            } else {
                dn += namePair[i];
            }
        }
        return dn;
    }

    public Set<String> getRequiredGroups() {
        return Sets.newCopyOnWriteArraySet(requiredGroups);
    }

    public void setRequiredGroups(Set<String> requiredGroups) {
        this.requiredGroups = requiredGroups;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public void setAttributeNames(String[] attributeNames) {
        this.attributeNames = attributeNames;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public String getLdapUrl() {
    	if (getDomainController() == null)
    		return null;
    	else
    		return String.format("%s://%s", (sslEnabled?"ldaps":"ldap"),  getDomainController());
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
