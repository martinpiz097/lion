/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.store;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.martin.electroList.structure.ElectroList;
import org.martin.lion.exceptions.UnknownFieldException;
import org.martin.lion.streams.ObjectReader;
import org.martin.lion.streams.ObjectWriter;
import org.martin.lion.system.SysInfo;

/**
 *
 * @author martin
 */
public class StoreManager<T> {
    private ElectroList<T> listObjects;
    
    private ObjectWriter<T> writer;
    private ObjectReader<T> reader;
    
    private final File fileRecords;
    //private final String tableName;

    private Class<T> objectsClazz;
    
    // Array utilizado para algunas operaciones reflection
    private Field[] classFields;
    
    public StoreManager(Class<T> objectsClazz, File tblFolder) throws IOException, ClassNotFoundException {
        listObjects = new ElectroList<>();

        this.objectsClazz = objectsClazz;
        
        fileRecords = new File(tblFolder, "records.db");
        
        if(!fileRecords.exists()) 
            fileRecords.createNewFile();
        writer = new ObjectWriter<>(fileRecords);
        reader = new ObjectReader<>(fileRecords);
        classFields = objectsClazz.getDeclaredFields();
        
        for (Field field : classFields)
            field.setAccessible(true);
        
        loadAll();
    }

    private boolean isValidField(String fieldName){
        for (int i = 0; i < classFields.length; i++)
            if (classFields[i].getName().equals(fieldName))
                return true;
        return false;
    }
    
    private void loadAll() throws ClassNotFoundException, IOException{
        listObjects = reader.readAllObjects();
    }
    
    private Field getField(String fieldName){
        for (int i = 0; i < classFields.length; i++)
            if (classFields[i].getName().equals(fieldName))
                return classFields[i];
        return null;
    }

    /**
     * Devuelve la cantidad de objetos almacenados.
     * @return Cantidad de objetos almacenados.
     */
    public int getObjectsCount(){
        return listObjects.size();
    }

    public void addObject(T object) throws IOException{
        writer.writeObject(object);
        listObjects.add(object);
    }

    public void addObjectsFrom(Collection<T> list) throws IOException{
        for (T t : list)
            listObjects.add(t);
        writer.update(listObjects);
    }
    
    public ElectroList<T> getObjects(){
        return listObjects;
    }

    public T getFirstObject(){
        return listObjects.pollFirst();
    }
    
    public T getLastObject(){
        return listObjects.pollLast();
    }
    
    public T getObjectBy(int index){
        return listObjects.isEmpty() ? null : listObjects.get(index);
    }
    
    public T getObjectBy(String fieldName, Object valueToFind) 
            throws IllegalArgumentException, IllegalAccessException, UnknownFieldException{
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException("El campo "+fieldName+" no existe");
        
        for (T object : listObjects)
            if (field.get(object).toString().contains(valueToFind.toString()))
                return object;
        
        return null;
    }
    
    public ElectroList<T> getObjectsBy(String fieldName, Object valueToFind) 
            throws IllegalArgumentException, IllegalAccessException, UnknownFieldException{
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException("El campo "+fieldName+" no existe");
        ElectroList<T> listResults = new ElectroList<>();
        
        for (T object : listObjects)
            if (field.get(object).toString().contains(valueToFind.toString()))
                listResults.add(object);
        
        return listResults;
    }
    
    public void setObject(int index, T newObject) throws IOException{
        listObjects.set(index, newObject);
        writer.update(listObjects);
    }

    public void setObjects(String fieldName, Object valueToFind, T newObject) 
            throws UnknownFieldException, IllegalArgumentException, 
            IllegalAccessException, IOException{
        
        Field field = getField(fieldName);
        if(field == null)
            throw new UnknownFieldException("El campo "+fieldName+" no existe");
    
        int counter = 0;
        for (T object : listObjects) {
            if (field.get(object).toString().contains(valueToFind.toString()))
                listObjects.set(counter, newObject);
            counter++;
        }
        writer.update(listObjects);
        
    }

    public void deleteAllObjects() throws IOException{
        listObjects.clear();
        writer.clearFile();
    }
    
    public void deleteObject(int index) throws IOException{
        listObjects.remove(index);
        writer.update(listObjects);
    }
    
    public void deleteObjectsBy(String fieldName, Object valueToFind) 
            throws UnknownFieldException, IllegalArgumentException, 
            IllegalAccessException, IOException{
        Field field = getField(fieldName);
        if(field == null)
            throw new UnknownFieldException("El campo "+fieldName+" no existe");
    
        int counter = 0;
        for (T object : listObjects) {
            if (field.get(object).toString().contains(valueToFind.toString()))
                listObjects.remove(object);
            counter++;
        }
        writer.update(listObjects);
    }
    
    public void deleteFile(){
        fileRecords.delete();
    }
}
