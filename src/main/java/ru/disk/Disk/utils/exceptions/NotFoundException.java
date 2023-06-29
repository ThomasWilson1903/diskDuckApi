package ru.disk.Disk.utils.exceptions;

public class NotFoundException extends Exception {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String type, String id) {
        super(type + " not fount by id " + id);
    }
}
