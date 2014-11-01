import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//HOW TO CREATE A .JAR FILE (MUST HAVE A CLASS THAT USES public static void main(String[] args))
//copy .class (and.java?) files to jdk1.6.0_22/bin
//make txt file in the same folder named Manifest.txt write "Main-Class: MemoryExperiment" with new lines
//using cmd prompt, cd to jdk1.6.0_22/bin
//jar cfm JARNAME.jar Manifest.txt MemoryExperiment.class

public class MemoryExperiment extends Applet implements ActionListener, Runnable{

	private Thread thread;
	private Graphics dbGraphics;
	private Image dbImage;

	private int time; //centiseconds
	private boolean showingLetters;

	private Button startButton, resetButton, submitButton;
	private Button[] letterButtons;
	private Button[] resetButtons; //for admin use only
	private int buttonCounter;
	private char[] letters;
	private char[][] letterPermus;
	private int permuCounter;
	private boolean suppressed, finished;

	private int[] numCorrect; //0-9 = silence, 10-19 = suppression
	private TextArea resultsArea; //for results

	private Font font;

	public void init(){

		setSize(700,400);
		setBackground(new Color(238,232,170));

		time = 0;
		showingLetters = false;

		startButton = new Button("START");
		startButton.addActionListener(this);
		startButton.setBackground(new Color(221,160,221));
		add(startButton);
		resetButton = new Button("RESET ANSWERS");
		resetButton.addActionListener(this);
		resetButton.setBackground(Color.WHITE);
	    resetButton.setVisible(false);
		add(resetButton);
		submitButton = new Button("SUBMIT ANSWERS");
		submitButton.setVisible(false);
		submitButton.addActionListener(this);
		submitButton.setBackground(new Color(152,251,152));
		add(submitButton);

		letterButtons = new Button[7];
		for(int i=0; i<letterButtons.length; i++){
			letterButtons[i] = new Button();
			letterButtons[i].addActionListener(this);
			letterButtons[i].setBackground(Color.WHITE);
			letterButtons[i].setVisible(false);
			add(letterButtons[i]);
		}
		buttonCounter = 0;

		resetButtons = new Button[2];
		resetButtons[0] = new Button("RESET TRIAL");
		resetButtons[0].addActionListener(this);
		resetButtons[0].setBackground(new Color(240, 50, 50));
		add(resetButtons[0]);
		resetButtons[1] = new Button("RESET SUBJECT");
		resetButtons[1].addActionListener(this);
		resetButtons[1].setBackground(new Color(240, 50, 50));
		add(resetButtons[1]);

		font = new Font("Comic Sans MS", Font.BOLD, 24);

		letters = new char[]{'F','K','L','M','Q','R','X'};
		letterPermus = new char[20][7];
		setPermus();
		permuCounter = 0;
		suppressed = false;
		finished = false;

		numCorrect = new int[20];
		resultsArea = new TextArea("Number of correct letters (out of 7):\n",10,28,TextArea.SCROLLBARS_VERTICAL_ONLY);
		resultsArea.setFocusable(false);
		resultsArea.setEditable(false);
		add(resultsArea);
	}

	public void paint(Graphics g){

		setButtonBounds();
		g.setFont(font);

		if(startButton.isVisible()){

			if(suppressed){

				g.drawString("Begin speaking.", 280,180);
				g.drawString("You may stop when entering your answers.",140,220);
			}
			else g.drawString("Please be quiet.",280,180);
			g.drawString("Press START to see the letters...", 160,50);
		}

		if(showingLetters){

			for(int i=0; i<7; i++){

				if(time < (100*(i+1))){

					g.drawString(letterPermus[permuCounter][i] + "", 330,200);
					break;
				}
			}
		}

		if(resetButton.isVisible()){

			for(int i=0; i<7; i++) g.drawString(letters[i] + "", 178+(60*i),195);
		}

		if(finished){

			g.drawString("Thank you for your participation.",160,200);
		}
	}

	public void update(Graphics g){

		if(dbImage == null){

			dbImage = createImage(getSize().width, getSize().height);
			dbGraphics = dbImage.getGraphics();
		}

		dbGraphics.setColor(getBackground());
		dbGraphics.fillRect(0, 0, getSize().width, getSize().height);
		dbGraphics.setColor(getForeground());
		paint(dbGraphics);

		g.drawImage(dbImage, 0, 0, this);
	}

