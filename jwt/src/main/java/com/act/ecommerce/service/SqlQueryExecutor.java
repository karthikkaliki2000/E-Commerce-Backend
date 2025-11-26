package com.act.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;

@Service
public class SqlQueryExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SqlQueryExecutor.class);
    private final DataSource dataSource;

    public SqlQueryExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String runDynamicQuery(String sql) {
        logger.info("Executing SQL: {}", sql);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hasResultSet = stmt.execute(sql);

            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    StringBuilder resultJson = new StringBuilder("[");
                    boolean firstRow = true;
                    while (rs.next()) {
                        if (!firstRow) resultJson.append(",");
                        resultJson.append("{");
                        for (int i = 1; i <= columnCount; i++) {
                            String col = meta.getColumnLabel(i);
                            String val = rs.getString(i);
                            resultJson.append("\"").append(col).append("\": \"")
                                    .append(val == null ? "" : val).append("\"");
                            if (i < columnCount) resultJson.append(", ");
                        }
                        resultJson.append("}");
                        firstRow = false;
                    }
                    resultJson.append("]");
                    logger.debug("Tabular result: {}", resultJson);
                    return resultJson.toString();
                }
            } else {
                int updateCount = stmt.getUpdateCount();
                logger.info("Update count: {}", updateCount);
                return "{ \"updatedRows\": " + updateCount + " }";
            }

        } catch (SQLException e) {
            logger.error("SQL execution failed", e);
            return "{ \"error\": \"" + e.getMessage().replace("\"", "'") + "\" }";
        }
    }
}
