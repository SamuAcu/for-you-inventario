package com.foryou;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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