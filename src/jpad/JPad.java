package jpad;

import javax.swing.UIManager;
import jpad.OsCheck.*;

public class JPad {
    public static String version = "1.0.0";
    static boolean isOSX = false;
    public static String systemLookAndFeel = null;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        //First, let's check the OS and set the Look and Feel based on that
        OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
        switch(ostype)
        {
            case Windows:
                    try
                    {
                        systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
                    }
                    catch(Exception ex)
                    {
                        systemLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
                    }
                break;
            case Linux:
                    try
                    {
                        systemLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
                    }
                    catch(Exception ex)
                    {
                        systemLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
                    }
                break;
            case MacOS:
                    try
                    {
                        systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
                    }
                    catch(Exception ex)
                    {
                        systemLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
                    }
                isOSX = true;
                break;
            case Other:
                    systemLookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
                break;
        }
        //JPad pad = new JPad();
        //pad.runGUI();
        runGUI();
    }
    
    static void runGUI()
    {
        if(isOSX)
            macSpecifics();
        setLookAndFeel(systemLookAndFeel);
        MainEditor mw;
        if(isOSX)
            mw = new MainEditor(isOSX);
        else
            mw = new MainEditor();
        mw.setVisible(true);
    }
    
    private static void setLookAndFeel(String lookAndFeelClass) 
    {
        try
        {
            UIManager.setLookAndFeel(lookAndFeelClass);
        }
        catch(Exception ex)
        {
            System.out.printf("Unable to set look and feel to '%s'\nException output: %s", lookAndFeelClass, ex.getMessage());
        }
    }
    
    static void macSpecifics()
    {
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "JPad");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    
}
