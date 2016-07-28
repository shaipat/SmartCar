package com.example.maor.smartcar;

import java.util.ArrayList;

/**
 * Created by Maor on 15/06/2016.
 */

public class PathStore {


    public static ArrayList<Line> lines;
    public static ArrayList<String> commandslist;

    PathStore()
    {
        lines = new ArrayList<>();
        commandslist = new ArrayList<>();
    }

    private static class PathStoreHolder {
        private final static PathStore INSTANCE = new PathStore();
    }

    public static PathStore getInstance() {
        return PathStore.PathStoreHolder.INSTANCE;
    }
}
