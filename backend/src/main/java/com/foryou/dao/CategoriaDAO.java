package com.foryou.dao;

import com.foryou.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaDAO {

    public List<Map<String, Object>> obtenerTodas() throws Exception {

        List<Map<String, Object>> categorias = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    nombre,
                    descripcion
                FROM categorias
                WHERE activo = TRUE
                ORDER BY nombre
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> categoria = new HashMap<>();

                categoria.put("id", resultSet.getLong("id"));
                categoria.put("nombre", resultSet.getString("nombre"));
                categoria.put("descripcion", resultSet.getString("descripcion"));

                categorias.add(categoria);
            }
        }

        return categorias;
    }
}