##About this project [![Build Status](https://travis-ci.org/dannyflax/Microfluidics-Simulation.svg?branch=master)](https://travis-ci.org/dannyflax/Microfluidics-Simulation)
This project uses [JOGL](https://www.jogamp.org) and [JOGL Utilities](https://github.com/dannyflax/JOGL-Utilities/).

##What's going on here?
For part of my program during my second semester at The Ohio State University, I performed a "lab-on-a-chip" experiment where I tested the effect of salinity on the adhesian of yeast cells to a PDMS surface. The results failed to disprove our null hypothesis, but I thought it would be cool to create a little simulation to model the experiment, showing both the setup and the results. 

##Build Instructions
Clone or download the project onto your local machine. If you don't already have [gradle](https://gradle.org), you can run the project the wrapper and the following commands:
```
./gradlew build
./gradlew run
```
Alternatively, if you do have gradle, the following two commands will run quicker:
```
gradle build
gradle run
```

##Controls
At the start of the application, you can walk around and view 3D models that show parts of the experimental setup. Movement can be controlled using WASD for basic motion, U and J to fly up and down, and the mouse to rotate the camera. Pressing "G" will switch to the simulation view, where you can see a microscopic view of the chip and the yeast cells sitting inside. From there, you can control water flow, add salinity, and view the yeast shearing.

##Screenshots

![The Chip](https://github.com/dannyflax/Microfluidics-Simulation/blob/master/Screenshots/shot1.png)
![Tower and chip close](https://github.com/dannyflax/Microfluidics-Simulation/blob/master/Screenshots/shot2.png)
![Tower and chip far](https://github.com/dannyflax/Microfluidics-Simulation/blob/master/Screenshots/shot4.png)
![Yeast Shearing Simulation](https://github.com/dannyflax/Microfluidics-Simulation/blob/master/Screenshots/shot3.png)
