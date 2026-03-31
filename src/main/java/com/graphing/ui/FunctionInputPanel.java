package com.graphing.ui;

import com.graphing.math.Function;
import com.graphing.math.FunctionType;
import com.graphing.math.parser.FunctionParser;
import com.graphing.ui.components.FunctionListRenderer;
import com.graphing.utils.ColorManager;
import com.graphing.ui.Theme;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

/**
 * Function input and list management
 */
public class FunctionInputPanel extends JPanel {
    
    private final JTextArea functionInput;
    private final JButton addButton;
    private final JButton removeButton;
    private final JButton clearButton;
    private final JList<Function> functionList;
    private final DefaultListModel<Function> listModel;
    private final JButton plotButton;
    
    private final FunctionParser parser;
    private final ColorManager colorManager;
    private final List<Function> functions;
    
    private FunctionInputListener listener;
    
    public FunctionInputPanel() {
        this.functions = new ArrayList<>();
        this.parser = new FunctionParser();
        this.colorManager = new ColorManager();
        
        // Initialize components
        this.functionInput = new JTextArea(2, 20); // 2 rows, 20 columns
        this.functionInput.setLineWrap(true);
        this.functionInput.setWrapStyleWord(true);
        this.addButton = new JButton("Add");
        this.removeButton = new JButton("Remove");
        this.clearButton = new JButton("Clear All");
        this.listModel = new DefaultListModel<>();
        this.functionList = new JList<>(listModel);
        this.plotButton = new JButton("PLOT");
        plotButton.setBackground(Theme.ACCENT);
        plotButton.setForeground(Color.WHITE);
        plotButton.setFont(Theme.font(Font.BOLD, 12));
        plotButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        plotButton.setFocusPainted(false);
        plotButton.putClientProperty("JButton.buttonType", "roundRect");
        plotButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFunction();
            }
        });
        
        setBackground(Theme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setFont(Theme.font(13));
        
        setupComponents();
        setupLayout();
        setupListeners();
        functionList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = functionList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle cellBounds = functionList.getCellBounds(index, index);
                    int relX = e.getX() - cellBounds.x;
                    System.out.println("[DEBUG] Click at relative X: " + relX + ", cell bounds: " + cellBounds);
                    // If click is within the actual icon area (18px left padding, 16px icon width)
                    if (relX >= 18 && relX <= 34) {
                        Function f = listModel.getElementAt(index);
                        System.out.println("[DEBUG] Toggling visibility for function: " + f.getExpression() + " (was visible: " + f.isVisible() + ")");
                        f.setVisible(!f.isVisible());
                        if (listener != null) {
                            listener.onFunctionUpdated(f);
                        }
                        functionList.repaint();
                    } else {
                        System.out.println("[DEBUG] Click outside checkbox area");
                    }
                }
            }
        });
    }
    
    private void setupComponents() {
        // Set up function input
        functionInput.setFont(Theme.font(14));
        functionInput.setToolTipText("Enter mathematical function (e.g., x^2, sin(x), exp(x))");
        functionInput.setRows(2);
        functionInput.setColumns(20);
        functionInput.setPreferredSize(new Dimension(300, 40));
        functionInput.setBackground(Theme.SURFACE);
        functionInput.setForeground(Theme.TEXT);

        
        // Set up buttons
        addButton.setMnemonic('A');
        removeButton.setMnemonic('R');
        clearButton.setMnemonic('C');
        addButton.setBackground(Theme.SURFACE);
        addButton.setForeground(Theme.TEXT);
        removeButton.setBackground(Theme.SURFACE);
        removeButton.setForeground(Theme.TEXT);
        clearButton.setBackground(Theme.DANGER_SOFT);
        clearButton.setForeground(Theme.DANGER.darker());
        addButton.setFont(Theme.font(12));
        removeButton.setFont(Theme.font(12));
        clearButton.setFont(Theme.font(12));
        addButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        removeButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        clearButton.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        
        // Set up function list
        functionList.setCellRenderer(new FunctionListRenderer());
        functionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        functionList.setVisibleRowCount(8);
        functionList.setBackground(Theme.SURFACE);
        functionList.setSelectionBackground(Theme.ACCENT_SOFT);
        functionList.setSelectionForeground(Theme.TEXT);
        functionList.setFont(Theme.font(13));
        
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(Theme.SURFACE);
        
        JPanel inputControls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputControls.setBackground(Theme.SURFACE);
        JScrollPane inputScroll = new JScrollPane(functionInput);
        final int boxWidth = 300;
        final int inputHeight = 40;
        final int plotHeight = 28;
        inputScroll.setPreferredSize(new Dimension(boxWidth, inputHeight));
        inputScroll.setBorder(BorderFactory.createEmptyBorder());
        functionInput.setMargin(new Insets(0, 0, 0, 0));
        plotButton.setPreferredSize(new Dimension(boxWidth - 8, plotHeight));
        inputControls.add(inputScroll);
        inputControls.add(addButton);
        inputControls.add(removeButton);
        inputControls.add(clearButton);
        
        inputPanel.add(inputControls, BorderLayout.NORTH);
        JPanel plotPanel = new JPanel();
        plotPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        plotPanel.setBackground(Theme.SURFACE);
        plotPanel.add(plotButton);
        inputPanel.add(plotPanel, BorderLayout.CENTER);
        
        // List panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        listPanel.setBackground(Theme.SURFACE);
        
        JScrollPane scrollPane = new JScrollPane(functionList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 0, 20)); // More top, left, right padding
        buttonPanel.setBackground(Theme.SURFACE);
        buttonPanel.add(removeButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(clearButton);
        
        listPanel.add(scrollPane, BorderLayout.CENTER);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add panels to main panel
        add(inputPanel, BorderLayout.NORTH);
        add(listPanel, BorderLayout.CENTER);
    }
    
    private void setupListeners() {
        // Add button listener
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFunction();
            }
        });
        
        // Enter key in input field (JTextArea)
        functionInput.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "addFunction");
        functionInput.getActionMap().put("addFunction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFunction();
            }
        });
        
        // Remove button listener
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedFunction();
            }
        });
        
        // Clear button listener
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFunctions();
            }
        });
        
        // List selection listener
        functionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateControlsForSelection();
                }
            }
        });
        
    }
    
    private void showMinimalDialog(Component parent, String message) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent));
        dialog.setUndecorated(true);
        dialog.setModal(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        panel.setBackground(Theme.SURFACE);
        JLabel label = new JLabel(message);
        label.setFont(Theme.font(12));
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(12));
        JButton okButton = new JButton("OK");
        okButton.setFont(Theme.font(12));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setMargin(new Insets(2, 18, 2, 18));
        okButton.setBackground(Theme.ACCENT_SOFT);
        okButton.setForeground(Theme.TEXT);
        okButton.addActionListener(e -> dialog.dispose());
        panel.add(okButton);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
    private boolean showMinimalConfirmDialog(Component parent, String message) {
        final boolean[] result = {false};
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent));
        dialog.setUndecorated(true);
        dialog.setModal(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        panel.setBackground(Theme.SURFACE);
        JLabel label = new JLabel(message);
        label.setFont(Theme.font(12));
        label.setForeground(Theme.TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(12));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton yesButton = new JButton("Yes");
        yesButton.setFont(Theme.font(12));
        yesButton.setMargin(new Insets(2, 18, 2, 18));
        JButton noButton = new JButton("No");
        noButton.setFont(Theme.font(12));
        noButton.setMargin(new Insets(2, 18, 2, 18));
        yesButton.setBackground(Theme.ACCENT_SOFT);
        yesButton.setForeground(Theme.TEXT);
        noButton.setBackground(Theme.SURFACE);
        noButton.setForeground(Theme.TEXT);
        yesButton.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        noButton.addActionListener(e -> { result[0] = false; dialog.dispose(); });
        buttonPanel.add(yesButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(noButton);
        panel.add(buttonPanel);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }
    private void addFunction() {
        String expression = functionInput.getText().trim();
        if (expression.isEmpty()) {
            showMinimalDialog(this, "Please enter a function expression.");
            return;
        }
        try {
            Color color = colorManager.getNextColor();
            FunctionType type = FunctionType.ALGEBRAIC; // Default type
            Function function = new Function(expression, color, type, expression);
            function.setVisible(true);
            functions.add(function);
            listModel.addElement(function);
            functionInput.setText("");
            if (listener != null) {
                listener.onFunctionAdded(function);
            }
        } catch (Exception e) {
            showMinimalDialog(this, "Invalid function: " + e.getMessage());
        }
    }
    
    private void removeSelectedFunction() {
        int selectedIndex = functionList.getSelectedIndex();
        if (selectedIndex >= 0) {
            Function function = listModel.getElementAt(selectedIndex);
            functions.remove(function);
            listModel.remove(selectedIndex);
            
            if (listener != null) {
                listener.onFunctionRemoved(function);
            }
        }
    }
    
    private void clearAllFunctions() {
        boolean result = showMinimalConfirmDialog(this, "Are you sure you want to remove all functions?");
        if (result) {
            functions.clear();
            listModel.clear();
            if (listener != null) {
                listener.onFunctionsCleared();
            }
        }
    }
    
    private void updateControlsForSelection() {
        
    }
    
    private void updateFunctionVisibility() {
        
    }
    
    /**
     * Get all functions
     */
    public List<Function> getFunctions() {
        return new ArrayList<>(functions);
    }
    
    /**
     * Set functions
     */
    public void setFunctions(List<Function> newFunctions) {
        functions.clear();
        listModel.clear();
        
        if (newFunctions != null) {
            functions.addAll(newFunctions);
            for (Function function : newFunctions) {
                listModel.addElement(function);
            }
        }
    }
    
    /**
     * Add a function
     */
    public void addFunction(Function function) {
        functions.add(function);
        listModel.addElement(function);
        
        if (listener != null) {
            listener.onFunctionAdded(function);
        }
    }
    
    /**
     * Remove a function
     */
    public void removeFunction(Function function) {
        functions.remove(function);
        listModel.removeElement(function);
        
        if (listener != null) {
            listener.onFunctionRemoved(function);
        }
    }
    
    /**
     * Set the function input listener
     */
    public void setFunctionInputListener(FunctionInputListener listener) {
        this.listener = listener;
    }
    
    /**
     * Interface for function input events
     */
    public interface FunctionInputListener {
        void onFunctionAdded(Function function);
        void onFunctionRemoved(Function function);
        void onFunctionUpdated(Function function);
        void onFunctionsCleared();
    }
    
    /**
     * Set the text of the function input box
     */
    public void setFunctionInputText(String text) {
        functionInput.setText(text);
        functionInput.requestFocusInWindow();
    }

    public void setFunctionVisibility(int index, boolean visible) {
        if (index >= 0 && index < functions.size()) {
            functions.get(index).setVisible(visible);
            listModel.set(index, functions.get(index)); // Force model update
            if (listener != null) {
                listener.onFunctionUpdated(functions.get(index));
            }
            functionList.repaint();
        }
    }
} 
