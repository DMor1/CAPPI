# CAPPI
Custom Communication Arduino-PC Interfacing Protocol

This repository includes two sets of files. One is firmware that is running on an arduino board and the other is a Processing script which is executed on a windows computer. 

The software here was developed around January 2013 for the ASME SPDC Student Design Competition. The complete code is archived here for historical reasons or simply as a reference to those who choose to use it. 

The purpose of this code is to allow a PlayStation3 Controller to interface through bluetooth with a Windows PC, parse the commands using the processing script (processing in this case is a java framework) and wirelessly transmit those commands via an Xbee radio to an arduino board. The microcontroller then acts as a slave device to control the movements of the robot. 

The robot itself was equipped with multiple servo motors to control a claw and other pieces of hardware, in addition to DC motors which were manipulated through a motor controller. In addition, wireless cameras were used to view the surroundings of the robot.

Note:
I'm aware there are better ways to implement this (Firmata) and various other libraries. In addition, this code highly tailored to the specific robot which was built for the competition and is unlikely to work out of the box without modification.
