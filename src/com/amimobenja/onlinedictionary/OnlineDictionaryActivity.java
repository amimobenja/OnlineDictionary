package com.amimobenja.onlinedictionary;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OnlineDictionaryActivity extends Activity {	
	EditText textWord;
	TextView WordDef;
	static String DefWord;
	
	private InputStream OpenHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
		
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		}
		catch (Exception ex) {
			Log.d("Networking", ex.getLocalizedMessage());
			throw new IOException("Error connecting");
		}
		return in;
	}
	
	private String WordDefinition(String word) {
		InputStream in = null;
		String strDefinition = "";
		try {
			in = OpenHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word=" +
		     word);
			Document doc = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				doc = db.parse(in);
			} catch (ParserConfigurationException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			}
			doc.getDocumentElement().normalize();
			
			//---retrieve all the <Definition> elements---
			NodeList definitionElements = doc.getElementsByTagName("Definition");
			
			//---iterate through each <Definition> elements---
			for (int i=0; i<definitionElements.getLength(); i++) {
				Node itemNode = definitionElements.item(i);
				if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
					//---Convert the Definition node into an Element---
					Element definitionElement = (Element) itemNode;
					
					//---get all the <WordDefinition> elements under the <Definition> element---
					NodeList wordDefinitionElements = (definitionElement).getElementsByTagName("WordDefinition");
					
					strDefinition = "";
					
					//---iterate through each <WordDefinition> element---
					for (int j=0; j<wordDefinitionElements.getLength(); j++) {
						//---convert a <WordDefinition> node into an Element---
						Element wordDefinitionElement = (Element) wordDefinitionElements.item(j);
						
						//---get all the child nodes under the <WordDefinition> element---
						NodeList textNodes = ((Node) wordDefinitionElement).getChildNodes();
						
						strDefinition += ((Node) textNodes.item(0)).getNodeValue() + ". \n";
					}
				}
			}
		} catch (IOException e1) {
			Log.d("NetworkingActivity", e1.getLocalizedMessage());
		}
		//---return the definitions of the word---
		return strDefinition;
	}
	
	private class AccessWebServiceTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			return WordDefinition(urls[0]);
		}
		
		protected void onPostExecute(String result) {	
			if (result.matches("")) {
				WordDef.setText("Not Defined");
			} else {
				WordDef.setText(result);
				
			}			
		}
	}
	
	public void onClickCheck(View view) {
		textWord = (EditText) findViewById(R.id.edTxtWord);
		DefWord = textWord.getText().toString();
		
		//---access a Web Service using GET---
		if (DefWord.matches("")) {
			Toast.makeText(getBaseContext(), "Can't Search an Empty value", Toast.LENGTH_LONG).show();
		} else {
			new AccessWebServiceTask().execute(DefWord);
		}	
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		requestWindowFeature(Window.FEATURE_LEFT_ICON);		
		setContentView(R.layout.main);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);
		
		WordDef = (TextView) findViewById(R.id.texViewWord);
		WordDef.setMovementMethod(new ScrollingMovementMethod());
		
	}
}
