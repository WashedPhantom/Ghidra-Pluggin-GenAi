/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ritgenaipluggintest;

import java.awt.BorderLayout;

import javax.swing.*;

import docking.ActionContext;
import docking.ComponentProvider;
import docking.action.DockingAction;
import docking.action.ToolBarData;
import ghidra.app.ExamplesPluginPackage;
import ghidra.app.plugin.PluginCategoryNames;
import ghidra.app.plugin.ProgramPlugin;
import ghidra.framework.plugintool.*;
import ghidra.framework.plugintool.util.PluginStatus;
import ghidra.util.HelpLocation;
import ghidra.util.Msg;
import resources.Icons;
//ghidra method imports
import ghidra.program.model.address.Address;
import ghidra.program.util.ProgramLocation;
import ghidra.program.model.listing.Program;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.listing.Data;
import ghidra.program.model.symbol.Symbol;
/**
 * Provide class-level documentation that describes what this plugin does.
 */
//@formatter:off
@PluginInfo(
	status = PluginStatus.STABLE,
	packageName = ExamplesPluginPackage.NAME,
	category = PluginCategoryNames.EXAMPLES,
	shortDescription = "Plugin short description goes here.",
	description = "Plugin long description goes here."
)
//@formatter:on~
public class RITGenAiPlugginTestPlugin extends ProgramPlugin {

	MyProvider provider;

	/**
	 * Plugin constructor.
	 * 
	 * @param tool The plugin tool that this plugin is added to.
	 */
	public RITGenAiPlugginTestPlugin(PluginTool tool) {
		super(tool);

		// Customize provider (or remove if a provider is not desired)
		String pluginName = getName();
		provider = new MyProvider(this, pluginName);

		// Customize help (or remove if help is not desired)
		String topicName = this.getClass().getPackage().getName();
		String anchorName = "HelpAnchor";
		provider.setHelpLocation(new HelpLocation(topicName, anchorName));
	}

	@Override
	public void init() {
		super.init();

		// Acquire services if necessary
	}
	

	// If provider is desired, it is recommended to move it to its own file
	private static class MyProvider extends ComponentProvider {

		private JPanel panel;
		private JTextArea chatArea;
		private JTextField inputField;
		private JButton sendButton;
		private DockingAction action;
		private final RITGenAiPlugginTestPlugin plugin;
		
		//API client access
		private RITGenAITestAIClient aiClient = new RITGenAITestAIClient();
		
		public MyProvider(RITGenAiPlugginTestPlugin plugin, String owner) {
			super(plugin.getTool(), "RIT Test", owner);
			
			//allows for provider to query the plugin for information
			this.plugin = plugin;
					
			buildPanel();
			createActions();
			
		}
				
		// Customize GUI
		private void buildPanel() {
			panel = new JPanel(new BorderLayout());
			
			// Chat history
			 chatArea = new JTextArea();
		    chatArea.setEditable(false);
		    chatArea.setLineWrap(true);
		    chatArea.setWrapStyleWord(true);

		    JScrollPane scrollPane = new JScrollPane(chatArea);

		    // Bottom input area
		    JPanel inputPanel = new JPanel(new BorderLayout());

		    inputField = new JTextField();

		    sendButton = new JButton("Send");
		    //Pressing enter or clicking send will do the same thing
		    sendButton.addActionListener(e -> sendMessage());
		    inputField.addActionListener(e -> sendMessage());

		    inputPanel.add(inputField, BorderLayout.CENTER);
		    inputPanel.add(sendButton, BorderLayout.EAST);

		    panel.add(scrollPane, BorderLayout.CENTER);
		    panel.add(inputPanel, BorderLayout.SOUTH);

		    setVisible(true);
		    
		}

		// Customize actions
		private void createActions() {
			action = new DockingAction("My Action", getOwner()) {
				@Override
				public void actionPerformed(ActionContext context) {
					Msg.showInfo(getClass(), panel, "Custom Action", "Hello!");
				}
			};
			action.setToolBarData(new ToolBarData(Icons.ADD_ICON, null));
			action.setEnabled(true);
			action.markHelpUnnecessary();
			dockingTool.addLocalAction(this, action);
		}
		
