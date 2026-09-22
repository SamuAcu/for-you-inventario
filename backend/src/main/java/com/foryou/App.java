package com.foryou;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {

        port(4567);

        get("/", (request, response) -> {
            return "FOR YOU - Backend funcionando correctamente";
        });

    }
}