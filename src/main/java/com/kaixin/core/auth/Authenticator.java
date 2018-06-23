package com.kaixin.core.auth;

import com.google.common.base.Optional;
import com.kaixin.core.app.KxConsts;
import com.kaixin.core.config.LdapConfiguration;
import com.kaixin.core.db.DbHandle;
import com.kaixin.core.sql.Sql;
import com.kaixin.core.sql2o.Connection;
import com.kaixin.core.sql2o.Sql2o;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(Authenticator.class);
    private LdapConfiguration configuration;
    private Sql sql;

    public Authenticator(LdapConfiguration configuration, Sql sql){
        this.configuration = checkNotNull(configuration);
        this.sql = checkNotNull(sql);
    }

    public Optional<Map<String,Object>> authenticate(BasicCredentials credentials)  {
        return doAuthenticate(AdCredentials.fromBasicCredentials(credentials));
    }
    
    private Map<String,Object> getLocalUser(String account) {
    	try (DbHandle handle = DbHandle.manualInstance()) {

			return handle.query(sql
                .select(KxConsts.TBL_USER)
                .where(KxConsts.COL_USER_ACCOUNT, Sql.EQ))
                .setUnamedParameter(0, account)
                .executeAndFetchFirstRow();
		}    	
    }

    private Optional<Map<String,Object>> doAuthenticate(AdCredentials credentials)  {

        DirContext boundContext = bindUser(credentials);
        
        if(boundContext==null){
        	String username = credentials.getUsername();
        	
        	//查本地用户
        	Map<String, Object> user = getLocalUser(username);
        	if (user != null && AuthUtil.validatePassword(credentials.getPassword(), (String)user.get(KxConsts.COL_USER_PASSWORD))) {
        		
        		return Optional.of(user);
        	}
        }
        else
        {
            AdPrincipal principal = getAdPrincipal(boundContext, credentials);
            if(authorized(principal)){
            	Map<String, Object> user = getLocalUser(principal.getUsername());
            	if (user == null ) {
                	//先导入本地用户??            		
            	}
            	else {
            		return Optional.fromNullable(user);
            	}
            }else{
                Set<String> missingGroups = configuration.getRequiredGroups();
                missingGroups.removeAll(principal.getGroupNames());
                LOG.warn(String.format("%s authenticated successfully but did not have authority. Missing Groups: %s", credentials.getUsername(), missingGroups.toString()));
            }
        }
        return Optional.absent();
     }

    private boolean authorized(AdPrincipal principal) {
        boolean authorized = true;
        for(String requiredGroup: configuration.getRequiredGroups()){
            authorized = authorized && principal.getGroupNames().contains(requiredGroup);
        }
        return authorized;
    }


    private AdPrincipal getAdPrincipal(DirContext boundContext, AdCredentials credentials) {
        try {
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchCtls.setReturningAttributes(configuration.getAttributeNames());
            NamingEnumeration<SearchResult> results = boundContext.search(configuration.getDomainBase(), String.format(configuration.getUsernameFilterTemplate(), credentials.getsAMAccountName()), searchCtls);
            SearchResult userResult = results.hasMoreElements() ? results.next() : null;

            if(userResult==null || results.hasMoreElements()){
                throw new RuntimeException(String.format("Inconsistent search for %s. Bind succeeded but post bind lookup failed. Assumptions/logic failed?", credentials.getUsername()));
            }

            Map<String, Object> attributes = AdUtilities.simplify(userResult.getAttributes());
            return new AdPrincipal(
                        (String)attributes.get(AdConstants.SCHEMA_ATTR_SAMACCOUNTNAME),
                        AdUtilities.extractDNParticles((Set) attributes.get(AdConstants.SCHEMA_ATTR_MEMBEROF), "cn"),
                        attributes);
        } catch (NamingException e) {
            throw new RuntimeException("User search failed. Configuration error?", e);
        }
    }

    private Set<String> extractGroupNames(Set<String> groupDNs){
        Set<String> result = new HashSet<String>();
        for(String groupDn : groupDNs){
            result.add(groupDn.substring(groupDn.indexOf('=') + 1, groupDn.indexOf(',')));
        }
        return result;
    }


    private DirContext bindUser(AdCredentials credentials) {
    	if (configuration.getLdapUrl() == null)
    		return null;
    	
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //See: http://docs.oracle.com/javase/jndi/tutorial/ldap/connect/create.html#TIMEOUT
        properties.put("com.sun.jndi.ldap.read.timeout", configuration.getReadTimeout()+"");  // How long to wait for a read response
        properties.put("com.sun.jndi.ldap.connect.timeout", configuration.getConnectionTimeout()+"");  // How long to wait for a network connection
        properties.put(Context.PROVIDER_URL, configuration.getLdapUrl());
        properties.put(Context.SECURITY_PRINCIPAL, credentials.getUserPrincipalName(configuration.getDomain()));
        properties.put(Context.SECURITY_CREDENTIALS, credentials.getPassword());
        properties.put(Context.REFERRAL, "ignore");
        try {
            return new InitialDirContext(properties);
        } catch (javax.naming.AuthenticationException e) {
            LOG.warn(String.format("User: %s failed to authenticate. Bad Credentials", credentials.getUsername()), e);
            return null;
        } catch (NamingException e) {
            LOG.warn("Could not bind with AD", e);
            return null;
        }
    }
}

