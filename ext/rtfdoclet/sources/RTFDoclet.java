import java.io.StringReader;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;
import com.sun.javadoc.*;

/**
 * RTF Doclet.<p>
 * This class is a plugin for javadoc producing documentation under a RTF format. The file
 * generated is usualy incorporated into a Microsod Word document.
 *
 * By default this doclet will write into a javaDocumentation.rtf file. The name of this file
 * could be override if the "-filename" parameter is provided to javadoc. Example: "javadoc
 * -doclet RTFDoclet -filename report.rtf *.java".
 *
 * Note:
 * <ul>
 * <li> The comment found by javadoc are parsed into a simple html parser. This parser understand
 * the P,UL/LI, BR, A tags.
 * <li> The produce RTF file could not be well read by some RTF reader (for example Wordpad)
 * due to the array that are present in the document.
 * </ul>
 *
 * @author Nicolas Zin (nzin@yahoo.com) Feb 1999
 * @author David Cozens ((David@cozens.freeserve.co.uk) June 1999
 * @author Joe Panko (jp99@sprintmail.com) January 2000
 * @author Thierry Bodhuin ((Thierry.Bodhuin@support-externe.space.alcatel.fr) April 2000
 */
public class RTFDoclet {
	// to specify if we have to print all arguments on one line
	private boolean collapseParameters = false;
	// the output stream
	private PrintWriter file;
	
	/**
	 * Constructor.<BR>
	 * The constructor could raise a FileNotFoundException if it doesn't manage to open
	 * the RTF file to write into.
	 */
	public RTFDoclet(String filenameP, boolean collapseParametersP) throws FileNotFoundException {
		file  = new PrintWriter(new FileOutputStream(filenameP));
		collapseParameters=collapseParametersP;
		
		file.println("{\\rtf\\pard\\plain\\ql");
		
		// fix the font table
		file.println("{\\fonttbl");
		file.println(" {\\f1\\fswiss\\fcharset0\\fprq2{\\*\\panose 020b0604020202020204}Arial;}");
		file.println(" {\\f2\\froman\\fcharset238\\fprq2 Times New Roman CE;}");
		file.println("}");
		
		// fix the color table (4 color: black, blue, green, red)
		file.println("{\\colortbl;\\red0\\green0\\blue0;\\red0\\green0\\blue192;\\red0\\green128\\blue0;\\red192\\green0\\blue64;}");
		
		// set the font size
		file.println("\\f1\\fs18");
		
		// new section begin at a new page
		file.println("\\sbkpage");
	}
	
	/**
	 * Method called at the end to put the rtf end block
	 */
	public void closeFile() {
		// close the '{\rtf' block
		file.println("}");
		file.close();
	}
	
