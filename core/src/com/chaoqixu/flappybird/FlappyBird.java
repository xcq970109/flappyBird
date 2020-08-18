package com.chaoqixu.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;
	Texture background;
	Texture bottomTube;
	Texture topTube;
	Texture gameOver;
	Texture scoreBoard;
	Texture startTexture;
	Texture hardOnTexture;
	Texture hardOffTexture;
	Texture hard;
	Texture ground;
	Texture logo;
	Texture[] numbers;
	TextureRegion[] bird;

	Button startButton;
	Button hardButton;

	final int cycle = 15;
	final int gravity = 1;
	int birdWidth;
	int birdHeight;
	int clock;
	int screenWidth;
	int screenHeight;
	int tubeHeight;
	int birdsY;
	int velocity = 0;
	int gameState = 0;
	int pipeVelocity;
	int tubeDistance;
	int tubeDistance2;
	int topTube1X;
	int topTube1Y;
	int topTube2X;
	int topTube2Y;
	int birdsX;
	int score;
	int highScore;
	int time = 0;
	int[] oscillator;

	boolean record = false;
	boolean hardMode = false;

	Random random = new Random();
	Preferences preferences;
	Stage stage;

	@Override
	public void create () {
		batch = new SpriteBatch();
		background = new Texture("bg.png");
		gameOver = new Texture("gameover.png");
		scoreBoard = new Texture("score.png");
		hard = new Texture("HARD.png");
		bottomTube = new Texture("bottomtube.png");
		topTube = new Texture("toptube.png");
		numbers = new Texture[10];
		ground = new Texture("ground.png");
		logo = new Texture("flappybirdlogo.png");
		for(int i = 0; i <10; i++){
			numbers[i] = new Texture(Integer.toString(i)+".png");
		}

		bird = new TextureRegion[2];
		bird[0] = new TextureRegion( new Texture("bird.png"));
		bird[1] = new TextureRegion( new Texture("bird2.png"));

		clock = 0;
		tubeHeight = bottomTube.getHeight();
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		birdWidth = bird[clock / cycle].getRegionWidth() ;
		birdHeight = bird[clock / cycle].getRegionHeight();
		birdsX = (screenWidth-birdWidth)/2;
		birdsY = (screenHeight-birdHeight)/2;
		tubeDistance = bird[0].getRegionHeight()*6;
		tubeDistance2 = bird[0].getRegionHeight()*6;
		pipeVelocity = screenWidth/95;
		oscillator = new int[]{0, 1, 2, 3,4,5,4,3,2,1,0,-1,-2,-3,-4,-5,-4,-3,-2,-1,0};

		preferences = Gdx.app.getPreferences("My preferences");

		stage = new Stage(new StretchViewport(screenWidth, screenHeight));
		Gdx.input.setInputProcessor(stage);

		Pixmap originalStartButton = new Pixmap(Gdx.files.internal("startButton2.png"));
		int buttonHeight = originalStartButton.getHeight();
		int buttonWidth = originalStartButton.getWidth();
		Pixmap scaledStartButton = new Pixmap(screenWidth/5, screenWidth/5*buttonHeight/buttonWidth, originalStartButton.getFormat());
		scaledStartButton.drawPixmap(originalStartButton,
				0, 0, buttonWidth, buttonHeight,
				0, 0, scaledStartButton.getWidth(), scaledStartButton.getHeight()
		);

		startTexture = new Texture(scaledStartButton);
		Button.ButtonStyle style = new Button.ButtonStyle();
		style.up =  new TextureRegionDrawable(new TextureRegion(startTexture));
		startButton = new Button(style);

		startButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				setGameRunning();
			}
		});

		Pixmap originalHardButtonOn = new Pixmap(Gdx.files.internal("hardOn.png"));
		Pixmap scaledHardButtonOn = new Pixmap(screenWidth/5, screenWidth/5*buttonHeight/buttonWidth, originalStartButton.getFormat());
		scaledHardButtonOn.drawPixmap(originalHardButtonOn,
				0, 0, buttonWidth, buttonHeight,
				0, 0, scaledStartButton.getWidth(), scaledStartButton.getHeight()
		);

		Pixmap originalHardButtonOff = new Pixmap(Gdx.files.internal("hardOff.png"));
		Pixmap scaledHardButtonOff = new Pixmap(screenWidth/5, screenWidth/5*buttonHeight/buttonWidth, originalStartButton.getFormat());
		scaledHardButtonOff.drawPixmap(originalHardButtonOff,
				0, 0, buttonWidth, buttonHeight,
				0, 0, scaledStartButton.getWidth(), scaledStartButton.getHeight()
		);

		hardOffTexture = new Texture(scaledHardButtonOff);
		hardOnTexture = new Texture(scaledHardButtonOn);
		Button.ButtonStyle style2 = new Button.ButtonStyle();
		style2.checked = new TextureRegionDrawable(new TextureRegion(hardOnTexture));
		style2.up = new TextureRegionDrawable(new TextureRegion(hardOffTexture));
		hardButton = new Button(style2);

		hardButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
			}
		});
		hardButton.setChecked(false);

		preferences.clear();
		preferences.flush();
	}

	@Override
	public void render () {
		//speed change
		if(score/50%3==0){
			pipeVelocity = screenWidth/95;
		}
		else if (score/50%3 ==1){
			pipeVelocity = screenWidth/90;
		}
		else{
			pipeVelocity = screenWidth/100;
		}

		//timer
		if(clock > cycle*2-1)
			clock = 0;

		//movement;
		if(gameState == 1){
			if(Gdx.input.justTouched()){
				velocity = -20;
			}else{
				velocity += gravity;
			}

			if( birdsY > 0 || velocity < 0){
				birdsY -= velocity;
			}
		}

		//start rendering;
		batch.begin();
		batch.draw(background,0,0,screenWidth,screenHeight);

		//Flying postures
		if(gameState == 0){
			batch.draw(bird[clock/cycle],birdsX,birdsY);
		}
		else if(velocity < 0) {
			if(gameState == 1)
				batch.draw(bird[clock / cycle], birdsX, birdsY, birdWidth/2, birdHeight/ 2, birdWidth, birdHeight, 1, 1, 30);
			else
				batch.draw(bird[0], birdsX, birdsY, birdWidth/2, birdHeight/ 2, birdWidth, birdHeight, 1, 1, 30);
		}
		else {
			if(gameState == 1)
				batch.draw(bird[clock / cycle], birdsX, birdsY, birdWidth/2, birdHeight/ 2, birdWidth, birdHeight, 1, 1, 330);
			else
				batch.draw(bird[0], birdsX, birdsY, birdWidth/2, birdHeight/ 2, birdWidth, birdHeight, 1, 1, 330);
		}

		//draw tube
		if(hardMode && gameState ==1){
			topTube1Y += oscillating();
			topTube2Y += oscillating();
		}

		if(gameState > 0){
			batch.draw(topTube, topTube1X, topTube1Y);
			batch.draw(bottomTube, topTube1X, topTube1Y - tubeDistance - tubeHeight);
			batch.draw(topTube, topTube2X, topTube2Y);
			if(!hardMode)
				batch.draw(bottomTube, topTube2X, topTube2Y - tubeDistance - tubeHeight);
			else
				batch.draw(bottomTube, topTube2X, topTube2Y - tubeDistance2 - tubeHeight);
		}
		batch.draw(ground,0,0,screenWidth,ground.getHeight()/2);

		//start screen
		if(gameState == 0){
			batch.draw(logo,screenWidth/2-logo.getWidth()/2,screenHeight*3/5);
			stage.addActor(startButton);
			stage.addActor(hardButton);
			hardButton.setPosition(screenWidth/2+screenWidth/54,screenHeight*1/5);
			startButton.setPosition((screenWidth/2-screenWidth/5-screenWidth/54),screenHeight*1/5);
			stage.draw();
		}

		//game over stage
		if(gameState == 2){
			batch.draw(gameOver,(screenWidth-gameOver.getWidth()*3)/2,screenHeight*3/4,gameOver.getWidth()*3,gameOver.getHeight()*3);
			batch.draw(scoreBoard,(screenWidth/2-scoreBoard.getWidth()*5/4),screenHeight/2,scoreBoard.getWidth()*5/2,scoreBoard.getHeight()*5/2);

			if(hardMode)
				batch.draw(hard,(screenWidth/2-screenWidth/5/2),screenHeight/2+scoreBoard.getHeight()*3/2,screenWidth/5,screenWidth/5*hard.getHeight()/hard.getWidth());
			drawScoreBoard();

			stage.addActor(startButton);
			stage.addActor(hardButton);
			hardButton.setPosition(screenWidth/2+screenWidth/54,screenHeight*2/5);
			startButton.setPosition((screenWidth/2-screenWidth/5-screenWidth/54),screenHeight*2/5);
			stage.draw();
		}

		//scoring and collision detection
		if(gameState == 1) {
			//hard mode check
			hardMode = hardButton.isChecked();

			topTube1X -= pipeVelocity;
			topTube2X = topTube1X+(screenWidth/2)+ topTube.getWidth();

			//pipe disappear
			if(topTube1X + topTube.getWidth()<0){
				topTube1X = topTube2X;
				topTube1Y = topTube2Y;
				topTube2X = topTube1X+(screenWidth/2)+ topTube.getWidth();
				topTube2Y = random.nextInt(screenHeight/3)+screenHeight*11/20;
				if(hardMode) {
					tubeDistance = tubeDistance2;
					tubeDistance2 = bird[0].getRegionHeight() * 4 + random.nextInt(bird[0].getRegionHeight()*2);
				}
				record = false;
			}

			//scoring
			if(birdsX > topTube1X+topTube.getWidth() && !record){
				record = true;
				++score;
				System.out.println("You score is "+score);
			}

			//collision detection
			else if(birdsY < ground.getHeight()/2){
				setGameOver();
			}
			else if( (topTube1X <= birdsX && birdsX <= topTube1X+topTube.getWidth()) || (topTube1X <= birdsX+bird[0].getRegionWidth() && birdsX + bird[0].getRegionWidth() <= topTube1X+topTube.getWidth()) ){
				if(birdsY+bird[0].getRegionHeight() > topTube1Y || birdsY < topTube1Y-tubeDistance){
					setGameOver();
				}
			}

			else if( (topTube2X <= birdsX && birdsX <= topTube2X+topTube.getWidth()) || (topTube2X <= birdsX+bird[0].getRegionWidth() && birdsX + bird[0].getRegionWidth() <= topTube2X+topTube.getWidth()) ){
				if(birdsY+bird[0].getRegionHeight() > topTube2Y || birdsY < topTube2Y-tubeDistance2){
					setGameOver();
				}
			}
			drawScore();

			//hard tag
			if(hardMode)
				batch.draw(hard,screenWidth/10,screenHeight*9/10,screenWidth/5,screenWidth/5*hard.getHeight()/hard.getWidth());
		}

		batch.end();
		++ clock;
		++ time;
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	public void setGameOver(){
		gameState = 2;
		if(!hardMode) {
			highScore = preferences.getInteger("High score", 0);

			if(highScore < score)
			{
				highScore = score;
				preferences.putInteger("High score", score);
				preferences.flush();
			}
		}
		else {
			highScore = preferences.getInteger("Hard High score", 0);
			if(highScore < score)
			{
				highScore = score;
				preferences.putInteger("Hard High score", score);
				preferences.flush();
			}
		}
	}
	public void setGameRunning(){
		clock = 0;
		gameState = 1;
		topTube1X = screenWidth;
		topTube2X = topTube1X+(screenWidth/3*2)+ topTube.getWidth();
		topTube1Y = random.nextInt(screenHeight/3)+screenHeight*11/20;
		topTube2Y = random.nextInt(screenHeight/3)+screenHeight*11/20;
		record = false;
		birdsX = (screenWidth-birdWidth)/2;
		birdsY = (screenHeight-birdHeight)/2;
		velocity = 0;
		score = 0;
		time = 0;
		hardButton.remove();
		startButton.remove();
		tubeDistance = bird[0].getRegionHeight()*6;
		tubeDistance2 = bird[0].getRegionHeight()*6;
	}

	public void drawScore(){
		int buffer = score;
		int bits;
		double buffer2 = buffer;

		if(buffer == 0)
			bits = 1;
		else{
			bits = (int) (Math.floor(Math.log10(buffer2)) + 1);
		}
		int baseX = screenWidth/2+bits*(screenWidth/20+screenWidth/216)/2;

		int count = 1;
		do{
			batch.draw(numbers[buffer%10],baseX-count*(screenWidth/20+screenWidth/216),screenHeight*4/5,screenWidth/20,screenWidth/20*3/2);
			buffer /= 10;
			count++;
		}while (buffer>0);
	}

	public void drawScoreBoard(){
		int buffer = score;
		int bits;
		double buffer2 = buffer;

		if(buffer == 0)
			bits = 1;
		else{
			bits = (int) (Math.floor(Math.log10(buffer2)) + 1);
		}
		int baseX = screenWidth/2-scoreBoard.getWidth()*5/8+bits*(screenWidth/20+screenWidth/216)/2+scoreBoard.getWidth()/18;

		int count = 1;
		do{
			batch.draw(numbers[buffer%10],baseX-count*(screenWidth/20+screenWidth/216),screenHeight/2+scoreBoard.getHeight()/2,screenWidth/20,screenWidth/20*3/2);
			buffer /= 10;
			count++;
		}while (buffer>0);

		buffer = highScore;
		buffer2 = buffer;

		if(buffer == 0)
			bits = 1;
		else{
			bits = (int) (Math.floor(Math.log10(buffer2)) + 1);
		}
		baseX = screenWidth/2 + scoreBoard.getWidth()*5/8+bits*(screenWidth/20+screenWidth/216)/2-scoreBoard.getWidth()/18;

		count = 1;
		do{
			batch.draw(numbers[buffer%10],baseX-count*(screenWidth/20+screenWidth/216),screenHeight/2+scoreBoard.getHeight()/2,screenWidth/20,screenWidth/20*3/2);
			buffer /= 10;
			count++;
		}while (buffer>0);
	}

	public int oscillating(){
		return oscillator[ (time/8) % (oscillator.length)];
	}
}
