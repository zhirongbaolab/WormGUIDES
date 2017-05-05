# WormGUIDES
Desktop version of the WormGUIDES iPad and Android apps.

Source code can be found at https://github.com/tangydoris/WormGUIDES.

This application uses JRE 1.8 version 77+. Source built using JRE 1.8 version 77 downloaded from http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html#jre-8u77-oth-JPR. At least update 77 is needed for transparency rendering of colors. Update 66 can be used, but only supports different transparencies for the shade white.<br>
The documentation for the shared URL paramters can be found in URLDocumentation.txt.
<br><br><br>



####About

WormGUIDES is a collaboration led by Drs. Zhirong Bao (MSKCC), Daniel Colon-Ramos (Yale), William Mohler (UConn) and Hari Shroff (NIH). For more information, visit our website at http://wormguides.org. 

The WormGUIDES app is developed and maintained by the laboratories of Dr. Zhirong Bao and Dr. William Mohler. Major contributors of the desktop app include Doris Tang (New York University), Braden Katzman (Columbia University) and Dr. Anthony Santella of the Bao Laboratory. For questions or comments contact support@wormguides.org.
<br><br><br>



####Notes on Eclipse Project Setup

#####Intellij Setup:
Downlaod IntelliJ Community edition at https://www.jetbrains.com/idea/download/<br>
Create an empty project<br>
Import the source code as a module in the newly created project<br>
Set up the git root for version control<br><br>

#####Eclipse Setup:
Download Eclipse for Java at http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/keplersr1<br>
Download the latest JDK at http://www.oracle.com/technetwork/java/javase/downloads/index.html<br>
Make sure environment variables point to the bin directory of the jdk (usually found in C:/Program Files/Java/jdk.../bin)<br>
If Eclipse cannot open, try to modify the eclipse.ini configuration file in the main eclipse directory<br>
Change the required java version in the argument -Dosgi.requiredJavaVersion from 1.7 to 1.8<br>
Make sure the -vm argument points to the correct javaw.exe for your Java 1.8+ version<br><br>

#####Project Setup:
Select File > Import > Existing Projects into Workspace<br>
Choose the Git directory you cloned and hit Finish<br><br>


#####If working with WormGUIDES:
JavaFX Runtime and Tooling is helpful in development this app in eclipse<br>
In Eclipse, go to Help > Install New Software...<br>
Insert the address<br> http://download.eclipse.org/efxclipse/updates-released/2.0.0/site into the Location field and hit OK<br>
Choose e(fx)clipse - install and pick the appropriate Eclipse IDE release<br>
Go through the wizard and restart Eclipse when finished<br>
(Steps taken from https://www.eclipse.org/efxclipse/install.html)<br><br>


#####If working with AceTree:
If there are many compiler errors, it is probably due to Eclipse not recognizing the dependencies in the jar subdirectory<br>
To fix this, right click on your project in the Package Explorer and go to Properties<br>
Select Java Build Path and choose the Libraries tab<br>
If there is a Java3D library listed, selected it and click Remove<br>
Select Add Library... > User Library > User Libraries... > New...<br>
Crease a new library with the name Java3D and hit OK<br>
Select the new Java3D library under Defined user libraries and click Add JARs > AceTree > jars<br>
Highlight all the jars in the directory and hit OK<br>
Add the new library to the project's build path and exit out of Properties<br>