package com.greensocratic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    private static final String DB_URL = "jdbc:sqlite:greensocratic.db";

    public DBManager() {
        initSchema();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initSchema() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE VIRTUAL TABLE IF NOT EXISTS resources USING fts5(title, content, tags);");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> searchResources(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<String> results = new ArrayList<>();
        String sql = "SELECT title, content FROM resources WHERE resources MATCH ? LIMIT 3;";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, query);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("title") + ": " + rs.getString("content"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}