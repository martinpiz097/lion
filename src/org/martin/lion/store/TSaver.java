/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.store;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.martin.electroList.structure.ElectroList;
import org.martin.lion.streams.ObjectWriter;

/**
 *
 * @author martin
 */
public class TSaver<T> extends Thread{
    private final ElectroList<T> listObjects;
    private final ObjectWriter<T> writer;
    private boolean save;
    
    public TSaver(ElectroList<T> listObjects, ObjectWriter<T> writer) {
        this.listObjects = listObjects;
        this.writer = writer;
    }
    
    public void saveNow(){
        save = true;
    }
    
    @Override
    public void run() {
        while (true) {            
            try {
                if (save) {
                    save = false;
                    writer.writeObject(listObjects.peekLast());
                }
                Thread.sleep(10);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(TSaver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
