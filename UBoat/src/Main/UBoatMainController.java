package Main;

import DTOs.DTO_CandidateResult;
import DTOs.DTO_CodeDescription;
import DTOs.DTO_MachineInfo;
import codeCalibration.CodeCalibrationController;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import encryptMessage.EncryptMessageController;
import http.HttpClientUtil;
import jakarta.servlet.http.HttpServletResponse;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import login.LoginController;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import setCode.SetCodeController;
import utils.Constants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static utils.Constants.GSON_INSTANCE;

public class UBoatMainController {

    @FXML private CodeCalibrationController codeCalibrationComponentController;
    @FXML private VBox codeCalibrationComponent;
    @FXML private EncryptMessageController encryptMessageComponentController;
    @FXML private VBox encryptMessageComponent;
    @FXML private Button btn_loadFile, btn_logOut;
    @FXML private TextField tf_filePath;
    @FXML private Label lb_battlefieldName;
    @FXML private TextArea ta_machineDetails, ta_candidates, ta_teamsDetails;
    @FXML private ScrollPane sp_mainPage;
    @FXML private Tab tab_machine, tab_contest;

    private final StringProperty userName;
    private String battlefieldName;
    private Node rootNode;
    private LoginController loginComponentController;
    private SetCodeController setCodeComponentController;
    private final BooleanProperty isXmlLoaded = new SimpleBooleanProperty(false);
    private final BooleanProperty isCodeChosen = new SimpleBooleanProperty(false);
    private BooleanProperty isReady,isBattleReady;
    private DTO_MachineInfo dto_machineInfo;
    private TimerTask resultRefresher;
    private TimerTask readyRefresher;
    private Timer timerResult,timerReady;

    //private Parent uBoatComponent;


    public UBoatMainController() {
        userName = new SimpleStringProperty("Anonymous");
        isReady = new SimpleBooleanProperty(false);
        isBattleReady = new SimpleBooleanProperty(false);
        //sp_mainPage.setContent(loginComponentController.getLoginPage());
    }

    public void setLoginController(LoginController loginController) {
        this.loginComponentController = loginController;
        loginController.setMainController(this);
    }

    public void setSetCodeController(SetCodeController setCodeController) {
        setCodeComponentController = setCodeController;
        setCodeController.setMainController(this);
    }
    public void setContentScene(){
        sp_mainPage.setContent(loginComponentController.getLoginPage());
    }

    @FXML public void initialize() {

        if (codeCalibrationComponentController != null &&
            encryptMessageComponentController  != null) {

            codeCalibrationComponentController.setMainController(this);
            encryptMessageComponentController.setMainController(this);
        }

        tab_contest.setDisable(true);
        isXmlLoaded.set(false);

        tab_contest.disableProperty().bind(isXmlLoaded.not().or(isCodeChosen.not()));

        isXmlLoaded.addListener((observable, oldValue, newValue) -> {
            codeCalibrationComponentController.enableDisableCodeCalibrationButtons(newValue);
            setMachineDetailsTextArea(newValue);

            if (newValue) {
                initializeTrieWithDictionary();
            }
        });
        isCodeChosen.addListener((observable, oldValue, newValue) -> {
            codeCalibrationComponentController.enableDisableCodeCalibrationButtons(newValue);
            setMachineConfigurationTextField(newValue);
        });
        rootNode = sp_mainPage.getContent();
        isBattleReady.addListener((observable, oldValue, newValue) -> {
            if(newValue)
                refresherResult();
        });
    }

