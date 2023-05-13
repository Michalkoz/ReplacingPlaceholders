package com.example.demo;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class XmlModifier extends Application {

    private File xmlFile;
    private File mappingFile;
    private Map<String, String> tagMapping;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("XML Modifier");

        // Creating the UI elements
        Label labelXmlFile = new Label("Select XML file:");
        Button btnXmlFile = new Button("Browse");
        Label labelMappingFile = new Label("Select mapping file:");
        Button btnMappingFile = new Button("Browse");
        Label labelTagMapping = new Label("Tag mapping:");

        TableView<Map.Entry<String, String>> tableView = new TableView<>();
        TableColumn<Map.Entry<String, String>, String> columnA = new TableColumn<>("kolumna A");
        columnA.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
        TableColumn<Map.Entry<String, String>, String> columnB = new TableColumn<>("kolumna B");
        columnB.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
        tableView.getColumns().addAll(columnA, columnB);

        Button btnModify = new Button("Modify");
        Label labelStatus = new Label();

        // Setting up the UI layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);
        grid.add(labelXmlFile, 0, 0);
        grid.add(btnXmlFile, 1, 0);
        grid.add(labelMappingFile, 0, 1);
        grid.add(btnMappingFile, 1, 1);
        grid.add(labelTagMapping, 0, 2);
        grid.add(tableView, 0, 3, 2, 1);
        grid.add(btnModify, 0, 4);
        grid.add(labelStatus, 1, 4);
        GridPane.setHalignment(btnModify, HPos.RIGHT);

        // Setting up the event handlers for the buttons
        btnXmlFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open XML file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
            xmlFile = fileChooser.showOpenDialog(primaryStage);
        });

        btnMappingFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open mapping file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
            mappingFile = fileChooser.showOpenDialog(primaryStage);
            if (mappingFile != null) {
                tagMapping = loadTagMappingFromFile(mappingFile);
                tableView.getItems().clear();
                tableView.getItems().addAll(tagMapping.entrySet());
            }
        });

        btnModify.setOnAction(e -> {
            if (xmlFile == null) {
                labelStatus.setText("Please select an XML file.");
            } else if (mappingFile == null) {
                labelStatus.setText("Please select a mapping file.");
            } else {
                try {
                    File modifiedXmlFile = modifyXml
                            (xmlFile, tagMapping);
                    labelStatus.setText("File successfully modified and saved as " + modifiedXmlFile.getName());
                } catch (Exception ex) {
                    labelStatus.setText("Error: " + ex.getMessage());
                }
            }
        });
        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    private Parent grid;
    /**
     * Loads the tag mapping from the specified file.
     *
     * @param file the file to load the mapping from
     * @return a map containing the tag mapping
     */
//    Scene scene = new Scene(grid, 500, 400);
//    primaryStage.setScene(scene);
//    primaryStage.show();


    /**
     * Loads the tag mapping from the specified file.
     */
    private Map<String, String> loadTagMappingFromFile(File file) {
        Map<String, String> tagMapping = new HashMap<>();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] tokens = scanner.nextLine().split(",");
                if (tokens.length == 2) {
                    tagMapping.put(tokens[0].trim(), tokens[1].trim());
                }
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Mapping file not found: " + file.getAbsolutePath(), ex);
        }
        return tagMapping;
    }

    /**
     * Modifies the specified XML file using the given tag mapping, and saves the modified file with a "_modified" suffix.
     */
    private File modifyXml(File xmlFile, Map<String, String> tagMapping) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);

        // Traverse the XML document and replace the tags according to the mapping
        replaceTags(document, tagMapping);

        // Save the modified document to a new file with a "_modified" suffix
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        File modifiedXmlFile = new File(xmlFile.getParent(), xmlFile.getName().replace(".xml", "_modified.xml"));
        StreamResult result = new StreamResult(modifiedXmlFile);
        transformer.transform(source, result);

        return modifiedXmlFile;
    }

    /**
     * Recursively traverses the specified element and replaces its tags according to the given mapping.
     */
    private void replaceTags(Element element, Map<String, String> tagMapping) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                replaceTags(childElement, tagMapping);
                String tagName = childElement.getTagName();
                if (tagMapping.containsKey(tagName)) {
                    System.out.println("cannot -> childElement.setTagName(tagMapping.get(tagName));");
//                    childElement.setTagName(tagMapping.get(tagName));
                }
            }
        }
    }
}

//
//    Program składa się z klasy `XmlModifier`, która dziedziczy po klasie `Application`. W metodzie `start`
//    tworzone są wszystkie elementy UI oraz ustawiane są ich event handlery. Kliknięcie przycisku "Browse" dla
//    pola wyboru pliku otwiera okno dialogowe pozwalające na wybór pliku. Wybór pliku dla pola "Select mapping file"
//    ładuje mapowanie tagów ze wskazanego pliku tekstowego i wyświetla je w tabeli. Kliknięcie przycisku "Modify"
//    modyfikuje wsk



