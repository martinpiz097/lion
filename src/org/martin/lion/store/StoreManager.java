/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.store;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.martin.electroList.structure.ElectroList;
import org.martin.lion.exceptions.InvalidObjectTypeException;
import org.martin.lion.exceptions.UnknownFieldException;
import org.martin.lion.store.threads.TSaver;
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
    
    private TSaver<T> tSaver;
    
    private final File fileRecords;
    //private final String tableName;

    private final Class<T> objectsClazz;
    
    // Array utilizado para algunas operaciones reflection
    private final Field[] classFields;
    private final Method[] classMethods;
    
    private final Method equalsMethod;
    
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
        classMethods = objectsClazz.getDeclaredMethods();
        
        for (Field field : classFields)
            field.setAccessible(true);
        
        equalsMethod = getMethod("equals");
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
    
    private boolean isString(Class<?> clazz){
        return clazz.getSimpleName().equals("String");
    }
    
//    private boolean isNumberInstance(Class<?> clazz){}

    private boolean isEqualsNumbers(Object o1, Object o2){
        Number n1, n2;
        //long l1, l2;
        //double d1, d2;
        //float f1, f2;
        
        if (Number.class.isAssignableFrom(o1.getClass()) && 
                Number.class.isAssignableFrom(o2.getClass())){
            n1 = (Number) o1;
            n2 = (Number) o2;
            if ((Double.class.isInstance(o1) && Double.class.isInstance(o2)) || 
                    (Float.class.isInstance(o1) && Float.class.isInstance(o2)))
                return n1.doubleValue() == n2.doubleValue();
            
            else 
                return n1.longValue() == n2.longValue();
        }
        else
            if ((double.class.isInstance(o1) && double.class.isInstance(o2)) || 
                    (float.class.isInstance(o1) && float.class.isInstance(o2)))
                return (double)o1 == (double)o2;
            
            else 
                return (long)o1 == (long)o2;
    }
    
    private void loadAll() throws ClassNotFoundException, IOException{
        listObjects = reader.readAllObjects();
    }
    
    private Field getField(String fieldName){
        for (int i = 0; i < classFields.length; i++)
            if (classFields[i].getName().equals(fieldName)){
                classFields[i].setAccessible(true);
                return classFields[i];
            }
        return null;
    }
    
    private Method getMethod(String methodName){
        for (int i = 0; i < classMethods.length; i++) {
            if (classMethods[i].getName().equals(methodName)) {
                classMethods[i].setAccessible(true);
                return classMethods[i];
            }
        }
        return null;
    }
    
    private long toNumber(String str){
        final char[] chars = str.toCharArray();
        
        if (chars == null || chars.length == 0)
            return 0;
        
        int valAcumulator = 0;
        
        for (int i = 0; i < chars.length; i++) {
            valAcumulator+=((int)chars[i]);
        }
        
        return valAcumulator;
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
        boolean isString = false;
        
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()) && !(isString = isString(field.getType())))
            throw new InvalidObjectTypeException(field.getType().getName());
            
        long sum = 0;
        
        if (isString) {
            for (T obj : listObjects) {
                try {
                    sum+=toNumber(field.get(obj).toString());
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        else for (T obj : listObjects) {
            try {
                sum+=field.getLong(obj);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return sum;
    }

    public void startSaver() {
        tSaver = new TSaver<>(listObjects, writer);
        tSaver.start();
    }
    
    public long getMaxBy(String fieldName){
        Field field = getField(fieldName);
        boolean isString = false;
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()) && !(isString = isString(field.getType())))
            throw new InvalidObjectTypeException(field.getType().getName());
            
        long max = Long.MIN_VALUE;
        long curValue = 0;
        
        if (isString) {
            for (T obj : listObjects) {
                try {
                    curValue = toNumber(field.get(obj).toString());
                    if (curValue > max)
                        max = curValue;
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        else for (T obj : listObjects) {
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
        boolean isString = false;
        
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()) && !(isString = isString(field.getType())))
            throw new InvalidObjectTypeException(field.getType().getName());
            
        long min = Long.MAX_VALUE;
        long curValue = 0;
        
        if (isString) {
            for (T obj : listObjects) {
                try {
                    curValue = toNumber(field.get(obj).toString());
                    if (curValue < min)
                        min = curValue;
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        else for (T obj : listObjects) {
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
        boolean isString = false;
        
        if (field == null)
            throw new UnknownFieldException(fieldName);
        if (!isNumberType(field.getType()) && !(isString = isString(field.getType())))
            throw new InvalidObjectTypeException(field.getType().getName());
        
        long counter = 0;
        double sum = 0;
        
        if (isString) {
            for (T object : listObjects) {
                try {
                    sum+=toNumber(field.get(object).toString());
                    counter++;
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        else for (T object : listObjects) {
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
    
    public void addObjectParallel(T object) throws IOException{
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

    public T getFirstObjectBy(String fieldName, Object valueToFind){
        Field field = getField(fieldName);
        if (field == null)
            throw new UnknownFieldException("El campo "+fieldName+" no existe");
        Object objField;
        
        for (T object : listObjects) {
            try {
                objField = field.get(object);
//                if (objField instanceof String && valueToFind instanceof String)
//                    if (((String)objField).equals((String)valueToFind))
//                        return object;
//                else 
                // Si ambos son numeros se debe convertir a tipos iguales para ser comparados 
                if (isNumberType(objField.getClass()) && isNumberType(valueToFind.getClass())) {
                    //return (Long)objField.equals((Long)valueToFind);
                    if (isEqualsNumbers(objField, valueToFind))
                        return object;
                }
                else if (objField.equals(valueToFind))
                    return object;
                /* Tambien existe la opcion corta transformando todo a String, 
                    hay que comprobar si ésta es más rápida
                     if (objField.toString().equals(valueToFind.toString())) {
                    return object;
                }
                */
                
//                // Invocacion a metodo equals desde reflection por si éste está
//                // sobreescrito.
//                if ((Boolean)(equalsMethod.invoke(field.get(object), valueToFind)))
//                    return object;
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            /*} catch (InvocationTargetException ex) {
                Logger.getLogger(StoreManager.class.getName()).log(Level.SEVERE, null, ex);
            */}
        }
        return null;
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
