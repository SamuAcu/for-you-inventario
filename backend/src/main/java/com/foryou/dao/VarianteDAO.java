package com.foryou.dao;

import com.foryou.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VarianteDAO {

    public List<Map<String, Object>> obtenerTodas() throws Exception {

        List<Map<String, Object>> variantes = new ArrayList<>();

        String sql = """
                SELECT
                    v.id,
                    p.nombre AS producto,
                    v.nombre,
                    v.sku,
                    v.color,
                    v.tamano,
                    v.presentacion,
                    v.costo_actual,
                    v.stock_actual,
                    v.stock_minimo
                FROM variantes v
                INNER JOIN productos p
                    ON v.producto_id = p.id
                WHERE v.activo = TRUE
                ORDER BY v.id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> variante = new HashMap<>();

                variante.put("id", resultSet.getLong("id"));
                variante.put("producto", resultSet.getString("producto"));
                variante.put("nombre", resultSet.getString("nombre"));
                variante.put("sku", resultSet.getString("sku"));
                variante.put("color", resultSet.getString("color"));
                variante.put("tamano", resultSet.getString("tamano"));
                variante.put("presentacion", resultSet.getString("presentacion"));
                variante.put("costo_actual", resultSet.getBigDecimal("costo_actual"));
                variante.put("stock_actual", resultSet.getBigDecimal("stock_actual"));
                variante.put("stock_minimo", resultSet.getBigDecimal("stock_minimo"));

                variantes.add(variante);
            }
        }

        return variantes;
    }
}