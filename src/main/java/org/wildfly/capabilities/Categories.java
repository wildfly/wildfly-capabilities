package org.wildfly.capabilities;

import java.util.List;

import io.quarkiverse.roq.data.runtime.annotations.DataMapping;

@DataMapping(value = "categories", parentArray = true)
public record Categories(List<Category> list) {
    public record Category(String id, String name, String description) {}
}
