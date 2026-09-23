package com.foryou;

import com.foryou.dao.ProductoDAO;
import com.google.gson.Gson;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {

        port(4567);

        ProductoDAO productoDAO = new ProductoDAO();
        Gson gson = new Gson();

        get("/", (request, response) -> {
            return "FOR YOU - Backend funcionando correctamente";
        });

        get("/api/productos", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            return gson.toJson(productoDAO.obtenerTodos());
        });

        awaitInitialization();

        System.out.println("=================================");
        System.out.println("FOR YOU BACKEND INICIADO");
        System.out.println("http://localhost:4567");
        System.out.println("=================================");
    }
}