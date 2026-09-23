package com.foryou.dao;

import com.foryou.config.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntradaDAO {

    public Map<String, Object> crear(
            Long proveedorId,
            String observaciones,
            List<Map<String, Object>> detalles
    ) throws Exception {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException(
                    "La entrada debe tener al menos un detalle"
            );
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                /*
                 * Por ahora estamos en desarrollo y todavía no tenemos
                 * autenticación conectada a Supabase Auth.
                 *
                 * Utilizamos el primer usuario activo de la tabla usuarios.
                 * Más adelante este ID vendrá del usuario autenticado.
                 */
                UUIDHolder usuario = obtenerUsuarioActivo(connection);

                BigDecimal total = BigDecimal.ZERO;

                // -------------------------------------------------
                // 1. Crear la entrada
                // -------------------------------------------------

                String sqlEntrada = """
                        INSERT INTO entradas (
                            proveedor_id,
                            usuario_id,
                            total,
                            observaciones
                        )
                        VALUES (?, ?, ?, ?)
                        RETURNING id, fecha
                        """;

                long entradaId;
                String fecha;

                try (PreparedStatement statement =
                             connection.prepareStatement(sqlEntrada)) {

                    if (proveedorId != null) {
                        statement.setLong(1, proveedorId);
                    } else {
                        statement.setNull(1, java.sql.Types.BIGINT);
                    }

                    statement.setObject(2, usuario.id);
                    statement.setBigDecimal(3, BigDecimal.ZERO);
                    statement.setString(4, observaciones);

                    try (ResultSet resultSet = statement.executeQuery()) {

                        if (!resultSet.next()) {
                            throw new Exception(
                                    "No se pudo crear la entrada"
                            );
                        }

                        entradaId = resultSet.getLong("id");
                        fecha = resultSet.getString("fecha");
                    }
                }

                // -------------------------------------------------
                // 2. Procesar cada detalle
                // -------------------------------------------------

                String sqlVariante = """
                        SELECT
                            stock_actual,
                            costo_actual
                        FROM variantes
                        WHERE id = ?
                        AND activo = TRUE
                        FOR UPDATE
                        """;

                String sqlDetalle = """
                        INSERT INTO detalle_entradas (
                            entrada_id,
                            variante_id,
                            cantidad,
                            costo_unitario,
                            subtotal
                        )
                        VALUES (?, ?, ?, ?, ?)
                        """;

                String sqlActualizarVariante = """
                        UPDATE variantes
                        SET
                            stock_actual = ?,
                            costo_actual = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """;

                String sqlMovimiento = """
                        INSERT INTO movimientos_inventario (
                            variante_id,
                            usuario_id,
                            tipo_movimiento,
                            cantidad,
                            stock_anterior,
                            stock_nuevo,
                            referencia_id,
                            motivo
                        )
                        VALUES (?, ?, 'ENTRADA', ?, ?, ?, ?, ?)
                        """;

                try (
                        PreparedStatement varianteStatement =
                                connection.prepareStatement(sqlVariante);

                        PreparedStatement detalleStatement =
                                connection.prepareStatement(sqlDetalle);

                        PreparedStatement actualizarStatement =
                                connection.prepareStatement(sqlActualizarVariante);

                        PreparedStatement movimientoStatement =
                                connection.prepareStatement(sqlMovimiento)
                ) {

                    for (Map<String, Object> detalle : detalles) {

                        long varianteId =
                                ((Number) detalle.get("variante_id"))
                                        .longValue();

                        BigDecimal cantidad =
                                new BigDecimal(
                                        detalle.get("cantidad").toString()
                                );

                        BigDecimal costoUnitario =
                                new BigDecimal(
                                        detalle.get("costo_unitario").toString()
                                );

                        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new IllegalArgumentException(
                                    "La cantidad debe ser mayor que cero"
                            );
                        }

                        if (costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
                            throw new IllegalArgumentException(
                                    "El costo no puede ser negativo"
                            );
                        }

                        // Obtener stock y costo actuales bloqueando la fila.
                        varianteStatement.setLong(1, varianteId);

                        BigDecimal stockAnterior;
                        BigDecimal costoAnterior;

                        try (ResultSet resultSet =
                                     varianteStatement.executeQuery()) {

                            if (!resultSet.next()) {
                                throw new IllegalArgumentException(
                                        "La variante " + varianteId
                                                + " no existe"
                                );
                            }

                            stockAnterior =
                                    resultSet.getBigDecimal("stock_actual");

                            costoAnterior =
                                    resultSet.getBigDecimal("costo_actual");
                        }

                        // -----------------------------------------
                        // Costo promedio ponderado
                        // -----------------------------------------

                        BigDecimal valorAnterior =
                                stockAnterior.multiply(costoAnterior);

                        BigDecimal valorEntrada =
                                cantidad.multiply(costoUnitario);

                        BigDecimal nuevoStock =
                                stockAnterior.add(cantidad);

                        BigDecimal nuevoCosto;

                        if (nuevoStock.compareTo(BigDecimal.ZERO) == 0) {
                            nuevoCosto = BigDecimal.ZERO;
                        } else {
                            nuevoCosto = valorAnterior
                                    .add(valorEntrada)
                                    .divide(
                                            nuevoStock,
                                            2,
                                            RoundingMode.HALF_UP
                                    );
                        }

                        BigDecimal subtotal =
                                cantidad.multiply(costoUnitario)
                                        .setScale(
                                                2,
                                                RoundingMode.HALF_UP
                                        );

                        // -----------------------------------------
                        // Guardar detalle
                        // -----------------------------------------

                        detalleStatement.setLong(1, entradaId);
                        detalleStatement.setLong(2, varianteId);
                        detalleStatement.setBigDecimal(3, cantidad);
                        detalleStatement.setBigDecimal(4, costoUnitario);
                        detalleStatement.setBigDecimal(5, subtotal);

                        detalleStatement.executeUpdate();

                        // -----------------------------------------
                        // Actualizar inventario
                        // -----------------------------------------

                        actualizarStatement.setBigDecimal(1, nuevoStock);
                        actualizarStatement.setBigDecimal(2, nuevoCosto);
                        actualizarStatement.setLong(3, varianteId);

                        actualizarStatement.executeUpdate();

                        // -----------------------------------------
                        // Registrar movimiento
                        // -----------------------------------------

                        movimientoStatement.setLong(1, varianteId);
                        movimientoStatement.setObject(2, usuario.id);
                        movimientoStatement.setBigDecimal(3, cantidad);
                        movimientoStatement.setBigDecimal(4, stockAnterior);
                        movimientoStatement.setBigDecimal(5, nuevoStock);
                        movimientoStatement.setLong(6, entradaId);

                        movimientoStatement.setString(
                                7,
                                "Entrada de inventario #" + entradaId
                        );

                        movimientoStatement.executeUpdate();

                        total = total.add(subtotal);
                    }
                }

                // -------------------------------------------------
                // 3. Actualizar total de la entrada
                // -------------------------------------------------

                String sqlTotal = """
                        UPDATE entradas
                        SET total = ?
                        WHERE id = ?
                        """;

                try (PreparedStatement statement =
                             connection.prepareStatement(sqlTotal)) {

                    statement.setBigDecimal(1, total);
                    statement.setLong(2, entradaId);

                    statement.executeUpdate();
                }

                connection.commit();

                Map<String, Object> resultado = new HashMap<>();

                resultado.put("id", entradaId);
                resultado.put("fecha", fecha);
                resultado.put("total", total);
                resultado.put("observaciones", observaciones);

                return resultado;

            } catch (Exception e) {

                connection.rollback();

                throw e;

            } finally {

                connection.setAutoCommit(true);
            }
        }
    }

    private UUIDHolder obtenerUsuarioActivo(Connection connection)
            throws Exception {

        String sql = """
                SELECT id
                FROM usuarios
                WHERE activo = TRUE
                ORDER BY created_at
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            if (!resultSet.next()) {
                throw new Exception(
                        "No existe un usuario activo"
                );
            }

            return new UUIDHolder(
                    resultSet.getObject("id")
            );
        }
    }

    private static class UUIDHolder {

        private final Object id;

        private UUIDHolder(Object id) {
            this.id = id;
        }
    }
}