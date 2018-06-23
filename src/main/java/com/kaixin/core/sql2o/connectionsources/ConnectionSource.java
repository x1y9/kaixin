package com.kaixin.core.sql2o.connectionsources;

import com.kaixin.core.sql2o.Connection;

import java.sql.SQLException;

/**
 * An abstraction layer for providing jdbc connection
 * to use from {@link Connection}
 * Created by nickl on 09.01.17.
 */
public interface ConnectionSource {

    java.sql.Connection getConnection() throws SQLException;

}
