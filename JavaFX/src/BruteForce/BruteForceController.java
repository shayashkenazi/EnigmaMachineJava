package BruteForce;

import DataStructures.Trie;
import DecryptionManager.Difficulty;
import Interfaces.SubController;
import MainApp.AppController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BruteForceController implements SubController, Initializable {

    private Trie trie = new Trie();
    private AppController appController;
    private Map<String, Button> dictionaryMap = new LinkedHashMap<>();


    @FXML private Button btn_clear, btn_proccess, btn_reset, btn_start;
    @FXML private TextField tf_codeConfiguration, tf_input, tf_output, tf_searchBar, tf_taskSize;
    @FXML private ListView<String> lv_dictionary;
    @FXML private ComboBox<Difficulty> cb_level;
    @FXML private Slider s_agents;


    @FXML void clearBtnClick(ActionEvent event) {
        tf_input.setText("");
        tf_output.setText("");
    }

    @FXML void proccessBtnClick(ActionEvent event) {

    }

    @FXML void resetBtnClick(ActionEvent event) {
        appController.resetBtnClick();
    }

    @FXML void searchBarTextChanged(InputMethodEvent event) { // TODO: Delete - doesn't work

        lv_dictionary.getItems().clear();
        List<String> allChildren = trie.returnAllChildren(tf_searchBar.getText());

        for (String childWord : allChildren) {
            lv_dictionary.getItems().add(childWord);
        }
    }


    @FXML void startBtnClick(ActionEvent event) {
        appController.testToDeleteShayHomo();
    }

    @Override
    public void setMainController(AppController mainController) {
        appController = mainController;
    }

    public void initializeTabAfterCodeConfiguration() {

        appController.initializeTrieWithDictionary();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { // TODO: use Scene Builder

        tf_searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            lv_dictionary.getItems().clear();

            List<String> allChildren = trie.returnAllChildren(newValue);

            for (String childWord : allChildren) {
                lv_dictionary.getItems().add(childWord);
            }
        });

        lv_dictionary.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    String itemString = lv_dictionary.getSelectionModel().getSelectedItem();
                    tf_input.appendText(itemString);
                }
            }
        });

        // Initialize Slider
        s_agents.valueProperty().addListener((observable, oldValue, newValue) -> {
            s_agents.setValue(newValue.intValue());
        });

        // Initialize difficulty ComboBox
        cb_level.getItems().add(Difficulty.EASY);
        cb_level.getItems().add(Difficulty.MEDIUM);
        cb_level.getItems().add(Difficulty.HARD);
        cb_level.getItems().add(Difficulty.IMPOSSIBLE);

        // Always keep Task Size Text-Field valid
        tf_taskSize.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue.equals(""))
                return;

            if (!newValue.matches("^[0-9]*[1-9][0-9]*$")) // Invalid number
                tf_taskSize.setText(oldValue);
        });

    }

    public Trie getTrie() { return trie; }
    public ListView<String> getLv_dictionary() { return lv_dictionary; }
    public Map<String, Button> getDictionaryMap() { return dictionaryMap; }
    public Slider getS_agents() { return s_agents; }
}
