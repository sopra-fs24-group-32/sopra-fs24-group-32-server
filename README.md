# SoPra FS24 - GPTuessr

## Introduction
Welcome to the exciting world of GPTuessr, a cutting-edge online multiplayer game that blends the classic fun of drawing and guessing games with the latest in AI-driven art technology. Inspired by popular games like “skribbl,” GPTuessr offers a unique platform for friends to connect, laugh, and unleash their creativity from anywhere in the world. At the heart of GPTuessr is our innovative use of DALL-E, an advanced AI that generates stunning images in real-time based on player descriptions. This not only adds an inventive twist to the traditional drawing elements but also enriches the gaming experience by allowing players to interact with AI in a dynamic and entertaining way. Our game is designed to be more than just a pastime; it's a fusion of art and technology, creating a playground where imagination meets the digital canvas. With features like user authentication, sophisticated lobby management, and intuitive game logic, all powered by DALL-E and ChatGPT’s APIs, GPTuessr is crafted to be accessible and enjoyable across various devices. By choosing to implement GPTuessr as a web application, we ensure that no matter where you are or what device you use, the experience is seamless and engaging. Prepare to guess, giggle, and gaze at AI-generated art that brings your wildest descriptions to life. Join us at GPTuessr, where every game is an adventure in creativity and fun!

## Technologies
- [DALL-E API](https://platform.openai.com/docs/guides/images/image-generation) - Image Generation API
- [ChatGPT API](https://platform.openai.com/docs/guides/text-generation/chat-completions-api) - ChatGPT API for Similary Check
- [Spring Boot](https://spring.io/projects/spring-boot) - Backend Framework
- [React](https://reactjs.org/) - Frontend Framework
- [Gradle](https://gradle.org/) - Build Tool
- [stompjs](https://stomp-js.github.io/stomp-websocket/) - Websocket Library for the client
- [WebScoket](https://spring.io/guides/gs/messaging-stomp-websocket/) - Websocket Library fom Spring Boot for the server

## High-Level Components
[GameController](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller/GameController.java)
[GameService](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service/GameService.java)
[DALL-E](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/game/dallE/DallE.java)
[ChatGPT](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/game/chatGPT/ChatGPT.java)
[Game](https://github.com/sopra-fs24-group-32/sopra-fs24-group-32-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/game/Game.java)

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

## Illustrations

## Roadmap

## Authors and Acknowledgment
### Authors
This game was brought to life by the dedicated efforts of students who designed and implemented it as part of the Software Engineering Lab course at the University of Zurich during the spring semester of 2024.

- Roger Jeasy Bavibidila- [rogerjeasy](https://github.com/rogerjeasy)
- Eduard Gash - [eduard54](https://github.com/eduard54)
- Nicolas Huber - [nicolasHuber3](https://github.com/nicolasHuber3)
- Nicolas Schärer - [NlcoIas](https://github.com/NlcoIas)
- Eric Rudischhauser - [Ericode99](https://github.com/Ericode99)
### Acknowledgment
We are grateful to Fengjiao Ji ([feji08](https://github.com/feji08)) for her consistently helpful guidance and insightful comments.

## License
