package com.kaixin.core.util;

import com.kaixin.core.db.TransactionInterceptor;
import com.kaixin.core.sql2o.Connection;

/**
 * Created by zhongshu on 2017/8/25.
 */
public class CheckUtil {

    /*
    public static Connection checkConnection(Connection connection) {
        if (connection == null) {
            connection = TransactionInterceptor.getConnection();
            if (connection == null)
                throw new RuntimeException("no connection for check permisson");
        }
        return connection;
    }
    */
}
