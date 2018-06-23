package com.kaixin.core.auth;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AdPrincipal {

    private final String username;
    private final Set<String> groupNames;
    private Map<String, Object> retrievedAttributes;

    public AdPrincipal(String username, Set<String> groupNames) {
        this(username, groupNames, null);
    }

    public AdPrincipal(String username, Set<String> groupNames, Map<String, Object> retrievedAttributes) {
        this.username = username;
        this.groupNames = groupNames!=null? ImmutableSet.copyOf(groupNames):Collections.EMPTY_SET;
        this.retrievedAttributes = retrievedAttributes!=null?ImmutableMap.copyOf(retrievedAttributes):Collections.EMPTY_MAP;

    }

    public String getUsername() {
        return username;
    }

    public Set<String> getGroupNames() {
        return groupNames;
    }

    public Map<String, Object> getRetrievedAttributes() {
        return ImmutableMap.copyOf(retrievedAttributes);
    }
}
