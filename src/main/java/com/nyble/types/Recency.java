package com.nyble.types;

import com.nyble.exceptions.RuntimeSqlException;
import com.nyble.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Recency {

    final static private Logger logger = LoggerFactory.getLogger(Recency.class);
    final static String updateQuery =
            "insert into consumers (system_id, consumer_id, payload, updated_at)\n" +
            "values  (?, ?, \n" +
            "\tjson_build_object('recency', \n" +
            "\t\tjson_build_object('lut', round( extract(epoch from now())*1000 )::text, 'value', ? )), \n" +
            "\tnow())\n" +
            "on conflict on constraint consumers_pk do update \n" +
            "set payload = jsonb_set(consumers.payload, '{recency}', \n" +
            "\tjson_build_object('lut', round( extract(epoch from now())*1000 )::text, 'value', ?)::jsonb, true),\n" +
            "\tupdated_at = now()\n" +
            "where to_timestamp((coalesce(nullif(consumers.payload->'recency'->>'value',''), '0')::int8 / 1000)) < to_timestamp((?)::int8 / 1000)";
    private static Connection connection;
    private static PreparedStatement ps;
    static {
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                if(ps != null){
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if(connection != null){
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }));
    }

    private static void checkStatement() throws SQLException {
        if(connection!=null && connection.isClosed()){
            if(ps==null || ps.isClosed()){
                synchronized (Recency.class){
                    if(ps == null || ps.isClosed()){
                        ps = connection.prepareStatement(updateQuery);
                    }
                }
            }
        }else{
            synchronized (Recency.class){
                if(connection==null || connection.isClosed()){
                    connection = DBUtil.getInstance().getConnection("datawarehouse");
                    ps = connection.prepareStatement(updateQuery);
                }
            }
        }
    }

    public static void updateConsumerRecency(String systemId, String consumerId, String actionDate) {
        try {
            checkStatement();
            ps.setInt(1, Integer.parseInt(systemId));
            ps.setInt(2, Integer.parseInt(consumerId));
            ps.setString(3, actionDate);
            ps.setString(4, actionDate);
            ps.setString(5, actionDate);
            ps.executeUpdate();
        }catch(SQLException e){
            throw new RuntimeSqlException(e.getMessage(), e);
        }
    }
}
