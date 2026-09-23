package com.foryou.dao;

import com.foryou.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovimientoDAO {

    public List<Map<String, Object>> obtenerTodos() throws Exception {

        List<Map<String, Object>> movimientos = new ArrayList<>();

        String sql = """
                SELECT
                    m.id,
                    m.created_at,
                    m.tipo_movimiento,
                    m.cantidad,
                    m.stock_anterior,
                    m.stock_nuevo,
                    m.referencia_id,
                    m.motivo,
                    v.id AS variante_id,
                    v.nombre AS variante,
                    v.sku,
                    p.nombre AS producto,
                    u.nombre AS usuario
                FROM movimientos_inventario m
                INNER JOIN variantes v
                    ON m.variante_id = v.id
                INNER JOIN productos p
                    ON v.producto_id = p.id
                INNER JOIN usuarios u
                    ON m.usuario_id = u.id
                ORDER BY m.created_at DESC, m.id DESC
                """;

        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                Map<String, Object> movimiento = new HashMap<>();

                movimiento.put(
                        "id",
                        resultSet.getLong("id")
                );

                movimiento.put(
                        "fecha",
                        resultSet.getString("created_at")
                );

                movimiento.put(
                        "tipo_movimiento",
                        resultSet.getString("tipo_movimiento")
                );

                movimiento.put(
                        "cantidad",
                        resultSet.getBigDecimal("cantidad")
                );

                movimiento.put(
                        "stock_anterior",
                        resultSet.getBigDecimal("stock_anterior")
                );

                movimiento.put(
                        "stock_nuevo",
                        resultSet.getBigDecimal("stock_nuevo")
                );

                movimiento.put(
                        "referencia_id",
                        resultSet.getObject("referencia_id")
                );

                movimiento.put(
                        "motivo",
                        resultSet.getString("motivo")
                );

                movimiento.put(
                        "variante_id",
                        resultSet.getLong("variante_id")
                );

                movimiento.put(
                        "variante",
                        resultSet.getString("variante")
                );

                movimiento.put(
                        "sku",
                        resultSet.getString("sku")
                );

                movimiento.put(
                        "producto",
                        resultSet.getString("producto")
                );

                movimiento.put(
                        "usuario",
                        resultSet.getString("usuario")
                );

                movimientos.add(movimiento);
            }
        }

        return movimientos;
    }
}