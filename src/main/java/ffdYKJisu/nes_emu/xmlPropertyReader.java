/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ffdYKJisu.nes_emu.domain.AddressingMode;
import ffdYKJisu.nes_emu.domain.uByte;

/**
 * Reads xml file for opcodes properties for printing purposes
 * @author Administrator
 */
public class xmlPropertyReader {
	public Document opCodeDoc;

	public xmlPropertyReader( String fileName ) {
		try {
			File file = new File( fileName );
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setIgnoringElementContentWhitespace( true );
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( file );
			doc.getDocumentElement().normalize();

			//System.out.println(" doc len : "+doc.getElementsByTagName("#text").getLength());
			NodeList root = doc.getChildNodes();
			// Children of root
			NodeList nodes = root.item( 0 ).getChildNodes();
			//doc.getDocumentElement().setIdAttribute("id", true);
			// This removes all the #text nodes caused by all the "\n"
			// in the original xml document
			//System.out.println("Looking thru " + nodes.getLength() + " nodes.");
			for ( int i = 0; i < nodes.getLength(); i++ ) {
				//System.out.println(i + ":" + nodes.item(i).getNodeName());
				if ( nodes.item( i ).getNodeName().toString().equals( "#text" ) )
					// System.out.print("we found a text!");
					nodes.item( i ).getParentNode().removeChild( nodes.item( i ) );
			}
			// Set id attribute for all opcodes
			for ( int i = 0; i < nodes.getLength(); i++ ) {
				//System.out.println(i + ":" + nodes.item(i).getNodeName());
				Element idNodes = (Element)nodes.item( i );
				//System.out.println(idNodes.getNodeName());
				//System.out.println("This node has id " + idNodes.getAttribute("id"));
				idNodes.setIdAttribute( "id", true );
			}

			this.opCodeDoc = doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getLength( uByte opCode ) {
		return this.getLength( opCode.toString().replaceAll( "^\\$", "" ) );
	}
	
	public int getLength( String opCode ) {

		opCode = new String( opCode.replaceAll( "^\\$", "" ) ); // remove leading $ if it exists
		//this.opCodeDoc.getE
		Node node = this.opCodeDoc.getElementById( opCode );
		Element e = (Element)node;
		if ( node == null )
			// System.err.println("Null node, opcode '" + opCode + "' not found.");
			throw new NullPointerException( "Op '" + opCode + "' doesn't exist." );
		else
			return Integer.valueOf( e.getAttribute( "length" ) );
	}

	public String getPrintName( String opCode ) {
		opCode = new String( opCode.replaceAll( "^\\$", "" ) ); // remove leading $ if it exists
		Node node = this.opCodeDoc.getElementById( opCode );
		Element e = (Element)node;
		if ( node == null )
			//System.err.println("Null node, opcode " + opCode + " not found.");
			throw new NullPointerException( "Op '" + opCode + "' doesn't exist." );
		else
			return e.getAttribute( "printName" );
	}

	public int getCycles (uByte opCode, boolean pageJumped, boolean branchTaken) {
		return this.getCycles(opCode.toString().replaceAll( "^\\$", ""),
			 pageJumped, branchTaken);
	}
	
	public int getCycles( String opCode, boolean pageJump, boolean branchTaken ) {
		opCode = new String( opCode.replaceAll( "^\\$", "" ) ); // remove leading $ if it exists
		Element e = this.opCodeDoc.getElementById( opCode );

		if ( e == null )
			throw new NullPointerException( "Op '" + opCode + "' doesn't exist." );
		else {
			int cycles = Integer.valueOf( e.getAttribute( "cycles" ) );
			int branchCycle = 0;
			int pageJumpCycle = 0;
			if ( !e.getAttribute( "onBranchCycle" ).isEmpty() && branchTaken )
				branchCycle = Integer.valueOf( e.getAttribute( "onBranchCycle" ) );
			if ( !e.getAttribute( "onPageJumpCycle" ).isEmpty() && pageJump )
				pageJumpCycle = Integer.valueOf( e.getAttribute( "onPageJumpCycle" ) );
			return cycles + branchCycle + pageJumpCycle;
		}
	}

	public AddressingMode getAddressingMode( String opCode ) {
		opCode = new String( opCode.replaceAll( "^\\$", "" ) ); // remove leading $ if it exists
		Element e = this.opCodeDoc.getElementById( opCode );
		if ( e == null )
			throw new NullPointerException( "Op '" + opCode + "' doesn't exist." );
		else {
			String addressingMode = e.getAttribute( "addressingMode" );
			return AddressingMode.get( addressingMode );
		}
	}

	public static void main( String[] args ) {
		xmlPropertyReader xml = new xmlPropertyReader( "NES_Opcodes_v3.xml" );
		NodeList root = xml.opCodeDoc.getChildNodes();
		StringBuffer sb = new StringBuffer();
		String op = "70";
		System.out.println( "printName " + xml.getPrintName( op ) );
		System.out.println( "length " + xml.getLength( op ) );
		System.out.println( "cycles " + xml.getCycles( op, false, false ) );
		System.out.println( "addressing mode " + xml.getAddressingMode( op ));
	/*
	for ( int i=0; i<256;i++) {
	String x = new String(String.format("%02X", i));
	// System.out.println(x);
	sb.append(xml.getPrintName(x) + " " + xml.getLength(x) + "\n");
	}
	
	System.out.println(sb.toString());
	
	System.out.println("len:" + xml.opCodeDoc.getChildNodes().item(0).getChildNodes().getLength());
	System.out.println("num bytes for 0x6A " +
	xml.getLength("6A"));
	 * */
	}
}