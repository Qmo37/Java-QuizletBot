# Java-QuizletBot

This is a simple Java program that can help you to automatically generate Quizlet flashcards from doc, docx, pdf, ppt, pptx, xls, xlsx files.

It has a built-in feature to convert the files to text and then generate the flashcards.

This program uses ChatGPT or Claude API to generate the questions and answers with a specific format.
Then we can use the generated text to create the flashcards.

We use maven to manage the dependencies and build the project.

We also provide a simple GUI for you to use this program.

## Features
- Convert doc, docx, pdf, ppt, pptx, xls, xlsx files to text
- Generate Quizlet flashcards from the text
- Use ChatGPT or Claude API to generate the questions and answers
- Provide a simple GUI for users

## Pre-requisites
### Java installed on your machine
### Maven installed on your machine (InteliJ IDEA has built-in Maven)
  #### For Linux
  ```
  sudo apt install maven
  ```
  #### For Mac
  ```
  brew install maven
  ```
  #### For Windows
  Download the maven from the official website and install it

### - Declare environment variable for Maven (Reference: https://ithelp.ithome.com.tw/articles/10227841)
  #### For Linux (For me, envrionment variable is already set)
  ```
  export PATH=$PATH:/usr/share/maven/bin
  ```
  #### For Mac
  ```
  export PATH=$PATH:/usr/local/Cellar/maven/3.8.1/bin
  ```
  #### For Windows
  Add the maven bin directory to the PATH

### API Key from ChatGPT or Claude API

## Usage
1. Download the files from the repository, using git clone or download the zip file
2. Navigate to the directory where the files are located

### Using the terminal
3. Run the following command to compile the program
```
nvm clean package
```
4. Run the following command to declare the environment variables for API Key
```
export ANTHROPIC_API_KEY=sk-ant-api03...(Contact us to get the API key or USE YOUR OWN KEY)
```
5. Run the following command to start the program
```
java -cp target/ai-chat-client-1.0-SNAPSHOT.jar com.aichat.ChatGUI
```

### Using the IDE (Not tested yet)
**InteliJ IDEA has built-in Maven**

3. Open the project in your favorite IDE
4. Declare the environment variables for API Key
5. Run the ChatGUI.java file
