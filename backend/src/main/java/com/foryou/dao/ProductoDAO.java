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
                    id,
                    nombre,
                    descripcion,
                    categoria_id,
                    unidad_medida_id
                FROM productos
                ORDER BY id
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
                producto.put("categoria_id", resultSet.getLong("categoria_id"));
                producto.put("unidad_medida_id", resultSet.getLong("unidad_medida_id"));

                productos.add(producto);
            }
        }

        return productos;
    }
}