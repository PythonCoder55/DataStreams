import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static java.awt.Font.*;

public class DataStreamFrame extends JFrame {
    JPanel entirePanel, controlPanel, textPanel, queryPanel;

    JButton selectFileButton, findFileButton, exitButton;

    JTextArea textArea, queryResultArea;
    JScrollPane textScrollPane, queryScrollPane;

    JTextField queryField;

    JFileChooser filePicker = new JFileChooser();
    File chosenFile;
    File currentDir = new File(System.getProperty("user.dir"));

    public static Set<String> stopWords = new TreeSet<>();
    public static Set<String> wordSet = new TreeSet<>();
    Map<String, List<Integer>> lineIndex = new TreeMap<>();
    Map<Integer, String> textLines = new TreeMap<>();

    int lineCount = 0;

    public DataStreamFrame() {
        entirePanel = new JPanel();
        entirePanel.setLayout(new BorderLayout());
        createControlPanel();
        entirePanel.add(controlPanel, BorderLayout.NORTH);
        createTextPanel();
        entirePanel.add(textPanel, BorderLayout.CENTER);
        createQueryPanel();
        entirePanel.add(queryPanel, BorderLayout.SOUTH);

        add(entirePanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
    }

    public void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 2));
        selectFileButton = new JButton("Choose File");
        exitButton = new JButton("Exit");

        selectFileButton.addActionListener((ActionEvent ae) -> {
            textArea.setText("");
            filePicker.setCurrentDirectory(currentDir);
            if (filePicker.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chosenFile = filePicker.getSelectedFile();
                Path file = chosenFile.toPath();
                lineCount = 0;
                try (Stream<String> lines = Files.lines(file)) {
                    lines.forEach(line -> {
                        textArea.append(line + "\n");
                        lineCount++;
                        textLines.put(lineCount, line);
                        String[] words = line.split("[^\\w']+");
                        String w;
                        for (String word : words) {
                            w = word.toLowerCase().trim();
                            w = w.replaceAll("_", " ").trim();
                            if (!isStopWord(w)) {
                                if (lineIndex.containsKey(w)) {
                                    lineIndex.get(w).add(lineCount);
                                } else {
                                    List<Integer> lineNumList = new LinkedList<>();
                                    lineNumList.add(lineCount);
                                    lineIndex.put(w, lineNumList);
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wordSet = lineIndex.keySet();
                for (String key : wordSet) {
                    Stream<Integer> words = lineIndex.get(key).stream();
                }
            } else {
                System.out.println("Please select a file to process!");
            }
            findFileButton.setEnabled(true);
            queryField.setEditable(true);
        });

        exitButton.addActionListener((ActionEvent ae) -> System.exit(0));

        controlPanel.add(selectFileButton);
        controlPanel.add(exitButton);
    }

    public void createTextPanel() {
        textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(1, 2));
        textArea = new JTextArea();
        textArea.setEditable(false);
        textScrollPane = new JScrollPane(textArea);
        queryResultArea = new JTextArea();
        queryResultArea.setEditable(false);
        queryScrollPane = new JScrollPane(queryResultArea);

        textPanel.add(textScrollPane);
        textPanel.add(queryScrollPane);
    }

    public void createQueryPanel() {
        queryPanel = new JPanel();
        queryPanel.setLayout(new GridLayout(1, 2));
        queryField = new JTextField(15);
        queryPanel.add(queryField);
        findFileButton = new JButton("Find in File");
        findFileButton.addActionListener((ActionEvent ae) -> {
            queryResultArea.setText("");
            String searchWord = queryField.getText();
            List<Integer> lineList = new LinkedList();
            lineList = lineIndex.get(searchWord);
            if (lineIndex.get(searchWord) == null) {
                queryResultArea.setText("Query not found in file.");
            } else {
                int listLength = lineList.size();
                for (int i = 0; i < listLength; i++) {
                    String lineString = textLines.get(lineList.get(i));
                    queryResultArea.append("Line " + lineList.get(i) + " - " + lineString + "\n");
                }
            }
        });
        queryPanel.add(findFileButton);
        findFileButton.setEnabled(false);
        queryField.setEditable(false);
    }


    public boolean isStopWord(String word) {
        if (stopWords.isEmpty()) {
            loadStopWords();
        }
        return stopWords.contains(word);
    }

    public void loadStopWords() {
        try (Stream<String> lines = Files.lines(Paths.get("src", "EnglishStopWords.txt"))) {
            lines.forEach(line -> {
                String[] words = line.split("\\R");
                String w;
                for (String word : words) {
                    w = word.toLowerCase().trim();
                    stopWords.add(w);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