	public void actionPerformed(ActionEvent e){

		Object source = e.getSource();

		if(source == startButton){

			startButton.setVisible(false);
			showingLetters = true;
		}

		for(int i=0; i<letterButtons.length; i++){

			if(source == letterButtons[i] && letterButtons[i].getLabel().equals("")){

				letterButtons[i].setLabel((buttonCounter+1) + "");
				buttonCounter++;

				if(buttonCounter == 7) submitButton.setVisible(true);
			}
		}

		if(source == resetButton){

			buttonCounter = 0;
			for(Button b : letterButtons) b.setLabel("");
			submitButton.setVisible(false);
		}

		if(source == submitButton){

			time = 0;

			char[] userSubmission = new char[7];

			for(int i=0; i<letterButtons.length; i++){

				int check = -1;

				for(int j=0; j<letterButtons.length; j++){

					if(Integer.parseInt(letterButtons[j].getLabel()) == i+1){

						check = j;
						break;
					}
				}

				userSubmission[i] = letters[check]; //works because alphabetical order
			}

			for(int i=0; i<7; i++){

				if(userSubmission[i] == letterPermus[permuCounter][i]) numCorrect[permuCounter]++;
			}

			for(int i=0; i<7; i++){

				letterButtons[i].setLabel("");
				letterButtons[i].setVisible(false);
			}
			buttonCounter = 0;
			resetButton.setVisible(false);
			submitButton.setVisible(false);
			permuCounter++;
			if(permuCounter == 10) suppressed = true;
			if(permuCounter == 20) finished = true;
			else startButton.setVisible(true);

			if(finished){

				int totalS = 0, totalA = 0;

				resultsArea.setText(resultsArea.getText() + "\nSilence:\n");
				for(int i=0; i<10; i++){ resultsArea.setText(resultsArea.getText() + "Trial " + (i+1) + ": " + numCorrect[i] + "\n"); totalS += numCorrect[i]; }
				resultsArea.setText(resultsArea.getText() + "\nArticulatory Suppression:\n");
				for(int i=10; i<20; i++){ resultsArea.setText(resultsArea.getText() + "Trial " + (i-9) + ": " + numCorrect[i] + "\n"); totalA += numCorrect[i]; }
				resultsArea.setText(resultsArea.getText() + "\n%ASE = (" + totalS + "-" + totalA + ") / " + totalS + " = " + (double)(totalS - totalA) / (double)(totalS));

			}
		}

		if(source == resetButtons[0] || source == resetButtons[1]){

			startButton.setVisible(true);
			submitButton.setVisible(false);
			resetButton.setVisible(false);
			for(Button b : letterButtons){ b.setLabel(""); b.setVisible(false); }
			buttonCounter = 0;
			time = 0;
			showingLetters = false;

			if(source == resetButtons[1]){

				permuCounter = 0;
				suppressed = false;
				finished = false;
				numCorrect = new int[20];
			}
		}
	}

	public void setButtonBounds(){

		startButton.setBounds(290,310,150,40);
		resetButton.setBounds(290,270,150,40);
		submitButton.setBounds(290,320,150,40);
		for(int i=0; i<letterButtons.length; i++) letterButtons[i].setBounds(170 + (60*i), 200, 30, 30);
		for(int i=0; i<resetButtons.length; i++) resetButtons[i].setBounds(750, 5 + (40*i), 130,30);
		resultsArea.setBounds(710, 100, 216, 230);
	}

	public void setPermus(){ //randomly generated by other program and copied

		letterPermus[0] = new char[]{'Q','F','K','M','R','L','X'};
		letterPermus[1] = new char[]{'R','K','Q','X','M','F','L'};
		letterPermus[2] = new char[]{'X','K','M','R','L','F','Q'};
		letterPermus[3] = new char[]{'X','F','Q','L','R','M','K'};
		letterPermus[4] = new char[]{'M','K','X','Q','L','F','R'};
		letterPermus[5] = new char[]{'X','F','R','Q','L','K','M'};
		letterPermus[6] = new char[]{'L','K','Q','F','R','M','X'};
		letterPermus[7] = new char[]{'Q','K','X','F','M','L','R'};
		letterPermus[8] = new char[]{'R','L','Q','F','M','X','K'};
		letterPermus[9] = new char[]{'Q','L','M','F','K','X','R'};
		letterPermus[10] = new char[]{'K','Q','R','F','X','L','M'};
		letterPermus[11] = new char[]{'Q','L','F','K','R','X','M'};
		letterPermus[12] = new char[]{'X','M','F','Q','K','L','R'};
		letterPermus[13] = new char[]{'Q','F','K','M','L','R','X'};
		letterPermus[14] = new char[]{'X','F','K','L','M','R','Q'};
		letterPermus[15] = new char[]{'R','F','X','M','L','K','Q'};
		letterPermus[16] = new char[]{'Q','R','X','L','K','M','F'};
		letterPermus[17] = new char[]{'K','X','F','L','M','Q','R'};
		letterPermus[18] = new char[]{'K','Q','F','L','R','M','X'};
		letterPermus[19] = new char[]{'X','Q','K','F','R','L','M'};
	}

	public void start(){

		if(thread == null){

			thread = new Thread(this);
			thread.start();
		}
	}

	public void run(){

		while(thread != null){

			repaint();

			try{
				Thread.sleep(20);

				if(showingLetters) time += 2;

				if(time == 700){

					showingLetters = false;
					for(Button b : letterButtons) b.setVisible(true);
					resetButton.setVisible(true);
				}
			}
			catch(InterruptedException e){
			}
		}
	}

	public void stop(){

		thread = null;
	}

	public static void main(String[] args){

		JFrame frame = new JFrame("Psych Experiment");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(700,400);

		Applet thisApplet = new MemoryExperiment();
		thisApplet.init();
		frame.setLayout(new BorderLayout());
		frame.getContentPane().add(thisApplet, BorderLayout.CENTER);
		frame.setVisible(true);
		thisApplet.start();
	}
}
