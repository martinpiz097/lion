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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.martin.electroList.structure.ElectroList;
import org.martin.lion.exceptions.InvalidObjectTypeException;
import org.martin.lion.exceptions.UnknownFieldException;
import org.martin.lion.streams.ObjectReader;
import org.martin.lion.streams.ObjectWriter;

/**
 *
 * @author martin
 */
public class StoreManager<T> {
    private ElectroList<T> listObjects;
    
    private final ObjectWriter<T> writer;
    private final ObjectReader<T> reader;
    
    private final File fileRecords;
    //private final String tableName;

    private final Class<T> objectsClazz;
    
    // Array utilizado para algunas operaciones reflection
    private final Field[] classFields;
    
    public StoreManager(Class<T> objectsClazz, File tblFolder) 
            throws IOException, ClassNotFoundException {
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

    private boolean isNumberType(Class<?> clazz){
        if (Number.class.isAssignableFrom(clazz)) return true;
        //return clazz.equals(byte.class)
        
        String className = clazz.getTypeName();
    
        return className.equals("byte") || className.equals("short") || 
                className.equals("int") || className.equals("long") || 
                className.equals("float") || className.equals("double"); 
    }
    
//    private boolean isNumberInstance(Class<?> clazz){}
    
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
    public long getObjectsCount(){
        return listObjects.size();
    }
    
    public long getSumBy(String fieldName){
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()))
            throw new InvalidObjectTypeException(field.getType().getName());
            
        long sum = 0;
        
        for (T obj : listObjects) {
            try {
                sum+=field.getLong(obj);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return sum;
    }
    
    public long getMaxBy(String fieldName){
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()))
            throw new InvalidObjectTypeException(field.getType().getName());
            
        long max = Long.MIN_VALUE;
        long curValue = 0;
        
        for (T obj : listObjects) {
            try {
                curValue = field.getLong(obj);
                if (curValue > max)
                    max = curValue;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return max;
    }
    
    public long getMinBy(String fieldName){
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()))
            throw new InvalidObjectTypeException(field.getType().getName());
            
        long min = Long.MAX_VALUE;
        long curValue = 0;
        
        for (T obj : listObjects) {
            try {
                curValue = field.getLong(obj);
                if (curValue < min)
                    min = curValue;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return min;
    }
    
    public double getAvgBy(String fieldName){
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()))
            throw new InvalidObjectTypeException(field.getType().getName());
        
        long counter = 0;
        double sum = 0;
        
        for (T object : listObjects) {
            try {
                sum+=field.getDouble(object);
                counter++;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return sum / counter;
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
            throws IllegalArgumentException, IllegalAccessException {
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException("El campo "+fieldName+" no existe");

        for (T object : listObjects)
            if (field.get(object).toString().contains(valueToFind.toString()))
                return object;
        
        return null;
    }
    
    public ElectroList<T> getObjectsBy(String fieldName, Object valueToFind) 
            throws IllegalArgumentException, IllegalAccessException {
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
            throws IllegalArgumentException, IllegalAccessException, IOException{
        
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
    
    public void setObject(T oldObj, T newObj) throws IOException{
        listObjects.set(listObjects.indexOf(oldObj), newObj);
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
            throws IllegalArgumentException, IllegalAccessException, IOException{
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
    
//    public static void main(String[] args) {
//        Field[] fields = StoreManager.class.getFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            System.out.println(field.getType());
//        }
//        System.out.println(int.class.asSubclass(Number.class));
//    }

}
