/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.exceptions;

/**
 *
 * @author martin
 */
public class InvalidObjectTypeException extends RuntimeException {

    /**
     * Creates a new instance of <code>InvalidObjectTypeException</code> without
     * detail message.
     */
    public InvalidObjectTypeException() {
    }

    /**
     * Constructs an instance of <code>InvalidObjectTypeException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidObjectTypeException(String msg) {
        super(msg);
    }
}
