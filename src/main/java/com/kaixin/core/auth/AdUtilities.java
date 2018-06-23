package com.kaixin.core.auth;


import com.kaixin.core.util.CaseInsensitiveHashMap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class AdUtilities {

    /**
     * LDAP attributes can be challenging to deal with. The Java api only makes it more cumbersome.
     * To simply things we are taking the following simplifying assumptions.
     * 1. memberOf is always a multivalued Set
     * 2. mail is always a single value, additional values are discarded
     * 3. Other attributes that return more than one result will be treated as multivalued and will be added
     *    as a set to the returned Map
     * 4. Other attributes that return one value will be returned as Object they came back from the server as.
     *
     * Use a {@link com.commercehub.dropwizard.authentication.AdPrincipalMapper} if you want to process the resulting
     * attributes with more wisdom.
     *
     * @param attributes The raw attributes returned from the ActiveDirectory domain controller
     * @return a CaseInsensitiveHashMap with the simplified attribute model.
     */
    public static CaseInsensitiveHashMap<Object> simplify(Attributes attributes){
        CaseInsensitiveHashMap<Object> result = new CaseInsensitiveHashMap<Object>();
        try {
            NamingEnumeration<? extends Attribute> attrs = attributes.getAll();
            while(attrs.hasMore()){
                Attribute a = attrs.next();
                Object val;
                if(a.getID().toLowerCase().equals(AdConstants.SCHEMA_ATTR_MAIL_LC)){
                    val = a.get(0);
                } else if (a.size()>1 || a.getID().toLowerCase().equals(AdConstants.SCHEMA_ATTR_MEMBEROF_LC)){
                    Set valSet = new HashSet();
                    for(int i =0; i< a.size(); i++){
                        valSet.add(a.get(i));
                    }
                    val = valSet;
                }else{
                    val = a.get();
                }
                result.put(a.getID(), val);
            }

        } catch (NamingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String extractDNParticle(String dn, String particle){

        for(String part: dn.split(",")){
            String upperPart = part.toUpperCase();
            String upperParticle = particle.toUpperCase();
            if(upperPart.startsWith(upperParticle+"=")){
                return part.substring(upperPart.indexOf(upperParticle + "=") + (upperParticle.length()+1)).trim( );

            }
        }
        return null;
    }

    public static Set<String> extractDNParticles(Collection<String> dnStrings, String particle){
        Set<String> result = new HashSet<String>();
        if (dnStrings != null) {  //zhongshu: add if for empty list
	        for(String dn: dnStrings){
	            String value = extractDNParticle(dn, particle);
	            if(null!=value){
	                result.add(value);
	            }
	        }
        }
        return result;
    }
}
