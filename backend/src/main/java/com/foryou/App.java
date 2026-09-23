package com.foryou;



import com.foryou.dao.EntradaDAO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.foryou.dao.UnidadMedidaDAO;
import com.foryou.dao.CategoriaDAO;
import com.foryou.dao.VarianteDAO;
import com.foryou.dao.ProductoDAO;
import com.google.gson.Gson;

import static spark.Spark.*;

import java.util.Map;

public class App {

    public static void main(String[] args) {

        port(4567);

        UnidadMedidaDAO unidadMedidaDAO = new UnidadMedidaDAO();
        EntradaDAO entradaDAO = new EntradaDAO();
        ProductoDAO productoDAO = new ProductoDAO();
        VarianteDAO varianteDAO = new VarianteDAO();
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        Gson gson = new Gson();

        get("/", (request, response) -> {
            return "FOR YOU - Backend funcionando correctamente";
        });

                post("/api/productos", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            try {

                JsonObject body = JsonParser.parseString(request.body()).getAsJsonObject();

                String nombre = body.get("nombre").getAsString();
                String descripcion = body.has("descripcion")
                        ? body.get("descripcion").getAsString()
                        : null;

                long categoriaId = body.get("categoria_id").getAsLong();
                long unidadMedidaId = body.get("unidad_medida_id").getAsLong();

                if (nombre == null || nombre.isBlank()) {
                    response.status(400);

                    return gson.toJson(Map.of(
                            "error", "El nombre del producto es obligatorio"
                    ));
                }

                Map<String, Object> producto = productoDAO.crear(
                        nombre,
                        descripcion,
                        categoriaId,
                        unidadMedidaId
                );

                response.status(201);

                return gson.toJson(producto);

            } catch (Exception e) {

                response.status(400);

                return gson.toJson(Map.of(
                        "error", "No se pudo crear el producto"
                ));
            }
        }); 


                post("/api/variantes", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            try {

                JsonObject body = JsonParser.parseString(request.body()).getAsJsonObject();

                long productoId = body.get("producto_id").getAsLong();
                String nombre = body.get("nombre").getAsString();

                String sku = body.has("sku") && !body.get("sku").isJsonNull()
                        ? body.get("sku").getAsString()
                        : null;

                String color = body.has("color") && !body.get("color").isJsonNull()
                        ? body.get("color").getAsString()
                        : null;

                String tamano = body.has("tamano") && !body.get("tamano").isJsonNull()
                        ? body.get("tamano").getAsString()
                        : null;

                String presentacion = body.has("presentacion") && !body.get("presentacion").isJsonNull()
                        ? body.get("presentacion").getAsString()
                        : null;

                BigDecimal costoActual = body.has("costo_actual")
                        ? body.get("costo_actual").getAsBigDecimal()
                        : BigDecimal.ZERO;

                BigDecimal stockActual = body.has("stock_actual")
                        ? body.get("stock_actual").getAsBigDecimal()
                        : BigDecimal.ZERO;

                BigDecimal stockMinimo = body.has("stock_minimo")
                        ? body.get("stock_minimo").getAsBigDecimal()
                        : BigDecimal.ZERO;

                if (nombre.isBlank()) {

                    response.status(400);

                    return gson.toJson(Map.of(
                            "error", "El nombre de la variante es obligatorio"
                    ));
                }

                if (costoActual.compareTo(BigDecimal.ZERO) < 0
                        || stockActual.compareTo(BigDecimal.ZERO) < 0
                        || stockMinimo.compareTo(BigDecimal.ZERO) < 0) {

                    response.status(400);

                    return gson.toJson(Map.of(
                            "error", "Costo y cantidades no pueden ser negativos"
                    ));
                }

                Map<String, Object> variante = varianteDAO.crear(
                        productoId,
                        nombre,
                        sku,
                        color,
                        tamano,
                        presentacion,
                        costoActual,
                        stockActual,
                        stockMinimo
                );

                response.status(201);

                return gson.toJson(variante);

            } catch (Exception e) {

                response.status(400);

                return gson.toJson(Map.of(
                        "error", "No se pudo crear la variante"
                ));
            }
        });


        post("/api/entradas", (request, response) -> {

    response.type("application/json; charset=UTF-8");

    try {

        JsonObject body =
                JsonParser.parseString(request.body()).getAsJsonObject();

        Long proveedorId = null;

        if (body.has("proveedor_id")
                && !body.get("proveedor_id").isJsonNull()) {

            proveedorId =
                    body.get("proveedor_id").getAsLong();
        }

        String observaciones = null;

        if (body.has("observaciones")
                && !body.get("observaciones").isJsonNull()) {

            observaciones =
                    body.get("observaciones").getAsString();
        }

        if (!body.has("detalles")
                || !body.get("detalles").isJsonArray()) {

            response.status(400);

            return gson.toJson(Map.of(
                    "error",
                    "La entrada debe contener un arreglo de detalles"
            ));
        }

        JsonArray detallesJson =
                body.getAsJsonArray("detalles");

        if (detallesJson.isEmpty()) {

            response.status(400);

            return gson.toJson(Map.of(
                    "error",
                    "La entrada debe tener al menos un detalle"
            ));
        }

        List<Map<String, Object>> detalles =
                new ArrayList<>();

        for (var elemento : detallesJson) {

            JsonObject detalle =
                    elemento.getAsJsonObject();

            if (!detalle.has("variante_id")
                    || !detalle.has("cantidad")
                    || !detalle.has("costo_unitario")) {

                response.status(400);

                return gson.toJson(Map.of(
                        "error",
                        "Cada detalle debe tener variante_id, cantidad y costo_unitario"
                ));
            }

            Map<String, Object> detalleMap =
                    new HashMap<>();

            detalleMap.put(
                    "variante_id",
                    detalle.get("variante_id").getAsLong()
            );

            detalleMap.put(
                    "cantidad",
                    detalle.get("cantidad").getAsBigDecimal()
            );

            detalleMap.put(
                    "costo_unitario",
                    detalle.get("costo_unitario").getAsBigDecimal()
            );

            detalles.add(detalleMap);
        }

        Map<String, Object> entrada =
                entradaDAO.crear(
                        proveedorId,
                        observaciones,
                        detalles
                );

        response.status(201);

        return gson.toJson(entrada);

    } catch (IllegalArgumentException e) {

        response.status(400);

        return gson.toJson(Map.of(
                "error",
                e.getMessage()
        ));

    } catch (Exception e) {

        e.printStackTrace();

        response.status(500);

        return gson.toJson(Map.of(
                "error",
                "No se pudo registrar la entrada"
        ));
    }
});


        get("/api/productos", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            return gson.toJson(productoDAO.obtenerTodos());
        });

                get("/api/variantes", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            return gson.toJson(varianteDAO.obtenerTodas());
        });
         
        get("/api/categorias", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            return gson.toJson(categoriaDAO.obtenerTodas());
        });

            get("/api/unidades-medida", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            return gson.toJson(unidadMedidaDAO.obtenerTodas());
        });


        awaitInitialization();

        System.out.println("=================================");
        System.out.println("FOR YOU BACKEND INICIADO");
        System.out.println("http://localhost:4567");
        System.out.println("=================================");
    }


    

}