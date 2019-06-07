/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigerdb.lion.streams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.mpizutil.electrolist.structure.ElectroList;

/**
 *
 * @author martin
 */
public class ObjectWriter<T> {
    private OOS oos;
    private final File fileObjects;
    //private TInsert tInsert;
    
    public ObjectWriter(File fileObjects) throws IOException {
        oos = new OOS(new FileOutputStream(fileObjects, true));
        this.fileObjects = fileObjects;
        //tInsert = new TInsert(oos);
        //tInsert.start();
    }
    
    public ObjectWriter(String fileObjectsPath) throws IOException{
        this(new File(fileObjectsPath));
    }

    public void writeObjectAndFlush(T object) throws IOException{
        writeObject(object);
        flush();
    }
    
    public void writeObject(T obj) throws IOException{
        oos.writeObject(obj);
    }
    
    public void flush() throws IOException{
        oos.flush();
    }
    
    public void close() throws IOException{
        oos.close();
    }
    
    public void writeObjectsFrom(ElectroList<T> listObjects) throws IOException{
        for (T obj : listObjects)
            oos.writeObject(obj);
        oos.flush();
    }
    
    public void clearFile() throws IOException{
        oos = new OOS(new FileOutputStream(fileObjects));
    }

    public void update(ElectroList<T> listObjects) throws IOException {
        clearFile();
        writeObjectsFrom(listObjects);
    }
    
}
