import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class VTaskMaster {
    private JFrame frame;
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    private JLabel clockLabel;
    private boolean darkMode = true;
    private String dataFile = "data/tasks.txt";

    public VTaskMaster() {
        initUI();
        loadTasks();
        startClock();
    }

    private void initUI() {
        frame = new JFrame("VTaskMaster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 520);
        frame.setLocationRelativeTo(null);

        // Top panel with title and clock and theme toggle
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(8, 8, 8, 8));
        JLabel title = new JLabel("VTaskMaster", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        top.add(title, BorderLayout.WEST);

        clockLabel = new JLabel("", SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        top.add(clockLabel, BorderLayout.EAST);

        frame.add(top, BorderLayout.NORTH);

        // Center: list
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setCellRenderer(new TaskCellRenderer());
        JScrollPane scroll = new JScrollPane(taskList);
        frame.add(scroll, BorderLayout.CENTER);

        // Right: controls
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.setBorder(new EmptyBorder(10,10,10,10));
        controls.setPreferredSize(new Dimension(260, 0));

        JTextField titleField = new JTextField();
        JTextArea descArea = new JTextArea(4, 20);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"Low","Medium","High"});
        JTextField dueField = new JTextField(); // free text due date
        JButton addBtn = new JButton("Add Task");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton toggleDoneBtn = new JButton("Toggle Done");
        JButton saveBtn = new JButton("Save Now");
        JCheckBox compactCheck = new JCheckBox("Compact View");
        JToggleButton themeToggle = new JToggleButton("Light Theme");

        // Layout helper
        controls.add(new JLabel("Title:"));
        controls.add(titleField);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(new JLabel("Description:"));
        controls.add(new JScrollPane(descArea));
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(new JLabel("Priority:"));
        controls.add(priorityBox);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(new JLabel("Due (optional):"));
        controls.add(dueField);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(addBtn);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(editBtn);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(deleteBtn);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(toggleDoneBtn);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(saveBtn);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(compactCheck);
        controls.add(Box.createRigidArea(new Dimension(0,6)));
        controls.add(themeToggle);

        frame.add(controls, BorderLayout.EAST);

        // Actions
        addBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            if (t.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a title.");
                return;
            }
            String d = descArea.getText().trim();
            String pr = (String)priorityBox.getSelectedItem();
            String due = dueField.getText().trim();
            Task task = new Task(t, d, pr, due, false);
            listModel.addElement(task);
            titleField.setText("");
            descArea.setText("");
            dueField.setText("");
        });

        deleteBtn.addActionListener(e -> {
            int i = taskList.getSelectedIndex();
            if (i >= 0) listModel.remove(i);
        });

        editBtn.addActionListener(e -> {
            int i = taskList.getSelectedIndex();
            if (i < 0) return;
            Task s = listModel.get(i);
            titleField.setText(s.getTitle());
            descArea.setText(s.getDescription());
            priorityBox.setSelectedItem(s.getPriority());
            dueField.setText(s.getDue());
            // After editing fields, user can press Add to create a duplicate - instead we'll replace on Save Edit
            int option = JOptionPane.showConfirmDialog(frame, "Save changes to selected?", "Edit", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                s.setTitle(titleField.getText().trim());
                s.setDescription(descArea.getText().trim());
                s.setPriority((String)priorityBox.getSelectedItem());
                s.setDue(dueField.getText().trim());
                listModel.set(i, s);
                titleField.setText("");
                descArea.setText("");
                dueField.setText("");
            }
        });

        toggleDoneBtn.addActionListener(e -> {
            int i = taskList.getSelectedIndex();
            if (i < 0) return;
            Task s = listModel.get(i);
            s.setDone(!s.isDone());
            listModel.set(i, s);
        });

        saveBtn.addActionListener(e -> {
            saveTasks();
            JOptionPane.showMessageDialog(frame, "Tasks saved.");
        });

        compactCheck.addActionListener(e -> {
            boolean compact = compactCheck.isSelected();
            taskList.setFixedCellHeight(compact ? 30 : -1);
            taskList.repaint();
        });

        themeToggle.addActionListener(e -> {
            darkMode = !themeToggle.isSelected();
            applyTheme();
            SwingUtilities.updateComponentTreeUI(frame);
        });

        // Double-click to edit quick
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = taskList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        Task t = listModel.get(index);
                        String newTitle = JOptionPane.showInputDialog(frame, "Edit title:", t.getTitle());
                        if (newTitle != null) {
                            t.setTitle(newTitle.trim());
                            listModel.set(index, t);
                        }
                    }
                }
            }
        });

        // Save on close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveTasks();
            }
        });

        applyTheme();
        frame.setVisible(true);
    }

    private void applyTheme() {
        if (darkMode) {
            UIManager.put("Panel.background", Color.decode("#2b2b2b"));
            UIManager.put("Label.foreground", Color.decode("#e6e6e6"));
            UIManager.put("Button.background", Color.decode("#444444"));
            UIManager.put("Button.foreground", Color.decode("#e6e6e6"));
            UIManager.put("TextField.background", Color.decode("#3a3a3a"));
            UIManager.put("TextField.foreground", Color.decode("#e6e6e6"));
            UIManager.put("TextArea.background", Color.decode("#3a3a3a"));
            UIManager.put("TextArea.foreground", Color.decode("#e6e6e6"));
            UIManager.put("List.background", Color.decode("#333333"));
            UIManager.put("List.foreground", Color.decode("#e6e6e6"));
        } else {
            UIManager.put("Panel.background", Color.decode("#f3f3f3"));
            UIManager.put("Label.foreground", Color.decode("#111111"));
            UIManager.put("Button.background", Color.decode("#eaeaea"));
            UIManager.put("Button.foreground", Color.decode("#111111"));
            UIManager.put("TextField.background", Color.decode("#ffffff"));
            UIManager.put("TextField.foreground", Color.decode("#111111"));
            UIManager.put("TextArea.background", Color.decode("#ffffff"));
            UIManager.put("TextArea.foreground", Color.decode("#111111"));
            UIManager.put("List.background", Color.decode("#ffffff"));
            UIManager.put("List.foreground", Color.decode("#111111"));
        }
    }

    private void startClock() {
        Timer timer = new Timer(500, e -> {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            clockLabel.setText(LocalDateTime.now().format(f));
        });
        timer.start();
    }

    private void loadTasks() {
        try {
            Path p = Paths.get(dataFile);
            if (!Files.exists(p)) {
                Files.createDirectories(p.getParent());
                Files.createFile(p);
                return;
            }
            BufferedReader br = Files.newBufferedReader(p);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Task t = new Task(line);
                listModel.addElement(t);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveTasks() {
        try {
            Path p = Paths.get(dataFile);
            Files.createDirectories(p.getParent());
            BufferedWriter bw = Files.newBufferedWriter(p);
            for (int i = 0; i < listModel.size(); i++) {
                Task t = listModel.get(i);
                bw.write(t.toCSV());
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    class TaskCellRenderer extends JPanel implements ListCellRenderer<Task> {
        private JLabel titleLabel = new JLabel();
        private JLabel descLabel = new JLabel();
        private JLabel meta = new JLabel();

        public TaskCellRenderer() {
            setLayout(new BorderLayout(6,6));
            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            top.add(titleLabel, BorderLayout.WEST);
            top.add(meta, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);
            add(descLabel, BorderLayout.CENTER);
            setBorder(new EmptyBorder(8,8,8,8));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            titleLabel.setText(value.getTitle());
            descLabel.setText("<html><i>"+value.getDescription()+"</i></html>");
            meta.setText((value.isDone() ? "✓ " : "") + value.getPriority() + (value.getDue().isEmpty() ? "" : " • due " + value.getDue()));
            if (isSelected) setBackground(Color.decode(darkMode ? "#3a3a3a" : "#dfefff"));
            else setBackground(Color.decode(darkMode ? "#2b2b2b" : "#ffffff"));
            titleLabel.setForeground(Color.decode(darkMode ? "#e6e6e6" : "#111111"));
            descLabel.setForeground(Color.decode(darkMode ? "#cfcfcf" : "#333333"));
            meta.setForeground(Color.decode(darkMode ? "#aabbaa" : "#666666"));
            return this;
        }
    }

    public static void main(String[] args) {
        // Set default font for nicer look
        try {
            UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 13));
            UIManager.put("List.font", new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new VTaskMaster());
    }
}