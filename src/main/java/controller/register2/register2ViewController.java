package controller.register2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import dbUtil.dbSqlite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import model.fxModel.registerFxModel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.ConfirmBox;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Klasa kontrolera odpowiedzialnego za obsługę okna na karcie Rejestr poza zakresem AP, opisanego w pliku register2View.fxml
 * Działanie analogiczne jak kontrolera registerViewController
 */
public class register2ViewController {
    public  register2ViewController() {}

    //Najpierw tabela z kolumnami do wyswietlania
    @FXML
    private VBox mainVBox;
    @FXML
    private TableView<registerFxModel> registerTableView;
    @FXML
    private TableColumn<registerFxModel, Integer> idRegisterByYearColumn;
    @FXML
    private TableColumn<registerFxModel, String> cardNumberColumn;
    @FXML
    private TableColumn<registerFxModel, String> calibrationDateColumn;
    @FXML
    private TableColumn<registerFxModel, String> instrumentNameColumn;
    @FXML
    private TableColumn<registerFxModel, String> instrumentSerialNumberColumn;
    @FXML
    private TableColumn<registerFxModel, String> instrumentIdentificationNumberColumn;
    @FXML
    private TableColumn<registerFxModel, String> instrumentClientColumn;
    @FXML
    private TableColumn<registerFxModel, String> userWhoCalibrateColumn;
    @FXML
    private TableColumn<registerFxModel, String> certificateNumberColumn;
    @FXML
    private TableColumn<registerFxModel, String> documentKindColumn;
    @FXML
    private TableColumn<registerFxModel, String> stateColumn;
    //Labele do wyświetlania szczegułów może dam to do innego fxmla jak mi się będzie chciało kiedyś
    @FXML
    private Label shortNameLabel;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label addDateLabel;
    @FXML
    private Label addPersonLabel;
    @FXML
    private Label calibrationDateLabel;
    @FXML
    private Label calibrationPersonLabel;
    @FXML
    private Label leftDateLabel;
    @FXML
    private Label leftPersonLabel;
    @FXML
    private Label remarksLabel;
    @FXML
    private TextField searchTextField;
    //Elementy do ładowania danych z tabeli REGISTER
    @FXML
    private ComboBox<String> yearComboBox;
    @FXML
    private ComboBox<String> isStateOkComboBox; //Czy wszystkie czy tylko te co mamy na stanie

    private List<register2Model> registerModelList = new ArrayList<register2Model>();
    private ObservableList<registerFxModel> registerFxObservableList = FXCollections.observableArrayList();
    FilteredList<registerFxModel> filteredRegisterFxObservableList = new FilteredList<>(registerFxObservableList, p -> true); //Lista filtrowana służy do szukania

    private registerFxModel editedRegisterElementFromList;

    public void setEditedRegisterElementFromList(registerFxModel editedRegisterElementFromList) {
        this.editedRegisterElementFromList = editedRegisterElementFromList;
    }
    private editCertificateNumberViewController editCertificateNumberMainController;
    private editCalibrationDateViewController editCalibrationDateMainController;

    private yearModel year;
    private void setYear(yearModel year) {
        this.year = year;
    }

    @FXML
    private void initialize(){
        isStateOkComboBox.getItems().addAll("Wszystkie","ON","OFF");
        isStateOkComboBox.setValue("Wszystkie");
        yearComboBox.setItems(getYearsList());
        yearComboBox.setValue(year.getYear()); //Domyślnie będzie rok bieżący :)
        initializeTableView();
        addFilter();
    }

