package ru.disk.Disk.utils.exceptions.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorModel {

    private String name;
    private String message;

    public ErrorModel(String name, String message) {
        this.message = message;
        this.name = name;
    }
}