		private String escapeJson(String text) {

		    return text
		            .replace("\\", "\\\\")
		            .replace("\"", "\\\"")
		            .replace("\n", "\\n")
		            .replace("\r", "");
		}
		
		
		private void sendMessage() {

		    String message = inputField.getText().trim();

		    if (message.isEmpty()) {
		        return;
		    }

		    chatArea.append("You: " + message + "\n\n");

		    inputField.setText("");

		    // AI response flow will go here
		    String context = plugin.buildContext();
		    String prompt =
		    	    "You are an expert reverse engineering assistant.\n\n" +
		    	    "Current Ghidra Context:\n" +
		    	    context +
		    	    "\n\n" +
		    	    "User Question:\n" +
		    	    message;
		  
		    prompt = escapeJson(prompt);
		    
		    String json =
		    		"{"
		    		+ "\"model\":\"qwen3:latest\","
		    		+ "\"messages\":["
		    		+ "{"
		    		+ "\"role\":\"system\","
		    		+ "\"content\":\"You are an expert reverse engineering assistant.\""
		    		+ "},"
		    		+ "{"
		    		+ "\"role\":\"user\","
		    		+ "\"content\":\"" + prompt + "\""
		    		+ "}"
		    		+ "]"
		    		+ "}";
		    
		    new Thread(() -> {

		        try {

		            String response =
		                    aiClient.send(json);

		            SwingUtilities.invokeLater(() -> {

		                chatArea.append("AI: ");
		                chatArea.append(response);
		                chatArea.append("\n\n");

		                chatArea.setCaretPosition(
		                    chatArea.getDocument().getLength());

		            });

		        }
		        catch (Exception ex) {

		            SwingUtilities.invokeLater(() -> {

		                chatArea.append(
		                    "AI: Error contacting server.\n");

		                chatArea.append(
		                    ex.getMessage());

		                chatArea.append("\n\n");

		            });

		        }

		    }).start();
		    
		    chatArea.setCaretPosition(chatArea.getDocument().getLength());
		}

		@Override
		public JComponent getComponent() {
			return panel;
		}
	}
	
	public class AIConfig {

	    public static String getApiKey() {
	        return System.getenv("AI_API_KEY");
	    }

	    public static String getEndpoint() {
	        return "https://api.genai.gccis.rit.edu/v1/chat/completions";
	    }

	    public static String getModel() {
	        return "qwen3:latest";
	    }
	}
	
	//Helper Methods for Ghidra information
	public Program getCurrentProgram() {
		return currentProgram;
	}
	
	public Address getCurrentAddress() {

	    if (currentLocation == null) {
	        return null;
	    }

	    return currentLocation.getAddress();
	}	
	
	public Function getCurrentFunction() {

		Address currentAddress = getCurrentAddress(); 
		
	    if (currentProgram == null || currentAddress == null) {
	        return null;
	    }

	    FunctionManager fm = currentProgram.getFunctionManager();

	    return fm.getFunctionContaining(currentAddress);
	}
	
	public String getCurrentFunctionName() {

	    Function function = getCurrentFunction();

	    if (function == null) {
	        return "No function selected";
	    }

	    return function.getName();
	}
	
	public String getCurrentSelection() {

	    if (currentProgram == null) {
	        return "No program loaded.";
	    }

	    if (currentSelection == null) {
	        return "No selection.";
	    }

	    Address start = currentSelection.getMinAddress();
	    Address end = currentSelection.getMaxAddress();

	    return "Selection: " 
	            + start 
	            + " - " 
	            + end;
	}
	
	public String getCurrentInstruction() {

	    if (currentProgram == null || currentLocation == null) {
	        return "No instruction selected.";
	    }

	    Address address = currentLocation.getAddress();

	    Instruction instruction =
	            currentProgram
	            .getListing()
	            .getInstructionAt(address);

	    if (instruction == null) {
	        return "No instruction at current address.";
	    }

	    return instruction.toString();
	}
	
	public String getCurrentData() {

	    if (currentProgram == null || currentLocation == null) {
	        return "No data selected.";
	    }

	    Address address = currentLocation.getAddress();

	    Data data =
	            currentProgram
	            .getListing()
	            .getDataAt(address);

	    if (data == null) {
	        return "No data at current address.";
	    }

	    return data.toString();
	}
	
	public String getCurrentSymbol() {

	    if (currentProgram == null || currentLocation == null) {
	        return "No symbol.";
	    }

	    Address address = currentLocation.getAddress();

	    Symbol symbol =
	            currentProgram
	            .getSymbolTable()
	            .getPrimarySymbol(address);

	    if (symbol == null) {
	        return "No symbol at address.";
	    }

	    return symbol.getName();
	}
	
	//For simplifying sendMessage()
	//chatArea.append(buildContext)
	public String buildContext() {

	    StringBuilder sb = new StringBuilder();

	    sb.append("Program:\n")
	      .append(currentProgram.getName())
	      .append("\n\n");

	    sb.append("Address:\n")
	      .append(getCurrentAddress())
	      .append("\n\n");

	    sb.append("Function:\n")
	      .append(getCurrentFunctionName())
	      .append("\n\n");

	    sb.append("Selection:\n")
	      .append(getCurrentSelection())
	      .append("\n\n");

	    sb.append("Instruction:\n")
	      .append(getCurrentInstruction())
	      .append("\n\n");

	    sb.append("Data:\n")
	      .append(getCurrentData())
	      .append("\n\n");

	    sb.append("Symbol:\n")
	      .append(getCurrentSymbol());

	    return sb.toString();
	}
}
