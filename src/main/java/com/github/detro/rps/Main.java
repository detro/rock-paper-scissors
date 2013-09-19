package com.github.detro.rps;

import com.github.detro.rps.http.Router;

public class Main {
    public static void main(String[] argv) {
        new Router().listen(8080);
    }
}
