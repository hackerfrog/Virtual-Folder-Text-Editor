import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

class VirtualFolderEditor implements ActionListener, ItemListener, KeyListener {
    
    //All Objests of Content
    String defaultTitle = "Virtual Folder Text Editor";
    JFrame frame = new JFrame(defaultTitle);
    ImageIcon logo = new ImageIcon("logo.png");
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem newFileItem = new JMenuItem("New");
    JMenuItem openFileItem = new JMenuItem("Open");
    JMenuItem saveFileItem = new JMenuItem("Save");
    JMenuItem saveAsFileItem = new JMenuItem("Save As");
    JMenuItem exitFileItem = new JMenuItem("Exit");
    JMenu editMenu = new JMenu("Edit");
    JMenuItem undoEditItem = new JMenuItem("Undo");
    JMenuItem redoEditItem = new JMenuItem("Redo");
    JMenuItem cutEditItem = new JMenuItem("Cut");
    JMenuItem copyEditItem = new JMenuItem("Copy");
    JMenuItem pasteEditItem = new JMenuItem("Paste");
    JMenuItem deleteEditItem = new JMenuItem("Delete");
    JMenu formatMenu = new JMenu("Format");
    JCheckBoxMenuItem wrapFormatItem = new JCheckBoxMenuItem("Word Wrap");
    JMenu viewMenu = new JMenu("View");
    JCheckBoxMenuItem treeViewItem = new JCheckBoxMenuItem("Tree");
    JCheckBoxMenuItem liveViewItem = new JCheckBoxMenuItem("Live #@ Table");
    JCheckBoxMenuItem statusViewItem = new JCheckBoxMenuItem("Status Bar");
    JMenu helpMenu = new JMenu("Help");
    JMenuItem helpHelpItem = new JMenuItem("View Help");
    JMenuItem aboutHelpItem = new JMenuItem("About");
    JTextArea textArea = new JTextArea();
    JTextArea textAreaLines = new JTextArea(" 1 ");
    String oldText = "";
    String oldPath = "";
    String editBuffer;
    Stack undoBuffer = new Stack(), redoBuffer = new Stack();
    JScrollPane textScrollPane;
    JPanel leftPanel = new JPanel();
    JScrollPane treeScrollPane;
    JLabel treeTitle = new JLabel("Tree");
    JTree tree = new JTree();
    DefaultTreeModel treeModel;
    DefaultMutableTreeNode hash = new DefaultMutableTreeNode("HashTags - #");
    DefaultMutableTreeNode mention = new DefaultMutableTreeNode("Mentions - @");
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("VFTE");
    JPanel rightPanel = new JPanel();
    JScrollPane currentHashTableScrollPane;
    JLabel hashTitle = new JLabel("Live HashTags & Mentions");
    JTable hashTable = new JTable();
    DefaultTableModel hashDataModel = new DefaultTableModel();
    String columnsName[] = {"Hash Tags", "Counts" };
    JPanel statusPanel = new JPanel();
    JLabel statusHashCounts = new JLabel("#[0]");
    JLabel statusMentionCounts = new JLabel("@[0]");
    JFileChooser fileChooser = new JFileChooser();
    JLabel capLock = new JLabel("[---]");
    JLabel numLock = new JLabel("[---]");
    JPanel superLeft = new JPanel();
    JButton openTreeFile = new JButton("Open");
    JButton syncTreeFile = new JButton("Sync");
    
    Stack allHash = new Stack();
    Stack allMention = new Stack();

