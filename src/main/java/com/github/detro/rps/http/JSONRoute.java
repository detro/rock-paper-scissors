package com.github.detro.rps.http;

import org.apache.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;


public abstract class JSONRoute extends Route {
    private static final Logger LOG = Logger.getLogger(JSONRoute.class);

    private static final String DEFAULT_CONTENT_TYPE = "application/json";

    public JSONRoute(String path) {
        super(path);
    }

    public JSONRoute(String path, String acceptType) {
        super(path, acceptType);
    }

    @Override
    public Object handle(Request req, Response res) {
        // We DO use Sessions (that's how we distinguish users)
        req.session(true);
        // This special 'Route' only deals in JSON
        res.header("content-type", DEFAULT_CONTENT_TYPE);

        // _Log all the things_
        LOG.debug(String.format("%s :%d%s - session '%s' %s",
                req.requestMethod(),
                req.port(),
                req.pathInfo(),
                req.session().id(),
                req.session().isNew() ? "(new)" : ""));

        // Prepare a string builder to hold the output body
        StringBuilder resBody = new StringBuilder("");

        // Pass this to subclasses to process
        process(req, res, resBody);

        return resBody;
    }

    abstract void process(Request req, Response res, StringBuilder resBody);
}