    public void getRegisterList(){
        try {
            registerFxObservableList.clear();
            Dao<register2Model, Integer> registerDao = DaoManager.createDao(dbSqlite.getConnectionSource(),register2Model.class);
            QueryBuilder<register2Model, Integer> registerQueryBuilder = registerDao.queryBuilder();
            if(isStateOkComboBox.getValue().equals(yearComboBox.getValue())) {     //Tylko kiedy obydwa mają tę samą wartość całkiem przypadkowo :)
                registerModelList = registerDao.queryForAll();
            }else{
                if(!isStateOkComboBox.getValue().equals("Wszystkie")&& yearComboBox.getValue().equals("Wszystkie")){
                    registerQueryBuilder.where().eq("state",isStateOkComboBox.getValue());
                }else if(isStateOkComboBox.getValue().equals("Wszystkie")&& !yearComboBox.getValue().equals("Wszystkie")){
                    registerQueryBuilder.where().like("calibrationDate","%"+yearComboBox.getValue()+"%");
                }else{
                    registerQueryBuilder.where().eq("state",isStateOkComboBox.getValue()).and().like("calibrationDate","%"+yearComboBox.getValue()+"%");
                }
                PreparedQuery<register2Model> prepare = registerQueryBuilder.prepare();
                registerModelList=registerDao.query(prepare);
            }
            Integer indeks = 0;
            for (register2Model registerElement : registerModelList) {
                System.out.println(registerElement.toString());
                registerFxObservableList.add(new registerFxModel(indeks,registerElement.getIdRegisterByYear(),registerElement.getCardNumber(),
                        registerElement.getCalibrationDate(), registerElement.getInstrument().getInstrumentName().getInstrumentName(),
                        registerElement.getInstrument().getSerialNumber(),registerElement.getInstrument().getIdentificationNumber(),
                        registerElement.getInstrument().getClient().getShortName(),registerElement.getUserWhoCalibrate().getLogin(),
                        registerElement.getCertificateNumber(),registerElement.getDocumentKind(),"",registerElement.getState()));
                indeks++;
            }
            dbSqlite.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void initializeTableView(){
        idRegisterByYearColumn.setCellValueFactory(new PropertyValueFactory<>("idRegisterByYear"));
        cardNumberColumn.setCellValueFactory(new PropertyValueFactory<>("cardNumber"));
        calibrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("calibrationDate"));
        instrumentNameColumn.setCellValueFactory(new PropertyValueFactory<>("instrumentName"));
        instrumentSerialNumberColumn.setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        instrumentIdentificationNumberColumn.setCellValueFactory(new PropertyValueFactory<>("identificationNumber"));
        instrumentClientColumn.setCellValueFactory(new PropertyValueFactory<>("client"));
        userWhoCalibrateColumn.setCellValueFactory(new PropertyValueFactory<>("calibratePerson"));
        certificateNumberColumn.setCellValueFactory(new PropertyValueFactory<>("certificateNumber"));
        documentKindColumn.setCellValueFactory(new PropertyValueFactory<>("documentKind"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("state"));
        registerTableView.setItems(filteredRegisterFxObservableList);
        registerTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue!=null) {
                setEditedRegisterElementFromList(newValue);
                showInformationAboutClient(registerModelList.get(editedRegisterElementFromList.getIndexOfRegisterModelList()).getInstrument().getClient());
                showInformationAboutHistory(registerModelList.get(editedRegisterElementFromList.getIndexOfRegisterModelList()).getIdStorehouse());
            }
        });
        registerTableView.prefHeightProperty().bind(mainVBox.heightProperty().multiply(0.7));
    }
    private void addFilter(){
        searchTextField.textProperty().addListener((value,oldValue, newValue) ->{
            filteredRegisterFxObservableList.setPredicate(item -> {
                if (item.getInstrumentName().toUpperCase().contains(newValue.toUpperCase())|| item.getSerialNumber().toUpperCase().contains(newValue.toUpperCase())||
                        item.getIdentificationNumber().toUpperCase().contains(newValue.toUpperCase())||item.getClient().toUpperCase().contains(newValue.toUpperCase())||
                        item.getCalibratePerson().toUpperCase().contains(newValue.toUpperCase())) {
                    return true;
                } else {
                    return false;
                }
            });
        } );
    }
    public ObservableList<String> getYearsList() {
        ObservableList<String> yearObservableList = FXCollections.observableArrayList();
        try {
            Dao<yearModel, Integer> yearDao = DaoManager.createDao(dbSqlite.getConnectionSource(), yearModel.class);
            List<yearModel> yearList = yearDao.queryForAll();
            setYear(yearList.get(yearList.size()-1));
            yearObservableList.add("Wszystkie");
            yearList.forEach(year -> {
                yearObservableList.add(year.getYear());
            });
            dbSqlite.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return yearObservableList;
    }
    @FXML
    public void editCertificateNumber(){
        if(editedRegisterElementFromList!=null && editedRegisterElementFromList.getState().equals("ON")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/register2/editCertificateNumberView.fxml"));
                VBox vBox = loader.load();
                editCertificateNumberMainController = loader.getController();
                if (editCertificateNumberMainController != null){
                    editCertificateNumberMainController.setRegisterMainController(this);
                    editCertificateNumberMainController.setEditedRegisterElement(registerModelList.get(editedRegisterElementFromList.getIndexOfRegisterModelList()));
                    editCertificateNumberMainController.setCertificateNumberTextField(editedRegisterElementFromList.getCertificateNumber());
                }
                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edytuj wybrany przyrząd");
                Scene scene = new Scene(vBox);
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void cancelCalibration(){
        if(editedRegisterElementFromList!=null && editedRegisterElementFromList.getState().equals("ON")) {
            if(ConfirmBox.display("Czy chcesz anulować wzorcowanie ?","Nie da się przywrócić anulowanej pozycji")){
                try {
                    register2Model editedRegisterElement=registerModelList.get(editedRegisterElementFromList.getIndexOfRegisterModelList());
                    editedRegisterElement.setState("OFF");
                    Dao<register2Model, Integer> registerDao = DaoManager.createDao(dbSqlite.getConnectionSource(),register2Model.class);
                    registerDao.update(editedRegisterElement);
                    dbSqlite.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @FXML
    public void editCalibrationDate(){
        if(editedRegisterElementFromList!=null && editedRegisterElementFromList.getState().equals("ON")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/register2/editCalibrationDateView.fxml"));
                VBox vBox = loader.load();
                editCalibrationDateMainController = loader.getController();
                if (editCalibrationDateMainController != null){
                    editCalibrationDateMainController.setRegisterMainController(this);
                    editCalibrationDateMainController.setEditedRegisterElement(registerModelList.get(editedRegisterElementFromList.getIndexOfRegisterModelList()));
                    editCalibrationDateMainController.setCalibrationDateDatePicker(LocalDate.parse(editedRegisterElementFromList.getCalibrationDate()));
                }
                Stage window = new Stage();
                window.initModality(Modality.APPLICATION_MODAL);
                window.setTitle("Edytuj wybrany przyrząd");
                Scene scene = new Scene(vBox);
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @FXML
    public void loadRegister(){
        getRegisterList();
    }
    private void showInformationAboutClient(clientModel client){
        if(client != null){
            shortNameLabel.setText(client.getShortName());
            fullNameLabel.setText(client.getFullName());
            cityLabel.setText(client.getPostCode()+ " "+ client.getCity());
            if(client.getFlatNumber().isEmpty()) {
                streetLabel.setText(client.getStreet() + " " + client.getHouseNumber());
            }else{
                streetLabel.setText(client.getStreet() + " " + client.getHouseNumber()+"/"+client.getFlatNumber());
            }
        }
    }
    private void showInformationAboutHistory(Integer idStorehouse){
        Dao<storehouseModel, Integer> storehouseDao = null;
        List<storehouseModel> result=null;
        try {
            storehouseDao = DaoManager.createDao(dbSqlite.getConnectionSource(), storehouseModel.class);
            QueryBuilder<storehouseModel, Integer> storehouseQueryBuilder = storehouseDao.queryBuilder();
            storehouseQueryBuilder.where().eq("idStorehouse",idStorehouse);
            PreparedQuery<storehouseModel> prepare = storehouseQueryBuilder.prepare();
            result=storehouseDao.query(prepare);
            dbSqlite.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(!result.isEmpty()){
            addDateLabel.setText(result.get(0).getAddDate());
            calibrationDateLabel.setText(result.get(0).getCalibrationDate());
            leftDateLabel.setText(result.get(0).getLeftDate());
            if(result.get(0).getUserWhoAdd()!=null){
                addPersonLabel.setText(result.get(0).getUserWhoAdd().getLogin());
            }else{
                addPersonLabel.setText("");
            }
            if(result.get(0).getUserWhoCalibrate()!=null) {
                calibrationPersonLabel.setText(result.get(0).getUserWhoCalibrate().getLogin());
            }else{
                calibrationPersonLabel.setText("");
            }
            if(result.get(0).getUserWhoLeft()!=null) {
                leftPersonLabel.setText(result.get(0).getUserWhoLeft().getLogin());
            }else{
                leftPersonLabel.setText("");
            }
            if(result.get(0).getRemarks()!=null){
                remarksLabel.setText(result.get(0).getRemarks());
            }else{
                remarksLabel.setText("");
            }
        }
    }
    @FXML
    private void exportToExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet spreadsheet = workbook.createSheet("Arkusz1");
        Row row = spreadsheet.createRow(0);
        //Nazwy kolumn
        row.createCell(0).setCellValue("Lp. ");
        row.createCell(1).setCellValue("Nr karty");
        row.createCell(2).setCellValue("Data wzorcowania");
        row.createCell(3).setCellValue("Nazwa");
        row.createCell(4).setCellValue("Nr fabryczny");
        row.createCell(5).setCellValue("Nr identyfikacyjny");
        row.createCell(6).setCellValue("Zleceniodawca");
        row.createCell(7).setCellValue("Wzorcujący");
        row.createCell(8).setCellValue("Nr Świadectwa/Protokołu");
        row.createCell(9).setCellValue("ŚW/PO");
        row.createCell(10).setCellValue("Stan");

        int i = 0;
        for (registerFxModel registerElement : filteredRegisterFxObservableList) {
            row = spreadsheet.createRow(i + 1);
            row.createCell(0).setCellValue(i+1);
            row.createCell(1).setCellValue(registerElement.getCardNumber());
            row.createCell(2).setCellValue(registerElement.getCalibrationDate());
            row.createCell(3).setCellValue(registerElement.getInstrumentName());
            row.createCell(4).setCellValue(registerElement.getSerialNumber());
            row.createCell(5).setCellValue(registerElement.getIdentificationNumber());
            row.createCell(6).setCellValue(registerElement.getClient());
            row.createCell(7).setCellValue(registerElement.getCalibratePerson());
            row.createCell(8).setCellValue(registerElement.getCertificateNumber());
            row.createCell(9).setCellValue(registerElement.getDocumentKind());
            row.createCell(10).setCellValue(registerElement.getState());
            i++;
        }
        for (int j = 0; j < 11; j++) {
            spreadsheet.autoSizeColumn(j);
        }
        FileOutputStream fileOut = new FileOutputStream("RejestrPozaAP.xlsx");
        workbook.write(fileOut);
        fileOut.close();
    }
}
