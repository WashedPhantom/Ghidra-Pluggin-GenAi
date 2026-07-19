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
//@formatter:on
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

		public MyProvider(Plugin plugin, String owner) {
			super(plugin.getTool(), "RIT Test", owner);
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
		
		private void sendMessage() {

		    String message = inputField.getText().trim();

		    if (message.isEmpty()) {
		        return;
		    }

		    chatArea.append("You: " + message + "\n\n");

		    inputField.setText("");

		    // AI response flow will go here
		    //String response = <call the AI>
		    chatArea.append("AI: Thinking...\n\n");
		    
		    chatArea.setCaretPosition(chatArea.getDocument().getLength());
		}

		@Override
		public JComponent getComponent() {
			return panel;
		}
	}
}
