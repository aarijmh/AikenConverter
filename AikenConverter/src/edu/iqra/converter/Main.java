package edu.iqra.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {

	private Label lblStatus;
	private TextField fileNameText;

	@Override
	public void start(Stage stage) {

		HBox topRow = new HBox();

		topRow.setSpacing(10.0);

		final ToggleGroup group = new ToggleGroup();

		RadioButton rb1 = new RadioButton("Aiken");
		rb1.setToggleGroup(group);
		rb1.setSelected(true);

		RadioButton rb2 = new RadioButton("XML");
		rb2.setToggleGroup(group);

		topRow.getChildren().addAll(rb1, rb2);

		Label topLabel = new Label("Select the input formal");
		;
		Label middleLable = new Label("Select file");

		FileChooser fileChooser = new FileChooser();
		fileNameText = new TextField();
		fileNameText.setEditable(false);
		Button btnFileChooser = new Button("Select File");

		HBox middleRow = new HBox();
		middleRow.getChildren().addAll(fileNameText, btnFileChooser);
		middleRow.setSpacing(10.0);

		btnFileChooser.setOnAction(x -> {
			File selectedFile = fileChooser.showOpenDialog(stage);
			if (selectedFile != null && selectedFile.canRead()) {
				fileNameText.setText(selectedFile.getAbsolutePath());
			}

		});

		HBox endRow = new HBox();
		endRow.setSpacing(10.0);

		Button btnConvert = new Button("Convert");

		lblStatus = new Label("");

		endRow.getChildren().addAll(btnConvert, lblStatus);

		btnConvert.setOnAction(x -> {

			if (rb1.isSelected())
				convertAiken();
		});

		VBox root = new VBox();

		root.getChildren().addAll(topLabel, topRow, middleLable, middleRow, new Label("Convert to excel"), endRow);

		root.setSpacing(10.0);
		root.setPadding(new Insets(50));

		Scene scene = new Scene(root, 320, 280);
		stage.setScene(scene);
		stage.show();
	}

	private void convertAiken() {
		Pattern pattern = Pattern.compile("^[A-Z][).]", Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		List<Question> questionBank = new ArrayList<>();
		try  {
			List<String> stream = Files.readAllLines(Paths.get(fileNameText.getText()));
			AtomicInteger state = new AtomicInteger(0);
			for(String line : stream) {
				switch (state.get()) {
				case 0:
					if(!line.strip().equals(""))
					{
						questionBank.add(new Question(line));
						state.set(1);
					}
					break;
				case 1:
					if(line.startsWith("ANSWER"))
					{
						questionBank.get(questionBank.size()-1).correctAnswer = line.split(" ")[1];
						state.set(0);
						break;
					}
					matcher = pattern.matcher(line);
					if (matcher.find())
						questionBank.get(questionBank.size()-1).answers.put(matcher.group(0).substring(0,1),line.substring(2).strip());
					break;
				default:
					break;
				}
			};
			
			printToExcel(questionBank);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private void printToExcel(List<Question> questionBank)
	{
		Workbook workbook = new XSSFWorkbook();
		 
		Sheet sheet = workbook.createSheet("Persons");
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 4000);
		 
		Row header = sheet.createRow(0);
		 
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		 
		XSSFFont  font = ((XSSFWorkbook) workbook).createFont();
		font.setFontName("Arial");
		font.setFontHeightInPoints((short) 16);
		font.setBold(true);
		headerStyle.setFont(font);
		 
		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Name");
		headerCell.setCellStyle(headerStyle);
		 
		headerCell = header.createCell(1);
		headerCell.setCellValue("Age");
		headerCell.setCellStyle(headerStyle);
	}

	public static void main(String[] args) {
		launch();
	}

}

class Question{
	String question;
	LinkedHashMap<String, String> answers = new LinkedHashMap<>();
	String correctAnswer;	
	Question(String question)
	{
		this.question = question;
	}
}