    public VirtualFolderEditor() {
        
        textAreaLines.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setFont(new Font("DialogInput", Font.PLAIN, 12));
        
        frame.setJMenuBar(menuBar);
        
        // Menu Structure
        menuBar.add(fileMenu);
            fileMenu.add(newFileItem);
            fileMenu.add(openFileItem);
                fileMenu.addSeparator();
            fileMenu.add(saveFileItem);
                saveFileItem.setEnabled(false);
            fileMenu.add(saveAsFileItem);
                saveAsFileItem.setEnabled(false);
                fileMenu.addSeparator();
            fileMenu.add(exitFileItem);
        menuBar.add(editMenu);
            editMenu.add(undoEditItem);
            editMenu.add(redoEditItem);
                editMenu.addSeparator();
            editMenu.add(cutEditItem);
            editMenu.add(copyEditItem);
            editMenu.add(pasteEditItem);
            editMenu.add(deleteEditItem);
        menuBar.add(formatMenu);
            formatMenu.add(wrapFormatItem);
        menuBar.add(viewMenu);
            viewMenu.add(treeViewItem);
                treeViewItem.setSelected(true);
            viewMenu.add(liveViewItem);
                liveViewItem.setSelected(true);
            viewMenu.add(statusViewItem);
                statusViewItem.setSelected(true);
        menuBar.add(helpMenu);
            helpMenu.add(helpHelpItem);
                helpMenu.addSeparator();
            helpMenu.add(aboutHelpItem);
        
        // Adding Shortcut to Menu Items
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
        openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        saveAsFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
        exitFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        undoEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        redoEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        cutEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        copyEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
        pasteEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
        deleteEditItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        helpHelpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        // Registering all types of events
        newFileItem.addActionListener(this);
        openFileItem.addActionListener(this);
        saveFileItem.addActionListener(this);
        saveAsFileItem.addActionListener(this);
        exitFileItem.addActionListener(this);
        undoEditItem.addActionListener(this);
        redoEditItem.addActionListener(this);
        cutEditItem.addActionListener(this);
        copyEditItem.addActionListener(this);
        pasteEditItem.addActionListener(this);
        deleteEditItem.addActionListener(this);
        helpHelpItem.addActionListener(this);
        aboutHelpItem.addActionListener(this);
        openTreeFile.addActionListener(this);
        syncTreeFile.addActionListener(this);
        wrapFormatItem.addItemListener(this);
        treeViewItem.addItemListener(this);
        liveViewItem.addItemListener(this);
        statusViewItem.addItemListener(this);
        textArea.addKeyListener(this);
        
        // Frame Components
            textAreaLines.setEditable(false);
            textAreaLines.setBackground(new Color(230,230,230));
            textScrollPane = new JScrollPane(textArea);
            textScrollPane.setRowHeaderView(textAreaLines);
        frame.add(textScrollPane);
        
            leftPanel.setLayout(new BorderLayout());
            leftPanel.add(treeTitle, "North");
                root.add(hash);
                root.add(mention);
                treeModel = new DefaultTreeModel(root);
                tree.setModel(treeModel);
                buildTree();
            leftPanel.add(tree);
                treeScrollPane = new JScrollPane(leftPanel);
                treeScrollPane.setPreferredSize(new Dimension(150, 300));
        
            rightPanel.setLayout(new BorderLayout());
            rightPanel.add(hashTitle, "North");
                hashTable.setModel(hashDataModel);
                    hashDataModel.addColumn(columnsName[0]);
                    hashDataModel.addColumn(columnsName[1]);
                hashTable.setEnabled(false);
            rightPanel.add(hashTable);
                currentHashTableScrollPane = new JScrollPane(rightPanel);
                currentHashTableScrollPane.setPreferredSize(new Dimension(180, 300));
        frame.add(currentHashTableScrollPane, "East");
            statusPanel.setLayout(new BorderLayout());
            JPanel leftStatusPanel = new JPanel(new BorderLayout());
                leftStatusPanel.add(statusHashCounts, "West");
                leftStatusPanel.add(statusMentionCounts, "East");
            statusPanel.add(leftStatusPanel, "West");
            JPanel rightStatusPanel = new JPanel();
                rightStatusPanel.setLayout(new BorderLayout());
                rightStatusPanel.add(capLock, "Center");
                rightStatusPanel.add(numLock, "East");
            statusPanel.add(rightStatusPanel, "East");
        frame.add(statusPanel, "South");
        superLeft.setLayout(new BorderLayout());
        superLeft.add(treeScrollPane);
        JPanel treeSouth = new JPanel();
        treeSouth.setLayout(new BorderLayout());
        treeSouth.add(openTreeFile);
        treeSouth.add(syncTreeFile, "West");
        superLeft.add(treeSouth, "South");
        frame.add(superLeft, "West");
        frame.setIconImage(logo.getImage());
        frame.setSize(720, 640);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
        
        textArea.requestFocus();
    }

