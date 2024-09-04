package com.cometproject.storage.mysql.data.statements;

import java.sql.PreparedStatement;

public interface StatementConsumer {
    void accept(final PreparedStatement statement) throws Exception;
}
