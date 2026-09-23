package com.foryou.dao;

import com.foryou.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnidadMedidaDAO {

    public List<Map<String, Object>> obtenerTodas() throws Exception {

        List<Map<String, Object>> unidades = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    nombre,
                    abreviatura
                FROM unidades_medida
                ORDER BY nombre
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> unidad = new HashMap<>();

                unidad.put("id", resultSet.getLong("id"));
                unidad.put("nombre", resultSet.getString("nombre"));
                unidad.put("abreviatura", resultSet.getString("abreviatura"));

                unidades.add(unidad);
            }
        }

        return unidades;
    }
}