    public static void main(String args[]) {
        new VirtualFolderEditor();
    }

    public void itemStateChanged(ItemEvent ie) {
        if(ie.getSource() == statusViewItem) {
            if(statusViewItem.isSelected()) {
                frame.add(statusPanel, "South");
                frame.revalidate();
            } else {
                frame.remove(statusPanel);
                frame.revalidate();
            }
        } else if(ie.getSource() == liveViewItem) {
            if(liveViewItem.isSelected()) {
                frame.add(currentHashTableScrollPane, "East");
                frame.revalidate();
            } else {
                frame.remove(currentHashTableScrollPane);
                frame.revalidate();
            }
        } else if(ie.getSource() == wrapFormatItem) {
            if(wrapFormatItem.isSelected()) {
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
            } else {
                textArea.setLineWrap(false);
                textArea.setWrapStyleWord(false);
            }
        } else if(ie.getSource() == treeViewItem) {
            if(treeViewItem.isSelected()) {
                frame.add(superLeft, "West");
                frame.revalidate();
            } else {
                frame.remove(superLeft);
                frame.revalidate();
            }
        }
    }

    public void actionPerformed(ActionEvent ae)
    {
        if(ae.getSource() == newFileItem) {
            textArea.setText("");
        } else if(ae.getSource() == openFileItem) {
            fileChooser.setDialogTitle("Open...");
            FileNameExtensionFilter filenameextensionfilter = new FileNameExtensionFilter("Text Files (*.txt)", new String[] {"txt"});
            fileChooser.addChoosableFileFilter(filenameextensionfilter);
            int k = fileChooser.showOpenDialog(frame);
            if(k == 0) {
                File file2 = fileChooser.getSelectedFile();
                oldPath = file2.getAbsolutePath();
                try {
                    FileReader filereader = new FileReader(oldPath);
                    BufferedReader bufferedreader = new BufferedReader(filereader);
                    textArea.setText("");
                    String s;
                    while((s = bufferedreader.readLine()) != null) {
                        textArea.append(s + "\n");
                    }
                    bufferedreader.close();
                    saveAsFileItem.setEnabled(true);
                } catch(IOException io) {
                    System.out.println(io);
                }
                frame.setTitle(defaultTitle + " - (" + oldPath + ")");
                oldText = textArea.getText();
                findHash();
            }
        } else if(ae.getSource() == saveFileItem) {
            saveMethod();
        } else if(ae.getSource() == saveAsFileItem) {
            fileChooser.setDialogTitle("Save As");
            int j = fileChooser.showSaveDialog(frame);
            if(j == 0) {
                File file1 = fileChooser.getSelectedFile();
                try {
                    FileWriter filewriter2 = new FileWriter(file1.getAbsolutePath());
                    PrintWriter printwriter2 = new PrintWriter(filewriter2);
                    printwriter2.print(textArea.getText());
                    printwriter2.close();
                    oldPath = file1.getAbsolutePath();
                    frame.setTitle(defaultTitle + " - (" + oldPath + ")");
                } catch(IOException io) {
                    System.out.println(io);
                }
            }
        } else if(ae.getSource() == exitFileItem) {
            if(oldText.equals(textArea.getText())) {
                frame.dispose();
            } else {
                int pinto = -1;
                if(oldPath.isEmpty()) {
                    pinto = JOptionPane.showConfirmDialog(frame, "Do you save changes to Untitled ?", "Unsaved Work...", JOptionPane.YES_NO_CANCEL_OPTION);
                } else {
                    File f = new File(oldPath);
                    pinto = JOptionPane.showConfirmDialog(frame, "Do you save chnages to " + f.getName() + " ?", "Unsaved Work...", JOptionPane.YES_NO_CANCEL_OPTION);
                }
                
                if(pinto == 0) {
                    saveMethod();
                    frame.dispose();
                } else if(pinto == 1) {
                    frame.dispose();
                } else {
                }
                
            }
        } else if (ae.getSource() == aboutHelpItem) {
            JFrame jframe = new JFrame("About");
            jframe.setSize(360, 360);
            jframe.setLocationRelativeTo(null);
            jframe.setDefaultCloseOperation(2);
            jframe.setVisible(true);
            jframe.setIconImage(logo.getImage());
            ImageIcon imageicon = new ImageIcon("aboutLogo.png");
            JLabel jlabel = new JLabel(imageicon);
            jframe.add(jlabel, "North");
            JPanel jpanel = new JPanel();
            jpanel.setLayout(new GridLayout(5, 2));
            jframe.add(jpanel, "Center");
            jpanel.add(new JLabel("Project Name"));
            jpanel.add(new JLabel(": Virtual Folder Text Editor"));
            jpanel.add(new JLabel("Version"));
            jpanel.add(new JLabel(": v0.2.1a"));
            jpanel.add(new JLabel("Developer"));
            jpanel.add(new JLabel(": Lovepreet Singh"));
            jpanel.add(new JLabel("Reg No"));
            jpanel.add(new JLabel(": 11400186"));
            jpanel.add(new JLabel("Group & Roll No"));
            jpanel.add(new JLabel(": A18"));
        } else if (ae.getSource() == cutEditItem) {
            editBuffer = textArea.getSelectedText();
            textArea.replaceSelection("");
        } else if (ae.getSource() == copyEditItem) {
            editBuffer = textArea.getSelectedText();
        } else if (ae.getSource() == pasteEditItem) {
            textArea.insert(editBuffer, textArea.getCaretPosition());
        } else if (ae.getSource() == deleteEditItem) {
            textArea.replaceSelection("");
        } else if (ae.getSource() == undoEditItem) {
            if(!undoBuffer.isEmpty()) {
                String reText = (String) undoBuffer.pop();
                textArea.setText(reText);
                redoBuffer.push(reText);
            }
        } else if (ae.getSource() == redoEditItem) {
            if(!redoBuffer.isEmpty()) {
                String reText = (String) redoBuffer.pop();
                textArea.setText(reText);
                undoBuffer.push(reText);
            }
        } else if (ae.getSource() == openTreeFile) {
            File f = new File(tree.getSelectionPath().getLastPathComponent().toString());
            if(f.isFile()) {
                oldPath = f.getAbsolutePath();
                
                try {
                    FileReader filereader = new FileReader(oldPath);
                    BufferedReader bufferedreader = new BufferedReader(filereader);
                    textArea.setText("");
                    String s;
                    while((s = bufferedreader.readLine()) != null) {
                        textArea.append(s + "\n");
                    }
                    bufferedreader.close();
                    saveAsFileItem.setEnabled(true);
                } catch(IOException io) {
                    System.out.println(io);
                }
                frame.setTitle(defaultTitle + " - (" + oldPath + ")");
                oldText = textArea.getText();
                findHash();
            }
        } else if(ae.getSource() == syncTreeFile) {
            syncFiles();
        }
    }

