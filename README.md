<p align="center">
    <img alt="GPTuessr" src="https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/ReadMePics/gptuessr.png" width="300" height="300"/> <br/>
</p>

# SoPra FS24 - GPTuessr

## Introduction
Welcome to the exciting world of GPTuessr, a cutting-edge online multiplayer game that blends the classic fun of drawing and guessing games with the latest in AI-driven art technology. Inspired by popular games like “skribbl,” GPTuessr offers a unique platform for friends to connect, laugh, and unleash their creativity from anywhere in the world. At the heart of GPTuessr is our innovative use of DALL-E, an advanced AI that generates stunning images in real-time based on player descriptions. This not only adds an inventive twist to the traditional drawing elements but also enriches the gaming experience by allowing players to interact with AI in a dynamic and entertaining way. Our game is designed to be more than just a pastime; it's a fusion of art and technology, creating a playground where imagination meets the digital canvas. With features like user authentication, sophisticated lobby management, and intuitive game logic, all powered by DALL-E and ChatGPT’s APIs, GPTuessr is crafted to be accessible and enjoyable across various devices. By choosing to implement GPTuessr as a web application, we ensure that no matter where you are or what device you use, the experience is seamless and engaging. Prepare to guess, giggle, and gaze at AI-generated art that brings your wildest descriptions to life. Join us at GPTuessr, where every game is an adventure in creativity and fun!

## Technologies
- [OpenAI API](https://platform.openai.com/docs/overview) - For image generation with [DALL-E](https://platform.openai.com/docs/guides/images/image-generation) and [ChatGPT](https://platform.openai.com/docs/guides/text-generation/chat-completions-api) for similary check between two sentences
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [React](https://reactjs.org/) - Frontend Framework
- [Gradle](https://gradle.org/) - Build Tool
- [stompjs](https://stomp-js.github.io/stomp-websocket/) - Websocket Library for the client
- [WebScoket](https://spring.io/guides/gs/messaging-stomp-websocket/) - Websocket Library fom Spring Boot for the server

## High-Level Components
#### GameController
The [GameController](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller/GameController.java) class communicates with the client side via endpoints, managing REST requests. It processes information exchanged with these endpoints through the GameService class.

#### GameService
The [GameService](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/GameService.java) class works in conjunction with the [GameController](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller/GameController.java) to manage game data and oversee the game process. It is responsible for handling the game logic and ensuring that the game runs smoothly.

#### DALL-E
The [DALL-E](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/game/dallE/DallE.java) class serves as a client to interact with the OpenAI DALL-E API to generate images based on the image description provided by the user. It sends requests to the API and processes the responses to provide images for the game.

#### ChatGPT
The [ChatGPT](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/game/chatGPT/ChatGPT.java) class is specifically engineered to facilitate interaction with OpenAI's ChatGPT model for the purpose of evaluating the similarity between textual inputs. This class leverages sophisticated prompt construction and API communication strategies to precisely assess and quantify the degree of similarity between two sentences.

#### Game
The [Game](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/game/Game.java) class orchestrates the setup and management of a game environment, designed to facilitate interactions and gameplay among multiple users.  It acts as a central hub, maintaining crucial game state information  and is also responsible for controlling the game's lifecycle, including starting the game, managing rounds, and tracking the progress of each game session

## Launch & Deployment
### Setup with your IDE of choice
Download your IDE of choice (e.g., [IntelliJ](https://www.jetbrains.com/idea/download/), [Visual Studio Code](https://code.visualstudio.com/), or [Eclipse](http://www.eclipse.org/downloads/)). Make sure Java 17 is installed on your system (for Windows, please make sure your `JAVA_HOME` environment variable is set to the correct version of Java).

### IntelliJ
If you consider to use IntelliJ as your IDE of choice, you can make use of your free educational license [here](https://www.jetbrains.com/community/education/#students).
1. File -> Open... -> SoPra server template
2. Accept to import the project as a `gradle project`
3. To build right click the `build.gradle` file and choose `Run Build`

### VS Code
The following extensions can help you get started more easily:
-   `vmware.vscode-spring-boot`
-   `vscjava.vscode-spring-initializr`
-   `vscjava.vscode-spring-boot-dashboard`
-   `vscjava.vscode-java-pack`

**Note:** You'll need to build the project first with Gradle, just click on the `build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot Dashboard_ extension if it already shows `soprafs24` and hit the play button to start the server. If it doesn't show up, restart VS Code and check again.

### Clone Repository
1. Clone the client-repository onto your local machine with the help of [Git](https://git-scm.com/downloads). You can find the client-repository information here [README.md](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-client)
```bash 
git clone https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-client.git
```
2. Then clone the server-repository 
```bash 
git clone https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server.git
```

## Building with Gradle
You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/).

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser.

### Test

```bash
./gradlew test
```

### Development Mode
You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

- The main branch is automatically mirrored onto Google Cloud App Engine via GitHub workflow

## API Endpoint Testing with Postman
We recommend using [Postman](https://www.getpostman.com) to test the API Endpoints.

## Debugging
If something is not working and/or you don't know what is going on. We recommend using a debugger and step-through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command), do the following:

1. Open Tab: **Run**/Edit Configurations
2. Add a new Remote Configuration and name it properly
3. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
4. Press `Shift + F9` or the use **Run**/Debug "Name of your task"
5. Set breakpoints in the application where you need it
6. Step through the process one step at a time

## Testing
Have a look here: https://www.baeldung.com/spring-boot-testing

## Create Releases
- [Follow Github documentation](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository)

## Roadmap
Like in every game there are always features that can be added in the future. Some of them could be the following:
#### Different Game modes
- A feature that could be implemented would be to change the evaluator for the image description guess. Instead of ChatGPT being the evaluator and distributing the points, there could be a gamemode where the host has to rank the descriptions by correctness or all players in the lobby have to vote for the best description.
- Since Open AI's ChatGPT is constantly evolving and can also recognize images, there could be a mode where one competes against ChatGPT instead of real people.

#### Social Integration
- Enable players to link their social media accounts to the game. Implementing this functionality will necessitate that new developers incorporate APIs for social media sharing.

## Authors and Acknowledgment
### Authors
This game was brought to life by a group of five dedicated students who designed and implemented it as part of the Software Engineering Lab course at the University of Zurich during the spring semester of 2024.

- Roger Jeasy Bavibidila- [rogerjeasy](https://github.com/rogerjeasy)
- Eduard Gash - [eduard54](https://github.com/eduard54)
- Nicolas Huber - [nicolasHuber3](https://github.com/nicolasHuber3)
- Nicolas Schärer - [NlcoIas](https://github.com/NlcoIas)
- Eric Rudischhauser - [Ericode99](https://github.com/Ericode99)
### Acknowledgment
We are grateful to Fengjiao Ji ([feji08](https://github.com/feji08)) for her consistently helpful guidance and insightful comments.

## License
This project is licensed under the Apache License 2.0 - see the [LICENSE](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/LICENSE) file for details