/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.streams;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.martin.electroList.structure.ElectroList;

/**
 *
 * @author martin
 * @param <T>
 */
public class ObjectReader<T>{
    private OIS ois;
    private final File fileObjects;

    public ObjectReader(File fileObjects) throws IOException {
        ois = new OIS(new FileInputStream(fileObjects));
        this.fileObjects = fileObjects;
    }
    
    public ObjectReader(String fileObjectsPath) throws IOException{
        this(new File(fileObjectsPath));
    }
    
    public T readObject() throws IOException, ClassNotFoundException{
        return (T) ois.readObject();
    }
    
    public void close() throws IOException{
        ois.close();
    }
    
    public ElectroList<T> readAllObjects() throws ClassNotFoundException, IOException{
        ElectroList<T> listObjects;
        listObjects = new ElectroList<>();
        
        for(;;){
            try {
                listObjects.add(readObject());
            } catch (EOFException ex) {
                break;
            }
        }
        return listObjects;
    }
    
}