    public void keyTyped(KeyEvent keyevent)
    {
    }

    public void keyPressed(KeyEvent keyevent)
    {
       textAreaLines.setText(linesMethod());
    }

    public void keyReleased(KeyEvent keyevent)
    {
        boolean isCapOn, isNumOn;
        isCapOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        isNumOn = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
        if(isCapOn) {
            capLock.setText("[CAP]");
        } else {
            capLock.setText("[---]");
        }
        if(isNumOn) {
            numLock.setText("[NUM]");
        } else {
            numLock.setText("[---]");
        }
        
        if(!oldText.equals(textArea.getText())) {
            if(oldPath.isEmpty()) {
                frame.setTitle(defaultTitle + " - * Untitled");
            } else {
                frame.setTitle(defaultTitle + " - * (" + oldPath + ")");
            }
        } else if(oldPath.isEmpty()) {
            frame.setTitle(defaultTitle);
        } else {
            frame.setTitle(defaultTitle + " - (" + oldPath + ")");
        }
        
        if(!textArea.getText().isEmpty()) {
            saveFileItem.setEnabled(true);
        } else {
            saveFileItem.setEnabled(false);
        }
        
        if(keyevent.getSource() == textArea)
        {
            findHash();
//            System.out.println("TextArea : " + keyevent.getKeyChar() + " = " + keyevent.getKeyCode());
        }
        
        if(keyevent.getKeyCode() == 32 || keyevent.getKeyCode() == 10) {
            undoBuffer.push(textArea.getText());
            redoBuffer.clear();
        }
    }

