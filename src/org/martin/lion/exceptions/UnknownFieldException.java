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
public class UnknownFieldException extends RuntimeException {

    /**
     * Creates a new instance of <code>UnknownFieldException</code> without
     * detail message.
     */
    public UnknownFieldException() {
    }

    /**
     * Constructs an instance of <code>UnknownFieldException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownFieldException(String msg) {
        super(msg);
    }
}
