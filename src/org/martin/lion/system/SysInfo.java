/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.system;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author martin
 */
public class SysInfo {
    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final String USER_NAME = System.getProperty("user.name");
    private static final boolean IS_WINDOWS = OS_NAME.contains("windows");
    public static final File ROOT_DIR;
    
    static {
        String rootPath = IS_WINDOWS ? "C:/Users/"+USER_NAME+"/tigerdb" : "/home/"+USER_NAME
                +"/"+"tigerdb";
        ROOT_DIR = new File(rootPath);
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdir();
    }
    
}