    public void findHash()
    {
        allHash.clear();
        String s = textArea.getText();
        String hashReg = "(#\\w+)";
        Pattern pattern = Pattern.compile(hashReg);
        Matcher matcher = pattern.matcher(s);
        hashDataModel.setRowCount(0);
        Integer hashCount = Integer.valueOf(0);
        
        while(matcher.find()) {
            hashDataModel.addRow(new Object[] {matcher.group(1), Integer.valueOf(1)});
            hashCount += 1;
            allHash.add(matcher.group(1));
        }
        statusHashCounts.setText("#[" + hashCount.toString() + "]");
        
        findMention();
    }
    
    public void findMention() {
        allMention.clear();
        String s = textArea.getText();
        String mentionReg = "(@\\w+)";
        Pattern pattern = Pattern.compile(mentionReg);
        Matcher matcher = pattern.matcher(s);
        Integer mentionCount = Integer.valueOf(0);
        
        
        while(matcher.find()) {
            hashDataModel.addRow(new Object[] {matcher.group(1), Integer.valueOf(1)});
            mentionCount += 1;
            allMention.add(matcher.group(1));
        }
        statusMentionCounts.setText("@[" + mentionCount.toString() + "]");
    }
    
    public void saveMethod() {
        if(!oldPath.isEmpty()){
            try {
                FileWriter filewriter = new FileWriter(oldPath);
                PrintWriter printwriter = new PrintWriter(filewriter);
                printwriter.println(textArea.getText());
                printwriter.close();
                oldText = textArea.getText();
                frame.setTitle(defaultTitle + " - (" + oldPath + ")");
            } catch(IOException io) {
                System.out.println(io);
            }
        } else {
            fileChooser.setDialogTitle("Save");
            int i = fileChooser.showSaveDialog(frame);
            FileNameExtensionFilter fileNameExtensionFilter  = new FileNameExtensionFilter("Text Documents (*.txt)", "txt");
            fileChooser.setFileFilter(fileNameExtensionFilter);
            if(i == 0) {
                File file = fileChooser.getSelectedFile();
                try {
//                    FileWriter filewriter1 = new FileWriter(file.getAbsolutePath());
//                    PrintWriter printwriter1 = new PrintWriter(filewriter1);
                    PrintWriter printwriter1 = new PrintWriter(file);
                    printwriter1.println(textArea.getText());
                    printwriter1.close();
                    oldPath = file.getAbsolutePath();
                    oldPath = oldPath.replace('\\', '/');
                    frame.setTitle(defaultTitle + " - (" + oldPath + ")");
                    saveAsFileItem.setEnabled(true);
                    oldText = textArea.getText();
                } catch(IOException io) {
                    System.out.println(io);
                }

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost/vfte", "root", "toor");
                    Statement sql = con.createStatement();
                    String addFile = "INSERT INTO files VALUES(NULL, '" + oldPath + "', '" + file.getName() + "')";
                    sql.executeUpdate(addFile);

                    
                    while(!allHash.isEmpty()) {
                        String aHash = ((String) allHash.pop()).toLowerCase();
                        String chkHash = "SELECT COUNT(*) AS num FROM hash WHERE hash = '" + aHash + "' AND file = '" + oldPath + "'";
                        ResultSet rs = sql.executeQuery(chkHash);
                        rs.next();
                        if(rs.getInt("num") == 0) {
                            String newHash = "INSERT INTO hash VALUES(NULL, '" + aHash + "', '" + oldPath + "')";
                            sql.executeUpdate(newHash);
                        }
                    }
                    
                    while(!allMention.isEmpty()) {
                        String aMention = ((String) allMention.pop()).toLowerCase();
                        String chkMention = "SELECT COUNT(*) AS num FROM mention WHERE mention = '" + aMention + "' AND file = '" + oldPath + "'";
                        ResultSet rs = sql.executeQuery(chkMention);
                        rs.next();
                        if(rs.getInt("num") == 0) {
                            String newMention = "INSERT INTO mention VALUES(NULL, '" + aMention + "', '" + oldPath + "')";
                            sql.executeUpdate(newMention);
                        }
                    }

                    con.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            
            buildTree();
            treeModel.reload();
        }
    }
    
    public String linesMethod() {
        String t = " 1 ";
        for(Integer i = 2; i <= textArea.getLineCount(); i++) {
            t = t + "\n " + i.toString() + " ";
        }
        return t;
    }
    
    public void buildTree() {
        
        hash.removeAllChildren();
        mention.removeAllChildren();
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/vfte", "root", "toor");
            Statement sql = con.createStatement();
            Statement sql2 = con.createStatement();
            
            String countHash = "SELECT COUNT(DISTINCT hash) AS chash FROM hash ORDER BY hash";
            ResultSet rs = sql.executeQuery(countHash);
            rs.next();
            DefaultMutableTreeNode [] hashChild = new DefaultMutableTreeNode[rs.getInt("chash")];
            
            String getHash = "SELECT * FROM hash GROUP BY hash ORDER BY hash";
            rs = sql.executeQuery(getHash);
            int hc = 0;
            while(rs.next()) {
                String aHash = rs.getString("hash");
                hashChild[hc] = new DefaultMutableTreeNode(aHash);
                hash.add(hashChild[hc]);
                
                String perHash = "SELECT * FROM hash WHERE hash = '" + aHash + "'";
                ResultSet rs2 = sql2.executeQuery(perHash);
                while(rs2.next()) {
                    hashChild[hc].add(new DefaultMutableTreeNode(rs2.getString("file")));
                }
                
                hc++;
            }
            
            rs.close();

            String countMention = "SELECT COUNT(DISTINCT mention) AS cmention FROM mention ORDER BY mention";
            rs = sql.executeQuery(countMention);
            rs.next();
            DefaultMutableTreeNode [] mentionChild = new DefaultMutableTreeNode[rs.getInt("cmention")];
            
            String getMention = "SELECT * FROM mention GROUP BY mention ORDER BY mention";
            rs = sql.executeQuery(getMention);
            int mc = 0;
            while(rs.next()) {
                String aMention = rs.getString("mention");
                mentionChild[mc] = new DefaultMutableTreeNode(aMention);
                mention.add(mentionChild[mc]);
                
                String perMention = "SELECT * FROM mention WHERE mention = '" + aMention + "'";
                ResultSet rs2 = sql2.executeQuery(perMention);
                while(rs2.next()) {
                    mentionChild[mc].add(new DefaultMutableTreeNode(rs2.getString("file")));
                }
                
                mc++;
            }

            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
    
    public void syncFiles() {
        try {
            
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/vfte", "root", "toor");
            Statement sql = con.createStatement();
            Statement sql2 = con.createStatement();
            
            String chkFiles = "SELECT * FROM files";
            ResultSet rs = sql.executeQuery(chkFiles);
            
            while(rs.next()) {
                File f = new File(rs.getString("file_path"));
                if(!f.exists()) {
                    String removeOther = "DELETE FROM hash WHERE file = '" + rs.getString("file_path") + "'";
                    sql2.executeUpdate(removeOther);
                    
                    removeOther = "DELETE FROM mention WHERE file = '" + rs.getString("file_path") + "'";
                    sql2.executeUpdate(removeOther);
                    
                    removeOther = "DELETE FROM files WHERE file_path = '" + rs.getString("file_path") + "'";
                    sql2.executeUpdate(removeOther);
                }
            }
            
            buildTree();
            treeModel.reload();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
