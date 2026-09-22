package com.foryou;

import com.foryou.config.DatabaseConnection;
import java.sql.Connection;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            System.out.println("=================================");
            System.out.println("CONEXION A SUPABASE EXITOSA");
            System.out.println("Base de datos: " + connection.getCatalog());
            System.out.println("=================================");

        } catch (Exception e) {

            System.out.println("=================================");
            System.out.println("ERROR AL CONECTAR CON SUPABASE");
            System.out.println("=================================");

            e.printStackTrace();
        }

        port(4567);

        get("/", (request, response) ->
                "FOR YOU - Backend funcionando correctamente"
        );
    }
}