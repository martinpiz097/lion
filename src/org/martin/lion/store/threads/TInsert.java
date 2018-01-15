/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.store.threads;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.martin.lion.streams.OOS;

/**
 *
 * @author martin
 */
public class TInsert<T> extends Thread{
    private final OOS oos;
    private T object;
    private boolean save;

    public TInsert(OOS oos) {
        this.oos = oos;
        object = null;
        this.save = false;
    }
    
    public synchronized void setObject(T object){
        this.object = object;
        save = true;
    }
    
    @Override
    public void run(){
        while (true) {            
            if (save) {
                try {
                    save = false;
                    oos.writeObject(object);
                } catch (IOException ex) {
                    Logger.getLogger(TInsert.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