	/**
	 * Method called by Javadoc to recognize the -filename parameter
	 */
	public static int optionLength(String option) {
		if(option.equals("-filename")) {
			return 2;
		}
		if (option.equals("-collapseParameters")) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Main procedure
	 */
	public static boolean start(RootDoc rootP) {
		String filenameP = "javaDocumentation.rtf";
		String optionsP[][]=rootP.options();
		boolean collapseParametersP = false;
		int iP;
		for (iP=0; iP<optionsP.length; iP++) {
			if ((optionsP[iP][0].equals("-filename")) && (optionsP[iP].length>1)) {
				filenameP = optionsP[iP][1];
			}
			if (optionsP[iP][0].equals("-collapseParameters")) {
				collapseParametersP=true;
			}
		}
		try {
			RTFDoclet docP = new RTFDoclet(filenameP, collapseParametersP);
			
			if (rootP.specifiedClasses()!=null)
				docP.listClasses(rootP);
			
			if (rootP.specifiedPackages()!=null)
				docP.listPackages(rootP);
			
			docP.closeFile();
		} catch (FileNotFoundException eP) {
			System.out.println("java.io.FileNotFoundException caught!");
			System.out.println("Impossible to write the file \""+filenameP+"\"");
			System.out.println("Be sure that the location is correct or the file is not already used by another application");
		}
		return true;
	}
	
	/**
	 * Generate documentation for a particular package
	 */
	public void listPackages(RootDoc rootP) {
		PackageDoc packagesP[] = rootP.specifiedPackages();
		
		for (int iP=0; iP<packagesP.length; iP++) {
			printPackage(packagesP[iP]);
			ClassDoc classesP[] = packagesP[iP].allClasses();
			for (int jP=0; jP<classesP.length; jP++) {
				printClass(classesP[jP]);
			}
			file.println("\\sect ");
		}
	}
	
	/**
	 * Print the name of a package
	 */
	public void printPackage(PackageDoc packageP) {
		file.println("{\\f1\\fs28 \\b Package "+ packageP+"\\par}");
	}
	
	public void listClasses(RootDoc rootP) {
		ClassDoc classesP[] = rootP.specifiedClasses();
		
		for (int jP=0; jP<classesP.length; jP++) {
			printClass(classesP[jP]);
		}
	}
	
	/**
	 * Print documentation for a particular class
	 */
	public void printClass(ClassDoc classeP) {
		file.println("{\\par}");
		file.print("{");

		String strClass = " class ";
		if( classeP.modifiers().endsWith("interface") ) {
			// Don't print the word "class" if this is an interface
			strClass = " ";
		}
		file.print("\\f1\\fs24 "+classeP.modifiers()+strClass+"{\\b "+classeP+"}" );
		
		// Top level interfaces don't have a superclass
		if( null != classeP.superclass()) {
			// by the way don't print superclass of top level class.
			if (classeP.superclass().toString().equals("java.lang.Object")==false) {
				file.print(" extends {\\f1\\cb1\\cf2 "+classeP.superclass()+"}");
			}
		}
		
		ClassDoc implementationsP[] = classeP.interfaces();
		for (int kP=0; kP<implementationsP.length; kP++) {
			if (kP==0) file.print(" implements ");
			file.print("{\\f1\\cb1\\cf2 "+implementationsP[kP]+"}");
			if (kP!=implementationsP.length-1) file.print(", ");
		}
		
		file.print("\\par\\par}");
		
		// Comment about the whole class
		file.print("{");
		printComment(classeP,"class "+classeP.name());
		file.println("\\par}");
		
		
		ConstructorDoc constructeursP[] = classeP.constructors();
		for (int kP=0; kP<constructeursP.length; kP++) {
			if (kP==0) {
				//file.println("\\trowd \\trgaph70 \\cellx1536 \\cellx9142\\pard\\ql ");
				//file.println("\\nowidctlpar\\widctlpar\\intbl\\adjustright {Constructors\\cell ");
				file.println("{Constructors }\\par ");
			}
			else
			{
				file.println("\\line ");
			}
			
			printElement(constructeursP[kP]);
			printComment(constructeursP[kP], "method "+classeP.name()+"."+constructeursP[kP].name());
			
			if (kP==constructeursP.length-1)
			{
				//file.println("\\cell }\\pard \\nowidctlpar\\widctlpar\\intbl\\adjustright {\\row }\\pard\\ql ");
				////file.println("\\par ");
		   }
			else
			{
			   file.println("\\par ");
			}
		}
		
		MethodDoc methodesP[] = classeP.methods();
		for (int kP=0; kP<methodesP.length; kP++) {
			if (kP==0) {
				//file.println("\\trowd \\trgaph70 \\cellx1536 \\cellx9142\\pard\\ql ");
				//file.println("{\\nowidctlpar\\widctlpar\\intbl\\adjustright Methods\\cell ");
				file.println("\\par \\outlinelevel1 { Methods }\\par ");

			}
			else
			{
				file.println("\\line ");
			}
			
			printElement(methodesP[kP]);
			printComment(methodesP[kP], "method "+classeP.name()+"."+methodesP[kP].name());
			
			if (kP==methodesP.length-1)
			{
				//file.println("\\cell }\\pard \\nowidctlpar\\widctlpar\\intbl\\adjustright {\\row }\\pard\\ql ");
				////file.println("\\par ");
			}
			else
			{
			   file.println("\\par ");
			}
		}
		
		FieldDoc fieldsP[] = classeP.fields();
		for (int kP=0; kP<fieldsP.length; kP++) {
			if (kP==0) {
				//file.println("\\trowd \\trgaph70 \\cellx1536 \\cellx9142\\pard\\ql ");
				//file.println("\\nowidctlpar\\widctlpar\\intbl\\adjustright {Fields\\cell ");
				file.println("{Fields }\\par ");

			}
			else
			{
				file.println("\\line ");
			}
			
			printElement(fieldsP[kP]);
			printComment(fieldsP[kP], "method "+classeP.name()+"."+fieldsP[kP].name());
			
			if (kP==fieldsP.length-1)
			{
				//file.println("\\cell }\\pard \\nowidctlpar\\widctlpar\\intbl\\adjustright {\\row }\\pard ");
				////file.println("\\par } ");
			}
			else
			{
			   file.println("\\par ");
			}
		}
		file.println("{\\par\\par}");
	}
	
	/**
	 * Parse and print a comment found
	 */
	public void printComment(Doc documentP, String fullNameP) {
		// print general comment
		if (!documentP.commentText().equals("")) {
			
			try {
				HtmlComment htmlP = new HtmlComment(new StringReader(documentP.commentText()));
				htmlP.parse();
				//file.println(htmlP.getResult()+"\\par ");
				printParagraph(htmlP.getResult()+"\\line ");
			} catch (ParseException eP) {
				
				System.out.println("Parse error in the comment of "+fullNameP+":");
				System.out.println(documentP.commentText());
				//file.println(documentP.commentText()+"\\par ");
				printParagraph(documentP.commentText()+"\\line ");
			} catch (TokenMgrError eP) {
				System.out.println("Parse error in the comment of "+fullNameP+":");
				System.out.println(documentP.commentText());
				//file.println(documentP.commentText()+"\\par ");
				printParagraph(documentP.commentText()+"\\line ");
			}
		}
		
		printParamTag(documentP,"@param","Parameters");
		printTag(documentP,"@return","Returns");
		printParamTag(documentP,"@throws","Throws");
		printTag(documentP,"@see","See Also");
		printTag(documentP,"@since","Since");
		printTag(documentP,"@version","Version");
	}
	
	/**
	 * Print a normal tag
	 */
	public void printTag(Doc documentP, String tagNameP,String logicalNameP) {
		// print tags
		Tag tagsP[] = documentP.tags(tagNameP);
		for (int iP=0;iP<tagsP.length;iP++) {
			if (iP==0) file.println("{\\f1\\cb1\\cf3 \\b "+logicalNameP+"} \\line ");
			//file.println("\\tab "+tagsP[iP].text()+" \\par ");
			printParagraph("\\tab "+tagsP[iP].text()+" \\line ");
		}
	}
	
	/**
	 * Print a 'parameter' tag. The difference with the normal is that the string found
	 * is separate into the header (normaly the name of the parameter) and the body (the
	 * explanation linked to the parameter)
	 */
	public void printParamTag(Doc documentP, String tagNameP,String logicalNameP) {
		// print tags
		Tag tagsP[] = documentP.tags(tagNameP);
		for (int iP=0;iP<tagsP.length;iP++) {
			if (iP==0) file.println("{\\f1\\cb1\\cf3 \\b "+logicalNameP+"} \\line ");
			
			int separator=tagsP[iP].text().indexOf(" ");
			String begining="";
			String ending ="";
			if (separator>=0) {
				begining=tagsP[iP].text().substring(0,separator);
				ending=tagsP[iP].text().substring(separator);
			};
			//file.println("\\tab "+begining+" - "+ending+" \\par ");
			printParagraph("\\tab "+begining+" - "+ending+" \\line ");
		}
	}
	
	/**
	 * Method used to print the name of a method and its parameters
	 */
	public void printElement(ProgramElementDoc elementP) {
		if (elementP instanceof ExecutableMemberDoc) {
			ExecutableMemberDoc memberP = (ExecutableMemberDoc) elementP;

			String returnType = " ";
			if( memberP instanceof MethodDoc ) {
				MethodDoc method = (MethodDoc)memberP;
				Type aType = method.returnType();
				if( null != aType ) {
					returnType = " " + aType.qualifiedTypeName() + aType.dimension() + " ";
				}
			}
			file.print("{\\f1\\cb1\\cf2 \\b "+elementP.modifiers()+returnType+elementP.name()+"(");
			
			Parameter[] parameters = memberP.parameters();
			for(int parNo = 0; parNo < parameters.length; parNo++) {
			    if(parNo != 0) {
    			    file.print(", ");
				}
				Type thisType = parameters[parNo].type();
				file.println();
				
				if (!collapseParameters) {
				    file.print("\\line\\tab ");
				}
				
			    file.print(thisType.typeName()+thisType.dimension()+" "+parameters[parNo].name());
			}
			file.println(")} \\line ");
		}
      else if (elementP instanceof FieldDoc) {
         FieldDoc fieldP = (FieldDoc) elementP;
         file.println("{\\f1\\cb1\\cf2 \\b "+fieldP.modifiers()
                        +" "+fieldP.type()+ " "+ fieldP.name()+"} \\line ");
      }
      else {
			file.println("{\\f1\\cb1\\cf2 \\b "+elementP.modifiers()+" "+ elementP.name()+"} \\line ");
		}
	}
	
	private void printParagraph(Object o) {
	    int nextSpace;
	    String s = o.toString().trim();
		
		nextSpace = s.indexOf((int)' ');
		
		while(nextSpace != -1) {
		    
			String sub = s.substring(0, nextSpace+1);
			s = s.substring(nextSpace).trim();
    		nextSpace = s.indexOf((int)' ');
    		file.print(sub);
		}
		file.println(s);
	}
	
}
