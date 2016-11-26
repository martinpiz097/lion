/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.martin.lion.system;

import java.io.File;

/**
 *
 * @author martin
 */
public class SysInfo {
    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final String USER_NAME = System.getProperty("user.name");
    private static final boolean isLinux = OS_NAME.contains("linux");
    public static final File ROOT_DIR;
    
    static {
        String rootPath = isLinux ? "tigerdb" : "C:/Users/"+USER_NAME+"/tigerdb";
        ROOT_DIR = new File(rootPath);
        if (!ROOT_DIR.exists()) ROOT_DIR.mkdir();
    }
}
