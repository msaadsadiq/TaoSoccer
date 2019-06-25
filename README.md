# TaoSoccer

This document contains the description and implementation details of the ECE674 course
project. The project aims to build a multi agent soccer team using the Tao of Soccer 1.3
engine and compete with other players in the class.

The report is organized as follows. Section 1 contains the step-by- step description on how my
code can be included in the server console and compiled. Actual file names and field are
selected to minimize any ambiguity. Although my team was built using Linux OS but the
description is written with Windows in mind because of the wide majority of users running
windows. Section 2 contains the detail of how the Multi-Agent system is designed and its
functionality, collaborations and interactions. This section provides details about the server
architecture and what objects and controls were utilized to improve on the basic AI team.
Section 3 provides in-depth explanation of my methodology and technique to improve on the
AI basic team. Flowcharts and field diagrams are used to provide explanation that can be
used in the future to build a better and a more sophisticated team. Section 4 contains the
documented program listings to provide a hard copy of the code with all the necessary
comments.

![alt-text](https://github.com/msaadsadiq/Tao_of_Soccer/1.jpeg)

Section 1 - How to compile and run the program

1. Copying my code to the server
Copy the folder &#39;dolphins&#39; to the soccer client path i.e.
C:&gt; cd \Documents\NetBeansProjects\soccer-src- 1.3\src\soccer\client\
this will copy my team into the project folder.
2. Running NetBeans IDE
Open NetBeansIDE, or if it is already opened, you will see a new package in the top right
pane called soccer.client.dolphins.
3. Buliding the Project
Build the project again so my team is also included in the soccer.jar file
a. Goto NetBeans IDE
b. Right click on the project name TOS
c. Click Build. Or alternatively press F11
Check if there is any exceptions in the build process, otherwise in the output box you should
see
build:
Building soccer.jar file
BUILD SUCCESSFUL (total time: 0 seconds)
4. Setting the path for java
Open the Command Prompt
C:&gt; cd \mywork
This makes C:\mywork the current directory.
C:\mywork&gt; dir
This displays the directory contents. You should see filenamehere.java among the files.
C:\mywork&gt; set path=%path%;C:\Program Files\Java\jdk1.5.0_09\bin
This tells the system where to find JDK programs.
C:\mywork&gt; javac filenamehere.java

This runs javac.exe, the compiler. You should see nothing but the next system prompt...
C:\mywork&gt; dir
javac has created the filenamehere.class file. You should see filenamehere.java and filenamehere.class among
the files.
C:\mywork&gt; java filenamehere
This runs the Java interpreter. You should then see your program output. While running the
program, the java path should be set in every new instance of the command prompt. This is
online required while working with Microsoft windows, while linux and mac OSX doesnt
require setting java paths.

5. Display the soccer field
Open the Command Prompt
C:&gt;
Change the directory to the program build
C:&gt; cd \Documents\NetBeansProjects\soccer-src- 1.3\build/
C:\Documents\NetBeansProjects\soccer-src- 1.3\build&gt;
This makes my current working directory as the program build folder. Now compile and run the java program for
the soccer monitor
C:\Documents\NetBeansProjects\soccer-src- 1.3\build&gt; java -cp soccer.jar
soccer.monitor.SoccerMonitor
This will run the program and show the soccer field.

6. Run th AI team or the opponent team
Open a new Command Prompt
C:&gt;
Change the directory to the program build
C:&gt; cd \Documents\NetBeansProjects\soccer-src- 1.3\build/
C:\Documents\NetBeansProjects\soccer-src- 1.3\build&gt;

This makes my current working directory as the program build folder. Now compile and run the java program for
the AI soccer team.
C:\Documents\NetBeansProjects\soccer-src- 1.3\build&gt; java -cp soccer.jar soccer.client.ai.AIPlayers
-r 11
This will run the AI team and place 11 players on the right side on the soccer field.

7. Run the dolphins Team
Open a new Command Prompt
C:&gt;
Change the directory to the program build
C:&gt; cd \Documents\NetBeansProjects\soccer-src- 1.3\build/
C:\Documents\NetBeansProjects\soccer-src- 1.3\build&gt;
This makes my current working directory as the program build folder. Now compile and run the java program for
the AI soccer team.
C:\Documents\NetBeansProjects\soccer-src- 1.3\build&gt; java -cp soccer.jar
soccer.client.dolphins.AIPlayers -l 11

This will run the dolphins team and place 11 players on the left side on the soccer field.
