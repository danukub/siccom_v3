# SICCOM - Spatial Interaction in Coral Reef Communities
## Version 3.0 - Climate change drives trait-shifts in coral reef communities
The program allows the simulation of spatial dynamics in a benthic coral reef community under various environmental settings in monthly iterations. Several coral species, with either massive or branching colony growth morphologies are implemented as individual colonies, which compete for space with each other as well as with macroalgae.


Coral species can be parametrised with respect to different life history characteristics, such as growth, reproduction, competitive abilities and susceptibilities to environmental conditions, such as extreme temperature events.


Life history characteristics of macroalgae, such as reproduction, growth, maximum size and mortality can also be parametrised in configuration files.




An article in which this model is applied and described in more detail can be found at:
-- link will be added asap -- 
<br/>
<br/>

## Wanna play?

<p>
  <img src="https://github.com/danukub/siccom_v3/blob/master/ReefView_siccom.png" alt="reef view pic" width="150" height="150"              align="left" > 
  <a href="https://github.com/danukub/siccom_v3/blob/master/siccom_os_v3.zip" 
  alt="Runnable program" />
    Runnable program
  </a> 
  to play with the simulation. 
  
  
  You can also alter model parameters and investigate their influence on the virtual coral community. They can either be changed in the running program by using respective tabs of the console panel, or you can directly adjust the config files within the "species" directory.
</p>

<br/>

### Interesting parameters to play with (environment config)
- bleachInterval (0 means no major bleaching events at all)
- longTermYears
- fragProb & fragRange
- breakageProb

### Run the program
1. Just uncompress the ZIP archive and make sure the "species" directoy is placed in the same directory as "siccom_os_v3.jar"
2.  - double-click the jar file

    - or use the command line (e.g. bash)
    ```bash
    $#: java -jar siccom_os_v3.jar
    ``` 

#### Important
- You have to have a Java Runtime Environment installed

<br/>

## Required JAR files to make it run in your favorite IDE
- Mason 16
- diva-0.3.jar
- j3dcore.jar   
- jcommon-1.0.16.jar     
- jhotdraw.jar  
- vecmath.jar
- iText-5.0.1.jar  
- j3dutils.jar  
- jfreechart-1.0.13.jar  
- jmf.jar
