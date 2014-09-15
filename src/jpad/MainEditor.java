/*
 * Copyright (C) 2014 mike.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package jpad;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.io.FileUtils;
import javax.swing.JRootPane;

/**
 *
 * @author mike
 */
public class MainEditor extends javax.swing.JFrame
{

    ///
    /// This is where we will put some global booleans, these will be essential later on
    ///
    public boolean _isOSX = false;
    public boolean test = true;
    public boolean isOpen = false;
    public boolean hasChanges = false;
    public boolean hasSavedToFile = false;
    public String curFile = null;
    public jpad.OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
    
    ///
    ///
    ///
    
    public MainEditor()
    {
        initComponents();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }
    public MainEditor(boolean isOSX)
    {
        initComponents();
        _isOSX = isOSX;
        macSpecifics();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }
    
    private void macSpecifics()
    {
        com.apple.eawt.Application app = new com.apple.eawt.Application();
        //app.setDockIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("res/icon.png")));
        try
        {
            OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("showAbout", (Class[])null));
            OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("quitApp", (Class[])null));
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //TODO: set keyboard shortcuts for openMenuItem
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }
    
    /// OS X Specifics
    public void showAbout()
    {
        throw new UnsupportedOperationException("Not ready!");
    }
    public boolean quitApp()
    {
        if(hasChanges)
        {
            int dialogResult = JOptionPane.showConfirmDialog(this, "You have unsaved changes, would you like to save these first?", "Question", JOptionPane.YES_NO_CANCEL_OPTION);
            switch(dialogResult)
            {
                case 0:
                    if(hasSavedToFile)
                        saveFile(curFile);
                    else
                        saveAs();
                    return true;
                case 1:
                    return true;
                case 2:
                    return false;
            }
        }
        return true;
    }
    public void openFile_OSX_Nix()
    {
        isOpen = true;
        FilenameFilter awtFilter = new FilenameFilter() 
        {
            @Override
            public boolean accept(File dir, String name)
            {
                String lowercaseName = name.toLowerCase();
                if(lowercaseName.endsWith(".txt"))
                    return true;
                else
                    return false;
            }
        };
        FileDialog fd = new FileDialog(this, "Open Text File", FileDialog.LOAD);
        fd.setFilenameFilter(awtFilter);
        fd.setVisible(true);
        if(fd.getFile() == null)
            return;
        else
            curFile = fd.getDirectory() + fd.getFile();
        //TODO: actually open the file
        try(FileInputStream inputStream = new FileInputStream(curFile))
        {
            String allText = org.apache.commons.io.IOUtils.toString(inputStream);
            mainTextArea.setText(allText);
        }
        catch(Exception ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error While Reading", JOptionPane.ERROR_MESSAGE);
        }
        JRootPane root = this.getRootPane();
		root.putClientProperty("Window.documentFile", new File(curFile));
		root.putClientProperty("Window.documentModified", Boolean.FALSE);
        hasChanges = false;
        hasSavedToFile = true;
        this.setTitle(String.format("JPad - %s", curFile));
        isOpen = false;
    }
    public void saveAs_OSX_Nix()
    {
        String fileToSaveTo = null;
        FilenameFilter awtFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
               String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".txt")) 
                                {
					return true;
				} 
                                else 
                                {
					return false;
				}
            }
        };  
        FileDialog fd = new FileDialog(this, "Save Text File", FileDialog.SAVE);
        fd.setDirectory(System.getProperty("java.home"));
        if(curFile == null)
            fd.setFile("Untitled.txt");
        else
            fd.setFile(curFile);
        fd.setFilenameFilter(awtFilter);
        fd.setVisible(true);
        if(fd.getFile() != null)
            fileToSaveTo = fd.getDirectory() + fd.getFile();
        else
        {    fileToSaveTo = fd.getFile(); return;}
        
        curFile = fileToSaveTo;
        JRootPane root = this.getRootPane();
		root.putClientProperty("Window.documentFile", new File(curFile));
        hasChanges = false;
        hasSavedToFile = true;
    }
    /// End OS X Specifics
    
    /// IO
    public void openFile()
    {
        if(_isOSX || ostype == ostype.Linux || ostype == ostype.Other)
        {
            openFile_OSX_Nix();
        }
        else
        {
            //actually open here, but for windows
        }
    }
    public void saveFile(String fileToSave)
    {
        try
        {
            if(!hasSavedToFile)
                saveAs();
            String docText = mainTextArea.getText();
            File file = new File(curFile);
            FileUtils.writeStringToFile(file, docText);
            hasChanges = false;
            JOptionPane.showMessageDialog(this, String.format("File saved to %s successfully!", curFile), "Information", JOptionPane.INFORMATION_MESSAGE);
            JRootPane root = this.getRootPane();
			root.putClientProperty("Window.documentModified", Boolean.FALSE);
			root.putClientProperty("Window.documentFile", new File(curFile));
            hasChanges = false;
            hasSavedToFile = true;
            this.setTitle(String.format("JPad - %s", curFile));
        } 
        catch (IOException ex)
        {
            Logger.getLogger(MainEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void saveAs()
    {
        if(_isOSX || ostype == ostype.Linux || ostype == ostype.Other)
        {
            saveAs_OSX_Nix();
            saveFile(curFile);
        }
        else
        {
            
        }
    }
    ///
    
    ///Misc
    void textChanged()
    {
        mainTextArea.getDocument().addDocumentListener(new DocumentListener() {

        @Override
        public void removeUpdate(DocumentEvent e) {
            if(!isOpen)
            {
                updateDocModded();
                hasChanges = true;
            }
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            if(!isOpen)
            {
                updateDocModded();
                hasChanges = true;
            }
        }
        @Override
        public void changedUpdate(DocumentEvent e) 
        {
            if(!isOpen)
            {
                updateDocModded();
                hasChanges = true;
            }
        }
  });
    }
	
	void updateDocModded()
	{
		JRootPane root = this.getRootPane();
		root.putClientProperty("Window.documentModified", Boolean.TRUE);
	}
    ///
    
    // <editor-fold defaultstate="collapsed" desc="Boring">
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jScrollPane1 = new javax.swing.JScrollPane();
        mainTextArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        seperator_OpenAndExit = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("JPad");
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
        });

        mainTextArea.setColumns(20);
        mainTextArea.setRows(5);
        jScrollPane1.setViewportView(mainTextArea);
        textChanged();

        jMenu1.setText("File");

        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openMenuItem.setText("Open...");
        openMenuItem.setToolTipText("Open a file");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(openMenuItem);

        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveMenuItem.setText("Save...");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, (java.awt.event.InputEvent.SHIFT_MASK | (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))));
        saveAsMenuItem.setText("Save As...");
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveAsMenuItem);
        jMenu1.add(seperator_OpenAndExit);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);
        if(_isOSX) {     exitMenuItem.getParent().remove(exitMenuItem); }

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_openMenuItemActionPerformed
    {//GEN-HEADEREND:event_openMenuItemActionPerformed
        openFile();
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveMenuItemActionPerformed
    {//GEN-HEADEREND:event_saveMenuItemActionPerformed
        if(hasSavedToFile == false)
            saveAs();
        else if (hasSavedToFile == true)
            saveFile(curFile);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveAsMenuItemActionPerformed
    {//GEN-HEADEREND:event_saveAsMenuItemActionPerformed
        saveAs();
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
    {//GEN-HEADEREND:event_exitMenuItemActionPerformed
        if(hasChanges)
        {
            int dialogResult = JOptionPane.showConfirmDialog(this, "You have unsaved changes, would you like to save these first?", "Question", JOptionPane.YES_NO_CANCEL_OPTION);
            switch(dialogResult)
            {
                case 0:
                    if(hasSavedToFile)
                        saveFile(curFile);
                    else
                        saveAs();
                    System.exit(0);
                    break;
                case 1:
                    System.exit(0);
                    break;
                case 2:
                    //cancel
                    break;
            }
        }
        else
        {
            System.exit(0);
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
       if(!_isOSX)
       {
           if(hasChanges)
        {
            int dialogResult = JOptionPane.showConfirmDialog(this, "You have unsaved changes, would you like to save these first?", "Question", JOptionPane.YES_NO_CANCEL_OPTION);
            switch(dialogResult)
            {
                case 0:
                    if(hasSavedToFile)
                        saveFile(curFile);
                    else
                        saveAs();
                    System.exit(0);
                    break;
                case 1:
                    System.exit(0);
                    break;
                case 2:
                    break;
            }
        }
        else
        {
            System.exit(0);
        }
       }
       else
       {
           if(hasChanges)
           {
               int dialogResult = JOptionPane.showConfirmDialog(this,
                       "You have unsaved changes, would you like to save these before you close?", "Question", JOptionPane.YES_NO_CANCEL_OPTION);
               switch(dialogResult)
               {
                   case 0:
                       if(hasSavedToFile)
                           saveFile(curFile);
                       else
                           saveAs();
					   this.setVisible(false);
                       break;
                   case 1:
                       this.setVisible(false);
					   this.dispose();
                       break;
                   case 2:
                       
                       break;
               }
           }
       }
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea mainTextArea;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JPopupMenu.Separator seperator_OpenAndExit;
    // End of variables declaration//GEN-END:variables
    //
}
