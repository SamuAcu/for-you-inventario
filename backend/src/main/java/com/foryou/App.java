package com.foryou;

import com.foryou.dao.UnidadMedidaDAO;
import com.foryou.dao.CategoriaDAO;
import com.foryou.dao.VarianteDAO;
import com.foryou.dao.ProductoDAO;
import com.google.gson.Gson;

import static spark.Spark.*;

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