package cz.muni.stanse.gui;

@SuppressWarnings("serial")
final class ConfigurationDialog extends javax.swing.JDialog {

    // package-private section

    ConfigurationDialog() {
        super(MainWindow.getInstance(),true);

        initComponents();

        sourceConfigurationManager =
            new SourceConfigurationManager(actualOpenedFileRadioButton,
                                              allOpenedFilesRadioButton,
                                              makefileRadioButton,
                                              allDirectoryFilesRadioButton,
                                          allDirectoryHierarchyFilesRadioButton,
                                              batchFileRadioButton,
                                              sourceCodeFileTextField,
                                              chooseFileOnDiscButton,
                                              makefileArgumentsTextField);
        checkersConfurationManager =
            new CheckersConfurationManager(checkersTree,addCheckerButton,
                            removeCheckerButton,addDataButton,removeDataButton);

        closeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override public void actionPerformed(
                                           final java.awt.event.ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
    }

    SourceConfigurationManager getSourceConfigurationManager() {
        return sourceConfigurationManager;
    }

    CheckersConfurationManager getCheckersConfurationManager() {
        return checkersConfurationManager;
    }

    // private section

    private final SourceConfigurationManager sourceConfigurationManager;
    private final CheckersConfurationManager checkersConfurationManager;

    //
    // Begin --- section generated by NetBeans - do not modify ! ---
    //
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sourceTypeButtonGroup = new javax.swing.ButtonGroup();
        configurationTabbedPane = new javax.swing.JTabbedPane();
        sourceCodePanel = new javax.swing.JPanel();
        selectTypeStaticText = new javax.swing.JTextPane();
        actualOpenedFileRadioButton = new javax.swing.JRadioButton();
        allOpenedFilesRadioButton = new javax.swing.JRadioButton();
        makefileRadioButton = new javax.swing.JRadioButton();
        allDirectoryFilesRadioButton = new javax.swing.JRadioButton();
        allDirectoryHierarchyFilesRadioButton = new javax.swing.JRadioButton();
        batchFileRadioButton = new javax.swing.JRadioButton();
        specifySourceFileStaticText = new javax.swing.JTextPane();
        sourceCodeFileTextField = new javax.swing.JTextField();
        chooseFileOnDiscButton = new javax.swing.JButton();
        makefileArgumentsTextField = new javax.swing.JTextField();
        specifyMakefileArgumentsStaticText = new javax.swing.JTextPane();
        checkersPanel = new javax.swing.JPanel();
        checkersTreeScrollPane = new javax.swing.JScrollPane();
        checkersTree = new javax.swing.JTree();
        addCheckerButton = new javax.swing.JButton();
        removeCheckerButton = new javax.swing.JButton();
        addDataButton = new javax.swing.JButton();
        removeDataButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Stanse configuration");
        setModal(true);
        setName("configurationDialog"); // NOI18N
        setResizable(false);

        selectTypeStaticText.setText("Select identification type of source file(s) to be checked:");
        selectTypeStaticText.setFocusable(false);

        sourceTypeButtonGroup.add(actualOpenedFileRadioButton);
        actualOpenedFileRadioButton.setText("Shown opened source file");
        actualOpenedFileRadioButton.setActionCommand("shown");

        sourceTypeButtonGroup.add(allOpenedFilesRadioButton);
        allOpenedFilesRadioButton.setText("All opened source files");
        allOpenedFilesRadioButton.setActionCommand("all");

        sourceTypeButtonGroup.add(makefileRadioButton);
        makefileRadioButton.setText("Makefile project");
        makefileRadioButton.setActionCommand("makefile");

        sourceTypeButtonGroup.add(allDirectoryFilesRadioButton);
        allDirectoryFilesRadioButton.setText("All source code files in directory");

        sourceTypeButtonGroup.add(allDirectoryHierarchyFilesRadioButton);
        allDirectoryHierarchyFilesRadioButton.setText("All source code files in directory and subdirectories");

        sourceTypeButtonGroup.add(batchFileRadioButton);
        batchFileRadioButton.setText("All source code files in specified batch-file");

        specifySourceFileStaticText.setText("Path-name to source file, Makefile, batch file, or directory:");
        specifySourceFileStaticText.setFocusable(false);

        sourceCodeFileTextField.setToolTipText("Specify path-name to source code file, which should be checked for bugs.");

        chooseFileOnDiscButton.setText("Choose");

        makefileArgumentsTextField.setToolTipText("You can specify arguments for 'make' utility here. [Used only when 'Makefile project' selected.]");

        specifyMakefileArgumentsStaticText.setText("Makefile arguments:");
        specifyMakefileArgumentsStaticText.setFocusable(false);

        javax.swing.GroupLayout sourceCodePanelLayout = new javax.swing.GroupLayout(sourceCodePanel);
        sourceCodePanel.setLayout(sourceCodePanelLayout);
        sourceCodePanelLayout.setHorizontalGroup(
            sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceCodePanelLayout.createSequentialGroup()
                .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sourceCodePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(selectTypeStaticText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(sourceCodePanelLayout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(allOpenedFilesRadioButton)
                            .addComponent(actualOpenedFileRadioButton))
                        .addGap(160, 160, 160))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourceCodePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(sourceCodePanelLayout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(makefileRadioButton)
                                    .addComponent(allDirectoryFilesRadioButton)
                                    .addComponent(allDirectoryHierarchyFilesRadioButton)
                                    .addComponent(batchFileRadioButton)))
                            .addComponent(specifySourceFileStaticText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(sourceCodePanelLayout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, sourceCodePanelLayout.createSequentialGroup()
                                        .addComponent(specifyMakefileArgumentsStaticText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(makefileArgumentsTextField))
                                    .addComponent(sourceCodeFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseFileOnDiscButton, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        sourceCodePanelLayout.setVerticalGroup(
            sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectTypeStaticText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actualOpenedFileRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allOpenedFilesRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(makefileRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allDirectoryFilesRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allDirectoryHierarchyFilesRadioButton)
                .addGap(5, 5, 5)
                .addComponent(batchFileRadioButton)
                .addGap(23, 23, 23)
                .addComponent(specifySourceFileStaticText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceCodeFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseFileOnDiscButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(specifyMakefileArgumentsStaticText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(makefileArgumentsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42))
        );

        configurationTabbedPane.addTab("Sources", sourceCodePanel);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        checkersTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        checkersTree.setRootVisible(false);
        checkersTreeScrollPane.setViewportView(checkersTree);

        addCheckerButton.setText("Add Checker");

        removeCheckerButton.setText("Remove Checker");

        addDataButton.setText("Add Data");

        removeDataButton.setText("Remove Data");

        javax.swing.GroupLayout checkersPanelLayout = new javax.swing.GroupLayout(checkersPanel);
        checkersPanel.setLayout(checkersPanelLayout);
        checkersPanelLayout.setHorizontalGroup(
            checkersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(addCheckerButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeCheckerButton)
                .addGap(107, 107, 107)
                .addComponent(addDataButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeDataButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(checkersTreeScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
        );
        checkersPanelLayout.setVerticalGroup(
            checkersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(checkersPanelLayout.createSequentialGroup()
                .addComponent(checkersTreeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(checkersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addCheckerButton)
                    .addComponent(removeCheckerButton)
                    .addComponent(removeDataButton)
                    .addComponent(addDataButton))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        configurationTabbedPane.addTab("Checkers", checkersPanel);

        closeButton.setText("Close");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(430, Short.MAX_VALUE)
                .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(configurationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(configurationTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        configurationTabbedPane.getAccessibleContext().setAccessibleName("Source code");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton actualOpenedFileRadioButton;
    private javax.swing.JButton addCheckerButton;
    private javax.swing.JButton addDataButton;
    private javax.swing.JRadioButton allDirectoryFilesRadioButton;
    private javax.swing.JRadioButton allDirectoryHierarchyFilesRadioButton;
    private javax.swing.JRadioButton allOpenedFilesRadioButton;
    private javax.swing.JRadioButton batchFileRadioButton;
    private javax.swing.JPanel checkersPanel;
    private javax.swing.JTree checkersTree;
    private javax.swing.JScrollPane checkersTreeScrollPane;
    private javax.swing.JButton chooseFileOnDiscButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JTabbedPane configurationTabbedPane;
    private javax.swing.JTextField makefileArgumentsTextField;
    private javax.swing.JRadioButton makefileRadioButton;
    private javax.swing.JButton removeCheckerButton;
    private javax.swing.JButton removeDataButton;
    private javax.swing.JTextPane selectTypeStaticText;
    private javax.swing.JTextField sourceCodeFileTextField;
    private javax.swing.JPanel sourceCodePanel;
    private javax.swing.ButtonGroup sourceTypeButtonGroup;
    private javax.swing.JTextPane specifyMakefileArgumentsStaticText;
    private javax.swing.JTextPane specifySourceFileStaticText;
    // End of variables declaration//GEN-END:variables
    //
    // End --- section generated by NetBeans - do not modify ! ---
    //
}