    @FXML void loadFileBtnClick(ActionEvent event) {

        isXmlLoaded.set(false);
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));

        File fileSelected = fileChooser.showOpenDialog(/*primaryStage*/null); // TODO: add stage

        if (fileSelected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could NOT choose a file!");
            alert.show();
            return;
        }
        RequestBody body =
                new MultipartBody.Builder()
                        .addFormDataPart("file1", fileSelected.getName(), RequestBody.create(fileSelected, MediaType.parse("text/plain")))
                        .build();

        String finalUrl = HttpUrl
                .parse(Constants.LOAD_XML)
                .newBuilder()
                .addQueryParameter(Constants.USERNAME, userName.getValue())
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("on fail" + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String body = response.body().string();
                if (response.code() == HttpServletResponse.SC_OK) {
                    isXmlLoaded.set(true);
                    //battlefieldName = response.body().string();
                    Platform.runLater(() -> {
                        tf_filePath.setText(fileSelected.getAbsolutePath());
                        battlefieldName = body;
                        lb_battlefieldName.setText(battlefieldName);
                    });
                } else { // TODO: fix - don't work? - WORK
                    Platform.runLater(() -> {
                        String errorMsg = null;
                        //try {
                            errorMsg = body;
                        //} catch (IOException e) {
                         //   throw new RuntimeException(e);
                        //}
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, errorMsg);
                        errorAlert.show();
                    });
                }
            }
        });
    }
    public void updateReadyManager(){
        String finalUrl = HttpUrl
                .parse(Constants.READY)
                .newBuilder()
                .addQueryParameter(Constants.CLASS_TYPE,Constants.UBOAT_CLASS)
                .addQueryParameter(Constants.SENTENCE_TO_CHECK,encryptMessageComponentController.getTf_output().getText())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String text = response.body().string();  // this is decode msg
                Platform.runLater(() -> {

                });
            }
        });
    }

    @FXML void logOutBtnClick(ActionEvent event) {

    }

    public void buildSpecificMachine(DTO_CodeDescription dto_codeDescription) {

        Gson gson = new Gson();
        String json_dtoCodeDescription = gson.toJson(dto_codeDescription);
        RequestBody bodyDto = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("dto_codeConfiguration", json_dtoCodeDescription)
                .build();

        String finalUrl = HttpUrl
                .parse(Constants.SET_CODE)
                .newBuilder()
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(bodyDto)
                .build();

        Call call = HttpClientUtil.HTTP_CLIENT.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    isCodeChosen.set(false);
                    isCodeChosen.set(true);
                }
            }
        });
    }

    public void codeCalibrationController_randomCodeBtnClick() {
        //TODO : CREATE RANDOM MACHINE

        String finalUrl = HttpUrl
                .parse(Constants.SET_CODE)
                .newBuilder()
                .addQueryParameter(WebConstants.Constants.CODE_TYPE, WebConstants.Constants.RANDOM_SET_CODE_TYPE) // TODO: constant
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /*String text = response.body().string();  // this is decode msg
                Platform.runLater(() -> {
                    encryptMessageComponentController.getTf_output().setText(text);
                });*/

                if (response.code() == 200) {
                    isCodeChosen.set(true);
                }
            }
        });


    }

    public void EncryptMessageController_processBtnClick(String msgToDecode){

        String finalUrl = HttpUrl
                .parse(Constants.ENCRYPT_CODE)
                .newBuilder()
                .addQueryParameter("decodeMsg",msgToDecode ) // TODO: constant
                .build()
                .toString();
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String text = response.body().string();  // this is decode msg
                Platform.runLater(() -> {
                    encryptMessageComponentController.getTf_output().setText(text);
                });
            }
        });

    }
    private void setMachineDetailsTextArea(Boolean newValue) {

        // request for dto
        String finalUrl = HttpUrl
                .parse(Constants.DTO)
                .newBuilder()
                .addQueryParameter("dtoType", "machineInfo") // TODO: constant
                //.addQueryParameter(Constants.USERNAME, userName.getValue())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json_dto = response.body().string();
                Type dtoType = new TypeToken<DTO_MachineInfo>() { }.getType();
                dto_machineInfo = GSON_INSTANCE.fromJson(json_dto, dtoType);
                String text = newValue ? createMachineInfoAsString() : "";
                Platform.runLater(() -> {
                    ta_machineDetails.setText(text);
                });
            }
        });
    }
    public String createMachineInfoAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Machine Status:\n");
        sb.append("1.    a) Number of Possible Rotors: " + dto_machineInfo.getNumOfPossibleRotors());
        sb.append("\n      b) Number of Rotors in use: " + dto_machineInfo.getNumOfUsedRotors());
        sb.append("\n2. Number of Reflectors: " + dto_machineInfo.getNumOfReflectors());
        return sb.toString();
    }
    private void setMachineConfigurationTextField(Boolean newValue) {

        // request for dto
        String finalUrl = HttpUrl
                .parse(Constants.DTO)
                .newBuilder()
                .addQueryParameter("dtoType", "machineConfiguration") // TODO: constant
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String text = newValue ? response.body().string() : "";
                Platform.runLater(() -> {
                    encryptMessageComponentController.getTf_codeConfiguration().setText(text);
                });
            }
        });
    }

    public void initializeTrieWithDictionary() {

        String finalUrl = HttpUrl
                .parse(Constants.DTO)
                .newBuilder()
                .addQueryParameter(WebConstants.Constants.DTO_TYPE, WebConstants.Constants.DICTIONARY)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Ohhhhh NOOOOOOOOOOOO !!!!! TIREEE");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String json_dictionary = response.body().string();
                Type dictionaryType = new TypeToken<Set<String>>() { }.getType(); // TODO: FIX !!!
                Set<String> dictionary = GSON_INSTANCE.fromJson(json_dictionary, dictionaryType);

                for (String word : dictionary) {
                    encryptMessageComponentController.getTrie().insert(word);
                    encryptMessageComponentController.getLv_dictionary().getItems().add(word);
                }
            }
        });
    }

    public void setUserName(String userName){
        this.userName.set(userName);
    }

    public void switchToMainPanel() {
        sp_mainPage.setContent(rootNode);
    }

    public void switchToSetCodePanel() {
        sp_mainPage.setContent(setCodeComponentController.getMainPage());
    }
    public void updateSetCodePanel() {
        setCodeComponentController.createSetCodeController(dto_machineInfo);
    }
    public void setIsReady(boolean isReady) {
        this.isReady.set(isReady);
    }

    public void codeSetController_setBtnClick() {

        // if I didnt fix to disable set button, add here a check like ex2
        List<Character> startPositionList = new ArrayList<>();
        List<Pair<String , Pair<Integer,Integer>>> rotorsIDList = createIDListForRotors(startPositionList);
        String reflectorID = setCodeComponentController.getReflector().getValue();
        List<Pair<Character, Character>> plugBoard = new ArrayList<>(); // empty (Not supported)
        DTO_CodeDescription res = new DTO_CodeDescription(dto_machineInfo.getABC(),rotorsIDList,startPositionList,reflectorID,plugBoard);
        if (searchErrorInitInput(rotorsIDList,reflectorID)) {
            return;
        }
        buildSpecificMachine(res);

        //update Code Configuration in all sub-components     // I think I already did it
/*        dto_codeDescription = engine.createCodeDescriptionDTO();
        String codeConfigurationText = createDescriptionFormat(dto_codeDescription);
        encryptDecryptController.getTa_codeConfiguration().setText(codeConfigurationText);
        tf_machineConfiguration.setText(codeConfigurationText);*/
    }

    private List<Pair<String ,Pair<Integer,Integer>>> createIDListForRotors(List<Character> startPositionList) {

        List<Pair<String ,Pair<Integer,Integer>>> rotorsIDList = new ArrayList<>();
        for(Pair<ChoiceBox<String>, ChoiceBox<Character>> pair :setCodeComponentController.getRotorsChoiceBoxes()){
            String curRotorId = pair.getKey().getValue();
            int curNotch = dto_machineInfo.getNotchPositionList().get(Integer.parseInt(curRotorId) -1);
            char curStartPosition = pair.getValue().getValue();
            rotorsIDList.add(new Pair<> (curRotorId,new Pair<>(curNotch,dto_machineInfo.getABCOrderOfSpecificRotor(Integer.parseInt(curRotorId) -1).indexOf(curStartPosition))));
            startPositionList.add(pair.getValue().getValue());
        }
        Collections.reverse(rotorsIDList);
        Collections.reverse(startPositionList);
        return rotorsIDList;
    }

    private boolean searchErrorInitInput(List<Pair<String, Pair<Integer, Integer>>> rotorsIDList, String reflectorID) {

        if(!checkRotorsIDList(rotorsIDList))
            return true;

        if(reflectorID == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"you not enter reflector ID");
            alert.show();
            return true;
        }
        return false;
    }

    private boolean checkRotorsIDList(List<Pair<String, Pair<Integer, Integer>>> rotorsIDList) {

        Set<String> set = new HashSet<>();
        if(rotorsIDList.size() < dto_machineInfo.getNumOfUsedRotors())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Error - You should select exactly " + dto_machineInfo.getNumOfUsedRotors()
                    + " Rotors!");
            alert.show();
            return false;
        }
        for (Pair<String, Pair<Integer, Integer>> id : rotorsIDList) {
            if (set.contains(id.getKey())) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "you enter duplicate rotor");
                alert.show();
                return false;
            }
            else
                set.add(id.getKey());
        }
        return true;
    }

    private void refresherResult(){
        resultRefresher = new TimerTask() {
            @Override
            public void run() {
                String finalUrl = HttpUrl
                        .parse(Constants.RESULT)
                        .newBuilder()
                        .addQueryParameter(Constants.CLASS_TYPE,Constants.UBOAT_CLASS)
                        .build()
                        .toString();
                HttpClientUtil.runAsync(finalUrl, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        if (response.code() == 200) {
                            String json_candidates = response.body().string();
                            Type setCandidatesType = new TypeToken<Set<DTO_CandidateResult>>() { }.getType(); // TODO: FIX !!!
                            Set<DTO_CandidateResult> setCandidates = GSON_INSTANCE.fromJson(json_candidates, setCandidatesType);
                            ta_candidates.clear();
                            for(DTO_CandidateResult dto_candidateResult : setCandidates){
                                Platform.runLater(() -> {
                                    ta_candidates.appendText("-----------------------------------------\n");
                                    ta_candidates.appendText(dto_candidateResult.getPrintedFormat());
                                    ta_candidates.appendText("-----------------------------------------\n");
                                });
                            }
                        }
                    }
                });
            }
        };
        timerResult = new Timer();
        timerResult.schedule(resultRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    public void checkReadyRefresher() {
        readyRefresher = new TimerTask() {
            @Override
            public void run() {
                String finalUrl = HttpUrl
                        .parse(Constants.CHECK_READY_BATTLE)
                        .newBuilder()
                        .addQueryParameter(Constants.CLASS_TYPE,Constants.UBOAT_CLASS)
                        .build()
                        .toString();
                HttpClientUtil.runAsync(finalUrl, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.code() == 200) {
                            isBattleReady.set(true);
                        }
                    }
                });
            }
        };
        timerReady = new Timer();
        timerReady.schedule(readyRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

}