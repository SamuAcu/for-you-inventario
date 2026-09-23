package com.foryou.dao;

import com.foryou.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductoDAO {

    public List<Map<String, Object>> obtenerTodos() throws Exception {

        List<Map<String, Object>> productos = new ArrayList<>();

        String sql = """
                SELECT
                    p.id,
                    p.nombre,
                    p.descripcion,
                    c.nombre AS categoria,
                    u.nombre AS unidad_medida,
                    u.abreviatura
                FROM productos p
                INNER JOIN categorias c
                    ON p.categoria_id = c.id
                INNER JOIN unidades_medida u
                    ON p.unidad_medida_id = u.id
                WHERE p.activo = TRUE
                ORDER BY p.id
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> producto = new HashMap<>();

                producto.put("id", resultSet.getLong("id"));
                producto.put("nombre", resultSet.getString("nombre"));
                producto.put("descripcion", resultSet.getString("descripcion"));
                producto.put("categoria", resultSet.getString("categoria"));
                producto.put("unidad_medida", resultSet.getString("unidad_medida"));
                producto.put("abreviatura", resultSet.getString("abreviatura"));

                productos.add(producto);
            }
        }

        return productos;
    }
}