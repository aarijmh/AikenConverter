package edu.iqra.converter;

import java.io.File;
import java.io.FileOutputStream;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
	private FileChooser saveFileChooser = new FileChooser();
	private Stage rootStage;

	@Override
	public void start(Stage stage) {
		rootStage = stage;

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
				lblStatus.setText("");
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
						questionBank.get(questionBank.size()-1).setCorrectAnswer(line.split(" ")[1]);
						state.set(0);
						break;
					}
					matcher = pattern.matcher(line);
					if (matcher.find())
						questionBank.get(questionBank.size()-1).getAnswers().put(matcher.group(0).substring(0,1),line.substring(2).strip());
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
	
	private void createHeaderRowOfWorksheet(Sheet sheet)
	{
		/*
		 * Header Creation
		 */
		Row header = sheet.createRow(0);
		 

		Cell headerCell = header.createCell(0);
		headerCell.setCellValue("Question");
		
		for(int i= 1; i < 11; i++)
		{
			header.createCell(i).setCellValue("Option "+i);			
		}
		
		for(int i= 11; i < 15; i++)
		{
			header.createCell(i).setCellValue("Answer "+(i-10));			
		}
		/*
		 * End Header Creation
		 */
	}
	
	private void printToExcel(List<Question> questionBank) throws IOException
	{
		Workbook workbook = new XSSFWorkbook();
		 
		Sheet sheet = workbook.createSheet("Quiz");
		sheet.setColumnWidth(0, 20);
		
		createHeaderRowOfWorksheet(sheet);

		/*
		 * Data Insertion
		 */
		int rowCount = 1;
		for(Question question : questionBank)
		{
			Row header = sheet.createRow(rowCount++);
			header.createCell(0).setCellValue(question.getQuestion());
			int answerCount = 1;
			for(var entry : question.getAnswers().entrySet())
			{
				if(answerCount > 10)
					break;
				header.createCell(answerCount++).setCellValue(entry.getValue());
			}
			header.createCell(11).setCellValue(question.getAnswers().get(question.getCorrectAnswer()));
		}
		
		/*
		 * End Data Insertion
		 */

		saveFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Excel Files ", "*.xlsx"));
		File file = saveFileChooser.showSaveDialog(rootStage);
		
		FileOutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
		workbook.write(outputStream);
		workbook.close();
		lblStatus.setText("Saved Successfully ");
		 
	}

	public static void main(String[] args) {
		launch();
	}

}

class Question{
	private String question;
	private LinkedHashMap<String, String> answers = new LinkedHashMap<>();
	private String correctAnswer;	
	Question(String question)
	{
		this.question = question;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public LinkedHashMap<String, String> getAnswers() {
		return answers;
	}
	public void setAnswers(LinkedHashMap<String, String> answers) {
		this.answers = answers;
	}
	public String getCorrectAnswer() {
		return correctAnswer;
	}
	public void setCorrectAnswer(String correctAnswer) {
		this.correctAnswer = correctAnswer;
	}
	
	
}