/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigerdb.lion.store.threads;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mpizutil.electrolist.structure.ElectroList;
import org.tigerdb.lion.streams.ObjectWriter;

/**
 *
 * @author martin
 */
public class TSaver<T> extends Thread{
    private final ElectroList<T> listObjects;
    private final ObjectWriter<T> writer;
    private int currentObjectCount;
    private int listSize;
    private boolean save;
    
    public TSaver(ElectroList<T> listObjects, ObjectWriter<T> writer) {
        this.listObjects = listObjects;
        this.writer = writer;
        currentObjectCount = listSize = listObjects.size();
        setName("TSaver "+getId());
    }

    public boolean hasNewObjects(){
        return currentObjectCount < listObjects.size();
    }
    
    public void updateCounter(){
        listSize = currentObjectCount = listObjects.size();
    }
    
    public void saveNow(){
        save = true;
    }
    
    @Override
    public void run() {
        ElectroList<T> subList;
        while (true) {            
            try {
                if (hasNewObjects()) {
                    //listSize = listObjects.size();
                    subList = (ElectroList<T>) 
                            listObjects.subList(currentObjectCount, listObjects.size());
                    updateCounter();
                    for (T t : subList) {
                        writer.writeObject(t);
                    }
                }
                Thread.sleep(10);
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(TSaver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
