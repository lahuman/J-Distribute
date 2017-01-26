package kr.pe.lahuman.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class XMLParser {

	
	private Document document = null;
	private XPath xpath = XPathFactory.newInstance().newXPath();
	public XMLParser(String xmlPath) throws SAXException, IOException, ParserConfigurationException{
//		// XML Document 객체 생성
//		String xml = "<root><row><col1 id='c1'>값1</col1><col2 id='c2' val='val2'>값2</col2></row>"
//				+ "<row><col1 id='c3'>값3</col1><col2 id='c4'>값4</col2></row></root>";
//		InputSource is = new InputSource (new StringReader(xml));

		InputStream ism = new FileInputStream(xmlPath);
		InputSource is = new InputSource (ism);
		document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(is);
		ism.close();
	}
	
	
	
	public String getString(String patten) throws XPathExpressionException{
		
		// 값1 값3 이 출력됨

		// id 가 c2 인 Node의 val attribute 값 가져오기
		return (String) xpath.evaluate(patten, document,
				XPathConstants.STRING);
//		System.out.println(col2.getAttributes().getNamedItem("val")
//				.getTextContent());
//		// val2 출력
//
//		// id 가 c3 인 Node 의 value 값 가져오기
//		System.out.println(xpath.evaluate("//*[@id='c3']", document,
//				XPathConstants.STRING));
//		// 값3 출력
//		
	}
	
	public String getString(Node node, String patten) throws XPathExpressionException{
		
		// 값1 값3 이 출력됨
		
		// id 가 c2 인 Node의 val attribute 값 가져오기
		return (String) xpath.evaluate(patten, node,
				XPathConstants.STRING);
//		System.out.println(col2.getAttributes().getNamedItem("val")
//				.getTextContent());
//		// val2 출력
//
//		// id 가 c3 인 Node 의 value 값 가져오기
//		System.out.println(xpath.evaluate("//*[@id='c3']", document,
//				XPathConstants.STRING));
//		// 값3 출력
//		
	}
	
	public List<String> getList(String patten) throws XPathExpressionException{
		// NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
		NodeList cols = (NodeList) xpath.evaluate(patten, document,
				XPathConstants.NODESET);
		List<String> result = new ArrayList<String>();
		for (int idx = 0; idx < cols.getLength(); idx++) {
			result.add(cols.item(idx).getTextContent());
		}
		return result;
	}
	
	public List<String> getList(Node node, String patten) throws XPathExpressionException{
		// NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
		NodeList cols = (NodeList) xpath.evaluate(patten, node,
				XPathConstants.NODESET);
		List<String> result = new ArrayList<String>();
		for (int idx = 0; idx < cols.getLength(); idx++) {
			result.add(cols.item(idx).getTextContent());
		}
		return result;
	}
	
	public NodeList getNodeList(String patten) throws XPathExpressionException{
		// NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
		NodeList nodeList = (NodeList) xpath.evaluate(patten, document,
				XPathConstants.NODESET);
//		List<String> result = new ArrayList<String>();
//		for (int idx = 0; idx < cols.getLength(); idx++) {
//			result.add(cols.item(idx).getTextContent());
//		}
		return nodeList;
	}
	
	
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		
		
		XMLParser xp = new XMLParser("C:\\workspaces\\kepler_workspace\\DailyCheckList\\src\\kr\\pe\\lahuman\\SERVER_INFO.xml");
		NodeList result = xp.getNodeList("/root/server");
		for(int i=0; i<result.getLength(); i++){
			Node node = result.item(i);
			System.out.println(xp.getList(node, "./commands/command"));
		}
		// 인터넷 상의 XML 문서는 요렇게 생성하면 편리함.
		// Document document =
		// DocumentBuilderFactory.newInstance().newDocumentBuilder()
		// .parse("http://www.example.com/test.xml");

		// xpath 생성

		
	}
}
