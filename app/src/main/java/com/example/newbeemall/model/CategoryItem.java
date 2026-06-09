package com.example.newbeemall.model;

import java.util.ArrayList;
import java.util.List;

public class CategoryItem {
    private final long id;
    private final String name;
    private final List<CategoryItem> children = new ArrayList<>();

    public CategoryItem(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CategoryItem> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return name;
    }
}
